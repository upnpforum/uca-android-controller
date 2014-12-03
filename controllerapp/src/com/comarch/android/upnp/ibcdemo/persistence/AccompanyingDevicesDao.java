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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AccompanyingDevicesDao extends BaseDao {

    private final static String TAG = MediaServerDao.class.getCanonicalName();
    
    private final static String DB_ACCOMPANYING_DEVICES_TABLE = "accompanying_devices";
    
    private final static String KEY_FROM_UUID = "from_uuid";
    private final static String KEY_FROM_UUID_DEF = "TEXT";
    //private final int KEY_FROM_UUID_COLUMN = 0;
    
    private final static String KEY_TO_UUID = "to_uuid";
    private final static String KEY_TO_UUID_DEF = "TEXT";
    private final int KEY_TO_UUID_COLUMN = 1;
    
    private final static String DB_CREATE_TABLE = String.format(
            "CREATE TABLE %s ( %s %s, %s %s)", DB_ACCOMPANYING_DEVICES_TABLE, KEY_FROM_UUID, KEY_FROM_UUID_DEF, KEY_TO_UUID,
            KEY_TO_UUID_DEF);

    private final static String DROP_DB_TABLE = "DROP TABLE IF EXISTS " + DB_ACCOMPANYING_DEVICES_TABLE;
    private final String DB_DELETE_ALL = String.format("DELETE FROM %s",DB_ACCOMPANYING_DEVICES_TABLE);
    private final String DB_DELETE_WITH_UUID = String.format("DELETE FROM %s WHERE %s= ? OR %s = ?",
    		DB_ACCOMPANYING_DEVICES_TABLE, KEY_FROM_UUID,KEY_TO_UUID);
    private final String DB_DELETE_ONE = String.format("DELETE FROM %s WHERE %s= ? AND %s = ?",
    		DB_ACCOMPANYING_DEVICES_TABLE, KEY_FROM_UUID,KEY_TO_UUID);
    private final String DB_INSERT_OR_REPLACE = String.format(
            "INSERT OR REPLACE INTO %s (%s, %s) VALUES (?, ?)", DB_ACCOMPANYING_DEVICES_TABLE,
            KEY_FROM_UUID, KEY_TO_UUID);
    
	public AccompanyingDevicesDao(Context context) {
		super(context, TAG);
	}
	
    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database creating table " + DB_CREATE_TABLE);
        db.execSQL(DB_CREATE_TABLE);
        Log.d(TAG, "Table " + DB_ACCOMPANYING_DEVICES_TABLE + " created");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Dropping table " + DROP_DB_TABLE);
        db.execSQL(DROP_DB_TABLE);
        onCreate(db);
        Log.d(TAG, "Table " + DB_ACCOMPANYING_DEVICES_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
    }
    
    public void deleteAll(){
    	execute(DB_DELETE_ALL);
    }
    
    public void deleteWithUuid(String uuid){
    	execute(DB_DELETE_WITH_UUID,new Object[]{uuid,uuid});
    }
    
    public void delete(String from,String to){
    	execute(DB_DELETE_ONE,new Object[]{from,to});
    }
    
    public void create(String from,String to){
    	execute(DB_INSERT_OR_REPLACE,new Object[]{from,to});
    }
    
    public List<String> getAccompanying(String key){
    	List<String> list = new LinkedList<String>();
    	try
    	{
    		String[] columns = { KEY_FROM_UUID, KEY_TO_UUID};
    		String where = KEY_FROM_UUID + "='" + key+"'";
    		Cursor cursor = db.query(DB_ACCOMPANYING_DEVICES_TABLE, columns, where, null, null, null, null);
    		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
    			list.add(cursor.getString(KEY_TO_UUID_COLUMN));
    		}
    		cursor.close();
    	}
    	catch (Exception e)
    	{
    		Log.e(TAG, "Caught exception: " + e.toString());
    	}
        return list;
    }
}
