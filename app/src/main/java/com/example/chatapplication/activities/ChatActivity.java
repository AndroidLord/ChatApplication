package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
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
import com.example.chatapplication.models.PdfModel;
import com.example.chatapplication.models.UserModel;
import com.example.chatapplication.utils.Credentials;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
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


        messageAdaptor = new MessageAdaptor(getApplicationContext(), messageModelArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        binding.recyclerViewChat.setLayoutManager(linearLayoutManager);

        binding.recyclerViewChat.setAdapter(messageAdaptor);


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

                    // ID, Message, Timestamp
                    MessageModel messageModel = new MessageModel(message, senderId, null, new Date().getTime());


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


            }

            ;

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

        binding.pdfImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select PDF File"),Credentials.REQUEST_CODE_ATTACHMENT_PDF);

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

        if(requestCode == Credentials.REQUEST_CODE_ATTACHMENT_PDF && resultCode==RESULT_OK){

            if(data!=null && data.getData()!=null){

                Uri uri = data.getData();

                // Getting the name of the pdf
                String uriString = uri.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();
                String displayName = null;

                if(uriString.startsWith("content://")){

                    Cursor cursor = null;
                    try{
                        cursor = this.getContentResolver().query(uri,null,null,null);

                        if(cursor!=null && cursor.moveToFirst()){
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }

                    }finally {
                        cursor.close();
                    }

                }
                else if(uriString.startsWith("file://")){
                    displayName = myFile.getName();
                }
                Toast.makeText(this, "PDF Name: " + displayName, Toast.LENGTH_SHORT).show();
                uploadPDF(uri,displayName);

            }

        }

    }

    private void uploadPDF(Uri data,String displayName) {

    final ProgressDialog pdfDialod = new ProgressDialog(this);
    pdfDialod.setTitle("Uploading PDF...");
    pdfDialod.show();

    final StorageReference reference = storage.getReference()
            .child("Pdfs")
            .child("uploads/"+System.currentTimeMillis()+".pdf");


    reference.putFile(data)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete());
                    Uri uri = uriTask.getResult();

                    pdfDialod.dismiss();

                    MessageModel messageModel = new MessageModel(null,senderId,null,new Date().getTime(),uri.toString(),displayName);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put(Credentials.DATABASE_REF_LAST_MSG, "PDF "+displayName);
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

                                    Log.d("chat", " Inside the senderRoom(PDF VERSION) ");

                                    database.getReference()
                                            .child(Credentials.DATABASE_REF_CHATS)
                                            .child(receiverRoom)
                                            .child("messages")
                                            .child(RANDOM_KEY)
                                            .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d("chat", " Inside the receiverRoom(PDF VERSION) ");

                                                }
                                            });

                                }
                            });



                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    float percent = (100 * snapshot.getBytesTransferred())/(snapshot.getTotalByteCount());
                    pdfDialod.setMessage("Uploaded: " + (int)percent+"%");

                }
            });

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

                                Log.d("chat", "onClick: ImageUrl is there: " + imageUrl);

                                MessageModel messageModel = new MessageModel(null, senderId, null, new Date().getTime());
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