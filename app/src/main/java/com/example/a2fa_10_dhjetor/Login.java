package com.example.a2fa_10_dhjetor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {
    private EditText editEmail;
    private EditText editPassword;
    private Button logIn;
    private TextView signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.email);
        editPassword = findViewById(R.id.password);
        logIn = findViewById(R.id.logIn);
        signUp = findViewById(R.id.signUp);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logIn.setOnClickListener(view -> handleLogIn());

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, signUp.class);
                startActivity(intent);
            }
        });


    }
    private void handleLogIn(){

        String email = this.editEmail.getText().toString();
        String password = this.editPassword.getText().toString();


        DB db = new DB(this);
        boolean goodCredentials = db.validateUser(email, password);

        if(goodCredentials){
            Toast.makeText(this, "Your Credentials are correct. Please verify by email.", Toast.LENGTH_LONG).show();
            db.logInUser(email, password);
            showVerificationPopUp(db, email);

        }else{
            Toast.makeText(this, "Incorrect Credentials!", Toast.LENGTH_LONG).show();
        }

    }









    private void showVerificationPopUp(DB db, String theEmail) {

        EditText codeInput = new EditText(this);
        codeInput.setHint("Enter verification code");
        codeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registration Successful");
        builder.setMessage("Please enter the verification code sent to your email. This window will close in 1 minute.");
        builder.setView(codeInput);

        // Butoni per validim
        builder.setPositiveButton("Submit", null);

        builder.setNeutralButton("Resent", (dialogInterface, i) -> {
            String verificationCode = db.getVerificationCode(theEmail);
            EmailSender emailSender = new EmailSender();
            emailSender.sendOTPEmail(theEmail, "Resent Verification", "Your verification Code: "+ verificationCode);

        });

        builder.setNegativeButton("Close", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            Toast.makeText(this, "You closed the verification window.", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> {
            // Resend the verification code
            String verificationCode = db.getVerificationCode(theEmail);
            EmailSender emailSender = new EmailSender();
            emailSender.sendOTPEmail(theEmail, "Resent Verification", "Your verification Code: " + verificationCode);

            // Show confirmation message
            Toast.makeText(this, "Verification code resent to " + theEmail, Toast.LENGTH_SHORT).show();

            // Do not call dialog.dismiss() to keep the dialog open
        });

        // Override butonin per me kallxu kodin
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String code = codeInput.getText().toString();

            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "Code cannot be empty!", Toast.LENGTH_SHORT).show();
            } else if (code.equals(db.getVerificationCode(theEmail))) {
                db.validateTheUser(theEmail);
                Toast.makeText(this, "Code verified successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                //SUCCESS
                SESSION.setLoggedEmail(theEmail);
                Intent intent = new Intent(Login.this, signUp.class);
                startActivity(intent);

            } else {
                Toast.makeText(this, "Invalid code. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Start a countdown timer
        new android.os.CountDownTimer(60000, 1000) { // 1 minute (60000 ms), tick every 1 second
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the dialog message to show remaining time
                dialog.setMessage("Please enter the verification code sent to your email. This window will close in "
                        + (millisUntilFinished / 1000) + " seconds.");
            }

            @Override
            public void onFinish() {
                // Dismiss the dialog when the timer finishes
                if (dialog.isShowing()) {
                    dialog.dismiss();
                    Toast.makeText(Login.this, "Time is up! Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }.start();
    }





}