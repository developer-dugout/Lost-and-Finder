package com.coding.pixel.labboapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.concurrent.TimeUnit;

public class UserRegisterActivity extends AppCompatActivity {

    private Button ContactVerifyBtn, VerifyCodeBtn;
    private MaterialEditText ContactInput, CodeInput;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        mAuth = FirebaseAuth.getInstance();

        ContactVerifyBtn = findViewById(R.id.btn_send_verify_code);
        VerifyCodeBtn = findViewById(R.id.btn_verify_code);
        ContactInput = findViewById(R.id.contact_input);
        CodeInput = findViewById(R.id.verify_code_input);
        loadingBar = new ProgressDialog(this);

        ContactVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String PhoneNumber = ContactInput.getText().toString();
                if(TextUtils.isEmpty(PhoneNumber))
                {
                    Toast.makeText(UserRegisterActivity.this, "Contact is Required...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Contact Verification");
                    loadingBar.setMessage("Please wait, While we are authenticate your contact...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            PhoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            UserRegisterActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });
        VerifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactVerifyBtn.setVisibility(View.INVISIBLE);
                ContactInput.setVisibility(View.INVISIBLE);

                String verificationCode = CodeInput.getText().toString();
                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(UserRegisterActivity.this, "Please write your verify Code first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, While we are verification your code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(UserRegisterActivity.this, "Invalid Contact, Enter Correct Phone Number...", Toast.LENGTH_SHORT).show();
                ContactVerifyBtn.setVisibility(View.VISIBLE);
                ContactInput.setVisibility(View.VISIBLE);

                VerifyCodeBtn.setVisibility(View.INVISIBLE);
                CodeInput.setVisibility(View.INVISIBLE);
            }
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token)
            {
                loadingBar.dismiss();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(UserRegisterActivity.this, "Code has been sent, Please check it!", Toast.LENGTH_SHORT).show();
                ContactVerifyBtn.setVisibility(View.INVISIBLE);
                ContactInput.setVisibility(View.INVISIBLE);

                VerifyCodeBtn.setVisibility(View.VISIBLE);
                CodeInput.setVisibility(View.VISIBLE);
                // ...
            }
        };
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            SendUserToMainActivity();
        }
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            loadingBar.dismiss();
                            Toast.makeText(UserRegisterActivity.this, "Successfully Logged now, \n Complete your profile Info...", Toast.LENGTH_SHORT).show();
                            SendUserToProfileActivity();
                        }
                        else
                        {
                            // Sign in failed, display a message and update the UI
                            String message = task.getException().toString();
                            Toast.makeText(UserRegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            // The verification code entered was invalid
                        }
                    }
                });
    }
    private void SendUserToProfileActivity()
    {
        Intent regIntent = new Intent(UserRegisterActivity.this, UserProfileActivity.class);
        startActivity(regIntent);
        finish();
    }
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(UserRegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}