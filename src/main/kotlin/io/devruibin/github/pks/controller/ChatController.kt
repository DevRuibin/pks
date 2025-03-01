package io.devruibin.github.pks.controller

import jakarta.annotation.Resource
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class ChatController {
    @Resource
    private lateinit var chatModel: OllamaChatModel

    @GetMapping("/chat")
    fun chat(@NotNull @NotBlank @RequestParam("prompt") prompt: String): ChatResponse {
        return chatModel.call(Prompt(prompt))
    }
}