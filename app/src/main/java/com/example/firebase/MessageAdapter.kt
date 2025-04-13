package com.example.firebase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message, message.senderId == currentUserId)
    }
    override fun getItemCount(): Int = messages.size
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: Message, isCurrentUser: Boolean) {
// Встановлюємо ім'я відправника
            senderTextView.text = if (isCurrentUser) "Ви" else "Інший користувач"
// Встановлюємо текст повідомлення
            messageTextView.text = message.text
// Змінюємо вигляд повідомлення залежно від відправника
            if (isCurrentUser) {
                messageTextView.setBackgroundResource(R.drawable.message_sent_background)
                messageTextView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            } else {
                messageTextView.setBackgroundResource(R.drawable.message_received_background)
                messageTextView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            }
        }
    }
}