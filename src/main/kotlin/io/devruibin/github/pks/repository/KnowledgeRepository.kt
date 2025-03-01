package io.devruibin.github.pks.repository

import io.devruibin.github.pks.data.Knowledge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface KnowledgeRepository: JpaRepository<Knowledge, Long> {
    fun findKnowledgeByUserIdAndCategory(userId: String, category: String): Knowledge?

    @Modifying
    @Query("UPDATE knowledge k SET k.content = :newContent WHERE k.userId = :userId AND k.category = :category")
    fun updateCategoryByUser(userId: String, category: String, newContent: String): Int


    @Modifying
    @Query("INSERT INTO knowledge (userId, category, content) VALUES (:userId, :category, :content)")
    fun insertKnowledge(userId: String, category: String, content: String): Int

}