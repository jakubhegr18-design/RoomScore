# RoomScore

Scan your room with your phone camera and get instant feedback — a score, improvement suggestions, chore recommendations, IKEA product ideas, and environmental data from Home Assistant.

## Features

- **Camera Scan** — Take up to 4 photos of your room from different angles for better accuracy
- **ML Kit Analysis** — Detects furniture, decor, clutter, and electronics
- **Room Score** — 0–100 rating with letter grade (A+ through F)
- **Improvement Tips** — Room-type-specific suggestions (bedroom, living room, office, etc.)
- **Chore Recommendations** — Actionable tasks based on what's detected
- **IKEA Shopping** — Product recommendations with prices matched to each improvement
- **Home Assistant Integration** — Factors in temperature, humidity, light, CO₂, and more

## Screenshots

| Home | Scan | Result | IKEA Recs | HA Data |
|------|------|--------|-----------|---------|
| Start screen with feature cards | Multi-photo capture with counter | Animated score + grade | Product cards with prices | Sensor readings card |

## Tech Stack

- **Android**: Kotlin, Jetpack Compose, Material 3, CameraX, ML Kit Image Labeling
- **Home Assistant**: Custom HACS component, REST API, sensor platform

## HACS Integration (optional)

Install the custom component to get a dedicated `/api/roomscore` endpoint:

1. Copy `custom_components/roomscore/` to your HA `config/custom_components/`
2. Restart Home Assistant
3. Go to **Settings → Devices & Services → Add Integration** → search for **RoomScore**

The Android app auto-detects the HACS endpoint and falls back to generic `/api/states` parsing if not found.

## Building

Open the `RoomScore/` folder in Android Studio and run on a device with API 26+.

```bash
./gradlew assembleDebug
```

## License

Apache 2.0
