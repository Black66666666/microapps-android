# TAVI UI Design System

Status: selected visual direction for the whole micro-app family.

This document is the source of truth for UI decisions in `microapps-android`. Screens and components in individual apps must follow this file and the shared implementation in `core/designsystem`. App-specific deviations require an explicit update here first.

## 1. Brand idea

**TAVI — Pocket Objects**

The apps should feel like small useful digital objects: calm, tactile, compact, recognizable and focused on one real-world task.

The product family must be recognizable even when app names are hidden.

### Permanent brand signatures

1. **Signature shape** — a soft rounded form with one concave corner / indentation.
2. **Signature dot** — a small high-contrast lime sphere/dot. It represents state, action and continuity.
3. **Quiet editorial layout** — warm neutral background, strong typography, large whitespace, restrained visual hierarchy.
4. **One tactile hero object per primary screen** — 3D is functional emphasis, not decoration.
5. **App accent color** — each app has one own accent, while the TAVI lime dot remains common across the family.

## 2. Selected direction

Selected reference direction: **Quiet Editorial / Pocket Objects**.

The current concept render is a direction reference, not a pixel-perfect mockup. Implementation must preserve the principles above while adapting to Android ergonomics and accessibility.

### What we explicitly reject

- neon/glassmorphism as the family identity;
- dark sci-fi dashboards as the default UI;
- rainbow gradients on common controls;
- decorative 3D on every card/button;
- excessive shadows, glow or animation;
- every app inventing its own component language;
- copying a single Pinterest/Dribbble/Behance reference.

## 3. Core palette

Canonical machine-readable values live in `docs/ui/tavi-tokens.json`.

- Warm background: `#F7F3EA`
- Raised surface: `#FFFDF8`
- Graphite text/surface: `#20211F`
- Secondary text: `#73746F`
- Hairline/border: `#E7E1D7`
- TAVI lime: `#B9F500`

### App accents

Initial mapping:

- Screenshot Inbox: orange `#FF8252`
- BuyTomorrow: blue `#3E87F7`
- MeetingMeter: violet `#8A5CE6`

Other apps receive one accent each, selected to remain distinct inside the same warm-neutral system. Accent colors must not replace the TAVI lime brand marker.

## 4. Shape language

### Base radius

- Small control: 14 dp
- Standard card/control: 20 dp
- Large surface: 28 dp
- Hero object silhouette: custom soft geometry with one concave corner

### Signature concave corner

The concave corner is the family-specific geometry. It should appear selectively in:

- app launcher icon composition;
- hero object;
- selected brand surfaces;
- onboarding / empty states;
- branded splash / family identification.

Do not force the concave shape into every ordinary list row.

## 5. Signature dot

The dot is always visually simple and must remain readable at small size.

### Static use

- launcher icons;
- TAVI mark;
- active navigation state;
- key status indicator;
- hero object.

### Motion use

The dot can communicate four states:

1. Rest — ready / inactive.
2. Motion — an action is happening.
3. Progress — dot moves along a short defined track or object edge.
4. Result — dot settles into the final position.

Motion must be short, physical and calm. No bouncing for decoration.

## 6. Typography

Use Android-native, highly legible sans-serif typography. Prefer a single family across all apps.

Hierarchy:

- Display: large, short editorial statement; max 2–4 lines.
- Title: app/action context.
- Body: concise explanatory copy.
- Label: controls and metadata.

Rules:

- sentence case;
- no all-caps UI except tiny brand/metadata labels;
- avoid dense text blocks;
- avoid excessive bold weights;
- headings should carry the visual character more than decorative graphics.

## 7. Layout

Default visual rhythm:

- 24 dp horizontal screen padding;
- 24–32 dp section spacing;
- 12–16 dp internal card spacing;
- generous whitespace around the hero object;
- primary action visually isolated from secondary actions.

The family should feel editorial rather than dashboard-heavy.

## 8. Surfaces and depth

The UI is mostly flat/light with restrained depth.

Use:

- warm neutral background;
- near-white cards;
- subtle 1 dp hairline borders;
- very soft low-opacity shadow only where hierarchy needs it;
- tactile 3D hero object rendered/constructed separately.

Do not use glossy glass panels as the default surface.

## 9. Components

Shared components belong in `core/designsystem`.

Required common components:

- `TaviTheme`
- `TaviScreen`
- `TaviTopBar`
- `TaviPrimaryButton`
- `TaviSecondaryButton`
- `TaviCard`
- `TaviTextField`
- `TaviChip`
- `TaviStatusDot`
- `TaviAppMark`
- `TaviHeroObjectFrame`
- `TaviSectionTitle`

App screens should not locally recreate colors, radii or common controls.

## 10. App icon family

Every launcher icon follows the same composition:

1. soft rounded base;
2. one app-specific tactile object;
3. one small TAVI dot;
4. one app accent color;
5. no text inside the icon.

Icons must still read as a family when displayed together.

## 11. Per-app object metaphor

Current mapping:

- Screenshot Inbox — a small stack/inbox of saved cards.
- MatchChoice — two opposing/meeting soft pieces with a selection state.
- WhoBringsWhat — a set of assignable tokens.
- BuyTomorrow — a waiting capsule / object held in pause.
- MeetingMeter — a tactile circular meter / recessed dial.
- WorthIt — value/balance object.
- BorrowBack — paired linked objects indicating out/return.
- BoxQR — container object with embedded code plane.
- Refill — fill-level vessel.
- TurnKeeper — moving token passed between participants.

These metaphors may evolve, but every app must have exactly one primary object metaphor.

## 12. Accessibility and Android rules

Brand styling must not reduce usability.

- minimum touch target 48 dp;
- text contrast must meet WCAG AA where applicable;
- color is never the only status signal;
- motion respects reduced-motion preferences where supported;
- 3D hero content must not block core controls;
- layouts must survive font scaling and narrow screens.

## 13. Repository workflow

GitHub is the UI source of truth.

Order of change:

1. update this document/tokens if a design rule changes;
2. update `core/designsystem`;
3. update app modules;
4. run unit tests, lint, instrumented-test compilation, APK builds and emulator smoke tests;
5. only then merge.

A screenshot or generated concept image alone never overrides this specification.

## 14. Current status

The visual direction is selected. The next implementation step is to replace the existing neon/glass shared design system with the TAVI light tactile system while preserving existing app behavior and tests.
