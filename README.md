# Smart Parcel App

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
