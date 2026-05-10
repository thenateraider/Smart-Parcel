# Smart Parcel App

Smart Parcel App is an Android application for an ESP32-based smart parcel box. The app pairs with the parcel box through NFC, communicates with the ESP32 through Bluetooth Low Energy (BLE), starts parcel delivery tracking, displays live delivery status, confirms parcel pickup with a 6-digit unlock code, and reads delivery history stored on the ESP32 SD card.

## Project Overview

Smart Parcel is a smart parcel box prototype for tracking parcel handling conditions during delivery. The hardware side uses an ESP32 connected to sensors and control modules such as a load cell, temperature sensor, IMU for fall/flip detection, tamper detection switch, buzzer, solenoid lock, display, and SD card storage.

The Android app follows the BLE protocol implemented in the Arduino firmware `LVGLSMParcel.ino`. A single BLE service/characteristic is used for command exchange, while NFC is used to read the parcel box BLE MAC address for quick pairing.

## Key Features

- Pair with the smart parcel box by reading an NFC tag payload in this format: `SMPC://AA:BB:CC:DD:EE:FF`
- Connect to the ESP32 BLE device named `SMARTPARCEL_1`
- Start a new delivery record with sender name, recipient name, unlock code, and sent timestamp
- Display active parcel status including Track ID, sender/recipient information, flip count, impact count, tamper count, weight, and temperature
- Confirm parcel pickup with a 6-digit unlock code
- Notify the ESP32 when the unlock code is entered incorrectly 3 times so the buzzer can trigger an alert
- Read the latest 10 delivery history records from the ESP32
- Reset the ESP32 storage state through a BLE command

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android BLE GATT API
- Android NFC Reader Mode
- Gradle Kotlin DSL
- Minimum SDK 33
- Target SDK 35

## Important Project Structure

```text
app/src/main/java/com/nateapps/smartparcelapp/
├── MainActivity.kt          # App entry point, navigation, main BLE message listener
├── BleHelper.kt             # BLE connection, service discovery, notify, and write helper
├── HomeScreen.kt            # Main screen: NFC pairing, start delivery, status, pickup confirmation
├── HomeViewModel.kt         # App state and status parser
├── HistoryScreen.kt         # Delivery history screen
├── MoreScreen.kt            # More/options screen
└── ui/theme/                # Theme, colors, and typography
```

## ESP32 Integration

The ESP32 firmware in `LVGLSMParcel.ino` advertises the BLE device as `SMARTPARCEL_1`.

| Item | Value |
| --- | --- |
| BLE Service UUID | `0000abcd-0000-1000-8000-00805f9b34fb` |
| BLE Characteristic UUID | `0000dcba-0000-1000-8000-00805f9b34fb` |
| Characteristic properties | Read, Write, Notify |
| NFC payload | `SMPC://<BLE_MAC_ADDRESS>` |

Main communication flow:

1. The user taps the phone on the NFC tag attached to the parcel box.
2. The app reads the BLE MAC address from `SMPC://...`.
3. The app connects to the ESP32 through BLE.
4. After service/characteristic discovery, the app sends `smpc_hi`.
5. The ESP32 replies with `smpc_welcome`.
6. The app sends `smpc_status` to request the current `/sdcard/status.txt`.
7. The ESP32 sends status lines through BLE notify between `smpc_status_start` and `smpc_status_end`.

## BLE Commands

| App command | Description |
| --- | --- |
| `smpc_hi` | Handshake after BLE connection is ready |
| `smpc_status` | Request current parcel status from `status.txt` |
| `smpc_starttrack,<sender>,<recipient>,<code>,<timestamp>;` | Start a new delivery tracking session |
| `smpc_code_true,<received_timestamp>;` | Confirm that the recipient entered the correct code and picked up the parcel |
| `smpc_code_false3time` | Notify ESP32 that the code was entered incorrectly 3 times |
| `smpc_list_info` | Request the latest delivery history list |
| `smpc_<TrackID>` | Request history details for a Track ID, for example `smpc_T0001` |
| `smpc_resetfactory` | Reset ESP32 SD card data |

| ESP32 message | Description |
| --- | --- |
| `smpc_welcome` | ESP32 is ready to communicate |
| `smpc_status_start` / `smpc_status_end` | Start/end markers for status data |
| `smpc_okstart` | Delivery tracking started successfully |
| `smpc_done_thank` | Parcel pickup confirmed successfully |
| `smpc_nowtemp_<value>` | Current temperature |
| `smpc_nowkg_<value>` | Current weight |
| `smpc_list_info_end` | End of history list |
| `smpc_resetok` | Factory reset completed |

