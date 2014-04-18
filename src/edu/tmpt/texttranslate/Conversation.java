package edu.tmpt.texttranslate;

import java.util.ArrayList;

import android.net.Uri;


public class Conversation {
	
	public int id;
	public String snippet;
	public String address;
	public ArrayList<Message> messages;
	public String name = null;
	public Uri pic;
	
	public void addMessage(Message m){
		if(messages == null){
			messages = new ArrayList<Message>();
			address = m.address;
			
		}
		
		if(name == null){
			name = m.name;
		}
		
		
		
		messages.add(m);
		
	}

	public String toString(){
		String snippetText = (snippet.length() < 25)?snippet+"...":snippet.substring(0, 25)+"...";
		return String.format("[%d] %s", messages.size(), snippetText);
		
	}
	
}
