package io.devruibin.github.pks.controller

import io.devruibin.github.pks.api.KnowledgeAPI
import io.devruibin.github.pks.api.req.KnowledgeRequestBody
import io.devruibin.github.pks.service.KnowledgeService
import jakarta.annotation.Resource
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class KnowledgeController: KnowledgeAPI  {
    @Resource
    private lateinit var knowledgeService: KnowledgeService

    @PostMapping("/knowledge")
    override fun getKnowledge(@RequestBody(required = true) prompt: KnowledgeRequestBody): String {
        return knowledgeService.getKnowledge(prompt)
    }
    @PostMapping("/knowledge/add")
    override fun addKnowledge(@RequestBody(required = true) prompt: KnowledgeRequestBody): String {
        return knowledgeService.addKnowledge(prompt)
    }

}