 package com.coding.pixel.labboapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private CircleImageView NavProfileView;
    private TextView getUserName;
    private TextView getUserStatus;
    private RecyclerView postList;
    private ImageButton addPostImage;
    private FirebaseDatabase user_db;
    private FirebaseUser cur_user;
    private DatabaseReference userdb_ref, post_ref, admin_watch_post_ref, likesRef;
    private String checker="", myUrl, userid;
    String currentUserId;
    Boolean likesChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar =  findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        user_db = FirebaseDatabase.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userdb_ref = user_db.getReference("Users");
        post_ref = user_db.getReference().child("Posts");
        admin_watch_post_ref = user_db.getReference().child("Admin Watch Posts");
        likesRef = user_db.getReference().child("Likes");

        navigationView = findViewById(R.id.nav_view);
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        postList = findViewById(R.id.all_user_posts);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        postList.setLayoutManager(linearLayoutManager);

        NavigationView navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        getUserName = header.findViewById(R.id.UserNameView);
        getUserStatus = header.findViewById(R.id.UserStatusView);
        NavProfileView = header.findViewById(R.id.nav_profile);
        addPostImage = findViewById(R.id.post_image_button);

        userdb_ref.child(currentUserId).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
        {
            if (dataSnapshot.exists())
            {

                if (dataSnapshot.hasChild("name"))
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    getUserName.setText(userName);
                }
                if (dataSnapshot.hasChild("status"))
                {
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    getUserStatus.setText(userStatus);
                }
                if (dataSnapshot.hasChild("image"))
                {
                    String userProfileImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(userProfileImage).into(NavProfileView);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Profile is not Exists...", Toast.LENGTH_SHORT).show();
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError)
        {
        }
    });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            UserMenuSelector(menuItem);
            return false;
        }
    });
        addPostImage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            CharSequence options[] = new CharSequence[]
                    {
                            "Finding",
                            "Returning"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Create Post For");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    if (i == 0)
                    {
                        Intent findIntent = new Intent(MainActivity.this, PostActivity.class);
                        startActivity(findIntent);
                    }
                    if (i == 1)
                    {
                        Intent returnIntent = new Intent(MainActivity.this, AdminWatchPostActivity.class);
                        startActivity(returnIntent);
                    }
                }
            });
            builder.show();
        }
    });
    DisplayAllUsersPosts();
}
    private void DisplayAllUsersPosts() {

        Query postInOrder = post_ref.orderByChild("counter");

        FirebaseRecyclerOptions<UsersData> options=new FirebaseRecyclerOptions.Builder<UsersData>().setQuery(postInOrder,UsersData.class).build();
        final FirebaseRecyclerAdapter<UsersData, PostsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<UsersData, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull UsersData model) {

                final String PostKey = getRef(position).getKey();

                holder.username.setText(model.getName());
                holder.time.setText(" at " +model.getTime());
                holder.date.setText(" "+model.getDate());
                holder.description.setText(model.getDescription());
                Picasso.get().load(model.getImage()).into(holder.user_post_image);
                Picasso.get().load(model.getPostimage()).into(holder.postImage);

                holder.setLikeButtonStatus(PostKey);

                holder.likePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        likesChecker = true;

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (likesChecker.equals(true))
                                {
                                    if (dataSnapshot.child(PostKey).hasChild(currentUserId))
                                    {
                                        likesRef.child(PostKey).child(currentUserId).removeValue();
                                        likesChecker = false;
                                    }
                                    else
                                    {
                                        likesRef.child(PostKey).child(currentUserId).setValue(true);
                                        likesChecker = false;
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                holder.commentPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentIntent.putExtra("PostKey" , PostKey);
                        startActivity(commentIntent);
                    }
                });
            }
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                PostsViewHolder viewHolder=new PostsViewHolder(view);
                return viewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        postList.setAdapter(firebaseRecyclerAdapter);
    }
 public static class PostsViewHolder extends RecyclerView.ViewHolder{
     TextView username,date,time,description, noOfLikes;
     CircleImageView user_post_image;
     ImageView postImage;
     ImageButton likePost, commentPost;
     int countsLikes;
     String currentUserID;
     DatabaseReference LikesRef;

     public PostsViewHolder(View itemView) {
         super(itemView);
         username=itemView.findViewById(R.id.post_user_name);
         date=itemView.findViewById(R.id.post_date);
         time=itemView.findViewById(R.id.post_time);
         description=itemView.findViewById(R.id.post_description);
         postImage=itemView.findViewById(R.id.post_image);
         user_post_image=itemView.findViewById(R.id.user_profile_image);
         likePost = itemView.findViewById(R.id.like_btn);
         commentPost = itemView.findViewById(R.id.comment_btn);
         noOfLikes = itemView.findViewById(R.id.display_no_of_likes);

         LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
         currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
     }

     public void setLikeButtonStatus(final String PostKey)
     {
         LikesRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot)
             {
                 if (dataSnapshot.child(PostKey).hasChild(currentUserID))
                 {
                     countsLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                     likePost.setImageResource(R.drawable.like);
                     noOfLikes.setText(Integer.toString(countsLikes)+(" Likes"));
                 }
                 else
                 {
                     countsLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                     likePost.setImageResource(R.drawable.dislike);
                     noOfLikes.setText(Integer.toString(countsLikes)+(" Likes"));
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
     }
 }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.donateinfo) {
            Intent uniInfo =new Intent(MainActivity.this, UniInfoActivity.class);
            startActivity(uniInfo);
        }
        if (id == R.id.devinfo) {
            Intent uniInfo =new Intent(MainActivity.this, AboutActivity.class);
            startActivity(uniInfo);
        }
        return super.onOptionsItemSelected(item);
    }
    private void UserMenuSelector(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.home:
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                break;
            case R.id.userprofile:
                Intent profileIntent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(profileIntent);
                break;
            case R.id.user_achiev:
                adminAccessUserPendingPost();
                break;
            case R.id.user_messages:
                Intent messageIntent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(messageIntent);
                break;
            case R.id.feedback:
                Intent feedbackIntent = new Intent(MainActivity.this, RatingActivity.class);
                startActivity(feedbackIntent);
                break;
            case R.id.logout:
                mAuth.signOut();
                Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(logoutIntent);
                break;
        }
    }

    private void adminAccessUserPendingPost()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Admin Access Activity :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("Right Password...");
        groupNameField.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(groupNameField);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                    Intent forwardMainIntent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(forwardMainIntent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Intent backMainIntent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(backMainIntent);
            }
        });

        builder.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture)
    {
    }
    private void SendUserToPostActivity()
    {
        startActivity(new Intent(MainActivity.this, PostActivity.class));
    }
}