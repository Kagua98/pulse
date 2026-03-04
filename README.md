# Pulse

**A comprehensive, cross-platform developer tools suite for Kotlin Multiplatform (KMP) applications.**

```
[version-badge]  [kotlin-badge]  [platform-badge]  [license-badge]
```

Pulse is a fully-featured, in-app developer toolbox that provides network inspection, log viewing, crash reporting, memory leak detection, device diagnostics, and performance monitoring -- all from a single library with zero configuration. Built with Compose Multiplatform, Pulse works seamlessly across Android, Desktop (JVM), and iOS targets.

**Key value propositions:**

- **One library, all platforms** -- Pulse runs on every KMP target. No more juggling Chucker for Android and separate tools for Desktop.
- **Zero configuration** -- Install the Ktor plugin (or OkHttp interceptor) and wrap your app with `PulseOverlay`. That is all it takes.
- **In-memory by design** -- Sensitive network data never touches disk. Pulse stores everything in memory with configurable retention limits.
- **Debug-only enforcement** -- On Android, Pulse actively crashes if included in a release build, preventing accidental production exposure.
- **Rich, themeable UI** -- Five built-in dark themes, smooth animations, and a professional inspector interface built entirely with Material 3 and Compose.
- **Beyond network inspection** -- Logs, crashes, memory leaks, device info, performance metrics, JWT decoding, cURL export, and PDF generation in a single tool.

---

## Table of Contents

