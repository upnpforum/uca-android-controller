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

import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.RGBDimmableLight;

public class DimmableLightDao extends BaseDao{

    private static String TAG = DimmableLightDao.class.getCanonicalName();

    private final static String DB_DIMMABLE_LIGHT_TABLE = "dimmable_lights";

    private final static String KEY_UUID = "uuid";
    private final static String KEY_UUID_DEF = "TEXT PRIMARY KEY";
    private final int KEY_UUID_COLUMN = 0;

    private final static String KEY_NAME = "name";
    private final static String KEY_NAME_DEF = "TEXT";
    private final int KEY_NAME_COLUMN = 1;

    private final static String KEY_HASH = "hash";
    private final static String KEY_HASH_DEF = "TEXT";
    private final int KEY_HASH_COLUMN = 2;

    private final static String KEY_CLASS = "class";
    private final static String KEY_CLASS_DEF = "TEXT";
    private final int KEY_CLASS_COLUMN = 3;

    private final static String DB_CREATE_DIMMABLE_LIGHT_TABLE = String.format(
            "CREATE TABLE %s ( %s %s, %s %s, %s %s, %s %s)", DB_DIMMABLE_LIGHT_TABLE, KEY_UUID, KEY_UUID_DEF, KEY_NAME,
            KEY_NAME_DEF, KEY_HASH, KEY_HASH_DEF,KEY_CLASS, KEY_CLASS_DEF);

    private final static String DROP_DB_DIMMABLE_LIGHT_TABLE = "DROP TABLE IF EXISTS " + DB_DIMMABLE_LIGHT_TABLE;

    private final String DB_INSERT_OR_REPLACE_DIMMABLE_LIGHT = String.format(
            "INSERT OR REPLACE INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)", DB_DIMMABLE_LIGHT_TABLE,
            KEY_UUID, KEY_NAME, KEY_HASH, KEY_CLASS);

    private final String DB_DELETE_DIMMABLE_LIGHT = String.format("DELETE FROM %s WHERE %s= ? ",
            DB_DIMMABLE_LIGHT_TABLE, KEY_UUID);

    private final String DB_DELETE_ALL_DIMMABLE_LIGHT = String.format("DELETE FROM %s",
            DB_DIMMABLE_LIGHT_TABLE);
    

    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database creating table " + DB_CREATE_DIMMABLE_LIGHT_TABLE);
        db.execSQL(DB_CREATE_DIMMABLE_LIGHT_TABLE);
        Log.d(TAG, "Table " + DB_CREATE_DIMMABLE_LIGHT_TABLE + " created");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Dropping table " + DB_CREATE_DIMMABLE_LIGHT_TABLE);
        db.execSQL(DROP_DB_DIMMABLE_LIGHT_TABLE);
        onCreate(db);
        Log.d(TAG, "Table " + DB_CREATE_DIMMABLE_LIGHT_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
    }

    public DimmableLightDao(Context context) {
        super(context,TAG);
    }

    public void insertOrUpdate( DimmableLight dimmableLight) {
        Object[] args = new String[] {dimmableLight.getUuid(), dimmableLight.getName(),
                dimmableLight.getConfigIdCloud(),dimmableLight.getClass().getSimpleName()};
        execute(DB_INSERT_OR_REPLACE_DIMMABLE_LIGHT, args);
    }

    public void delete(String uuid) {
        execute(DB_DELETE_DIMMABLE_LIGHT, uuid);
    }

    public void deleteAll() {
        execute(DB_DELETE_ALL_DIMMABLE_LIGHT);
    }
        
    public Iterable<DimmableLight> getAll() {
        return new DimmableLightIterable();
    }

    public DimmableLight getDimmableLights(String uuid) {
        String[] columns = { KEY_UUID, KEY_NAME, KEY_HASH, KEY_CLASS };
        String where = KEY_UUID + "='" + uuid+"'";
        Cursor cursor = db.query(DB_DIMMABLE_LIGHT_TABLE, columns, where, null, null, null, null);
        DimmableLight light = null;
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(KEY_NAME_COLUMN);
            if(RGBDimmableLight.class.getSimpleName().equals(cursor.getString(KEY_CLASS_COLUMN))){
            	light = new RGBDimmableLight(uuid, name);
            }else{
            	light = new DimmableLight(uuid, name);
            }
            light.setConfigIdCloud(cursor.getString(KEY_HASH_COLUMN));
        }
        return light;
    }

    private Cursor getAllDimmableLightsRaw() {
        String[] columns = { KEY_UUID, KEY_NAME, KEY_HASH, KEY_CLASS };
        return db.query(DB_DIMMABLE_LIGHT_TABLE, columns, null, null, null, null, null);
    }
    
    class DimmableLightIterable implements Iterable<DimmableLight> {
        @Override
        public Iterator<DimmableLight> iterator() {
            return new DimmableLightIterator();
        }
    }

    class DimmableLightIterator implements Iterator<DimmableLight> {
        
        private Cursor cursor;
        
        public DimmableLightIterator() {
            this.cursor = DimmableLightDao.this.getAllDimmableLightsRaw();
        }

        @Override
        public boolean hasNext() {
            return cursor.getCount()!=0 && !cursor.isLast();
        }

        @Override
        public DimmableLight next() {
            cursor.moveToNext();
            String uuid = cursor.getString(KEY_UUID_COLUMN);
            String name = cursor.getString(KEY_NAME_COLUMN);
            String hash = cursor.getString(KEY_HASH_COLUMN);
            String clazz = cursor.getString(KEY_CLASS_COLUMN);
            DimmableLight light = null;
            if(RGBDimmableLight.class.getSimpleName().equals(clazz)){
            	light = new RGBDimmableLight(uuid, name);
            }else{
            	light = new DimmableLight(uuid, name);
            }
            light.setConfigIdCloud(hash);
            return light;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
