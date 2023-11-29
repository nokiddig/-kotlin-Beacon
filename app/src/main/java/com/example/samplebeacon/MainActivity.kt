@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.samplebeacon

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.samplebeacon.constant.ConstString
import com.example.samplebeacon.constant.ConstStyle
import com.example.samplebeacon.constant.ConstValue
import com.example.samplebeacon.ui.theme.SampleBeaconTheme
import org.altbeacon.beacon.AltBeacon
import org.altbeacon.beacon.Beacon

class MainActivity : ComponentActivity() {
    private lateinit var beaconService: BeaconService
    val TAG:String = "MainActivity"
    val appPermission = AppPermission(this)
    val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle Permission granted/rejected
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                Log.d(TAG, "$permissionName permission granted: $isGranted")
                if (isGranted) {

                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beaconService = BeaconService(this)
        appPermission.checkPermissions()
        setContent {
            SampleBeaconTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }

    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = ConstString.ROUTE_GREETING ){
            composable(ConstString.ROUTE_HOME) { HomeTab(navController = navController) }
            composable(ConstString.ROUTE_GREETING) { Greeting("Beacon", navController)}
        }
    }
    data class BottomNavItem(
        val title: String,
        val selectedIcon: ImageVector,
        val unSelectedIcon: ImageVector,
        val hasNews: Boolean,
        val badgeCount: Int? = null
    )
    @Composable
    fun HomeTab(navController: NavController) {
        val  items = listOf(
            BottomNavItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unSelectedIcon = Icons.Outlined.Home,
                hasNews = false
            ),
            BottomNavItem(
                title = "Send",
                selectedIcon = Icons.Filled.Share,
                unSelectedIcon = Icons.Outlined.Share,
                hasNews = false
            )
        )
        var selectedIndex by rememberSaveable {
            mutableStateOf(0)
        }
        val scannedBeacons = rememberSaveable { mutableStateOf(emptyList<Beacon>()) }

        Scaffold (modifier = Modifier.padding(5.dp),
            topBar = {TopAppBar(title = { Text(text = "Home")},
                navigationIcon = {
                    BackButton{
                        navController.popBackStack()
                    }
                })},
            content = {
                Column(Modifier.padding(top = 100.dp)) {
                    Row (Modifier.fillMaxWidth()){
                        Button(onClick = {
                            val beacon = AltBeacon.Builder()
                                .setId1(ConstValue.UUID) // UUID của Beacon
                                .setId2(ConstValue.MAJOR.toString()) // Major
                                .setId3(ConstValue.MINOR.toString()) // Minor
                                .setManufacturer(0xFFFF) // Nhà sản xuất (đây là ví dụ với nhà sản xuất AltBeacon)
                                .setTxPower(-59) // Công suất phát tín hiệu
                                .setDataFields(listOf(0L)) // Dữ liệu tùy chỉnh (nếu cần)
                                .build()
                            beaconService.startAdvertising(beacon)
                        },
                            modifier = Modifier.weight(1f)    ) {
                            Text(text = "Beacon Broadcast")
                        }
                        Button(onClick = {
                            beaconService.stopAdvertising()
                        },
                            modifier = Modifier.weight(1f)) {
                            Text(text = "Stop Broadcast")
                        }
                    }
                    Row (Modifier.fillMaxWidth()){
                        Button(
                            onClick = {
                                beaconService.startScanBeacons { beacons ->
                                    scannedBeacons.value = beacons
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Start scanning beacon")
                        }
                        Button(
                            onClick = {
                                beaconService.stopScanning()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Stop scanning beacon")
                        }
                    }
                    ScanResult(beaconList = scannedBeacons,
                        Modifier
                            .padding(it)
                            .height(200.dp))
                    Button(
                        onClick = {
                            beaconService.startInBackground(60000)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Start in background 1 minute")
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = {
                                selectedIndex = index
                                navController.navigate(item.title)
                            },
                            icon = {
                                BadgedBox(badge = {

                                }) {
                                    Icon(
                                        imageVector = if (index == selectedIndex) item.selectedIcon
                                            else item.unSelectedIcon,
                                        contentDescription = item.title)
                                }
                            },
                            label = {Text(text = item.title)})
                    }
                }
            }
        )
    }

    @Composable
    fun ScanResult(beaconList: MutableState<List<Beacon>>, modifier: Modifier) {
        var scannedBeacons by remember { mutableStateOf(beaconList) }
        if (scannedBeacons.value.isEmpty()){
            LazyColumn (modifier = Modifier
                .fillMaxWidth()
                .height(ConstValue.HOME_LAZYCOLUMN_HEIGHT.dp)
                .border(color = Color.Black, width = 2.dp)){
                item {
                    Text(text = "Beacon not found")
                }
            }
        }
        else {
            LazyColumn (modifier = Modifier
                .fillMaxWidth()
                .height(ConstValue.HOME_LAZYCOLUMN_HEIGHT.dp)
                .border(color = Color.Black, width = 2.dp)){
                items(scannedBeacons.value) { beacon ->
                    Text("Beacon: $beacon")
                }
            }
        }

        // Cập nhật danh sách beacon khi có thay đổi
        LaunchedEffect(beaconList) {
            scannedBeacons = beaconList
        }
    }

    @Composable
    fun BackButton(onclick: () -> Unit) {
        IconButton(onClick = onclick) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
        }
    }
}

@Composable
fun Greeting(name: String, navController: NavController) {
    Column (modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "Hello $name!",
            style = ConstStyle.h1
        )
        Button(onClick = {
            navController.navigate(ConstString.ROUTE_HOME)
        }) {
            Text(text = "Home screen")
        }
    }
}
