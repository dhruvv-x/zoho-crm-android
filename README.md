# OCTFIS – Zoho CRM Android Application

This project is a Zoho CRM Android application developed using Kotlin and Jetpack Compose. The main purpose of the app is to connect with Zoho CRM APIs and provide a simple mobile interface for handling CRM operations like managing accounts, contacts, deals, activities, meetings, and tasks.

I built this project to improve my understanding of:

* Android app architecture
* REST API integration
* OAuth authentication
* Jetpack Compose UI
* State management using MVVM

The project follows a clean structure and focuses on practical CRM functionality rather than only UI design.

---

# Features

## Authentication

* Zoho OAuth authentication flow
* Secure token handling
* Persistent session management using DataStore

## CRM Modules

* Accounts management
* Contacts management
* Deals tracking
* Quotes management
* Activities dashboard
* Calls management
* Meetings management
* Tasks management

## UI & UX

* Built entirely using Jetpack Compose
* Material 3 design system
* Navigation Compose integration
* Responsive modern UI
* Theme preference handling

## Architecture

* MVVM (Model-View-ViewModel)
* Repository pattern
* Retrofit-based API layer
* Centralized service locator
* Clean package structure

---

# Tech Stack

| Category       | Technology            |
| -------------- | --------------------- |
| Language       | Kotlin                |
| UI Toolkit     | Jetpack Compose       |
| Architecture   | MVVM                  |
| Navigation     | Navigation Compose    |
| Networking     | Retrofit + OkHttp     |
| JSON Parsing   | Gson Converter        |
| Storage        | DataStore Preferences |
| Authentication | Zoho OAuth            |
| Build System   | Gradle Kotlin DSL     |
| Minimum SDK    | 26                    |
| Target SDK     | 35                    |

---

# Project Structure

```text
com.pookie.octfis
│
├── data
│   ├── model
│   ├── remote
│   ├── repository
│
├── navigation
│
├── ui
│   ├── components
│   ├── screens
│
├── MainActivity.kt
└── OctfisApp.kt
```

---

# Key Components

## Remote Layer

Handles:

* Zoho API services
* OAuth authentication
* Token management
* API client configuration

Main files:

* `ZohoApiClient.kt`
* `ZohoApiService.kt`
* `ZohoAuthManager.kt`
* `TokenStore.kt`

## Repository Layer

Acts as the bridge between UI and remote APIs.

Repositories include:

* AccountRepository
* ContactRepository
* DealRepository
* QuoteRepository
* ActivityRepository
* CallRepository
* MeetingRepository
* TaskRepository

## UI Layer

Compose-based screens and ViewModels.

Examples:

* DashboardScreen
* AccountsScreen
* ContactsScreen
* DealsScreen
* CreateAccountScreen
* CreateContactScreen

---

# API Integration

The application integrates with Zoho CRM APIs for:

* Fetching CRM records
* Creating records
* Updating CRM data
* Authentication and authorization
* Activity tracking

The networking stack uses:

* Retrofit 2.11.0
* OkHttp 4.12.0
* Gson Converter

---

# Installation & Setup

## Prerequisites

* Android Studio Hedgehog or newer
* JDK 17
* Android SDK 35
* Zoho CRM Developer Account

---

## Clone Repository

```bash
git clone https://github.com/dhruvv-x/zoho-crm-android.git
```

---

## Open Project

1. Open Android Studio
2. Select "Open"
3. Choose the project folder
4. Sync Gradle

---

## Configure Zoho OAuth

Inside `app/build.gradle.kts`:

```kotlin
buildConfigField(
    "String",
    "ZOHO_CLIENT_ID",
    "YOUR_CLIENT_ID"
)
```

Replace the client ID with your own Zoho OAuth credentials.

---

# Running the App

```bash
./gradlew assembleDebug
```

Or simply run directly from Android Studio on:

* Emulator
* Physical Android device

---

# Screens Included

* Dashboard
* Accounts
* Contacts
* Deals
* Quotes
* Activities
* Create Account
* Create Contact
* Edit Account
* Detail Screens

---

# What I Learned

While building this project, I gained hands-on experience with:

* Working with Zoho CRM APIs
* Implementing OAuth authentication flow
* Building modern UI using Jetpack Compose
* Managing API calls using Retrofit
* Using ViewModels and repository pattern
* Structuring Android projects using MVVM
* Handling navigation and state management

This project helped me understand how real-world Android applications are organized and connected with backend services.

---

# Future Improvements

Potential enhancements:

* Offline caching
* Pagination support
* Push notifications
* CRM analytics dashboard
* Dependency Injection (Hilt/Koin)
* Unit & UI testing
* Dark mode improvements

---

# Author

## Dhruv

Android Developer

GitHub:
[https://github.com/dhruvv-x](https://github.com/dhruvv-x)

Repository:
[https://github.com/dhruvv-x/zoho-crm-android](https://github.com/dhruvv-x/zoho-crm-android)

---

# Note

This project was built for learning, practice, and portfolio purposes.
