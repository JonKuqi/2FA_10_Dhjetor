package com.example.a2fa_10_dhjetor;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import androidx.annotation.Nullable;


import org.mindrot.jbcrypt.BCrypt;

import java.util.Random;

public class DB extends SQLiteOpenHelper {
    private static final String DBNAME = "two_Factor6";


    public DB(@Nullable Context context) {
        super(context, DBNAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + "User" + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "email TEXT, " +
                "fullName TEXT, " +
                "passwordHash TEXT, " +
                "salt TEXT, " +
                "verificationCode TEXT) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }


    public boolean signUp(SignUpDto data){
        SQLiteDatabase db = this.getReadableDatabase();

        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(data.getPassword(), salt);

        ContentValues contentValues = new ContentValues();

        String verificationCode = generateVerificationCode();

        System.out.println(verificationCode);

        contentValues.put("email", data.getEmail());
        contentValues.put("fullName", data.getName());
        contentValues.put("passwordHash", hashedPassword);
        contentValues.put("salt", salt);
        contentValues.put("verificationCode", verificationCode);

        long result = db.insert("User", null, contentValues);
        db.close();
        if (result != -1) {
            EmailSender emailSender = new EmailSender();
            try {

                emailSender.sendOTPEmail(data.getEmail(),"Verification Code","Verification Code for Sign up: "+ verificationCode);

                Log.d("DB", "Inserting user with email: " + data.getEmail());
            }catch (Exception e){
                Log.e("EmailError", "Failed to send OTP email", e);

            }
            return true;
        }
        return false;

    }
    public String generateVerificationCode(){
        Random random = new Random();
        return Integer.toString(random.nextInt(999999));
    }




    public String getVerificationCode(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String verificationCode = null;

        Cursor cursor = db.rawQuery(
                "SELECT verificationCode FROM User WHERE email = ?",
                new String[]{email}
        );

        if (cursor != null && cursor.moveToFirst()) {
            verificationCode = cursor.getString(cursor.getColumnIndexOrThrow("verificationCode"));
        }

        if (cursor != null) {
            cursor.close();
        }
        return verificationCode;
    }

    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM User WHERE email=? and verificationCode = ?", new String[]{email, "0"});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }




    public void validateTheUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            db = this.getWritableDatabase(); // Use writable database for update
            db.beginTransaction(); // Start a transaction

            db.execSQL("UPDATE User SET verificationCode = ? WHERE email=?", new Object[]{"0",email});

            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.endTransaction(); // End the transaction
                db.close(); // Close the database to avoid memory leaks
            }
        }
    }

    public void logInUser(String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        String verificationCode = generateVerificationCode();


        db.execSQL("UPDATE User SET verificationCode = ? WHERE email=?", new Object[]{verificationCode, email});
        EmailSender emailSender = new EmailSender();
        emailSender.sendOTPEmail(email,"Verification Code for Log in",  verificationCode);

    }


    public boolean validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT passwordHash, salt FROM User WHERE email=? and verificationCode = ?", new String[]{email, "0"});

        if (cursor.moveToFirst()) {
            String storedHash = cursor.getString(0);
            cursor.close();
            return BCrypt.checkpw(password, storedHash); // Compare hashed passwords
        }
        cursor.close();
        return false;
    }
}
