package e.wilso.firebasetutorial;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import e.wilso.firebasetutorial.Module.Message;
import e.wilso.firebasetutorial.Module.User;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

   private static final String TAG = "MessageActivity";
   private static final String REQUIRED = "Required";

   private Button btnBack;
   private Button btnSend;
   private EditText edtSentText;
   private TextView tvAuthor;
   private TextView tvTime;
   private TextView tvBody;

   private FirebaseUser user;

   private DatabaseReference mDatabase;
   private DatabaseReference mMessageReference;
   //private ValueEventListener mMessageListener;
   private ChildEventListener mMessageListener;

   private ArrayList<Message> messagesList = new ArrayList<>();

   Intent intent;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_message);

      findView();
      initialize();

      btnSend.setOnClickListener(this);
      btnBack.setOnClickListener(this);
   }

   private void findView() {
      btnSend = findViewById(R.id.btn_send);
      btnBack = findViewById(R.id.btn_back);
      edtSentText = findViewById(R.id.edt_sent_text);
      tvAuthor = findViewById(R.id.tv_author);
      tvTime = findViewById(R.id.tv_time);
      tvBody = findViewById(R.id.tv_body);
   }

   private void initialize() {
      mDatabase = FirebaseDatabase.getInstance().getReference();
      mMessageReference = FirebaseDatabase.getInstance().getReference("message");
      user = FirebaseAuth.getInstance().getCurrentUser();

      tvAuthor.setText("");
      tvTime.setText("");
      tvBody.setText("");
   }

   @Override
   public void onClick(View v) {
      int i = v.getId();

      if(i == btnSend.getId()) {
         submitMessage();
         edtSentText.setText("");
      }
      else if(i == btnBack.getId()) {
         MessageActivity.this.setResult(RESULT_OK, intent);
         MessageActivity.this.finish();
      }
   }

   @Override
   protected void onStart() {
      super.onStart();

      ChildEventListener childEventListener = new ChildEventListener() {
         @Override
         public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // A new message has been added
            // onChildAdded() will be called for each node at the first time
            Message message = dataSnapshot.getValue(Message.class);
            messagesList.add(message);

            Log.e(TAG, "onChildAdded:" + message.body);

            Message latest = messagesList.get(messagesList.size() - 1);

            tvAuthor.setText(latest.author);
            tvBody.setText(latest.body);
            tvTime.setText(latest.time);
         }

         @Override
         public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.e(TAG, "onChildChanged:" + dataSnapshot.getKey());

            // A message has changed
            Message message = dataSnapshot.getValue(Message.class);
            Toast.makeText(MessageActivity.this, "onChildChanged: " + message.body, Toast.LENGTH_SHORT).show();
         }

         @Override
         public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.e(TAG, "onChildRemoved:" + dataSnapshot.getKey());

            // A message has been removed
            Message message = dataSnapshot.getValue(Message.class);
            Toast.makeText(MessageActivity.this, "onChildRemoved: " + message.body, Toast.LENGTH_SHORT).show();
         }

         @Override
         public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.e(TAG, "onChildMoved:" + dataSnapshot.getKey());

            // A message has changed position
            Message message = dataSnapshot.getValue(Message.class);
            Toast.makeText(MessageActivity.this, "onChildMoved: " + message.body, Toast.LENGTH_SHORT).show();
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "postMessages:onCancelled", databaseError.toException());
            Toast.makeText(MessageActivity.this, "Failed to load Message.", Toast.LENGTH_SHORT).show();
         }
      };

      mMessageReference.addChildEventListener(childEventListener);
      mMessageListener = childEventListener;

      /*ValueEventListener messageListener = new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
               Message message = dataSnapshot.getValue(Message.class);

               Log.e(TAG, "onDataChange: Message data is updated: " + message.author + ", " + message.time + ", " + message.body);

               tvAuthor.setText(message.author);
               tvTime.setText(message.time);
               tvBody.setText(message.body);
            }
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {
            // Failed to read value
            Log.e(TAG, "onCancelled: Failed to read message");

            tvAuthor.setText("");
            tvTime.setText("");
            tvBody.setText("onCancelled: Failed to read message!");
         }
      };

      mMessageReference.addValueEventListener(messageListener);

      // copy for removing at onStop()
      mMessageListener = messageListener;*/
   }

   @Override
   protected void onStop() {
      super.onStop();

      if(mMessageReference != null) {
         mMessageReference.removeEventListener(mMessageListener);
      }
   }

   private void submitMessage() {
      final String body = edtSentText.getText().toString();

      if(TextUtils.isEmpty(body)) {
         edtSentText.setError(REQUIRED);
         return;
      }

      // User data change listener
      mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);

            if(user == null) {
               Log.e(TAG, "onDataChange: User data is null!");
               Toast.makeText(MessageActivity.this, "onDataChange: User data is null!", Toast.LENGTH_SHORT).show();
               return;
            }

            writeNewMessage(body);
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {
            // Failed to read value
            Log.e(TAG, "onCancelled: Failed to read user!");
         }
      });
   }

   private void writeNewMessage(String body) {
      String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
      Message message = new Message(getUsernameFromEmail(user.getEmail()), body, time);

      Map<String, Object> messageValues = message.toMap();
      Map<String, Object> childUpdates = new HashMap<>();

      String key = mDatabase.child("message").push().getKey();

      childUpdates.put("/message/" + key, messageValues);
      childUpdates.put("/user-messages/" + user.getUid() + "/" + key, messageValues);

      mDatabase.updateChildren(childUpdates);

      //mMessageReference.setValue(message);
   }

   private String getUsernameFromEmail(String email) {
      if(email.contains("@")) {
         return email.split("@")[0];
      }
      else {
         return email;
      }
   }
}
