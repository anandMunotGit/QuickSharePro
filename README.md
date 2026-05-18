<div align="center">
  <h1>QuickShare Pro 🚀</h1>
  <p><strong>A lightning-fast, secure, offline peer-to-peer file sharing application for Android.</strong></p>
</div>

<br/>

**QuickShare Pro** is an open-source, production-ready Android application built to share files seamlessly between devices without requiring an active internet connection. It utilizes **Wi-Fi Direct (P2P)** to achieve transfer speeds up to 50x faster than traditional Bluetooth, ensuring large files like high-definition videos, heavy documents, and entire folders transfer in seconds.

Designed with modern Android development standards, this app boasts a clean architecture, reactive UI, and robust background handling mechanisms to ensure uninterrupted transfers even when the app is minimized.

---

## ✨ Key Features

- ⚡ **Ultra-Fast Transfers:** Powered by Wi-Fi Direct (802.11 P2P), reaching speeds of 30+ MB/s depending on hardware.
- 📶 **100% Offline:** No internet, mobile data, or external network required. Devices connect directly to each other.
- 📱 **Modern UI/UX:** Built entirely with **Jetpack Compose** and **Material Design 3** for a buttery-smooth, responsive user experience.
- 🔄 **Flawless Background Execution:** Safely minimizes to the background using **Foreground Services**. Integrates `PowerManager.PARTIAL_WAKE_LOCK` and `WifiManager.WIFI_MODE_FULL_HIGH_PERF` to defeat Android's Doze mode and prevent connection drops.
- 📂 **Universal File Picker:** Easily select multiple photos, videos, audio files, or any document using the native Android `MediaStore` and Storage Access Framework.
- 🔁 **Resilient Error Recovery:** Automatically detects distance-based socket drops and provides a clean UI flow to retain file selections and retry the connection.
- 🔐 **Privacy First:** Only requests necessary permissions (handling Android 13+ granular media and notification permissions seamlessly). No tracking, no ads.

---

## 🛠️ Tech Stack & Architecture

QuickShare Pro is built using the latest industry standards for Android development:

