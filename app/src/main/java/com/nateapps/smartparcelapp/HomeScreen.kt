package com.nateapps.smartparcelapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Landslide
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ScheduleSend
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.twotone.Archive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@Composable

fun HomeScreen(
    onNavigateToHistory: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val isExpanded = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as Activity
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val coroutineScope = rememberCoroutineScope()

    val showDialog = remember { mutableStateOf(false) }
    val welcomeReceived = remember { mutableStateOf(false) }
    val hasAttemptedPairing = remember { mutableStateOf(false) }
    val showSendDialog = remember { mutableStateOf(false) }
    // Recipient dialog states
    val showRecipientDialog = remember { mutableStateOf(false) }
    val showConfirmReceiveDialog = remember { mutableStateOf(false) }
    val inputUnlockCode = remember { mutableStateOf("") }
    val recipientName = remember { mutableStateOf("") }
    val senderName = remember { mutableStateOf("") }
    val senderUnlockCode = remember { mutableStateOf("") }

    var showSuccessPopup by remember { mutableStateOf(false) }
    // --- Success & waiting state ---
    val showSuccessState by viewModel.showSuccessState.collectAsState()
    val waitingForThank by viewModel.waitingForThank.collectAsState()
    val waitingForStart by viewModel.waitingForStart.collectAsState()
    //----- BLE States ----
    val connectedDeviceName by viewModel.connectedDeviceName.collectAsState()
    val macAddress by viewModel.macAddress.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val currentConnectedMac by viewModel.currentConnectedMac.collectAsState()
    val connectedMac by viewModel.connectedMac.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val parsedStatus by viewModel.parsedStatus.collectAsState()
    val statusReady by viewModel.statusReady.collectAsState()


    // Status buffer to accumulate lines from ESP32
    val statusBuffer = remember { mutableStateListOf<String>() }

    val processStatus by viewModel.processStatus.collectAsState()
    val liveStatus by viewModel.liveStatus.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "จำเป็นต้องอนุญาต BLUETOOTH_CONNECT", Toast.LENGTH_LONG).show()
        }
    }
    val showResetDialog = remember { mutableStateOf(false) }
    val isResetting = remember { mutableStateOf(false) }
    val resetPressStartTime = remember { mutableStateOf(0L) }



    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }


    if (showDialog.value) {
        val callback = remember {
            NfcAdapter.ReaderCallback { tag ->
                val ndef = Ndef.get(tag)
                ndef?.connect()
                val message = ndef?.ndefMessage
                val payload = message?.records?.firstOrNull()?.payload
                val rawText = payload?.let {
                    String(it.copyOfRange(3, it.size), Charsets.UTF_8).trim()
                }
                ndef?.close()

                Log.d("NFC", "✅ Read raw: $rawText")

                val prefix = "SMPC://"
                if (rawText != null && rawText.startsWith(prefix)) {
                    val mac = rawText.removePrefix(prefix).uppercase()
                    val macRegex = Regex("([A-F0-9]{2}:){5}[A-F0-9]{2}")
                    if (macRegex.matches(mac) && mac != currentConnectedMac) {
                        activity.runOnUiThread {
                            coroutineScope.launch {
                                viewModel.updateMac(mac)
                            }
                            hasAttemptedPairing.value = true
                            showDialog.value = false
                        }
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            nfcAdapter?.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A,
                Bundle().apply {
                    putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
                }
            )
            onDispose {
                nfcAdapter?.disableReaderMode(activity)
            }
        }

        AlertDialog(

            onDismissRequest = { showDialog.value = false },
            confirmButton = {},
            title = { Text("กำลังรอ NFC",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center)
                        },
            text = { Text("กรุณาแตะโทรศัพท์กับกล่องพัสดุ",
                    color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge

            ) },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showSuccessPopup) {
        AlertDialog(

            onDismissRequest = { showSuccessPopup = false },
            confirmButton = {},
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "สำเร็จ",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                }
            },
            text = {
                Text(
                    "สร้างรายการส่งพัสดุเสร็จสมบูรณ์",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
        LaunchedEffect(Unit) {
            delay(5000)
            showSuccessPopup = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        // Overlay: Loading while waiting for smpc_done_thank
        // ✅ Loading overlay — ให้มาไว้ตรงนี้เลย
        if (waitingForThank) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    .clickable(enabled = false, onClick = {}) // บล็อกการกด
                    .zIndex(1f), // 🧷 ทับทุกสิ่ง
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("กำลังทำรายการยืนยันตัวตน", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        if (waitingForStart) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = false, onClick = {}) // บล็อกการกด
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "กำลังดำเนินการ...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (isConnected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📦 เชื่อมต่อกับ :\n ${connectedDeviceName ?: connectedMac}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }


        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top 30% section (message)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .weight(0.3f),
                contentAlignment = Alignment.BottomStart
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (!isConnected) {

                            Text(
                                text = "ยินดีต้อนรับ\n\nSmart Parcel",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Left,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = if (showSuccessState) "เสร็จสมบูรณ์" else "สถานะปัจจุบัน ",
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when {
                                    showSuccessState -> "พัสดุถูกจัดส่งเรียบร้อยแล้ว"
                                    parsedStatus?.lowercase() == "unregistered" -> "ยังไม่ได้ลงทะเบียนพัสดุ"
                                    parsedStatus?.lowercase() == "done" -> "พัสดุก่อนหน้าจัดส่งสมบูรณ์แล้ว สามารถเริ่มรายการส่งใหม่ได้ทันที"
                                    parsedStatus?.lowercase() == "process" -> "พัสดุอยู่ระหว่างการนำส่ง \nหากคุณเป็นผู้รับ โปรดปลดล็อกด้วยรหัสผ่าน"
                                    else -> "--------"
                                },
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    val statusIcon = when {
                        showSuccessState -> Icons.Outlined.SentimentVerySatisfied
                        parsedStatus?.lowercase() == "unregistered" -> Icons.Default.BookmarkAdd
                        parsedStatus?.lowercase() == "done" -> Icons.TwoTone.Archive
                        parsedStatus?.lowercase() == "process" -> Icons.Default.ScheduleSend
                        else -> Icons.Default.AllInbox
                    }
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "สถานะไอคอน",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(0.4f)
                            .align(Alignment.Bottom)
                            .sizeIn(minWidth = 72.dp, minHeight = 72.dp)
                    )
                }
            }

            // Bottom 60% section (buttons and status)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    tonalElevation = 24.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top, // Added to center content vertically
                        modifier = Modifier
                            .fillMaxSize() // Ensure Column fills the Box
                            .padding(16.dp)
                    ) {
                        if (!isConnected) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.height(72.dp), // เพิ่มความสูงของปุ่ม
                                        onClick = {
                                        hasAttemptedPairing.value = false
                                        showDialog.value = true
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Nfc,
                                            contentDescription = "จับคู่ NFC",
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(36.dp) // Bigger icon
                                        )
                                        Spacer(modifier = Modifier.width(8.dp)) // Spacer between icon and text
                                        Text(
                                            " เริ่มจับคู่อุปกรณ์",
                                            style = MaterialTheme.typography.titleLarge, // Bigger text
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))


                            if (parsedStatus?.lowercase() == "unregistered" || parsedStatus?.lowercase() == "done") {
                                Log.d("BLE", "เข้าUI หน้า พร้อมรอสร้างรายการส่ง Status: $parsedStatus")
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            ),
                                            onClick = {
                                                onNavigateToHistory()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .sizeIn(minHeight = 64.dp),
                                            shape = RoundedCornerShape(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.History,
                                                contentDescription = "ประวัติ",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.sizeIn(minWidth = 24.dp, minHeight = 24.dp)
                                            )
                                            Text(
                                                " ประวัติ",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        Button(
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            ),
                                            onClick = {
                                                showSendDialog.value = true
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .sizeIn(minHeight = 64.dp),
                                            shape = RoundedCornerShape(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Send,
                                                contentDescription = "เริ่มส่ง",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.sizeIn(minWidth = 24.dp, minHeight = 24.dp)
                                            )
                                            Text(
                                                "สร้างรายการส่ง", style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            } else if (parsedStatus?.lowercase() == "process") {
                                // ตัวแปรจำลอง รอรับค่าจาก ESP32 ผ่าน BLE (TODO)
                                val flipCount = processStatus.flipCount
                                val impactCount = processStatus.impactCount
                                val tamperCount = processStatus.tamperCount
                                val initialWeight = processStatus.initialWeight
                                val currentWeight = liveStatus.liveWeight
                                val initialTemp = processStatus.initialTemp
                                val currentTemp = liveStatus.liveTemp
                                // แทนค่าจำลองใน UI ส่วน process ด้วยตัวแปรจริงจาก status.txt
                                val sentTime = processStatus.sentTime
                                val sender = processStatus.sender
                                val recipient = processStatus.recipient
                                val trackid = processStatus.trackId

                                Spacer(modifier = Modifier.height(4.dp))
                                // --- เงื่อนไขสำหรับ waitingForThank และ showSuccessState ---
                                if (showSuccessState) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(96.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "พัสดุถูกจัดส่งสมบูรณ์",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = {
                                                clearAllState(
                                                    context,
                                                    coroutineScope,
                                                    viewModel,
                                                    recipientName,
                                                    senderName,
                                                    senderUnlockCode,
                                                    processStatus
                                                )
                                                viewModel.setShowSuccessState(false)
                                                statusBuffer.clear()
                                                viewModel.clearStatusBuffer()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("เสร็จสิ้น",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text("ข้อมูลจ่าหน้าพัสดุ", style = MaterialTheme.typography.titleLarge)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Tag,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Track ID : $trackid", style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // เวลาที่ส่ง
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        {
                                            Icon(
                                                imageVector = Icons.Default.PunchClock,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("เวลาที่ส่ง : ${formatSentTime(sentTime)}", style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // ชื่อผู้ส่ง
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Person,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ชื่อผู้ส่ง : $sender", style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // ชื่อผู้รับ
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonPinCircle,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ชื่อผู้รับ : $recipient", style = MaterialTheme.typography.bodyLarge)
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text("บันทึกขณะการนำส่ง", style = MaterialTheme.typography.titleLarge)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // การพลิกคว่ำ
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ScreenRotation,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("การพลิกคว่ำ : $flipCount ครั้ง", style = MaterialTheme.typography.bodyLarge,
                                                color = if (flipCount.toIntOrNull() ?: 0 > 1) Color.Red else LocalTextStyle.current.color,
                                                fontWeight = if (flipCount.toIntOrNull() ?: 0 > 1) FontWeight.Bold else null)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // การตกกระแทก
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Landslide,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "การตกกระแทก : $impactCount ครั้ง",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = if (impactCount.toIntOrNull() ?: 0 > 1) Color.Red else LocalTextStyle.current.color,
                                                fontWeight = if (impactCount.toIntOrNull() ?: 0 > 1) FontWeight.Bold else null
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // การงัดแงะ
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LockOpen,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("การงัดแงะ : $tamperCount ครั้ง",style = MaterialTheme.typography.bodyLarge,
                                                color = if (tamperCount.toIntOrNull() ?: 0 > 1) Color.Red else LocalTextStyle.current.color,
                                                fontWeight = if (tamperCount.toIntOrNull() ?: 0 > 1) FontWeight.Bold else null)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // น้ำหนัก
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Scale,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("น้ำหนัก : $initialWeight → $currentWeight kg", style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // อุณหภูมิ
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Thermostat,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("อุณหภูมิ : $initialTemp°C → $currentTemp°C", style = MaterialTheme.typography.bodyLarge)
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            onClick = {
                                                showConfirmReceiveDialog.value = true
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .sizeIn(minHeight = 64.dp),
                                            shape = RoundedCornerShape(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "ยืนยันรับพัสดุ",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.sizeIn(minWidth = 24.dp, minHeight = 24.dp)
                                            )
                                            Text(" ยืนยันรับพัสดุ",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground)
                                        }

                                        // เพิ่มปุ่ม "รับพัสดุ" ต่อจากปุ่ม “ยืนยันรับพัสดุ”
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                BleHelper.sendMessage(context, "smpc_disconnect")
                                                Toast.makeText(context, "❌ ตัดการเชื่อมต่อ", Toast.LENGTH_SHORT).show()
                                                clearAllState(
                                                    context,
                                                    coroutineScope,
                                                    viewModel,
                                                    recipientName,
                                                    senderName,
                                                    senderUnlockCode,
                                                    processStatus
                                                )
                                                statusBuffer.clear()
                                                viewModel.clearStatusBuffer()

                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .sizeIn(minHeight = 64.dp),
                                            shape = RoundedCornerShape(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "ไม่ใช่ผู้รับพัสดุ",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                " ไม่ใช่ผู้รับพัสดุ",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(120.dp))
                                    }
                                }
                            }else{ // ไม่มีอะไรเลย
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                            }
                        }
                    }
                }
            }
        }

        if (showSendDialog.value) {
            AlertDialog(
                onDismissRequest = { showSendDialog.value = false },
                confirmButton = {
                    Button(onClick = {
                        showSendDialog.value = false
                        showRecipientDialog.value = true
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("ยืนยัน",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                },
                dismissButton = {
                    Button(onClick = { showSendDialog.value = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text("ยกเลิก",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("เริ่มรายการส่งใหม่",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                ) },
                text = { Text("คุณต้องการเริ่มรายการส่งใหม่หรือไม่?",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                ) }
            )
        }

        if (showRecipientDialog.value) {
            AlertDialog(
                onDismissRequest = { showRecipientDialog.value = false },
                title = {
                    Text("กรอกข้อมูลการส่ง",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        OutlinedTextField(
                            value = senderName.value,
                            onValueChange = { senderName.value = it },
                            label = { Text("ชื่อผู้ส่ง", color = MaterialTheme.colorScheme.onBackground) }
                        )
                        OutlinedTextField(
                            value = recipientName.value,
                            onValueChange = { recipientName.value = it },
                            label = { Text("ชื่อผู้รับ", color = MaterialTheme.colorScheme.onBackground) }
                        )

                        OutlinedTextField(
                            value = senderUnlockCode.value,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) senderUnlockCode.value = it
                            },
                            label = { Text("รหัสปลดล็อก (6 หลัก)", color = MaterialTheme.colorScheme.onBackground) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (senderName.value.isNotBlank() && recipientName.value.isNotBlank() && senderUnlockCode.value.length == 6) {
                                showRecipientDialog.value = false
                                val timestamp = java.text.SimpleDateFormat("ddMMyyyy-HHmmss").format(java.util.Date())
                                val message = "smpc_starttrack,${senderName.value},${recipientName.value},${senderUnlockCode.value},$timestamp;"
                                BleHelper.sendMessage(context, message)

                                viewModel.setWaitingForStart(true)
                                Toast.makeText(context, "กำลังเริ่มการส่ง...", Toast.LENGTH_SHORT).show()

                                // รอดัก smpc_okstart เฉพาะในรอบนี้
                                BleHelper.setMessageListener { msg ->
                                    val decodedMsg = msg.trim()
                                    Log.d("BLE", "📩 [starttrack] Received: '$decodedMsg'")

                                    if (decodedMsg == "smpc_okstart") {
                                        coroutineScope.launch {
                                            showSuccessPopup = true
                                            delay(5000) // รอให้ Popup แสดงผล 5 วินาที
                                            showSuccessPopup = false
                                            clearAllState(
                                                context,
                                                coroutineScope,
                                                viewModel,
                                                recipientName,
                                                senderName,
                                                senderUnlockCode,
                                                processStatus
                                            )
                                            viewModel.setWaitingForStart(false)
                                            viewModel.clearStatusBuffer()
                                            //Toast.makeText(context, "📦 กลับสู่หน้าจับคู่", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "โปรดกรอกข้อมูลให้ครบถ้วนและถูกต้อง (รหัสปลดล็อกต้องมี 6 หลัก)", Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = senderName.value.isNotBlank() && recipientName.value.isNotBlank() && senderUnlockCode.value.length == 6
                    ) {
                        Text("เริ่มการส่ง"
                        ,style = MaterialTheme.typography.bodyMedium)
                    }
                },
                dismissButton = {
                    Button(onClick = { showRecipientDialog.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("ยกเลิก",color = Color.White,style = MaterialTheme.typography.bodyMedium)
                    }

                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // ยืนยันรับพัสดุ: Dialog ใส่รหัสผ่าน
        // --- เพิ่มตัวแปรสำหรับ logic block, error count, error message ---
        val unlockCodeErrorCount = remember { mutableStateOf(0) }
        val unlockCodeIncorrectMessage = remember { mutableStateOf("") }
        val showBlockDialog = remember { mutableStateOf(false) }

        // --- Dialog block เมื่อกรอกรหัสผิดเกิน 3 ครั้ง ---
        if (showBlockDialog.value) {
            AlertDialog(
                onDismissRequest = { showBlockDialog.value = false },
                confirmButton = {},
                title = {
                    Text(
                        "รหัสไม่ถูกต้อง !!!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        "คุณกรอกรหัสผิดเกิน 3 ครั้ง",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface
            )

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(5000)
                showBlockDialog.value = false
                unlockCodeErrorCount.value = 0
                unlockCodeIncorrectMessage.value = ""
            }

        }

        if (showConfirmReceiveDialog.value) {
            @OptIn(ExperimentalComposeUiApi::class)
            val codeDigits = remember { List(6) { mutableStateOf("") } }
            val focusRequesters = remember { List(6) { FocusRequester() } }
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            AlertDialog(
                onDismissRequest = {
                    showConfirmReceiveDialog.value = false
                    // reset codeDigits
                    codeDigits.forEach { it.value = "" }
                    unlockCodeIncorrectMessage.value = ""
                },
                title = {
                    Text(
                        "ยืนยันการรับพัสดุ",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "กรอกรหัสผ่าน 6 หลักเพื่อยืนยันว่าคุณเป็นผู้รับพัสดุ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                codeDigits.forEachIndexed { index, state ->
                                    OutlinedTextField(
                                        value = state.value,
                                        onValueChange = { input ->
                                            val isDeleting = state.value.isNotEmpty() && input.isEmpty()
                                            if (input.length <= 1 && input.all { c -> c.isDigit() } || isDeleting) {
                                                state.value = input
                                                if (input.isNotEmpty()) {
                                                    if (index < 5) {
                                                        focusRequesters[index + 1].requestFocus()
                                                    } else {
                                                        keyboardController?.hide()
                                                    }
                                                } else if (isDeleting) {
                                                    if (index > 0) {
                                                        focusRequesters[index - 1].requestFocus()
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .width(42.dp)
                                            .height(62.dp)
                                            .focusRequester(focusRequesters[index]),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            textAlign = TextAlign.Center,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        visualTransformation = PasswordVisualTransformation(),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // --- แสดงข้อความ error ถ้ามี ---
                        if (unlockCodeIncorrectMessage.value.isNotEmpty()) {
                            Text(
                                unlockCodeIncorrectMessage.value,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    val enteredCode = codeDigits.joinToString("") { it.value }
                    Button(
                        onClick = {
                            if (enteredCode == processStatus.unlockCode) {
                                val timestampget = java.text.SimpleDateFormat("ddMMyyyy-HHmmss").format(java.util.Date())
                                val messagetrue = "smpc_code_true,$timestampget;"
                                viewModel.setWaitingForThank(true)
                                BleHelper.sendMessage(context, messagetrue)
                                Toast.makeText(context, "✅ รหัสถูกต้อง กำลังยืนยัน...", Toast.LENGTH_SHORT).show()
                                showConfirmReceiveDialog.value = false
                                codeDigits.forEach { it.value = "" }
                                unlockCodeErrorCount.value = 0
                                unlockCodeIncorrectMessage.value = ""


                            } else {
                                unlockCodeErrorCount.value++
                                unlockCodeIncorrectMessage.value = "รหัสไม่ถูกต้อง กรุณาตรวจสอบ"
                                if (unlockCodeErrorCount.value >= 3) {
                                    showConfirmReceiveDialog.value = false
                                    showBlockDialog.value = true
                                    BleHelper.sendMessage(context, "smpc_code_false3time")
                                    // เคลยร์ค่ารหัสถุก
                                    clearAllState(
                                        context,
                                        coroutineScope,
                                        viewModel,
                                        recipientName,
                                        senderName,
                                        senderUnlockCode,
                                        processStatus
                                    )
                                }
                            }
                        },
                        enabled = enteredCode.length == 6
                    ) {
                        Text("ยืนยัน",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                },
                dismissButton = {
                    Button(onClick = {

                        showConfirmReceiveDialog.value = false
                        // reset codeDigits
                        codeDigits.forEach { it.value = "" }
                        unlockCodeIncorrectMessage.value = ""
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text("ยกเลิก", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
        // Speed Dial moved to overlay bottom-end
        // --------- Reset dialog (factory reset) ---------
        if (showResetDialog.value) {
            AlertDialog(
                onDismissRequest = { showResetDialog.value = false },
                title = {
                    Text("รีเซตอุปกรณ์", style = MaterialTheme.typography.headlineSmall)
                },
                text = {
                    if (isResetting.value) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("กำลังรีเซตกล่อง...")
                        }
                    } else {
                        Text(
                            "คุณต้องการรีเซตอุปกรณ์เป็นค่าโรงงานหรือไม่?",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                confirmButton = {
                    if (!isResetting.value) {
                        Button(onClick = {
                            isResetting.value = true
                            BleHelper.sendMessage(context, "smpc_resetfactory")

                            BleHelper.setMessageListener { msg ->
                                if (msg.trim() == "smpc_resetok") {
                                    isResetting.value = false
                                    showResetDialog.value = false
                                    clearAllState(
                                        context,
                                        coroutineScope,
                                        viewModel,
                                        recipientName,
                                        senderName,
                                        senderUnlockCode,
                                        processStatus
                                    )
                                    statusBuffer.clear()
                                    Toast.makeText(context, "รีเซตเสร็จสมบูรณ์", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Text("ยืนยัน")
                        }
                    }
                },
                dismissButton = {
                    if (!isResetting.value) {
                        Button(onClick = { showResetDialog.value = false }
                        ,colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground
                            )) {
                            Text("ยกเลิก")
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }

    // --- Overlay Speed Dial FAB (always on top) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f) // Ensure on top of everything
            .graphicsLayer { clip = false }
    ) {

            if (isConnected) {
                val shadowDp = if (isExpanded.value) 12.dp else 4.dp

                // Floating Button ตำแหน่งคงที่
                FloatingActionButton(
                    onClick = { isExpanded.value = !isExpanded.value },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 40.dp, bottom = 130.dp)
                        .size(64.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = if (!isExpanded.value) Icons.Default.Menu else Icons.Default.Close,
                        contentDescription = if (!isExpanded.value) "เมนู" else "ปิดเมนู",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // เมนูที่ขยาย "ขึ้นไป" เหนือ FAB
                AnimatedVisibility(
                    visible = isExpanded.value,
                    enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 35.dp, bottom = 210.dp) // ขยับขึ้นเหนือ FAB
                        .graphicsLayer {
                            clip = false // ✅ สำคัญ: ป้องกันไม่ให้ตัดเงา
                        }
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.graphicsLayer { clip = false }
                    ) {
                        // รีเฟรช
                        Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.graphicsLayer { clip = false } ) {
                            Text(
                                "รีเฟรชข้อมูล",
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    BleHelper.sendMessage(context, "smpc_status")

                                    isExpanded.value = false
                                },
                                modifier = Modifier
                                    .height(60.dp)
                                    .padding(end = 10.dp)
                                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(50)),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    Icons.Filled.Sync,
                                    "รีเฟรช",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // รีเซ็ต
                        Row(verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "คืนค่าโรงงาน",
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    showResetDialog.value = true
                                    isExpanded.value = false
                                },
                                modifier = Modifier
                                    .height(80.dp)
                                    .padding(bottom = 16.dp, end = 10.dp)
                                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(50)),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(50)

                            ) {
                                Icon(
                                    Icons.Filled.Restore,
                                    "คืนค่าโรงงาน",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }


        }
    }



// ฟังก์ชันแปลงรูปแบบวันที่จาก ddMMyyyy-HHmmss เป็น "d MMM yyyy เวลา HH:mm" (เช่น 4 ก.ค. 2025 เวลา 22:17)
fun formatSentTime(raw: String): String {
    return try {
        val inputFormat = SimpleDateFormat("ddMMyyyy-HHmmss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy เวลา HH:mm", Locale("th", "TH"))
        val date = inputFormat.parse(raw)
        outputFormat.format(date ?: return raw)
    } catch (e: Exception) {
        raw
    }
}
fun clearAllState(
    context: Context,
    coroutineScope: CoroutineScope,
    viewModel: HomeViewModel,
    recipientName: MutableState<String>,
    senderName: MutableState<String>,
    senderUnlockCode: MutableState<String>,
    processStatus: ProcessStatus
) {
    coroutineScope.launch {
        BleHelper.disconnect(context)

        // เคลียร์ค่าจาก UI ฝั่งผู้ส่ง
        recipientName.value = ""
        senderName.value = ""
        senderUnlockCode.value = ""

        // เคลียร์ค่าทั้งหมดใน processStatus
        processStatus.unlockCode = ""
        processStatus.sentTime = ""
        processStatus.sender = ""
        processStatus.recipient = ""
        processStatus.trackId = ""
        processStatus.flipCount = ""
        processStatus.impactCount = ""
        processStatus.tamperCount = ""
        processStatus.initialWeight = ""
        processStatus.currentWeight = ""
        processStatus.initialTemp = ""
        processStatus.currentTemp = ""

        // รีเซ็ตสถานะ BLE
        viewModel.setConnected(false)
        viewModel.setStatus("")
        viewModel.updateMac(null)
        viewModel.updateCurrentConnectedMac(null)
        viewModel.updateConnectedMac("")
    }
    }

