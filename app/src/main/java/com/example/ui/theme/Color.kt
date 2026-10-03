package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Futuristic Dark Palette (Base requirement)
val BackgroundDark = Color(0xFF07090B)           // Base near-black background
val BackgroundSecondary = Color(0xFF0B0F12)       // Secondary background
val SurfaceCard = Color(0xFF11161A)               // Card surface
val SurfaceElevated = Color(0xFF151B20)           // Elevated surface
val SurfaceHigherElevated = Color(0xFF1A2127)     // Higher elevated surface
val SurfaceActive = Color(0xFF1F2933)             // Active / highlighted surface

// Subtle cool-gray/blue-gray borders
val BorderSubtle = Color(0x1F94A3B8)              // ~12% opacity border
val BorderMedium = Color(0x3394A3B8)              // ~20% opacity border
val BorderActive = Color(0x52818CF8)              // Subtle lavender border for focus/active

// Primary Text & Muted Hierarchy
val TextPrimary = Color(0xFFF8FAFC)               // Near-white
val TextSecondary = Color(0xFF94A3B8)             // Muted cool-gray
val TextTertiary = Color(0xFF64748B)              // Low-contrast gray

// Primary Accent (Warm Amber / Gold)
val AmberPrimary = Color(0xFFF59E0B)              // Warm amber / gold
val AmberPrimaryDark = Color(0xFFFBBF24)
val AmberOnPrimary = Color(0xFF07090B)
val AmberOnPrimaryDark = Color(0xFF07090B)
val AmberPrimaryContainer = Color(0xFF261D0C)
val AmberPrimaryContainerDark = Color(0xFF291E0A)
val AmberOnPrimaryContainer = Color(0xFFFDE68A)
val AmberOnPrimaryContainerDark = Color(0xFFFDE68A)
val AmberGlow = Color(0x2EF59E0B)

// Secondary Accent (Futuristic Lavender / Blue)
val LavenderAccent = Color(0xFF818CF8)            // Futuristic lavender
val LavenderPrimary = Color(0xFF6366F1)
val LavenderPrimaryContainer = Color(0xFF1E1F3B)
val LavenderOnPrimaryContainer = Color(0xFFE0E7FF)
val LavenderGlow = Color(0x2B818CF8)
val BlueAccent = Color(0xFF38BDF8)                // Futuristic cyan/blue

// Semantic Colors
val ChapterReadGreen = Color(0xFF10B981)          // Subtle green success
val ChapterReadGreenGlow = Color(0x2E10B981)
val ChapterPinnedGold = Color(0xFFF59E0B)         // Warm gold
val ChapterUnreadBorder = Color(0x1FFFFFFF)
val FlameStreak = Color(0xFFF97316)               // Streak orange
val ErrorRed = Color(0xFFEF4444)                  // Subtle error red
val ErrorRedContainer = Color(0xFF2A1215)

// Legacy compatibility aliases to avoid breaking any callers
val SlateSecondary = LavenderAccent
val SlateOnSecondary = Color(0xFF07090B)
val SlateSecondaryContainer = Color(0xFF181F26)
val SlateOnSecondaryContainer = Color(0xFFE2E8F0)

val SlateSecondaryDark = LavenderAccent
val SlateOnSecondaryDark = Color(0xFF07090B)
val SlateSecondaryContainerDark = Color(0xFF181F26)
val SlateOnSecondaryContainerDark = Color(0xFFE2E8F0)

val EmeraldTertiary = ChapterReadGreen
val EmeraldOnTertiary = Color(0xFFFFFFFF)
val EmeraldTertiaryContainer = Color(0xFF0D281E)
val EmeraldOnTertiaryContainer = Color(0xFFA7F3D0)

val EmeraldTertiaryDark = ChapterReadGreen
val EmeraldOnTertiaryDark = Color(0xFF07090B)
val EmeraldTertiaryContainerDark = Color(0xFF0D281E)
val EmeraldOnTertiaryContainerDark = Color(0xFFA7F3D0)

val BackgroundLight = Color(0xFF07090B)
val SurfaceLight = Color(0xFF11161A)
val SurfaceVariantLight = Color(0xFF151B20)
val OnSurfaceLight = TextPrimary
val OnSurfaceVariantLight = TextSecondary
val OutlineLight = BorderSubtle

val SurfaceDark = SurfaceCard
val SurfaceVariantDark = SurfaceElevated
val OnSurfaceDark = TextPrimary
val OnSurfaceVariantDark = TextSecondary
val OutlineDark = BorderSubtle


