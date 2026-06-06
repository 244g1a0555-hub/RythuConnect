package com.example.rythuconnect


import android.os.Bundle
import android.content.Intent
import android.net.Uri
import com.google.android.gms.location.LocationServices
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.platform.LocalContext
import android.location.Location
import android.util.Log
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.PropertyName
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.FirebaseException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import android.os.Build
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

enum class Lang { ENGLISH, TELUGU, HINDI, KANNADA, MALAYALAM, MARATHI }

object AppLang {
    var lang = mutableStateOf(Lang.ENGLISH)
    fun t(en: String, te: String, hi: String, kn: String, ml: String, mr: String) = when (lang.value) {
        Lang.TELUGU -> te; Lang.HINDI -> hi; Lang.KANNADA -> kn
        Lang.MALAYALAM -> ml; Lang.MARATHI -> mr; else -> en
    }
}

data class Market(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("lat") @set:PropertyName("lat") var lat: Double = 0.0,
    @get:PropertyName("lon") @set:PropertyName("lon") var lon: Double = 0.0,
    @get:PropertyName("latitude") @set:PropertyName("latitude") var latitude: Double = 0.0,
    @get:PropertyName("longitude") @set:PropertyName("longitude") var longitude: Double = 0.0,
    @get:PropertyName("address") @set:PropertyName("address") var address: String = ""
) {
    val actualLat get() = if (lat != 0.0) lat else latitude
    val actualLon get() = if (lon != 0.0) lon else longitude
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val results = FloatArray(1)
    try {
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000
    } catch (e: Exception) { return 999f }
}

fun formatTimestamp(timestamp: Any?): String {
    return try {
        if (timestamp is com.google.firebase.Timestamp) {
            val date = timestamp.toDate()
            val now = Calendar.getInstance()
            val postDate = Calendar.getInstance()
            postDate.time = date
            
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            
            if (now.get(Calendar.DATE) == postDate.get(Calendar.DATE) &&
                now.get(Calendar.MONTH) == postDate.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)) {
                return "Today, ${timeFormat.format(date)}"
            } else if (now.get(Calendar.DATE) - postDate.get(Calendar.DATE) == 1 &&
                now.get(Calendar.MONTH) == postDate.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)) {
                return "Yesterday, ${timeFormat.format(date)}"
            }
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(date)
        } else ""
    } catch (e: Exception) { "" }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RythuConnectTheme { RythuConnectApp() } }
    }
}

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val SELL_CROPS = "sell_crops"
    const val VIEW_CROPS = "view_crops"
    const val BUY_CROPS = "buy_crops"
    const val WEATHER = "weather"
    const val MARKET_PRICES = "market_prices"
    const val CONTACT = "contact"
    const val PROFILE = "profile"
    const val NEARBY_MARKETS = "nearby_markets"
    const val FEEDBACK = "feedback"
}

