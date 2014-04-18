package edu.tmpt.texttranslate;

public class Message {
	
	
	public String text;
	public String address;
	public long time;
	public boolean sent = false;
	public String name;
	public String translated;
	public LangCode[] trail;
	public String toString(){
		return translated;
	}

}
