@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.samplebeacon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.samplebeacon.constant.ConstString
import com.example.samplebeacon.constant.ConstStyle
import com.example.samplebeacon.ui.theme.SampleBeaconTheme

class MainActivity : ComponentActivity() {
    lateinit var beaconApp: BeaconReferenceApplication
    override fun onCreate(savedInstanceState: Bundle?) {
//        beaconApp = application as BeaconReferenceApplication
//        val regionViewModel = BeaconManager.getInstanceForApplication(this).getRegionViewModel(beaconApp.region)
//        // observer will be called each time the monitored regionState changes (inside vs. outside region)
//        regionViewModel.regionState.observe(this, monitoringObserver)
//        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
//        regionViewModel.rangedBeacons.observe(this, rangingObserver)

        super.onCreate(savedInstanceState)
        setContent {
            SampleBeaconTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }

//    val monitoringObserver = Observer<Int> { state ->
//        var dialogTitle = "Beacons detected"
//        var dialogMessage = "didEnterRegionEvent has fired"
//        var stateString = "inside"
//        if (state == MonitorNotifier.OUTSIDE) {
//            dialogTitle = "No beacons detected"
//            dialogMessage = "didExitRegionEvent has fired"
//            stateString == "outside"
//            beaconCountTextView.text = "Outside of the beacon region -- no beacons detected"
//            beaconListView.adapter = ArrayAdapter(this, R.layout.simple_list_item_1, arrayOf("--"))
//        }
//        else {
//            beaconCountTextView.text = "Inside the beacon region."
//        }
//        Log.d(TAG, "monitoring state changed to : $stateString")
//        val builder =
//            AlertDialog.Builder(this)
//        builder.setTitle(dialogTitle)
//        builder.setMessage(dialogMessage)
//        builder.setPositiveButton(R.string.ok, null)
//        alertDialog?.dismiss()
//        alertDialog = builder.create()
//        alertDialog?.show()
//    }
//
//    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
//        Log.d(TAG, "Ranged: ${beacons.count()} beacons")
//        if (BeaconManager.getInstanceForApplication(this).rangedRegions.size > 0) {
//            beaconCountTextView.text = "Ranging enabled: ${beacons.count()} beacon(s) detected"
//            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
//                beacons
//                    .sortedBy { it.distance }
//                    .map { "${it.id1}\nid2: ${it.id2} id3:  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray())
//        }
//    }
}
@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ConstString.ROUTE_GREETING ){
        composable(ConstString.ROUTE_HOME) { HomeTab(navController = navController) }
        composable(ConstString.ROUTE_GREETING) { Greeting("Beacon", navController)}
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
    Scaffold (modifier = Modifier.padding(5.dp),
        topBar = {TopAppBar(title = { Text(text = "Home")})},
        content = { Column(modifier = Modifier.padding(it)) {
            Text(text = "hello")
        }},
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
                        })
                }
            }
        }
    )
}
