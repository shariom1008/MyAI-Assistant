AURIX LIVE UNDERWATER BACKGROUND PATCH

This patch keeps the existing AurixOriginalUi hierarchy, buttons, icons,
voice button, navigation, service logic, and callbacks unchanged.
It adds one visual-only procedural animation behind the existing UI.

FILES:
- MainActivity.kt  -> replace app/src/main/java/com/example/myaiassistant/MainActivity.kt
- UnderwaterLiveBackground.kt -> add to the same package folder

NO NEW GRADLE DEPENDENCY.
NO IMAGE/VIDEO ASSET REQUIRED.

Implementation:
- MainActivity adds UnderwaterLiveBackground as the first child of root.
- Existing AurixOriginalUi is still added afterward, so it stays above the animation.
- The background view consumes no touch events.
- Fish, bubbles, light rays, and seabed are drawn procedurally.

After replacing/adding files:
1. Clean Project
2. Rebuild Project
3. Run the app

If the background looks too bright/dim, adjust only the alpha/color constants
inside UnderwaterLiveBackground.kt; do not change AurixOriginalUi.
