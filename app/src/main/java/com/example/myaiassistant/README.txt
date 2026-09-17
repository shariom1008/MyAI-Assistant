AURIX LIVE UNDERWATER — SAFE BACKGROUND PATCH

This patch is based on the MainActivity source supplied in the chat.

IMPORTANT:
- Do NOT replace AurixService.kt.
- Do NOT modify AurixOriginalUi.
- Existing orb, buttons, icons, text, callbacks and navigation remain intact.
- UnderwaterLiveBackground is a separate sibling behind the original UI.
- The UI root is NOT made transparent.

Files:
1. MainActivity.kt — only createInterface() is changed for the live background.
2. UnderwaterLiveBackground.kt — procedural animated background.
3. README.txt

Install:
- Replace MainActivity.kt with the included file.
- Add UnderwaterLiveBackground.kt.
- Leave AurixService.kt untouched.
- Clean Project > Rebuild Project > Run.

Note: This source patch has not been Android/Gradle-built in this environment.