@Composable
fun RythuConnectApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    
    // Notification Channel Setup
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = AppLang.t("New Crops", "కొత్త పంటలు", "नई फसलें", "ಹೊಸ ಬೆಳೆಗಳು", "പുതിയ വിളകൾ", "नवीन పికే")
            val channel = NotificationChannel("crops_channel", channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    // Live Notification Listener
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        var initialLoad = true
        db.collection("crops")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                if (initialLoad) {
                    initialLoad = false
                    return@addSnapshotListener
                }
                val doc = snapshot.documents.firstOrNull()
                if (doc != null) {
                    val cropName = doc.getString("cropName") ?: "Crop"
                    val farmer = doc.getString("farmerName") ?: "A farmer"
                    
                    val title = AppLang.t("New Crop Alert! 🌾", "కొత్త పంట హెచ్చరిక! 🌾", "नई फसल की सूचना! 🌾", "ಹೊಸ ಬೆಳೆ ಎಚ್ಚರಿಕೆ! 🌾", "പുതിയ വിള അറിയിപ്പ്! 🌾", "नवीन पीक सूचना! 🌾")
                    val messageEn = "$farmer listed $cropName for sale."
                    val messageTe = "$farmer $cropName అమ్మకానికి పెట్టారు."
                    val messageHi = "$farmer ने $cropName बिक्री کے لیے درج کیا۔"
                    val messageKn = "$farmer $cropName ಮಾರಾಟಕ್ಕಿದೆ."
                    val messageMl = "$farmer $cropName വിൽപ്പനയ്ക്കായി വെച്ചു."
                    val messageMr = "$farmer ने $cropName विक्रीसाठी ठेवले आहे."
                    
                    sendAppNotification(context, title, AppLang.t(messageEn, messageTe, messageHi, messageKn, messageMl, messageMr))
                }
            }
    }

    val startDestination = if (auth.currentUser != null) Routes.HOME else Routes.LOGIN
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.SIGNUP) { SignupScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.SELL_CROPS) { SellCropsScreen(navController) }
        composable(Routes.VIEW_CROPS) { ViewCropsScreen(navController) }
        composable(Routes.BUY_CROPS) { BuyCropsScreen(navController) }
        composable(Routes.WEATHER) { WeatherScreen(navController) }
        composable(Routes.MARKET_PRICES) { MarketPricesScreen(navController) }
        composable(Routes.CONTACT) { ContactScreen(navController) }
        composable(Routes.PROFILE) { ProfileScreen(navController) }
        composable(Routes.NEARBY_MARKETS) { NearbyMarketsScreen(navController) }
        composable(Routes.FEEDBACK) { FeedbackScreen(navController) }
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
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        LangSelector()
        Spacer(modifier = Modifier.height(16.dp))
        val brandName = AppLang.t("Rythu Connect", "రైతు కనెక్ట్", "किसान कनेक्ट", "ರೈತ కనెన్", "കർഷക കണക്ട്", "शेतकरी कनेक्ट")
        Text("🌾 $brandName", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = green)
        Text(AppLang.t("Login","లాగిన్","लॉगिन","ಲಾಗಿನ್","ലോഗിൻ","लॉगिन"), fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it },
            label = { Text(AppLang.t("Email","ఈమెయిల్","ईमेल","ಇಮೇಲ್","ഇമെയിൽ","ईमेल")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text(AppLang.t("Password","పాస్‌వర్డ్","पासवर्ड","ಪಾಸ್‌ವರ್ಡ್","పాസ്‌వేడ్","पासवर्ड")) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { TextButton(onClick = { passwordVisible = !passwordVisible }) { Text(if (passwordVisible) AppLang.t("Hide","దాచు","छिपाएं","మರೆమాడి","మறയ്ക്കുക","लपవా") else AppLang.t("Show","చూపించు","దिखाएं","ತೋರಿಸಿ","കാణിക്കുക","దాఖవా"), fontSize = 12.sp) } },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        if (errorMsg.isNotEmpty()) { Text(errorMsg, color = if (errorMsg.contains("✅")) Color(0xFF2E7D32) else Color.Red, fontSize = 13.sp); Spacer(modifier = Modifier.height(8.dp)) }
        Button(onClick = {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                isLoading = true
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { isLoading = false; navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
                    .addOnFailureListener { isLoading = false; errorMsg = it.message ?: "Login failed" }
            } else { errorMsg = AppLang.t("Please fill all fields", "అన్ని ఫీల్డ్‌లను పూరించండి", "कृपया सभी फ़ील्ड भरें", "ದಯವಿಟ್ಟು ಎಲ್ಲಾ ಕ್ಷೇತ್ರಗಳನ್ನು భర్తి ಮಾಡಿ", "దయవాయి అన్ని వివరాలు ఇవ్వండి", "कृपया सर्व फील्ड भरा") }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
            Text(if (isLoading) AppLang.t("Please wait...", "దయచేసి వేచి ఉండండి...", "कृपया प्रतीक्षा करें...", "ದಯವಿಟ್ಟು నిరీక్షించి...", "ദയవాయి కాత్తిരിക്കూ...", "कृपया प्रतीक्षा करा...") else AppLang.t("Login","లాగిన్","लॉगिन","ಲಾಗಿన్","లొగిన్","लॉगिन"), color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(8.dp))
        var loginWithPhone by remember { mutableStateOf(false) }
        var phoneNumber by remember { mutableStateOf("") }
        var otpCode by remember { mutableStateOf("") }
        var verificationId by remember { mutableStateOf("") }
        var isOtpSent by remember { mutableStateOf(false) }
        val context = LocalContext.current as ComponentActivity

        TextButton(onClick = { loginWithPhone = !loginWithPhone }) {
            Text(if (loginWithPhone) "Use Email instead" else "Login with Phone Number", color = green)
        }

        if (loginWithPhone) {
            OutlinedTextField(
                value = phoneNumber, onValueChange = { phoneNumber = it },
                label = { Text("+91XXXXXXXXXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (!isOtpSent) {
                Button(onClick = {
                    isLoading = true
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                        .setActivity(context)
                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                auth.signInWithCredential(credential)
                                    .addOnSuccessListener { isLoading = false; navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
                            }
                            override fun onVerificationFailed(e: FirebaseException) {
                                isLoading = false; errorMsg = e.message ?: "Failed"
                            }
                            override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                isLoading = false; verificationId = vId; isOtpSent = true; errorMsg = "✅ OTP Sent!"
                            }
                        }).build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }, modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)) {
                    Text(if (isLoading) "Sending OTP..." else "Send OTP", color = Color.White)
                }
            } else {
                OutlinedTextField(
                    value = otpCode, onValueChange = { otpCode = it },
                    label = { Text("Enter OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    isLoading = true
                    val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { isLoading = false; navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
                        .addOnFailureListener { isLoading = false; errorMsg = it.message ?: "Invalid OTP" }
                }, modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)) {
                    Text(if (isLoading) "Verifying..." else "Verify OTP", color = Color.White)
                }
            }
        }
        TextButton(onClick = { navController.navigate(Routes.SIGNUP) }) {
            Text(AppLang.t("No account? Signup","అకౌంట్ లేదా? సైన్అప్","खाता नहीं? साइनअप","ಖಾತೆ ಇಲ್ಲವೇ? సೈನ್ అప్","അക്കൗണ്ട് ഇല്ലേ? സൈൻഅപ്പ്","खाते नाही? साइनअप"), color = green)
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
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        LangSelector()
        Spacer(modifier = Modifier.height(12.dp))
        Text("🌾 ${AppLang.t("New Account","కొత్త అకౌంట్","नया खाता","ಹೊಸ ಖಾತೆ","പുതിയ అకౌంట్","नवीन खाते")}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = green)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(AppLang.t("Full Name","పూర్తి పేరు","पूरा नाम","ಪೂರ್ಣ ಹೆಸರು","పూర్ണ്ണ పేరు","पूर्ण नाव")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(AppLang.t("Email","ఈమెయిల్","ईमेल","ಇಮೇಲ್","ഇമെയിൽ","ईमेल")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(AppLang.t("Phone","ఫోన్","फोन","ಫೋನ್","ఫోన్","फोन")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text(AppLang.t("Village / City","గ్రామం / నగరం","गांव / शहर","ಗ್ರಾಮ / ನಗರ","గ్రాయం / నగరం","गाव / शहर")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(AppLang.t("Password","పాస్‌వర్డ్","पासवर्ड","పాస్‌వర్డ్","పాസ്‌వేడ్","पासवर्ड")) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        if (errorMsg.isNotEmpty()) { Text(errorMsg, color = if (errorMsg.contains("✅")) Color(0xFF2E7D32) else Color.Red, fontSize = 13.sp); Spacer(modifier = Modifier.height(8.dp)) }
        Button(onClick = {
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                isLoading = true
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val db = FirebaseFirestore.getInstance()
                        val user = hashMapOf("name" to name, "phone" to phone, "village" to village, "email" to email)
                        db.collection("users").document(it.user!!.uid).set(user)
                        isLoading = false
                        navController.navigate(Routes.HOME) { popUpTo(Routes.SIGNUP) { inclusive = true } }
                    }
                    .addOnFailureListener { isLoading = false; errorMsg = it.message ?: "Signup failed" }
            } else { errorMsg = AppLang.t("Please fill all fields", "అన్ని ఫీల్డ్‌లను పూరించండి", "कृपया सभी फ़ील्ड भरें", "ದಯವಿಟ್ಟು ಎಲ್ಲಾ ಕ್ಷೇತ್ರಗಳನ್ನು భర్తి ಮಾಡಿ", "దยవాయి అన్ని వివరాలు ఇవ్వండి", "कृपया सर्व फील्ड भरा") }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
            Text(if (isLoading) AppLang.t("Creating...", "సృష్టిస్తోంది...", "बना रहा है...", "ರಚಿಸಲಾಗುತ್ತಿದೆ...", "സൃഷ്ടിക്കുന്നു...", "तयार करत आहे...") else AppLang.t("Signup","సైన్అప్","साइनअप","సೈನ್ అప్","സൈൻഅപ്പ്","साइनअप"), color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text(AppLang.t("Already have account? Login","అకౌంట్ ఉందా? లాగిన్","खाता है? लॉगिन","ಖಾತೆ ಇದೆಯೇ? ಲಾಗಿన్","అకౌంట్ ഉണ്ടో? లొగిన్","खाते आहे? लॉगिन"), color = green)
        }
    }
}

@Composable
fun LangSelector() {
    val currentLang by AppLang.lang
    val languages = listOf(
        Lang.ENGLISH to "EN",
        Lang.TELUGU to "తెలుగు",
        Lang.HINDI to "हिन्दी",
        Lang.KANNADA to "ಕನ್ನಡ",
        Lang.MALAYALAM to "മലയാളം",
        Lang.MARATHI to "మరాఠీ"
    )
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        LangSelector()
        Spacer(modifier = Modifier.height(12.dp))
        val brandName = AppLang.t("Rythu Connect", "రైతు కనెక్ట్", "किसान कनेक्ट", "ರೈತ కనెన్", "കർഷക കണക്ട്", "शेतकरी कनेक्ट")
        Text("🌾 $brandName", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = green)
        Text(AppLang.t("Farmer Connect","రైతు కనెక్ట్","किसान कनेक्ट","రైత కనెక్ట్","കർഷക കണക്ട്","शेतकरी कनेक्ट"), fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.farmer_img),
            contentDescription = "Farmer Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        MenuButton("🌾 ${AppLang.t("Sell Crops","పంట అమ్మండి","फसल बेचें","బెಳೆ మారి","വിള విൽക്കുക","पीक विका")}", green) { navController.navigate(Routes.SELL_CROPS) }
        MenuButton(text = "🛒 ${AppLang.t("Buy Crops", "పంట కొనండి", "फसल खरीदें", "బెಳೆ ಖರೀದಿಸಿ", "വിള വാങ്ങുക", "पीक खरेदी करा")}", color = Color(0xFF1565C0)) { navController.navigate(Routes.BUY_CROPS) }
        MenuButton("📋 ${AppLang.t("View Crops","పంటల జాబితా","फसलें देखें","బెళెగళన్ను నోడి","വിളകൾ കാണുക","पिके पहा")}", Color(0xFF00796B)) { navController.navigate(Routes.VIEW_CROPS) }
        MenuButton("🌤️ ${AppLang.t("Weather","వాతావరణం","मौसम","హవామాన","കാലാവస్థ","హవామాన")}", Color(0xFF1565C0)) { navController.navigate(Routes.WEATHER) }
        MenuButton("💰 ${AppLang.t("Market Prices","మార్కెట్ ధరలు","बाजार भाव","మారికేట్ ధరలు","വിపణి വില","बाजार भाव")}", Color(0xFFE65100)) { navController.navigate(Routes.MARKET_PRICES) }
        MenuButton("📞 ${AppLang.t("Contact","సంప్రదించండి","संपर्क करें","సంపర్కಿಸಿ","ബന്ധപ്പെടുക","संपर्क करा")}", Color(0xFF6A1B9A)) { navController.navigate(Routes.CONTACT) }
        MenuButton("👤 ${AppLang.t("My Profile","నా ప్రొఫైల్","मेरी प्रोफाइल","నన్న ప్రొఫైల్","ఎന്റെ ప్రొഫൈల్","మాझी प्रोफाइल")}", Color(0xFF00838F)) { navController.navigate(Routes.PROFILE) }
        MenuButton(
            "📍 ${AppLang.t("Nearby Markets", "సమీప మార్కెట్లు", "आस-पास के बाजार", "హత్తరద మారుకట్టెలు", "അടുത്തുള്ള విపణികൾ", "जवळपासची बाजारपेठ")}",
            Color(0xFF00897B)
        ) {
            navController.navigate(Routes.NEARBY_MARKETS)
        }
        MenuButton(
            "⭐ ${AppLang.t("Feedback", "అభిప్రాయం", "ప్రతిక్రియ", "అభీప్రాయ", "అభిప్రాయం", "प्रतिसाद")}",
            Color(0xFFFFA000)
        ) {
            navController.navigate(Routes.FEEDBACK)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // MAGIC BUTTON: Click this once to add markets and prices to your Firebase automatically!
        Button(
            onClick = {
                val db = FirebaseFirestore.getInstance()
                
                // Adding Markets
                val markets = listOf(
                    hashMapOf("name" to "Anantapur Market Yard", "address" to "Anantapur, AP", "lat" to 14.6819, "lon" to 77.6006),
                    hashMapOf("name" to "Guntur Market Yard", "address" to "Guntur, AP", "lat" to 16.3067, "lon" to 80.4365),
                    hashMapOf("name" to "Kurnool Market Yard", "address" to "Kurnool, AP", "lat" to 15.8281, "lon" to 78.0373),
                    hashMapOf("name" to "Adoni Market Yard", "address" to "Kurnool, AP", "lat" to 15.6291, "lon" to 77.2724),
                    hashMapOf("name" to "Nandyal Market", "address" to "Nandyal, AP", "lat" to 15.4847, "lon" to 78.4812),
                    hashMapOf("name" to "Chittoor Tomato Market", "address" to "Chittoor, AP", "lat" to 13.2172, "lon" to 79.1003),
                    hashMapOf("name" to "Tirupati Yard", "address" to "Tirupati, AP", "lat" to 13.6285, "lon" to 79.4192),
                    hashMapOf("name" to "Nellore Market", "address" to "Nellore, AP", "lat" to 14.4426, "lon" to 79.9865),
                    hashMapOf("name" to "Vijayawada Yard", "address" to "Vijayawada, AP", "lat" to 16.5062, "lon" to 80.6480),
                    hashMapOf("name" to "Visakhapatnam Market", "address" to "Vizag, AP", "lat" to 17.6868, "lon" to 83.2185)
                )
                markets.forEach { market ->
                    db.collection("markets").add(market)
                }

                // Adding Live Prices
                val prices = listOf(
                    hashMapOf("cropName" to "Paddy (Common)", "price" to "₹2,183/Qtl", "trend" to "up"),
                    hashMapOf("cropName" to "Red Chilli (Guntur)", "price" to "₹18,500/Qtl", "trend" to "down"),
                    hashMapOf("cropName" to "Cotton", "price" to "₹7,200/Qtl", "trend" to "up"),
                    hashMapOf("cropName" to "Groundnut", "price" to "₹6,850/Qtl", "trend" to "stable"),
                    hashMapOf("cropName" to "Tomato", "price" to "₹15/Kg", "trend" to "down"),
                    hashMapOf("cropName" to "Onion", "price" to "₹22/Kg", "trend" to "up")
                )
                prices.forEach { price ->
                    db.collection("live_prices").add(price)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            val setupText = AppLang.t("🛠️ Click once to Setup Firebase (Markets & Prices)", 
                "🛠️ ఫైర్‌బేస్ సెటప్ చేయడానికి ఒకసారి క్లిక్ చేయండి (మార్కెట్లు & ధరలు)", 
                "🛠️ फायरबेस सेटअप करने के लिए एक बार क्लिक करें (बाजार और कीमतें)",
                "🛠️ ಫೈರ್‌ಬೇಸ್ ಸೆಟಪ್ ಮಾಡಲು ಒಮ್ಮೆ ಕ್ಲಿಕ್ ಮಾಡಿ (ಮಾರುಕಟ್ಟೆಗಳು ಮತ್ತು ಬೆಲೆಗಳು)",
                "🛠️ ഫയർബേസ് സജ്ജീകരിക്കുന്നതിന് ഒരിക്കൽ ക്ലിಕ್ ചെയ്യുക (മാർക്കറ്റുകളും വിലകളും)",
                "🛠️ फायरबेस सेटअप करण्यासाठी एकदा क्लिक करा (बाजार आणि किमती)")
            Text(setupText, color = Color(0xFF2E7D32), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { auth.signOut(); navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } } }) {
            Text(AppLang.t("Logout","లాగౌట్","లॉगアウト","లాగౌట్","లోగౌట్","लॉगआउट"), color = Color.Red)
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cropName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var farmerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var mandal by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val storage = FirebaseStorage.getInstance()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        LangSelector()
        BackButton(navController)
        Text("🌾 ${AppLang.t("Sell Crops","పంట అమ్మండి","फसल बेचें","బెಳೆ మారి","വിള విൽക്കുക","पीक विका")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(16.dp))

        // Image Picker Section
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            onClick = { launcher.launch("image/*") },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = "Selected Image", modifier = Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📸", fontSize = 40.sp)
                        Text(AppLang.t("Add Crop Photo", "పంట ఫోటో జోడించండి", "फसल की फोटो जोड़ें", "ಬೆಳೆ ಫೋಟೋ ಸೇರಿಸಿ", "വിള ഫോട്ടോ ചേർക്കുക", "पिकाचा फोटो जोडा"), color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = cropName, onValueChange = { cropName = it },
            label = { Text(AppLang.t("Crop Name","పంట పేరు","फसल का नाम","ಬೆಳೆ ಹೆಸರು","വിളയുടെ పేరు","पिकाचे नाव")) },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = quantity, onValueChange = { quantity = it },
            label = { Text(AppLang.t("Quantity in KG","పరిమాణం (కేజీ)","మాత్ర (కిలో)","ಪ್ರಮಾಣ (ಕೆಜಿ)","అളవ్ (കിലോ)","प्रमाण (किलो)")) },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = price, onValueChange = { price = it },
            label = { Text(AppLang.t("Price per KG (₹)","కేజీకి ధర (₹)","प्रति किलो मूल्य (₹)","ಪ್ರతి ಕೆజి ಬೆಲೆ (₹)","കിലോയ്ക്ക് വില (₹)","प्रति किलो भाव (₹)")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = farmerName, onValueChange = { farmerName = it }, label = { Text(AppLang.t("Farmer Name", "రైతు పేరు", "किसान का नाम", "ರైತರ ಹೆಸರು", "കർഷకന്റെ పేర", "शेतಕऱ्याचे नाव")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text(AppLang.t("Phone Number", "ఫోన్ నంబర్", "फ़ोन नंबर", "ఫొన్ ಸಂಖ್ಯె", "ఫోన్ నంబర్", "फोन नंबर")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text(AppLang.t("Village", "గ్రామం", "गांव", "గ్రామ", "గ్రామం", "गाव")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = mandal, onValueChange = { mandal = it }, label = { Text(AppLang.t("Mandal", "మండలం", "मंडल", "మండల", "మండలం", "मंडल")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text(AppLang.t("District", "జిల్లా", "जिला", "జిల్లా", "జిల్లే", "जिल्हा")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text(AppLang.t("State", "రాష్ట్రం", "राज्य", "రాజ్య", "సంస్థానము", "राज्य")) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (cropName.isNotEmpty() && quantity.isNotEmpty()) {
                    isLoading = true
                    
                    val saveToFirestore: (String) -> Unit = { url ->
                        val crop = hashMapOf(
                            "cropName" to cropName, "quantity" to quantity, "price" to price, 
                            "farmerName" to farmerName, "phoneNumber" to phoneNumber, 
                            "village" to village, "mandal" to mandal, "district" to district, "state" to state,
                            "imageUrl" to url,
                            "uid" to (auth.currentUser?.uid ?: ""),
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("crops").add(crop)
                            .addOnSuccessListener { submitted = true; isLoading = false }
                            .addOnFailureListener { isLoading = false }
                    }

                    if (imageUri != null) {
                        scope.launch(Dispatchers.Default) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(imageUri!!)
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                
                                // Extreme compression for performance
                                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                val width = 300
                                val height = (300 / ratio).toInt()
                                
                                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
                                val outputStream = ByteArrayOutputStream()
                                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                                
                                withContext(Dispatchers.Main) {
                                    saveToFirestore("data:image/jpeg;base64,$base64Image")
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Log.e("UploadError", e.message ?: "Error")
                                    saveToFirestore("")
                                }
                            }
                        }
                    } else {
                        saveToFirestore("")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text(if (isLoading) AppLang.t("Saving...", "సేవ్ అవుతోంది...", "सहेज रहा है...", "ಉಳಿಸಲಾಗುತ್ತಿದೆ...", "సేవ్య ചെയ്യുന്നു...", "जतन करत आहे...") else AppLang.t("Submit","సమర్పించు","జమా کریں","సల్లించు","സമർപ്പിക്കുക","सादर करा"), color = Color.White)
        }

        if (submitted) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✅ ${AppLang.t("Saved!","సేవ్ అయ్యింది!","सहजा गया!","ಉಳಿಸಲಾಗಿದೆ!","సూక్షించు!","जतन केले!")}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text("${AppLang.t("Crop: ", "పంట: ", "फसल: ", "బెಳೆ: ", "విళ: ", "पीक: ")} $cropName")
                    Text("${AppLang.t("Quantity: ", "పరిమాణం: ", "మాత్ర: ", "ప్రమాణ: ", "అళవు: ", "प्रमाण: ")} $quantity KG")
                    if (price.isNotEmpty()) Text("${AppLang.t("Price: ", "ధర: ", "मूल్య: ", "ಬెలె: ", "వില: ", "भाव: ")} ₹$price/KG")
                }
            }
        }
    }
}

@Composable
fun BuyCropsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var crops by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = db.collection("crops")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    crops = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap()
                        data + ("docId" to doc.id)
                    }
                }
            }
        onDispose { listener.remove() }
    }

    // LazyColumn vaadatam valla app asalu hang avvadhu
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            BackButton(navController)
            Text(AppLang.t("Buy Crops", "పంట కొనండి", "फसल खरीदें", "బెళె ఖరీదిసి", "విళు వాంగూ", "పీక్ ఖరేదీ కరా"), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            }
        } else if (crops.isEmpty()) {
            item {
                Text(AppLang.t("No crops available for sale.", "అమ్మకానికి పంటలు లేవు.", "बिक्री के लिए कोई फसल उपलब्ध नहीं है।", "మారూకట్టెయల్లి యావుదే బెళెగళు లభ్యవిల్ల.", "విൽപ്പനയ്ക്കాయి വിളകളൊന്നും ലഭ്യമల్ల.", "विक्रीसाठी कोणतीही పికే లభ్యవిల్ల."), color = Color.Gray)
            }
        } else {
            items(crops) { crop ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val imgUrl = crop["imageUrl"]?.toString() ?: ""
                        if (imgUrl.isNotEmpty()) {
                            val imageModel = remember(imgUrl) {
                                if (imgUrl.startsWith("data:image")) {
                                    try {
                                        val base64String = imgUrl.substringAfter(",")
                                        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    } catch (e: Exception) { imgUrl }
                                } else { imgUrl }
                            }
                            
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Crop Image",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(180.dp).padding(bottom = 12.dp)
                            )
                        }

                        Text(crop["cropName"]?.toString() ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1B5E20))
                        Text("${AppLang.t("Quantity: ", "పరిమాణం: ", "మాత్ర: ", "ప్రమాణ: ", "అళవు: ", "ప్రమాణ: ")} ${crop["quantity"]} KG", color = Color.Gray)
                        Text("${AppLang.t("Price: ", "ధర: ", "మూల్య: ", "బెలె: ", "వెల: ", "భావ్: ")} ₹${crop["price"]}/KG", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                        Text("${AppLang.t("Farmer: ", "రైతు: ", "కిసాన్: ", "రైత: ", "కర్షకన్: ", "శేతకరీ: ")} ${crop["farmerName"]?.toString() ?: ""}", fontWeight = FontWeight.SemiBold)
                        Text("${AppLang.t("Phone: ", "ఫోన్: ", "ఫోన్: ", "ఫోన్: ", "ఫోన్: ", "ఫోన్: ")} ${crop["phoneNumber"]?.toString() ?: ""}", color = Color(0xFF1565C0), fontWeight = FontWeight.Medium)
                        Text("${AppLang.t("Village: ", "గ్రామం: ", "గావ్: ", "గ్రామ: ", "గ్రామం: ", "గావ్: ")} ${crop["village"]?.toString() ?: ""}")

                        val timeStr = formatTimestamp(crop["timestamp"])
                        if (timeStr.isNotEmpty()) {
                            Text("🕒 $timeStr", color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }

                        val addressParts = listOfNotNull(crop["mandal"], crop["district"], crop["state"]).filter { it.toString().isNotEmpty() }
                        if (addressParts.isNotEmpty()) {
                            Text(addressParts.joinToString(", "))
                        }

                        // Delete Option for Owner
                        if (crop["uid"] == auth.currentUser?.uid) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    val docId = crop["docId"]?.toString() ?: ""
                                    if (docId.isNotEmpty()) {
                                        db.collection("crops").document(docId).delete()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("🗑️ ${AppLang.t("Delete Listing", "పంటను తొలగించు", "ఫసల్ హటాయేం", "బెలెయన్ను తేగిసి", "విళ నీక్కం చేయూ", "పీక్ కాఢూన్ టాకా")}")
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ViewCropsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var crops by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = db.collection("crops")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    crops = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap()
                        data + ("docId" to doc.id)
                    }
                }
            }
        onDispose { listener.remove() }
    }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            LangSelector()
            BackButton(navController)
            Text("📋 ${AppLang.t("Listed Crops","పంటల జాబితా","फसलों की सूची","బెళెగళన్ను నోడి","വിളകൾ കാണുക","पिके पहा")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (crops.isEmpty()) {
            item {
                Text(AppLang.t("No crops listed yet!", "ఇంకా పంటలు జాబితా చేయబడలేదు!", "अभी तक कोई फसल सूचीबद्ध नहीं है!", "ఇನ್ನೂ ಯಾವುದೇ ಬೆಳೆಗಳನ್ನು పట్టి చేయలేదు!", "വിളകളൊന്നും ഇതുവരെ പട്ടികപ്പെടുത്തിയിട്ടില്ല!", "అद्याప कोणतीही ಪికే सूचीबद्ध केलेली नाहीत!"), color = Color.Gray)
            }
        } else {
            items(crops) { crop ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val imgUrl = crop["imageUrl"]?.toString() ?: ""
                        if (imgUrl.isNotEmpty()) {
                            val imageModel = remember(imgUrl) {
                                if (imgUrl.startsWith("data:image")) {
                                    try {
                                        val base64String = imgUrl.substringAfter(",")
                                        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    } catch (e: Exception) { imgUrl }
                                } else { imgUrl }
                            }
                            AsyncImage(model = imageModel, contentDescription = "Crop",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(150.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(crop["cropName"]?.toString() ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${crop["quantity"]} KG", color = Color(0xFF00796B), fontWeight = FontWeight.Bold)
                        }
                        val price = crop["price"]?.toString() ?: ""
                        if (price.isNotEmpty()) Text("₹$price/KG", color = Color(0xFFE65100), fontSize = 13.sp)
                        
                        val timeStr = formatTimestamp(crop["timestamp"])
                        if (timeStr.isNotEmpty()) {
                            Text("🕒 $timeStr", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                        }

                        // Delete Option for Owner
                        if (crop["uid"] == auth.currentUser?.uid) {
                            IconButton(
                                onClick = {
                                    val docId = crop["docId"]?.toString() ?: ""
                                    if (docId.isNotEmpty()) {
                                        db.collection("crops").document(docId).delete()
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("🗑️", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun sendAppNotification(context: Context, title: String, message: String) {
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val notification = NotificationCompat.Builder(context, "crops_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(System.currentTimeMillis().toInt(), notification)
}

@Composable
fun WeatherScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocationAndWeather(fusedLocationClient) { result, data ->
                weatherData = data
                errorMsg = if (data == null) result else null
                isLoading = false
            }
        } else {
            errorMsg = AppLang.t("Location permission denied", "లొకేషన్ అనుమతి నిరాకరించబడింది", "स्थान अनुमति अस्वीकार कर दी गई", "ಸ್ಥಳ ಅನುಮತಿ ನಿರಾಕರಿಸಲಾಗಿದೆ", "ലൊക്കേഷൻ അനുമതി നിഷേധിച്ചു", "स्थान परवानगी नाकारली")
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(navController)
            LangSelector()
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "🌤️ " + AppLang.t("Weather", "వాతావరణం", "मौसम", "హవామాన", "കാലാവస్థ", "హవామాన"),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1565C0)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1565C0))
            }
        } else if (errorMsg != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(errorMsg!!, color = Color(0xFFC62828), fontWeight = FontWeight.Medium)
                }
            }
        } else if (weatherData != null) {
            Text(
                text = "📍 ${weatherData!!.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            WeatherDisplay(weatherData!!)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = AppLang.t("Farming Tips", "వ్యవసాయ సూచనలు", "खेती के सुझाव", "ಕೃಷಿ ಸಲಹೆಗಳು", "കൃഷി ടിപ്സ്", "शेती टिप्स"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        WeatherTipCard(
            "🚜", 
            AppLang.t("Check soil moisture before sowing.", "విత్తే ముందు నేల తేమను తనిఖీ చేయండి.", "बुवाई से पहले मिट्टी की नमी की जांच करें।", "ಬಿತ್ತನೆ ಮಾಡುವ ಮೊದಲು మಣ್ಣಿನ తేవాంశವನ್ನು పರೀಕ್ಷಿಸಿ.", "വിതയ്ക്കുന്നതിന് ముമ്പ് മണ്ണിലെ ഈർപ്പം പരിശോധിക്കുക.", "पेरणीपूर्वी जमिनीतील ओलावा तपासा.")
        )
        WeatherTipCard(
            "💧", 
            AppLang.t("Irrigate based on weather forecast.", "వాతావరణ సూచన ఆధారంగా నీరు పెట్టండి.", "मौसम के पूर्वानुमान के आधार पर सिंचाई करें।", "ಹವಾಮಾನ మున్సూచನೆಯ ఆಧಾರದ ಮೇಲೆ నీరාවరి ಮಾಡಿ.", "കാലാവస్థా ప్రవചനത്തിന്റെ അടിസ്ഥാനത്തിൽ నനയ്ക്കുക.", "हवामान अंदाजाच्या आधारे ओलित करा.")
        )
    }
}

@Composable
fun WeatherDisplay(data: WeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = AppLang.t("Live Conditions", "ప్రత్యక్ష పరిస్థితులు", "लाइव स्थिति", "ಲೈವ್ ಪರಿಸ್ಥಿತಿಗಳು", "തത്സമയ സാഹചര്യങ്ങൾ", "थेट परिस्थिती"),
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${data.main.temp.toInt()}°C",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Text(
                text = data.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                fontSize = 20.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                WeatherDetailItem("🌡️", AppLang.t("Temp", "ఉష్ణోగ్రత", "तापमान", "ತಾಪಮಾನ", "താపനില", "तापमान"), "${data.main.temp}°C")
                WeatherDetailItem("💧", AppLang.t("Humidity", "తేమ", "नमी", "తేవాంశ", "ఈర్പ്പം", "आद्रता"), "${data.main.humidity}%")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                WeatherDetailItem("💨", AppLang.t("Wind", "గాలి", "హవా", "గాలి", "കാറ്റ്", "वारा"), "${data.wind.speed} m/s")
                WeatherDetailItem("⏲️", AppLang.t("Pressure", "పీడనం", "దబావ", "ఒత్తడ", "മർദ്ദം", "दाब"), "${data.main.pressure} hPa")
            }
        }
    }
}

@Composable
fun WeatherDetailItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 24.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}

@Composable
fun WeatherTipCard(icon: String, tip: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(tip, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

private fun fetchLocationAndWeather(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onResult: (String, WeatherResponse?) -> Unit
) {
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                val apiKey = "cdba21b102f59eb10a58432b2c1ad874"

                RetrofitClient.instance.getWeather(lat, lon, apiKey)
                    .enqueue(object : Callback<WeatherResponse> {
                        override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                            if (response.isSuccessful) {
                                onResult("Success", response.body())
                            } else {
                                val error = AppLang.t("Weather data not found", "వాతావరణ డేటా కనుగొనబడలేదు", "मौसम डेटा नहीं मिला", "ಹವಾಮಾನ ಡೇಟಾ ಕಂಡುಬಂದಿಲ್ಲ", "കാലാവസ്ഥാ ഡാറ്റ കണ്ടെത്തിയില്ല", "हवामान डेटा आढळला नाही")
                                onResult("$error (Error: ${response.code()})", null)
                            }
                        }
                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                            val networkError = AppLang.t("Network Error", "నెట్‌వర్క్ లోపం", "नेटवर्क त्रुटि", "ನೆಟ್‌ವರ್ಕ್ ದೋಷ", "നെറ്റ്‌വർക്ക് പിശക്", "नेटवर्क त्रुटी")
                            onResult("$networkError: ${t.message}", null)
                        }
                    })
            } else {
                val locNotFound = AppLang.t("Location not found. Please enable GPS.", "లొకేషన్ కనుగొనబడలేదు. దయచేసి GPSని ప్రారంభించండి.", "स्थान नहीं मिला। कृपया जीपीएस सक्षम करें।", "ಸ್ಥಳ ಕಂಡುಬಂದಿಲ್ಲ. ದಯವಿಟ್ಟು ಜಿಪಿಎಸ್ ಸಕ್ರಿಯಗೊಳಿಸಿ.", "ലൊക്കേഷൻ കണ്ടെത്തിയില്ല. ദಯവായി ജിപിഎസ് പ്രവർത്തനക്ഷമമാക്കുക.", "स्थान आढळले नाही. कृपया जीपीएस सक्षम करा.")
                onResult(locNotFound, null)
            }
        }.addOnFailureListener {
            val failedLoc = AppLang.t("Failed to get location", "లొకేషన్ పొందడంలో విఫలమైంది", "स्थान प्राप्त करने में विफल", "ಸ್ಥಳ ಪಡೆಯುವಲ್ಲಿ ವಿಫಲವಾಗಿದೆ", "ലൊക്കേഷൻ ലഭിക്കുന്നതിൽ പരാജയപ്പെട്ടു", "स्थान मिळविण्यात अपयशी")
            onResult("$failedLoc: ${it.message}", null)
        }
    } catch (e: SecurityException) {
        val permMissing = AppLang.t("Location permission missing", "లొకేషన్ అనుమతి లేదు", "स्थान अनुमति मौजूद नहीं है", "ಸ್ಥಳ ಅನುಮತಿ ಇಲ್ಲ", "ലൊക്കേഷൻ അനുമതി ഇല്ല", "स्थान परवानगी गहाಳ आहे")
        onResult(permMissing, null)
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
    val db = FirebaseFirestore.getInstance()
    var priceList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = db.collection("live_prices")
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e != null) {
                    errorMsg = "Network error. Please check your connection."
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    priceList = snapshot.documents.map { it.data ?: emptyMap() }
                    errorMsg = if (priceList.isEmpty()) "No market prices available." else null
                }
            }
        onDispose { listener.remove() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        BackButton(navController)
        Text("💰 ${AppLang.t("Market Prices","మార్కెట్ ధరలు","बाजार भाव","మారికేట్ ధరలు","വിപണി വില","बाजार भाव")}", 
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE65100))
            }
        } else if (errorMsg != null) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Text(errorMsg!!, modifier = Modifier.padding(16.dp), color = Color.DarkGray)
            }
        } else {
            priceList.forEach { data ->
                val crop = data["cropName"]?.toString() ?: ""
                val price = data["price"]?.toString() ?: ""
                val trend = data["trend"]?.toString() ?: ""

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(crop, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(price, color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (trend == "up") Text(" 📈", fontSize = 14.sp)
                            else if (trend == "down") Text(" 📉", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        LangSelector(); BackButton(navController)
        Text("📞 ${AppLang.t("Contact","సంప్రదించండి","संपर्क करें","సంపర్కಿಸಿ","ബന്ധപ്പെടുക","संपर्क करा")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
        Spacer(modifier = Modifier.height(20.dp))
        ContactCard(AppLang.t("Helpline","హెల్ప్‌లైన్","हेल्पलाइन","ಸಹಾಯವಾಣಿ","ഹെൽപ്പ്‌ലൈൻ","मदत रेषा"), "1800-180-1551")
        ContactCard(AppLang.t("Email","ఈమెయిల్","ईमेल","ఇమేల్","ഇമെയിൽ","ईमेल"), "rythuconnect@ap.gov.in")
        ContactCard(AppLang.t("Website","వెబ్‌సైట్","వెబ్‌సైట్","వెబ్‍సైట్","వెబ్‌సైట్","वेबसाइट"), "www.rythuconnect.ap.gov.in")
        ContactCard(AppLang.t("Office","కార్యాలయం","కార్యాలయం","కచేరి","ഓഫിస్","कार्यालय"), "Amaravati, Andhra Pradesh")
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
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
            Text(AppLang.t("My Details","నా వివరాలు","मेरी जानकारी","నన్న వివర","ఎന്റെ വിവരങ്ങൾ","మాझी माहिती"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = green)
            Spacer(modifier = Modifier.height(12.dp))
            if (isEditing) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(AppLang.t("Full Name","పూర్తి పేరు","పూరా नाम","పೂರ್ಣ ಹೆಸರು","పూర్ണ്ണ పేరు","पूर्ण नाव")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(AppLang.t("Phone","ఫోన్","फोन","ಫೋನ್","ఫోన్","फोन")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text(AppLang.t("Village / City","గ్రామం / నగరం","गांव / शहर","ಗ್ರಾమ / ನಗರ","గ్రాయం / నగరం","गाव / शहर")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        isSaving = true
                        val updates = hashMapOf<String, Any>("name" to name, "phone" to phone, "village" to village)
                        db.collection("users").document(uid).update(updates)
                            .addOnSuccessListener { isSaving = false; isEditing = false; successMsg = AppLang.t("✅ Saved!", "✅ సేవ్ అయ్యింది!", "✅ सहजा गया!", "✅ ಉಳಿಸಲಾಗಿದೆ!", "✅ సూక్షിച്ചു!", "✅ జतन केले!") }
                            .addOnFailureListener { isSaving = false }
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = green)) {
                        Text(if (isSaving) AppLang.t("Saving...", "సేవ్ అవుతోంది...", "सहेज रहा है...", "ಉಳಿಸಲಾಗುತ್ತಿದೆ...", "సేవ్య ചെയ്യുന്നു...", "जतन करत आहे...") else AppLang.t("Save","సేవ్","सहेजें","ಉಳಿಸు","సేవ్య చేయూ","जतन करा"), color = Color.White)
                    }
                    OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                        Text(AppLang.t("Cancel","రద్దు","రద్దు करें","రద్దుమాడు","റദ്ദാക്കുക","रद्द करा"))
                    }
                }
            } else {
                ProfileInfoRow("👤 ${AppLang.t("Name","పేరు","नाम","ಹೆసరు","పేరు","नाव")}", name)
                ProfileInfoRow("📱 ${AppLang.t("Phone","ఫోన్","फोन","ಫೋನ್","ಫೋನ್","फोन")}", phone)
                ProfileInfoRow("🏘️ ${AppLang.t("Village","గ్రామం","गांव","గ్రామం","గ్రాయం","गाव")}", village)
                ProfileInfoRow("📧 ${AppLang.t("Email","ఈమెయిల్","ఈమెయిల్","ఇమేల్","ఇమెయిల్","ईमेल")}", email)
                Spacer(modifier = Modifier.height(16.dp))
                if (successMsg.isNotEmpty()) { Text(successMsg, color = green, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp)) }
                Button(onClick = { isEditing = true; successMsg = "" }, modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F))) {
                    Text(AppLang.t("✏️ Edit Profile","✏️ ప్రొఫైల్ మార్చు","✏️ प्रोफाइल संपादित करें","✏️ ప్రొఫైల్ ಸಂಪಾದಿಸಿ","✏️ ప్రొఫైల్ ఎడిట్ చేయూ","✏️ प्रोफाइल संपादित करा"), color = Color.White)
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
        Text(AppLang.t("Back","వెనుకకు","వాపస","హింది","തിരിച്ച്","मागे"), color = Color.Gray)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun NearbyMarketsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var marketsList by remember { mutableStateOf<List<Pair<Market, Float>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isDemoData by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) { refreshTrigger++ } else { isLoading = false }
    }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                db.collection("markets").get().addOnSuccessListener { snapshot ->
                    val firestoreMarkets = snapshot.toObjects(Market::class.java)
                    
                    val sourceList = if (firestoreMarkets.isEmpty()) {
                        isDemoData = true
                        listOf(
                            Market("Guntur Market Yard", 16.3067, 80.4365, address = "Guntur, AP"),
                            Market("Vijayawada Yard", 16.5062, 80.6480, address = "Vijayawada, AP"),
                            Market("Kurnool Yard", 15.8281, 78.0373, address = "Kurnool, AP")
                        )
                    } else {
                        isDemoData = false
                        firestoreMarkets
                    }

                    marketsList = sourceList.map { market ->
                        val dist = if (location != null) {
                            calculateDistance(location.latitude, location.longitude, market.actualLat, market.actualLon)
                        } else { 0f }
                        market to dist
                    }.sortedBy { it.second }

                    isLoading = false
                }.addOnFailureListener { isLoading = false }
            }
        } catch (e: SecurityException) { isLoading = false }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BackButton(navController)
            TextButton(onClick = { refreshTrigger++ }) {
                val refreshText = AppLang.t("🔄 Refresh", "🔄 రిఫ్రెష్", "🔄 తాజా करें", "🔄 ರಿಫ್ರೆಶ್", "🔄 പുതുക്കുക", "🔄 రిఫ్రెష్")
                Text(refreshText, color = Color(0xFF00897B), fontSize = 12.sp)
            }
        }
        
        Text("📍 ${AppLang.t("Nearby Markets", "సమీప మార్కెట్లు", "आस-पास के बाजार", "హత్తరద మారుకట్టెలు", "അടുത്തുള്ള విపണികൾ", "जवळपासची बाजारपेठ")}", 
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00897B))
        
        if (isDemoData) {
            val demoNote = AppLang.t("📝 Note: Showing Demo Data (Firestore is empty)", "📝 గమనిక: డెమో డేటాను చూపిస్తోంది (ఫైర్‌బేస్ ఖాళీగా ఉంది)", "📝 नोट: डेमो डेटा दिखा रहा है (फायरबेस खाली है)", "📝 ಗಮನಿಸಿ: ಡೆಮೊ ಡೇಟಾವನ್ನು ತೋರಿಸಲಾಗುತ್ತಿದೆ (ಫೈರ್‌ಬೇಸ್ ಖಾಲಿಯಿದೆ)", "📝 കുറിപ്പ്: ഡെമോ ഡാറ്റ കാണിക്കുന്നു (ഫയർബേസ് ശൂന്യമാണ്)", "📝 टीप: डेमो डेटा दर्शवित आहे (फायरबेस रिकामे आहे)")
            Text(demoNote, fontSize = 12.sp, color = Color(0xFFE65100), modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00897B))
            }
        } else {
            marketsList.forEach { (market, distance) ->
                MarketItem(
                    name = market.name, 
                    address = market.address, 
                    distance = if (distance > 0) "%.1f km away".format(distance) else "Location N/A"
                )
            }
        }
    }
}

