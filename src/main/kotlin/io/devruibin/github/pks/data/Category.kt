package io.devruibin.github.pks.data

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "category_list")
data class Category (
    @Id
    val id: Long?,
    val name: String
)