package io.devruibin.github.pks.api.req

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class KnowledgeRequestBody(@NotNull @NotBlank val prompt: String,
                                @NotNull @NotBlank val userId: String
)
