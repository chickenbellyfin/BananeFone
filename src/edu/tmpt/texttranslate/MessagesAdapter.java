package edu.tmpt.texttranslate;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class MessagesAdapter extends ArrayAdapter<Message> {

	public MessagesAdapter(Context context, int resource) {
		super(context, resource);
		
		
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public View getView (int position, View convertView, ViewGroup parent){
		View view = super.getView(position, convertView, parent);
		if(((Message)getItem(position)).sent){

			view.setBackgroundColor(0xffcccccc);
			
		} else {

			view.setBackgroundColor(0xffffc143);
		}
		return view;
		
	}

	
	
	
	

}
