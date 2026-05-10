package com.nateapps.smartparcelapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nateapps.smartparcelapp.ui.theme.OrangePrimary
import com.nateapps.smartparcelapp.ui.theme.SmartParcelAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: HomeViewModel by viewModels()

        // BleHelper.setMessageListener will now be set after a successful connection.

        setContent {

            SmartParcelAppTheme() {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    systemUiController.setStatusBarColor(
                        color = OrangePrimary,
                        darkIcons = false
                    )
                }

                // --- BLE Auto-Connect LaunchedEffect ---
                androidx.compose.runtime.LaunchedEffect(viewModel.macAddress.collectAsState().value) {
                    val mac = viewModel.macAddress.value
                    if (mac.isNullOrBlank()) return@LaunchedEffect
                    if (viewModel.isConnected.value || mac == viewModel.currentConnectedMac.value) return@LaunchedEffect

                    viewModel.updateCurrentConnectedMac(mac)

                    BleHelper.connect(
                        context = this@MainActivity,
                        macAddress = mac,
                        onConnected = {
                            Log.d("BLE", "✅ เชื่อมต่อกับ $mac สำเร็จ")
                            //viewModel.setStatus("")
                            //viewModel.clearStatusBuffer()
                            //viewModel.clearProcessStatus()
                            //viewModel.setStatusReady(false)
                            //viewModel.setWaitingForStatusResponse(false)

                            viewModel.updateConnectedMac(mac)
                            viewModel.setConnected(true)
                            setupBleMessageListener(viewModel, this@MainActivity)
                        },
                        onFail = {
                            Log.e("BLE", "❌ เชื่อมต่อกับ $mac ล้มเหลว")
                            viewModel.updateMac(null)
                            viewModel.setConnected(false)
                            viewModel.updateCurrentConnectedMac(null)
                        }
                    )
                }
                // --- End BLE Auto-Connect LaunchedEffect ---

                SmartParcelApp()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SmartParcelApp() {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTabIndex) { newIndex ->
                selectedTabIndex = newIndex
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(animationSpec = tween(300)) { fullWidth: Int -> fullWidth }  with
                            slideOutHorizontally(animationSpec = tween(300)) { fullWidth: Int -> -fullWidth }
                } else {
                    slideInHorizontally(animationSpec = tween(300)) { fullWidth: Int -> -fullWidth } with
                            slideOutHorizontally(animationSpec = tween(300)) { fullWidth: Int -> fullWidth }
                }.using(
                    SizeTransform(clip = false)
                )
            },
            //modifier = Modifier.padding(padding)
        ) { screen ->
            when (screen) {
                0 -> HomeScreen(onNavigateToHistory = { selectedTabIndex = 1 })
                1 -> HistoryScreen()
                2 -> MoreScreen()
            }
        }
    }
}
    private fun setupBleMessageListener(viewModel: HomeViewModel, context: android.content.Context) {
        var statusMode = false
        val statusBuffer = mutableListOf<String>()
        BleHelper.setMessageListener { msg ->
            val decodedMsg = msg.trim()
            android.util.Log.d("BLE", "📩 อ่านค่า ได้Received: '$decodedMsg'")

            // Status boundaries handler
            if (decodedMsg == "smpc_status_start") {
                statusBuffer.clear()
                viewModel.clearStatusBuffer()
                statusMode = true
            } else if (decodedMsg == "smpc_status_end") {
                statusMode = false
                viewModel.updateProcessStatusFromBuffer()
            } else if (statusMode) {
                viewModel.appendStatusLine(decodedMsg)
                Log.d("BLE", " Status รวม buffer (${statusBuffer.size} lines so far)")
            } else {
                // Handle other BLE messages as before
                when {
                    decodedMsg == "smpc_welcome" -> {
                        viewModel.setConnected(true)
                        BleHelper.sendMessage(context, "smpc_status")
                        Log.d("BLE", "----------'รอ status.txt'-----------")
                    }
                    decodedMsg == "smpc_done_thank" -> {
                        viewModel.setWaitingForThank(false)
                        viewModel.setShowSuccessState(true)
                    }
                    decodedMsg == "smpc_disconnect" -> {
                        BleHelper.disconnect(context)
                        viewModel.clearAllStateFromMain(context)
                    }
                    decodedMsg == "smpc_list_info_end" -> {
                        viewModel.setHasFetchedHistory(true)
                    }
                }
            }

            if (decodedMsg.startsWith("smpc_nowtemp_")) {
                val tempStr = decodedMsg.removePrefix("smpc_nowtemp_").trim()
                viewModel.setLiveTemp(tempStr)
                Log.d("BLE", "🌡️ liveTemp updated via Main: $tempStr")
            }

            if (decodedMsg.startsWith("smpc_nowkg_")) {
                val weightStr = decodedMsg.removePrefix("smpc_nowkg_").trim()
                viewModel.setLiveWeight(weightStr)
                Log.d("BLE", "⚖️ liveWeight updated via Main: $weightStr")
            }
        }
        Log.d("BLE", "✅ BLE message listener is set up")
    }