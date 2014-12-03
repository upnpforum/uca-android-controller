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

import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.comarch.android.upnp.ibcdemo.model.ControlPoint;

public class ControlPointDao extends BaseDao {
    private final static String TAG = ControlPointDao.class.getCanonicalName();
    
    private final static String DB_CONTROL_POINTS_TABLE = "control_points";

    private final static String KEY_UUID = "uuid";
    private final static String KEY_UUID_DEF = "TEXT PRIMARY KEY";
    private final int KEY_UUID_COLUMN = 0;

    private final static String KEY_NAME = "name";
    private final static String KEY_NAME_DEF = "TEXT";
    private final int KEY_NAME_COLUMN = 1;
    
    private final static String HASH_NAME = "hash";
    private final static String HASH_NAME_DEF = "TEXT";
    private final int HASH_NAME_COLUMN = 2;
    
    private final static String DB_CREATE_TABLE = String.format(
            "CREATE TABLE %s ( %s %s, %s %s, %s %s)", DB_CONTROL_POINTS_TABLE, KEY_UUID, KEY_UUID_DEF, KEY_NAME,
            KEY_NAME_DEF,HASH_NAME, HASH_NAME_DEF);

    private final static String DROP_DB_TABLE = "DROP TABLE IF EXISTS " + DB_CONTROL_POINTS_TABLE;

    private final String DB_INSERT_OR_REPLACE = String.format(
            "INSERT OR REPLACE INTO %s (%s, %s, %s) VALUES (?, ?, ?)", DB_CONTROL_POINTS_TABLE,
            KEY_UUID, KEY_NAME, HASH_NAME);

    private final String DB_DELETE_CP = String.format("DELETE FROM %s WHERE %s= ? ",
            DB_CONTROL_POINTS_TABLE, KEY_UUID);

    private final String DB_DELETE_ALL_CP = String.format("DELETE FROM %s",
            DB_CONTROL_POINTS_TABLE);

   
    
    public ControlPointDao(Context context) {
        super(context,TAG);
    }
    
    
    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database creating table "+DB_CONTROL_POINTS_TABLE);
        db.execSQL(DB_CREATE_TABLE);
        Log.d(TAG, "Table " + DB_CONTROL_POINTS_TABLE + " created");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Dropping table "+DB_CONTROL_POINTS_TABLE);
        db.execSQL(DROP_DB_TABLE);
        onCreate(db);
        Log.d(TAG, "Table " + DB_CONTROL_POINTS_TABLE + " updated from ver." + oldVersion + " to ver."
                + newVersion);
    }
    
    //===========================================
    class ControlPointIterable implements Iterable<ControlPoint> {
        @Override
        public Iterator<ControlPoint> iterator() {
            return new ControlPointIterator();
        }
    }

    class ControlPointIterator implements Iterator<ControlPoint> {
        
        private Cursor cursor;
        
        public ControlPointIterator() {
            this.cursor = ControlPointDao.this.getAllControlPointRaw();
        }

        @Override
        public boolean hasNext() {
            return cursor.getCount()!=0 && !cursor.isLast();
        }

        @Override
        public ControlPoint next() {
            cursor.moveToNext();
            String uuid = cursor.getString(KEY_UUID_COLUMN);
            String name = cursor.getString(KEY_NAME_COLUMN);
            String hash = cursor.getString(HASH_NAME_COLUMN);
            ControlPoint cp = new ControlPoint(uuid, name);
            cp.setConfigIdCloud(hash);
            return cp;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    public void deleteAll() {
        execute(DB_DELETE_ALL_CP);
    }

    public void delete(String uuid) {
        execute(DB_DELETE_CP, uuid);
    }
    
    public void insertOrUpdate( ControlPoint controlPoint) {
    	Object[] args = new Object[] {controlPoint.getUuid(), controlPoint.getName(),controlPoint.getConfigIdCloud()};
        execute(DB_INSERT_OR_REPLACE, args);
    }
    
    public Iterable<ControlPoint> getAll() {
        return new ControlPointIterable();
    }
    
    public Cursor getAllControlPointRaw() {
        String[] columns = { KEY_UUID, KEY_NAME,HASH_NAME };
        return db.query(DB_CONTROL_POINTS_TABLE, columns, null, null, null, null, null);
    }
    
    public ControlPoint getOne(String uuid) {
        String[] columns = { KEY_UUID, KEY_NAME,HASH_NAME };
        String where = KEY_UUID + "='" + uuid+"'";
        Cursor cursor = db.query(DB_CONTROL_POINTS_TABLE, columns, where, null, null, null, null);
        ControlPoint device = null;
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(KEY_NAME_COLUMN);
            String hash = cursor.getString(HASH_NAME_COLUMN);
            device = new ControlPoint(uuid, name);
            device.setConfigIdCloud(hash);
        }
        return device;
    }
}
