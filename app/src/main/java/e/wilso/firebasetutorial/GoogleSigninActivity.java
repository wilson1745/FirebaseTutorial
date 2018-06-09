package e.wilso.firebasetutorial;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSigninActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

   private static final String TAG = "JSAGoogleSignIn";
   // 一個常數 RC_SIGN_IN 做為回傳判別
   private static final int REQUEST_CODE_SIGN_IN = 1;

   private FirebaseAuth mAuth;

   private GoogleApiClient mGoogleApiClient;
   private TextView txtStatus;
   private TextView txtDetail;
   private Button btnsignout, btndisconnect;
   private LinearLayout layout_signout_discon;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_google_signin);

      findView();
      initialize();

      findViewById(R.id.btn_sign_in).setOnClickListener(this);
      btnsignout.setOnClickListener(this);
      btndisconnect.setOnClickListener(this);
   }

   private void findView() {
      txtStatus = findViewById(R.id.txtStatus);
      txtDetail = findViewById(R.id.txtDetail);

      btnsignout = findViewById(R.id.btn_sign_out);
      btndisconnect = findViewById(R.id.btn_disconnect);

      layout_signout_discon = findViewById(R.id.layout_sign_out_and_disconnect);
   }

   private void initialize() {
      GoogleSignInOptions gso = new GoogleSignInOptions
              .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              .requestIdToken(getString(R.string.default_web_client_id))
              .requestEmail()
              .build();

      mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
              .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                 @Override
                 public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Toast.makeText(GoogleSigninActivity.this, "Google", Toast.LENGTH_LONG).show();
                 }
              })
              .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
              .build();

      mAuth = FirebaseAuth.getInstance();
   }

   @Override
   protected void onStart() {
      super.onStart();
      // Check if user is signed in (non-null) and update UI accordingly.
      FirebaseUser currentuser = mAuth.getCurrentUser();
      updateUI(currentuser);
   }

   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      Log.e(TAG, "onConnectionFailed():" + connectionResult);
      Toast.makeText(getApplicationContext(), "Google Play Services error.", Toast.LENGTH_SHORT).show();
   }

   private void updateUI(FirebaseUser currentuser) {
      if(currentuser != null) {
         txtStatus.setText("Google User Email: " + currentuser.getEmail());
         txtDetail.setText("Firebase User ID: " + currentuser.getUid());

         findViewById(R.id.btn_sign_in).setVisibility(View.GONE);
         layout_signout_discon.setVisibility(View.VISIBLE);
      }
      else {
         txtStatus.setText("Signed Out");
         txtDetail.setText(null);

         findViewById(R.id.btn_sign_in).setVisibility(View.VISIBLE);
         layout_signout_discon.setVisibility(View.GONE);
      }
   }

   @Override
   public void onClick(View v) {
      int i = v.getId();

      if(i == R.id.btn_sign_in) {
         signIn();
      }
      else if(i == btnsignout.getId()) {
         signOut();
      }
      else if(i == btndisconnect.getId()) {
         revokeAccess();
      }
   }

   private void signIn() {
      Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
      startActivityForResult(intent, REQUEST_CODE_SIGN_IN);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      if (requestCode == REQUEST_CODE_SIGN_IN){
         GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
         if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            //取得使用者並試登入
            firebaseAuthWithGoogle(account);
         }
         else {
            // failed -> update UI
            updateUI(null);
            Toast.makeText(getApplicationContext(), "SignIn: failed!", Toast.LENGTH_SHORT).show();
         }
      }
   }

   private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
      Log.e(TAG, "firebaseAuthWithGoogle():" + account.getId());

      AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
      mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
         @Override
         public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()) {
               // Sign in success
               Log.e(TAG, "signInWithCredential: Success!");
               FirebaseUser user = mAuth.getCurrentUser();
               updateUI(user);
            }
            else {
               // Sign in fails
               Log.w(TAG, "signInWithCredential: Failed!", task.getException());
               Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_SHORT).show();
               updateUI(null);
            }
         }
      });
   }

   private void signOut() {
      // sign out Firebase
      mAuth.signOut();

      // sign out Google
      Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
         @Override
         public void onResult(@NonNull Status status) {
            updateUI(null);
         }
      });
   }

   private void revokeAccess() {
      // sign out Firebase
      mAuth.signOut();

      // revoke access Google
      Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
         @Override
         public void onResult(@NonNull Status status) {
            updateUI(null);
         }
      });
   }
}