## ESP32 SD Card Data Format

When a delivery is active, `/sdcard/status.txt` uses this format:

```text
process
<sender>
<recipient>
<unlock_code>
<track_id>
<sent_timestamp>
<flip_count>
<impact_count>
<tamper_count>
<initial_weight>
<initial_temperature>
```

After parcel pickup is confirmed, the ESP32 changes the status to `done` and creates a backup history file such as `/sdcard/T0001_info.txt`.

## How To Use

1. Enable Bluetooth and NFC on the Android phone.
2. Open the Smart Parcel app.
3. Tap the phone on the NFC tag attached to the parcel box.
4. After BLE connection is established, start a new delivery.
5. Enter sender name, recipient name, and a 6-digit unlock code.
6. The app sends the delivery data to the ESP32 and tracking begins.
7. During delivery, the app can show flip count, impact count, tamper count, weight, and temperature.
8. When the recipient receives the parcel, confirm pickup and enter the 6-digit unlock code.
9. If the code is correct, the ESP32 saves the history record and ends the delivery session.

## ESP32/NFC Preparation

- Upload the ESP32 firmware from the Arduino project that contains the `LVGLSMParcel.ino` logic.
- Confirm that the ESP32 advertises BLE as `SMARTPARCEL_1`.
- Prepare an NFC NDEF text payload as `SMPC://<BLE_MAC_ADDRESS>`, for example `SMPC://A1:B2:C3:D4:E5:F6`.
- Make sure the SD card has the initial files:

```text
/sdcard/status.txt   -> unregistered
/sdcard/c_tid.txt    -> 0
```

## Build

Open the project with Android Studio or run:

```bash
./gradlew assembleDebug
```

Run unit tests:

```bash
./gradlew test
```

Note: Real device testing requires an Android phone that supports NFC and Bluetooth LE.

## Android Permissions

- `NFC` for reading the parcel box tag
- `BLUETOOTH_SCAN` for Bluetooth-related behavior on Android 12+
- `BLUETOOTH_CONNECT` for BLE connection and communication with the ESP32

## Real-World Usage Notes

This project is suitable for a smart parcel box prototype for homes, dormitories, offices, or small parcel pickup/drop-off points. It can be extended with cloud sync, internet notifications, multi-user accounts, recipient QR codes, or an admin dashboard.

Before production use, additional security should be added, such as BLE encryption, app-to-device authentication, safer unlock-code handling, and protection against NFC tag cloning.

---

# Smart Parcel App ภาษาไทย

แอป Android สำหรับใช้งานร่วมกับกล่องพัสดุอัจฉริยะ Smart Parcel ที่ใช้ ESP32 เป็นตัวควบคุมหลัก แอปนี้ทำหน้าที่จับคู่กับกล่องผ่าน NFC, สื่อสารกับ ESP32 ผ่าน BLE, เริ่มรายการฝากส่งพัสดุ, แสดงสถานะระหว่างขนส่ง, ยืนยันการรับพัสดุด้วยรหัส 6 หลัก และเรียกดูประวัติการส่งจากข้อมูลที่บันทึกไว้ใน SD card ของอุปกรณ์

## ภาพรวมโปรเจกต์

Smart Parcel เป็นระบบกล่องพัสดุอัจฉริยะสำหรับติดตามสถานะของพัสดุระหว่างการนำส่ง โดยฝั่งฮาร์ดแวร์ใช้ ESP32 เชื่อมต่อกับอุปกรณ์ตรวจวัดและอุปกรณ์ควบคุม เช่น โหลดเซลล์, เซนเซอร์อุณหภูมิ, IMU สำหรับตรวจจับการตก/พลิกคว่ำ, สวิตช์ตรวจจับการงัดแงะ, buzzer, solenoid lock, จอแสดงผล และ SD card สำหรับเก็บสถานะ/ประวัติ

ฝั่ง Android App สื่อสารกับ ESP32 ตามโปรโตคอลในไฟล์ Arduino `LVGLSMParcel.ino` โดยใช้ BLE service/characteristic เดียวสำหรับรับส่งคำสั่ง และใช้ NFC เป็นวิธีอ่าน MAC Address ของกล่องเพื่อเชื่อมต่อได้ง่าย

## ความสามารถหลัก

