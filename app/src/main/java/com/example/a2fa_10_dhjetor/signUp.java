package com.example.a2fa_10_dhjetor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

import java.util.regex.Pattern;

public class signUp extends AppCompatActivity {

    private EditText editEmail;
    private EditText editName;
    private EditText editPassword;
    private EditText editConfirm;
    private Button signUp;
    private TextView logIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);


        editEmail = findViewById(R.id.email);
        editName = findViewById(R.id.name);
        editPassword = findViewById(R.id.password);
        editConfirm = findViewById(R.id.confirmPassword);
        signUp = findViewById(R.id.signUp);
        logIn = findViewById(R.id.logIn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signUp.setOnClickListener(view -> handleSignUp());

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signUp.this, Login.class);
                startActivity(intent);
            }
        });


    }

    private void handleSignUp(){
        String email = this.editEmail.getText().toString();
        String name = this.editName.getText().toString();
        String password = this.editPassword.getText().toString();
        String confirmPassword = this.editConfirm.getText().toString();

        String validateM = validateMessage(email, name, password, confirmPassword);
        if(validateM.isEmpty()){
            //Ruje ne DB

            DB db = new DB(this);
            db.signUp(new SignUpDto(
                    name, email, password
            ));
            showVerificationPopUp(db, email);
            db.close();

        }else{
            Toast.makeText(this, validateM, Toast.LENGTH_SHORT).show();
        }

    }

    private String validateMessage(String email, String name, String password, String confirmPassword){
        if(name.isEmpty()){
            return "Name cannot be empty";
        }
        if(email.isEmpty()){
            return "Email cannot be empty";
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return "Please enter a valid Email";
        }
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        if(!Pattern.matches(passwordPattern, password)){
            return "Invalid password.";
        }
        if(!confirmPassword.equals(password)){
            return "Confirm password must mach password";
        }
        DB db = new DB(this);

        if(db.checkEmail(email)){
            db.close();
            return "User with that email exists.";
        }

        return "";
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

        builder.setNegativeButton("Close", (dialogInterface, i) -> {
            dialogInterface.dismiss(); // Close the dialog
            Toast.makeText(this, "You closed the verification window.", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();



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
                Intent intent = new Intent(signUp.this, Login.class);
                startActivity(intent);


            } else {
                Toast.makeText(this, "Invalid code. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });


        new android.os.CountDownTimer(60000, 1000) { // 1 minute (60000 ms), tick every 1 second
            @Override
            public void onTick(long millisUntilFinished) {

                dialog.setMessage("Please enter the verification code sent to your email. This window will close in "
                        + (millisUntilFinished / 1000) + " seconds.");
            }

            @Override
            public void onFinish() {

                if (dialog.isShowing()) {
                    dialog.dismiss();
                    Toast.makeText(signUp.this, "Time is up! Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }.start();
    }
}