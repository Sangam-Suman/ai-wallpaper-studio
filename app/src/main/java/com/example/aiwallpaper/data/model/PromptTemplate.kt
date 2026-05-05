package com.example.aiwallpaper.data.model

data class PromptTemplate(val emoji: String, val label: String, val prompt: String)

val PROMPT_TEMPLATES = listOf(
    PromptTemplate("🌃", "Lofi Study", "Lofi study room at night, warm lamp, books, rain on window, cozy aesthetic"),
    PromptTemplate("🌆", "Cyberpunk Tokyo", "Cyberpunk Tokyo rain-soaked street at night, neon signs, reflections in puddles, fog"),
    PromptTemplate("🌌", "Space Nebula", "Vivid cosmic nebula with swirling gases and stars, deep space, purple blue teal hues"),
    PromptTemplate("🌊", "Ocean Sunset", "Golden sunset over calm ocean, warm orange pink sky, peaceful silhouette"),
    PromptTemplate("🏔️", "Mountain Dawn", "Snow-capped mountain peaks at golden dawn, dramatic misty valleys, epic sky"),
    PromptTemplate("🌸", "Sakura Dream", "Cherry blossom path in Kyoto, soft pink petals falling, dreamy soft light"),
    PromptTemplate("🔥", "Lava Art", "Abstract flowing molten lava, dark background, red orange yellow glowing gradients"),
    PromptTemplate("🌿", "Enchanted Forest", "Enchanted forest, bioluminescent plants and mushrooms, fireflies, mystical purple fog"),
    PromptTemplate("🦋", "Butterfly Garden", "Colorful butterflies in golden hour garden, bokeh, magical warm light"),
    PromptTemplate("🌙", "Moon Lake", "Full moon over misty lake at midnight, reflections, ethereal blue atmosphere"),
)
