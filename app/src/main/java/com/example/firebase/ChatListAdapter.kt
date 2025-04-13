package com.example.firebase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ChatListAdapter(
    private var userList: List<User>,
    private val onItemClick: (String, String) -> Unit // Додаємо userId як другий параметр
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ChatViewHolder(view)
    }
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
        holder.itemView.setOnClickListener {
            onItemClick(user.username, user.userId) // Передаємо ім'я та ідентифікатор користувача
        }
    }
    override fun getItemCount(): Int = userList.size
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        fun bind(user: User) {
            usernameTextView.text = user.username
        }
    }
}