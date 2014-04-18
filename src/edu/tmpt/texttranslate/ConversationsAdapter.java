package edu.tmpt.texttranslate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationsAdapter extends ArrayAdapter<Conversation>{
	
	private final static String TAG = ConversationsAdapter.class.getSimpleName();

	private Context ctx;
	public ConversationsAdapter(Context context, int resource) {
		super(context, resource);
		ctx = context;
	}
	
	public View getView (int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null){

            LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_convo, null);
		} 

		TextView name = (TextView)view.findViewById(R.id.name);
		TextView count = (TextView)view.findViewById(R.id.count);
		ImageView pic = (ImageView)view.findViewById(R.id.pic);
		
		Conversation c = getItem(position);
		
		if(c.pic != null){
			pic.setImageURI(c.pic);
		} else {
			pic.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_launcher));
		}

		if(c.name == null){
			name.setText(c.address);
		} else {
			name.setText(c.name);
		}

		count.setText(c.messages.size()+" messages");
		
		
		return view;
			
	}


}
