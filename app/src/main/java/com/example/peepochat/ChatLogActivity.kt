package com.example.peepochat

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView


class ChatLogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        findViewById<RecyclerView>(R.id.recyclerview_chatlog).adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

        listenForMessages()

        findViewById<Button>(R.id.sendbutton_chatlog).setOnClickListener {
            Log.d("All", "Attempting to send MSG")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if(chatMessage != null){
                    Log.d("All", chatMessage?.text.toString())

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }

                    findViewById<RecyclerView>(R.id.recyclerview_chatlog).scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long) {
        constructor() : this("", "", "", "", -1)
    }

    private fun performSendMessage() {
        val text = findViewById<EditText>(R.id.edittext_chatlog).text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user!!.uid

        if (fromId == null) return

        //val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/messages").push()
        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/user-messages/$fromId/$toId").push()

        val toRef = FirebaseDatabase.getInstance(databaseUrl).getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis()/1000)

        ref.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("All", "Chat message saved: ${ref.key}")
                findViewById<EditText>(R.id.edittext_chatlog).text.clear()
                findViewById<RecyclerView>(R.id.recyclerview_chatlog).scrollToPosition(adapter.itemCount - 1)
            }

        toRef.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance(databaseUrl).getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance(databaseUrl).getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }
}

class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.msg_from).text = text
        var uri = user.profileImageUrl
        var targetImageView = viewHolder.itemView.findViewById<CircleImageView>(R.id.pfp_from)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.msg_to).text = text
        var uri = user.profileImageUrl
        var targetImageView = viewHolder.itemView.findViewById<CircleImageView>(R.id.pfp_to)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}