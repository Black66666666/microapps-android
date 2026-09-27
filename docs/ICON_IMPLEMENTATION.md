# Launcher icon implementation

For every application module:

- `res/mipmap-anydpi/ic_launcher.xml` provides a fallback launcher icon;
- `res/mipmap-anydpi-v26/ic_launcher.xml` provides the adaptive icon for Android 8+;
- `res/drawable/ic_launcher_foreground.xml` contains app-specific foreground artwork;
- the application manifest explicitly sets both `android:icon` and `android:roundIcon`;
- an instrumented test verifies the installed package exposes a non-zero icon resource.

Final brand artwork can replace these resources later without changing package structure or application logic.
