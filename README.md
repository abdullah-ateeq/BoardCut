# BoardCut 🪵✂️

**BoardCut** is a modern, high-precision Android application built with **Jetpack Compose** and **Material Design 3** that optimizes 1D linear cutting stock lists for woodworking, metalworking, carpentry, piping, framing, and manufacturing.

It solves the classic **Linear Cutting Stock Problem (1D Bin Packing)** to minimize raw material waste, accurately accounting for saw blade thickness (*kerf loss*), end-trim squared edges, and reusable offcut classification.

---

## 🌟 Key Features

- 📐 **1D Linear Cutting Stock Optimization**
  - Advanced greedy/first-fit bin-packing cutting engine optimized for minimal scrap waste.
  - Generates an optimal sequential cutting plan across multiple stock boards.

- 🪚 **Kerf & Trim Allowance Compensation**
  - **Saw Kerf Loss:** Accounts for blade thickness on every cut to eliminate sizing errors on the workbench.
  - **Trim Allowance:** Accommodates squaring off rough board ends prior to primary cuts.

- 📏 **Multi-Unit System & Real-Time Conversion**
  - Supports **Millimeters (`mm`)**, **Centimeters (`cm`)**, **Inches (`in`)**, and **Feet (`ft`)**.
  - Intelligent default kerf and trim settings tailored per unit.
  - Seamless unit switching with auto-converted length inputs.

- 📊 **Visual Board Cutting Diagrams**
  - Interactive composable diagram representing each stock board.
  - Displays color-coded cut pieces, kerf gaps, trim margins, and remaining offcuts/scrap.
  - Includes cut sequence numbers and millimeter positioning overlays.

- ♻️ **Smart Offcut Categorization**
  - Distinguishes between **Reusable Offcuts** (green threshold) and **Scrap Waste**.
  - Configurable minimum reusable length setting.

- 📁 **Project Presets & Saved History**
  - Includes pre-configured templates (e.g., 5-Tier Hardwood Bookshelf, 2x4 Garage Workbench, Garden Picnic Bench).
  - Save, edit, duplicate, and reload custom cutting projects with persistent state storage.

- 📄 **Export & Share Cutting Lists**
  - Export comprehensive formatted cutting plans as **Text Summaries** or **CSV Spreadsheets** ready for workshop execution.

---

## 📱 Tech Stack & Architecture

- **Language:** Kotlin 100%
- **UI Framework:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Domain Layer
- **State Management:** Kotlin Coroutines & `StateFlow`
- **Build System:** Gradle (Kotlin DSL `build.gradle.kts` + Version Catalog `libs.versions.toml`)
- **Android Target:** SDK 37 (Min SDK 24)

---

## 🏗️ Project Structure

```text
BoardCut/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/boardcut/
│   │   │   │   ├── data/             # Project & Settings Repositories, Presets
│   │   │   │   ├── engine/           # CuttingOptimizer engine & PlanExporter
│   │   │   │   ├── model/            # Domain models (CutPiece, StockBoard, OptimizationResult)
│   │   │   │   ├── ui/               # Jetpack Compose UI (Screens, Components, Theme)
│   │   │   │   └── viewmodel/        # BoardCutViewModel state holder
│   │   │   └── AndroidManifest.xml
│   │   └── test/                     # Comprehensive Unit Test Suite
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml            # Dependency Version Catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🧪 Testing

The cutting engine is verified with comprehensive unit test coverage:

- **Kerf Calculation:** Tests precise kerf loss accounting and trailing cut logic.
- **Waste Calculation:** Verifies exact material utilization percentage and offcut splitting.
- **Stock Capacity:** Handles multi-board allocation and stock board overflow handling.
- **Unit Conversions:** Cross-validates unit transformation algorithms (`mm`, `cm`, `in`, `ft`).
- **Impossible Cuts:** Gracefully validates edge cases where cut pieces exceed stock capacity.

Run unit tests via Gradle:
```bash
./gradlew test
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 11 or higher
- Android Device or Emulator running Android 7.0 (API 24) or higher

### Build and Run
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/BoardCut.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle project files.
4. Run on an attached device or emulator (`app`).

---

## 📄 License

This project is open-source under the MIT License.
