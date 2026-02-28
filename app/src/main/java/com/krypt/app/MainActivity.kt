package com.krypt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay

// ─── Color System ─────────────────────────────────────────────────────────────
val D_BG      = Color(0xFF000000)
val D_SURFACE = Color(0xFF0F0F0F)
val D_CARD    = Color(0xFF1A1A1A)
val D_DIVIDER = Color(0xFF262626)
val D_TEXT    = Color(0xFFF2F2F2)
val D_SUBTEXT = Color(0xFF888888)

val L_BG      = Color(0xFFFAFAFA)
val L_SURFACE = Color(0xFFFFFFFF)
val L_CARD    = Color(0xFFF2F2F2)
val L_DIVIDER = Color(0xFFE5E5E5)
val L_TEXT    = Color(0xFF111111)
val L_SUBTEXT = Color(0xFF777777)

object KryptTheme {
    var isDark by mutableStateOf(true)
    val bg:      Color get() = if (isDark) D_BG      else L_BG
    val surface: Color get() = if (isDark) D_SURFACE else L_SURFACE
    val card:    Color get() = if (isDark) D_CARD    else L_CARD
    val divider: Color get() = if (isDark) D_DIVIDER else L_DIVIDER
    val text:    Color get() = if (isDark) D_TEXT    else L_TEXT
    val subtext: Color get() = if (isDark) D_SUBTEXT else L_SUBTEXT
    val accent:  Color get() = if (isDark) D_TEXT    else L_TEXT // white in dark, black in light
    val danger:  Color = Color(0xFFE53935)
    val success: Color = Color(0xFF43A047)
}

// Legacy aliases for backward compat
val KryptBlack   get() = KryptTheme.bg
val KryptDark    get() = KryptTheme.surface
val KryptCard    get() = KryptTheme.card
val KryptAccent  get() = KryptTheme.accent
val KryptText    get() = KryptTheme.text
val KryptSubtext get() = KryptTheme.subtext
// unused legacy color - kept for compile compat
val MatrixGreen  = Color(0xFF4CAF50)

private fun darkScheme() = darkColorScheme(
    primary = D_TEXT, onPrimary = D_BG, background = D_BG, onBackground = D_TEXT,
    surface = D_SURFACE, onSurface = D_TEXT, surfaceVariant = D_CARD, onSurfaceVariant = D_TEXT,
    secondary = D_TEXT, onSecondary = D_BG, outline = D_DIVIDER
)
private fun lightScheme() = lightColorScheme(
    primary = L_TEXT, onPrimary = L_BG, background = L_BG, onBackground = L_TEXT,
    surface = L_SURFACE, onSurface = L_TEXT, surfaceVariant = L_CARD, onSurfaceVariant = L_TEXT,
    secondary = L_TEXT, onSecondary = L_BG, outline = L_DIVIDER
)

class MainActivity : ComponentActivity() {
    private val viewModel: KryptViewModel by viewModels { KryptViewModel.Factory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("krypt_prefs", MODE_PRIVATE)
        KryptTheme.isDark = prefs.getBoolean("dark_mode", true)
        setContent {
            val isDark = KryptTheme.isDark
            MaterialTheme(colorScheme = if (isDark) darkScheme() else lightScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = KryptTheme.bg) {
                    KryptApp(viewModel = viewModel, onToggleTheme = {
                        KryptTheme.isDark = !KryptTheme.isDark
                        prefs.edit().putBoolean("dark_mode", KryptTheme.isDark).apply()
                    })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkClient.disconnect()
    }
}

@Composable
fun KryptApp(viewModel: KryptViewModel, onToggleTheme: () -> Unit) {
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(2200); showSplash = false }
    AnimatedContent(
        targetState = showSplash,
        transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(400)) },
        label = "splash"
    ) { splash ->
        if (splash) MinimalistSplash() else KryptNavGraph(viewModel, onToggleTheme)
    }
}

@Composable
fun MinimalistSplash() {
    val alpha by animateFloatAsState(1f, tween(1000, easing = FastOutSlowInEasing), label = "a")
    val infiniteTransition = rememberInfiniteTransition(label = "p")
    val lineW by infiniteTransition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), "lw"
    )

    Box(Modifier.fillMaxSize().background(D_BG), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(alpha)) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = D_CARD
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔒", fontSize = 34.sp)
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("KRYPT", fontSize = 28.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Default,
                color = D_TEXT, letterSpacing = 10.sp)
            Spacer(Modifier.height(8.dp))
            Text("end-to-end encrypted", fontSize = 11.sp, color = D_SUBTEXT, letterSpacing = 2.sp)
            Spacer(Modifier.height(36.dp))
            Box(Modifier.width(56.dp).height(1.dp).background(D_DIVIDER)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(lineW).background(D_TEXT.copy(alpha = 0.4f)))
            }
        }
        Text("by Rahul", color = D_SUBTEXT.copy(alpha = 0.4f), fontSize = 11.sp, letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp).alpha(alpha))
    }
}

@Composable
fun KryptNavGraph(viewModel: KryptViewModel, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.callState.isInCall) {
        if (uiState.callState.isInCall) {
            navController.navigate("call/${uiState.callState.remoteUuid}") { launchSingleTop = true }
        }
    }

    NavHost(navController = navController, startDestination = "contacts") {
        composable("contacts") {
            ContactsScreen(
                viewModel = viewModel,
                onOpenChat = { uuid -> viewModel.openConversation(uuid); navController.navigate("chat/$uuid") },
                onOpenStatus = { navController.navigate("status") },
                onToggleTheme = onToggleTheme
            )
        }
        composable("chat/{uuid}", arguments = listOf(navArgument("uuid") { type = NavType.StringType })) { bs ->
            val uuid = bs.arguments?.getString("uuid") ?: return@composable
            ChatScreen(viewModel = viewModel, contactUuid = uuid,
                onStartCall = { viewModel.startCall(uuid) },
                onBack = { navController.popBackStack() })
        }
        composable("call/{uuid}", arguments = listOf(navArgument("uuid") { type = NavType.StringType })) { bs ->
            val uuid = bs.arguments?.getString("uuid") ?: return@composable
            CallScreen(viewModel = viewModel, remoteUuid = uuid, onEndCall = {
                viewModel.endCall(); navController.popBackStack()
            })
        }
        composable("status") {
            StatusScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
