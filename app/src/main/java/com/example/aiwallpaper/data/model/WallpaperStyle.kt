package com.example.aiwallpaper.data.model

enum class WallpaperStyle(val label: String, val promptSuffix: String) {
    AMOLED(
        label = "AMOLED",
        promptSuffix = "pure black AMOLED background, deep space aesthetic, dark theme with high-contrast neon glowing accents, perfect for OLED displays, striking and dramatic"
    ),
    ANIME(
        label = "Anime",
        promptSuffix = "anime art style, vibrant saturated colors, detailed manga-inspired illustration, Japanese animation aesthetic, cel-shaded, beautiful character design"
    ),
    MINIMAL(
        label = "Minimal",
        promptSuffix = "minimalist design, clean geometric shapes, subtle muted color palette, modern aesthetic, elegant use of negative space, less is more"
    ),
    NATURE(
        label = "Nature",
        promptSuffix = "photorealistic nature landscape, serene scenery, cinematic natural lighting, golden hour, breathtaking high-detail scenery, ultra-realistic photography style"
    )
}
