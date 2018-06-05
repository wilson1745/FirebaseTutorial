package e.wilso.firebasetutorial;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

   String[] item = {"Cloud Messaging", "Authentication", "Realtime Database", "Cloud Storage"};

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      final ListView list = findViewById(R.id.list);
      final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, item);
      list.setAdapter(adapter);
      list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(MainActivity.this, adapter.getItem(position).toString(), Toast.LENGTH_LONG).show();
            action(position);
         }
      });
   }

   private void action(int position) {
      switch (position) {
         case 0:
            Intent intent = new Intent(MainActivity.this, Cloud_MessagingActivity.class);
            startActivity(intent);
            break;
         case 1:
            Intent intent1 = new Intent(MainActivity.this, AuthenticationActivity.class);
            startActivity(intent1);
            break;
         case 2:

            break;
         default:
            break;
      }
   }
}
