package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.adaptors.MessageAdaptor;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.models.MessageModel;
import com.example.chatapplication.utils.Credentials;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    MessageAdaptor messageAdaptor;
    ArrayList<MessageModel> messageModelArrayList;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        String receiverId = getIntent().getStringExtra("receiverId");
        String senderId = FirebaseAuth.getInstance().getUid();

        messageModelArrayList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messageAdaptor = new MessageAdaptor(getApplicationContext(), messageModelArrayList);
        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewChat.setAdapter(messageAdaptor);

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        database.getReference()
                .child(Credentials.DATABASE_REF_CHATS)
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){

                            messageModelArrayList.clear();

                            Log.d("chat", "Getting the Data for List ");

                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                                MessageModel messageModel = snapshot1.getValue(MessageModel.class);
                                messageModelArrayList.add(messageModel);

                                Log.d("chat", " Data: " + messageModel.getMessage());

                            }
                            Log.d("chat", " Data has been added and list updated " + messageModelArrayList.get(0).getMessage());
                            messageAdaptor.notifyDataSetChanged();


                        }
                        else{
                            // snapshot is empty

                            Toast.makeText(ChatActivity.this, "Snapshot is empty", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = binding.messageEditText.getText().toString().trim();

                if (!message.isEmpty()) {

                    binding.messageEditText.setText("");

                    Log.d("chat", "onClick: Message is there: " + message);

                    MessageModel messageModel = new MessageModel(message, senderId, new Date().getTime());


                    HashMap<String, Object> map = new HashMap<>();
                    map.put(Credentials.DATABASE_REF_LAST_MSG,messageModel.getMessage());
                    map.put(Credentials.DATABASE_REF_LAST_MSG_TIME,messageModel.getTimeStamp());

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}