- จับคู่กล่องพัสดุด้วย NFC tag ที่บันทึกค่าในรูปแบบ `SMPC://AA:BB:CC:DD:EE:FF`
- เชื่อมต่อ BLE กับ ESP32 ชื่ออุปกรณ์ `SMARTPARCEL_1`
- เริ่มรายการส่งพัสดุใหม่ พร้อมบันทึกชื่อผู้ส่ง, ชื่อผู้รับ, รหัสปลดล็อก และเวลาส่ง
- แสดงสถานะพัสดุระหว่างนำส่ง เช่น Track ID, ข้อมูลผู้ส่ง/ผู้รับ, จำนวนการพลิกคว่ำ, จำนวนการตกกระแทก, จำนวนการงัดแงะ, น้ำหนัก และอุณหภูมิ
- ยืนยันรับพัสดุด้วยรหัสปลดล็อก 6 หลัก
- แจ้ง ESP32 เมื่อกรอกรหัสผิดครบ 3 ครั้ง เพื่อให้ buzzer ส่งเสียงเตือน
- อ่านประวัติการส่งพัสดุ 10 รายการล่าสุดจาก ESP32
- รีเซตข้อมูลอุปกรณ์กลับค่าเริ่มต้นผ่านคำสั่ง BLE

## เทคโนโลยีที่ใช้

- Kotlin
- Jetpack Compose
- Material 3
- Android BLE GATT API
- Android NFC Reader Mode
- Gradle Kotlin DSL
- Minimum SDK 33
- Target SDK 35

## โครงสร้างสำคัญ

```text
app/src/main/java/com/nateapps/smartparcelapp/
├── MainActivity.kt          # จุดเริ่มต้นแอป, navigation, BLE message listener หลัก
├── BleHelper.kt             # จัดการ BLE connection, service discovery, notify, write
├── HomeScreen.kt            # หน้าหลัก: จับคู่ NFC, เริ่มส่ง, ดูสถานะ, ยืนยันรับ
├── HomeViewModel.kt         # state หลักของแอปและ parser ข้อมูล status
├── HistoryScreen.kt         # หน้าประวัติการส่งพัสดุ
├── MoreScreen.kt            # เมนูเพิ่มเติม
└── ui/theme/                # theme, color, typography
```

## การทำงานร่วมกับ ESP32

ESP32 ในไฟล์ `LVGLSMParcel.ino` เปิด BLE advertising ด้วยชื่อ `SMARTPARCEL_1`

| รายการ | ค่า |
| --- | --- |
| BLE Service UUID | `0000abcd-0000-1000-8000-00805f9b34fb` |
| BLE Characteristic UUID | `0000dcba-0000-1000-8000-00805f9b34fb` |
| Characteristic properties | Read, Write, Notify |
| NFC payload | `SMPC://<BLE_MAC_ADDRESS>` |

ลำดับการสื่อสารหลัก:

1. ผู้ใช้แตะ NFC tag ที่กล่องพัสดุ
2. แอปอ่าน MAC Address จาก payload `SMPC://...`
3. แอปเชื่อมต่อ BLE ไปยัง ESP32
4. เมื่อพบ service/characteristic แล้ว แอปส่ง `smpc_hi`
5. ESP32 ตอบ `smpc_welcome`
6. แอปส่ง `smpc_status` เพื่ออ่านสถานะจาก `/sdcard/status.txt`
7. ESP32 ส่งข้อมูลสถานะเป็นหลาย notify คั่นด้วย `smpc_status_start` และ `smpc_status_end`

## BLE Commands

| คำสั่งจากแอป | ความหมาย |
| --- | --- |
| `smpc_hi` | ทักทาย ESP32 หลังเชื่อมต่อ BLE สำเร็จ |
| `smpc_status` | ขอข้อมูลสถานะปัจจุบันจาก `status.txt` |
| `smpc_starttrack,<sender>,<recipient>,<code>,<timestamp>;` | เริ่มรายการส่งพัสดุใหม่ |
| `smpc_code_true,<received_timestamp>;` | ยืนยันว่าผู้รับกรอกรหัสถูกต้องและรับพัสดุแล้ว |
| `smpc_code_false3time` | แจ้งว่ากรอกรหัสผิดครบ 3 ครั้ง |
| `smpc_list_info` | ขอรายชื่อประวัติการส่งล่าสุด |
| `smpc_<TrackID>` | ขอรายละเอียดประวัติของ Track ID นั้น เช่น `smpc_T0001` |
| `smpc_resetfactory` | รีเซตข้อมูลใน SD card ของกล่อง |

