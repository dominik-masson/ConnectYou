package com.bnyro.contacts.navigation

import android.content.res.Configuration
import androidx.activity.addCallback
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bnyro.contacts.ui.activities.MainActivity

val bottomNavItems = listOf(
    NavRoutes.Phone,
    NavRoutes.Contacts,
    NavRoutes.Messages
)

@Composable
fun NavContainer(
    initialTabIndex: Int
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val initialTab = bottomNavItems[initialTabIndex.coerceIn(0, 1)]
    var selectedRoute by remember {
        mutableStateOf(initialTab)
    }
    LaunchedEffect(Unit) {
        val activity = context as MainActivity
        activity.onBackPressedDispatcher.addCallback {
            if (selectedRoute != NavRoutes.Settings && selectedRoute != NavRoutes.About) {
                activity.finish()
            } else {
                navController.popBackStack()
            }
        }
    }

    // listen for destination changes (e.g. back presses)
    DisposableEffect(Unit) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            allRoutes.firstOrNull { it.route == destination.route?.split("/")?.first() }
                ?.let { selectedRoute = it }
        }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val orientation = LocalConfiguration.current.orientation
    Scaffold(
        bottomBar = {
            if (orientation == Configuration.ORIENTATION_PORTRAIT && bottomNavItems.contains(
                    selectedRoute
                )
            ) {
                NavigationBar(
                    tonalElevation = 5.dp
                ) {
                    bottomNavItems.forEach {
                        NavigationBarItem(
                            label = {
                                Text(stringResource(it.stringRes!!))
                            },
                            icon = {
                                Icon(it.icon!!, null)
                            },
                            selected = it == selectedRoute,
                            onClick = {
                                selectedRoute = it
                                navController.navigate(it.route)
                            }
                        )
                    }
                }
            }
        }
    ) { pV ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(pV)
        ) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                NavigationRail {
                    bottomNavItems.forEach {
                        NavigationRailItem(selected = it == selectedRoute,
                            onClick = {
                                selectedRoute = it
                                navController.navigate(it.route)
                            },
                            icon = { Icon(it.icon!!, null) },
                            label = {
                                Text(stringResource(it.stringRes!!))
                            })
                    }
                }
            }
            AppNavHost(
                navController,
                startDestination = initialTab,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}