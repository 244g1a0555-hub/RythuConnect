# 🌾 Rythu Connect 

A multilingual Android application that directly connects farmers with buyers — eliminating middlemen and ensuring fair prices.

## 📱 About

Rythu Connect bridges the gap between farmers and the market by providing crop selling/buying, real-time weather, market prices, and nearby market location — all in 6 Indian languages.

## ❓ Problem

- Farmers depend on middlemen who take 20–40% commission
- No direct access to buyers across regions
- Most apps are English-only, excluding non-English-speaking farmers
- Farmers don't know real-time market prices

## ✅ Solution

Farmers upload crop details (name, photo, price, quantity) → Buyers browse listings → Direct contact, no middleman, no commission.

## ✨ Features

| Feature | Description |
|---|---|
| 🌾 Sell Crops | List crops with photo, price, and quantity |
| 🛒 Buy Crops | Browse and contact farmers directly |
| 📋 Crop List | View all available crops in the market |
| ⛅ Weather | Real-time weather updates |
| 💰 Market Prices | Live prices for major crops |
| 📞 Consult | Connect with agricultural experts |
| 👤 My Profile | Manage farmer profile and listings |
| 📍 Nearby Markets | GPS-based market finder |

## 🌍 Supported Languages

English | తెలుగు | हिन्दी | ಕನ್ನಡ | മലയാളം | मराठी

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Backend:** Firebase Firestore
- **Auth:** Firebase Authentication (Email + Phone OTP)
- **Storage:** Firebase Storage
- **Navigation:** Jetpack Navigation Component
- **Image Loading:** Coil
- **Location:** GPS / Google Maps

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- JDK 17+
- A Firebase project with Firestore, Authentication, and Storage enabled

### Setup
1. Clone the repository
   ```bash
   git clone https://github.com/your-username/RythuConnect.git
   ```
2. Open the project in Android Studio
3. Add your own `google-services.json` file to the `app/` directory
4. Sync Gradle and run the app

## 📂 Project Structure
```
app/src/main/java/com/example/rythuconnect/
├── MainActivity.kt        # Navigation, Login, Signup screens
├── ui/theme/               # App theming
└── ...
```

## 🚧 Future Plans

- AI-based crop disease detection
- Government scheme alerts
- Farmer loan/subsidy information
- AI-based price prediction
- Buyer reviews and ratings
- More regional languages

## 👨‍💻 Developer

**Sake Bhaskar**  
Srinivasa Ramanujan Institute of Technology  
Solo Developer

## 📄 License

This project is for educational purposes.

---

🌱 *Built with passion to empower Indian farmers.*
