package com.coding.pixel.labboapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView postsList;
    private FirebaseDatabase user_db;
    private FirebaseUser cur_user;
    private DatabaseReference userdb_ref, post_ref, admin_watch_post_ref;
    private String checker="", myUrl, userid;
    private ProgressDialog loadingBar;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.admin_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pending Admin Posts");

        mAuth = FirebaseAuth.getInstance();
        user_db = FirebaseDatabase.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userdb_ref = user_db.getReference("Users");
        post_ref = user_db.getReference().child("Posts");
        admin_watch_post_ref = user_db.getReference().child("Admin Watch Posts");
        loadingBar = new ProgressDialog(this);

        postsList = findViewById(R.id.all_admin_posts);
        postsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        postsList.setLayoutManager(linearLayoutManager);

        DisplayAllAdminPosts();
    }
    private void DisplayAllAdminPosts()
    {
        Query postInOrder = admin_watch_post_ref.orderByChild("counter");

        FirebaseRecyclerOptions<UsersData> options=new FirebaseRecyclerOptions.Builder<UsersData>().setQuery(postInOrder,UsersData.class).build();
        final FirebaseRecyclerAdapter<UsersData, AdminActivity.PostsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<UsersData, AdminActivity.PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AdminActivity.PostsViewHolder holder, final int position, @NonNull UsersData model) {

                final String PostKey = getRef(position).getKey();

                holder.adminusername.setText(model.getName());
                holder.admintime.setText(" at " +model.getTime());
                holder.admindate.setText(" "+model.getDate());
                holder.admindescription.setText(model.getDescription());
                Picasso.get().load(model.getImage()).into(holder.admin_user_post_image);
                Picasso.get().load(model.getPostimage()).into(holder.adminPostImage);

                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        loadingBar.setTitle("Post Accepted");
                        loadingBar.setMessage("Please wait, while we are uploading your post...");
                        loadingBar.show();

                        Intent acceptedIntent = new Intent(AdminActivity.this, MainActivity.class);
                        acceptedIntent.putExtra("PostKey" , PostKey);
                        startActivity(acceptedIntent);
                    }
                });
                holder.declineBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        loadingBar.setTitle("Post Deleted");
                        loadingBar.setMessage("Your post in not valid, its been deleted form database...");
                        loadingBar.show();

                        Intent declinedIntent = new Intent(AdminActivity.this, AdminActivity.class);
                        declinedIntent.putExtra("PostKey" , PostKey);
                        startActivity(declinedIntent);

                        admin_watch_post_ref.child("Admin Watch Posts").orderByChild("uid").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot postsnapshot :dataSnapshot.getChildren())
                                {
                                    String key = postsnapshot.getKey();
                                    dataSnapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_watch_post_layout,parent,false);
                PostsViewHolder viewHolder=new AdminActivity.PostsViewHolder(view);
                return viewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        postsList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        TextView adminusername, admindate, admintime, admindescription;
        CircleImageView admin_user_post_image;
        ImageView adminPostImage;
        Button acceptBtn, declineBtn;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            adminusername=itemView.findViewById(R.id.admin_watch_post_user_name);
            admindate=itemView.findViewById(R.id.admin_watch_post_date);
            admintime=itemView.findViewById(R.id.admin_watch_post_time);
            admindescription=itemView.findViewById(R.id.admin_watch_post_description);
            adminPostImage=itemView.findViewById(R.id.admin_watch_post_image);
            admin_user_post_image=itemView.findViewById(R.id.admin_watch_user_profile_image);
            acceptBtn = itemView.findViewById(R.id.admin_watch_post_accept_btn);
            declineBtn = itemView.findViewById(R.id.admin_watch_post_decline_btn);

        }
    }
}
