package com.example.rythuconnect


import android.os.Bundle
import android.content.Intent
import android.net.Uri
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
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        LangSelector()
        Spacer(modifier = Modifier.height(16.dp))
        Text("🌾 Rythu Connect", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = green)
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
            trailingIcon = { TextButton(onClick = { passwordVisible = !passwordVisible }) { Text(if (passwordVisible) AppLang.t("Hide","దాచు","छिपाएं","మರೆమాಡಿ","మറയ്ക്കുക","लपवा") else AppLang.t("Show","చూపించు","దिखाएं","ತೋರಿಸಿ","കാణിക്കുക","దాఖవా"), fontSize = 12.sp) } },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        if (errorMsg.isNotEmpty()) { Text(errorMsg, color = Color.Red, fontSize = 13.sp); Spacer(modifier = Modifier.height(8.dp)) }
        Button(onClick = {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                isLoading = true
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { isLoading = false; navController.navigate("home") { popUpTo("login") { inclusive = true } } }
                    .addOnFailureListener { isLoading = false; errorMsg = it.message ?: "Login failed" }
            } else { errorMsg = AppLang.t("Please fill all fields", "అన్ని ఫీల్డ్‌లను పూరించండి", "कृपया सभी फ़ील्ड भरें", "ದಯವಿಟ್ಟು ಎಲ್ಲಾ ಕ್ಷೇತ್ರಗಳನ್ನು భర్తి ಮಾಡಿ", "ദയവായി എല്ലാ വിവരങ്ങളും നൽകുക", "कृपया सर्व फील्ड भरा") }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
            Text(if (isLoading) AppLang.t("Please wait...", "దయచేసి వేచి ఉండండి...", "कृपया प्रतीक्षा करें...", "ದಯವಿಟ್ಟು నిరీక్షించి...", "ദയവായി కాത്തിരിക്കൂ...", "कृपया प्रतीक्षा करा...") else AppLang.t("Login","లాగిన్","लॉगिन","ಲಾಗಿನ್","ലോഗిన్","लॉगिन"), color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("signup") }) {
            Text(AppLang.t("No account? Signup","అకౌంట్ లేదా? సైన్అప్","खाता नहीं? साइनअप","ಖಾತೆ ಇಲ್ಲವೇ? सೈನ್ ಅಪ್","അക്കൗണ്ട് ഇല്ലേ? സൈൻഅപ്പ്","खाते नाही? साइनअप"), color = green)
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
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(AppLang.t("Password","పాస్‌వర్డ్","पासवर्ड","ಪಾಸ್‌ವರ್ಡ್","పాസ്‌వేడ్","पासवर्ड")) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
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
            } else { errorMsg = AppLang.t("Please fill all fields", "అన్ని ఫీల్డ్‌లను పూరించండి", "कृपया सभी फ़ील्ड भरें", "ದಯవిಟ್ಟು ಎಲ್ಲಾ ಕ್ಷೇತ್ರಗಳನ್ನು భర్తి ಮಾಡಿ", "ദയవായി എല്ലാ വിവരങ്ങളും നൽകുക", "कृपया सर्व फील्ड भरा") }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
            Text(if (isLoading) AppLang.t("Creating...", "సృష్టిస్తోంది...", "बना रहा है...", "ರಚಿಸಲಾಗುತ್ತಿದೆ...", "സൃഷ്ടിക്കുന്നു...", "तयार करत आहे...") else AppLang.t("Signup","సైన్అప్","साइनअप","ಸೈನ್ ಅಪ್","സൈൻഅപ്പ്","साइनअप"), color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text(AppLang.t("Already have account? Login","అకౌంట్ ఉందా? లాగిన్","खाता है? लॉगिन","ಖಾತೆ ಇದೆಯೇ? ಲಾಗಿನ್","అకౌంట్ ഉണ്ടో? ലോഗിన్","खाते आहे? लॉगिन"), color = green)
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
        Text("🌾 Rythu Connect", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = green)
        Text(AppLang.t("Farmer Connect","రైతు కనెక్ట్","किसान कनेक्ट","రైత కనెక్ట్","കർഷക കണക്ട്","शेतकरी कनेक्ट"), fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.farmer_img),
            contentDescription = "Farmer Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        MenuButton("🌾 ${AppLang.t("Sell Crops","పంట అమ్మండి","फसल बेचें","ಬೆಳೆ ಮಾರಿ","വിള വിൽക്കുക","पीक विका")}", green) { navController.navigate("sell_crops") }
            MenuButton(text = "🛒 ${AppLang.t("Buy Crops", "పంట కొనండి", "फसल खरीदें", "ಬೆಳೆ ಖರೀದಿಸಿ", "വിള വാങ്ങുക", "पीक खरेदी करा")}", color = Color(0xFF1565C0)) { navController.navigate(route = "buy_crops") }
            MenuButton("📋 ${AppLang.t("View Crops","పంటల జాబితా","फसलें देखें","ಬెಳೆಗಳನ್ನು నోడి","വിളകൾ കാണുക","पिके पहा")}", Color(0xFF00796B)) { navController.navigate("view_crops") }
        MenuButton("🌤️ ${AppLang.t("Weather","వాతావరణం","मौसम","ಹವಾಮಾನ","കാലാവస్థ","हवाమాన")}", Color(0xFF1565C0)) { navController.navigate("weather") }
        MenuButton("💰 ${AppLang.t("Market Prices","మార్కెట్ ధరలు","बाजार भाव","మಾರುకಟ್ಟె బెలె","വിపണി വില","बाजार भाव")}", Color(0xFFE65100)) { navController.navigate("market_prices") }
        MenuButton("📞 ${AppLang.t("Contact","సంప్రదించండి","संपर्क करें","ಸಂಪర్కಿಸಿ","ബന്ധപ്പെടുക","संपर्क करा")}", Color(0xFF6A1B9A)) { navController.navigate("contact") }
        MenuButton("👤 ${AppLang.t("My Profile","నా ప్రొఫైల్","मेरी प्रोफाइल","నನ್ನ ప్రొఫైల్","ఎന്റെ ప్రొഫൈల్","माझी प्रोफाइल")}", Color(0xFF00838F)) { navController.navigate("profile") }
        MenuButton(
            "📍 ${AppLang.t("Nearby Markets", "సమీప మార్కెట్లు", "आस-पास के बाजार", "ಹತ್ತಿರದ మారుకಟ್ಟెలు", "അടുത്തുള്ള విపണികൾ", "जवळपासची बाजारपेठ")}",
            Color(0xFF00897B)
        ) {
            navController.navigate("nearby_markets")
        }
        MenuButton(
            "⭐ ${AppLang.t("Feedback", "అభిప్రాయం", "प्रतिक్రియ", "అభిప్రాయ", "അഭിപ്രായം", "प्रतिसाद")}",
            Color(0xFFFFA000)
        ) {
            navController.navigate("feedback")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { auth.signOut(); navController.navigate("login") { popUpTo("home") { inclusive = true } } }) {
            Text(AppLang.t("Logout","లాగౌట్","लॉगआउट","ಲಾಗೌట్","లోగౌట్","लॉगआउट"), color = Color.Red)
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
    var farmerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var mandal by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        LangSelector()
        BackButton(navController)
        Text("🌾 ${AppLang.t("Sell Crops","పంట అమ్మండి","फसल बेचें","ಬೆಳೆ ಮಾರಿ","വിള విൽക്കുക","पीक विका")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = cropName, onValueChange = { cropName = it },
            label = { Text(AppLang.t("Crop Name","పంట పేరు","फसल का नाम","ಬೆಳೆ ಹೆಸರು","വിളയുടെ పేరు","पिकाचे नाव")) },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = quantity, onValueChange = { quantity = it },
            label = { Text(AppLang.t("Quantity in KG","పరిమాణం (కేజీ)","మాత్ర (కిలో)","ಪ್ರಮಾಣ (ಕೆಜಿ)","അളവ് (കിലോ)","प्रमाण (किलो)")) },
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = price, onValueChange = { price = it },
            label = { Text(AppLang.t("Price per KG (₹)","కేజీకి ధర (₹)","प्रति किलो मूल्य (₹)","ಪ್ರతి ಕೆಜಿ ಬೆಲೆ (₹)","കിലോയ്ക്ക് വില (₹)","प्रति किलो भाव (₹)")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = farmerName, onValueChange = { farmerName = it }, label = { Text(AppLang.t("Farmer Name", "రైతు పేరు", "किसान का नाम", "ರೈತರ ಹೆಸರು", "കർഷകന്റെ പേര്", "शेतకऱ्याचे नाव")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text(AppLang.t("Phone Number", "ఫోన్ నంబర్", "फ़ोन नंबर", "ಫೋನ್ ಸಂಖ್ಯೆ", "ഫോൺ നമ്പർ", "फोन नंबर")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text(AppLang.t("Village", "గ్రామం", "गांव", "ಗ್ರಾಮ", "గ్రాయం", "गाव")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = mandal, onValueChange = { mandal = it }, label = { Text(AppLang.t("Mandal", "మండలం", "मंडल", "ಮಂಡಲ", "మండలం", "मंडल")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text(AppLang.t("District", "జిల్లా", "जिला", "జిల్లా", "జిల్లే", "जिल्हा")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text(AppLang.t("State", "రాష్ట్రం", "राज्य", "రాజ్య", "సంస్థానము", "राज्य")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (cropName.isNotEmpty() && quantity.isNotEmpty()) {
                    isLoading = true
                    val crop = hashMapOf(
                        "cropName" to cropName, "quantity" to quantity, "price" to price, "farmerName" to farmerName, "phoneNumber" to phoneNumber, "village" to village, "mandal" to mandal, "district" to district, "state" to state,
                        "uid" to (auth.currentUser?.uid ?: ""),
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
            Text(if (isLoading) AppLang.t("Saving...", "సేవ్ అవుతోంది...", "सहेज रहा है...", "ಉಳಿಸಲಾಗುತ್ತಿದೆ...", "സేవ్ ചെയ്യുന്നു...", "जतन करत आहे...") else AppLang.t("Submit","సమర్పించు","जमा करें","సల్లించు","സമർപ്പിക്കുക","सादर करा"), color = Color.White)
        }

        if (submitted) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✅ ${AppLang.t("Saved!","సేవ్ అయ్యింది!","सहजा गया!","ಉಳಿಸಲಾಗಿದೆ!","సూక్షించు!","जतन केले!")}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text("${AppLang.t("Crop: ", "పంట: ", "फसल: ", "ಬೆಳೆ: ", "വിള: ", "पीक: ")} $cropName")
                    Text("${AppLang.t("Quantity: ", "పరిమాణం: ", "మాత్ర: ", "ಪ್ರಮಾಣ: ", "അളവ്: ", "प्रमाण: ")} $quantity KG")
                    if (price.isNotEmpty()) Text("${AppLang.t("Price: ", "ధర: ", "मूल్య: ", "ಬೆಲೆ: ", "వില: ", "भाव: ")} ₹$price/KG")
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

    DisposableEffect(Unit) {
        val listener = db.collection("crops")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    crops = snapshot.documents.mapNotNull { it.data }
                }
            }
        onDispose { listener.remove() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        BackButton(navController)
        Text(AppLang.t("Buy Crops", "పంట కొనండి", "फसल खरीदें", "ಬೆಳೆ ಖರೀದಿಸಿ", "വിള വാങ്ങുക", "पीक खरेदी करा"), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1565C0))
            }
        } else if (crops.isEmpty()) {
            Text(AppLang.t("No crops available for sale.", "అమ్మకానికి పంటలు లేవు.", "बिक्री के लिए कोई फसल उपलब्ध नहीं है।", "ಮಾರಾಟಕ್ಕೆ ಯಾವುದೇ ಬೆಳೆಗಳು ಲಭ್ಯವಿಲ್ಲ.", "വിൽപ്പനയ്ക്ക് വിളകളൊന്നും ലഭ്യമല്ല.", "विक्रीसाठी कोणतीही पिके उपलब्ध नाहीत."), color = Color.Gray)
        } else {
            crops.forEach { crop ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(crop["cropName"]?.toString() ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1B5E20))
                        Text("${AppLang.t("Quantity: ", "పరిమాణం: ", "మాత్ర: ", "ಪ್ರಮಾಣ: ", "അളവ്: ", "प्रमाण: ")} ${crop["quantity"]} KG", color = Color.Gray)
                        Text("${AppLang.t("Price: ", "ధర: ", "मूल్య: ", "ಬೆಲೆ: ", "వില: ", "भाव: ")} ₹${crop["price"]}/KG", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                        Text("${AppLang.t("Farmer: ", "రైతు: ", "किसान: ", "రైత: ", "ಕർഷകൻ: ", "शेतकरी: ")} ${crop["farmerName"]?.toString() ?: ""}", fontWeight = FontWeight.SemiBold)
                        Text("${AppLang.t("Phone: ", "ఫోన్: ", "फोन: ", "ಫೋನ್: ", "ఫోన్: ", "फोन: ")} ${crop["phoneNumber"]?.toString() ?: ""}", color = Color(0xFF1565C0), fontWeight = FontWeight.Medium)
                        Text("${AppLang.t("Village: ", "గ్రామం: ", "गांव: ", "గ్రామ: ", "గ్రామం: ", "गाव: ")} ${crop["village"]?.toString() ?: ""}")

                        val addressParts = listOfNotNull(crop["mandal"], crop["district"], crop["state"]).filter { it.toString().isNotEmpty() }
                        if (addressParts.isNotEmpty()) {
                            Text(addressParts.joinToString(", "))
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
    var crops by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        db.collection("crops").get()
            .addOnSuccessListener { result -> crops = result.documents.mapNotNull { it.data }; isLoading = false }
            .addOnFailureListener { isLoading = false }
    }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            LangSelector()
            BackButton(navController)
            Text("📋 ${AppLang.t("Listed Crops","పంటల జాబితా","फसलों की सूची","ಬೆಳೆಗಳ పట్టి","ವಿಳകളുടെ പട്ടിക","पिकांची यादी")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
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
                Text(AppLang.t("No crops listed yet!", "ఇంకా పంటలు జాబితా చేయబడలేదు!", "अभी तक कोई फसल सूचीबद्ध नहीं है!", "ಇನ್ನೂ ಯಾವುದೇ ಬೆಳೆಗಳನ್ನು ಪಟ್ಟಿ ಮಾಡಿಲ್ಲ!", "വിളകളൊന്നും ഇതുവരെ പട്ടികപ്പെടുത്തിയിട്ടില്ല!", "अद्याप कोणतीही पिके सूचीबद्ध केलेली नाहीत!"), color = Color.Gray)
            }
        } else {
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
                        if (price.isNotEmpty()) Text("₹$price/KG", color = Color(0xFFE65100), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        LangSelector(); BackButton(navController)
        Text("🌤️ ${AppLang.t("Weather","వాతావరణం","मौसम","ಹವಾಮಾನ","കാലാവസ്ഥ","हवामान")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(20.dp))
        WeatherCard(AppLang.t("Hyderabad", "హైదరాబాద్", "हैदराबाद", "ಹೈದರಾಬಾದ್", "ഹൈദരാബാദ്", "हैदराबाद"), "32°C", AppLang.t("Sunny","ఎండ","धूप","ಬಿಸಿಲು","വെയിൽ","ऊन"))
        WeatherCard(AppLang.t("Warangal", "వరంగల్", "वरंगल", "ವರಂಗಲ್", "വാറങ്കల్", "वरंगल"), "29°C", AppLang.t("Cloudy","మేఘావృతం","बादल","ಮೋಡ","మేఘం","ढगाळ"))
        WeatherCard(AppLang.t("Vijayawada", "విజయవాడ", "विजयवाड़ा", "విజయవాడ", "വിജയവാഡ", "विजयवाडा"), "34°C", AppLang.t("Hot","వేడి","గర్మ్","బిసి","ചൂട്","ఉष्ण"))
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
        AppLang.t("Rice", "వరి", "चावल", "ಅಕ್ಕಿ", "അരി", "तांदूळ") to "₹2200/q",
        AppLang.t("Wheat", "గోధుమ", "गेहूं", "ಗೋಧಿ", "ಗೋतമ്പ്", "गहू") to "₹2015/q",
        AppLang.t("Cotton", "పత్తి", "कपास", "ಹತ್ತಿ", "പരുത്തി", "कापूस") to "₹6500/q",
        AppLang.t("Maize", "మొక్కజొన్న", "मक्का", "ಮೆಕ್ಕೆజೋಳ", "ചോളം", "मका") to "₹1850/q",
        AppLang.t("Turmeric", "పసుపు", "हल्दी", "ಅరిಶಿನ", "മഞ്ഞൾ", "हळद") to "₹9200/q"
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        LangSelector(); BackButton(navController)
        Text("💰 ${AppLang.t("Market Prices","మార్కెట్ ధరలు","बाजार भाव","ಮಾರುಕಟ್ಟೆ ಬెలె","വിపണി വില","बाजार भाव")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
        Spacer(modifier = Modifier.height(20.dp))
        prices.forEach { (crop, price) ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(crop, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text(price, color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ContactScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        LangSelector(); BackButton(navController)
        Text("📞 ${AppLang.t("Contact","సంప్రదించండి","संपर्क करें","ಸಂಪರ್ಕಿಸಿ","ബന്ധപ്പെടുക","संपर्क करा")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
        Spacer(modifier = Modifier.height(20.dp))
        ContactCard(AppLang.t("Helpline","హెల్ప్‌లైన్","हेल्पलाइन","ಸಹಾಯವಾಣಿ","ഹെൽപ്പ്‌ലൈൻ","मदत रेषा"), "1800-180-1551")
        ContactCard(AppLang.t("Email","ఈమెయిల్","ईमेल","ఇమేల్","ഇമെയിൽ","ईमेल"), "rythuconnect@ap.gov.in")
        ContactCard(AppLang.t("Website","వెబ్‌సైట్","वेबसाइट","వెబ్‍సైట్","വെബ്‌సైట్","वेबसाइट"), "www.rythuconnect.ap.gov.in")
        ContactCard(AppLang.t("Office","కార్యాలయం","कार्यालय","ಕಚೇರಿ","ഓఫీస్","कार्यालय"), "Amaravati, Andhra Pradesh")
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
            Text(AppLang.t("My Details","నా వివరాలు","मेरी जानकारी","ನನ್ನ ವಿವರ","ఎന്റെ വിവരങ്ങൾ","माझी माहिती"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = green)
            Spacer(modifier = Modifier.height(12.dp))
            if (isEditing) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(AppLang.t("Full Name","పూర్తి పేరు","పూరా नाम","ಪೂರ್ಣ ಹೆಸರು","పూర్ണ്ണ పేరు","पूर्ण नाव")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(AppLang.t("Phone","ఫోన్","फोन","ಫೋನ್","ఫోన్","फोन")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text(AppLang.t("Village / City","గ్రామం / నగరం","गांव / शहर","ಗ್ರಾಮ / ನಗರ","గ్రాయం / నగరం","गाव / शहर")) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        isSaving = true
                        val updates = hashMapOf<String, Any>("name" to name, "phone" to phone, "village" to village)
                        db.collection("users").document(uid).update(updates)
                            .addOnSuccessListener { isSaving = false; isEditing = false; successMsg = AppLang.t("✅ Saved!", "✅ సేవ్ అయ్యింది!", "✅ सहजा गया!", "✅ ಉಳಿಸಲಾಗಿದೆ!", "✅ సూక్షിച്ചു!", "✅ जतन केले!") }
                            .addOnFailureListener { isSaving = false }
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = green)) {
                        Text(if (isSaving) AppLang.t("Saving...", "సేవ్ అవుతోంది...", "सहेज रहा है...", "ಉಳಿಸಲಾಗುತ್ತಿದೆ...", "സేవ్ ചെയ്യുന്നു...", "जतन करत आहे...") else AppLang.t("Save","సేవ్","सहेजें","ಉಳಿಸು","സేవ్ చేయూ","जतन करा"), color = Color.White)
                    }
                    OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                        Text(AppLang.t("Cancel","రద్దు","రద్దు करें","రద్దుమాಡು","റദ്ദാക്കുക","रद्द करा"))
                    }
                }
            } else {
                ProfileInfoRow("👤 ${AppLang.t("Name","పేరు","नाम","ಹೆసರು","పేరు","नाव")}", name)
                ProfileInfoRow("📱 ${AppLang.t("Phone","ఫోన్","फोन","ಫೋನ್","ఫోన్","फोन")}", phone)
                ProfileInfoRow("🏘️ ${AppLang.t("Village","గ్రామం","गांव","గ్రామ","గ్రాయం","गाव")}", village)
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        BackButton(navController)

        Spacer(modifier = Modifier.height(16.dp))

        Text("📍 ${AppLang.t("Nearby Markets", "సమీప మార్కెట్లు", "आस-पास के बाजार", "ಹತ್ತಿರದ మారుకಟ್ಟెలు", "അടുത്തുള്ള വിපണികൾ", "जवळपासची बाजारपेठ")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00897B))
        Spacer(modifier = Modifier.height(16.dp))

        MarketItem(AppLang.t("🌾 Guntur Market Yard", "🌾 గుంటూరు మార్కెట్ యార్డ్", "🌾 गुंटूर मार्केट यार्ड", "🌾 ಗುಂಟೂರು ಮಾರುಕಟ್ಟೆ ಯಾರ್ಡ್", "🌾 ഗുണ്ടూర్ మార్కెట్ యాർഡ്", "🌾 गुंटूर मार्केट यार्ड"))
        MarketItem(AppLang.t("🌾 Vijayawada Market Yard", "🌾 విజయవాడ మార్కెట్ యార్డ్", "🌾 विजयवाड़ा मार्केट यार्ड", "🌾 ವಿಜಯವಾಡ ಮಾರುಕಟ್ಟೆ ಯಾರ್ಡ್", "🌾 വിജയವಾಡ ಮಾರ್ಕಟ್ യാർഡ്", "🌾 विजयवाडा मार्केट यार्ड"))
        MarketItem(AppLang.t("🌾 Kurnool Market Yard", "🌾 కర్నూలు మార్కెట్ యార్డ్", "🌾 कुरनूल मार्केट यार्ड", "🌾 ಕರ್ನೂಲ್ ಮಾರುಕಟ್ಟೆ ಯಾರ್ಡ್", "🌾 കർനൂൾ മാർക്കറ്റ് യാർഡ്", "🌾 कुरनूल मार्केट यार्ड"))
    }
}

@Composable
fun MarketItem(name: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
        Text(name, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
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

        Text("⭐ ${AppLang.t("Feedback", "అభిప్రాయం", "प्रतिक्रिया", "అభీప్రాయ", "അഭിപ്രായം", "प्रतिसाद")}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(AppLang.t("Write feedback", "అభిప్రాయం రాయండి", "प्रतिक्रिया लिखें", "అభీప్రాయ బರೆಯಿರಿ", "അഭിപ്രായം രേഖപ്പെടുത്തുക", "प्रतिसाद लिहा")) }
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
        ) {
            Text(AppLang.t("Send Feedback", "అభిప్రాయాన్ని పంపండి", "प्रतिक्रिया भेजें", "అభీప్రాయ కಳುహించి", "അഭിപ്രായം അയക്കുക", "प्रतिसाद पाठवा"), color = Color.White)
        }
    }
}
