AURIX LIVE UNDERWATER BACKGROUND — FIXED

This patch fixes the previous invisible-background issue.

WHY THE PREVIOUS PATCH COULD BE INVISIBLE:
The existing AurixOriginalUi is added as a full-screen View. If that UI root
has an opaque background, a background placed behind it is hidden.

THIS PATCH:
- Places UnderwaterLiveBackground INSIDE the existing AurixOriginalUi root
  at child index 0.
- Makes only that UI root's background transparent so the animation can show.
- Leaves existing buttons, icons, mic/orb, text, navigation, callbacks,
  service logic and interaction code unchanged.
- Uses procedural Canvas animation; no video/image asset and no new dependency.
- Background view is non-clickable and does not consume touch events.

FILES:
1. MainActivity.kt
   Replace:
   app/src/main/java/com/example/myaiassistant/MainActivity.kt

2. UnderwaterLiveBackground.kt
   Add to:
   app/src/main/java/com/example/myaiassistant/UnderwaterLiveBackground.kt

BUILD:
1. Replace/add the two files.
2. Clean Project.
3. Rebuild Project.
4. Run the app.

EXPECTED:
The underwater scene should be visible behind the existing AURIX interface,
with moving fish, bubbles, light rays and particles.

NOTE:
This patch was source-checked against the current MainActivity structure,
but an Android Gradle build was not run in this environment.
