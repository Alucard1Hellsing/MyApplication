package com.example.firebase
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
class ChatActivity : AppCompatActivity() {
    // Оголошення змінних для елементів інтерфейсу та Firebase
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val messages = mutableListOf<Message>() // Список повідомлень
    private lateinit var chatId: String // Унікальний ідентифікатор чату
    private lateinit var username: String // Ім'я співрозмовника\
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        auth = FirebaseAuth.getInstance()
        val databaseUrl = "https://fir-46d42-default-rtdb.europe-west1.firebasedatabase.app/"
        database = FirebaseDatabase.getInstance(databaseUrl).reference
// Отримання даних про співрозмовника з попередньої активності
        username = intent.getStringExtra("username") ?: "Невідомий користувач"
        val otherUserId = intent.getStringExtra("userId")
// Перевірка коректності отриманих даних
        if (otherUserId.isNullOrEmpty()) {
            Log.e("ChatActivity", "OtherUserId is null or empty")
            Toast.makeText(this, "Помилка: Ідентифікатор іншого користувача не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return

        }
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("ChatActivity", "Користувач не авторизований")
            Toast.makeText(this, "Будь ласка, увійдіть у систему", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
// Формування унікального ідентифікатора чату
        chatId = if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
// Логування даних для відлагодження
        Log.d("ChatActivity", "CurrentUserId: $currentUserId")
        Log.d("ChatActivity", "OtherUserId: $otherUserId")
        Log.d("ChatActivity", "ChatId: $chatId")
// Ініціалізація елементів інтерфейсу
        findViewById<TextView>(R.id.chatTitle).text = "Чат з $username"
        chatRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
// Налаштування RecyclerView для відображення повідомлень
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messages)
        chatRecyclerView.adapter = messageAdapter
// Завантаження повідомлень з бази даних
        loadMessages()
// Обробка кнопки відправлення повідомлення
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            }
        }
// Обробка кнопки "назад"
        val goBack = findViewById<ImageButton>(R.id.goBack)
        goBack.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
            finish()
        }
    }
    // Метод для завантаження повідомлень з Firebase
    private fun loadMessages() {
        if (chatId.isNotEmpty()) {
            database.child("chats").child(chatId).child("messages")
                .orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messages.clear() // Очищення списку перед оновленням
                        for (messageSnapshot in snapshot.children) {
                            val message = messageSnapshot.getValue(Message::class.java)
                            message?.let { messages.add(it) }
                        }
                        messageAdapter.notifyDataSetChanged()
                        chatRecyclerView.scrollToPosition(messages.size - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatActivity", "Помилка завантаження повідомлень: ${error.message}")
                    }
                })
        }
    }
    // Метод для відправлення повідомлення
    private fun sendMessage(text: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null && chatId.isNotEmpty()) {
            val messageId = database.push().key
            val message = Message(
                senderId = currentUserId,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            messageId?.let {
                database.child("chats").child(chatId).child("messages").child(it)
                    .setValue(message)
                    .addOnSuccessListener {
                        Log.d("ChatActivity", "Повідомлення успішно надіслано")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatActivity", "Помилка надсилання повідомлення: ${e.message}")
                    }
            }
        } else {
            Log.e("ChatActivity", "Неможливо надіслати повідомлення: currentUserId або chatId порожні")
        }
    }
}