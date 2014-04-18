package edu.tmpt.texttranslate;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ConversationsActivity extends Activity {
	
	private static final String TAG = ConversationsActivity.class.getSimpleName();
	
	final String inboxURI = "content://sms/sent";
	
	private ListView list;
	private ConversationsAdapter convoList;

	class Updater extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			SmsReader.getAllConversations(getBaseContext());			
			return null;
		}
		
		@Override
		protected void onPostExecute(Integer result){
			updateList();	

			setProgressBarIndeterminateVisibility(false);
		}
	};
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_refresh){

			setProgressBarIndeterminateVisibility(true);
			new Updater().execute(0);
		}


	    return true;
	  } 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_conversations);
		
		list = (ListView)findViewById(R.id.convoList);
		convoList = new ConversationsAdapter(getBaseContext(), R.layout.item_convo);
		list.setAdapter(convoList);
		SmsManager smsm = SmsManager.getDefault();
		updateList();
		DistanceCalculator.loadLocations(getFilesDir(), this);
		setProgressBarIndeterminateVisibility(true);
		new Updater().execute(Integer.MAX_VALUE);
		//updateList();
		//getConversations();
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				int thread_id = ((Conversation)list.getItemAtPosition(pos)).id;	
				Intent intent = new Intent();
				intent.setClass(getBaseContext(), MessagesActivity.class);
				intent.putExtra("thread_id", thread_id);
				startActivity(intent);
				
			}
		});
		

		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conversations, menu);
		return true;
	}
	
	public void updateList(){
		convoList.clear();
		for(Conversation c:SmsReader.conversations){
			convoList.add(c);
		}
		convoList.notifyDataSetChanged();
	}
	
	public void getConversations(){
		convoList.clear();
		Uri u = Uri.parse(inboxURI);
		String order = "date";
		Cursor db = getContentResolver().query(u, null, null, null, order);
		
		while(db.moveToNext()){
			try {
				
				for(int i = 0; i < db.getColumnCount(); i++){
					Log.d(TAG, db.getColumnName(i)+":"+db.getString(i));
				}
				Log.d(TAG, "----");
				
			
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		
		db.close();
	
	}

}
