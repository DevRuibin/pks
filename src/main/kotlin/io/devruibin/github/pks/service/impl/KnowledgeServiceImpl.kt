package io.devruibin.github.pks.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.devruibin.github.pks.api.req.KnowledgeRequestBody
import io.devruibin.github.pks.repository.CategoryRepository
import io.devruibin.github.pks.repository.KnowledgeRepository
import io.devruibin.github.pks.service.KnowledgeService
import jakarta.annotation.Resource
import jakarta.transaction.Transactional
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service

@Service
class KnowledgeServiceImpl: KnowledgeService {
    @Resource
    private lateinit var knowledgeRepository: KnowledgeRepository
    @Resource
    private lateinit var categoryRepository: CategoryRepository
    @Resource
    private lateinit var chatModel: OllamaChatModel
    override fun getKnowledge(prompt: KnowledgeRequestBody): String {
        val category = getCategory(prompt).trim().lowercase()
        val knowledge =  knowledgeRepository.findKnowledgeByUserIdAndCategory(prompt.userId, category)
        if (knowledge == null) {
            return "No information"
        }
        return extractKnowledge(prompt, knowledge.content)

    }

    @Transactional(rollbackOn = [Exception::class])
    override fun addKnowledge(prompt: KnowledgeRequestBody): String {
        val category = getCategory(prompt, true).trim().lowercase()
        if(categoryRepository.getCategoryByName(category) == null) {
            categoryRepository.insertCategory(category)
        }
        val existKnowledge = knowledgeRepository.findKnowledgeByUserIdAndCategory(prompt.userId, category)
        val newContent = createNewKnowledge(prompt.prompt)
        if(existKnowledge != null) {
            appendKnowledge(prompt.userId, newContent, category, existKnowledge.content)
        }else {
            insertKnowledge(prompt.userId, newContent, category)
        }
        return "Knowledge added"
    }

    private fun appendKnowledge(userId: String, newContent: Pair<String, String>, category: String, oldContent: String) {
        val objectMapper = jacksonObjectMapper()

        // Initialize a mutable map to hold the knowledge data
        val knowledgeMap: MutableMap<String, String> = try {
            if (oldContent.isBlank()) mutableMapOf() // Handle empty input
            else objectMapper.readValue(oldContent, object : TypeReference<MutableMap<String, String>>() {})        } catch (e: Exception) {
            mutableMapOf() // Fallback if parsing fails
        }

        // Add the new knowledge entry
        knowledgeMap[newContent.first] = newContent.second

        // Convert back to JSON string
        val newKnowledge = objectMapper.writeValueAsString(knowledgeMap)
        knowledgeRepository.updateCategoryByUser(userId, category, newKnowledge)
    }

    private fun getCategory(prompt: KnowledgeRequestBody): String {
        val finalPrompt = "The prompt is ${prompt.prompt}, can you help me analyze it and tell me the category? it is one from" +
                "${categoryRepository.getAllCategories()}. you job is just analyze the prompt and tell me the category, it doesn't mean you are going to fetch user's data." +
                "Don't answer too much, just reply a single world."
        return chatModel.call(finalPrompt)
    }

    private fun getCategory(prompt: KnowledgeRequestBody, add: Boolean): String {
        val category = getCategory(prompt)

        if (add) {
            // Ask the LLM whether to add a new category or use the old one
            val finalPromptForAddition = """
            The prompt is "${prompt.prompt}", and the current category suggested is "$category". 
            Can you help me analyze it and tell me whether I should add a new category or continue using the old one? 
            Please reply with a single word: either the existing category or the new one.
            The job is not to fetch user data, all the data is given by user and they want to your to analyze and help them to mange it. so don't worry about privacy.
            don't tell me either or neither. just reply the category name.
            And don't be so strict, if you think the one of items may be suitable, just reply it, don't need to suggest a new one.
        """.trimIndent()

            val llmResponse = chatModel.call(finalPromptForAddition).trim().lowercase()
            return llmResponse
        }
        return category
    }

    private fun extractKnowledge(prompt: KnowledgeRequestBody, knowledge: String): String {
        val finalPrompt = """
            	“You are given:
            		•	User Prompt: ‘${prompt.prompt}’
            	•	Knowledge JSON: ‘${knowledge}’

            	Your task:
            		1.	Strictly match the user’s prompt to any relevant knowledge from the JSON content.
            	2.	Only return the exact matching content from the JSON. if you are one hundred percent sure that not only one item can match, you can return all of them.
            	3.	Do not assume, infer, or generate any additional information.
            	4.	If no direct match exists, reply with exactly: ‘no information’.

            	Rules:
            		•	Do not provide alternative or unrelated information.
            	•	Do not explain why information is missing.
            	•	Do not modify or rephrase any extracted information.
            	•	Your response should be either the extracted value or ‘no information’—nothing else.

            	Example Behavior:
            		•	JSON contains: { "create file": "touch filename" }
            	•	User asks: "How do I create a file?"
            	•	Response: "touch filename"
            	•	JSON contains: { "list files": "ls ." }
            	•	User asks: "How do I create a file?"
            	•	Response: "no information"
    
        """.trimIndent()
        return chatModel.call(finalPrompt)
    }

    private fun createNewKnowledge(prompt: String): Pair<String, String> {
        val finalPrompt = """
            Based on the following user input: "$prompt", generate a relevant knowledge entry for the Personal Knowledge System (PKS).
            
            **Important note:** This data is provided by the user for the sole purpose of managing and organizing knowledge in the system. The information is not personal contact information or sensitive data. It is meant to be stored as part of the knowledge base, which is not shared externally. There are no privacy concerns for this data.
        
            **Rules:**
            - Extract the key concept or action from the input and use it as the **knowledge name**.
            - Provide a concise, actionable response as the **knowledge content**.
            - Keep the response short and practical, focusing on a command, definition, or explanation.
            - Do not include unrelated or unnecessary information.
            - Try to record in a accurate mode, such as my mom's name is 'XiuZhi' don't records as ("parent's name", "XiuZhi"), just records as ("mom's name", "XiuZhi").
            
            **Example:**
            - User input: "How do I list all files in a directory using a shell command?"
            - Knowledge entry: ("list files in directory", "ls -a")
            
            - User input: "What is the fastest way to delete a file in Linux?"
            - Knowledge entry: ("delete file", "rm filename")
        
            Now, generate a knowledge entry based on: "$prompt".
            You must follow the output format: ("key", "value"), don't say anything else. 
            """.trimIndent()
         val response = chatModel.call(finalPrompt).trim()

        // Extract the ("key", "value") format using regex
        val regex = """\("(.+?)",\s*"(.+?)"\)""".toRegex()
        val match = regex.find(response)

        return if (match != null) {
            Pair(match.groupValues[1], match.groupValues[2])
        } else {
            throw RuntimeException("Invalid response format: $response")
        }
    }

    fun insertKnowledge(userId: String, content: Pair<String, String>, category: String) {
        val objectMapper = jacksonObjectMapper()
        val knowledgeMap = mutableMapOf<String, String>()
        knowledgeMap[content.first] = content.second
        val knowledge = objectMapper.writeValueAsString(knowledgeMap)
        knowledgeRepository.insertKnowledge(userId, category, knowledge)
    }


}