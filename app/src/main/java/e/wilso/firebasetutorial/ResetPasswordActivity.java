package e.wilso.firebasetutorial;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

   private EditText edEmail;
   private Button btnResetPassowrd;
   private Button btnBack;
   private FirebaseAuth mAuth = FirebaseAuth.getInstance();
   Intent intent;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_reset_password);

      findView();

      btnResetPassowrd.setOnClickListener(this);
      btnBack.setOnClickListener(this);
   }

   private void findView() {
      edEmail = findViewById(R.id.edt_reset_email);
      btnResetPassowrd = findViewById(R.id.btn_reset_password);
      btnBack = findViewById(R.id.btn_back);
   }

   @Override
   public void onClick(View view) {
      int i = view.getId();

      if(i == btnResetPassowrd.getId()) {
         // 返回此字符串的一個副本（開頭和結尾的空格去掉）或者這個字符串有前導或結尾空白格
         String email = edEmail.getText().toString().trim();

         if(TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter your email!", Toast.LENGTH_LONG).show();
            return;
         }

         mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()) {
                  Toast.makeText(ResetPasswordActivity.this, "Check email to reset your password!", Toast.LENGTH_SHORT).show();
               }
               else {
                  Toast.makeText(ResetPasswordActivity.this, "Fail to send reset password email!", Toast.LENGTH_SHORT).show();
               }
            }
         });
      }

      else if(i == btnBack.getId()) {
         ResetPasswordActivity.this.setResult(RESULT_OK, intent);
         ResetPasswordActivity.this.finish();
         //finish();
      }
   }
}
