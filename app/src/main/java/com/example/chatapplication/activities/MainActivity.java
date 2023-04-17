package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.adaptors.UsersAdaptor;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.models.UserModel;
import com.example.chatapplication.utils.Credentials;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    FirebaseDatabase database;
    ArrayList<UserModel> userModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        database = FirebaseDatabase.getInstance();
        userModelArrayList = new ArrayList<>();

        UsersAdaptor usersAdaptor = new UsersAdaptor(MainActivity.this,userModelArrayList);
        binding.recyclerView.setAdapter(usersAdaptor);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        database.getReference()
                .child(Credentials.DATABASE_REF_USERS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        userModelArrayList.clear();
                        for(DataSnapshot snapshot1: snapshot.getChildren()){
                            userModelArrayList.add(snapshot1.getValue(UserModel.class));
                        }
                        usersAdaptor.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.chats_menu){
            Toast.makeText(this, "Opening Chat...", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.newGroup_menu){
            Toast.makeText(this, "Creating New Group", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.search_menu){
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.setting_menu){
            Toast.makeText(this, "Setting", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}