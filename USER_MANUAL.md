# Wake on LAN User Manual

## Table of Contents
1. [Introduction](#introduction)
2. [Installation](#installation)
3. [Quick Start](#quick-start)
4. [Features](#features)
5. [FAQ](#faq)
6. [Troubleshooting](#troubleshooting)

---

## Introduction

Wake on LAN is a lightweight Android application for remotely waking up devices that support Wake-on-LAN (WoL) functionality over the network. The app supports two wake-up modes:
- **Direct Mode**: Send WoL magic packets directly within the same local network
- **Proxy Mode**: Send WoL magic packets through an SSH proxy server (supports OpenWrt and Ubuntu)

### System Requirements
- Android device
- Target device must support and have Wake-on-LAN enabled
- For Proxy Mode: A proxy server running OpenWrt or Ubuntu

---

## Installation

### Method 1: Download APK
1. Visit the [Releases page](https://github.com/fengluo2/Wake-on-LAN/releases)
2. Download the latest APK file
3. Install the APK on your Android device
   - Note: First-time installation may require enabling "Unknown sources" in settings

### Method 2: Build from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/fengluo2/Wake-on-LAN.git
   ```
2. Open the project in Android Studio
3. Build and install to your device

---

## Quick Start

### Step 1: Launch the App
Open the Wake on LAN app. You will see the device list page (empty on first use).

### Step 2: Add a Device

#### Add a Direct Connection Device (Recommended for LAN devices)
1. Tap the **"Add Link"** button in the bottom right corner (+ icon)
2. Fill in the device information:
   - **Name**: Give the device a recognizable name (e.g., My PC, Living Room TV)
   - **Link Type**: Select "Direct"
   - **Host IP**: IP address of the target device (e.g., 192.168.1.100)
   - **Host MAC**: MAC address of the target device (format: 00:11:22:33:44:55)
   - **Remark** (optional): Add notes
3. Tap **"Save Link"**

#### Add a Proxy Device (For remote wake-up)
1. Tap the **"Add Link"** button in the bottom right corner (+ icon)
2. Fill in the device information:
   - **Name**: Give the device a recognizable name
   - **Link Type**: Select "Proxy"
   - **Proxy Address**: IP address or domain of the proxy server
   - **Proxy Port**: SSH port (default: 22)
   - **Proxy Login Type**: Select "Password" or "Private key"
   - **Proxy Login User**: SSH username (usually root)
   - **Proxy Login Passwd/Private Key**: Enter password or private key based on login type
   - **Host IP**: IP address of the target device
   - **Host MAC**: MAC address of the target device
   - **Remark** (optional): Add notes
3. Tap **"Save Link"**

### Step 3: Wake Up a Device
1. Find the device you want to wake up in the device list
2. Tap the **"Send WoL"** button on the right side of the device entry
3. The app will send the WoL magic packet and display the result

---

## Features

### 1. Device Management

#### View Device List
- The main screen displays all added devices
- Each device shows name, type (Direct/Proxy), and notes

#### Edit Device
1. In the device list, tap the **"Edit"** button (pencil icon) for the device
2. Modify the device information
3. Tap **"Save Link"** to save changes

#### Delete Device
- **Delete single device**: Tap the **"Remove"** button (trash icon) for the device
- **Delete all devices**: Tap the menu in the top right corner → Select **"Remove All"**

### 2. Sending WoL Packets

#### Direct Mode Sending
- The app sends UDP magic packets directly to the target device via Wi-Fi or cellular network
- Sending port: Usually UDP port 9
- Use case: Device and phone are on the same local network

#### Proxy Mode Sending
- The app connects to the proxy server via SSH
- Executes the `wakeonlan` command on the proxy server to send magic packets
- Use cases:
  - Remote wake-up of devices not on the same network
  - Target network has firewall restrictions
  - Need to send WoL packets through a specific gateway

### 3. Import and Export

#### Export Device List
1. Tap the menu in the top right corner (three dots)
2. Select **"Export"**
3. The app will export the device list as a JSON file
4. The file location will be displayed after successful export

#### Import Device List
1. Tap the menu in the top right corner (three dots)
2. Select **"Import"**
3. Select a previously exported JSON file
4. The device list will be imported into the app

### 4. Other Features

#### View Open Source Licenses
- Tap the menu in the top right corner → Select **"License"**
- View license information for open source components used by the app

#### About
- Tap the menu in the top right corner → Select **"About"**
- View app version and related information

---

## FAQ

### Q1: How do I get a device's MAC address?

**Windows:**
1. Open Command Prompt (CMD)
2. Type `ipconfig /all`
3. Find the network adapter's "Physical Address" - this is the MAC address

**Linux/macOS:**
1. Open Terminal
2. Type `ifconfig` or `ip link show`
3. Find the network adapter's MAC address (ether or HWaddr)

**Router Admin Interface:**
- Log into your router's admin interface
- View the list of connected devices to see all device MAC addresses

### Q2: What settings are needed for a device to be wakeable?

1. **BIOS/UEFI Settings:**
   - Enter BIOS/UEFI settings
   - Enable "Wake on LAN", "PME Event Wake Up", or similar options
   - Enable "Power On by PCI-E/PCI" option

2. **Operating System Settings (Windows):**
   - Open "Device Manager"
   - Find network adapter, right-click and select "Properties"
   - In the "Power Management" tab, check "Allow this device to wake the computer"
   - In the "Advanced" tab, enable Wake on LAN related options

3. **Operating System Settings (Linux):**
   ```bash
   # Check WoL status
   sudo ethtool eth0 | grep Wake-on
   
   # Enable WoL
   sudo ethtool -s eth0 wol g
   ```

4. **Power Settings:**
   - Ensure device is in shutdown or sleep state (not completely powered off)
   - Ethernet cable must remain connected
   - Power supply must remain connected

### Q3: How do I configure the proxy server for Proxy Mode?

#### OpenWrt System:
1. SSH connect to OpenWrt router
2. Install wakeonlan tool:
   ```bash
   opkg update
   opkg install wakeonlan
   ```
3. Verify installation:
   ```bash
   opkg list-installed | grep wakeonlan
   ```

#### Ubuntu/Debian System:
1. SSH connect to server
2. Install wakeonlan tool:
   ```bash
   sudo apt update
   sudo apt install wakeonlan
   ```
3. Verify installation:
   ```bash
   dpkg -l | grep wakeonlan
   ```

#### SSH Authentication Configuration:
- **Password Authentication**: Use SSH user password directly
- **Key Authentication**: Use SSH private key (recommended, more secure)
  - Paste private key content into "Proxy Login private Key" field
  - Private key format is usually PEM format

### Q4: Why can't I wake up the device?

Please check the following items in order:

1. **Does the device support WoL:**
   - Not all devices support Wake-on-LAN
   - Some laptops don't support WoL when running on battery power

2. **Network Connection:**
   - Target device's ethernet cable must be connected
   - Router or switch must be powered on
   - For Direct Mode, ensure phone and target device are on the same network

3. **MAC Address and IP Address:**
   - Confirm MAC address format is correct (use colons as separators, e.g., 00:11:22:33:44:55)
   - Confirm IP address is correct

4. **Firewall:**
   - Some routers or firewalls may block WoL packets
   - Try allowing UDP port 9 in the router

5. **Proxy Mode Specific Issues:**
   - Confirm wakeonlan tool is installed on proxy server
   - Confirm SSH connection information (address, port, username, password/key) is correct
   - Confirm proxy server and target device are on the same network

---

## Troubleshooting

### Issue: Failed to Send WoL Packet

**Possible Causes and Solutions:**

1. **Insufficient Network Permissions:**
   - Check if the app has been granted network permissions
   - In Android settings: Apps → Wake on LAN → Permissions → Ensure necessary permissions are granted

2. **Wi-Fi Not Connected (Direct Mode):**
   - Ensure device is connected to Wi-Fi network
   - Cellular data network may not be able to send WoL packets to LAN devices

3. **Proxy Server Connection Failed (Proxy Mode):**
   - Check proxy server address and port
   - Check SSH username and password/key
   - Confirm proxy server is accessible from the internet (if using remotely)
   - Check if firewall is blocking SSH connections

### Issue: Import Failed

**Possible Causes and Solutions:**

1. **File Format Error:**
   - Ensure you're importing a JSON file exported by this app
   - Don't manually modify the exported JSON file

2. **File Read Permissions:**
   - Check if the app has been granted storage permissions
   - Grant necessary file access permissions in Android settings

### Issue: Unsupported System Type

**Explanation:**
- Proxy mode currently only supports OpenWrt and Ubuntu systems
- If you need support for other systems, please submit an Issue on GitHub

**Possible Solutions:**
1. Manually install wakeonlan tool on other Linux distributions
2. Modify the system to be compatible with apt or opkg package managers
3. Submit a feature request to the project to support more systems

### Issue: App Crashes or Behaves Abnormally

**Recommended Actions:**
1. Ensure you're using the latest version of the app
2. Clear app cache and data (Note: This will clear all saved devices, please export first)
3. Reinstall the app
4. Submit a bug report on GitHub, including:
   - Android version
   - Device model
   - Detailed steps to reproduce
   - Error messages (if any)

---

## Technical Details

### WoL Magic Packet Format
A Wake-on-LAN magic packet is a UDP packet containing:
- 6 bytes of 0xFF
- Target device's MAC address repeated 16 times

### Direct Mode Operation
1. App constructs WoL magic packet
2. Sends via UDP broadcast to network (default port 9 or 7)
3. Target device's network card receives magic packet and wakes up the system

### Proxy Mode Operation
1. App connects to proxy server via SSH
2. Executes wakeonlan command on proxy server
3. Proxy server sends WoL magic packet to target device
4. Target device wakes up

---

## Support and Feedback

If you encounter issues or have suggestions for improvement:
- Submit an [Issue](https://github.com/fengluo2/Wake-on-LAN/issues) on GitHub
- Contribute code: Submit a [Pull Request](https://github.com/fengluo2/Wake-on-LAN/pulls)

---

## License

This project is licensed under an open source license. See the [LICENSE.md](LICENSE.md) file for details.

---

**Last Updated: November 2025**
