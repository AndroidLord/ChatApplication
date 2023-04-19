package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivityVerifyOtpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyOtpActivity extends AppCompatActivity {

    ActivityVerifyOtpBinding binding;
    String inputOTP1,
            inputOTP2,
            inputOTP3,
            inputOTP4,
            inputOTP5,
            inputOTP6;


    FirebaseAuth firebaseAuth;

    private static String verificationId;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Message");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String phoneNo = "+91 " + getIntent().getStringExtra("mobile");

        binding.phoneLable.setText( phoneNo);

        binding.inputOTP1.requestFocus();
        moveOtpNumber();

        PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(phoneNo)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(VerifyOtpActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        progressDialog.dismiss();
                        verificationId = verifyId;
                    }
                }).build();


        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);


        binding.submitButtonVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                inputOTP1 = binding.inputOTP1.getText().toString();
                inputOTP2 = binding.inputOTP2.getText().toString();
                inputOTP3 = binding.inputOTP3.getText().toString();
                inputOTP4 = binding.inputOTP4.getText().toString();
                inputOTP5 = binding.inputOTP5.getText().toString();
                inputOTP6 = binding.inputOTP6.getText().toString();

                if (!inputOTP1.isEmpty() &&
                        !inputOTP2.isEmpty() &&
                        !inputOTP3.isEmpty() &&
                        !inputOTP4.isEmpty() &&
                        !inputOTP5.isEmpty() &&
                        !inputOTP6.isEmpty()){

                    String smsCode = inputOTP1.trim() +
                            inputOTP2.trim() +
                            inputOTP3.trim() +
                            inputOTP4.trim() +
                            inputOTP5.trim() +
                            inputOTP6.trim();

                    signingInWithCredential(smsCode);

                }
                else{

                    Toast.makeText(VerifyOtpActivity.this, "OTP not filled", Toast.LENGTH_SHORT).show();

                }

            }});

    }

    private void signingInWithCredential(String smsCode) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, smsCode);

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    Toast.makeText(VerifyOtpActivity.this, "Logged In.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(VerifyOtpActivity.this,SetupProfileActivity.class));
                    finishAffinity();

                }
                else{
                    Toast.makeText(VerifyOtpActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void moveOtpNumber() {

        binding.inputOTP1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().isEmpty()){
                    binding.inputOTP2.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.inputOTP2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().isEmpty()){
                    binding.inputOTP3.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.inputOTP3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().isEmpty()){
                    binding.inputOTP4.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.inputOTP4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().isEmpty()){
                    binding.inputOTP5.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.inputOTP5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().isEmpty()){
                    binding.inputOTP6.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

}