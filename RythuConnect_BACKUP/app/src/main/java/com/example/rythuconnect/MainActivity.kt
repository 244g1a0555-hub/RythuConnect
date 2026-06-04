package com.example.rythuconnect


import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.rythuconnect.ui.theme.RythuConnectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.ui.platform.LocalContext
enum class Lang { ENGLISH, TELUGU, HINDI, KANNADA, MALAYALAM, MARATHI }

object AppLang {
    var lang = mutableStateOf(Lang.ENGLISH)
    fun t(en: String, te: String, hi: String, kn: String, ml: String, mr: String) = when (lang.value) {
        Lang.TELUGU -> te; Lang.HINDI -> hi; Lang.KANNADA -> kn
        Lang.MALAYALAM -> ml; Lang.MARATHI -> mr; else -> en
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RythuConnectTheme { RythuConnectApp() } }
    }
}

@Composable
fun RythuConnectApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "home" else "login"
    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = "login") { LoginScreen(navController) }
        composable(route = "signup") { SignupScreen(navController) }
        composable(route = "home") { HomeScreen(navController) }
        composable(route = "sell_crops") { SellCropsScreen(navController) }
        composable(route = "view_crops") { ViewCropsScreen(navController) }
        composable(route = "buy_crops") { BuyCropsScreen(navController) }
        composable(route = "weather") { WeatherScreen(navController) }
        composable(route = "market_prices") { MarketPricesScreen(navController) }
        composable(route = "contact") { ContactScreen(navController) }
        composable(route = "profile") { ProfileScreen(navController) }
        composable(route = "nearby_markets") { NearbyMarketsScreen(navController) }
        composable(route = "feedback") { FeedbackScreen(navController) }
    }
}
@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val green = Color(0xFF2E7D32)
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        LangSelector()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ã°Å¸Å’Â¾ Rythu Connect", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = green)
        Text(AppLang.t("Login","Ã Â°Â²Ã Â°Â¾Ã Â°â€”Ã Â°Â¿Ã Â°Â¨Ã Â±Â","Ã Â¤Â²Ã Â¥â€°Ã Â¤â€”Ã Â¤Â¿Ã Â¤Â¨","Ã Â²Â²Ã Â²Â¾Ã Â²â€”Ã Â²Â¿Ã Â²Â¨Ã Â³Â","Ã Â´Â²Ã Âµâ€¹Ã Â´â€”Ã Â´Â¿Ã ÂµÂ»","Ã Â¤Â²Ã Â¥â€°Ã Â¤â€”Ã Â¤Â¿Ã Â¤Â¨"), fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it },
            label = { Text(AppLang.t("Email","Ã Â°â€¡Ã Â°Â®Ã Â±â€ Ã Â°Â¯Ã Â°Â¿Ã Â°Â²Ã Â±Â","Ã Â¤Ë†Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â²","Ã Â²â€¡Ã Â²Â®Ã Â³â€¡Ã Â²Â²Ã Â³Â","Ã Â´â€¡Ã Â´Â®Ã Âµâ€ Ã Â´Â¯Ã Â´Â¿Ã ÂµÂ½","Ã Â¤Ë†Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â²")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text(AppLang.t("Password","Ã Â°ÂªÃ Â°Â¾Ã Â°Â¸Ã Â±ÂÃ¢â‚¬Å’Ã Â°ÂµÃ Â°Â°Ã Â±ÂÃ Â°Â¡Ã Â±Â","Ã Â¤ÂªÃ Â¤Â¾Ã Â¤Â¸Ã Â¤ÂµÃ Â¤Â°Ã Â¥ÂÃ Â¤Â¡","Ã Â²ÂªÃ Â²Â¾Ã Â²Â¸Ã Â³ÂÃ¢â‚¬Å’Ã Â²ÂµÃ Â²Â°Ã Â³ÂÃ Â²Â¡Ã Â³Â","Ã Â´ÂªÃ Â´Â¾Ã Â´Â¸Ã ÂµÂÃ¢â‚¬Å’Ã Â´ÂµÃ Âµâ€¡Ã Â´Â¡Ã ÂµÂ","Ã Â¤ÂªÃ Â¤Â¾Ã Â¤Â¸Ã Â¤ÂµÃ Â¤Â°Ã Â¥ÂÃ Â¤Â¡")) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { TextButton(onClick = { passwordVisible = !passwordVisible }) { Text(if (passwordVisible) "Hide" else "Show", fontSize = 12.sp) } },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        if (errorMsg.isNotEmpty()) { Text(errorMsg, color = Color.Red, fontSize = 13.sp); Spacer(modifier = Modifier.height(8.dp)) }
        Button(onClick = {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                isLoading = true
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { isLoading = false; navController.navigate("home") { popUpTo("login") { inclusive = true } } }
                    .addOnFailureListener { isLoading = false; errorMsg = it.message ?: "Login failed" }
            } else { errorMsg = "Please fill all fields" }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
            Text(if (isLoading) "Please wait..." else AppLang.t("Login","Ã Â°Â²Ã Â°Â¾Ã Â°â€”Ã Â°Â¿Ã Â°Â¨Ã Â±Â","Ã Â¤Â²Ã Â¥â€°Ã Â¤â€”Ã Â¤Â¿Ã Â¤Â¨","Ã Â²Â²Ã Â²Â¾Ã Â²â€”Ã Â²Â¿Ã Â²Â¨Ã Â³Â","Ã Â´Â²Ã Âµâ€¹Ã Â´â€”Ã Â´Â¿Ã ÂµÂ»","Ã Â¤Â²Ã Â¥â€°Ã Â¤â€”Ã Â¤Â¿Ã Â¤Â¨"), color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("signup") }) {
            Text(AppLang.t("No account? Signup","Ã Â°â€¦Ã Â°â€¢Ã Â±Å’Ã Â°â€šÃ Â°Å¸Ã Â±Â Ã Â°Â²Ã Â±â€¡Ã Â°Â¦Ã Â°Â¾? Ã Â°Â¸Ã Â±Ë†Ã Â°Â¨Ã Â±ÂÃ Â°â€¦Ã Â°ÂªÃ Â±Â","Ã Â¤â€“Ã Â¤Â¾Ã Â¤Â¤Ã Â¤Â¾ Ã Â¤Â¨Ã Â¤Â¹Ã Â¥â‚¬Ã Â¤â€š? Ã Â¤Â¸Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â¨Ã Â¤â€¦Ã Â¤Âª","Ã Â²â€“Ã Â²Â¾Ã Â²Â¤Ã Â³â€  Ã Â²â€¡Ã Â²Â²Ã Â³ÂÃ Â²Â²Ã Â²ÂµÃ Â³â€¡? Ã Â²Â¸Ã Â³Ë†Ã Â²Â¨Ã Â³ÂÃ¢â‚¬Å’Ã Â²â€¦Ã Â²ÂªÃ Â³Â","Ã Â´â€¦Ã Â´â€¢Ã ÂµÂÃ Â´â€¢Ã Âµâ€”Ã Â´Â£Ã ÂµÂÃ Â´Å¸Ã ÂµÂ Ã Â´â€¡Ã Â´Â²Ã ÂµÂÃ Â´Â²Ã Âµâ€¡? Ã Â´Â¸Ã ÂµË†Ã ÂµÂ»Ã Â´â€¦Ã Â´ÂªÃ ÂµÂ","Ã Â¤â€“Ã Â¤Â¾Ã Â¤Â¤Ã Â¥â€¡ Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Â¹Ã Â¥â‚¬? Ã Â¤Â¸Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â¨Ã Â¤â€¦Ã Â¤Âª"), color = green)
        }
    }
}

