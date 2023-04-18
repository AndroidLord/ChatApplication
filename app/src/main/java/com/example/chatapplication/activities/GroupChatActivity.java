package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.adaptors.GroupMessageAdaptor;
import com.example.chatapplication.databinding.ActivityGroupChatBinding;
import com.example.chatapplication.models.MessageModel;
import com.example.chatapplication.utils.Credentials;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;

    GroupMessageAdaptor groupMessageAdaptor;
    ArrayList<MessageModel> messageModelArrayList;

    String senderRoom, receiverRoom;
    Uri imageUri;

    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;

    String profileImage, groupId, senderId, senderName;
    boolean group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");


        groupId = getIntent().getStringExtra("groupId");
        profileImage = getIntent().getStringExtra("profileImage");

        senderId = FirebaseAuth.getInstance().getUid();

        messageModelArrayList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        binding.backArrowIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.profileImageIV.setVisibility(View.GONE);


//            Glide.with(getApplicationContext())
//                    .load(profileImage)
//                    .placeholder(R.drawable.avatar)
//                    .into(binding.profileImageIV);


        binding.userName.setText(name);


        groupMessageAdaptor = new GroupMessageAdaptor(getApplicationContext(), messageModelArrayList);
        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewChat.setAdapter(groupMessageAdaptor);


        // Two Things will be done here?
        // 1) Retrive the Data
        // 2) Send the Data, when pressed
        // Remember to work on Attachment for Group, as it will cause error

        // Point (1) Completed, Retrieve Message
        database.getReference()
                .child(Credentials.DATABASE_REF_GROUP_CHAT)
                .child(groupId)
                .child(Credentials.DATABASE_REF_GROUP_CHAT_MESSAGES)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            // Data is there in Group Chat
                            messageModelArrayList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                                messageModelArrayList.add(messageModel);
                                Log.d("group", "onDataChange: " + messageModel.getSenderName() + "\n Message: " + messageModel.getMessage());
                            }
                            groupMessageAdaptor.notifyDataSetChanged();
                        } else {
                            // Chat Group is empty
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Point (2) Completed, Send Message
        binding.sendIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = binding.messageEditText.getText().toString().trim();

                if (!message.isEmpty()) {
                    binding.messageEditText.setText("");

                    Log.d("chat", "onClick: Message is there: " + message);

                    database.getReference()
                            .child(Credentials.DATABASE_REF_USERS)
                            .child(senderId)
                            .child("name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String senderName = snapshot.getValue(String.class);
                                    Log.d("group", "onDataChange: SenderName: " + senderName);

                                    MessageModel messageModel = new MessageModel(message, senderId, senderName, new Date().getTime());


                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put(Credentials.DATABASE_REF_LAST_MSG, messageModel.getMessage());
                                    map.put(Credentials.DATABASE_REF_LAST_MSG_TIME, messageModel.getTimeStamp());

                                    database.getReference()
                                            .child(Credentials.DATABASE_REF_GROUP_CHAT)
                                            .child(groupId)
                                            .updateChildren(map);

                                    String RANDOM_KEY = database.getReference().push().getKey();

                                    database.getReference()
                                            .child(Credentials.DATABASE_REF_GROUP_CHAT)
                                            .child(groupId)
                                            .child(Credentials.DATABASE_REF_GROUP_CHAT_MESSAGES)
                                            .child(RANDOM_KEY)
                                            .setValue(messageModel);

                                    groupMessageAdaptor.notifyDataSetChanged();


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                } else {
                    // if the text is empty
                    Toast.makeText(GroupChatActivity.this, "Please Enter a message", Toast.LENGTH_SHORT).show();
                }


            }
        });

        binding.attachmentIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Credentials.REQUEST_CODE_ATTACHMENT_PHOTO);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Credentials.REQUEST_CODE_ATTACHMENT_PHOTO) {

            if (data != null) {

                if (data.getData() != null) {

                    imageUri = data.getData();
                    photoAttachment();
                }

            }

        }

    }

    private void photoAttachment() {

        Calendar calendar = Calendar.getInstance();

        StorageReference storageReference = storage.getReference()
                .child(Credentials.DATABASE_REF_CHATS)
                .child(calendar.getTimeInMillis() + "");

        dialog.show();
        storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {


                            // Image has been successfull stored and url is with us
                            String imageUrl = uri.toString();
                            //String message = binding.messageEditText.getText().toString().trim();

                            if (!imageUrl.isEmpty()) {

                                //  binding.messageEditText.setText("");

                                database.getReference()
                                        .child(Credentials.DATABASE_REF_USERS)
                                        .child(senderId)
                                        .child("name")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                senderName = snapshot.getValue(String.class);

                                                Log.d("group", "onClick: ImageUrl is there: " + imageUrl);


                                                MessageModel messageModel = new MessageModel(null, senderId, senderName, new Date().getTime());
                                                messageModel.setImageUrl(imageUrl);

                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put(Credentials.DATABASE_REF_LAST_MSG, "Photo");
                                                map.put(Credentials.DATABASE_REF_LAST_MSG_TIME, messageModel.getTimeStamp());

                                                database.getReference()
                                                        .child(Credentials.DATABASE_REF_GROUP_CHAT)
                                                        .child(groupId)
                                                        .updateChildren(map);


                                                String RANDOM_KEY = database.getReference().push().getKey();

                                                database.getReference()
                                                        .child(Credentials.DATABASE_REF_GROUP_CHAT)
                                                        .child(groupId)
                                                        .child(Credentials.DATABASE_REF_GROUP_CHAT_MESSAGES)
                                                        .child(RANDOM_KEY)
                                                        .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                                Log.d("chat", " Inside the senderRoom ");


                                                            }
                                                        });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            } else {
                                binding.messageEditText.requestFocus();
                            }

                        }
                    });

                }

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        finish();
    }
}