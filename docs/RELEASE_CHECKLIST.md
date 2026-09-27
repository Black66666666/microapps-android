# Release checklist

Every application must pass before an APK is delivered:

- launcher icon and round icon configured;
- adaptive icon available on API 26+;
- clean debug build;
- unit tests;
- Android lint;
- instrumented tests compiled;
- clean install and smoke test on Android emulator;
- persistence/restart path checked when the app stores data;
- permissions checked where required;
- sharing, reminders or links checked where used;
- no visual-system rollout during the active design freeze.
