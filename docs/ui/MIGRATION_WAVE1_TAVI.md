# Wave 1 TAVI migration

This migration moves all five Wave 1 applications from the archived neon/glass UI to the active TAVI Pocket Objects design system.

Migrated apps:

- Screenshot Inbox
- MatchChoice
- WhoBringsWhat
- BuyTomorrow
- MeetingMeter

The migration preserves app logic and test tags while replacing shared theme, surfaces, buttons, text fields, chips, status indicators, top bars, Android system chrome and MeetingMeter's custom neon meter.

Validation gate before merge:

1. unit tests;
2. Android lint;
3. instrumented-test APK compilation;
4. all five debug APK builds;
5. Android emulator connected tests.
