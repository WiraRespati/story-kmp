# 📱 Story App — Kotlin Multiplatform (KMP)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Room KMP](https://img.shields.io/badge/AndroidX%20Room-2.8.4-4CAF50.svg?logo=sqlite&logoColor=white)](https://developer.android.com/kotlin/multiplatform/room)
[![Ktor](https://img.shields.io/badge/Ktor-3.1.1-black.svg?logo=ktor&logoColor=white)](https://ktor.io)
[![Koin](https://img.shields.io/badge/Koin-4.2.2-EB5424.svg)](https://insert-koin.io/)
[![Platforms](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-3DDC84.svg)](https://kotlinlang.org/docs/multiplatform.html)

Aplikasi **Story Sharing** modern berbasis **Kotlin Multiplatform (KMP)** dan **Compose Multiplatform (CMP)**. Berbagi 100% logika bisnis, manajemen state, komunikasi API, caching database lokal, hingga tampilan antarmuka (UI) lintas platform antara **Android** dan **iOS**.

---

## 📌 Daftar Isi
- [✨ Fitur Aplikasi](#-fitur-aplikasi)
- [🛠️ Tech Stack](#️-tech-stack)
- [🏗️ Arsitektur Aplikasi](#️-arsitektur-aplikasi)
- [📂 Struktur Direktori](#-struktur-direktori)
- [🚀 Setup & Panduan Menjalankan Project](#-setup--panduan-menjalankan-project)
- [💻 Perintah Gradle yang Sering Digunakan](#-perintah-gradle-yang-sering-digunakan)

---

## ✨ Fitur Aplikasi

### 1. 🔐 Autentikasi & Akun
* **Register:** Pendaftaran akun baru dengan validasi format email, nama, dan kata sandi minimal 8 karakter.
* **Login & Sesi:** Otentikasi pengguna berbasis JWT Bearer Token dengan penyimpanan sesi yang aman (`TokenStorage`).
* **Guest Mode:** Kemampuan untuk menjelajahi dan mengunggah cerita tanpa akun melalui endpoint tamu.
* **Profil & Logout:** Informasi akun aktif dan pembersihan token saat logout.

### 2. 📰 Feed Cerita Interaktif (Story List)
* **Daftar Cerita:** Menampilkan feed cerita komunitas lengkap dengan foto, nama pengunggah, deskripsi, dan waktu publikasi.
* **Pencarian Real-Time:** Filter instan berdasarkan nama pengunggah atau isi deskripsi cerita.
* **Filter Lokasi:** Menyaring hanya cerita yang memiliki koordinat lokasi geografis (GPS).
* **Filter Bookmark:** Menyaring hanya cerita yang ditandai sebagai favorit oleh pengguna.
* **Pull-to-Refresh & Auto-Refresh:** Memperbarui data secara otomatis setelah cerita baru berhasil diunggah.

### 3. 🔍 Detail Cerita & Reverse-Geocoding
* **Informasi Lengkap:** Tampilan gambar layar penuh, nama pengguna, tanggal upload, dan teks cerita lengkap.
* **Reverse-Geocoding Otomatis:** Mengonversi koordinat Latitude & Longitude menjadi nama lokasi/alamat yang mudah dibaca menggunakan OpenStreetMap Nominatim API.
* **Tombol Bookmark:** Menyimpan atau menghapus cerita favorit langsung dari halaman detail.

### 4. 📸 Unggah Cerita (Add Story) & Media Picker
* **Cross-Platform Image Picker:** Pengambilan gambar langsung dari Kamera atau Galeri perangkat untuk Android dan iOS.
* **Validasi Media:** Pengecekan otomatis batas ukuran file foto (< 1MB) dan kelengkapan deskripsi.
* **Pemilihan Lokasi Interaktif:**
  * Pencarian alamat tempat berbasis OpenStreetMap (OSM Geocoding).
  * Opsi memilih lokasi langsung dari peta atau menyertakan koordinat saat ini.
* **Mode Tamu (Guest Upload):** Pilihan unggah cerita sebagai tamu (*guest*).

### 5. 💾 Offline Mode & Outbox Queue (Didukung Room KMP)
* **Database Caching Lokal:** Feed cerita di-cache otomatis ke database lokal SQLite menggunakan **AndroidX Room KMP**, memungkinkan aplikasi tetap dapat membaca cerita meski tanpa koneksi internet.
* **Antrean Offline Drafts (Outbox):** Jika gagal terhubung ke internet saat membuat cerita, cerita akan otomatis tersimpan sebagai draft lokal lengkap dengan gambar base64, deskripsi, dan titik koordinat.
* **Menu Tersimpan & Draft:** Halaman terdedikasi untuk melihat daftar favorit dan antrean draft offline.
* **Sinkronisasi Otomatis (Sync Drafts):** Tombol sinkronisasi sekali sentuh untuk mengunggah seluruh draft offline ke server saat koneksi internet kembali pulih.

### 6. 🗺️ Peta Interaktif (Interactive Leaflet Map)
* **Peta Interaktif Cross-Platform:** Integrasi Leaflet JS dan OpenStreetMap via WebView teroptimasi di Android dan iOS (`LeafletMapView`).
* **Pin Marker Cerita:** Menampilkan penanda lokasi pada peta untuk semua cerita yang memiliki koordinat GPS.
* **Pratinjau Cerita:** Menampilkan popup dan kartu ringkas cerita ketika marker ditekan.

### 7. 🎨 Kustomisasi Tampilan & Tema
* **Light / Dark Theme:** Mendukung tema Terang (*Light Mode*) dan Gelap (*Dark Mode*) dengan transisi warna Material 3 yang halus.
* **Font Family Switcher:** Opsi mengubah jenis font tampilan aplikasi (didukung Google Poppins: Regular, Medium, SemiBold, Bold) yang disimpan secara persisten.

---

## 🛠️ Tech Stack

| Komponen | Pustaka / Teknologi | Keterangan |
| :--- | :--- | :--- |
| **Language** | **Kotlin 2.4.10** | Kotlin Multiplatform Mobile |
| **UI Framework** | **Compose Multiplatform 1.11.1** | JetBrains Compose cross-platform (100% shared UI) |
| **Design System** | **Material Design 3 (1.11.0)** | Material 3 Components & Dynamic Colors |
| **Navigation** | **Jetpack Navigation Compose 2.9.2** | Navigasi halaman cross-platform resmi (`NavHost`, `composable`) |
| **Local Database** | **AndroidX Room KMP 2.8.4** | Database SQLite lokal resmi dengan KSP, Entities, DAOs, dan Flow |
| **SQLite Driver** | **SQLite Bundled 2.7.0** | `BundledSQLiteDriver` untuk konsistensi query SQLite lintas OS |
| **Key-Value Storage** | **Multiplatform Settings 1.3.0** | Penyimpanan token sesi (`SharedPreferences` di Android & `NSUserDefaults` di iOS) |
| **Networking** | **Ktor Client 3.1.1** | HTTP client multiplatform dengan engine OkHttp (Android) & Darwin (iOS) |
| **API Features** | **Ktor Auth & Logging** | Plugin Content Negotiation, Bearer Auth, dan Logging Inspector |
| **Serialization** | **Kotlinx Serialization JSON 1.8.0** | Parser JSON type-safe performa tinggi |
| **Dependency Injection** | **Koin Multiplatform 4.2.2** | Service Locator / DI untuk KMP (`koin-core`, `koin-compose-viewmodel`) |
| **Image Loading** | **Coil 3.5.0** | Pustaka pemuat gambar asinkron & caching multiplatform (`coil-compose`, `coil-network-ktor3`) |
| **Concurrency** | **Kotlinx Coroutines 1.11.0** | `StateFlow`, `SharedFlow`, Coroutine Scope |
| **Architecture** | **MVVM + Clean Architecture** | Unidirectional Data Flow (UDF) & Repository Pattern |
| **Maps Engine** | **Leaflet JS + OpenStreetMap** | Peta interaktif berbasis WebView cross-platform |
| **Code Processing** | **Google KSP 2.3.11** | Pemrosesan anotasi Room saat compile-time |

---

## 🏗️ Arsitektur Aplikasi

Proyek ini menerapkan prinsip **Clean Architecture** dan **MVVM (Model-View-ViewModel)** dengan alur data satu arah (*Unidirectional Data Flow*):

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                   │
│  Screens: Login, Register, Home, Detail, Add, Map      │
└───────────────────────────▲────────────────────────────┘
                            │ (StateFlow / Actions)
┌───────────────────────────▼────────────────────────────┐
│                    ViewModel Layer                     │
│  HomeViewModel, DetailViewModel, AddStoryViewModel...  │
└───────────────────────────▲────────────────────────────┘
                            │ (Flow<ApiResult<T>>)
┌───────────────────────────▼────────────────────────────┐
│                    Repository Layer                    │
│      StoryRepository, AuthRepository, ThemeRepository  │
└───────────────────────────▲────────────────────────────┘
              ┌─────────────┴─────────────┐
              ▼                           ▼
┌───────────────────────────┐ ┌──────────────────────────┐
│      Remote Data Source   │ │    Local Database Source │
│    Ktor 3 StoryApiService │ │ AndroidX Room AppDatabase│
│    OpenStreetMap Geocoding│ │   (StoryDao, BookmarkDao,│
│                           │ │      OfflineDraftDao)    │
└───────────────────────────┘ └──────────────────────────┘
```

---

## 📂 Struktur Direktori

```text
Story/
├── androidApp/                       # Modul Aplikasi Android (Entry point Android)
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/com/learn/story/MainActivity.kt
├── iosApp/                           # Proyek Aplikasi iOS (Entry point iOS)
│   ├── iosApp/                       # File SwiftUI & App delegate
│   └── iosApp.xcodeproj             # Xcode Project
├── shared/                           # Shared Kotlin Multiplatform Module
│   ├── build.gradle.kts              # Konfigurasi dependensi KMP, KSP & Room
│   └── src/
│       ├── commonMain/               # Kode Utama Bersama (Cross-Platform)
│       │   ├── composeResources/     # Font Poppins, Vector Drawables, Resources
│       │   └── kotlin/com/learn/story/
│       │       ├── App.kt            # Entry point UI Compose & Koin initialization
│       │       ├── data/
│       │       │   ├── local/        # AndroidX Room Database KMP
│       │       │   │   ├── AppDatabase.kt
│       │       │   │   ├── DatabaseBuilder.kt
│       │       │   │   ├── dao/      # StoryDao, BookmarkDao, OfflineDraftDao
│       │       │   │   └── entity/   # StoryEntity, BookmarkEntity, OfflineDraftEntity
│       │       │   ├── model/        # Story, Auth, OfflineStoryDraft models
│       │       │   ├── network/      # Ktor Client Factory & Engine abstraction
│       │       │   ├── remote/       # StoryApiService, GeocodingService
│       │       │   ├── repository/   # StoryRepository, AuthRepository, ThemeRepository
│       │       │   └── storage/      # TokenStorage (Multiplatform Settings)
│       │       ├── di/               # Koin AppModule
│       │       └── ui/
│       │           ├── components/   # Tombol kustom, TextField, Card, LeafletMapView
│       │           ├── navigation/   # Jetpack Navigation Compose NavHost & Routes
│       │           ├── screens/      # Auth, Home, Detail, Add, Map, Profile screens
│       │           └── theme/        # Material 3 Color, Typography, Theme
│       ├── androidMain/              # Implementasi Spesifik Android
│       │   ├── AndroidManifest.xml   # StoryContextProvider declaration
│       │   └── kotlin/com/learn/story/
│       │       ├── data/local/       # DatabaseBuilder.android.kt & ContextProvider
│       │       ├── data/network/     # OkHttp Engine
│       │       └── ui/               # Android Camera/Gallery & Android WebView
│       └── iosMain/                  # Implementasi Spesifik iOS
│           └── kotlin/com/learn/story/
│               ├── MainViewController.kt
│               ├── data/local/       # DatabaseBuilder.ios.kt (NSDocumentDirectory)
│               ├── data/network/     # Darwin HTTP Engine
│               └── ui/               # iOS UIImagePickerController & WKWebView
└── gradle/
    └── libs.versions.toml            # Gradle Version Catalog terpusat
```

---

## 🚀 Setup & Panduan Menjalankan Project

### Prasyarat Sistem
1. **Java Development Kit (JDK):** JDK 17 atau **JDK 21** (direkomendasikan *Azul Zulu 21*).
2. **Android Studio:** Android Studio Ladybug (2024.2+) atau versi lebih baru dengan plugin **Kotlin Multiplatform Mobile**.
3. **Xcode:** Xcode 15+ (diperlukan hanya jika ingin menjalankan target iOS pada macOS).

---

### Langkah Menjalankan Aplikasi

#### 1. Clone Repositori
```bash
git clone <repository-url>
cd Story
```

#### 2. Menjalankan di Android
* **Menggunakan Android Studio:**
  1. Buka folder proyek di Android Studio.
  2. Tunggu proses Gradle Sync selesai.
  3. Pilih konfigurasi run `androidApp` pada toolbar atas.
  4. Pilih Emulator atau Perangkat Fisik Android, lalu klik **Run** (▶).
* **Menggunakan Terminal:**
  ```bash
  ./gradlew :androidApp:installDebug
  ```

#### 3. Menjalankan di iOS
* **Menggunakan Xcode:**
  1. Buka folder `iosApp/iosApp.xcodeproj` di Xcode.
  2. Pilih Simulator iOS (misalnya *iPhone 16 Pro*).
  3. Tekan tombol **Run** (Cmd + R).
* **Menggunakan Android Studio:**
  1. Pasang plugin *Kotlin Multiplatform Mobile*.
  2. Pilih konfigurasi run `iosApp` dan pilih target simulator.
  3. Klik tombol **Run**.

---

## 💻 Perintah Gradle yang Sering Digunakan

| Tugas | Perintah |
| :--- | :--- |
| **Kompilasi Shared Module (Semua Target)** | `./gradlew :shared:assemble` |
| **Kompilasi Android Debug Library** | `./gradlew :shared:assembleDebug` |
| **Build APK Android Debug** | `./gradlew :androidApp:assembleDebug` |
| **Install APK ke Perangkat Android** | `./gradlew :androidApp:installDebug` |
| **Link Framework iOS (Simulator)** | `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` |
| **Jalankan Unit Test Android** | `./gradlew :shared:testDebugUnitTest` |
| **Jalankan Unit Test iOS Simulator** | `./gradlew :shared:iosSimulatorArm64Test` |
| **Bersihkan Build Cache** | `./gradlew clean` |

---

## 📄 Lisensi
Project ini dibuat dan dikembangkan untuk keperluan edukasi dan implementasi arsitektur Kotlin Multiplatform modern.