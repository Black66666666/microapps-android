# Scroll Receipt decisions

## D-001 — iOS excluded from first version
Do not ship an estimated video count based on screen time.

## D-002 — Gate 0A before full MVP
Do not start backend, History, Receipt or Global Scroll Index implementation until physical-device measurement proves the core detector.

## D-003 — automatic measurement UX
Normal counting has no Start button. Once Accessibility is enabled, the service processes supported target-app events independently from the Scroll Receipt activity. Diagnostic Start buttons only create controlled test baselines.

## D-004 — shared repository design system
Scroll Receipt uses `core/designsystem` so its interface stays consistent with the rest of the microapps portfolio.

## D-005 — SDK during Gate 0A
The module currently uses API 35 to match the monorepo CI. Before Google Play release it must be raised to the then-required target API and policy checks repeated.
