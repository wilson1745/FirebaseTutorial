package e.wilso.firebasetutorial;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener {

   private static final String TAG = "FirebaseEmailPassword";

   private TextView txtStatus;
   private TextView txtDetail;
   private EditText edtEmail;
   private EditText edtPassword;
   private Button btnsignin, btnsignout, btncreateaccount, btnverify;

   private FirebaseAuth mAuth;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_authentication);

      findView();

      //------------------------------
      getSharedPreferences("login", MODE_PRIVATE);
      SharedPreferences setting = getSharedPreferences("login", MODE_PRIVATE);
      edtEmail.setText(setting.getString("USERID", ""));
      edtPassword.setText(setting.getString("PASS", ""));
      //------------------------------

      btnsignin.setOnClickListener(this);
      btnsignout.setOnClickListener(this);
      btncreateaccount.setOnClickListener(this);
      btnverify.setOnClickListener(this);

      mAuth = FirebaseAuth.getInstance();
   }

   private void findView() {
      txtStatus = findViewById(R.id.status);
      txtDetail = findViewById(R.id.detail);
      edtEmail = findViewById(R.id.edt_email);
      edtPassword = findViewById(R.id.edt_password);

      btnsignin = findViewById(R.id.btn_email_sign_in);
      btnsignout = findViewById(R.id.btn_sign_out);
      btncreateaccount = findViewById(R.id.btn_email_create_account);
      btnverify = findViewById(R.id.btn_verify_email);
   }

   @Override
   public void onClick(View view) {
      int i = view.getId();

      if(i == btnsignin.getId()) {
         signIn(edtEmail.getText().toString(), edtPassword.getText().toString());
      }
      else if(i == btnsignout.getId()) {
         signOut();
      }
      else if(i == btncreateaccount.getId()) {
         createAccount(edtEmail.getText().toString(), edtPassword.getText().toString());
      }
      else if(i == btnverify.getId()) {
         sendEmailVerification();
      }

   }

   private void signIn(String email, String password) {
      Log.e(TAG, "signIn: " + email);
      if(!validateForm(email, password)) {
         return;
      }

      //------------------------------
      SharedPreferences setting = getSharedPreferences("login", MODE_PRIVATE);
      setting.edit().putString("USERID", email).apply();
      setting.edit().putString("PASS", password).apply();
      //------------------------------

      // 記得先初始化 mAuth = FirebaseAuth.getInstance()
      mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
         @Override
         public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()) {
               Log.e(TAG, "signIn: Success!");

               // update UI with the signed-in user's information
               FirebaseUser user = mAuth.getCurrentUser();
               updateUI(user);
            }
            else {
               Log.e(TAG, "signIn: Fail!", task.getException());
               Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_SHORT).show();
               updateUI(null);
            }
         }
      });
   }

   private void signOut() {
      mAuth.signOut();
      updateUI(null);
   }

   private void createAccount(String email, String password) {
      Log.e(TAG, "createAccount:" + email);
      if(!validateForm(email, password)) {
         return;
      }

      // Firebase自動從資料庫新增該會員
      mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
         @Override
         public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()) {
               Log.e(TAG, "createAccount: Success!");

               // update UI with the signed-in user's information
               FirebaseUser user = mAuth.getCurrentUser();
               updateUI(user);
            }
            else {
               Log.e(TAG, "createAccount: Fail!", task.getException());
               Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_SHORT).show();
               updateUI(null);
            }
         }
      });
   }

   private void sendEmailVerification() {
      // Disable Verify Email button
      btnverify.setEnabled(false);

      final FirebaseUser user = mAuth.getCurrentUser();
      user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
         @Override
         public void onComplete(@NonNull Task<Void> task) {
            // Re-enable Verify Email button
            btnverify.setEnabled(true);

            if(task.isSuccessful()) {
               Toast.makeText(getApplicationContext(), "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
            }
            else {
               Log.e(TAG, "sendEmailVerification failed!", task.getException());
               Toast.makeText(getApplicationContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show();
            }
         }
      });
   }

   private void updateUI(FirebaseUser user) {
      if(user != null) {
         txtStatus.setText("User Email" + user.getEmail() + "(verified: " + user.isEmailVerified() + ")");
         txtDetail.setText("Firebase User ID: " + user.getUid());

         findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
         findViewById(R.id.email_password_fields).setVisibility(View.GONE);
         findViewById(R.id.layout_signed_in_buttons).setVisibility(View.VISIBLE);

         findViewById(R.id.btn_verify_email).setEnabled(!user.isEmailVerified());
      }
      else {
         txtStatus.setText("Signed Out");
         txtDetail.setText(null);

         findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
         findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
         findViewById(R.id.layout_signed_in_buttons).setVisibility(View.GONE);
      }
   }

   // java中，String類下的isEmpty() 返回的只是字符串的長度是否為0，如果字符串為null就會直接報空指針
   // android中，TextUtils.isEmpty(要判斷的字符串) 會對null和長度進行判斷，所以不會報空指針。
   private boolean validateForm(String email, String password) {
      if(TextUtils.isEmpty(email)) {
         Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
         return false;
      }
      if (TextUtils.isEmpty(password)) {
         Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
         return false;
      }
      if (password.length() < 6) {
         Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
         return false;
      }

      return true;
   }
}
