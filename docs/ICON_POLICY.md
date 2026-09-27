# Launcher icon policy

Every Android application module must define both `android:icon` and `android:roundIcon` in its application manifest.

Each app must ship a default launcher resource plus an adaptive icon for API 26+.

Instrumented tests must assert that the installed package exposes a non-zero launcher icon resource. This is a release-blocking requirement for every current and future microapp.
