# Microapps Android

Monorepo for a portfolio of small Android applications used to validate product hypotheses quickly.

## Strategy

- Android first.
- Each app is a standalone product with its own `applicationId` and user value.
- Shared infrastructure lives in `core/`.
- Apps are developed and released in waves.
- Market signal decides which apps receive further investment.

## UI source of truth

The whole portfolio uses the **TAVI — Pocket Objects** visual system.

- Design rules: [`docs/UI_DESIGN_SYSTEM.md`](docs/UI_DESIGN_SYSTEM.md)
- Design tokens: [`docs/ui/tavi-tokens.json`](docs/ui/tavi-tokens.json)
- Vector brand mark: [`docs/ui/tavi-mark.svg`](docs/ui/tavi-mark.svg)
- Shared Android implementation: `core/designsystem`

App modules must consume the shared design system instead of inventing local colors, shapes, radii or common components.

## Wave 1

1. Screenshot Inbox
2. MatchChoice
3. WhoBringsWhat
4. BuyTomorrow
5. MeetingMeter

## Planned portfolio

See `docs/PORTFOLIO.md` once Wave 1 scaffolding is merged.
