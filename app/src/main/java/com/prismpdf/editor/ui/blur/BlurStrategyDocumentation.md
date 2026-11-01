# PrismPDF Blur & RenderEffect Strategy

## Multi-Tiered Blur Architecture

### Tier 1 — Android 12+ (API 31+)
Hardware-accelerated pipeline execution via `RenderEffect.createBlurEffect`.
No bitmap allocation overhead; renders directly from the GPU framebuffer.

### Tier 2 — Legacy Fallback (API < 31)
Offline scale-down canvas extraction:
1. Capture underlying app windows at 1/8th scale factor
2. Apply horizontal/vertical box-blur shader or matrix transform
3. Cache result as drawing brush bitmap
4. Scale back up at render time with hardware acceleration

## Memory Isolation Rules
- No reallocation inside standard loop layers or `drawWithContent` passes
- All structural memory components cached within state tracking slots
- Frame buffer recycling for the downscaled bitmap on legacy tiers

## Interpolation Continuity
Splash animation vector progress curves use explicit float transformations — no integer rounding.
Target value mapping ensures smooth scaling sequences without layout jittering.

## Hardware Drawing Context Enforcement
All composition layers maintain access to the hardware-accelerated drawing buffer context:
`android:hardwareAccelerated="true"` declared in the manifest for all activities.