1. [Why Pulse?](#why-pulse)
2. [Feature Comparison](#feature-comparison)
3. [Architecture Overview](#architecture-overview)
4. [Getting Started](#getting-started)
5. [Configuration](#configuration)
6. [Features Deep Dive](#features-deep-dive)
    - [Network Inspector](#network-inspector)
    - [Log Viewer](#log-viewer)
    - [Crash Reporter](#crash-reporter)
    - [Memory Leak Detection](#memory-leak-detection)
    - [Device Info Panel](#device-info-panel)
    - [Performance Monitor](#performance-monitor)
    - [Export (TXT, PDF, Share)](#export-txt-pdf-share)
    - [JWT Token Decoding](#jwt-token-decoding)
    - [cURL Command Generation](#curl-command-generation)
    - [Theme System](#theme-system)
    - [Quick Settings Tile](#quick-settings-tile)
    - [Access Modes](#access-modes)
    - [Collapsible Content](#collapsible-content)
7. [Security](#security)
8. [How Pulse Captures Network Traffic](#how-pulse-captures-network-traffic)
9. [Upcoming Features / Roadmap](#upcoming-features--roadmap)
10. [API Reference](#api-reference)
11. [FAQ](#faq)
12. [Contributing](#contributing)
13. [License](#license)

---

## Why Pulse?

### The Problems

Debugging a Kotlin Multiplatform application is harder than it should be. Consider the daily challenges:

- **Network issues are invisible.** When an API call fails, you either stare at logcat output or wire up verbose logging interceptors. On Desktop and iOS, you have even fewer options.
- **Logs are ephemeral.** Log statements scroll past in the console and are gone. Filtering by tag or severity requires custom tooling.
- **Crashes vanish.** An uncaught exception on a background thread might flash by before you can read the stack trace. On non-Android targets, there is no Firebase Crashlytics equivalent running locally.
- **Memory leaks are silent.** LeakCanary exists for Android, but accessing its data requires switching apps. On other KMP targets, leak detection is largely absent.
- **Device context requires separate lookups.** Checking OS version, memory stats, or app version during debugging means leaving your IDE to open Settings.
- **Performance is a black box.** CPU usage, RAM consumption, and frame rate are not surfaced in any convenient way during development.

### Why Existing Tools Fall Short for KMP

| Tool | Limitation for KMP |
|------|-------------------|
| **Chucker** | Android-only. Does not run on Desktop or iOS targets. |
| **Flipper** | Requires a separate desktop application running alongside. Android-focused. Discontinued by Meta. |
| **Stetho** | Chrome DevTools bridge. Android-only, deprecated, no longer maintained. |
| **OkHttp Logging Interceptor** | Text output only. No UI. OkHttp-specific (does not work with Ktor on non-JVM targets). |

None of these tools provide a unified, in-app debugging experience across all Kotlin Multiplatform targets.

### The Philosophy

Pulse follows three guiding principles:

1. **One tool, all platforms.** Write your debugging workflow once. It works on Android, Desktop, and iOS without platform-specific setup.
2. **Zero configuration.** Pulse is useful the instant you add it. Sane defaults for everything. Override only when you need to.
3. **In-app, always available.** No external applications, no browser DevTools, no USB debugging. The developer tools live inside your app, accessible via a floating button, notification, or shake gesture.

---

## Feature Comparison

The following table compares Pulse against popular Android debugging libraries. Pulse is the only tool designed from the ground up for Kotlin Multiplatform.

| Feature | Pulse | Chucker | Flipper | Stetho | OkHttp Logging |
|---|:---:|:---:|:---:|:---:|:---:|
| **Android Support** | Yes | Yes | Yes | Yes | Yes |
| **Desktop (JVM) Support** | Yes | -- | -- | -- | Partial |
| **iOS Support** | Yes | -- | -- | -- | -- |
| **Network Inspection** | Yes | Yes | Yes | Yes | Yes |
| **Request/Response Bodies** | Yes | Yes | Yes | Yes | Yes |
| **JSON Pretty-Printing** | Yes | Yes | Yes | Yes | -- |
| **Search and Filter** | Yes | Yes | Yes | Partial | -- |
| **Status Code Filters** | Yes | Partial | -- | -- | -- |
| **Log Viewer** | Yes | -- | Yes | Yes | -- |
| **Log Level Filtering** | Yes | -- | Partial | -- | -- |
| **Crash Reporter** | Yes | -- | Yes | -- | -- |
| **Memory Leak Detection** | Yes | -- | Partial | -- | -- |
| **Device Info Panel** | Yes | -- | Yes | -- | -- |
| **Performance Monitor (CPU/RAM/FPS)** | Yes | -- | Partial | -- | -- |
| **Export as TXT** | Yes | Yes | -- | -- | -- |
| **Export as PDF** | Yes | -- | -- | -- | -- |
| **Share via System Sheet** | Yes | Yes | -- | -- | -- |
| **JWT Token Decoding** | Yes | -- | -- | -- | -- |
| **cURL Command Generation** | Yes | Yes | -- | -- | -- |
| **Theme Customization** | Yes | -- | -- | -- | -- |
| **Quick Settings Tile** | Yes | Yes | -- | -- | -- |
| **Multiple Access Modes** | Yes | Partial | -- | -- | -- |
| **FAB Overlay** | Yes | -- | -- | -- | -- |
| **Notification Access** | Yes | Yes | -- | -- | -- |
| **Shake Gesture Access** | Yes | -- | -- | -- | -- |
| **Ktor Support** | Yes | -- | -- | -- | -- |
| **OkHttp Support** | Yes | Yes | Yes | Yes | Yes |
| **Collapsible Headers** | Yes | -- | -- | -- | -- |
| **Multi-Select Export** | Yes | -- | -- | -- | -- |
| **Debug-Only Enforcement** | Yes | Yes | -- | -- | -- |
| **In-Memory Storage** | Yes | -- | -- | -- | -- |
| **Sensitive Header Redaction** | Yes | Partial | -- | -- | -- |
| **Data Retention Policies** | Yes | -- | -- | -- | -- |
| **Compose Multiplatform UI** | Yes | -- | -- | -- | -- |
| **No External App Required** | Yes | Yes | -- | -- | Yes |

**Figure 1:** Feature comparison between Pulse and popular Android debugging libraries.

---

## Architecture Overview

Pulse follows a layered, modular architecture. The common (shared) code defines all models, stores, UI, and interception logic. Platform-specific implementations use Kotlin's `expect`/`actual` mechanism to provide native behavior for crash handling, device info, leak detection, notifications, shake detection, and more.

```
+------------------------------------------------------------------+
|                        Application Layer                         |
|  +------------------------------------------------------------+  |
|  |  PulseOverlay (Composable wrapper)                         |  |
|  |    +-- DraggableFab / NotificationAccess / ShakeDetector   |  |
|  |    +-- PulseScreen (inspector)                             |  |
|  +------------------------------------------------------------+  |
+------------------------------------------------------------------+
|                          UI Layer                                 |
|  +--------------------+  +-------------------+  +--------------+ |
|  | PulseHome          |  | TransactionList   |  | LogViewer    | |
|  | (Dashboard)        |  | TransactionDetail |  | CrashViewer  | |
|  +--------------------+  +-------------------+  +--------------+ |
|  +--------------------+  +-------------------+  +--------------+ |
|  | LeakScreen         |  | DeviceInfoScreen  |  | Settings     | |
|  +--------------------+  +-------------------+  +--------------+ |
|  +--------------------+  +-------------------+                   |
|  | PerformanceOverlay |  | ExportDialog      |                   |
|  +--------------------+  +-------------------+                   |
+------------------------------------------------------------------+
|                       Core Layer (commonMain)                     |
|  +------------------+  +------------------+  +-----------------+ |
|  | Pulse (Singleton)|  | PulseConfig      |  | PulseAccessMode | |
|  | - transactions   |  | - enabled        |  | - Fab           | |
|  | - logs           |  | - maxTransactions|  | - Notification  | |
|  | - crashes        |  | - maxContentLen  |  | - ShakeGesture  | |
|  +------------------+  +------------------+  +-----------------+ |
|  +------------------+  +------------------+  +-----------------+ |
|  | TransactionStore |  | LogStore         |  | CrashStore      | |
|  | (InMemory impl)  |  | (InMemory impl)  |  | (InMemory impl) | |
|  +------------------+  +------------------+  +-----------------+ |
|  +------------------+  +------------------+  +-----------------+ |
|  | PulseKtorPlugin  |  | SecurityManager  |  | JwtDecoder      | |
|  | (HttpClientPlugin)|  | (Header redact)  |  | (Token decode)  | |
|  +------------------+  +------------------+  +-----------------+ |
+------------------------------------------------------------------+
|                  Platform Layer (expect/actual)                   |
|  +------------------+  +------------------+  +-----------------+ |
|  | CrashHandler     |  | DeviceInfo       |  | LeakDetector    | |
|  | ShakeDetector    |  | PerformanceMonitor|  | PdfExporter    | |
|  | ShareHandler     |  | NotificationHelper|  | DebugGuard     | |
|  | BackHandler      |  | StatusBarEffect  |  | TimeSource      | |
|  +------------------+  +------------------+  +-----------------+ |
|       androidMain          desktopMain           nativeMain      |
+------------------------------------------------------------------+
```

**Figure 2:** Pulse architecture overview showing the layered design across common and platform source sets.

### Core Components

| Component | Responsibility |
|-----------|---------------|
| `Pulse` object | Singleton entry point. Holds references to all stores, exposes logging/crash APIs, and manages global configuration state. |
| `TransactionStore` | Interface for storing HTTP transactions. Default implementation (`InMemoryTransactionStore`) uses `MutableStateFlow` with configurable max size. |
| `LogStore` | In-memory store for log entries with level, tag, message, and optional throwable. Max 2000 entries by default. |
| `CrashStore` | In-memory store for crash entries. Max 50 entries by default. |
| `PulseKtorPlugin` | Implements Ktor's `HttpClientPlugin` interface to intercept all HTTP traffic. |
| `SecurityManager` | Manages sensitive header redaction and data retention policies. |
| `PulseConfig` | Configuration DSL with `enabled`, `maxTransactions`, and `maxContentLength` settings. |

### Data Flow: Network Request Lifecycle

The following describes how a network request flows through Pulse from interception to display:

```
1. App makes HTTP request via Ktor HttpClient
       |
2. PulseKtorPlugin.intercept() captures:
   - Request method, URL, host, path, scheme
   - Request headers (as Map<String, String>)
   - Request body (up to maxContentLength bytes)
   - Start timestamp
       |
3. Request is executed via execute(request)
       |
4. Response is saved via call.save() for multi-read
       |
5. Plugin captures response data:
   - Status code and message
   - Response headers
   - Response body (up to maxContentLength bytes)
   - Duration (endTime - startTime)
       |
6. HttpTransaction object is created and added to TransactionStore
       |
7. TransactionStore.transactions StateFlow emits updated list
       |
8. UI (TransactionListScreen) collects the StateFlow and renders
```

**Figure 3:** Data flow for network request interception and display.

---

## Getting Started

### Installation

Add Pulse to your module's `build.gradle.kts`. Use `debugImplementation` to ensure Pulse is never included in release builds.

```kotlin
// build.gradle.kts (shared module or app module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Pulse for debug builds
            implementation("io.pulse:pulse:<version>")
        }
    }
}
```

For Android projects using the standard Gradle dependency configurations:

```kotlin
// build.gradle.kts (app module)
dependencies {
    debugImplementation("io.pulse:pulse:<version>")
    releaseImplementation("io.pulse:pulse-noop:<version>")
}
```

> **Note:** The `pulse-noop` artifact provides empty stub implementations of all public APIs, ensuring zero overhead in release builds with no code changes required.

### Step 1: Install the Ktor Plugin

If your application uses Ktor for networking, install the `PulseKtorPlugin` on your `HttpClient`:

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.pulse.PulseKtorPlugin

val httpClient = HttpClient(CIO) {
    install(PulseKtorPlugin) {
        // Optional: customize configuration
        enabled = true
        maxTransactions = 500
        maxContentLength = 1_000_000L // 1 MB
    }
}
```

Every request and response flowing through this `HttpClient` will now be automatically captured and displayed in the Pulse inspector.

### Step 2: Wrap Your App with PulseOverlay

In your top-level composable, wrap your application content with `PulseOverlay`:

```kotlin
import io.pulse.ui.PulseOverlay

@Composable
fun App() {
    PulseOverlay {
        // Your application content
        MyAppContent()
    }
}
```

That is it. A draggable floating action button ("P") will appear in the bottom-right corner of your app. Tap it to open the Pulse inspector.

<!-- Screenshot: PulseOverlay FAB button visible in the bottom-right corner of the app -->

### Step 3 (Optional): Add Logging

Use the Pulse logging API anywhere in your shared code:

```kotlin
import io.pulse.Pulse

// Standard log levels
Pulse.v("Network", "Connecting to server...")
Pulse.d("Auth", "Token refreshed successfully")
Pulse.i("Sync", "Sync completed: 42 records")
Pulse.w("Cache", "Cache miss for key: user_profile")
Pulse.e("Payment", "Payment failed", exception)
```

### Setup for OkHttp / Retrofit

If your project uses OkHttp (common in Android-only modules or Retrofit setups), Pulse provides an OkHttp interceptor:

```kotlin
import io.pulse.PulseOkHttpInterceptor
import okhttp3.OkHttpClient

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(PulseOkHttpInterceptor())
    .build()
```

For Retrofit:

```kotlin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient) // OkHttpClient with PulseOkHttpInterceptor
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

### Standalone Logging (No Network)

Pulse can be used purely as a log viewer and crash reporter without any network interception:

```kotlin
import io.pulse.Pulse
import io.pulse.ui.PulseOverlay

// Just wrap your app -- no Ktor or OkHttp setup needed
@Composable
fun App() {
    PulseOverlay {
        MyAppContent()
    }
}

// Use the logging API throughout your codebase
fun someFunction() {
    Pulse.i("MyFeature", "Feature initialized")
    try {
        riskyOperation()
    } catch (e: Exception) {
        Pulse.e("MyFeature", "Operation failed", e)
    }
}
```

---

## Configuration

### Global Configuration

Use `Pulse.configure {}` to adjust global settings at application startup:

```kotlin
import io.pulse.Pulse

// Typically called in your Application.onCreate() or equivalent
Pulse.configure {
    enabled = true              // Enable/disable Pulse entirely (default: true)
    maxTransactions = 1000      // Max HTTP transactions to retain (default: 500)
    maxContentLength = 2_000_000L  // Max body size to capture in bytes (default: 1 MB)
}
```

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | `Boolean` | `true` | Master switch. When `false`, the Ktor plugin skips interception and the overlay is hidden. |
| `maxTransactions` | `Int` | `500` | Maximum number of HTTP transactions retained in memory. Oldest entries are dropped first. Options: 500, 1000, 2000, 5000. |
| `maxContentLength` | `Long` | `1_000_000` | Maximum request/response body size (in bytes) to capture. Bodies exceeding this limit are replaced with a size placeholder. |

### Access Modes

Pulse supports three ways for developers to open the inspector. Set the access mode globally or via the `PulseOverlay` composable:

```kotlin
import io.pulse.Pulse
import io.pulse.PulseAccessMode

// Set globally
Pulse.accessMode = PulseAccessMode.Fab

// Or pass directly to PulseOverlay
PulseOverlay(
    accessMode = PulseAccessMode.ShakeGesture,
) {
    MyAppContent()
}
```

| Mode | Description | Platforms |
|------|-------------|-----------|
| `PulseAccessMode.Fab` | Draggable floating action button overlay. Default mode. | Android, Desktop, iOS |
| `PulseAccessMode.Notification` | Persistent notification in the notification shade. Tapping opens the inspector. | Android only |
| `PulseAccessMode.ShakeGesture` | Shake the device to open the inspector. | Android only |

> **Note:** On non-Android platforms, `Notification` and `ShakeGesture` modes are no-ops. The FAB mode is recommended for cross-platform consistency.

### Theme Customization

Pulse ships with five built-in dark themes. Change the theme at runtime:

```kotlin
import io.pulse.Pulse
import io.pulse.ui.theme.PulseTheme

Pulse.currentTheme = PulseTheme.Ocean
```

| Theme | Description |
|-------|-------------|
| `PulseTheme.Purple` | Default. Deep purple/violet dark theme. |
| `PulseTheme.Ocean` | Deep navy blue dark theme. |
| `PulseTheme.Forest` | Dark green/emerald theme. |
| `PulseTheme.Sunset` | Warm amber/orange dark theme. |
| `PulseTheme.Ruby` | Deep red/crimson dark theme. |

All themes maintain consistent status-code coloring (green for 2xx, blue for 3xx, orange for 4xx, red for 5xx) regardless of the selected palette.

### Security Settings

Configure sensitive header redaction and data retention:

```kotlin
import io.pulse.internal.SecurityManager

// Enable header redaction (disabled by default)
SecurityManager.redactSensitiveHeaders = true

// Add custom headers to the redaction list
SecurityManager.addSensitiveHeader("X-Custom-Token")
SecurityManager.addSensitiveHeader("X-Session-Id")

// Remove a header from the redaction list
SecurityManager.removeSensitiveHeader("cookie")

// Set data retention (auto-clear after duration in milliseconds; 0 = never)
SecurityManager.dataRetentionMs = 30 * 60 * 1000L // 30 minutes
```

**Default sensitive headers** that are redacted when `redactSensitiveHeaders` is enabled:
- `Authorization`
- `Cookie`
- `Set-Cookie`
- `X-Api-Key`
- `X-Auth-Token`
- `Proxy-Authorization`
- `WWW-Authenticate`

When redaction is active, sensitive header values are displayed as `--------` in the Pulse UI.

### Performance Overlay

Toggle the real-time performance overlay:

```kotlin
import io.pulse.Pulse

Pulse.showPerformanceOverlay = true
```

This overlay displays live CPU usage, RAM consumption, and FPS metrics. It can also be toggled from the Android Quick Settings tile.

---

## Features Deep Dive

### Network Inspector

The network inspector is the centerpiece of Pulse. It provides a complete view of all HTTP traffic flowing through your application.

**What it does:**
- Captures every HTTP request and response made through Ktor's `HttpClient` (or OkHttp)
- Displays method, URL, status code, duration, and response size in a scrollable list
- Provides detailed views with Overview, Request, and Response tabs
- Supports search by URL, host, method, path, or status code
- Supports filtering by status code range (2xx, 3xx, 4xx, 5xx, Error)
- Color-codes HTTP methods (GET = green, POST = blue, PUT = orange, PATCH = purple, DELETE = red)
- Color-codes status codes (2xx = green, 3xx = blue, 4xx = orange, 5xx = red)

**How it works internally:**

The `PulseKtorPlugin` implements Ktor's `HttpClientPlugin` interface and intercepts requests using `HttpSend.intercept`. For each request:

1. A unique ID is generated using `Uuid.random()`
2. Request details (headers, body, content type) are captured before execution
3. The request is executed via `execute(request)`
4. If the request fails, a `Failed` transaction is recorded with the error message
5. On success, `call.save()` is called to allow reading the response body multiple times
6. Response details are captured (status, headers, body, timing)
7. An `HttpTransaction` object is created and added to the `TransactionStore`

**Data captured per transaction:**

| Field | Description |
|-------|-------------|
| `method` | HTTP method (GET, POST, PUT, PATCH, DELETE, etc.) |
| `url` | Full request URL |
| `host` | Host portion of the URL |
| `path` | Path portion of the URL |
| `scheme` | URL scheme (http, https) |
| `requestHeaders` | Map of request header names to values |
| `requestBody` | Request body text (up to `maxContentLength`) |
| `requestContentType` | Content-Type of the request |
| `requestSize` | Size of the request body in bytes |
| `responseCode` | HTTP status code (200, 404, 500, etc.) |
| `responseMessage` | HTTP status message ("OK", "Not Found", etc.) |
| `responseHeaders` | Map of response header names to values |
| `responseBody` | Response body text (up to `maxContentLength`) |
| `responseContentType` | Content-Type of the response |
| `responseSize` | Size of the response body in bytes |
| `duration` | Request duration in milliseconds |
| `timestamp` | Unix timestamp (epoch milliseconds) when the request was initiated |
| `error` | Error message if the request failed |
| `status` | `Requested`, `Complete`, or `Failed` |

**Code example:**

```kotlin
val client = HttpClient(CIO) {
    install(PulseKtorPlugin)
}

// All requests are automatically captured
val response = client.get("https://api.example.com/users")
```

<!-- Screenshot: Network inspector showing a list of HTTP transactions with method badges, status codes, and timing -->

<!-- Screenshot: Transaction detail screen showing Overview, Request, and Response tabs -->

**Multi-select export:**

Long-press a transaction in the list to enter selection mode. Select multiple transactions and export them all at once.

<!-- Screenshot: Multi-select mode with several transactions selected and the Export button visible -->

**Figure 4:** The network inspector list view with search, status filters, and color-coded transaction entries.

---

### Log Viewer

**What it does:**
- Displays all log entries recorded via the `Pulse.v()`, `Pulse.d()`, `Pulse.i()`, `Pulse.w()`, and `Pulse.e()` APIs
- Supports filtering by log level (VERBOSE, DEBUG, INFO, WARN, ERROR)
- Supports text search across tag and message fields
- Expandable entries that show full message text, timestamp, and attached stack traces
- Selectable text in expanded entries for easy copying
- Maximum retention of 2000 log entries (oldest are dropped first)

**How it works internally:**

Each call to `Pulse.log()` creates a `LogEntry` with a unique UUID, log level, tag, message, optional throwable stack trace, and timestamp. The entry is prepended to the `LogStore`'s `MutableStateFlow`, and the UI collects this flow to render the list.

**Code example:**

```kotlin
import io.pulse.Pulse

// Verbose -- very detailed diagnostic information
Pulse.v("HTTP", "Sending request to /api/v2/users with 3 query params")

// Debug -- useful for development
Pulse.d("Auth", "Token expires in 3600 seconds, scheduling refresh")

// Info -- normal operational messages
Pulse.i("Sync", "Background sync completed: 142 records in 2.3s")

// Warn -- potentially harmful situations
Pulse.w("Cache", "Cache entry expired, falling back to network")

// Error -- errors with optional throwable
try {
    parseResponse(data)
} catch (e: JsonParseException) {
    Pulse.e("Parser", "Failed to parse API response", e)
}
```

<!-- Screenshot: Log viewer screen showing entries at different log levels with color-coded level badges -->

<!-- Screenshot: Expanded log entry showing full message and stack trace -->

**Figure 5:** The log viewer with level filter chips and expandable entries.

---

### Crash Reporter

**What it does:**
- Automatically captures uncaught exceptions across all threads
- Records exception class, message, full stack trace, thread name, and timestamp
- Displays crashes in a dedicated screen with expandable stack traces
- Stack traces are rendered in monospace font and are fully selectable for copying

**How it works internally:**

When the `Pulse` singleton is initialized, it calls `installCrashHandler()`, which is an `expect` function with platform-specific implementations. On Android, this installs a `Thread.UncaughtExceptionHandler` that records the crash via `Pulse.recordCrash()` before delegating to the original handler. The crash data is stored in the `CrashStore` as a `CrashEntry`.

**Data captured per crash:**

| Field | Description |
|-------|-------------|
| `exceptionClass` | Simple class name of the thrown exception |
| `message` | Exception message string |
| `stackTrace` | Full stack trace as a string |
| `threadName` | Name of the thread where the crash occurred |
| `timestamp` | Unix timestamp (epoch milliseconds) |

**Code example:**

Crash recording is automatic. However, you can also manually record crashes for exceptions you catch but want to track:

```kotlin
import io.pulse.Pulse

try {
    dangerousOperation()
} catch (e: Exception) {
    // Record the crash without re-throwing
    Pulse.recordCrash(Thread.currentThread().name, e)

    // Handle the error gracefully
    showErrorUI()
}
```

<!-- Screenshot: Crash viewer screen showing a list of caught exceptions with expandable stack traces -->

**Figure 6:** The crash reporter with expandable stack traces and thread information.

---

### Memory Leak Detection

**What it does:**
- Integrates with LeakCanary on Android to provide memory leak detection directly within the Pulse inspector
- Displays the count of currently retained objects
- Provides a "Dump Heap" action to trigger heap analysis
- Includes a launcher to open LeakCanary's detailed analysis UI
- Shows informational text explaining how LeakCanary detects leaks

**How it works internally:**

The `LeakDetector` module uses `expect`/`actual` declarations. On Android, the actual implementation queries LeakCanary's `AppWatcher` for retained object counts and triggers heap dumps via LeakCanary's APIs. On Desktop and iOS (native), leak detection is reported as unavailable, and the screen displays an appropriate message.

**Platform availability:**

| Platform | Leak Detection |
|----------|---------------|
| Android (debug) | Full support via LeakCanary 2.14 |
| Desktop (JVM) | Not available -- screen shows informational message |
| iOS (native) | Not available -- screen shows informational message |

**Code example:**

No code is required. LeakCanary is included as an `androidMain` dependency and is automatically initialized. The Pulse UI provides buttons to interact with it:

```kotlin
// The LeakCanary dependency is already declared in Pulse's build.gradle.kts:
// androidMain.dependencies {
//     implementation("com.squareup.leakcanary:leakcanary-android:2.14")
// }

// Just open the Pulse inspector and navigate to "Leaks"
```

<!-- Screenshot: Leak screen showing retained object count and Dump Heap action button -->

**Figure 7:** The memory leak detection screen showing retained object count and available actions.

---

### Device Info Panel

**What it does:**
- Displays comprehensive information about the device, operating system, and application
- Data is organized into labeled sections (e.g., App Info, Device, OS, Memory)
- All text is selectable for easy copying
- Information is gathered via platform-specific `expect`/`actual` implementations

**How it works internally:**

The `rememberDeviceInfoSections()` composable function is an `expect` declaration. On Android, the actual implementation reads from `Build`, `PackageManager`, `ActivityManager`, and `Runtime` APIs. On Desktop, it reads JVM system properties. On iOS, it reads from `UIDevice` and `ProcessInfo`. Each implementation returns a list of `InfoSection` objects containing title-value pairs.

**Typical information displayed (Android):**

| Section | Fields |
|---------|--------|
| App Info | Package name, version name, version code, build type |
| Device | Manufacturer, model, brand, hardware |
| OS | Android version, SDK level, security patch |
| Memory | Total RAM, available RAM, used RAM, heap size |

<!-- Screenshot: Device info screen showing organized sections with selectable key-value pairs -->

**Figure 8:** The device info panel with organized sections and selectable values.

---

### Performance Monitor

**What it does:**
- Displays a real-time, draggable overlay showing CPU usage, RAM consumption, and FPS
- CPU and RAM metrics include progress bars with color-coded thresholds (green/yellow/red)
- FPS counter changes color based on frame rate (green >= 55, yellow >= 30, red < 30)
- The overlay can be dismissed with a close button
- Can be toggled via `Pulse.showPerformanceOverlay` or the Android Quick Settings tile

**How it works internally:**

The `rememberPerformanceSnapshots()` composable is an `expect` function. On Android, the actual implementation polls system metrics (CPU via `/proc/stat`, memory via `ActivityManager`, FPS via Choreographer frame callbacks). On Desktop, it reads JVM runtime memory stats and system load. The data is emitted as a `PerformanceSnapshot` containing `cpuUsagePercent`, `memoryUsedMb`, `memoryTotalMb`, `memoryUsagePercent`, and `fps`.

**Color thresholds:**

| Metric | Green | Yellow | Red |
|--------|-------|--------|-----|
| CPU | < 50% | 50-80% | > 80% |
| RAM | < 60% | 60-85% | > 85% |
| FPS | >= 55 | 30-54 | < 30 |

**Code example:**

```kotlin
import io.pulse.Pulse

// Enable the performance overlay programmatically
Pulse.showPerformanceOverlay = true

// Disable it
Pulse.showPerformanceOverlay = false
```

<!-- Screenshot: Performance overlay showing CPU 23%, RAM 156/512 MB, and 60 FPS with color-coded indicators -->

**Figure 9:** The draggable performance overlay showing live CPU, RAM, and FPS metrics.

---

### Export (TXT, PDF, Share)

**What it does:**
- Exports individual or multiple HTTP transactions in various formats
- **Copy to Clipboard** -- Copies the full transaction details as formatted text
- **Share as Text** -- Opens the system share sheet with the transaction text
- **Export as TXT File** -- Creates a .txt file and shares it via the system share sheet
- **Export as PDF** -- Generates a formatted PDF document and shares it (Android)
- Supports bulk export when multiple transactions are selected

**How it works internally:**

The `ExportDialog` composable presents the export options. Text export is handled by `TransactionExporter.exportAsSingleText()` and `exportAsText()` which generate structured plain-text representations. PDF generation uses the platform-specific `generateTransactionsPdf()` expect function, which on Android uses the `android.graphics.pdf.PdfDocument` API. Sharing is handled by the platform-specific `shareText()` and `shareFile()` functions.

**Export text format example:**

```
=======================================
  Pulse - Transaction Export
=======================================

--- General ---
URL:      https://api.example.com/users/42
Method:   GET
Scheme:   https
Host:     api.example.com
Path:     users/42
Time:     14:32:07

--- Status ---
Code:     200
Message:  OK
Duration: 234 ms
Status:   Complete

--- Request ---
Content-Type: application/json
Size:         0 B

Request Headers:
  Authorization: Bearer eyJhbGci...
  Accept: application/json

--- Response ---
Content-Type: application/json
Size:         1.2 KB

Response Headers:
  Content-Type: application/json
  Cache-Control: no-cache

Response Body:
{
  "id": 42,
  "name": "Jane Doe",
  "email": "jane@example.com"
}

=======================================
```

<!-- Screenshot: Export dialog showing options for Share as Text, Export as TXT File, Export as PDF, and Copy to Clipboard -->

**Figure 10:** The export dialog with multiple format options.

---

### JWT Token Decoding

**What it does:**
- Automatically detects JWT tokens in request/response headers and bodies
- Decodes the JWT header and payload (base64url decoding)
- Pretty-prints the decoded JSON
- Extracts standard claims: `exp` (expiration), `iat` (issued at), `iss` (issuer), `sub` (subject)
- Indicates whether the token is expired

**How it works internally:**

The `JwtDecoder` utility uses a regex pattern to identify potential JWT tokens (three base64url-encoded segments separated by dots). For each candidate, it verifies that the header segment decodes to valid JSON. It then decodes both header and payload segments using Kotlin's `Base64.UrlSafe` decoder, pretty-prints the JSON, and extracts standard claims using lightweight regex-based parsing (no JSON library dependency).

**Code example:**

```kotlin
import io.pulse.util.decodeJwt
import io.pulse.util.isJwtToken
import io.pulse.util.findJwtTokens

val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

// Check if a string is a JWT
val isJwt = isJwtToken(token) // true

// Decode a JWT
val decoded = decodeJwt(token)
// decoded?.header  -> {"alg": "HS256", "typ": "JWT"}
// decoded?.payload -> {"sub": "1234567890", "name": "John Doe", "iat": 1516239022}
// decoded?.isExpired -> false
// decoded?.issuer -> null
// decoded?.subject -> "1234567890"

// Find all JWTs in a block of text
val tokens = findJwtTokens(responseBody)
// Returns List<Pair<IntRange, String>> -- position ranges and token strings
```

**Figure 11:** JWT tokens detected in authorization headers are automatically decoded and displayed.

---

### cURL Command Generation

**What it does:**
- Generates a valid cURL command for any captured HTTP transaction
- Includes all request headers, body, and URL
- The generated command can be copied to clipboard with a single tap
- Useful for replaying requests in terminal or sharing with teammates

**How it works internally:**

The `toCurlCommand()` extension function on `HttpTransaction` builds a cURL string by iterating over the request method, headers, body, and URL. Single quotes in the body are properly escaped.

**Generated cURL example:**

```bash
curl -X POST \
  -H 'Authorization: Bearer eyJhbGci...' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{"name": "Jane Doe", "email": "jane@example.com"}' \
  'https://api.example.com/users'
```

**Figure 12:** The cURL button on the transaction detail screen copies a ready-to-use command.

---

### Theme System

**What it does:**
- Provides five visually distinct dark themes that change the entire Pulse inspector UI
- Themes are applied via Compose's `CompositionLocalProvider` for efficient propagation
- Status-code colors (success, redirect, client error, server error) remain consistent across themes
- HTTP method colors are theme-independent for maximum readability

**How it works internally:**

Each theme is defined as a `PulseColorPalette` data class containing ten color tokens: `surface`, `surfaceVariant`, `onSurface`, `onSurfaceDim`, `divider`, `searchBackground`, `success`, `redirect`, `clientError`, and `serverError`. The active palette is provided via `LocalPulseColors`, a `staticCompositionLocalOf`. The `PulseColors` object provides `@Composable` getters that read from the local composition, allowing seamless theme switching without restarting the inspector.

**Available themes:**

| Theme | Surface Color | Accent |
|-------|-------------|--------|
| Purple (default) | `#1E1E2E` | Violet/purple tones |
| Ocean | `#0D1B2A` | Deep navy blue |
| Forest | `#0D1F12` | Dark emerald green |
| Sunset | `#1F1408` | Warm amber/brown |
| Ruby | `#1F0D0D` | Deep crimson/red |

**Code example:**

```kotlin
import io.pulse.Pulse
import io.pulse.ui.theme.PulseTheme

// Switch themes at runtime
Pulse.currentTheme = PulseTheme.Forest

// Available themes
PulseTheme.entries.forEach { theme ->
    println("${theme.label}: ${theme.name}")
}
// Output:
// Purple: Purple
// Ocean: Ocean
// Forest: Forest
// Sunset: Sunset
// Ruby: Ruby
```

<!-- Screenshot: Side-by-side comparison of the Purple and Ocean themes in the network inspector -->

**Figure 13:** Pulse themes provide distinct visual identities while maintaining readability.

---

### Quick Settings Tile

**What it does:**
- Adds a "Pulse" tile to the Android Quick Settings panel
- Tapping the tile toggles the performance overlay on/off
- The tile state (active/inactive) syncs with `Pulse.showPerformanceOverlay`
- When activated, the tile brings the app to the foreground

**How it works internally:**

The `PulseTileService` extends Android's `TileService` and is registered in the `AndroidManifest.xml` with the `QS_TILE` intent filter. When clicked, it toggles `PulseTileState.isActive` and syncs this state with `Pulse.showPerformanceOverlay`. It then uses `startActivityAndCollapse()` to bring the app to the foreground.

**Setup:**

No additional setup is required. The tile service is declared in Pulse's `AndroidManifest.xml` and is automatically merged into your app's manifest. Users can add the "Pulse" tile to their Quick Settings panel by editing their tile layout.

<!-- Screenshot: Android Quick Settings panel showing the Pulse tile in active state -->

**Figure 14:** The Quick Settings tile provides instant access to the performance monitor.

---

### Access Modes

**What it does:**
- Provides three distinct mechanisms for opening the Pulse inspector
- **FAB (Floating Action Button):** A draggable circular button that overlays your app content. Shows a transaction count badge. Works on all platforms.
- **Notification:** A persistent notification in the Android notification shade. Tapping opens the inspector.
- **Shake Gesture:** Shaking the physical device opens the inspector. Useful when you do not want visible UI elements.

**How it works internally:**

The `PulseOverlay` composable switches between access mechanisms based on the `accessMode` parameter:

- **FAB mode** renders a `DraggableFab` composable that uses `pointerInput` with `detectDragGestures` for drag support. The FAB's position is persisted in `FabOffsetHolder` so it survives inspector open/close cycles.
- **Notification mode** invokes the `NotificationAccessEffect` composable (an `expect`/`actual` function), which on Android creates a persistent notification via `NotificationHelper`.
- **Shake mode** invokes the `ShakeDetectorEffect` composable, which on Android uses `SensorManager` to detect shake gestures.

**Code example:**

```kotlin
import io.pulse.PulseAccessMode
import io.pulse.ui.PulseOverlay

@Composable
fun App() {
    PulseOverlay(
        enabled = true,
        accessMode = PulseAccessMode.Fab, // or Notification, ShakeGesture
    ) {
        MyAppContent()
    }
}
```

The access mode can also be changed at runtime from the Settings screen within the Pulse inspector.

<!-- Screenshot: Settings screen showing the Access Mode radio buttons for FAB, Notification, and Shake Gesture -->

**Figure 15:** Access mode can be changed at runtime via the Settings screen.

---

### Collapsible Content

**What it does:**
- Long lists of HTTP headers are automatically collapsed when there are more than 5 entries
- The first 3 headers are shown with a "Show N more..." toggle
- Expanding/collapsing uses smooth `AnimatedVisibility` transitions
- Log entries and crash stack traces are expandable via tap interactions

**How it works internally:**

The `HeadersPanel` composable checks the header count against `COLLAPSE_THRESHOLD` (5). If the count exceeds the threshold, only `COLLAPSED_VISIBLE_COUNT` (3) headers are shown initially. A clickable "Show N more..." label toggles the remaining headers via `AnimatedVisibility` with `expandVertically()`/`shrinkVertically()` animations. Similarly, log entries in `LogViewerScreen` and crash entries in `CrashViewerScreen` track their expanded state and render full content conditionally.

<!-- Screenshot: Headers panel showing 3 visible headers with a "Show 8 more..." toggle link -->

**Figure 16:** Headers are collapsed by default to keep the UI manageable, with smooth expand/collapse animations.

---

## Security

Pulse is a developer tool that handles sensitive data (API tokens, session cookies, request/response bodies). Security is built into the design at every level.

### Debug-Only Enforcement

On Android, Pulse includes a `DebugGuard` that checks `ApplicationInfo.FLAG_DEBUGGABLE` at initialization. If Pulse is included in a non-debuggable (release) build, it throws an `IllegalStateException` immediately:

```
IllegalStateException: Pulse must only be used in debug builds!
Use debugImplementation instead of implementation in your build.gradle.
```

This is an intentional hard crash that prevents accidental inclusion of Pulse in production APKs.

### Recommended Gradle Setup

Always use `debugImplementation` to scope Pulse to debug builds only:

```kotlin
dependencies {
    // Pulse is ONLY included in debug builds
    debugImplementation("io.pulse:pulse:<version>")

    // No-op artifact for release builds (empty stubs, zero overhead)
    releaseImplementation("io.pulse:pulse-noop:<version>")
}
```

### Sensitive Header Redaction

When `SecurityManager.redactSensitiveHeaders` is enabled, Pulse replaces the values of known authentication-related headers with a placeholder in the UI:

```kotlin
import io.pulse.internal.SecurityManager

SecurityManager.redactSensitiveHeaders = true
// "Authorization: Bearer eyJhbG..." becomes "Authorization: --------"
```

Default sensitive headers: `Authorization`, `Cookie`, `Set-Cookie`, `X-Api-Key`, `X-Auth-Token`, `Proxy-Authorization`, `WWW-Authenticate`.

### In-Memory Storage

Pulse deliberately avoids disk persistence. All HTTP transactions, logs, and crash entries are stored in `MutableStateFlow` instances backed by in-memory lists. When the process dies, all captured data is lost. This is by design -- sensitive data should never be written to the filesystem in a debugging tool.

### Data Retention Policies

The `SecurityManager.dataRetentionMs` property allows automatic clearing of captured data after a specified duration. By default this is set to `0` (no auto-clear). Additionally, each store has a configurable maximum size:

| Store | Default Max Size |
|-------|-----------------|
| `TransactionStore` | 500 entries |
| `LogStore` | 2000 entries |
| `CrashStore` | 50 entries |

When the maximum is reached, the oldest entries are dropped automatically.

### Content Length Limits

Request and response bodies exceeding `maxContentLength` (default: 1 MB) are not captured. Instead, a placeholder message is recorded: `[Body too large: N bytes]`. This prevents memory exhaustion when large payloads are transferred.

---

## How Pulse Captures Network Traffic

This section provides a detailed technical explanation of how Pulse intercepts network traffic for both Ktor and OkHttp.

### Ktor: HttpClientPlugin Interface

Pulse's Ktor integration is implemented as a first-class Ktor `HttpClientPlugin`. The `PulseKtorPlugin` companion object implements `HttpClientPlugin<PulseConfig, PulseKtorPlugin>`.

**Plugin lifecycle:**

1. **`prepare()`** -- Creates a `PulseConfig` instance and applies the user's configuration block.
2. **`install()`** -- Called by Ktor when the plugin is installed on an `HttpClient`. This is where interception is wired up.

**Interception mechanism:**

```kotlin
scope.plugin(HttpSend).intercept { request ->
    if (!Pulse.enabled) return@intercept execute(request)

    val id = Uuid.random().toString()
    val startTime = epochMillis()

    // 1. Capture request details BEFORE execution
    val requestBody = captureRequestBody(request.body, maxContentLength)
    val requestHeaders = captureHeaders(request)

    // 2. Execute the request
    val originalCall = try {
        execute(request)
    } catch (cause: Exception) {
        // Record failed transaction
        Pulse.store.addTransaction(/* ... Failed status ... */)
        throw cause
    }

    // 3. Save the response for multi-read
    val savedCall = originalCall.save()

    // 4. Capture response details
    val responseBody = captureResponseBody(savedCall, maxContentLength)

    // 5. Record the complete transaction
    Pulse.store.addTransaction(/* ... Complete status ... */)

    savedCall // Return the saved call so downstream code can read the body
}
```

**Key technical details:**

- **`HttpSend.intercept`** is used instead of `HttpResponsePipeline` because it provides access to both the request being sent and the response received, with proper error handling.
- **`call.save()`** is critical. Ktor response bodies are consumable streams by default. Calling `save()` copies the response body into memory so it can be read multiple times -- once by Pulse for logging, and again by the application code.
- **Request body capture** handles multiple `OutgoingContent` subtypes: `ByteArrayContent` is decoded to a string, `ReadChannelContent` and `WriteChannelContent` are recorded as `[Streaming content]`, and `ProtocolUpgrade` is recorded as `[Protocol upgrade]`.
- **Body size limits** prevent memory issues. If a body exceeds `maxContentLength`, the placeholder `[Body too large: N bytes]` is recorded instead of the actual content.

### OkHttp: Interceptor Interface

For OkHttp-based networking, Pulse provides `PulseOkHttpInterceptor` which implements OkHttp's `Interceptor` interface:

```kotlin
class PulseOkHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // Capture request details...

        val response = chain.proceed(request)
        // Use peekBody() for non-destructive response reading
        val responseBody = response.peekBody(maxContentLength)

        // Record transaction...
        return response
    }
}
```

**Key technical details:**

- **`response.peekBody()`** reads the response body without consuming it. This is the OkHttp equivalent of Ktor's `call.save()` -- it allows Pulse to read the body without interfering with the application's ability to read it later.
- The interceptor should be added as an application interceptor (via `addInterceptor()`) rather than a network interceptor to capture the final request/response as seen by the application.

### What Data Is Captured

Both interceptors capture identical data fields, as defined by the `HttpTransaction` model:

```
URL, Method, Scheme, Host, Path
Request: Headers, Body, Content-Type, Size
Response: Status Code, Message, Headers, Body, Content-Type, Size
Timing: Start timestamp, Duration
Error: Exception message (if failed)
Status: Requested -> Complete | Failed
```

---

## Upcoming Features / Roadmap

The following features are planned for future releases of Pulse:

| Feature | Description | Priority |
|---------|-------------|----------|
| **Encrypted Payload Decryption** | Ability to decrypt encrypted request/response bodies using developer-provided keys. Useful for apps that encrypt API payloads end-to-end. | High |
| **WebSocket Inspection** | Real-time monitoring of WebSocket frames (text and binary), connection lifecycle events, and message history. | High |
| **GraphQL Query Visualization** | Parse and display GraphQL queries, mutations, and subscriptions with syntax highlighting. Show query variables and response data in a structured tree view. | Medium |
| **Network Request Mocking** | Intercept and replace responses with mock data directly from the Pulse UI. Useful for testing error scenarios and edge cases without modifying server code. | Medium |
| **Database Inspector** | Browse SQLite/Room databases and SQLDelight schemas. View tables, execute queries, and inspect row data in real time. | Medium |
| **SharedPreferences / DataStore Viewer** | View and edit SharedPreferences and Jetpack DataStore entries from within the Pulse inspector. | Medium |
| **Custom Plugin System** | Extensible plugin architecture allowing third-party developers to add custom inspection panels and data sources to Pulse. | Low |
| **CI/CD Integration** | Export Pulse reports (network logs, crash data) as artifacts in CI/CD pipelines. Generate JUnit-compatible XML reports for automated testing. | Low |
| **Automated Performance Regression Detection** | Track performance metrics over time and alert developers when CPU, memory, or frame rate metrics regress beyond configurable thresholds. | Low |

---

## API Reference

### Pulse Object

The `Pulse` object is the primary entry point for all Pulse functionality.

```kotlin
object Pulse {
    // --- State ---
    var enabled: Boolean                        // Master enable/disable switch
    var accessMode: PulseAccessMode             // How the inspector is opened
    var currentTheme: PulseTheme                // Active color theme
    var showPerformanceOverlay: Boolean          // Toggle performance overlay
    val maxTransactions: Int                     // Current max transaction limit (read-only)

    // --- Data Stores ---
    val store: TransactionStore                  // HTTP transaction store
    val logStore: LogStore                       // Log entry store
    val crashStore: CrashStore                   // Crash entry store

    // --- Reactive Streams ---
    val transactions: StateFlow<List<HttpTransaction>>  // Observable transaction list
    val logs: StateFlow<List<LogEntry>>                 // Observable log list
    val crashes: StateFlow<List<CrashEntry>>            // Observable crash list

    // --- Configuration ---
    fun configure(block: PulseConfig.() -> Unit)  // Apply configuration

    // --- Clearing ---
    fun clear()                                  // Clear ALL data (network + logs + crashes)
    fun clearNetwork()                           // Clear network transactions only
    fun clearLogs()                              // Clear log entries only
    fun clearCrashes()                           // Clear crash entries only

    // --- Logging API ---
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
    fun v(tag: String, message: String)          // VERBOSE
    fun d(tag: String, message: String)          // DEBUG
    fun i(tag: String, message: String)          // INFO
    fun w(tag: String, message: String, throwable: Throwable? = null)  // WARN
    fun e(tag: String, message: String, throwable: Throwable? = null)  // ERROR

    // --- Crash API ---
    fun recordCrash(thread: String, throwable: Throwable)  // Manually record a crash
}
```

### PulseOverlay Composable

```kotlin
@Composable
fun PulseOverlay(
    enabled: Boolean = true,               // Whether the overlay is active
    accessMode: PulseAccessMode = PulseAccessMode.Fab,  // Access mechanism
    content: @Composable () -> Unit,       // Your application content
)
```

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `enabled` | `Boolean` | `true` | Controls whether the access mechanism (FAB/notification/shake) is rendered. |
| `accessMode` | `PulseAccessMode` | `Fab` | Determines how the developer opens the inspector. |
| `content` | `@Composable () -> Unit` | required | Your application's composable content, rendered behind the overlay. |

### PulseKtorPlugin Configuration

```kotlin
val client = HttpClient(engine) {
    install(PulseKtorPlugin) {
        enabled: Boolean = true              // Enable/disable interception
        maxTransactions: Int = 500           // Max transactions to retain
        maxContentLength: Long = 1_000_000L  // Max body size to capture (bytes)
    }
}
```

### PulseAccessMode Enum

```kotlin
enum class PulseAccessMode {
    Fab,            // Draggable FAB overlay (all platforms)
    Notification,   // Persistent notification (Android only)
    ShakeGesture,   // Device shake gesture (Android only)
}
```

### PulseTheme Enum

```kotlin
enum class PulseTheme(val label: String) {
    Purple("Purple"),
    Ocean("Ocean"),
    Forest("Forest"),
    Sunset("Sunset"),
    Ruby("Ruby"),
}
```

### SecurityManager

```kotlin
object SecurityManager {
    val sensitiveHeaders: MutableSet<String>    // Set of header names to redact (lowercase)
    var dataRetentionMs: Long                   // Auto-clear duration (0 = never)
    var redactSensitiveHeaders: Boolean         // Enable/disable redaction

    fun redactHeaderValue(key: String, value: String): String  // Apply redaction to a header
    fun addSensitiveHeader(header: String)      // Add a header to the redaction list
    fun removeSensitiveHeader(header: String)   // Remove a header from the redaction list
}
```

### LogLevel Enum

```kotlin
enum class LogLevel(val label: String) {
    VERBOSE("V"),
    DEBUG("D"),
    INFO("I"),
    WARN("W"),
    ERROR("E"),
}
```

### JWT Utilities

```kotlin
// Check if a string looks like a JWT
fun isJwtToken(text: String): Boolean

// Decode a JWT token into its components
fun decodeJwt(token: String): DecodedJwt?

// Find all JWT tokens in a block of text
fun findJwtTokens(text: String): List<Pair<IntRange, String>>

data class DecodedJwt(
    val header: String,       // Pretty-printed JSON header
    val payload: String,      // Pretty-printed JSON payload
    val signature: String,    // Raw signature string
    val isExpired: Boolean,   // Whether the token has expired
    val expiresAt: Long?,     // Expiration timestamp (epoch seconds)
    val issuedAt: Long?,      // Issued-at timestamp (epoch seconds)
    val issuer: String?,      // Issuer claim
    val subject: String?,     // Subject claim
)
```

### cURL and Export Utilities

```kotlin
// Generate a cURL command from a transaction
fun HttpTransaction.toCurlCommand(): String

// Generate shareable text from a transaction
fun HttpTransaction.toShareText(): String
```

### HttpTransaction Model

```kotlin
data class HttpTransaction(
    val id: String,
    val method: String,
    val url: String,
    val host: String,
    val path: String,
    val scheme: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val requestContentType: String? = null,
    val requestSize: Long = 0L,
    val responseCode: Int? = null,
    val responseMessage: String? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val responseContentType: String? = null,
    val responseSize: Long = 0L,
    val duration: Long = 0L,
    val timestamp: Long = 0L,
    val error: String? = null,
    val status: TransactionStatus = TransactionStatus.Requested,
) {
    val isSuccess: Boolean       // responseCode in 200..299
    val isRedirect: Boolean      // responseCode in 300..399
    val isClientError: Boolean   // responseCode in 400..499
    val isServerError: Boolean   // responseCode in 500..599
    val isFailed: Boolean        // status == Failed
    val responseSummary: String  // Human-readable status summary
}

enum class TransactionStatus {
    Requested,
    Complete,
    Failed,
}
```

### TransactionStore Interface

```kotlin
interface TransactionStore {
    val transactions: StateFlow<List<HttpTransaction>>
    fun addTransaction(transaction: HttpTransaction)
    fun updateTransaction(id: String, update: (HttpTransaction) -> HttpTransaction)
    fun clear()
}
```

---

## FAQ

### General

**Q: Does Pulse work with Kotlin Multiplatform?**

A: Yes. Pulse is built from the ground up for KMP. The `commonMain` source set contains all models, stores, UI (Compose Multiplatform), and Ktor interception logic. Platform-specific features use `expect`/`actual` declarations with implementations in `androidMain`, `desktopMain`, and `nativeMain`.

**Q: Does Pulse require any third-party JSON library?**

A: No. Pulse includes its own lightweight JSON pretty-printer and JWT decoder implemented with pure Kotlin string operations and regex. There is no dependency on kotlinx.serialization, Gson, Moshi, or any other JSON library.

**Q: Will Pulse slow down my app?**

A: Pulse is designed for debug builds and has minimal impact. Network interception adds a small overhead for body capture (using `call.save()` for Ktor and `peekBody()` for OkHttp). The in-memory stores use `StateFlow` for efficient UI updates. The `maxContentLength` limit prevents large payloads from consuming excessive memory.

**Q: Can I use Pulse in production?**

A: No, and this is enforced. On Android, Pulse checks `ApplicationInfo.FLAG_DEBUGGABLE` at initialization and crashes if included in a release build. Use `debugImplementation` in your Gradle configuration to ensure Pulse is never included in production APKs.

### Network Inspection

**Q: Does Pulse support HTTPS?**

A: Yes. Pulse intercepts requests at the application level (not the network level), so it sees the decrypted request/response data regardless of whether the connection uses HTTP or HTTPS.

**Q: What happens with large response bodies?**

A: Bodies exceeding `maxContentLength` (default: 1 MB) are not captured. Instead, Pulse records a placeholder: `[Body too large: N bytes]`. You can increase this limit via `PulseConfig.maxContentLength`, but be aware that very large bodies will increase memory usage.

**Q: Does Pulse support streaming responses?**

A: Streaming request bodies (`ReadChannelContent`, `WriteChannelContent`) are recorded as `[Streaming content]` without capturing the actual stream data. For response bodies, Ktor's `call.save()` buffers the full response, so non-streaming responses are captured normally.

**Q: Can I filter transactions by status code?**

A: Yes. The network inspector provides filter chips for All, 2xx (Success), 3xx (Redirect), 4xx (Client Error), 5xx (Server Error), and Error (connection failures). You can also search by URL, host, method, path, or status code using the search bar.

### Logging

**Q: What is the maximum number of log entries stored?**

A: The `LogStore` retains up to 2000 entries by default. When the limit is reached, the oldest entries are dropped to make room for new ones.

**Q: Can I log exceptions?**

A: Yes. The `Pulse.w()` and `Pulse.e()` methods accept an optional `Throwable` parameter. The exception's stack trace is captured and displayed in the expanded log view.

### Platform-Specific

**Q: Which features are Android-only?**

A: The following features require Android and are no-ops on other platforms:
- Notification access mode
- Shake gesture access mode
- Quick Settings tile
- Memory leak detection (LeakCanary integration)
- PDF export

The FAB access mode, network inspector, log viewer, crash reporter, device info, performance overlay, text export, JWT decoding, and theme system work on all platforms.

**Q: Does the Quick Settings tile require any permissions?**

A: No additional runtime permissions are required. The `PulseTileService` is declared in the library's `AndroidManifest.xml` with the `BIND_QUICK_SETTINGS_TILE` permission, which is a system-level permission that Android grants automatically to tile services.

---

## Contributing

Contributions to Pulse are welcome. Please follow these guidelines:

### Getting Started

1. Fork the repository
2. Create a feature branch from `develop`: `git checkout -b feature/my-feature develop`
3. Make your changes
4. Ensure all targets compile: `./gradlew :lib:pulse:build`
5. Submit a pull request to `develop`

### Code Style

- Follow the existing Kotlin coding conventions in the project
- Use `internal` visibility for implementation details
- Use `expect`/`actual` for any platform-specific functionality
- Place common code in `commonMain`, platform code in `androidMain`, `desktopMain`, or `nativeMain`
- All UI components should use `PulseColors` for theming consistency

### Architecture Guidelines

- **Models** go in `io.pulse.model`
- **Stores** go in `io.pulse.store`
- **UI screens** go in `io.pulse.ui`
- **UI components** go in `io.pulse.ui.components`
- **Theme** code goes in `io.pulse.ui.theme`
- **Utilities** go in `io.pulse.util`
- **Platform abstractions** go in `io.pulse.internal`

### Pull Request Checklist

- [ ] Code compiles on all targets (Android, Desktop, iOS)
- [ ] New `expect` declarations have `actual` implementations for all platforms
- [ ] UI components use `PulseColors` theme tokens (no hardcoded colors)
- [ ] New features include appropriate documentation
- [ ] No new dependencies added to `commonMain` without discussion

---

## License

```
Copyright 2024 Pulse Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
