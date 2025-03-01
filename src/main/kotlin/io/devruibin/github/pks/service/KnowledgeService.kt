package io.devruibin.github.pks.service

import io.devruibin.github.pks.api.req.KnowledgeRequestBody

interface KnowledgeService {
    fun getKnowledge(prompt: KnowledgeRequestBody): String
    fun addKnowledge(prompt: KnowledgeRequestBody): String
}