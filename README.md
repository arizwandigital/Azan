# FocusGuard — Android focus timer (personal APK)

Private, on-device experimental app blocker. **This repository branch is distinct from the My Azan main branch**.

## What it does
- Select launchable third-party applications and define a daily blocked interval for each.
- Define independent Instagram Reels, Facebook Reels and YouTube Shorts blocked intervals.
- On an accessibility event, if an app is blocked, FocusGuard navigates back to Android Home.
- Reels / Shorts identification is **best effort** based on visible accessibility labels and view IDs, not a guaranteed way to suppress short-form content.
- You can disable the service in Android Accessibility settings at any time.
- No network permission, no server, no advertising, no logging of text.

## Install / grant access
1. In GitHub **Actions → Build FocusGuard APK**, download the completed `FocusGuard-APK` artifact and extract `app-debug.apk`.
2. Install on Android 8+ and open FocusGuard.
3. Read the disclosure; tap **Open Accessibility Settings**; enable **FocusGuard Blocking Service** if Android allows it.
4. Return to the app, choose apps, enable rules, set start/end and (for Reels) choose individual schedules.
5. Check on your own device before relying on any rule.

## Important limitations
- It does **not** force-stop apps; it returns to Home when it recognizes a blocked screen.
- The screen may briefly appear before a block. App-specific UI labels are not stable; selective blocking may fail.
- Behavior may vary by phone and Android version. Some devices restrict sideloaded apps' accessibility permission until manually allowing restricted settings.
- Default rules are OFF, and no accessibility service runs before the user enables it.
- This personal-sideload design has **not** been reviewed for Google Play accessibility eligibility.
- The build produces a debug-signed APK. Subsequent builds may use different debug signing keys.
