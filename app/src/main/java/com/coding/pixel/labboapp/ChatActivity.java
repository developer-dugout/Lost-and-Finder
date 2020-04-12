package com.coding.pixel.labboapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView Chat_Msg_List;
    private ImageButton post_Chat_Msg_Btn;
    private MaterialEditText Chat_Msg_Input;
    private DatabaseReference UserRef, ChatRef;
    private FirebaseAuth mAuth;
    private String Post_Key, current_User_Id;
    private long countPost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.chatBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Chat Activity");

        //Post_Key = getIntent().getExtras().get("PostKey").toString();
        mAuth = FirebaseAuth.getInstance();
        current_User_Id = mAuth.getCurrentUser().getUid();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRef = FirebaseDatabase.getInstance().getReference().child("Chat Box");

        Chat_Msg_List = findViewById(R.id.chat_msg_list);
        Chat_Msg_List.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        Chat_Msg_List.setLayoutManager(linearLayoutManager);

        post_Chat_Msg_Btn = findViewById(R.id.post_chat_btn);
        Chat_Msg_Input = findViewById(R.id.chat_msg_input);

        post_Chat_Msg_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UserRef.child(current_User_Id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("name").getValue().toString();
                            ChatMsgValidation(userName);
                            Chat_Msg_Input.setText("");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                    }
                });
            }
        });
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Comments> options = new
                FirebaseRecyclerOptions.Builder<Comments>().setQuery(ChatRef, Comments.class).build();
        FirebaseRecyclerAdapter<Comments, ChatActivity.CommentsViewHolder> adapter = new
                FirebaseRecyclerAdapter<Comments, ChatActivity.CommentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatActivity.CommentsViewHolder holder, int position, @NonNull Comments model) {
                        holder.name.setText(model.getName());
                        holder.comment.setText(model.getComment());
                        holder.date.setText(model.getDate());
                        holder.time.setText(model.getTime());
                    }
                    @NonNull
                    @Override
                    public ChatActivity.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout, viewGroup, false);
                        ChatActivity.CommentsViewHolder viewHolder = new ChatActivity.CommentsViewHolder(view);
                        return viewHolder;
                    }
                };
        Chat_Msg_List.setAdapter(adapter);
        adapter.startListening();
    }
    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView comment, date, time, name;
        public CommentsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.chat_username);
            comment = itemView.findViewById(R.id.chat_text);
            date = itemView.findViewById(R.id.chat_date);
            time = itemView.findViewById(R.id.chat_time);
        }
    }
    private void ChatMsgValidation(String userName)
    {
        String commentText = Chat_Msg_Input.getText().toString();
        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please Write Something first...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ChatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.exists())
                    {
                        countPost = dataSnapshot.getChildrenCount();
                    }
                    else
                    {
                        countPost = 0;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordTime.getTime());

            final String RandomKey = current_User_Id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", current_User_Id);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("name", userName);
            commentsMap.put("counter", countPost);
            ChatRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(ChatActivity.this, "You have Commented Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(ChatActivity.this, "Error occurred here...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}