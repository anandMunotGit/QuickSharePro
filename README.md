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

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📸 Screenshots

*(You can add your application screenshots below)*
