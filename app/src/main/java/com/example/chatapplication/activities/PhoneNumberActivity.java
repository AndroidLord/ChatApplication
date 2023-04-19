package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser != null){
                    Intent intent = new Intent(PhoneNumberActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    // There is no user
                }
            }
        };

        binding.phoneNoEditText.requestFocus();



        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!binding.phoneNoEditText.getText().toString().isEmpty()){

                    if(binding.phoneNoEditText.getText().toString().length()==10){

                        Toast.makeText(PhoneNumberActivity.this, "Sending OTP", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(PhoneNumberActivity.this,VerifyOtpActivity.class);
                        intent.putExtra("mobile",binding.phoneNoEditText.getText().toString().trim());

                        startActivity(intent);

                    }
                    else{
                        Toast.makeText(PhoneNumberActivity.this, "Enter a 10 Digit Phone Number", Toast.LENGTH_SHORT).show();
                        binding.phoneNoEditText.requestFocus();
                    }

                }
                else{
                    Toast.makeText(PhoneNumberActivity.this, "Please Enter The Phone No.", Toast.LENGTH_SHORT).show();
                }


            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

}