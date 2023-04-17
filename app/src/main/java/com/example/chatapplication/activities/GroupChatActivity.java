package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.chatapplication.R;
import com.example.chatapplication.adaptors.MessageAdaptor;
import com.example.chatapplication.databinding.ActivityGroupChatBinding;
import com.example.chatapplication.models.MessageModel;
import com.example.chatapplication.utils.Credentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;

    MessageAdaptor messageAdaptor;
    ArrayList<MessageModel> messageModelArrayList;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;


    String senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messageModelArrayList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        senderId = FirebaseAuth.getInstance().getUid();

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

                }


            }
        });


    }
}