| ข้อความจาก ESP32 | ความหมาย |
| --- | --- |
| `smpc_welcome` | ESP32 พร้อมสื่อสาร |
| `smpc_status_start` / `smpc_status_end` | จุดเริ่ม/จบข้อมูลสถานะ |
| `smpc_okstart` | เริ่มรายการส่งสำเร็จ |
| `smpc_done_thank` | ยืนยันรับพัสดุสำเร็จ |
| `smpc_nowtemp_<value>` | อุณหภูมิปัจจุบัน |
| `smpc_nowkg_<value>` | น้ำหนักปัจจุบัน |
| `smpc_list_info_end` | ส่งรายการประวัติครบแล้ว |
| `smpc_resetok` | รีเซตอุปกรณ์สำเร็จ |

## รูปแบบข้อมูลบน SD Card ฝั่ง ESP32

ขณะมีรายการส่ง สถานะใน `/sdcard/status.txt` มีรูปแบบ:

```text
process
<sender>
<recipient>
<unlock_code>
<track_id>
<sent_timestamp>
<flip_count>
<impact_count>
<tamper_count>
<initial_weight>
<initial_temperature>
```

เมื่อรับพัสดุสำเร็จ ESP32 จะเปลี่ยนสถานะเป็น `done` และสร้างไฟล์สำรองตาม Track ID เช่น `/sdcard/T0001_info.txt` เพื่อใช้เป็นประวัติย้อนหลัง

## วิธีใช้งานแอป

1. เปิด Bluetooth และ NFC บนโทรศัพท์ Android
2. เปิดแอป Smart Parcel
3. แตะโทรศัพท์กับ NFC tag บนกล่องพัสดุเพื่อจับคู่
4. เมื่อเชื่อมต่อแล้ว กดเริ่มรายการส่งใหม่
5. กรอกชื่อผู้ส่ง, ชื่อผู้รับ และรหัสปลดล็อก 6 หลัก
6. แอปส่งข้อมูลไปยัง ESP32 เพื่อเริ่มติดตามพัสดุ
7. ระหว่างขนส่ง แอปสามารถแสดงจำนวนการพลิกคว่ำ, การตกกระแทก, การงัดแงะ, น้ำหนัก และอุณหภูมิ
8. เมื่อผู้รับได้รับพัสดุ ให้กดยืนยันรับและกรอกรหัส 6 หลัก
9. หากรหัสถูกต้อง ESP32 จะบันทึกประวัติและจบรายการส่ง

## การเตรียม ESP32/NFC

- อัปโหลด firmware ฝั่ง ESP32 จาก Arduino project ที่มี logic ใน `LVGLSMParcel.ino`
- ตรวจสอบว่า ESP32 advertising BLE ด้วยชื่อ `SMARTPARCEL_1`
- เตรียม NFC tag แบบ NDEF text payload เป็น `SMPC://<BLE_MAC_ADDRESS>` เช่น `SMPC://A1:B2:C3:D4:E5:F6`
- ตรวจสอบ SD card ให้มีไฟล์เริ่มต้น:

```text
/sdcard/status.txt   -> unregistered
/sdcard/c_tid.txt    -> 0
```

## วิธี Build โปรเจกต์ Android

เปิดด้วย Android Studio หรือใช้คำสั่ง:

```bash
./gradlew assembleDebug
```

สำหรับรัน unit test:

```bash
./gradlew test
```

หมายเหตุ: โปรเจกต์นี้ต้องใช้เครื่อง Android ที่รองรับ NFC และ Bluetooth LE เพื่อทดสอบการทำงานจริงกับกล่องพัสดุ

## สิทธิ์ที่แอปใช้งาน

- `NFC` สำหรับอ่าน tag ที่เก็บ MAC Address ของกล่อง
- `BLUETOOTH_SCAN` สำหรับการทำงานที่เกี่ยวข้องกับ Bluetooth บน Android 12+
- `BLUETOOTH_CONNECT` สำหรับเชื่อมต่อและสื่อสารกับ ESP32 ผ่าน BLE

## แนวทางนำไปใช้งานจริง

ระบบนี้เหมาะกับการทดลองหรือพัฒนา prototype กล่องพัสดุอัจฉริยะ เช่น กล่องรับฝากพัสดุในบ้าน หอพัก สำนักงาน หรือจุดรับส่งพัสดุขนาดเล็ก โดยสามารถต่อยอดเพิ่ม cloud sync, แจ้งเตือนผ่าน internet, ระบบผู้ใช้หลายคน, QR code สำหรับผู้รับ, หรือ dashboard สำหรับผู้ดูแลได้

ก่อนใช้งานจริงควรเพิ่มการป้องกันด้านความปลอดภัย เช่น การเข้ารหัสข้อมูล BLE, authentication ระหว่างแอปกับอุปกรณ์, การจัดการรหัสผ่านอย่างปลอดภัย และกลไกป้องกันการปลอมแปลง NFC tag
