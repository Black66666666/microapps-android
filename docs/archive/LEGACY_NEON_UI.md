# Legacy UI — Neon / Glass direction

Status: **ARCHIVED / DO NOT USE FOR NEW WORK**.

This file records the previous visual direction so it is not lost, while making clear that it is no longer the product UI standard.

## Previous direction

The old shared design system used:

- dark navy backgrounds;
- neon cyan / blue / purple / pink accents;
- glass-like cards;
- gradient buttons;
- glowing decorative backgrounds;
- components such as `NeonBackdrop`, `GlassCard`, `GradientButton`, `NeonTextField` and `NeonChip`.

The implementation currently remains in:

`core/designsystem/src/main/java/com/microapps/designsystem/DesignSystem.kt`

It is kept temporarily only because existing Wave 1 apps still depend on it. Removing or moving that source file before the TAVI migration would break builds.

## Replacement

The active UI source of truth is:

`docs/UI_DESIGN_SYSTEM.md`

Brand: **TAVI — Pocket Objects**

Direction: **Quiet Editorial / tactile objects / warm neutral surfaces / signature concave form / lime TAVI dot**.

## Removal rule

Once all app modules are migrated to the TAVI implementation in `core/designsystem` and CI + emulator tests are green, the legacy neon-specific names and code should be deleted rather than preserved in production code.
