# Legacy UI — Neon / Glass direction

Status: **ARCHIVED / REMOVED FROM PRODUCTION UI**.

This file records the previous visual direction so it is not lost. It is not an active implementation reference.

## Previous direction

The old shared design system used:

- dark navy backgrounds;
- neon cyan / blue / purple / pink accents;
- glass-like cards;
- gradient buttons;
- glowing decorative backgrounds;
- components such as `NeonBackdrop`, `GlassCard`, `GradientButton`, `NeonTextField` and `NeonChip`.

## Historical implementation

Before the TAVI migration, the old implementation lived in:

`core/designsystem/src/main/java/com/microapps/designsystem/DesignSystem.kt`

Wave 1 used those neon-specific APIs directly. The TAVI migration removed the neon constants and component API from production code and migrated all five Wave 1 apps to the `Tavi*` shared components.

## Replacement

The active UI source of truth is:

`docs/UI_DESIGN_SYSTEM.md`

Brand: **TAVI — Pocket Objects**

Direction: **Quiet Editorial / tactile objects / warm neutral surfaces / signature form / lime TAVI dot**.

The active Android implementation lives in:

`core/designsystem/src/main/java/com/microapps/designsystem/DesignSystem.kt`

Do not restore the legacy neon API. If a historical comparison is needed, use Git history.
