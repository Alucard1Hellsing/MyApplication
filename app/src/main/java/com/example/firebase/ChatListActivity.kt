package com.example.firebase
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
class ChatListActivity : AppCompatActivity() {
    // Змінні для UI елементів та Firebase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private val userList = mutableListOf<User>() // Список користувачів
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
// Ініціалізація Firebase
        auth = FirebaseAuth.getInstance()
// Підключення до Firebase Realtime Database
        val databaseUrl = "https://fir-46d42-default-rtdb.europe-west1.firebasedatabase.app/"
        database = FirebaseDatabase.getInstance(databaseUrl).reference
// Налаштування RecyclerView для відображення списку користувачів
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
// Ініціалізація адаптера для списку користувачів
        adapter = ChatListAdapter(userList) { username, userId ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("userId", userId) // Передаємо ID користувача у вікно чату
            startActivity(intent)
        }
        chatRecyclerView.adapter = adapter

// Ініціалізація меню налаштувань
        drawerLayout = findViewById(R.id.drawerLayout)
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val saveUsernameButton = findViewById<Button>(R.id.saveUsernameButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val editUsername = findViewById<EditText>(R.id.editUsername)
        val settingsMenu = findViewById<LinearLayout>(R.id.settingsMenu)
// Отримуємо налаштування застосунку
        val sharedPreferences = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE)
// Встановлюємо ширину меню у 70% ширини екрану
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val drawerWidth = (screenWidth * 0.7).toInt()
        settingsMenu.layoutParams.width = drawerWidth
// Обробка натискання на кнопку відкриття меню
        settingsButton.setOnClickListener {
            drawerLayout.openDrawer(settingsMenu)
        }
// Обробка натискання на кнопку збереження нового імені користувача
        saveUsernameButton.setOnClickListener {
            val newUsername = editUsername.text.toString().trim()
            if (newUsername.isNotEmpty()) {
                val userId = auth.currentUser?.uid
                userId?.let {
// Оновлюємо ім'я користувача у базі даних
                    database.child("users").child(it).child("username").setValue(newUsername)
                        .addOnSuccessListener {
// Зберігаємо ім'я локально
                            sharedPreferences.edit().putString("username", newUsername).apply()
                            Toast.makeText(this, "Ім'я користувача змінено!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Помилка збереження: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Будь ласка, введіть нове ім'я", Toast.LENGTH_SHORT).show()
            }
        }
// Обробка натискання на кнопку виходу з акаунту
        logoutButton.setOnClickListener {
            auth.signOut()
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
// Завантаження списку користувачів
        loadUsers()
    }
    // Функція для завантаження списку користувачів з бази даних
    private fun loadUsers() {
        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear() // Очищаємо список перед оновленням
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key // Отримуємо ID користувача
                    val user = userSnapshot.getValue(User::class.java) // Отримуємо дані користувача

                    if (user != null && userId != null) {
                        user.userId = userId // Присвоюємо отриманий ID об'єкту користувача
                        userList.add(user) // Додаємо користувача до списку
                    }
                }
                adapter.notifyDataSetChanged() // Оновлюємо UI після завантаження даних
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatListActivity, "Помилка завантаження користувачів", Toast.LENGTH_SHORT).show()
            }
        })
    }
}