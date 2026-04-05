package com.therohankumar.audio.filters

data class FilterConfig(
    val tremolo: TremoloParams? = null,
    val vibrato: VibratoParams? = null,
    val rotation: RotationParams? = null,
    val distortion: DistortionParams? = null,
    val echo: EchoParams? = null,
    val reverb: ReverbParams? = null,
    val karaoke: KaraokeParams? = null,
    val bassboost: Float? = null,   // gain in dB, -6 to +6
    val nightcore: Boolean = false,
    val vaporwave: Boolean = false
) {
    fun hasAnyFilter(): Boolean =
        tremolo != null || vibrato != null || rotation != null ||
        distortion != null || echo != null || reverb != null ||
        karaoke != null || bassboost != null || nightcore || vaporwave
}

data class TremoloParams(val frequency: Float = 4.0f, val depth: Float = 0.75f)
data class VibratoParams(val frequency: Float = 4.0f, val depth: Float = 0.5f)
data class RotationParams(val hz: Float = 0.2f)
data class DistortionParams(
    val sinOffset: Float = 0f,
    val cosOffset: Float = 0f,
    val tanOffset: Float = 0f,
    val scale: Float = 1f,
    val offset: Float = 0f
)
data class EchoParams(val delay: Float = 0.3f, val decay: Float = 0.5f)
data class ReverbParams(val roomSize: Float = 0.5f, val damping: Float = 0.5f)
data class KaraokeParams(val level: Float = 1.0f, val monoLevel: Float = 1.0f)
