/**
 *
 * Copyright 2013-2014 UPnP Forum All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE OR WARRANTIES OF 
 * NON-INFRINGEMENT, ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are 
 * those of the authors and should not be interpreted as representing official 
 * policies, either expressed or implied, by the UPnP Forum.
 *
 **/
package com.comarch.android.upnp.ibcdemo.persistence;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.comarch.android.upnp.ibcdemo.model.MessageChat;

public class ChatDao extends BaseDao{

	private final static String TAG = ChatDao.class.getSimpleName();
	private final static String DB_TABLE = "chat_messages";
	
    private final static String KEY_FROM = "fromJid";
    private final static String KEY_FROM_DEF = "TEXT";
    private final int KEY_FROM_COLUMN = 0;
    
    private final static String KEY_TO = "toJid";
    private final static String KEY_TO_DEF = "TEXT";
    private final int KEY_TO_COLUMN = 1;
    
    private final static String KEY_MESSAGE = "message";
    private final static String KEY_MESSAGE_DEF = "TEXT";
    private final int KEY_MESSAGE_COLUMN = 2;
    
    private final static String KEY_DATETIME = "datetime";
    private final static String KEY_DATETIME_DEF = "INTEGER";
    private final int KEY_DATETIME_COLUMN = 3;
    
    private final static String KEY_PK = "id";
    private final static String KEY_PK_DEF = "TEXT PRIMARY KEY";
    private final int KEY_PK_COLUMN = 4;
    
    private final static String KEY_READ = "read";
    private final static String KEY_READ_DEF = "INTEGER";
    
    private final static String DB_CREATE_TABLE = String.format(
            "CREATE TABLE %s ( %s %s, %s %s,%s %s,%s %s,%s %s,%s %s)", DB_TABLE, KEY_FROM, KEY_FROM_DEF, KEY_TO,
            KEY_TO_DEF,KEY_MESSAGE,KEY_MESSAGE_DEF,KEY_DATETIME,KEY_DATETIME_DEF,KEY_PK,KEY_PK_DEF,KEY_READ,KEY_READ_DEF);
    private final static String DROP_DB_TABLE = "DROP TABLE IF EXISTS " + DB_TABLE;
    private final String DB_DELETE_ALL = String.format("DELETE FROM %s",DB_TABLE);

    
	public ChatDao(Context context) {
		super(context,TAG);
	}
	
    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database creating table " + DB_CREATE_TABLE);
        db.execSQL(DB_CREATE_TABLE);
        Log.d(TAG, "Table " + DB_TABLE + " created");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Dropping table " + DROP_DB_TABLE);
        db.execSQL(DROP_DB_TABLE);
        onCreate(db);
        Log.d(TAG, "Table " + DB_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
    }
    
    public void deleteAll(){
    	execute(DB_DELETE_ALL);
    }
    
    public void add(MessageChat msg){
    	this.add(msg.getFrom(),msg.getTo(),msg.getMessage(),msg.getDatetime());
    }
    public void add(String from,String to,String body,Long time){
    	String query = String.format("INSERT OR REPLACE INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)", DB_TABLE,
        KEY_PK, KEY_FROM, KEY_TO,KEY_MESSAGE,KEY_DATETIME,KEY_READ);
    	execute(query,UUID.randomUUID().toString(),from,to,body,time, 0);
    }
    private MessageChat objectFromCursor(Cursor cursor){
    	String from = cursor.getString(KEY_FROM_COLUMN);
    	String to = cursor.getString(KEY_TO_COLUMN);
    	String body = cursor.getString(KEY_MESSAGE_COLUMN);
    	Long datetime= cursor.getLong(KEY_DATETIME_COLUMN);
    	String id = cursor.getString(KEY_PK_COLUMN);
    	return new MessageChat(id,from,to,body,datetime);
    	
    }
    
    public boolean hasUnread(String resource){
        String[] columns = { KEY_FROM, KEY_TO,KEY_READ};
        String where = KEY_READ +"=0 AND ("+KEY_FROM + "='"+resource+"' OR "+KEY_TO +"='"+resource+"')";
        Cursor cursor = db.query(DB_TABLE, columns, where, null, null, null, null);
        Log.d(TAG,"hasUnread "+cursor.getCount());
        return cursor.getCount() != 0;
    }
    
    public void markAsRead(String resource){
    	ContentValues values = new ContentValues();
    	values.put(KEY_READ, 1);
    	String where = KEY_FROM + "='" + resource+"' OR "+KEY_TO +"='"+resource+"'";
    	db.update(DB_TABLE, values, where, null);
    }
    public List<MessageChat> getMessages(String resource){
    	List<MessageChat> list = new LinkedList<MessageChat>();
        String[] columns = { KEY_FROM, KEY_TO,KEY_MESSAGE,KEY_DATETIME,KEY_PK};
        String where = KEY_FROM + "='" + resource+"' OR "+KEY_TO +"='"+resource+"'";
        Cursor cursor = db.query(DB_TABLE, columns, where, null, null, null, KEY_DATETIME+" ASC");
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        	list.add(objectFromCursor(cursor));
        }
        cursor.close();
    	return list;
    }

}
