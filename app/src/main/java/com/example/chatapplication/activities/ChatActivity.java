package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.adaptors.GroupMessageAdaptor;
import com.example.chatapplication.adaptors.MessageAdaptor;
import com.example.chatapplication.databinding.ActivityChatBinding;
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

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    MessageAdaptor messageAdaptor;
    GroupMessageAdaptor groupMessageAdaptor;
    ArrayList<MessageModel> messageModelArrayList;

    String senderRoom, receiverRoom;
    Uri imageUri;

    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;

    String receiverId, senderId, profileImage;
    String groupId;
    boolean group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        String name = getIntent().getStringExtra("name");


        receiverId = getIntent().getStringExtra("receiverId");
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


            Glide.with(getApplicationContext())
                    .load(profileImage)
                    .placeholder(R.drawable.avatar)
                    .into(binding.profileImageIV);

            // telling Status
            database.getReference()
                    .child(Credentials.DATABASE_REF_PRESENCE)
                    .child(receiverId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                String status = snapshot.getValue(String.class);

                                if (status != null) {

                                    if (status.equals("Offline")) {
                                        binding.onlineOfflineStatus.setVisibility(View.GONE);
                                    } else {

                                        binding.onlineOfflineStatus.setText(status);
                                        binding.onlineOfflineStatus.setVisibility(View.VISIBLE);

                                    }
                                }

                            } else {
                                binding.onlineOfflineStatus.setVisibility(View.GONE);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            binding.onlineOfflineStatus.setVisibility(View.GONE);
                        }
                    });

            final Handler handler = new Handler();
            binding.messageEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    database.getReference()
                            .child(Credentials.DATABASE_REF_PRESENCE)
                            .child(senderId)
                            .setValue("Typing...");

                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(userStoppedTyping, 1000);


                }

                Runnable userStoppedTyping = new Runnable() {
                    @Override
                    public void run() {

                        database.getReference()
                                .child(Credentials.DATABASE_REF_PRESENCE)
                                .child(senderId)
                                .setValue("Online");

                    }
                };
            });



        binding.userName.setText(name);


        SettingUpAdaptorAndRecyclerView();



            // When user is Chatting with other user

            senderRoom = senderId + receiverId;
            receiverRoom = receiverId + senderId;

            // retrieving Text
            database.getReference()
                    .child(Credentials.DATABASE_REF_CHATS)
                    .child(senderRoom)
                    .child("messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {

                                messageModelArrayList.clear();

                                Log.d("chat", "Getting the Data for List ");

                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                                    MessageModel messageModel = snapshot1.getValue(MessageModel.class);
                                    messageModelArrayList.add(messageModel);

                                    Log.d("chat", " Data: " + messageModel.getMessage());

                                }
                                Log.d("chat", " Data has been added and list updated " + messageModelArrayList.get(0).getMessage());
                                messageAdaptor.notifyDataSetChanged();


                            } else {
                                // snapshot is empty


                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            // Sending Text
            binding.sendIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String message = binding.messageEditText.getText().toString().trim();

                    if (!message.isEmpty()) {

                        binding.messageEditText.setText("");

                        Log.d("chat", "onClick: Message is there: " + message);

                        MessageModel messageModel = new MessageModel(message, senderId, new Date().getTime());


                        HashMap<String, Object> map = new HashMap<>();
                        map.put(Credentials.DATABASE_REF_LAST_MSG, messageModel.getMessage());
                        map.put(Credentials.DATABASE_REF_LAST_MSG_TIME, messageModel.getTimeStamp());

                        database.getReference()
                                .child(Credentials.DATABASE_REF_CHATS)
                                .child(senderRoom)
                                .updateChildren(map);

                        database.getReference()
                                .child(Credentials.DATABASE_REF_CHATS)
                                .child(receiverRoom)
                                .updateChildren(map);


                        String RANDOM_KEY = database.getReference().push().getKey();

                        database.getReference()
                                .child(Credentials.DATABASE_REF_CHATS)
                                .child(senderRoom)
                                .child("messages")
                                .child(RANDOM_KEY)
                                .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Log.d("chat", " Inside the senderRoom ");

                                        database.getReference()
                                                .child(Credentials.DATABASE_REF_CHATS)
                                                .child(receiverRoom)
                                                .child("messages")
                                                .child(RANDOM_KEY)
                                                .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Log.d("chat", " Inside the receiverRoom ");

                                                    }
                                                });

                                    }
                                });

                    } else {
                        binding.messageEditText.requestFocus();
                    }


            };

        });


        binding.attachmentIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Credentials.REQUEST_CODE_ATTACHMENT);

            }
        });

    }

    private void SettingUpAdaptorAndRecyclerView() {
        if (group) {
            groupMessageAdaptor = new GroupMessageAdaptor(getApplicationContext(), messageModelArrayList);

        } else {
            messageAdaptor = new MessageAdaptor(getApplicationContext(), messageModelArrayList);
        }
        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewChat.setAdapter(messageAdaptor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Credentials.REQUEST_CODE_ATTACHMENT) {

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


        storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {


                            // Image has been successfull stored and url is with us
                            String imageUrl = uri.toString();
                            //String message = binding.messageEditText.getText().toString().trim();

                            if (!imageUrl.isEmpty()) {

                                //  binding.messageEditText.setText("");

                                Log.d("chat", "onClick: ImageUrl is there: " + imageUrl);

                                MessageModel messageModel = new MessageModel(null, senderId, new Date().getTime());
                                messageModel.setImageUrl(imageUrl);
                                messageModel.setSenderId(senderId);

                                HashMap<String, Object> map = new HashMap<>();
                                map.put(Credentials.DATABASE_REF_LAST_MSG, "Photo");
                                map.put(Credentials.DATABASE_REF_LAST_MSG_TIME, messageModel.getTimeStamp());

                                database.getReference()
                                        .child(Credentials.DATABASE_REF_CHATS)
                                        .child(senderRoom)
                                        .updateChildren(map);

                                database.getReference()
                                        .child(Credentials.DATABASE_REF_CHATS)
                                        .child(receiverRoom)
                                        .updateChildren(map);


                                String RANDOM_KEY = database.getReference().push().getKey();

                                database.getReference()
                                        .child(Credentials.DATABASE_REF_CHATS)
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(RANDOM_KEY)
                                        .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                Log.d("chat", " Inside the senderRoom ");

                                                database.getReference()
                                                        .child(Credentials.DATABASE_REF_CHATS)
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(RANDOM_KEY)
                                                        .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Log.d("chat", " Inside the receiverRoom ");

                                                            }
                                                        });

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
    protected void onPause() {
        super.onPause();

        String currentId = FirebaseAuth.getInstance().getUid();

        database.getReference()
                .child(Credentials.DATABASE_REF_PRESENCE)
                .child(currentId)
                .setValue("Offline");


    }


}