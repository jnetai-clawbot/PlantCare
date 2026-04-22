# 🌿 PlantCare

Smart plant care for your garden — an Android app to track watering, health, and seasonal care for all your plants.

## Features

- **Plant Collection** — Add plants with name, species, location, photo, and date planted
- **Watering Reminders** — Configurable per-plant watering schedules with notifications
- **Sunlight Tracking** — Track sunlight requirements (full sun, partial shade, full shade, indirect)
- **Health Log** — Notes and photos over time to track plant health
- **Wishlist** — Keep a list of plants you want to get, one-tap move to your collection
- **Seasonal Reminders** — Spring, summer, autumn, and winter care tips
- **Export** — Export all plant data as JSON
- **Dark Theme** — Material Design 3 with green color scheme
- **About** — Version info, update check via GitHub, share

## Tech Stack

- Kotlin + AndroidX
- Room database
- Material Design 3 (dark theme)
- AlarmManager for reminders
- Coroutines (Dispatchers.IO for all DB ops)
- Navigation Component

## Building

```bash
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/PlantCare.apk`

## License

MIT