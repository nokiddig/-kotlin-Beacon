@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.samplebeacon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.samplebeacon.ui.theme.SampleBeaconTheme
import org.altbeacon.beacon.Beacon

class MainActivity : ComponentActivity() {
    lateinit var beaconService: BeaconService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beaconService = BeaconService(this)

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
        val  items = listOf<BottomNavItem>(
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
        var scannedBeacons = rememberSaveable { mutableStateOf(emptyList<Beacon>()) }
        beaconService.scanBeacons { beacons -> scannedBeacons.value = beacons }
        Scaffold (modifier = Modifier.padding(5.dp),
            topBar = {TopAppBar(title = { Text(text = "Home")},
                navigationIcon = {
                    BackButton{
                        navController.popBackStack()
                    }
                })},
            content = {
                Column(Modifier.padding(top = 100.dp)) {
                    Button(onClick = {
                        var beacon:Beacon = Beacon.Builder()
                            .setId1("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6") // UUID của Beacon
                            .setId2("1") // Major
                            .setId3("2") // Minor
                            .setManufacturer(0xFFFF) // Nhà sản xuất (đây là ví dụ với nhà sản xuất AltBeacon)
                            .setTxPower(-59) // Công suất phát tín hiệu
                            .setDataFields(listOf(0L)) // Dữ liệu tùy chỉnh (nếu cần)
                            .build()
                        beaconService.startAdvertising(beacon)
                    },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Beacon Broadcast")
                    }
                    Button(onClick = {
                        beaconService.stopAdvertising()
                    },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Stop Broadcast")
                    }
                    ScanResult(beaconList = scannedBeacons,
                        Modifier
                            .padding(it)
                            .height(200.dp))
                    Button(
                        onClick = {
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Start On Background")
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
                .height(200.dp)
                .border(color = Color.Black, width = 2.dp)){
                item {
                    Text(text = "Scanning.....")
                }
            }
        }
        else {
            LazyColumn (modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(color = Color.Black, width = 2.dp)){
                items(scannedBeacons.value) { beacon ->
                    // Hiển thị thông tin về beacon
                    Text("Beacon: ${beacon.toString()}")
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
            Text(text = "Home")
        }
    }
}
