package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivitySetupProfileBinding;
import com.example.chatapplication.models.UserModel;
import com.example.chatapplication.utils.Credentials;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupProfileActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_GET_IMG = 10;
    ActivitySetupProfileBinding binding;
    Uri imageUri;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        binding.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GET_IMG);
            }
        });

        binding.setupProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = binding.nameTextView.getText().toString().trim();

                if(name.isEmpty()){
                    binding.nameTextView.setError("Please Enter a Name");
                    binding.nameTextView.requestFocus();
                    return;
                }
                binding.progressBar.setVisibility(View.VISIBLE);
                if(imageUri != null){
                // when the Image is present
                    StorageReference storageReference = storage.getReference()
                            .child(Credentials.STORAGE_REF_PROFILES)
                            .child(firebaseAuth.getUid());

                    storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(SetupProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();

                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        String userId = firebaseAuth.getUid();
                                        String userName = name;
                                        String phoneNo = firebaseAuth.getCurrentUser().getPhoneNumber();
                                        String imageUrlLink = uri.toString();

                                        UserModel user = new UserModel(userId,userName,phoneNo,imageUrlLink);

                                        signingInUser(userId, user);


                                    }
                                });

                            }
                            else{
                                Toast.makeText(SetupProfileActivity.this, "Task Failed.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
                else{
                    // Image is not Present
                    // but User should be created.

                    String userId = firebaseAuth.getUid();
                    String userName = name;
                    String phoneNo = firebaseAuth.getCurrentUser().getPhoneNumber();

                    UserModel user = new UserModel(userId,userName,phoneNo, null);

                    signingInUser(userId, user);
                }

            }
        });

 }

    private void signingInUser(String userId, UserModel user) {

        database.getReference()
                .child(Credentials.DATABASE_REF_USERS)
                .child(userId)
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            Intent intent = new Intent(SetupProfileActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            // Task is unsuccessful
                            Toast.makeText(SetupProfileActivity.this, "User is not created", Toast.LENGTH_SHORT).show();
                        }

                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_GET_IMG){
            if(data != null){
                if(data.getData() != null){
                    imageUri = data.getData();
                    binding.profileImageView.setImageURI(imageUri);
                }
            }
        }

    }
}