*   **Language:** [Kotlin](https://kotlinlang.org/) (100%)
*   **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Architecture:** Model-View-ViewModel (MVVM) + Clean Architecture Principles
*   **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
*   **Asynchrony & State:** Kotlin Coroutines and `StateFlow`
*   **Networking:** Raw TCP Sockets via `java.net.Socket` and `ServerSocket` over Wi-Fi Direct (`WifiP2pManager`)
*   **Background Work:** Foreground Services with persistent notifications.

### How It Works Under the Hood
1. **Discovery Phase:** The sender initiates a `WifiP2pManager.discoverPeers()` call. The receiver advertises itself.
2. **Connection Phase:** Upon selection, the sender initiates a Wi-Fi Direct connection, establishing a Group Owner (usually the receiver).
3. **Socket Phase:** The Group Owner opens a `ServerSocket`. The client connects via a standard TCP socket.
4. **Transfer Phase:** A custom binary protocol (`TransferEngine`) transmits the file metadata (name, size) followed by the raw byte streams in highly optimized chunks.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Iguana (or newer)
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 34 (Android 14)
- Two physical Android devices (Wi-Fi Direct cannot be reliably tested on standard Android Emulators).

### Installation
1. Clone the repository:
   ```bash
<div align="center">
  <h1>QuickShare Pro 🚀</h1>
  <p><strong>A lightning-fast, secure, offline peer-to-peer file sharing application for Android.</strong></p>
</div>

<br/>

**QuickShare Pro** is an open-source, production-ready Android application built to share files seamlessly between devices without requiring an active internet connection. It utilizes **Wi-Fi Direct (P2P)** to achieve transfer speeds up to 50x faster than traditional Bluetooth, ensuring large files like high-definition videos, heavy documents, and entire folders transfer in seconds.

Designed with modern Android development standards, this app boasts a clean architecture, reactive UI, and robust background handling mechanisms to ensure uninterrupted transfers even when the app is minimized.

---

## ✨ Key Features

- ⚡ **Ultra-Fast Transfers:** Powered by Wi-Fi Direct (802.11 P2P), reaching speeds of 30+ MB/s depending on hardware.
- 📶 **100% Offline:** No internet, mobile data, or external network required. Devices connect directly to each other.
- 📱 **Modern UI/UX:** Built entirely with **Jetpack Compose** and **Material Design 3** for a buttery-smooth, responsive user experience.
- 🔄 **Flawless Background Execution:** Safely minimizes to the background using **Foreground Services**. Integrates `PowerManager.PARTIAL_WAKE_LOCK` and `WifiManager.WIFI_MODE_FULL_HIGH_PERF` to defeat Android's Doze mode and prevent connection drops.
- 📂 **Universal File Picker:** Easily select multiple photos, videos, audio files, or any document using the native Android `MediaStore` and Storage Access Framework.
- 🔁 **Resilient Error Recovery:** Automatically detects distance-based socket drops and provides a clean UI flow to retain file selections and retry the connection.
- 🔐 **Privacy First:** Only requests necessary permissions (handling Android 13+ granular media and notification permissions seamlessly). No tracking, no ads.

---

## 🛠️ Tech Stack & Architecture

QuickShare Pro is built using the latest industry standards for Android development:

*   **Language:** [Kotlin](https://kotlinlang.org/) (100%)
*   **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Architecture:** Model-View-ViewModel (MVVM) + Clean Architecture Principles
*   **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
*   **Asynchrony & State:** Kotlin Coroutines and `StateFlow`
*   **Networking:** Raw TCP Sockets via `java.net.Socket` and `ServerSocket` over Wi-Fi Direct (`WifiP2pManager`)
*   **Background Work:** Foreground Services with persistent notifications.

### How It Works Under the Hood
1. **Discovery Phase:** The sender initiates a `WifiP2pManager.discoverPeers()` call. The receiver advertises itself.
2. **Connection Phase:** Upon selection, the sender initiates a Wi-Fi Direct connection, establishing a Group Owner (usually the receiver).
3. **Socket Phase:** The Group Owner opens a `ServerSocket`. The client connects via a standard TCP socket.
4. **Transfer Phase:** A custom binary protocol (`TransferEngine`) transmits the file metadata (name, size) followed by the raw byte streams in highly optimized chunks.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Iguana (or newer)
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 34 (Android 14)
- Two physical Android devices (Wi-Fi Direct cannot be reliably tested on standard Android Emulators).

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/QuickSharePro.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run the app on **two** physical devices to test the file-sharing functionality.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! 
If you find a bug or have an idea for an enhancement (like byte-level transfer resuming), feel free to open an issue or submit a Pull Request.

---

## 📸 Screenshots

<p align="center">
  <img width="22%" alt="img1" src="https://github.com/user-attachments/assets/7d55a73e-19de-4cf3-a0a2-b1b05ba130ad" />
  <img width="22%" alt="img2" src="https://github.com/user-attachments/assets/263e3788-6894-4b95-a420-f62765f1a64c" />
  <img width="22%" alt="img3" src="https://github.com/user-attachments/assets/27d99990-a347-4461-a4b7-cabe808d07ac" />
  <img width="22%" alt="img4" src="https://github.com/user-attachments/assets/c9090d35-3e3a-4f5c-b446-21deeff2c0ef" />
</p>
<p align="center">
  <img width="22%" alt="img5" src="https://github.com/user-attachments/assets/5de624a1-648b-4646-b631-34b5644e6872" />
  <img width="22%" alt="img6" src="https://github.com/user-attachments/assets/76ee9d70-02cb-4397-899d-d0b52b09e063" />
  <img width="22%" alt="img7" src="https://github.com/user-attachments/assets/50701194-3c10-4a05-b714-8927f3bfd80a" />
  <img width="22%" alt="img8" src="https://github.com/user-attachments/assets/14ea4194-5452-4321-8a45-577ccc240b60" />
</p>