@Composable
fun SignupScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val green = Color(0xFF2E7D32)
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        LangSelector()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Ã°Å¸Å’Â¾ ${AppLang.t("New Account","Ã Â°â€¢Ã Â±Å Ã Â°Â¤Ã Â±ÂÃ Â°Â¤ Ã Â°â€¦Ã Â°â€¢Ã Â±Å’Ã Â°â€šÃ Â°Å¸Ã Â±Â","Ã Â¤Â¨Ã Â¤Â¯Ã Â¤Â¾ Ã Â¤â€“Ã Â¤Â¾Ã Â¤Â¤Ã Â¤Â¾","Ã Â²Â¹Ã Â³Å Ã Â²Â¸ Ã Â²â€“Ã Â²Â¾Ã Â²Â¤Ã Â³â€ ","Ã Â´ÂªÃ ÂµÂÃ Â´Â¤Ã Â´Â¿Ã Â´Â¯ Ã Â´â€¦Ã Â´â€¢Ã ÂµÂÃ Â´â€¢Ã Âµâ€”Ã Â´Â£Ã ÂµÂÃ Â´Å¸Ã ÂµÂ","Ã Â¤Â¨Ã Â¤ÂµÃ Â¥â‚¬Ã Â¤Â¨ Ã Â¤â€“Ã Â¤Â¾Ã Â¤Â¤Ã Â¥â€¡")}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = green)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(AppLang.t("Full Name","Ã Â°ÂªÃ Â±â€šÃ Â°Â°Ã Â±ÂÃ Â°Â¤Ã Â°Â¿ Ã Â°ÂªÃ Â±â€¡Ã Â°Â°Ã Â±Â","Ã Â¤ÂªÃ Â¥â€šÃ Â¤Â°Ã Â¤Â¾ Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Â®","Ã Â²ÂªÃ Â³â€šÃ Â²Â°Ã Â³ÂÃ Â²Â£ Ã Â²Â¹Ã Â³â€ Ã Â²Â¸Ã Â²Â°Ã Â³Â","Ã Â´ÂªÃ Âµâ€šÃ ÂµÂ¼Ã Â´Â£Ã ÂµÂÃ Â´Â£ Ã Â´ÂªÃ Âµâ€¡Ã Â´Â°Ã ÂµÂ","Ã Â¤ÂªÃ Â¥â€šÃ Â¤Â°Ã Â¥ÂÃ Â¤Â£ Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Âµ")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(AppLang.t("Email","Ã Â°â€¡Ã Â°Â®Ã Â±â€ Ã Â°Â¯Ã Â°Â¿Ã Â°Â²Ã Â±Â","Ã Â¤Ë†Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â²","Ã Â²â€¡Ã Â²Â®Ã Â³â€¡Ã Â²Â²Ã Â³Â","Ã Â´â€¡Ã Â´Â®Ã Âµâ€ Ã Â´Â¯Ã Â´Â¿Ã ÂµÂ½","Ã Â¤Ë†Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â²")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(AppLang.t("Phone","Ã Â°Â«Ã Â±â€¹Ã Â°Â¨Ã Â±Â","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Â¨","Ã Â²Â«Ã Â³â€¹Ã Â²Â¨Ã Â³Â","Ã Â´Â«Ã Âµâ€¹Ã ÂµÂº","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Â¨")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text(AppLang.t("Village / City","Ã Â°Å Ã Â°Â°Ã Â±Â","Ã Â¤â€”Ã Â¤Â¾Ã Â¤â€šÃ Â¤Âµ","Ã Â²Å Ã Â²Â°Ã Â³Â","Ã Â´â€”Ã ÂµÂÃ Â´Â°Ã Â´Â¾Ã Â´Â®Ã Â´â€š","Ã Â¤â€”Ã Â¤Â¾Ã Â¤Âµ")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(AppLang.t("Password","Ã Â°ÂªÃ Â°Â¾Ã Â°Â¸Ã Â±ÂÃ¢â‚¬Å’Ã Â°ÂµÃ Â°Â°Ã Â±ÂÃ Â°Â¡Ã Â±Â","Ã Â¤ÂªÃ Â¤Â¾Ã Â¤Â¸Ã Â¤ÂµÃ Â¤Â°Ã Â¥ÂÃ Â¤Â¡","Ã Â²ÂªÃ Â²Â¾Ã Â²Â¸Ã Â³ÂÃ¢â‚¬Å’Ã Â²ÂµÃ Â²Â°Ã Â³ÂÃ Â²Â¡Ã Â³Â","Ã Â´ÂªÃ Â´Â¾Ã Â´Â¸Ã ÂµÂÃ¢â‚¬Å’Ã Â´ÂµÃ Âµâ€¡Ã Â´Â¡Ã ÂµÂ","Ã Â¤ÂªÃ Â¤Â¾Ã Â¤Â¸Ã Â¤ÂµÃ Â¤Â°Ã Â¥ÂÃ Â¤Â¡")) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        if (errorMsg.isNotEmpty()) { Text(errorMsg, color = Color.Red, fontSize = 13.sp); Spacer(modifier = Modifier.height(8.dp)) }
        Button(onClick = {
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                isLoading = true
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val db = FirebaseFirestore.getInstance()
                        val user = hashMapOf("name" to name, "phone" to phone, "village" to village, "email" to email)
                        db.collection("users").document(it.user!!.uid).set(user)
                        isLoading = false
                        navController.navigate("home") { popUpTo("signup") { inclusive = true } }
                    }
                    .addOnFailureListener { isLoading = false; errorMsg = it.message ?: "Signup failed" }
            } else { errorMsg = "Please fill all fields" }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
            Text(if (isLoading) "Creating..." else AppLang.t("Signup","Ã Â°Â¸Ã Â±Ë†Ã Â°Â¨Ã Â±ÂÃ Â°â€¦Ã Â°ÂªÃ Â±Â","Ã Â¤Â¸Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â¨Ã Â¤â€¦Ã Â¤Âª","Ã Â²Â¸Ã Â³Ë†Ã Â²Â¨Ã Â³ÂÃ¢â‚¬Å’Ã Â²â€¦Ã Â²ÂªÃ Â³Â","Ã Â´Â¸Ã ÂµË†Ã ÂµÂ»Ã Â´â€¦Ã Â´ÂªÃ ÂµÂ","Ã Â¤Â¸Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â¨Ã Â¤â€¦Ã Â¤Âª"), color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text(AppLang.t("Already have account? Login","Ã Â°â€¦Ã Â°â€¢Ã Â±Å’Ã Â°â€šÃ Â°Å¸Ã Â±Â Ã Â°â€°Ã Â°â€šÃ Â°Â¦Ã Â°Â¾? Ã Â°Â²Ã Â°Â¾Ã Â°â€”Ã Â°Â¿Ã Â°Â¨Ã Â±Â","Ã Â¤â€“Ã Â¤Â¾Ã Â¤Â¤Ã Â¤Â¾ Ã Â¤Â¹Ã Â¥Ë†? Ã Â¤Â²Ã Â¥â€°Ã Â¤â€”Ã Â¤Â¿Ã Â¤Â¨","Ã Â²â€“Ã Â²Â¾Ã Â²Â¤Ã Â³â€  Ã Â²â€¡Ã Â²Â¦Ã Â³â€ Ã Â²Â¯Ã Â³â€¡? Ã Â²Â²Ã Â²Â¾Ã Â²â€”Ã Â²Â¿Ã Â²Â¨Ã Â³Â","Ã Â´â€¦Ã Â´â€¢Ã ÂµÂÃ Â´â€¢Ã Âµâ€”Ã Â´Â£Ã ÂµÂÃ Â´Å¸Ã ÂµÂ Ã Â´â€°Ã Â´Â£Ã ÂµÂÃ Â´Å¸Ã Âµâ€¹? Ã Â´Â²Ã Âµâ€¹Ã Â´â€”Ã Â´Â¿Ã ÂµÂ»","Ã Â¤â€“Ã Â¤Â¾Ã Â¤Â¤Ã Â¥â€¡ Ã Â¤â€ Ã Â¤Â¹Ã Â¥â€¡? Ã Â¤Â²Ã Â¥â€°Ã Â¤â€”Ã Â¤Â¿Ã Â¤Â¨"), color = green)
        }
    }
}

@Composable
fun LangSelector() {
    val currentLang by AppLang.lang
    val languages = listOf(Lang.ENGLISH to "EN", Lang.TELUGU to "Ã Â°Â¤Ã Â±â€ ", Lang.HINDI to "Ã Â¤Â¹Ã Â¤Â¿", Lang.KANNADA to "Ã Â²â€¢", Lang.MALAYALAM to "Ã Â´Â®", Lang.MARATHI to "Ã Â¤Â®")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        languages.forEach { (language, label) ->
            TextButton(onClick = { AppLang.lang.value = language },
                colors = ButtonDefaults.textButtonColors(contentColor = if (currentLang == language) Color(0xFF2E7D32) else Color.Gray),
                contentPadding = PaddingValues(4.dp)) {
                Text(label, fontWeight = if (currentLang == language) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val green = Color(0xFF2E7D32)
    val auth = FirebaseAuth.getInstance()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        LangSelector()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Ã°Å¸Å’Â¾ Rythu Connect", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = green)
        Text(AppLang.t("Farmer Connect","Ã Â°Â°Ã Â±Ë†Ã Â°Â¤Ã Â±Â Ã Â°â€¢Ã Â°Â¨Ã Â±â€ Ã Â°â€¢Ã Â±ÂÃ Â°Å¸Ã Â±Â","Ã Â¤â€¢Ã Â¤Â¿Ã Â¤Â¸Ã Â¤Â¾Ã Â¤Â¨ Ã Â¤â€¢Ã Â¤Â¨Ã Â¥â€¡Ã Â¤â€¢Ã Â¥ÂÃ Â¤Å¸","Ã Â²Â°Ã Â³Ë†Ã Â²Â¤ Ã Â²â€¢Ã Â²Â¨Ã Â³â€ Ã Â²â€¢Ã Â³ÂÃ Â²Å¸Ã Â³Â","Ã Â´â€¢Ã ÂµÂ¼Ã Â´Â·Ã Â´â€¢ Ã Â´â€¢Ã Â´Â£Ã Â´â€¢Ã ÂµÂÃ Â´Â±Ã ÂµÂÃ Â´Â±Ã ÂµÂ","Ã Â¤Â¶Ã Â¥â€¡Ã Â¤Â¤Ã Â¤â€¢Ã Â¤Â°Ã Â¥â‚¬ Ã Â¤â€¢Ã Â¤Â¨Ã Â¥â€¡Ã Â¤â€¢Ã Â¥ÂÃ Â¤Å¸"), fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.farmer_img),
            contentDescription = "Farmer Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        MenuButton("Ã°Å¸Å’Â¾ ${AppLang.t("Sell Crops","Ã Â°ÂªÃ Â°â€šÃ Â°Å¸ Ã Â°â€¦Ã Â°Â®Ã Â±ÂÃ Â°Â®Ã Â°â€šÃ Â°Â¡Ã Â°Â¿","Ã Â¤Â«Ã Â¤Â¸Ã Â¤Â² Ã Â¤Â¬Ã Â¥â€¡Ã Â¤Å¡Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â¬Ã Â³â€ Ã Â²Â³Ã Â³â€  Ã Â²Â®Ã Â²Â¾Ã Â²Â°Ã Â²Â¿","Ã Â´ÂµÃ Â´Â¿Ã Â´Â³ Ã Â´ÂµÃ Â´Â¿Ã ÂµÂ½Ã Â´â€¢Ã ÂµÂÃ Â´â€¢Ã ÂµÂÃ Â´â€¢","Ã Â¤ÂªÃ Â¥â‚¬Ã Â¤â€¢ Ã Â¤ÂµÃ Â¤Â¿Ã Â¤â€¢Ã Â¤Â¾")}", green) { navController.navigate("sell_crops") }
            MenuButton(text = "Buy Crops", color = Color(0xFF1565C0)) { navController.navigate(route = "buy_crops") }
            MenuButton("Ã°Å¸â€œâ€¹ ${AppLang.t("View Crops","Ã Â°ÂªÃ Â°â€šÃ Â°Å¸Ã Â°Â² Ã Â°Å“Ã Â°Â¾Ã Â°Â¬Ã Â°Â¿Ã Â°Â¤Ã Â°Â¾","Ã Â¤Â«Ã Â¤Â¸Ã Â¤Â²Ã Â¥â€¡Ã Â¤â€š Ã Â¤Â¦Ã Â¥â€¡Ã Â¤â€“Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â¬Ã Â³â€ Ã Â²Â³Ã Â³â€ Ã Â²â€”Ã Â²Â³Ã Â²Â¨Ã Â³ÂÃ Â²Â¨Ã Â³Â Ã Â²Â¨Ã Â³â€¹Ã Â²Â¡Ã Â²Â¿","Ã Â´ÂµÃ Â´Â¿Ã Â´Â³Ã Â´â€¢Ã ÂµÂ¾ Ã Â´â€¢Ã Â´Â¾Ã Â´Â£Ã ÂµÂÃ Â´â€¢","Ã Â¤ÂªÃ Â¤Â¿Ã Â¤â€¢Ã Â¥â€¡ Ã Â¤ÂªÃ Â¤Â¹Ã Â¤Â¾")}", Color(0xFF00796B)) { navController.navigate("view_crops") }
        MenuButton("Ã°Å¸Å’Â¤Ã¯Â¸Â ${AppLang.t("Weather","Ã Â°ÂµÃ Â°Â¾Ã Â°Â¤Ã Â°Â¾Ã Â°ÂµÃ Â°Â°Ã Â°Â£Ã Â°â€š","Ã Â¤Â®Ã Â¥Å’Ã Â¤Â¸Ã Â¤Â®","Ã Â²Â¹Ã Â²ÂµÃ Â²Â¾Ã Â²Â®Ã Â²Â¾Ã Â²Â¨","Ã Â´â€¢Ã Â´Â¾Ã Â´Â²Ã Â´Â¾Ã Â´ÂµÃ Â´Â¸Ã ÂµÂÃ Â´Â¥","Ã Â¤Â¹Ã Â¤ÂµÃ Â¤Â¾Ã Â¤Â®Ã Â¤Â¾Ã Â¤Â¨")}", Color(0xFF1565C0)) { navController.navigate("weather") }
        MenuButton("Ã°Å¸â€™Â° ${AppLang.t("Market Prices","Ã Â°Â®Ã Â°Â¾Ã Â°Â°Ã Â±ÂÃ Â°â€¢Ã Â±â€ Ã Â°Å¸Ã Â±Â Ã Â°Â§Ã Â°Â°Ã Â°Â²Ã Â±Â","Ã Â¤Â¬Ã Â¤Â¾Ã Â¤Å“Ã Â¤Â¾Ã Â¤Â° Ã Â¤Â­Ã Â¤Â¾Ã Â¤Âµ","Ã Â²Â®Ã Â²Â¾Ã Â²Â°Ã Â³ÂÃ Â²â€¢Ã Â²Å¸Ã Â³ÂÃ Â²Å¸Ã Â³â€  Ã Â²Â¬Ã Â³â€ Ã Â²Â²Ã Â³â€ ","Ã Â´ÂµÃ Â´Â¿Ã Â´ÂªÃ Â´Â£Ã Â´Â¿ Ã Â´ÂµÃ Â´Â¿Ã Â´Â²","Ã Â¤Â¬Ã Â¤Â¾Ã Â¤Å“Ã Â¤Â¾Ã Â¤Â° Ã Â¤Â­Ã Â¤Â¾Ã Â¤Âµ")}", Color(0xFFE65100)) { navController.navigate("market_prices") }
        MenuButton("Ã°Å¸â€œÅ¾ ${AppLang.t("Contact","Ã Â°Â¸Ã Â°â€šÃ Â°ÂªÃ Â±ÂÃ Â°Â°Ã Â°Â¦Ã Â°Â¿Ã Â°â€šÃ Â°Å¡Ã Â°â€šÃ Â°Â¡Ã Â°Â¿","Ã Â¤Â¸Ã Â¤â€šÃ Â¤ÂªÃ Â¤Â°Ã Â¥ÂÃ Â¤â€¢ Ã Â¤â€¢Ã Â¤Â°Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â¸Ã Â²â€šÃ Â²ÂªÃ Â²Â°Ã Â³ÂÃ Â²â€¢Ã Â²Â¿Ã Â²Â¸Ã Â²Â¿","Ã Â´Â¬Ã Â´Â¨Ã ÂµÂÃ Â´Â§Ã Â´ÂªÃ ÂµÂÃ Â´ÂªÃ Âµâ€ Ã Â´Å¸Ã ÂµÂÃ Â´â€¢","Ã Â¤Â¸Ã Â¤â€šÃ Â¤ÂªÃ Â¤Â°Ã Â¥ÂÃ Â¤â€¢ Ã Â¤â€¢Ã Â¤Â°Ã Â¤Â¾")}", Color(0xFF6A1B9A)) { navController.navigate("contact") }
        MenuButton("Ã°Å¸â€˜Â¤ ${AppLang.t("My Profile","Ã Â°Â¨Ã Â°Â¾ Ã Â°ÂªÃ Â±ÂÃ Â°Â°Ã Â±Å Ã Â°Â«Ã Â±Ë†Ã Â°Â²Ã Â±Â","Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â°Ã Â¥â‚¬ Ã Â¤ÂªÃ Â¥ÂÃ Â¤Â°Ã Â¥â€¹Ã Â¤Â«Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â²","Ã Â²Â¨Ã Â²Â¨Ã Â³ÂÃ Â²Â¨ Ã Â²ÂªÃ Â³ÂÃ Â²Â°Ã Â³Å Ã Â²Â«Ã Â³Ë†Ã Â²Â²Ã Â³Â","Ã Â´Å½Ã Â´Â¨Ã ÂµÂÃ Â´Â±Ã Âµâ€  Ã Â´ÂªÃ ÂµÂÃ Â´Â°Ã ÂµÅ Ã Â´Â«Ã ÂµË†Ã ÂµÂ½","Ã Â¤Â®Ã Â¤Â¾Ã Â¤ÂÃ Â¥â‚¬ Ã Â¤ÂªÃ Â¥ÂÃ Â¤Â°Ã Â¥â€¹Ã Â¤Â«Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â²")}", Color(0xFF00838F)) { navController.navigate("profile") }
        MenuButton(
            "Ã°Å¸â€œÂ Nearby Markets",
            Color(0xFF00897B)
        ) {
            navController.navigate("nearby_markets")
        }
        MenuButton(
            "Ã¢Â­Â Feedback",
            Color(0xFFFFA000)
        ) {
            navController.navigate("feedback")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { auth.signOut(); navController.navigate("login") { popUpTo("home") { inclusive = true } } }) {
            Text(AppLang.t("Logout","Ã Â°Â²Ã Â°Â¾Ã Â°â€”Ã Â±ÂÃ Â°â€¦Ã Â°ÂµÃ Â±ÂÃ Â°Å¸Ã Â±Â","Ã Â¤Â²Ã Â¥â€°Ã Â¤â€”Ã Â¤â€ Ã Â¤â€°Ã Â¤Å¸","Ã Â²Â²Ã Â²Â¾Ã Â²â€”Ã Â³ÂÃ¢â‚¬Å’Ã Â²â€Ã Â²Å¸Ã Â³Â","Ã Â´Â²Ã Âµâ€¹Ã Â´â€”Ã Âµâ€”Ã Â´Å¸Ã ÂµÂÃ Â´Å¸Ã ÂµÂ","Ã Â¤Â²Ã Â¥â€°Ã Â¤â€”Ã Â¤â€ Ã Â¤â€°Ã Â¤Å¸"), color = Color.Red)
        }
    }
}

@Composable
fun MenuButton(text: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(56.dp)) {
        Text(text, fontSize = 14.sp, color = Color.White)
    }
}

@Composable
fun SellCropsScreen(navController: NavController) {
    var cropName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var uploadingImage by remember { mutableStateOf(false) }
    var farmerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var mandal by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            uploadingImage = true
            val ref = storage.reference.child("crops/${auth.currentUser?.uid}/${System.currentTimeMillis()}.jpg")
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        imageUrl = url.toString()
                        uploadingImage = false
                    }
                }
                .addOnFailureListener { uploadingImage = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        LangSelector()
        BackButton(navController)
        Text("Ã°Å¸Å’Â¾ ${AppLang.t("Sell Crops","Ã Â°ÂªÃ Â°â€šÃ Â°Å¸ Ã Â°â€¦Ã Â°Â®Ã Â±ÂÃ Â°Â®Ã Â°â€šÃ Â°Â¡Ã Â°Â¿","Ã Â¤Â«Ã Â¤Â¸Ã Â¤Â² Ã Â¤Â¬Ã Â¥â€¡Ã Â¤Å¡Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â¬Ã Â³â€ Ã Â²Â³Ã Â³â€  Ã Â²Â®Ã Â²Â¾Ã Â²Â°Ã Â²Â¿","Ã Â´ÂµÃ Â´Â¿Ã Â´Â³ Ã Â´ÂµÃ Â´Â¿Ã ÂµÂ½Ã Â´â€¢Ã ÂµÂÃ Â´â€¢Ã ÂµÂÃ Â´â€¢","Ã Â¤ÂªÃ Â¥â‚¬Ã Â¤â€¢ Ã Â¤ÂµÃ Â¤Â¿Ã Â¤â€¢Ã Â¤Â¾")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = cropName, onValueChange = { cropName = it },
            label = { Text(AppLang.t("Crop Name","Ã Â°ÂªÃ Â°â€šÃ Â°Å¸ Ã Â°ÂªÃ Â±â€¡Ã Â°Â°Ã Â±Â","Ã Â¤Â«Ã Â¤Â¸Ã Â¤Â² Ã Â¤â€¢Ã Â¤Â¾ Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Â®","Ã Â²Â¬Ã Â³â€ Ã Â²Â³Ã Â³â€  Ã Â²Â¹Ã Â³â€ Ã Â²Â¸Ã Â²Â°Ã Â³Â","Ã Â´ÂµÃ Â´Â¿Ã Â´Â³Ã Â´Â¯Ã ÂµÂÃ Â´Å¸Ã Âµâ€  Ã Â´ÂªÃ Âµâ€¡Ã Â´Â°Ã ÂµÂ","Ã Â¤ÂªÃ Â¤Â¿Ã Â¤â€¢Ã Â¤Â¾Ã Â¤Å¡Ã Â¥â€¡ Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Âµ")) },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = quantity, onValueChange = { quantity = it },
            label = { Text(AppLang.t("Quantity in KG","Ã Â°ÂªÃ Â°Â°Ã Â°Â¿Ã Â°Â®Ã Â°Â¾Ã Â°Â£Ã Â°â€š (Ã Â°â€¢Ã Â±â€¡Ã Â°Å“Ã Â±â‚¬)","Ã Â¤Â®Ã Â¤Â¾Ã Â¤Â¤Ã Â¥ÂÃ Â¤Â°Ã Â¤Â¾ (Ã Â¤â€¢Ã Â¤Â¿Ã Â¤Â²Ã Â¥â€¹)","Ã Â²ÂªÃ Â³ÂÃ Â²Â°Ã Â²Â®Ã Â²Â¾Ã Â²Â£ (Ã Â²â€¢Ã Â³â€ Ã Â²Å“Ã Â²Â¿)","Ã Â´â€¦Ã Â´Â³Ã Â´ÂµÃ ÂµÂ (Ã Â´â€¢Ã Â´Â¿Ã Â´Â²Ã Âµâ€¹)","Ã Â¤ÂªÃ Â¥ÂÃ Â¤Â°Ã Â¤Â®Ã Â¤Â¾Ã Â¤Â£ (Ã Â¤â€¢Ã Â¤Â¿Ã Â¤Â²Ã Â¥â€¹)")) },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = price, onValueChange = { price = it },
            label = { Text(AppLang.t("Price per KG (Ã¢â€šÂ¹)","Ã Â°â€¢Ã Â±â€¡Ã Â°Å“Ã Â±â‚¬Ã Â°â€¢Ã Â°Â¿ Ã Â°Â§Ã Â°Â° (Ã¢â€šÂ¹)","Ã Â¤ÂªÃ Â¥ÂÃ Â¤Â°Ã Â¤Â¤Ã Â¤Â¿ Ã Â¤â€¢Ã Â¤Â¿Ã Â¤Â²Ã Â¥â€¹ Ã Â¤Â®Ã Â¥â€šÃ Â¤Â²Ã Â¥ÂÃ Â¤Â¯ (Ã¢â€šÂ¹)","Ã Â²ÂªÃ Â³ÂÃ Â²Â°Ã Â²Â¤Ã Â²Â¿ Ã Â²â€¢Ã Â³â€ Ã Â²Å“Ã Â²Â¿ Ã Â²Â¬Ã Â³â€ Ã Â²Â²Ã Â³â€  (Ã¢â€šÂ¹)","Ã Â´â€¢Ã Â´Â¿Ã Â´Â²Ã Âµâ€¹Ã Â´Â¯Ã ÂµÂÃ Â´â€¢Ã ÂµÂÃ Â´â€¢Ã ÂµÂ Ã Â´ÂµÃ Â´Â¿Ã Â´Â² (Ã¢â€šÂ¹)","Ã Â¤ÂªÃ Â¥ÂÃ Â¤Â°Ã Â¤Â¤Ã Â¤Â¿ Ã Â¤â€¢Ã Â¤Â¿Ã Â¤Â²Ã Â¥â€¹ Ã Â¤Â­Ã Â¤Â¾Ã Â¤Âµ (Ã¢â€šÂ¹)")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = farmerName, onValueChange = { farmerName = it }, label = { Text("Farmer Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = mandal, onValueChange = { mandal = it }, label = { Text("Mandal") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                // Image Upload Section
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(model = imageUrl, contentDescription = "Crop Image",
                        modifier = Modifier.fillMaxWidth().height(180.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ã¢Å“â€¦ ${AppLang.t("Image Uploaded!","Ã Â°Â«Ã Â±â€¹Ã Â°Å¸Ã Â±â€¹ Ã Â°â€¦Ã Â°ÂªÃ Â±ÂÃ¢â‚¬Å’Ã Â°Â²Ã Â±â€¹Ã Â°Â¡Ã Â±Â Ã Â°â€¦Ã Â°Â¯Ã Â°Â¿Ã Â°â€šÃ Â°Â¦Ã Â°Â¿!","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Å¸Ã Â¥â€¹ Ã Â¤â€¦Ã Â¤ÂªÃ Â¤Â²Ã Â¥â€¹Ã Â¤Â¡ Ã Â¤Â¹Ã Â¥â€¹ Ã Â¤â€”Ã Â¤Â¯Ã Â¤Â¾!","Ã Â²Â«Ã Â³â€¹Ã Â²Å¸Ã Â³â€¹ Ã Â²â€¦Ã Â²ÂªÃ Â³ÂÃ¢â‚¬Å’Ã Â²Â²Ã Â³â€¹Ã Â²Â¡Ã Â³Â Ã Â²â€ Ã Â²â€”Ã Â²Â¿Ã Â²Â¦Ã Â³â€ !","Ã Â´Â«Ã Âµâ€¹Ã Â´Å¸Ã ÂµÂÃ Â´Å¸Ã Âµâ€¹ Ã Â´â€¦Ã Â´ÂªÃ ÂµÂÃ¢â‚¬Å’Ã Â´Â²Ã Âµâ€¹Ã Â´Â¡Ã ÂµÂ Ã Â´â€ Ã Â´Â¯Ã Â´Â¿!","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Å¸Ã Â¥â€¹ Ã Â¤â€¦Ã Â¤ÂªÃ Â¤Â²Ã Â¥â€¹Ã Â¤Â¡ Ã Â¤ÂÃ Â¤Â¾Ã Â¤Â²Ã Â¤Â¾!")}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                } else if (uploadingImage) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                    Text(AppLang.t("Uploading...","Ã Â°â€¦Ã Â°ÂªÃ Â±ÂÃ¢â‚¬Å’Ã Â°Â²Ã Â±â€¹Ã Â°Â¡Ã Â±Â Ã Â°â€¦Ã Â°ÂµÃ Â±ÂÃ Â°Â¤Ã Â±â€¹Ã Â°â€šÃ Â°Â¦Ã Â°Â¿...","Ã Â¤â€¦Ã Â¤ÂªÃ Â¤Â²Ã Â¥â€¹Ã Â¤Â¡ Ã Â¤Â¹Ã Â¥â€¹ Ã Â¤Â°Ã Â¤Â¹Ã Â¤Â¾ Ã Â¤Â¹Ã Â¥Ë†...","Ã Â²â€¦Ã Â²ÂªÃ Â³ÂÃ¢â‚¬Å’Ã Â²Â²Ã Â³â€¹Ã Â²Â¡Ã Â³Â Ã Â²â€ Ã Â²â€”Ã Â³ÂÃ Â²Â¤Ã Â³ÂÃ Â²Â¤Ã Â²Â¿Ã Â²Â¦Ã Â³â€ ...","Ã Â´â€¦Ã Â´ÂªÃ ÂµÂÃ¢â‚¬Å’Ã Â´Â²Ã Âµâ€¹Ã Â´Â¡Ã ÂµÂ Ã Â´Å¡Ã Âµâ€ Ã Â´Â¯Ã ÂµÂÃ Â´Â¯Ã ÂµÂÃ Â´Â¨Ã ÂµÂÃ Â´Â¨Ã ÂµÂ...","Ã Â¤â€¦Ã Â¤ÂªÃ Â¤Â²Ã Â¥â€¹Ã Â¤Â¡ Ã Â¤Â¹Ã Â¥â€¹Ã Â¤Â¤ Ã Â¤â€ Ã Â¤Â¹Ã Â¥â€¡..."), color = Color.Gray)
                } else {
                    Text(AppLang.t("Ã°Å¸â€œÂ¸ Add Crop Photo","Ã°Å¸â€œÂ¸ Ã Â°ÂªÃ Â°â€šÃ Â°Å¸ Ã Â°Â«Ã Â±â€¹Ã Â°Å¸Ã Â±â€¹ Ã Â°Å“Ã Â±â€¹Ã Â°Â¡Ã Â°Â¿Ã Â°â€šÃ Â°Å¡Ã Â°â€šÃ Â°Â¡Ã Â°Â¿","Ã°Å¸â€œÂ¸ Ã Â¤Â«Ã Â¤Â¸Ã Â¤Â² Ã Â¤Â«Ã Â¥â€¹Ã Â¤Å¸Ã Â¥â€¹ Ã Â¤Å“Ã Â¥â€¹Ã Â¤Â¡Ã Â¤Â¼Ã Â¥â€¡Ã Â¤â€š","Ã°Å¸â€œÂ¸ Ã Â²Â¬Ã Â³â€ Ã Â²Â³Ã Â³â€  Ã Â²Â«Ã Â³â€¹Ã Â²Å¸Ã Â³â€¹ Ã Â²Â¸Ã Â³â€¡Ã Â²Â°Ã Â²Â¿Ã Â²Â¸Ã Â²Â¿","Ã°Å¸â€œÂ¸ Ã Â´ÂµÃ Â´Â¿Ã Â´Â³ Ã Â´Â«Ã Âµâ€¹Ã Â´Å¸Ã ÂµÂÃ Â´Å¸Ã Âµâ€¹ Ã Â´Å¡Ã Âµâ€¡Ã ÂµÂ¼Ã Â´â€¢Ã ÂµÂÃ Â´â€¢Ã ÂµÂÃ Â´â€¢","Ã°Å¸â€œÂ¸ Ã Â¤ÂªÃ Â¥â‚¬Ã Â¤â€¢ Ã Â¤Â«Ã Â¥â€¹Ã Â¤Å¸Ã Â¥â€¹ Ã Â¤Å“Ã Â¥â€¹Ã Â¤Â¡Ã Â¤Â¾"), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { launcher.launch("image/*") }) {
                    Text(if (imageUrl.isNotEmpty()) AppLang.t("Change Photo","Ã Â°Â«Ã Â±â€¹Ã Â°Å¸Ã Â±â€¹ Ã Â°Â®Ã Â°Â¾Ã Â°Â°Ã Â±ÂÃ Â°Å¡Ã Â±Â","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Å¸Ã Â¥â€¹ Ã Â¤Â¬Ã Â¤Â¦Ã Â¤Â²Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â«Ã Â³â€¹Ã Â²Å¸Ã Â³â€¹ Ã Â²Â¬Ã Â²Â¦Ã Â²Â²Ã Â²Â¿Ã Â²Â¸Ã Â²Â¿","Ã Â´Â«Ã Âµâ€¹Ã Â´Å¸Ã ÂµÂÃ Â´Å¸Ã Âµâ€¹ Ã Â´Â®Ã Â´Â¾Ã Â´Â±Ã ÂµÂÃ Â´Â±Ã ÂµÂÃ Â´â€¢","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Å¸Ã Â¥â€¹ Ã Â¤Â¬Ã Â¤Â¦Ã Â¤Â²Ã Â¤Â¾") else AppLang.t("Choose Photo","Ã Â°Â«Ã Â±â€¹Ã Â°Å¸Ã Â±â€¹ Ã Â°Å½Ã Â°â€šÃ Â°Å¡Ã Â±ÂÃ Â°â€¢Ã Â±â€¹Ã Â°â€šÃ Â°Â¡Ã Â°Â¿","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Å¸Ã Â¥â€¹ Ã Â¤Å¡Ã Â¥ÂÃ Â¤Â¨Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â«Ã Â³â€¹Ã Â²Å¸Ã Â³â€¹ Ã Â²â€ Ã Â²Â¯Ã Â³ÂÃ Â²â€¢Ã Â³â€  Ã Â²Â®Ã Â²Â¾Ã Â²Â¡Ã Â²Â¿","Ã Â´Â«Ã Âµâ€¹Ã Â´Å¸Ã ÂµÂÃ Â´Å¸Ã Âµâ€¹ Ã Â´Â¤Ã Â´Â¿Ã Â´Â°Ã Â´Å¾Ã ÂµÂÃ Â´Å¾Ã Âµâ€ Ã Â´Å¸Ã ÂµÂÃ Â´â€¢Ã ÂµÂÃ Â´â€¢Ã ÂµÂÃ Â´â€¢","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Å¸Ã Â¥â€¹ Ã Â¤Â¨Ã Â¤Â¿Ã Â¤ÂµÃ Â¤Â¡Ã Â¤Â¾"))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (cropName.isNotEmpty() && quantity.isNotEmpty()) {
                    isLoading = true
                    val crop = hashMapOf(
                        "cropName" to cropName, "quantity" to quantity, "price" to price, "farmerName" to farmerName, "phoneNumber" to phoneNumber, "village" to village, "mandal" to mandal, "district" to district, "state" to state,
                        "imageUrl" to imageUrl, "uid" to (auth.currentUser?.uid ?: ""),
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("crops").add(crop)
                        .addOnSuccessListener { submitted = true; isLoading = false }
                        .addOnFailureListener { isLoading = false }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text(if (isLoading) "Saving..." else AppLang.t("Submit","Ã Â°Â¸Ã Â°Â®Ã Â°Â°Ã Â±ÂÃ Â°ÂªÃ Â°Â¿Ã Â°â€šÃ Â°Å¡Ã Â±Â","Ã Â¤Å“Ã Â¤Â®Ã Â¤Â¾ Ã Â¤â€¢Ã Â¤Â°Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â¸Ã Â²Â²Ã Â³ÂÃ Â²Â²Ã Â²Â¿Ã Â²Â¸Ã Â³Â","Ã Â´Â¸Ã Â´Â®Ã ÂµÂ¼Ã Â´ÂªÃ ÂµÂÃ Â´ÂªÃ Â´Â¿Ã Â´â€¢Ã ÂµÂÃ Â´â€¢Ã ÂµÂÃ Â´â€¢","Ã Â¤Â¸Ã Â¤Â¾Ã Â¤Â¦Ã Â¤Â° Ã Â¤â€¢Ã Â¤Â°Ã Â¤Â¾"), color = Color.White)
        }

        if (submitted) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ã¢Å“â€¦ ${AppLang.t("Saved!","Ã Â°Â¸Ã Â±â€¡Ã Â°ÂµÃ Â±Â Ã Â°â€¦Ã Â°Â¯Ã Â°Â¿Ã Â°â€šÃ Â°Â¦Ã Â°Â¿!","Ã Â¤Â¸Ã Â¤Â¹Ã Â¥â€¡Ã Â¤Å“Ã Â¤Â¾ Ã Â¤â€”Ã Â¤Â¯Ã Â¤Â¾!","Ã Â²â€°Ã Â²Â³Ã Â²Â¿Ã Â²Â¸Ã Â²Â²Ã Â²Â¾Ã Â²â€”Ã Â²Â¿Ã Â²Â¦Ã Â³â€ !","Ã Â´Â¸Ã Âµâ€šÃ Â´â€¢Ã Â§ÂÃ Â´Â·Ã Â´Â¿Ã Â´Å¡Ã ÂµÂÃ Â´Å¡Ã ÂµÂ!","Ã Â¤Å“Ã Â¤Â¤Ã Â¤Â¨ Ã Â¤â€¢Ã Â¥â€¡Ã Â¤Â²Ã Â¥â€¡!")}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text("Crop: $cropName"); Text("Quantity: $quantity KG")
                    if (price.isNotEmpty()) Text("Price: Ã¢â€šÂ¹$price/KG")
                }
            }
        }
    }
}

@Composable
fun BuyCropsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var crops by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = Unit) {
        db.collection("crops").get()
            .addOnSuccessListener { result -> crops = result.documents.mapNotNull { it.data }; isLoading = false }
            .addOnFailureListener { isLoading = false }
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        BackButton(navController)
        Text("Buy Crops", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) { CircularProgressIndicator() }
        else {
            crops.forEach { crop ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(crop["cropName"]?.toString() ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Quantity: ${crop["quantity"]} KG", color = Color.Gray)
                        Text("Price: ${crop["price"]}/KG", color = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Farmer: ${crop["farmerName"]?.toString() ?: ""}", fontWeight = FontWeight.Bold)
                        Text("Phone: ${crop["phoneNumber"]?.toString() ?: ""}", color = Color(0xFF1565C0))
                        Text("Village: ${crop["village"]?.toString() ?: ""}")
                        Text("Mandal: ${crop["mandal"]?.toString() ?: ""}")
                        Text("District: ${crop["district"]?.toString() ?: ""}")
                        Text("State: ${crop["state"]?.toString() ?: ""}")
                    }
                }
            }
        }
    }
}

@Composable
fun ViewCropsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var crops by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        db.collection("crops").get()
            .addOnSuccessListener { result -> crops = result.documents.mapNotNull { it.data }; isLoading = false }
            .addOnFailureListener { isLoading = false }
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LangSelector(); BackButton(navController)
        Text("Ã°Å¸â€œâ€¹ ${AppLang.t("Listed Crops","Ã Â°ÂªÃ Â°â€šÃ Â°Å¸Ã Â°Â² Ã Â°Å“Ã Â°Â¾Ã Â°Â¬Ã Â°Â¿Ã Â°Â¤Ã Â°Â¾","Ã Â¤Â«Ã Â¤Â¸Ã Â¤Â²Ã Â¥â€¹Ã Â¤â€š Ã Â¤â€¢Ã Â¥â‚¬ Ã Â¤Â¸Ã Â¥â€šÃ Â¤Å¡Ã Â¥â‚¬","Ã Â²Â¬Ã Â³â€ Ã Â²Â³Ã Â³â€ Ã Â²â€”Ã Â²Â³ Ã Â²ÂªÃ Â²Å¸Ã Â³ÂÃ Â²Å¸Ã Â²Â¿","Ã Â´ÂµÃ Â´Â¿Ã Â´Â³Ã Â´â€¢Ã Â´Â³Ã ÂµÂÃ Â´Å¸Ã Âµâ€  Ã Â´ÂªÃ Â´Å¸Ã ÂµÂÃ Â´Å¸Ã Â´Â¿Ã Â´â€¢","Ã Â¤ÂªÃ Â¤Â¿Ã Â¤â€¢Ã Â¤Â¾Ã Â¤â€šÃ Â¤Å¡Ã Â¥â‚¬ Ã Â¤Â¯Ã Â¤Â¾Ã Â¤Â¦Ã Â¥â‚¬")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        else if (crops.isEmpty()) Text("No crops listed yet!", color = Color.Gray)
        else LazyColumn {
            items(crops) { crop ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val imgUrl = crop["imageUrl"]?.toString() ?: ""
                        if (imgUrl.isNotEmpty()) {
                            AsyncImage(model = imgUrl, contentDescription = "Crop",
                                modifier = Modifier.fillMaxWidth().height(150.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(crop["cropName"]?.toString() ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${crop["quantity"]} KG", color = Color(0xFF00796B), fontWeight = FontWeight.Bold)
                        }
                        val price = crop["price"]?.toString() ?: ""
                        if (price.isNotEmpty()) Text("Ã¢â€šÂ¹$price/KG", color = Color(0xFFE65100), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LangSelector(); BackButton(navController)
        Text("Ã°Å¸Å’Â¤Ã¯Â¸Â ${AppLang.t("Weather","Ã Â°ÂµÃ Â°Â¾Ã Â°Â¤Ã Â°Â¾Ã Â°ÂµÃ Â°Â°Ã Â°Â£Ã Â°â€š","Ã Â¤Â®Ã Â¥Å’Ã Â¤Â¸Ã Â¤Â®","Ã Â²Â¹Ã Â²ÂµÃ Â²Â¾Ã Â²Â®Ã Â²Â¾Ã Â²Â¨","Ã Â´â€¢Ã Â´Â¾Ã Â´Â²Ã Â´Â¾Ã Â´ÂµÃ Â´Â¸Ã ÂµÂÃ Â´Â¥","Ã Â¤Â¹Ã Â¤ÂµÃ Â¤Â¾Ã Â¤Â®Ã Â¤Â¾Ã Â¤Â¨")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(20.dp))
        WeatherCard("Hyderabad", "32Ã‚Â°C", AppLang.t("Sunny","Ã Â°Å½Ã Â°â€šÃ Â°Â¡","Ã Â¤Â§Ã Â¥â€šÃ Â¤Âª","Ã Â²Â¬Ã Â²Â¿Ã Â²Â¸Ã Â²Â¿Ã Â²Â²Ã Â³Â","Ã Â´ÂµÃ Âµâ€ Ã Â´Â¯Ã Â´Â¿Ã ÂµÂ½","Ã Â¤Å Ã Â¤Â¨"))
        WeatherCard("Warangal", "29Ã‚Â°C", AppLang.t("Cloudy","Ã Â°Â®Ã Â±â€¡Ã Â°ËœÃ Â°Â¾Ã Â°ÂµÃ Â±Æ’Ã Â°Â¤Ã Â°â€š","Ã Â¤Â¬Ã Â¤Â¾Ã Â¤Â¦Ã Â¤Â²","Ã Â²Â®Ã Â³â€¹Ã Â²Â¡","Ã Â´Â®Ã Âµâ€¡Ã Â´ËœÃ Â´â€š","Ã Â¤Â¢Ã Â¤â€”Ã Â¤Â¾Ã Â¤Â³"))
        WeatherCard("Vijayawada", "34Ã‚Â°C", AppLang.t("Hot","Ã Â°ÂµÃ Â±â€¡Ã Â°Â¡Ã Â°Â¿","Ã Â¤â€”Ã Â¤Â°Ã Â¥ÂÃ Â¤Â®","Ã Â²Â¬Ã Â²Â¿Ã Â²Â¸Ã Â²Â¿","Ã Â´Å¡Ã Âµâ€šÃ Â´Å¸Ã ÂµÂ","Ã Â¤â€°Ã Â¤Â·Ã Â¥ÂÃ Â¤Â£"))
    }
}

@Composable
fun WeatherCard(city: String, temp: String, condition: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text(city, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(condition, color = Color.Gray) }
            Text(temp, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        }
    }
}

@Composable
fun MarketPricesScreen(navController: NavController) {
    val prices = listOf(
        Pair("Rice / Ã Â°ÂµÃ Â°Â°Ã Â°Â¿ / Ã Â¤Å¡Ã Â¤Â¾Ã Â¤ÂµÃ Â¤Â²", "Ã¢â€šÂ¹2200/q"), Pair("Wheat / Ã Â°â€”Ã Â±â€¹Ã Â°Â§Ã Â±ÂÃ Â°Â® / Ã Â¤â€”Ã Â¥â€¡Ã Â¤Â¹Ã Â¥â€šÃ Â¤â€š", "Ã¢â€šÂ¹2015/q"),
        Pair("Cotton / Ã Â°ÂªÃ Â°Â¤Ã Â±ÂÃ Â°Â¤Ã Â°Â¿ / Ã Â¤â€¢Ã Â¤ÂªÃ Â¤Â¾Ã Â¤Â¸", "Ã¢â€šÂ¹6500/q"), Pair("Maize / Ã Â°Â®Ã Â±Å Ã Â°â€¢Ã Â±ÂÃ Â°â€¢Ã Â°Å“Ã Â±Å Ã Â°Â¨Ã Â±ÂÃ Â°Â¨ / Ã Â¤Â®Ã Â¤â€¢Ã Â¥ÂÃ Â¤â€¢Ã Â¤Â¾", "Ã¢â€šÂ¹1850/q"),
        Pair("Turmeric / Ã Â°ÂªÃ Â°Â¸Ã Â±ÂÃ Â°ÂªÃ Â±Â / Ã Â¤Â¹Ã Â¤Â²Ã Â¥ÂÃ Â¤Â¦Ã Â¥â‚¬", "Ã¢â€šÂ¹9200/q")
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LangSelector(); BackButton(navController)
        Text("Ã°Å¸â€™Â° ${AppLang.t("Market Prices","Ã Â°Â®Ã Â°Â¾Ã Â°Â°Ã Â±ÂÃ Â°â€¢Ã Â±â€ Ã Â°Å¸Ã Â±Â Ã Â°Â§Ã Â°Â°Ã Â°Â²Ã Â±Â","Ã Â¤Â¬Ã Â¤Â¾Ã Â¤Å“Ã Â¤Â¾Ã Â¤Â° Ã Â¤Â­Ã Â¤Â¾Ã Â¤Âµ","Ã Â²Â®Ã Â²Â¾Ã Â²Â°Ã Â³ÂÃ Â²â€¢Ã Â²Å¸Ã Â³ÂÃ Â²Å¸Ã Â³â€  Ã Â²Â¬Ã Â³â€ Ã Â²Â²Ã Â³â€ ","Ã Â´ÂµÃ Â´Â¿Ã Â´ÂªÃ Â´Â£Ã Â´Â¿ Ã Â´ÂµÃ Â´Â¿Ã Â´Â²","Ã Â¤Â¬Ã Â¤Â¾Ã Â¤Å“Ã Â¤Â¾Ã Â¤Â° Ã Â¤Â­Ã Â¤Â¾Ã Â¸Â§")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
        Spacer(modifier = Modifier.height(20.dp))
        prices.forEach { (crop, price) ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(crop, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(price, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ContactScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LangSelector(); BackButton(navController)
        Text("Ã°Å¸â€œÅ¾ ${AppLang.t("Contact","Ã Â°Â¸Ã Â°â€šÃ Â°ÂªÃ Â±ÂÃ Â°Â°Ã Â°Â¦Ã Â°Â¿Ã Â°â€šÃ Â°Å¡Ã Â°â€šÃ Â°Â¡Ã Â°Â¿","Ã Â¤Â¸Ã Â¤â€šÃ Â¤ÂªÃ Â¤Â°Ã Â¥ÂÃ Â¤â€¢ Ã Â¤â€¢Ã Â¤Â°Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â¸Ã Â²â€šÃ Â²ÂªÃ Â²Â°Ã Â³ÂÃ Â²â€¢Ã Â²Â¿Ã Â²Â¸Ã Â²Â¿","Ã Â´Â¬Ã Â´Â¨Ã ÂµÂÃ Â´Â§Ã Â´ÂªÃ ÂµÂÃ Â´ÂªÃ Âµâ€ Ã Â´Å¸Ã ÂµÂÃ Â´â€¢","Ã Â¤Â¸Ã Â¤â€šÃ Â¤ÂªÃ Â¤Â°Ã Â¥ÂÃ Â¤â€¢ Ã Â¤â€¢Ã Â¤Â°Ã Â¤Â¾")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
        Spacer(modifier = Modifier.height(20.dp))
        ContactCard(AppLang.t("Helpline","Ã Â°Â¹Ã Â±â€ Ã Â°Â²Ã Â±ÂÃ Â°ÂªÃ Â±ÂÃ¢â‚¬Å’Ã Â°Â²Ã Â±Ë†Ã Â°Â¨Ã Â±Â","Ã Â¤Â¹Ã Â¥â€¡Ã Â¤Â²Ã Â¥ÂÃ Â¤ÂªÃ Â¤Â²Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â¨","Ã Â²Â¸Ã Â²Â¹Ã Â²Â¾Ã Â²Â¯Ã Â²ÂµÃ Â²Â¾Ã Â²Â£Ã Â²Â¿","Ã Â´Â¹Ã Âµâ€ Ã ÂµÂ½Ã Â´ÂªÃ ÂµÂÃ¢â‚¬Å’Ã Â´Â²Ã ÂµË†Ã ÂµÂ»","Ã Â¤Â®Ã Â¤Â¦Ã Â¤Â¤ Ã Â¤Â°Ã Â¥â€¡Ã Â¤Â·Ã Â¤Â¾"), "1800-180-1551")
        ContactCard(AppLang.t("Email","Ã Â°â€¡Ã Â°Â®Ã Â±â€ Ã Â°Â¯Ã Â°Â¿Ã Â°Â²Ã Â±Â","Ã Â¤Ë†Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â²","Ã Â²â€¡Ã Â²Â®Ã Â³â€¡Ã Â²Â²Ã Â³Â","Ã Â´â€¡Ã Â´Â®Ã Âµâ€ Ã Â´Â¯Ã Â´Â¿Ã ÂµÂ½","Ã Â¤Ë†Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â²"), "rythuconnect@ap.gov.in")
        ContactCard(AppLang.t("Website","Ã Â°ÂµÃ Â±â€ Ã Â°Â¬Ã Â±ÂÃ¢â‚¬Å’Ã Â°Â¸Ã Â±Ë†Ã Â°Å¸Ã Â±Â","Ã Â¤ÂµÃ Â¥â€¡Ã Â¤Â¬Ã Â¤Â¸Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Å¸","Ã Â²ÂµÃ Â³â€ Ã Â²Â¬Ã Â³ÂÃ¢â‚¬Å’Ã Â²Â¸Ã Â³Ë†Ã Â²Å¸Ã Â³Â","Ã Â´ÂµÃ Âµâ€ Ã Â´Â¬Ã ÂµÂÃ¢â‚¬Å’Ã Â´Â¸Ã ÂµË†Ã Â´Â±Ã ÂµÂÃ Â´Â±Ã ÂµÂ","Ã Â¤ÂµÃ Â¥â€¡Ã Â¤Â¬Ã Â¤Â¸Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Å¸"), "www.rythuconnect.ap.gov.in")
        ContactCard(AppLang.t("Office","Ã Â°â€¢Ã Â°Â¾Ã Â°Â°Ã Â±ÂÃ Â°Â¯Ã Â°Â¾Ã Â°Â²Ã Â°Â¯Ã Â°â€š","Ã Â¤â€¢Ã Â¤Â¾Ã Â¤Â°Ã Â¥ÂÃ Â¤Â¯Ã Â¤Â¾Ã Â¤Â²Ã Â¤Â¯","Ã Â²â€¢Ã Â²Å¡Ã Â³â€¡Ã Â²Â°Ã Â²Â¿","Ã Â´â€œÃ Â´Â«Ã Â±â‚¬Ã Â´Â¸Ã ÂµÂ","Ã Â¤â€¢Ã Â¤Â¾Ã Â¤Â°Ã Â¥ÂÃ Â¤Â¯Ã Â¤Â¾Ã Â¤Â²Ã Â¤Â¯"), "Amaravati, Andhra Pradesh")
    }
}

@Composable
fun ContactCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
            Text(value, fontSize = 16.sp)
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid ?: ""
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var successMsg by remember { mutableStateOf("") }
    val green = Color(0xFF2E7D32)
    LaunchedEffect(Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                name = doc.getString("name") ?: ""; phone = doc.getString("phone") ?: ""
                village = doc.getString("village") ?: ""; email = doc.getString("email") ?: auth.currentUser?.email ?: ""
                isLoading = false
            }.addOnFailureListener { isLoading = false }
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LangSelector(); BackButton(navController)
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(90.dp).background(green, CircleShape), contentAlignment = Alignment.Center) {
                    Text(name.firstOrNull()?.uppercase() ?: "R", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = green)
                Text(email, fontSize = 13.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(AppLang.t("My Details","Ã Â°Â¨Ã Â°Â¾ Ã Â°ÂµÃ Â°Â¿Ã Â°ÂµÃ Â°Â°Ã Â°Â¾Ã Â°Â²Ã Â±Â","Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â°Ã Â¥â‚¬ Ã Â¤Å“Ã Â¤Â¾Ã Â¤Â¨Ã Â¤â€¢Ã Â¤Â¾Ã Â¤Â°Ã Â¥â‚¬","Ã Â²Â¨Ã Â²Â¨Ã Â³ÂÃ Â²Â¨ Ã Â²ÂµÃ Â²Â¿Ã Â²ÂµÃ Â²Â°","Ã Â´Å½Ã Â´Â¨Ã ÂµÂÃ Â´Â±Ã Âµâ€  Ã Â´ÂµÃ Â´Â¿Ã Â´ÂµÃ Â´Â°Ã Â´â„¢Ã ÂµÂÃ Â´â„¢Ã ÂµÂ¾","Ã Â¤Â®Ã Â¤Â¾Ã Â¤ÂÃ Â¥â‚¬ Ã Â¤Â®Ã Â¤Â¾Ã Â¤Â¹Ã Â¤Â¿Ã Â¤Â¤Ã Â¥â‚¬"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = green)
            Spacer(modifier = Modifier.height(12.dp))
            if (isEditing) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(AppLang.t("Full Name","Ã Â°ÂªÃ Â±â€šÃ Â°Â°Ã Â±ÂÃ Â°Â¤Ã Â°Â¿ Ã Â°ÂªÃ Â±â€¡Ã Â°Â°Ã Â±Â","Ã Â¤ÂªÃ Â¥â€šÃ Â¤Â°Ã Â¤Â¾ Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Â®","Ã Â²ÂªÃ Â³â€šÃ Â²Â°Ã Â³ÂÃ Â²Â£ Ã Â²Â¹Ã Â³â€ Ã Â²Â¸Ã Â²Â°Ã Â³Â","Ã Â´ÂªÃ Âµâ€šÃ ÂµÂ¼Ã Â´Â£Ã ÂµÂÃ Â´Â£ Ã Â´ÂªÃ Âµâ€¡Ã Â´Â°Ã ÂµÂ","Ã Â¤ÂªÃ Â¥â€šÃ Â¤Â°Ã Â¥ÂÃ Â¤Â£ Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Âµ")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(AppLang.t("Phone","Ã Â°Â«Ã Â±â€¹Ã Â°Â¨Ã Â±Â","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Â¨","Ã Â²Â«Ã Â³â€¹Ã Â²Â¨Ã Â³Â","Ã Â´Â«Ã Âµâ€¹Ã ÂµÂº","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Â¨")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text(AppLang.t("Village / City","Ã Â°Å Ã Â°Â°Ã Â±Â","Ã Â¤â€”Ã Â¤Â¾Ã Â¤â€šÃ Â¤Âµ","Ã Â²Å Ã Â²Â°Ã Â³Â","Ã Â´â€”Ã ÂµÂÃ Â´Â°Ã Â´Â¾Ã Â´Â®Ã Â´â€š","Ã Â¤â€”Ã Â¤Â¾Ã Â¤Âµ")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        isSaving = true
                        val updates = hashMapOf<String, Any>("name" to name, "phone" to phone, "village" to village)
                        db.collection("users").document(uid).update(updates)
                            .addOnSuccessListener { isSaving = false; isEditing = false; successMsg = "Ã¢Å“â€¦ Saved!" }
                            .addOnFailureListener { isSaving = false }
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = green)) {
                        Text(if (isSaving) "Saving..." else AppLang.t("Save","Ã Â°Â¸Ã Â±â€¡Ã Â°ÂµÃ Â±Â","Ã Â¤Â¸Ã Â¤Â¹Ã Â¥â€¡Ã Â¤Å“Ã Â¥â€¡Ã Â¤â€š","Ã Â²â€°Ã Â²Â³Ã Â²Â¿Ã Â²Â¸Ã Â³Â","Ã Â´Â¸Ã Âµâ€¡Ã Â´ÂµÃ ÂµÂ Ã Â´Å¡Ã Âµâ€ Ã Â´Â¯Ã ÂµÂÃ Â´Â¯Ã Âµâ€š","Ã Â¤Å“Ã Â¤Â¤Ã Â¤Â¨ Ã Â¤â€¢Ã Â¤Â°Ã Â¤Â¾"), color = Color.White)
                    }
                    OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                        Text(AppLang.t("Cancel","Ã Â°Â°Ã Â°Â¦Ã Â±ÂÃ Â°Â¦Ã Â±Â","Ã Â¤Â°Ã Â¤Â¦Ã Â¥ÂÃ Â¤Â¦ Ã Â¤â€¢Ã Â¤Â°Ã Â¥â€¡Ã Â¤â€š","Ã Â²Â°Ã Â²Â¦Ã Â³ÂÃ Â²Â¦Ã Â³ÂÃ Â²Â®Ã Â²Â¾Ã Â²Â¡Ã Â³Â","Ã Â´Â±Ã Â´Â¦Ã ÂµÂÃ Â´Â¦Ã Â´Â¾Ã Â´â€¢Ã ÂµÂÃ Â´â€¢Ã ÂµÂÃ Â´â€¢","Ã Â¤Â°Ã Â¤Â¦Ã Â¥ÂÃ Â¤Â¦ Ã Â¤â€¢Ã Â¤Â°Ã Â¤Â¾"))
                    }
                }
            } else {
                ProfileInfoRow("Ã°Å¸â€˜Â¤ ${AppLang.t("Name","Ã Â°ÂªÃ Â±â€¡Ã Â°Â°Ã Â±Â","Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Â®","Ã Â²Â¹Ã Â³â€ Ã Â²Â¸Ã Â²Â°Ã Â³Â","Ã Â´ÂªÃ Âµâ€¡Ã Â´Â°Ã ÂµÂ","Ã Â¤Â¨Ã Â¤Â¾Ã Â¤Âµ")}", name)
                ProfileInfoRow("Ã°Å¸â€œÂ± ${AppLang.t("Phone","Ã Â°Â«Ã Â±â€¹Ã Â°Â¨Ã Â±Â","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Â¨","Ã Â²Â«Ã Â³â€¹Ã Â²Â¨Ã Â³Â","Ã Â´Â«Ã Âµâ€¹Ã ÂµÂº","Ã Â¤Â«Ã Â¥â€¹Ã Â¤Â¨")}", phone)
                ProfileInfoRow("Ã°Å¸ÂËœÃ¯Â¸Â ${AppLang.t("Village","Ã Â°Å Ã Â°Â°Ã Â±Â","Ã Â¤â€”Ã Â¤Â¾Ã Â¤â€šÃ Â¤Âµ","Ã Â²Å Ã Â²Â°Ã Â±Â","Ã Â´â€”Ã ÂµÂÃ Â°Â°Ã Â´Â¾Ã Â°Â®Ã Â´â€š","Ã Â¤â€”Ã Â¤Â¾Ã Â¤Âµ")}", village)
                ProfileInfoRow("Ã°Å¸â€œÂ§ ${AppLang.t("Email","Ã Â°â€¡Ã Â°Â®Ã Â±â€ Ã Â°Â¯Ã Â°Â¿Ã Â°Â²Ã Â±Â","Ã Â¤Ë†Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â²","Ã Â²â€¡Ã Â²Â®Ã Â³â€¡Ã Â²Â²Ã Â³Â","Ã Â´â€¡Ã Â´Â®Ã Â¯â€ Ã Â´Â¯Ã Â´Â¿Ã ÂµÂ½","Ã Â¤Ë†Ã Â¤Â®Ã Â¥â€¡Ã Â¤Â²")}", email)
                Spacer(modifier = Modifier.height(16.dp))
                if (successMsg.isNotEmpty()) { Text(successMsg, color = green, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp)) }
                Button(onClick = { isEditing = true; successMsg = "" }, modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F))) {
                    Text(AppLang.t("Ã¢Å“ÂÃ¯Â¸Â Edit Profile","Ã¢Å“ÂÃ¯Â¸Â Ã Â°ÂªÃ Â±ÂÃ Â°Â°Ã Â±Å Ã Â°Â«Ã Â±Ë†Ã Â°Â²Ã Â±Â Ã Â°Â®Ã Â°Â¾Ã Â°Â°Ã Â±ÂÃ Â°Å¡Ã Â±Â","Ã¢Å“ÂÃ¯Â¸Â Ã Â¤ÂªÃ Â¥ÂÃ Â¤Â°Ã Â¥â€¹Ã Â¤Â«Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â² Ã Â¤Â¸Ã Â¤â€šÃ Â¤ÂªÃ Â¤Â¾Ã Â¤Â¦Ã Â¤Â¿Ã Â¤Â¤ Ã Â¤â€¢Ã Â¤Â°Ã Â¥â€¡Ã Â¤â€š","Ã¢Å“ÂÃ¯Â¸Â Ã Â²ÂªÃ Â³ÂÃ Â²Â°Ã Â³Å Ã Â²Â«Ã Â³Ë†Ã Â²Â²Ã Â³Â Ã Â²Â¸Ã Â²â€šÃ Â²ÂªÃ Â²Â¾Ã Â²Â¦Ã Â²Â¿Ã Â²Â¸Ã Â²Â¿","Ã¢Å“ÂÃ¯Â¸Â Ã Â´ÂªÃ ÂµÂÃ Â´Â°Ã ÂµÅ Ã Â´Â«Ã ÂµË†Ã ÂµÂ½ Ã Â´Å½Ã Â´Â¡Ã Â´Â¿Ã Â´Â±Ã ÂµÂÃ Â´Â±Ã ÂµÂ Ã Â´Å¡Ã Âµâ€ Ã Â´Â¯Ã ÂµÂÃ Â´Â¯Ã Âµâ€š","Ã¢Å“ÂÃ¯Â¸Â Ã Â¤ÂªÃ Â¥ÂÃ Â¤Â°Ã Â¥â€¹Ã Â¤Â«Ã Â¤Â¾Ã Â¤â€¡Ã Â¤Â² Ã Â¤Â¸Ã Â¤â€šÃ Â¤ÂªÃ Â¤Â¾Ã Â¤Â¦Ã Â¤Â¿Ã Â¤Â¤ Ã Â¤â€¢Ã Â¤Â°Ã Â¤Â¾"), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF00695C))
            Text(value.ifEmpty { "-" }, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun BackButton(navController: NavController) {
    TextButton(onClick = { navController.popBackStack() }) {
        Text(AppLang.t("Back","Ã Â°ÂµÃ Â±â€ Ã Â°Â¨Ã Â°â€¢Ã Â±ÂÃ Â°â€¢Ã Â°Â¿","Ã Â¤ÂµÃ Â¤Â¾Ã Â¤ÂªÃ Â¤Â¸","Ã Â²Â¹Ã Â²Â¿Ã Â²â€šÃ Â²Â¦Ã Â³â€ ","Ã Â´Â¤Ã Â´Â¿Ã Â´Â°Ã Â´Â¿Ã Â´Å¡Ã ÂµÂÃ Â´Å¡Ã ÂµÂ","Ã Â¤Â®Ã Â¤Â¾Ã Â¤â€”Ã Â¥â€¡"), color = Color.Gray)
    }
    Spacer(modifier = Modifier.height(8.dp))
}
@Composable
fun NearbyMarketsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        BackButton(navController)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Ã°Å¸â€œÂ Nearby Markets")

        Text("Ã°Å¸Å’Â¾ Guntur Market Yard")
        Text("Ã°Å¸Å’Â¾ Vijayawada Market Yard")
        Text("Ã°Å¸Å’Â¾ Kurnool Market Yard")
    }
}
@Composable
fun FeedbackScreen(navController: NavController) {

    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        BackButton(navController)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Ã¢Â­Â Feedback")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Write feedback") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:244g1a0555@srit.ac.in")
                        putExtra(Intent.EXTRA_SUBJECT, "Rythu Connect Feedback")
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Feedback")
        }
    }
}


