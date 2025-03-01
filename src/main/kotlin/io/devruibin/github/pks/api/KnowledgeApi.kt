package io.devruibin.github.pks.api

import io.devruibin.github.pks.api.req.KnowledgeRequestBody
import org.springframework.web.bind.annotation.RequestBody

interface KnowledgeAPI{
    fun getKnowledge(@RequestBody(required = true) prompt: KnowledgeRequestBody): String
    fun addKnowledge(@RequestBody(required = true) prompt: KnowledgeRequestBody): String
}