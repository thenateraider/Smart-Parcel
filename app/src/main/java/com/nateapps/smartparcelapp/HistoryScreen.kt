package com.nateapps.smartparcelapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Landslide
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
@Preview
fun HistoryScreen(viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val fileList by viewModel.historyFileList.collectAsState()
    val isLoading = remember { mutableStateOf(false) }

    // State for showing detail dialog and info
    val showDetailDialog = remember { mutableStateOf(false) }
    val infoLines = remember { mutableStateOf<List<String>>(emptyList()) }
    val isFetchingInfo = remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 30.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        {

            Box(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp)
                ,
                contentAlignment = Alignment.BottomCenter,

            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp, top = 36.dp)
                        ,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,

                    ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f),
                        //fill max width for text


                    ) {
                        Column (
                            horizontalAlignment = Alignment.Start

                        ) {
                            Text(
                                text = "ประวัติ\n\nการส่งพัสดุ",
                                //modifier = Modifier.padding(end = 3.dp),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold

                            )

                            Text(
                                text ="แสดง 10 รายการล่าสุด",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                            Spacer(modifier = Modifier.padding(24.dp))
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "History Icon",
                                modifier = Modifier.size(64.dp)
                            )
                    }

                    // Hide the refresh button in the top row if there is a list
                    /*if (fileList.isEmpty()) {
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.clearHistoryFileList()
                                val tempList = mutableListOf<String>()
                                isLoading.value = true
                                BleHelper.sendMessage(context, "smpc_list_info")

                                BleHelper.setMessageListener { msg ->
                                    when {
                                        msg == "empty" -> {
                                            viewModel.updateHistoryFileList(emptyList())
                                            isLoading.value = false
                                        }
                                        msg == "smpc_list_info_end" -> {
                                            viewModel.updateHistoryFileList(tempList)
                                            isLoading.value = false
                                        }
                                        msg.startsWith("T") && msg.length == 5 -> {
                                            if (!tempList.contains(msg)) {
                                                tempList.add(msg)
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("รีเฟรชประวัติ", style = MaterialTheme.typography.titleLarge)
                        }
                    }*/
                }
            }// Title and Refresh Button Row (always visible)


            // Bottom 70%
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 85.dp, start = 13.dp, end = 13.dp) // Add padding to the bottom
                    .weight(0.85f) // Fill 85% of the remaining space
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
                    ) // Set background color and radius
                    .clip(RoundedCornerShape(36.dp)) // Clip the content to the rounded shape

            ) {
                if (isLoading.value) {
                    Box(
                        modifier = Modifier.fillMaxSize(), // Fill the entire Box
                        contentAlignment = Alignment.Center // Center the CircularProgressIndicator
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                        )
                    }
                } else if (fileList.isEmpty()) {
                    // Show text below if fileList is empty
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        Text("ยังไม่มีประวัติ", fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.padding(8.dp))
                        // Show refresh button below the text if fileList is empty
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.clearHistoryFileList()
                                val tempList = mutableListOf<String>()
                                isLoading.value = true
                                BleHelper.sendMessage(context, "smpc_list_info")

                                BleHelper.setMessageListener { msg ->
                                    when {
                                        msg == "empty" -> {
                                            viewModel.updateHistoryFileList(emptyList())
                                            isLoading.value = false
                                        }
                                        msg == "smpc_list_info_end" -> {
                                            viewModel.updateHistoryFileList(tempList)
                                            isLoading.value = false
                                        }
                                        msg.startsWith("T") && msg.length == 5 -> {
                                            if (!tempList.contains(msg)) {
                                                tempList.add(msg)
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("รีเฟรชประวัติ",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        items(fileList) { name ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(16.dp)
                                    .clickable {
                                        val command = "smpc_${name}"
                                        isFetchingInfo.value = true
                                        showDetailDialog.value = true
                                        infoLines.value = emptyList()

                                        BleHelper.sendMessage(context, command)

                                        val tempLines = mutableListOf<String>()
                                        BleHelper.setMessageListener { msg ->
                                            if (msg == ";") {
                                                infoLines.value = tempLines.toList()
                                                isFetchingInfo.value = false
                                            } else {
                                                tempLines.add(msg)
                                            }
                                        }
                                    }
                            ) {
                                Text(text = "📦 รหัสพัสดุเลขที่ : $name", fontSize = 18.sp)
                            }
                        }
                    }
                    // Floating Action Button for refresh when list is not empty
                    FloatingActionButton(
                        onClick = {
                            viewModel.clearHistoryFileList()
                            val tempList = mutableListOf<String>()
                            isLoading.value = true
                            BleHelper.sendMessage(context, "smpc_list_info")

                            BleHelper.setMessageListener { msg ->
                                when {
                                    msg == "empty" -> {
                                        viewModel.updateHistoryFileList(emptyList())
                                        isLoading.value = false
                                    }
                                    msg == "smpc_list_info_end" -> {
                                        viewModel.updateHistoryFileList(tempList)
                                        isLoading.value = false
                                    }
                                    msg.startsWith("T") && msg.length == 5 -> {
                                        if (!tempList.contains(msg)) {
                                            tempList.add(msg)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 10.dp, end = 10.dp) // Adjusted padding
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)), // Added shadow
                        containerColor = Color.Black // Changed color
                    ) { // ไอคอนรีเฟรช สีขาว
                        Icon(Icons.Filled.Refresh, "Refresh history", tint = Color.White)
                    }
                }
            }
        }
        // Detail Dialog
        if (showDetailDialog.value) {
            AlertDialog(
                containerColor = MaterialTheme.colorScheme.surface,
                onDismissRequest = {
                    showDetailDialog.value = false
                    infoLines.value = emptyList()
                    isFetchingInfo.value = false
                },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = {
                        showDetailDialog.value = false
                        infoLines.value = emptyList()
                        isFetchingInfo.value = false
                    }) {
                        Text("ปิด",
                            style = MaterialTheme.typography.titleLarge)
                        // สีตัวหนังสือที่นี่ไม่ได้เปลี่ยนตาม instruction
                    }
                },
                title = {
                    Row (verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Inventory2,
                            contentDescription = "Box Icon",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("รายละเอียดพัสดุ",

                            //style = MaterialTheme.typography.headlineMedium,
                            fontSize = 32.dp.value.sp,
                            fontStyle = MaterialTheme.typography.titleLarge.fontStyle,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                },
                text = {
                    if (isFetchingInfo.value) {
                        Column (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Column {
                            val info = infoLines.value
                            val trackid = info.getOrNull(4) ?: "ไม่มีข้อมูล" //T0001
                            val sender = info.getOrNull(1) ?: "ไม่มีข้อมูล"
                            //***********************
                            val rawSentTime = info.getOrNull(5) ?: "ไม่มีข้อมูล" //DDMMYYYY-HHMMSS
                            val sentTime = if (rawSentTime != "ไม่มีข้อมูล" && rawSentTime.length == 15) {
                                try {
                                    val parser = SimpleDateFormat("ddMMyyyy-HHmmss", Locale.getDefault())
                                    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                    formatter.format(parser.parse(rawSentTime)!!)
                                } catch (e: Exception) {
                                    rawSentTime // fallback to raw if parsing fails
                                }
                            } else rawSentTime
                            //***********************

                            //***********************
                            val rawGetTime = info.getOrNull(3) ?: "ไม่มีข้อมูล" //DDMMYYYY-HHMMSS
                            val getTime = if (rawGetTime != "ไม่มีข้อมูล" && rawGetTime.length == 15) {
                                try {
                                    val parser = SimpleDateFormat("ddMMyyyy-HHmmss", Locale.getDefault())
                                    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                    formatter.format(parser.parse(rawGetTime)!!)
                                } catch (e: Exception) {
                                    rawGetTime // fallback to raw if parsing fails
                                }
                            } else rawGetTime
                            //***********************

                            val recipient = info.getOrNull(2) ?: "ไม่มีข้อมูล"
                            val flipCount = info.getOrNull(6) ?: "ไม่มีข้อมูล"
                            val impactCount = info.getOrNull(7) ?: "ไม่มีข้อมูล"
                            val tamperCount = info.getOrNull(8) ?: "ไม่มีข้อมูล"
                            val weight = info.getOrNull(9)?.replace("-", " → ") ?: "ไม่มีข้อมูล"
                            val temp = info.getOrNull(10)?.replace("-", "°C → ")?.plus("°C") ?: "ไม่มีข้อมูล"

                            Text("ข้อมูลจ่าหน้าพัสดุ", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Filled.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Track ID : $trackid", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Default.PunchClock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("เวลาที่ส่ง : $sentTime", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("เวลาที่รับ : $getTime", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ชื่อผู้ส่ง : $sender", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Default.PersonPinCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ชื่อผู้รับ : $recipient", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("บันทึกขณะการนำส่ง", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Default.ScreenRotation, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("การพลิกคว่ำ : $flipCount ครั้ง", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Default.Landslide, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("การตกกระแทก : $impactCount ครั้ง", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("การงัดแงะ : $tamperCount ครั้ง", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Outlined.Scale, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("น้ำหนัก : $weight kg", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Outlined.Thermostat, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("อุณหภูมิ : $temp", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            )
        }
    }
}