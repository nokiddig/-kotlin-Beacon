@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.samplebeacon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.samplebeacon.constant.ConstString
import com.example.samplebeacon.constant.ConstStyle
import com.example.samplebeacon.constant.ConstValue
import com.example.samplebeacon.ui.theme.SampleBeaconTheme

class MainActivity : ComponentActivity() {
    lateinit var beaconApp: BeaconReferenceApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val viewmodel:MainViewmodel = viewModel()
    val listBeacon by rememberSaveable { mutableStateOf(viewmodel.getListBeacon()) }
    Scaffold (modifier = Modifier.padding(5.dp),
        topBar = {TopAppBar(title = { Text(text = "Home")},
            navigationIcon = {
                BackButton{
                    navController.popBackStack()
                }
            })},
        content = {
                  LazyColumn(modifier = Modifier
                      .padding(it)
                      .height(ConstValue.HOME_LAZYCOLUMN_HEIGHT.dp)) {
                      items(items = listBeacon) {
                          item -> Text(text = item.toString())
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
fun BackButton(onclick: () -> Unit) {
    IconButton(onClick = onclick) {
        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
    }
}
