package edu.tmpt.texttranslate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MessagesActivity extends Activity {
	
	private final static String TAG = MessagesActivity.class.getSimpleName();
	
	private int threadID;
	private Conversation convers;
	
	private ListView msgList;
	private EditText input;
	private Button send;
	
	MessagesAdapter messages;
	
	private Translator trans;
	
	
	class TranslateFetch extends AsyncTask<MessagesAdapter, Void, String> {

		@Override
		protected String doInBackground(MessagesAdapter ... m) {
			for(int i = 0; i < m[0].getCount(); i++){
			
				Message msg = m[0].getItem(i);
				if(!msg.sent){
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					Log.d(TAG, "bla");
					if(!prefs.contains(msg.time+"")){
						msg.translated = trans.getTranslation(msg.text, 0.6, 10);
						Log.d(TAG, "bla2");
						
						prefs.edit().putString(msg.time+"", msg.translated).commit();
					} else {
						msg.translated = prefs.getString(msg.time+"", "");
						if(msg.translated.equals(msg.text)){
							msg.translated = trans.getTranslation(msg.text, 0.6, 10);
							prefs.edit().putString(msg.time+"", msg.translated).commit();
						}
					}
				}
			}
			return null;
		}

		protected void onPostExecute(String result) {
			
			messages.notifyDataSetChanged();
			setProgressBarIndeterminateVisibility(false);
			return;
		}
	}
	
	class Updater extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			SmsReader.getAllConversations(getBaseContext());			
			return null;
		}
		
		@Override
		protected void onPostExecute(Integer result){
			update();	
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
		setContentView(R.layout.activity_messages);
		
		msgList = (ListView)findViewById(R.id.messages);
		input = (EditText)findViewById(R.id.input);
		send = (Button)findViewById(R.id.send);
		
		trans = new Translator();
		
		send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendText();
			}
		});
		
		
		
		messages = new MessagesAdapter(this, android.R.layout.simple_list_item_1);
		msgList.setAdapter(messages);
		
		msgList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View position,
					int pos, long id) {
				Intent intent = new Intent(getBaseContext(), MapActivity.class);
				intent.putExtra("msg", ((Message)messages.getItem(pos)).text);
				startActivity(intent);
			}
			
		});
		
		
		msgList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View position,
					int pos, long id) {
				Toast.makeText(getBaseContext(), messages.getItem(pos).text, Toast.LENGTH_LONG).show();;
				return true;
			}
			
		});
		
		threadID = getIntent().getExtras().getInt("thread_id");
		update();
		
		
		//updateTask.execute(Integer.MAX_VALUE);
		
	}
	
	private void update(){
		
		messages.clear();	
		
		for(Conversation c:SmsReader.conversations){
			if(c.id == threadID){
				convers = c;
				break;
			}
		}
		try {
		for(Message m:convers.messages){
			messages.add(m);
		}
		
		messages.notifyDataSetChanged();
		setProgressBarIndeterminateVisibility(true);

		new TranslateFetch().execute(messages);
		} catch(Exception e){		
			
		}
		
	}
	
	private void sendText(){
		
		try {
			SmsReader.send(this, input.getText().toString(), convers);			
			input.setText("");
			KeyboardUtil.hideKeyboard(this);
			setProgressBarIndeterminateVisibility(true);

			new Updater().execute(Integer.MAX_VALUE);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		msgList.requestFocus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.messages, menu);
		return true;
	}


	
}
