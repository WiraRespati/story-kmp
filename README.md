# 📱 Story App — Kotlin Multiplatform (KMP)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Room KMP](https://img.shields.io/badge/AndroidX%20Room-2.8.4-4CAF50.svg?logo=sqlite&logoColor=white)](https://developer.android.com/kotlin/multiplatform/room)
[![Ktor](https://img.shields.io/badge/Ktor-3.1.1-black.svg?logo=ktor&logoColor=white)](https://ktor.io)
[![Koin](https://img.shields.io/badge/Koin-4.2.2-EB5424.svg)](https://insert-koin.io/)
[![BuildKonfig](https://img.shields.io/badge/BuildKonfig-0.22.0-blueviolet.svg)](https://github.com/yshrsmz/BuildKonfig)
[![Platforms](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-3DDC84.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Aplikasi **Story Sharing** modern kelas produksi berbasis **Kotlin Multiplatform (KMP)** dan **Compose Multiplatform (CMP)**. Seluruh lapisan aplikasi—mulai dari tampilan antarmuka (UI Declarative), manajemen state (*StateFlow*), komunikasi jaringan API, caching database lokal SQLite, antrean sinkronisasi *offline outbox*, hingga kompresi gambar adaptif—dibagikan (**100% Shared Code**) antara **Android** dan **iOS**.

---

## 📌 Daftar Isi
- [✨ Fitur Unggulan](#-fitur-unggulan)
- [🛠️ Tech Stack & Versi Dependensi](#️-tech-stack--versi-dependensi)
- [🏗️ Arsitektur Aplikasi & Alur Data](#️-arsitektur-aplikasi--alur-data)
- [📸 Deep Dive: Pipeline Kompresi Gambar (< 1 MB)](#-deep-dive-pipeline-kompresi-gambar--1-mb)
- [💾 Deep Dive: Mode Offline & Outbox Queue (Room KMP)](#-deep-dive-mode-offline--outbox-queue-room-kmp)
- [🛡️ Deep Dive: Keamanan & Hiding Base URL (BuildKonfig)](#️-deep-dive-keamanan--hiding-base-url-buildkonfig)
- [🗺️ Deep Dive: Peta Interaktif (Leaflet JS + OpenStreetMap)](#️-deep-dive-peta-interaktif-leaflet-js--openstreetmap)
- [📂 Struktur Direktori Proyek](#-struktur-direktori-proyek)
- [🚀 Setup & Panduan Menjalankan Project](#-setup--panduan-menjalankan-project)
- [🧪 Pengujian Unit & Verifikasi (Testing)](#-pengujian-unit--verifikasi-testing)
- [💻 Perintah Gradle yang Sering Digunakan](#-perintah-gradle-yang-sering-digunakan)
- [❓ Tanya Jawab & Pemecahan Masalah (FAQ)](#-tanya-jawab--pemecahan-masalah-faq)
- [📄 Lisensi](#-lisensi)

---

## ✨ Fitur Unggulan

### 1. 🔐 Autentikasi & Akun Pengguna
* **Registrasi Akun Baru:** Validasi masukan formulir *real-time* untuk format email yang sah, nama pengguna, dan kata sandi minimal 8 karakter.
* **Login & Sesi Aman:** Autentikasi berbasis JWT (*Bearer Token*) dengan penanganan sesi persisten melalui `TokenStorage` (didukung `Multiplatform Settings`).
* **Guest Mode (Mode Tamu):** Pengguna dapat langsung menjelajah feed cerita dan mengunggah cerita sebagai *guest* tanpa perlu login terlebih dahulu.
* **Profil & Logout:** Tampilan profil akun aktif serta pembersihan sesi token secara aman saat logout.

### 2. 📰 Feed Cerita Interaktif (Story Feed)
* **Daftar Cerita Komunitas:** Feed visual responsif menampilkan gambar cerita beresolusi tinggi, nama pengguna, deskripsi, dan waktu publikasi yang diformat ramah pengguna.
* **Pencarian Real-Time:** Filter dinamis instan berdasarkan kata kunci nama pengunggah atau isi konten cerita.
* **Filter Multi-Kategori:**
  * **Semua:** Menampilkan semua cerita terbaru dari komunitas.
  * **Tersimpan (Bookmark):** Hanya menampilkan cerita yang ditandai sebagai favorit oleh pengguna.
  * **Ada Lokasi:** Hanya menyaring cerita yang menyertakan koordinat GPS.
* **Pull-to-Refresh & Auto-Refresh:** Memperbarui data feed secara mulus (*swipe down*) dan otomatis memuat cerita baru setelah proses unggah selesai.

### 3. 🔍 Detail Cerita & Reverse-Geocoding Otomatis
* **Tampilan Layar Penuh:** Pratinjau gambar cerita HD, informasi pengguna, waktu unggah lengkap, dan teks deskripsi.
* **Reverse-Geocoding Otomatis:** Mengubah koordinat GPS (`lat`, `lon`) menjadi nama alamat/wilayah asli yang mudah dibaca menggunakan integrasi OpenStreetMap Nominatim API secara asinkron.
* **Aksi Favorit (Bookmark):** Menambah atau menghapus cerita favorit secara instan langsung dari layar detail.

### 4. 📸 Unggah Cerita, Media Picker & Kompresi Gambar (< 1 MB Terjamin)
* **Cross-Platform Media Picker:**
  * Pengambilan foto langsung dari **Kamera** perangkat menggunakan intent Android / `UIImagePickerController` iOS.
  * Pemilihan foto dari **Galeri** media pengguna.
* **Pipeline Kompresi Gambar Bertingkat (< 1 MB):**
  * Seluruh foto dari kamera atau galeri otomatis diproses dan dikompresi hingga berukuran aman **< 950 KB** (sesuai standar batas upload API 1 MB).
  * **Rotasi Otomatis (EXIF Orientation):** Membaca metadata orientasi EXIF kamera secara otomatis agar foto selalu tegak lurus (*upright*) dan tidak terbalik di perangkat apa pun.
  * **Pencegahan Out-of-Memory (OOM):** Menggunakan teknik *subsampling* `inSampleSize` saat mendecode foto kamera resolusi tinggi (48MP/108MP).
  * **Downscaling & Iterative Quality:** Kompresi kualitas JPEG adaptif (85% -> 15%) dan downscaling proporsional bertahap (80% per iterasi) bila file masih besar.
* **Indikator Visual Ukuran:** Badge informatif pada pratinjau gambar menampilkan ukuran file riil setelah dikompresi (`Ukuran: X KB (Telah terkompresi < 1 MB)`).
* **Penentuan Titik Lokasi Cerita:**
  * Pencarian alamat menggunakan OpenStreetMap Geocoding.
  * Pilihan mengambil koordinat lokasi saat ini atau memilih manual dari peta interaktif.
* **Unggah Tamu (Guest Upload):** Mendukung unggah cerita langsung tanpa autentikasi melalui endpoint `/stories/guest`.

### 5. 💾 Mode Offline & Antrean Outbox (Didukung Room KMP)
* **Local Database Caching (SQLite):** Seluruh feed cerita di-cache otomatis ke database lokal SQLite via **AndroidX Room KMP**. Pengguna tetap dapat membaca cerita meskipun sedang berada di mode pesawat atau jaringan internet terputus.
* **Offline Outbox Queue (Draft Cerita):** Saat membuat cerita di kondisi tanpa internet, cerita otomatis disimpan ke antrean draft lokal SQLite dengan foto berformat Base64, teks deskripsi, dan titik koordinat.
* **Halaman Tersimpan & Draft:** Menu terpisah untuk mengelola daftar bookmark dan antrean draft offline.
* **Sinkronisasi Otomatis (Sync Drafts):** Tombol sinkronisasi sekali sentuh untuk mengunggah seluruh draft yang tersimpan secara bertahap saat koneksi internet pulih.

### 6. 🗺️ Peta Interaktif Tanpa Kunci API (Leaflet JS + OpenStreetMap)
* **Zero API Key:** Menggunakan ekosistem OpenStreetMap & Leaflet JS, sepenuhnya gratis dan tidak membutuhkan API Key pihak ketiga berbayar (seperti Google Maps API).
* **Cross-Platform WebView:** Berjalan melalui WebView teroptimasi di Android dan `WKWebView` di iOS.
* **Pin Marker & Callout:** Menandai seluruh lokasi cerita pada peta dengan penanda interaktif dan kartu ringkas cerita ketika marker ditekan.
* **Bi-directional Bridge:** Komunikasi dua arah antara antarmuka JavaScript peta dan kode Kotlin Multiplatform native.

### 7. 🎨 Personalisasi Tema & Tipografi
* **Light & Dark Theme:** Transisi tema Terang dan Gelap yang ramah mata dengan skema warna Material Design 3.
* **Dynamic Font Switcher:** Pilihan mengubah jenis tipografi aplikasi secara instan (didukung Google Poppins: Regular, Medium, SemiBold, Bold) yang disimpan persisten di memori lokal.

### 8. 🛡️ Keamanan & Hiding Base URL (BuildKonfig)
* **Base URL Tersembunyi:** URL API Story **tidak pernah di-hardcode** di dalam repositori publik.
* **Injeksi Type-Safe:** URL dimuat dari berkas `local.properties` (yang diabaikan oleh Git) dan di-generate menjadi objek Kotlin saat build time menggunakan **BuildKonfig**.
* **Dukungan CI/CD:** Mendukung injeksi variabel melalui properti Gradle `-PSTORY_API_BASE_URL` saat automated testing dan release build.

---

## 🛠️ Tech Stack & Versi Dependensi

| Kategori | Teknologi / Pustaka | Versi | Peran & Keterangan |
| :--- | :--- | :--- | :--- |
| **Bahasa Utama** | **Kotlin Multiplatform** | `2.4.10` | 100% kode bersama untuk Android dan iOS |
| **UI Framework** | **Compose Multiplatform** | `1.11.1` | Antarmuka deklaratif bersama JetBrains |
| **Design System** | **Material Design 3** | `1.11.0` | Komponen Material 3, Dynamic Theming, Color Tokens |
| **Navigasi** | **Jetpack Navigation Compose** | `2.9.2` | Sistem routing & navigasi layar type-safe di KMP |
| **Database Lokal** | **AndroidX Room KMP** | `2.8.4` | ORM SQLite resmi: Entities, DAOs, Database Builder |
| **SQLite Driver** | **SQLite Bundled** | `2.7.0` | Driver SQLite embedded untuk konsistensi query SQLite lintas OS |
| **Key-Value Storage**| **Multiplatform Settings** | `1.3.0` | Penyimpanan token & preferensi (`SharedPreferences` / `NSUserDefaults`) |
| **Networking** | **Ktor Client** | `3.1.1` | HTTP client multiplatform (OkHttp di Android, Darwin di iOS) |
| **Network Plugins** | **ContentNegotiation, Auth, Logging** | `3.1.1` | JSON serialization, Bearer token interceptor, HTTP traffic logger |
| **Serialisasi Data** | **Kotlinx Serialization JSON** | `1.8.0` | Parsing JSON performa tinggi dengan type safety |
| **Dependency Injection** | **Koin Multiplatform** | `4.2.2` | Service locator & ViewModel injection (`koin-compose-viewmodel`) |
| **Image Loading** | **Coil 3** | `3.5.0` | Asynchronous image loading & memory caching di CMP |
| **Image Compression** | **Native Bitmap & CoreGraphics** | Native | Pipeline kompresi bertingkat adaptif menjamin ukuran file < 1 MB |
| **Secret Management** | **BuildKonfig** | `0.22.0` | Generate konstanta Base URL & rahasia via `local.properties` |
| **Peta Interaktif** | **Leaflet JS + OpenStreetMap** | `1.9.4` | Peta open-source tanpa kuota/kunci API berbayar |
| **Annotation Processing**| **Google KSP** | `2.3.11` | Kompiler anotasi Room saat compile-time |
| **Concurrency** | **Kotlinx Coroutines** | `1.11.0` | Asynchronous programming (`StateFlow`, `SharedFlow`, Channels) |

---

## 🏗️ Arsitektur Aplikasi & Alur Data

Proyek ini dirancang mengikuti panduan resmi arsitektur modern Android & Kotlin Multiplatform menggunakan **Clean Architecture** dan **MVVM (Model-View-ViewModel)** dengan **Unidirectional Data Flow (UDF)**:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose Multiplatform)                 │
│  Screens: LoginScreen, RegisterScreen, HomeScreen, DetailScreen,        │
│           AddStoryScreen, MapScreen, SavedStoriesScreen, ProfileScreen  │
└────────────────────────────────────▲────────────────────────────────────┘
                                     │ (StateFlow / UI Events)
┌────────────────────────────────────▼────────────────────────────────────┐
│                             ViewModel Layer                             │
│   AuthViewModel, HomeViewModel, DetailViewModel, AddStoryViewModel,    │
│                 MapViewModel, SavedStoriesViewModel                    │
└────────────────────────────────────▲────────────────────────────────────┘
                                     │ (Flow<ApiResult<T>>)
┌────────────────────────────────────▼────────────────────────────────────┐
│                            Repository Layer                             │
│       StoryRepository, AuthRepository, ThemeRepository, GeocodingRepo   │
└────────────────────────────────────▲────────────────────────────────────┘
                      ┌──────────────┴──────────────┐
                      ▼                             ▼
┌────────────────────────────────────────┐ ┌──────────────────────────────┐
│           Remote Data Source           │ │    Local Database Source     │
│   • Ktor 3 StoryApiService             │ │   • AndroidX Room AppDatabase│
│   • BuildKonfig.BASE_URL (Encrypted)   │ │     - StoryDao (Feed Cache)  │
│   • OpenStreetMap Nominatim Geocoding  │ │     - BookmarkDao (Favorites)│
│                                        │ │     - OfflineDraftDao(Outbox)│
│                                        │ │   • Multiplatform Settings   │
│                                        │ │     - TokenStorage (JWT)     │
│                                        │ │     - Theme & Font Preference│
└────────────────────────────────────────┘ └──────────────────────────────┘
```

### Prinsip Alur Data Satu Arah (UDF):
1. **User Action:** Interaksi pengguna pada layar UI (seperti menekan tombol unggah atau mengetik pencarian) memicu event ke `ViewModel`.
2. **State Management:** `ViewModel` memanggil `Repository`, mengelola `StateFlow`, dan mengabarkan state UI (`Loading`, `Success`, `Error`).
3. **Reactive UI:** Komponen Composable mengamati `StateFlow` dan melakukan render ulang secara otomatis sesuai state terbaru.

---

## 📸 Deep Dive: Pipeline Kompresi Gambar (< 1 MB)

Salah satu tantangan terbesar pada aplikasi berbagi foto adalah mencegah error jaringan karena batas ukuran file upload di server (maksimal 1 MB), sekaligus menjaga aplikasi agar tidak mengalami *OutOfMemory (OOM)* pada perangkat dengan kamera modern beresolusi tinggi (48 MP hingga 108 MP).

```
   Foto Kamera / Galeri (10 MB - 50 MB)
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│ 1. Pembacaan EXIF Orientation (Auto-Rotate)         │
│    Mendeteksi rotasi kamera asli (90°, 180°, 270°)  │
└──────────────────┬──────────────────────────────────┘
                   ▼
┌─────────────────────────────────────────────────────┐
│ 2. Subsampling Memory-Safe (inSampleSize)           │
│    Mencegah crash OOM sebelum memuat bitmap utuh    │
└──────────────────┬──────────────────────────────────┘
                   ▼
┌─────────────────────────────────────────────────────┐
│ 3. Downscaling Proporsional (Max Dimension 1280px)  │
│    Mempertahankan rasio aspek asli gambar           │
└──────────────────┬──────────────────────────────────┘
                   ▼
┌─────────────────────────────────────────────────────┐
│ 4. Kompresi Kualitas JPEG Bertahap (Iterative)      │
│    Looping reduksi kualitas (85% -> 75% -> ... 15%) │
└──────────────────┬──────────────────────────────────┘
                   ▼
    Ukuran <= 950 KB? ────► [TIDAK] ──► 5. Adaptive Downscale (Resize 80%)
           │                                          │
        [YA] ◄────────────────────────────────────────┘
           ▼
   Target Tercapai (< 950 KB) -> Tampilkan Badge Ukuran di UI & Siap Upload!
```

### Implementasi Spesifik Platform:
* **Android (`ImagePicker.android.kt`):**
  * Menggunakan `android.media.ExifInterface` untuk menjaga orientasi gambar selalu tegak.
  * Menggunakan `BitmapFactory.Options.inJustDecodeBounds = true` untuk menghitung rasio pemotongan memori (`inSampleSize`) tanpa mengalokasikan RAM besar.
  * Menggunakan matriks transformasi dan `Bitmap.createScaledBitmap` untuk resolusi adaptif.
* **iOS (`ImagePicker.ios.kt`):**
  * Menggunakan `UIGraphicsBeginImageContextWithOptions` dan CoreGraphics untuk melakukan render ulang gambar secara proporsional.
  * Menggunakan `UIImageJPEGRepresentation` dengan reduksi faktor kualitas bertahap hingga ukuran data berada di bawah batas target.

---

## 💾 Deep Dive: Mode Offline & Outbox Queue (Room KMP)

Aplikasi mengimplementasikan pola **Offline-First with Network-Bound Resource** menggunakan **AndroidX Room KMP**:

### 1. Struktur Database (`AppDatabase.kt`)
* **`StoryEntity`:** Menyimpan cache feed cerita komunitas (ID, nama, deskripsi, photoUrl, createdAt, lat, lon).
* **`BookmarkEntity`:** Menyimpan daftar cerita yang ditandai sebagai favorit secara lokal beserta timestamp penandaan.
* **`OfflineDraftEntity`:** Menyimpan cerita yang dibuat saat offline:
  * ID unik (UUID generator).
  * Deskripsi cerita.
  * Foto terkompresi yang di-encode ke format **Base64** di dalam database SQLite.
  * Titik koordinat lokasi (`lat`, `lon`) dan nama tempat (`locationName`).
  * Status pengunggah (`isGuest`).

### 2. Mekanisme Sinkronisasi Outbox:
* Saat pengguna menekan "Unggah" tanpa adanya jaringan internet, repository otomatis menyimpan cerita ke `OfflineDraftDao`.
* Pengguna mendapatkan notifikasi visual bahwa cerita disimpan di antrean offline.
* Pada halaman **Tersimpan & Draft**, pengguna dapat melihat seluruh daftar draft dan memicu sinkronisasi massal (*batch upload*) ketika koneksi internet telah kembali normal.

---

## 🛡️ Deep Dive: Keamanan & Hiding Base URL (BuildKonfig)

Untuk mematuhi standar keamanan aplikasi modern dan mencegah kebocoran alamat API maupun secret key di repositori terbuka (seperti GitHub), Base URL tidak ditulis di dalam kode Kotlin.

### Cara Kerja:
1. File **[local.properties](file:///Users/mac/StudioProjects/Story/local.properties)** (yang otomatis terdaftar pada `.gitignore`) menampung variabel konfigurasi:
   ```properties
   STORY_API_BASE_URL=https://story-api.dicoding.dev/v1/
   ```
2. Plugin **`com.codingfeline.buildkonfig`** pada `shared/build.gradle.kts` membaca variabel tersebut saat proses build:
   ```kotlin
   val storyApiBaseUrl: String = localProps.getProperty("STORY_API_BASE_URL")
       ?: (project.findProperty("STORY_API_BASE_URL") as? String)
       ?: ""

   buildkonfig {
       packageName = "com.learn.story"
       defaultConfigs {
           buildConfigField(STRING, "BASE_URL", storyApiBaseUrl)
       }
   }
   ```
3. Kompiler men-generate berkas `BuildKonfig.kt` internal di dalam modul `shared` (`commonMain`):
   ```kotlin
   internal object BuildKonfig {
       public val BASE_URL: String = "https://story-api.dicoding.dev/v1/"
   }
   ```
4. `HttpClientFactory.kt` menggunakan `BuildKonfig.BASE_URL` secara aman:
   ```kotlin
   defaultRequest {
       url(BuildKonfig.BASE_URL)
   }
   ```
5. **Dukungan CI/CD:** Pada server CI/CD (seperti GitHub Actions), Base URL dapat di-inject secara langsung tanpa membuat file melalui parameter Gradle:
   ```bash
   ./gradlew assembleDebug -PSTORY_API_BASE_URL="https://story-api.dicoding.dev/v1/"
   ```

---

## 🗺️ Deep Dive: Peta Interaktif (Leaflet JS + OpenStreetMap)

Sebagai alternatif modern dari Google Maps SDK yang membutuhkan kartu kredit dan konfigurasi API key kompleks:
* **Ekosistem Terbuka:** Menggunakan **Leaflet JS 1.9.4** dan ubin peta **OpenStreetMap Tile Layer**.
* **Komponen Bersama `LeafletMapView`:**
  * Di Android: Dirender menggunakan `android.webkit.WebView` dengan JavaScript Interface `AndroidBridge`.
  * Di iOS: Dirender menggunakan `WebKit.WKWebView` dengan `WKScriptMessageHandler`.
* **Dukungan Fitur Peta:**
  * Penandaan multi-marker untuk semua cerita yang memiliki koordinat GPS.
  * Tampilan popup info nama pengunggah dan cuplikan cerita saat marker disentuh.
  * Pemilihan lokasi interaktif: Sentuhan pada peta mengirimkan koordinat lintang & bujur kembali ke antarmuka Compose.

---

## 📂 Struktur Direktori Proyek

```text
Story/
├── androidApp/                               # Modul Entry Point Aplikasi Android
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml               # Izin Internet, Kamera, Lokasi & FileProvider
│       ├── res/xml/file_paths.xml            # Konfigurasi URI FileProvider Kamera
│       └── kotlin/com/learn/story/
│           ├── MainActivity.kt               # Android Host Activity
│           └── StoryApp.kt                   # Application Class
│
├── iosApp/                                   # Proyek Aplikasi iOS (Xcode & SwiftUI)
│   ├── iosApp/
│   │   ├── Info.plist                        # Izin Kamera & Galeri (NSCameraUsageDescription)
│   │   ├── iOSApp.swift                      # SwiftUI App Lifecycle
│   │   └── ContentView.swift                 # UIViewControllerRepresentable Host
│   └── iosApp.xcodeproj                     # Xcode Project
│
├── shared/                                   # Modul Bersama Utama (Kotlin Multiplatform)
│   ├── build.gradle.kts                      # Konfigurasi KMP, KSP, Room & BuildKonfig
│   └── src/
│       ├── commonMain/                       # 100% Kode Bersama Cross-Platform
│       │   ├── composeResources/             # Font Poppins, Gambar Vektor, String XML
│       │   └── kotlin/com/learn/story/
│       │       ├── App.kt                    # Root Composable & Inisialisasi Tema
│       │       ├── data/
│       │       │   ├── local/                # Database AndroidX Room KMP
│       │       │   │   ├── AppDatabase.kt    # Definisi Database & Expect Constructor
│       │       │   │   ├── DatabaseBuilder.kt# Expect fungsi pembuatan RoomDatabase
│       │       │   │   ├── dao/              # StoryDao, BookmarkDao, OfflineDraftDao
│       │       │   │   └── entity/           # StoryEntity, BookmarkEntity, OfflineDraftEntity
│       │       │   ├── model/                # Story, User, OfflineStoryDraft, ApiResult
│       │       │   ├── network/              # HttpClientFactory (Ktor 3 + BuildKonfig)
│       │       │   ├── remote/               # StoryApiService, GeocodingService
│       │       │   ├── repository/           # StoryRepository, AuthRepository, ThemeRepository
│       │       │   └── storage/              # TokenStorage (Multiplatform Settings)
│       │       ├── di/
│       │       │   └── AppModule.kt          # Koin DI Module (Network, Database, Repos, VMs)
│       │       └── ui/
│       │           ├── components/           # Custom Buttons, TextFields, Cards, MapView
│       │           │   └── map/              # LeafletMapView, Marker Models, HTML Generator
│       │           ├── navigation/           # Navigation Compose NavHost & AppScreen Sealed Class
│       │           ├── picker/               # Expect functions for Camera & Gallery Pickers
│       │           ├── screens/              # Login, Register, Home, Detail, Add, Map, Saved
│       │           └── theme/                # Material 3 Colors, ThemeMode, Poppins Typography
│       │
│       ├── androidMain/                      # Implementasi Spesifik Android
│       │   ├── AndroidManifest.xml           # ContextProvider Manifest
│       │   └── kotlin/com/learn/story/
│       │       ├── data/local/               # DatabaseBuilder.android.kt (Context provider)
│       │       ├── data/network/             # OkHttp Engine Factory
│       │       ├── ui/components/map/        # LeafletMapView.android.kt (Android WebView)
│       │       └── ui/picker/                # ImagePicker.android.kt (EXIF & Bitmap Compress)
│       │
│       ├── iosMain/                          # Implementasi Spesifik iOS
│       │   └── kotlin/com/learn/story/
│       │       ├── MainViewController.kt     # Compose UIViewController untuk SwiftUI
│       │       ├── data/local/               # DatabaseBuilder.ios.kt (NSDocumentDirectory)
│       │       ├── data/network/             # Darwin Engine Factory
│       │       ├── ui/components/map/        # LeafletMapView.ios.kt (WKWebView & ScriptBridge)
│       │       └── ui/picker/                # ImagePicker.ios.kt (CoreGraphics & JPEG Compress)
│       │
│       └── commonTest/                       # Unit Test Cross-Platform
│           └── kotlin/com/learn/story/
│               └── SharedCommonTest.kt       # Pengujian Model, Tanggal, Peta, & Theme
│
├── gradle/
│   └── libs.versions.toml                    # Centralized Version Catalog
├── local.properties.example                  # Template konfigurasi rahasia untuk developer
└── README.md                                 # Dokumentasi lengkap proyek
```

---

## 🚀 Setup & Panduan Menjalankan Project

### Prasyarat Sistem
1. **Java Development Kit (JDK):** JDK 17 atau **JDK 21** (direkomendasikan *Azul Zulu 21*).
2. **Android Studio:** Android Studio Ladybug (2024.2+) atau versi lebih baru dengan plugin **Kotlin Multiplatform Mobile**.
3. **Xcode:** Xcode 15+ (diperlukan hanya jika ingin menjalankan target iOS pada macOS).
4. **CocoaPods / Xcode Command Line Tools:** Pastikan `xcode-select --install` sudah terpasang.

---

### Langkah Menjalankan Aplikasi

#### 1. Clone Repositori
```bash
git clone <repository-url>
cd Story
```

#### 2. Konfigurasi Environment & Base URL (`local.properties`)
Salin template `local.properties.example` menjadi berkas `local.properties`:
```bash
cp local.properties.example local.properties
```

Buka `local.properties` dan pastikan konfigurasi path Android SDK dan Story API Base URL telah sesuai:
```properties
sdk.dir=/Users/your-username/Library/Android/sdk
STORY_API_BASE_URL=https://story-api.dicoding.dev/v1/
```

> [!TIP]
> Jalankan perintah `./gradlew :shared:generateBuildKonfig` untuk memverifikasi bahwa class type-safe `BuildKonfig.kt` berhasil dibuat.

#### 3. Menjalankan di Android
* **Melalui Android Studio:**
  1. Buka folder proyek di Android Studio.
  2. Tunggu proses *Gradle Sync* selesai.
  3. Pilih konfigurasi run `androidApp` pada toolbar atas.
  4. Pilih Emulator atau Perangkat Fisik Android (aktifkan *USB Debugging*), lalu tekan **Run** (▶).
* **Melalui Terminal:**
  ```bash
  ./gradlew :androidApp:installDebug
  ```

#### 4. Menjalankan di iOS
* **Melalui Xcode:**
  1. Buka file `iosApp/iosApp.xcodeproj` di Xcode.
  2. Pilih target Simulator iOS (misalnya *iPhone 16 Pro*).
  3. Tekan tombol **Run** (Cmd + R).
* **Melalui Android Studio:**
  1. Pastikan plugin *Kotlin Multiplatform Mobile* aktif.
  2. Pilih konfigurasi `iosApp` dan pilih target simulator yang tersedia.
  3. Klik tombol **Run**.

---

## 🧪 Pengujian Unit & Verifikasi (Testing)

Aplikasi dilengkapi dengan rangkaian pengujian unit cross-platform di `shared/src/commonTest` ([SharedCommonTest.kt](file:///Users/mac/StudioProjects/Story/shared/src/commonTest/kotlin/com/learn/story/SharedCommonTest.kt)) yang mencakup:
* **Verifikasi Model Data:** Validasi integritas atribut `Story` dan `OfflineStoryDraft`.
* **Pemformatan Waktu:** Pengujian parser tanggal ISO-8601 ke format ramah pengguna (`YYYY-MM-DD • HH:mm`).
* **HTML Generator Peta:** Memverifikasi script Leaflet JS, URL CDN, layer OpenStreetMap, dan script bridge komunikasi cross-platform.
* **Theme Repository:** Pengujian persistensi preferensi tema (`Light`, `Dark`, `System`) menggunakan `MapSettings` in-memory.

### Menjalankan Unit Test:
```bash
# Menjalankan unit test di lingkungan JVM Android
./gradlew :shared:testDebugUnitTest

# Menjalankan unit test di lingkungan iOS Simulator (macOS)
./gradlew :shared:iosSimulatorArm64Test

# Menjalankan seluruh pengujian multiplatform
./gradlew :shared:allTests
```

---

## 💻 Perintah Gradle yang Sering Digunakan

| Tugas | Perintah |
| :--- | :--- |
| **Generate BuildKonfig (Base URL & Config)** | `./gradlew :shared:generateBuildKonfig` |
| **Kompilasi Shared Module (Semua Target)** | `./gradlew :shared:assemble` |
| **Kompilasi Android Debug Library** | `./gradlew :shared:assembleDebug` |
| **Build APK Android Debug** | `./gradlew :androidApp:assembleDebug` |
| **Install APK ke Perangkat Android** | `./gradlew :androidApp:installDebug` |
| **Link Framework iOS (Simulator Arm64)** | `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` |
| **Jalankan Unit Test Android** | `./gradlew :shared:testDebugUnitTest` |
| **Jalankan Unit Test iOS Simulator** | `./gradlew :shared:iosSimulatorArm64Test` |
| **Bersihkan Seluruh Build Cache** | `./gradlew clean` |

---

## ❓ Tanya Jawab & Pemecahan Masalah (FAQ)

### 1. `Unresolved reference: BuildKonfig` saat pertama kali clone?
* **Penyebab:** Objek `BuildKonfig` di-generate secara otomatis saat proses build dan tidak disimpan di git.
* **Solusi:** Pastikan Anda telah membuat `local.properties` (lihat langkah 2) lalu jalankan perintah:
  ```bash
  ./gradlew :shared:generateBuildKonfig
  ```

### 2. Peringatan Gradle Daemon out of JVM Metaspace?
* **Penyebab:** Kompilasi Kotlin Multiplatform dengan KSP dan Compose membutuhkan kapasitas memory metaspace yang cukup.
* **Solusi:** Tambahkan konfigurasi berikut pada berkas `gradle.properties`:
  ```properties
  org.gradle.jvmargs=-Xmx6144m -XX:MaxMetaspaceSize=2048m
  ```

### 3. Izin Kamera atau Galeri tidak muncul di iOS?
* **Penyebab:** Deskripsi izin privasi Apple belum terbaca di `Info.plist`.
* **Solusi:** Pastikan kunci `NSCameraUsageDescription` dan `NSPhotoLibraryUsageDescription` sudah terisi di [iosApp/iosApp/Info.plist](file:///Users/mac/StudioProjects/Story/iosApp/iosApp/Info.plist).

### 4. Mengapa peta tidak membutuhkan Google Maps API Key?
* Aplikasi menggunakan pustaka terbuka **Leaflet JS** dan ubin peta **OpenStreetMap**, sehingga dapat langsung digunakan tanpa batasan kuota berbayar atau keharusan memasukkan billing kartu kredit.

---

## 📄 Lisensi
Proyek ini dibuat dan dikembangkan untuk keperluan edukasi, implementasi arsitektur bersih (*Clean Architecture*), dan percontohan aplikasi modern kelas produksi berbasis **Kotlin Multiplatform**.