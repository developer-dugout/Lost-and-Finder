package com.coding.pixel.labboapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class CommentsActivity extends AppCompatActivity {

    private RecyclerView comments_List;
    private ImageButton post_Comment_Btn;
    private MaterialEditText comments_Input;
    private DatabaseReference UserRef, PostRef;
    private FirebaseAuth mAuth;
    private String Post_Key, current_User_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar = findViewById(R.id.commentBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Comments");

        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_User_Id = mAuth.getCurrentUser().getUid();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        comments_List = findViewById(R.id.comments_list);
        comments_List.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        comments_List.setLayoutManager(linearLayoutManager);

        post_Comment_Btn = findViewById(R.id.post_comment_btn);
        comments_Input = findViewById(R.id.comments_input);

        post_Comment_Btn.setOnClickListener(new View.OnClickListener() {
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

                            CommentValidation(userName);
                            comments_Input.setText("");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
                FirebaseRecyclerOptions.Builder<Comments>().setQuery(PostRef, Comments.class).build();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> adapter = new
                FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model)
            {
                holder.name.setText(model.getName());
                holder.comment.setText(model.getComment());
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());
            }
            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout, viewGroup, false);
                CommentsViewHolder viewHolder = new CommentsViewHolder(view);
                return viewHolder;
            }
        };
        comments_List.setAdapter(adapter);
        adapter.startListening();
    }
    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView comment, date, time, name;
        public CommentsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.comments_username);
            comment = itemView.findViewById(R.id.comment_text);
            date = itemView.findViewById(R.id.comment_date);
            time = itemView.findViewById(R.id.comment_time);
        }
    }
    private void CommentValidation(String userName) {
        String commentText = comments_Input.getText().toString();
        Query query = PostRef.orderByChild("time").equalTo(comments_Input.getText().toString());
        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please Write Something first...", Toast.LENGTH_SHORT).show();
        }
        else
        {
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

            PostRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(CommentsActivity.this, "You have Commented Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(CommentsActivity.this, "Error occurred here...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
