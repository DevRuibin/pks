package io.devruibin.github.pks.repository

import io.devruibin.github.pks.data.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CategoryRepository: JpaRepository<Category, Long> {
    fun getCategoryByName(name: String): Category?

    @Query("SELECT * FROM category_list", nativeQuery = true)
    fun getAllCategories(): List<Category>

    @Modifying
    @Query("INSERT INTO category_list (name) VALUES (:name)", nativeQuery = true)
    fun insertCategory(name: String): Int
}