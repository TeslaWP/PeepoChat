package com.example.peepochat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView


class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        //var recview = findViewById<RecyclerView>(R.id.recyclerview_newmessage)
        //val adapter = GroupAdapter<GroupieViewHolder>()
        //recview.adapter = adapter

        fetchUsers()
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    Log.d("All", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null){
                        adapter.add(UserItem(user))
                    }
                }
                var recview = findViewById<RecyclerView>(R.id.recyclerview_newmessage)

                adapter.setOnItemClickListener {item, view ->

                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()

                }
                recview.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.username_textview_newmessage).text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.pfp_imageview_newmessage))
    }

    override fun getLayout(): Int {
    return R.layout.user_row_new_message
    }

}