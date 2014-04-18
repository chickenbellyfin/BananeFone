package edu.tmpt.texttranslate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SmsReader {
	
	private final static String TAG = SmsReader.class.getSimpleName();
	
	private final static String conversationURI = "content://sms/conversations";
	private final static String messageURI = "content://sms/";
	private final static String sentURI = "content://sms/sent";
	
	public static ArrayList<Conversation> conversations = new ArrayList<Conversation>();
	
	public static Uri picUriHolder;
	
	public static int m_id = 0;
	
	public static void getAllConversations(Context c){
		conversations = new ArrayList<Conversation>();
		
		Uri u = Uri.parse(conversationURI);
		String order = "date";
		String[] proj = {"thread_id", "snippet"};
		Cursor db = c.getContentResolver().query(u, proj, null, null, order);
		
		while(db.moveToNext()){
			try {				
				int id = db.getInt(db.getColumnIndex("thread_id"));	
				Conversation con = new Conversation();
				con.id = id;
				con.snippet = db.getString(db.getColumnIndex("snippet"));
				getMessages(con, c);
				conversations.add(con);
				
			
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		db.close();
		//return conver;

	}
	
	private static void getMessages(Conversation co, Context c){
		Uri u = Uri.parse(messageURI);
		String order = "date";
		String[] proj = {"thread_id", "address", "date", "body", "_id", "person"};
		Cursor db = c.getContentResolver().query(u, proj, null, null, order);
		
		while(db.moveToNext()){
			try{
				int tid = db.getInt(db.getColumnIndex("thread_id"));
				if(tid == co.id){
					Message m = new Message();
					m.address = db.getString(db.getColumnIndex("address"));
					m.time = db.getLong(db.getColumnIndex("date"));
					m.text = db.getString(db.getColumnIndex("body"));
					m.sent = isSent(db.getInt(db.getColumnIndex("_id")), c);
					m.translated = m.text;
					
					m.name = getName(c, db.getInt(db.getColumnIndex("person")));
					if(m.text.length() != 0){
						co.addMessage(m);
					}
					
					if(co.pic == null){
						co.pic = getPhotoUri(c,  db.getInt(db.getColumnIndex("person")));
					}
				}
			}catch(Exception e){
				e.printStackTrace();				
			}
		}
		Collections.sort(co.messages, new Comparator<Message>() {

			@Override
			public int compare(Message lhs, Message rhs) {
				return (int) (lhs.time-rhs.time);
			}
		});
		
		db.close();	
				
	}
	
	private static String getName(Context ctx, int pid){
		
		long contactId = 0;
		Uri uri = ContactsContract.RawContacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.RawContacts._ID, ContactsContract.RawContacts.CONTACT_ID };
		Cursor cursor = ctx.getContentResolver().query(uri, projection, 
		        ContactsContract.RawContacts._ID + " = ?",
		        new String[] { pid+"" }, null);

		if (cursor.moveToFirst()) {
		    int contactIdIndex = cursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID);
		    contactId = cursor.getLong(contactIdIndex);
		}
		cursor.close();
		
		
		 projection = new String[] {
				Contacts._ID,
				Contacts.DISPLAY_NAME,	// the name of the contact
				//Contacts.PHOTO_ID		// the id of the column in the data table for the image
			};
		
		final Cursor contact = ctx.getContentResolver().query(
				Contacts.CONTENT_URI,
				projection,
				Contacts._ID + "=?",	// filter entries on the basis of the contact id
				new String[]{String.valueOf(contactId)},	// the parameter to which the contact id column is compared to
				null);
		
		if(contact.moveToFirst()) {
			int id = contact.getInt(contact.getColumnIndex(Contacts._ID));
			String name = contact.getString(
				contact.getColumnIndex(Contacts.DISPLAY_NAME));
				//final String photoId = contact.getString(
				//		contact.getColumnIndex(Contacts.PHOTO_ID));
				//doSomethingWithAContactName(name);
				//doSomethingWithAContactPhotoId(photoId);
				if(id == contactId){
					if(name.length() == 0)return null;
					return name;
				}
			}
			contact.close();
			return null;
	}
	
	private static  boolean isSent(int mid, Context c){
		Uri u = Uri.parse(sentURI);
		String order = "date";
		String[] proj  = {"_id"};
		Cursor db = c.getContentResolver().query(u, proj, null, null, order);
		
		while(db.moveToNext()){
			if( db.getInt(db.getColumnIndex("_id")) == mid) return true;
			
					
		}
		db.close();
		return false;
	}
	
	public static void send(Context c, String msg, Conversation conv){
		if(msg.length() == 0){
			Toast.makeText(c, "No message!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Uri u = Uri.parse(sentURI);
		String order = "";
		ContentValues val = new  ContentValues();
		val.put("thread_id", conv.id);
		val.put("address", conv.address);
		val.put("date", System.currentTimeMillis());
		val.put("body", msg);
		val.put("_id", (int)(Math.random()*90000)+ 80000);
		c.getContentResolver().insert(u, val);
		SmsManager smsm = SmsManager.getDefault();
		smsm.sendTextMessage(conv.address, null, msg, null, null);

	
	}
	
	/**
	 * @return the photo URI
	 */
	public static Uri getPhotoUri(Context ctx, long pid) {
		
		long contactId = 0;
		Uri uri = ContactsContract.RawContacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.RawContacts._ID, ContactsContract.RawContacts.CONTACT_ID };
		Cursor cursor = ctx.getContentResolver().query(uri, projection, 
		        ContactsContract.RawContacts._ID + " = ?",
		        new String[] { pid+"" }, null);

		if (cursor.moveToFirst()) {
		    int contactIdIndex = cursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID);
		    contactId = cursor.getLong(contactIdIndex);
		}
		cursor.close();
		
	    try {
	        Cursor cur = ctx.getContentResolver().query(
	                ContactsContract.Data.CONTENT_URI,
	                null,
	                ContactsContract.Data.CONTACT_ID + "=" +contactId + " AND "
	                        + ContactsContract.Data.MIMETYPE + "='"
	                        + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
	                null);
	        if (cur != null) {
	            if (!cur.moveToFirst()) {
	                return null; // no photo
	            }
	        } else {
	            return null; // error in cursor process
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
	            .parseLong(""+contactId));
	    return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}
	
}
