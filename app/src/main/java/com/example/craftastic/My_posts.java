package com.example.craftastic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class My_posts extends AppCompatActivity implements DeleteDialog.saveDeleteListener{
    ImageView homeIcon, favoritesIcon, adsIcon, accountIcon;
    FloatingActionButton postBtn;
    ArrayList<Craft> crafts;
    RecyclerView craftRecyclerView;
    MyPostsAdapter adapter;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        if (LoginRedirecting.redirectToLogin(this)) {
            finish();
            return;
        }
        initLayouts();
        Navigation.initNavButtons(homeIcon, favoritesIcon, postBtn, adsIcon, accountIcon, this);
        initRecyclerView();
    }

    private void initLayouts(){
        adsIcon = findViewById(R.id.adsIcon);
        adsIcon.setColorFilter(ContextCompat.getColor(this, R.color.pink));
        adsIcon.setEnabled(false);
        postBtn = findViewById(R.id.postBtn);
        accountIcon = findViewById(R.id.accountIcon);
        homeIcon = findViewById(R.id.homeIcon);
        craftRecyclerView = findViewById(R.id.myPostsList);
        favoritesIcon = findViewById(R.id.favoritesIcon);
    }

    private void initRecyclerView() {
        try {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            CraftsDataSource ds = new CraftsDataSource(this);
            ds.open();
            crafts = ds.getUserPostedCrafts(currentUser.getUid());
            RecyclerView.LayoutManager layout= new LinearLayoutManager(this);
            craftRecyclerView.setLayoutManager(layout);
            adapter = new MyPostsAdapter(crafts, this);
            initAdapterOnClickListener(adapter);
            craftRecyclerView.setAdapter(adapter);
        }catch (Exception e){
            Toast.makeText(this, "An error occured when retrieving data", Toast.LENGTH_LONG).show();
        }
    }

    private void initAdapterOnClickListener(MyPostsAdapter adapter){
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyPostsAdapter.MyPostViewHolder holder = (MyPostsAdapter.MyPostViewHolder) v.getTag();
                int position = holder.getAdapterPosition();
                Intent intent = new Intent(My_posts.this, PostDetails.class);
                int craftId = crafts.get(position).getCraftId();
                intent.putExtra("craftId", craftId);
                intent.putExtra("LAST_VISITED_ACTIVITY", My_posts.class.getSimpleName());
                startActivity(intent);
            }
        });
    }


    public void deleteCraft(int position) {
        FragmentManager fm = getSupportFragmentManager();
        DeleteDialog dialog = new DeleteDialog(position);
        dialog.show(fm, "Delete Dialog");
    }



    @Override
    public void didFinishDeleteDialog(boolean choice, int position) {
        if(choice){
            Craft craft = crafts.get(position);
            CraftsDataSource ds = new CraftsDataSource(this);
            try {
                ds.open();
                boolean didDelete = ds.deleteCraft(craft.getCraftId());
                ds.close();
                if(didDelete){
                    crafts.remove(position);
                    adapter.notifyItemRemoved(position);
                }
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void editCraft(int position) {
        Intent intent = new Intent(this, PostCraft.class);
        intent.putExtra("CRAFT_ID", crafts.get(position).getCraftId());
        intent.putExtra("LAST_ACTIVITY", My_posts.class.getSimpleName());
        startActivity(intent);
    }


}