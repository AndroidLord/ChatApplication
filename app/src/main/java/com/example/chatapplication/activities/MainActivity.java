package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.adaptors.UsersAdaptor;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.models.MembersModel;
import com.example.chatapplication.models.UserModel;
import com.example.chatapplication.utils.Credentials;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements UsersAdaptor.OnUserItemClickListener {

    ActivityMainBinding binding;

    FirebaseDatabase database;
    ArrayList<UserModel> userModelArrayList;
    UserModel myUserData;

    private ActionMode actionMode;
    private UsersAdaptor usersAdaptor;
    private List<UserModel> selectedUserList = new ArrayList<>();
    private boolean isSelectedMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        database = FirebaseDatabase.getInstance();
        userModelArrayList = new ArrayList<>();

        usersAdaptor = new UsersAdaptor(MainActivity.this, userModelArrayList);
        binding.recyclerView.setAdapter(usersAdaptor);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersAdaptor.setOnUserItemClick(this);


        database.getReference()
                .child(Credentials.DATABASE_REF_USERS)
                .orderByChild(Credentials.DATABASE_REF_LAST_MSG_TIME)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        userModelArrayList.clear();

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {


                            UserModel userModel = snapshot1.getValue(UserModel.class);

                            if (!userModel.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                                userModelArrayList.add(userModel);
                            } else {
                                myUserData = userModel;
                            }
                        }
                        usersAdaptor.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        DatabaseReference groupRef = database.getReference()
                .child(Credentials.DATABASE_REF_GROUP_CHAT);

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<UserModel> userModels = new ArrayList<>();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                    DatabaseReference myGrpRef = snapshot1.getRef();
                    String senderID = FirebaseAuth.getInstance().getUid();


                    for (DataSnapshot snap : snapshot1.child(Credentials.DATABASE_REF_GROUP_CHAT_Members).getChildren()) {

                        if (Objects.equals(snap.getKey(), senderID)) {

                            String grpName = snapshot1.child(Credentials.DATABASE_REF_GROUP_CHAT_NAME).getValue(String.class);
                            String id = myGrpRef.getKey();

                            UserModel userModel = new UserModel(id, grpName, true,snapshot1.getKey());
                            userModels.add(userModel);
                        }
                    }
                }

                userModelArrayList.addAll(userModels);
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
        if (id == R.id.chats_menu) {
            Toast.makeText(this, "Opening Chat...", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.newGroup_menu) {
            Toast.makeText(this, "Creating New Group", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.search_menu) {
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.setting_menu) {
            Toast.makeText(this, "Setting", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        usersAdaptor.notifyDataSetChanged();

        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference()
                .child(Credentials.DATABASE_REF_PRESENCE)
                .child(currentId)
                .setValue("Online");


    }

    @Override
    protected void onPause() {
        super.onPause();

        String currentId = FirebaseAuth.getInstance().getUid();

        database.getReference()
                .child(Credentials.DATABASE_REF_PRESENCE)
                .child(currentId)
                .setValue("Offline");


    }

    ActionMode.Callback actionModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.select_menu, menu);


            // Disable the regular toolbar
            getSupportActionBar().hide();
            // getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

            // Show the action mode title
            if (!selectedUserList.isEmpty()) {
                mode.setTitle(selectedUserList.size() + " items selected");
            }


            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            if (item.getItemId() == R.id.delete_menu) {
                Toast.makeText(MainActivity.this, "Chat Deleted", Toast.LENGTH_SHORT).show();

                return true;
            }
            if (item.getItemId() == R.id.createGroup_menu) {

                Toast.makeText(MainActivity.this, "Creating Group", Toast.LENGTH_SHORT).show();

                List<UserModel> userModels = new ArrayList<>();
                String name = "";
                selectedUserList.add(new UserModel(myUserData.getUserId(), myUserData.getName()));


                for (UserModel userModel : selectedUserList) {
                    userModels.add(new UserModel(userModel.getUserId(), userModel.getName()));
                    name += userModel.getName();
                }

                if (selectedUserList.size() > 1) {

                    DatabaseReference groupReference = database.getReference()
                            .child(Credentials.DATABASE_REF_GROUP_CHAT)
                            .push();


                    for (int i = 0; i < selectedUserList.size(); i++) {

                        groupReference.child(Credentials.DATABASE_REF_GROUP_CHAT_Members)
                                .child(userModels.get(i).getUserId())
                                .setValue(userModels.get(i));
                    }

                    name +=" Group";
                    groupReference.child(Credentials.DATABASE_REF_GROUP_CHAT_NAME).setValue(name);

                    usersAdaptor.notifyDataSetChanged();

                    UserModel userModel = new UserModel(null,name,true,groupReference.getKey());
                    userModelArrayList.add(userModel);
                    usersAdaptor.notifyDataSetChanged();


                    selectedUserList.clear();
                    usersAdaptor.updateSelectedUserList(false);
                    updateActionModeTitle();
                    Intent intent = new Intent(MainActivity.this, GroupChatActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("groupId",groupReference.getKey());
                    startActivity(intent);


                } else {
                    Toast.makeText(MainActivity.this, "Select Two or more Members", Toast.LENGTH_SHORT).show();
                }


                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            actionMode.finish();
            selectedUserList.clear();
            usersAdaptor.updateSelectedUserList(false);
            usersAdaptor.notifyDataSetChanged();
            getSupportActionBar().show();
        }
    };

    private void updateActionModeTitle() {
        int size = selectedUserList.size();
        if (size == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(size + " items selected");
        }
    }

    @Override
    public void onItemClick(int position, boolean isSelectedMode) {

        this.isSelectedMode = isSelectedMode;


        if (actionMode == null) {
            selectedUserList.clear();
            actionMode = startActionMode(actionModeCallBack);
        }

        UserModel userModel = userModelArrayList.get(position);

//        if (isSelectedMode) {
//
//        }
        if (selectedUserList.contains(userModel)) {
            // remove it
            selectedUserList.remove(userModel);
            Log.d("select", "onClick: Unselected inside of Click(Adaptor)");
        } else {
            // add it
            selectedUserList.add(userModel);

            Log.d("select", "onClick: Selected inside of Click(Adaptor)");

        }
        if (selectedUserList.size() == 0) {
            isSelectedMode = false;

            selectedUserList.clear();
            usersAdaptor.updateSelectedUserList(false);
            usersAdaptor.notifyDataSetChanged();
            getSupportActionBar().show();

            Log.d("select", "onClick: No Selection inside of Click(Main)");
            // Toast.makeText(context, "No Selection", Toast.LENGTH_SHORT).show();
        }
        updateActionModeTitle();


    }

    @Override
    public void onLongItemClick(int position, boolean isSelectedMode) {

        Log.d("click", "onLongItemClick: Long Click!");
        this.isSelectedMode = isSelectedMode;

        if (actionMode == null) {
            selectedUserList.clear();
            Toast.makeText(this, "Action Mode Created", Toast.LENGTH_SHORT).show();
            actionMode = startActionMode(actionModeCallBack);
        }


        UserModel userModel = userModelArrayList.get(position);

        if (selectedUserList.contains(userModel)) {
            // contains userModel, so remove it
            selectedUserList.remove(userModel);
            Log.d("select", "onClick: UnSelected inside of LongClick(Adaptor)");
            //Toast.makeText(context, "Selected", Toast.LENGTH_SHORT).show();
        } else {
            // Doesn't contain userModel, so we add it
            selectedUserList.add(userModel);

            Log.d("select", "onClick: selected inside of LongClick(Main)");
            //Toast.makeText(context, "Unselected", Toast.LENGTH_SHORT).show();
        }

        if (selectedUserList.size() == 0) {

            isSelectedMode = false;

            selectedUserList.clear();
            usersAdaptor.updateSelectedUserList(false);
            usersAdaptor.notifyDataSetChanged();
            getSupportActionBar().show();

            Log.d("select", "onClick: No Selection inside of LongClick(Main)");
            //Toast.makeText(context, "No Selection", Toast.LENGTH_SHORT).show();
        }

        updateActionModeTitle();


    }

    @Override
    public void onBackPressed() {

        if (isSelectedMode) {

            selectedUserList.clear();
            usersAdaptor.updateSelectedUserList(false);
            usersAdaptor.notifyDataSetChanged();
            actionMode.finish();
            actionMode = null;
            getSupportActionBar().show();


        }

        super.onBackPressed();
    }
}