@Composable
fun MarketItem(name: String, address: String, distance: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), 
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(address, fontSize = 14.sp, color = Color.Gray)
            }
            if (distance.isNotEmpty()) {
                Surface(color = Color(0xFF00897B), shape = CircleShape) {
                    val awayText = AppLang.t("km away", "కి.మీ దూరంలో", "किमी दूर", "ಕಿಮೀ ದೂರದಲ್ಲಿ", "കിലോമീറ്റർ ദൂരെ", "किमी दूर")
                    Text("$distance $awayText", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
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
            .verticalScroll(rememberScrollState())
    ) {

        BackButton(navController)

        Spacer(modifier = Modifier.height(16.dp))

        Text("⭐ ${AppLang.t("Feedback", "అభిప్రాయం", "ప్రतिक్రియ", "అభీప్రాయ", "അഭിപ്രാయం", "प्रतिसाद")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(AppLang.t("Write feedback", "అభిప్రాయం రాయండి", "प्रतिक्रिया लिखें", "అభీప్రాయ ಬರೆಯಿರಿ", "അഭിപ്രായം രേഖപ്പെടുത്തുക", "प्रतिसाद लिहा")) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                try {
                    val sub = AppLang.t("Rythu Connect Feedback", "రైతు కనెక్ట్ అభిప్రాయం", "किसान कनेक्ट फीडबैक", "ರೈತ ಕನೆಕ್ಟ್ ಫೀಡ್‌ಬ್ಯಾಕ್", "കർഷക കണക്ട് ഫീഡ്‌ബാക്ക്", "शेतकरी कनेक्ट फीडबॅक")
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:244g1a0555@srit.ac.in")
                        putExtra(Intent.EXTRA_SUBJECT, sub)
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
        ) {
            Text(AppLang.t("Send Feedback", "అభిప్రాయాన్ని పంపండి", "प्रतिक्रिया भेजें", "అభిప్రాయాన్ని పంపండి", "అభిప్రాయం അയക്കുക", "प्रतिसाद पाठवा"), color = Color.White)
        }
    }
}
