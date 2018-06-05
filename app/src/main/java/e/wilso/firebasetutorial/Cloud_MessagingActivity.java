package e.wilso.firebasetutorial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.messaging.FirebaseMessaging;

public class Cloud_MessagingActivity extends AppCompatActivity {

   private Button btn_subscribe;
   private Button btn_unsubscribe;

   private final String TOPIC = "JavaSampleApproach";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_cloud__messaging);

      btn_subscribe = (Button) findViewById(R.id.btn_subscribe);
      btn_unsubscribe = (Button) findViewById(R.id.btn_unsubscribe);

      btn_subscribe.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            // TOPIC = "JavaSampleApproach"
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);
         }
      });

      btn_unsubscribe.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
         }
      });
   }
}
