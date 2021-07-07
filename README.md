# Velotracker
For the application to work, you need to get 2 tokens - https://www.mapbox.com/
1) Public token - in R.strings.xml
```xml
<string name="access_token">MAPBOX_ACCESS_TOKEN</string>
```
2) Secret token with scope "DOWNLOADS:READ" - in C:\Users\user\\.gradle\gradle.properties
MAPBOX_DOWNLOADS_TOKEN=PASTE_YOUR_SECRET_TOKEN_HERE

Works on android versions 8 - 11 (on others - I don't know)

On some OC, it is necessary to disable battery saving mode for the app in order for the app to run in the background.
EMUI: Settings-Battery-App launch.
Others: I don't know.

