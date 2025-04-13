package com.example.firebase
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
class RegisterActivity : AppCompatActivity() {
    // Об'єкт для аутентифікації через Firebase
    private lateinit var auth: FirebaseAuth

    // Об'єкт для роботи з Realtime Database
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
// Ініціалізуємо Firebase Authentication
        auth = FirebaseAuth.getInstance()
// Підключаємо базу даних Firebase за вказаною URL-адресою
        val databaseUrl = "https://fir-46d42-default-rtdb.europe-west1.firebasedatabase.app/"
        database = FirebaseDatabase.getInstance(databaseUrl)
// Отримуємо елементи інтерфейсу

        val emailInput = findViewById<EditText>(R.id.emailInput) // Поле для введення email
        val passwordInput = findViewById<EditText>(R.id.passwordInput) // Поле для введення пароля
        val usernameInput =
            findViewById<EditText>(R.id.usernameInput) // Поле для введення імені користувача
        val registerButton = findViewById<Button>(R.id.registerButton) // Кнопка реєстрації
        val loginLink = findViewById<TextView>(R.id.loginLink) // Посилання для переходу на вхід
// Обробка натискання кнопки реєстрації
        registerButton.setOnClickListener {
            val email = emailInput.text.toString().trim() // Отримуємо email
            val password = passwordInput.text.toString().trim() // Отримуємо пароль
            val username = usernameInput.text.toString().trim() // Отримуємо ім'я користувача
            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                registerUser(email, password, username) // Викликаємо функцію реєстрації
            } else {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show()
            }
        }
// Обробка натискання на посилання входу
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)) // Перехід до активності входу
            finish() // Завершуємо цю активність
        }
    }

    // Функція реєстрації нового користувача
    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid // Отримуємо унікальний ID користувача
// Зберігаємо додаткові дані користувача у Firebase Realtime Database
                    userId?.let {
                        val userData = mapOf(
                            "username" to username,
                            "email" to email
                        )
                        database.reference.child("users").child(it)
                            .setValue(userData)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "Користувач успішно зареєстрований")
                                Toast.makeText(
                                    this,
                                    "Користувач зареєстрований",
                                    Toast.LENGTH_SHORT
                                ).show()
// Після успішної реєстрації переходимо до списку чатів
                                startActivity(Intent(this, ChatListActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterActivity", "Помилка збереження даних: ${e.message}")
                                Toast.makeText(
                                    this, "Помилка збереження даних: ${e.message}",

                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Log.e("RegisterActivity", "Помилка реєстрації: ${task.exception?.message}")
                    Toast.makeText(
                        this,
                        "Помилка реєстрації: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}