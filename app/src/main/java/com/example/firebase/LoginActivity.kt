package com.example.firebase
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
class LoginActivity : AppCompatActivity() {
    // Ініціалізація Firebase Authentication
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
// Отримуємо екземпляр Firebase Authentication
        auth = FirebaseAuth.getInstance()
// Перевіряємо, чи користувач уже ввійшов у систему
        if (auth.currentUser != null) {
            startActivity(Intent(this, ChatListActivity::class.java)) // Перехід до списку чатів
            finish() // Завершуємо активність входу
        }
        val emailInput = findViewById<EditText>(R.id.emailInput) // Поле для введення електронної пошти
        val passwordInput = findViewById<EditText>(R.id.passwordInput) // Поле для введення пароля
        val loginButton = findViewById<Button>(R.id.loginButton) // Кнопка входу
        val registerLink = findViewById<TextView>(R.id.registerLink) // Посилання для переходу до реєстрації
// Обробник натискання кнопки входу
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim() // Отримання та обрізання пробілів у пошті
            val password = passwordInput.text.toString().trim() // Отримання та обрізання пробілів у паролі
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password) // Викликаємо метод входу
            } else {

                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show()
            }
        }
// Обробник натискання на посилання реєстрації
        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java)) // Перехід до активності реєстрації
        }
    }
    // Метод для авторизації користувача
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Вхід успішний", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ChatListActivity::class.java)) // Перехід до списку чатів
                    finish() // Завершення активності входу
                } else {
                    Toast.makeText(this, "Помилка входу: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}