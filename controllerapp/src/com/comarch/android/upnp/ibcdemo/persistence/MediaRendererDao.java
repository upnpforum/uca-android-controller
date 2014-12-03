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

import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;

public class MediaRendererDao extends BaseDao{

    private final static String TAG = MediaServerDao.class.getCanonicalName();
    
    private final static String DB_MEDIA_RENDERER_TABLE = "media_renderers";

    private final static String KEY_UUID = "uuid";
    private final static String KEY_UUID_DEF = "TEXT PRIMARY KEY";
    private final int KEY_UUID_COLUMN = 0;

    private final static String KEY_NAME = "name";
    private final static String KEY_NAME_DEF = "TEXT";
    private final int KEY_NAME_COLUMN = 1;
    
    private final static String HASH_NAME = "hash";
    private final static String HASH_NAME_DEF = "TEXT";
    private final int HASH_NAME_COLUMN = 2;
    
    private final static String DB_CREATE_MEDIA_SERVER_TABLE = String.format(
            "CREATE TABLE %s ( %s %s, %s %s, %s %s)", DB_MEDIA_RENDERER_TABLE, KEY_UUID, KEY_UUID_DEF, KEY_NAME,
            KEY_NAME_DEF, HASH_NAME, HASH_NAME_DEF);

    private final static String DROP_DB_MEDIA_RENDERER_TABLE = "DROP TABLE IF EXISTS " + DB_MEDIA_RENDERER_TABLE;

    private final String DB_INSERT_OR_REPLACE_MEDIA_RENDERER = String.format(
            "INSERT OR REPLACE INTO %s (%s, %s, %s) VALUES (?, ?, ?)", DB_MEDIA_RENDERER_TABLE,
            KEY_UUID, KEY_NAME, HASH_NAME);

    private final String DB_DELETE_MEDIA_RENDERER = String.format("DELETE FROM %s WHERE %s= ? ",
            DB_MEDIA_RENDERER_TABLE, KEY_UUID);

    private final String DB_DELETE_ALL_MEDIA_RENDERER = String.format("DELETE FROM %s",
            DB_MEDIA_RENDERER_TABLE);

    
    public MediaRendererDao(Context context) {
        super(context, TAG);
    }
    
    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database creating table "+DB_MEDIA_RENDERER_TABLE);
        db.execSQL(DB_CREATE_MEDIA_SERVER_TABLE);
        Log.d(TAG, "Table " + DB_MEDIA_RENDERER_TABLE + " created");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Dropping table "+DB_MEDIA_RENDERER_TABLE);
        db.execSQL(DROP_DB_MEDIA_RENDERER_TABLE);
        onCreate(db);
        Log.d(TAG, "Table " + DB_MEDIA_RENDERER_TABLE + " updated from ver." + oldVersion + " to ver."
                + newVersion);
    }
    //===========================================
    class MediaRendererIterable implements Iterable<MediaRenderer> {
        @Override
        public Iterator<MediaRenderer> iterator() {
            return new MediaRendererIterator();
        }
    }

    class MediaRendererIterator implements Iterator<MediaRenderer> {
        
        private Cursor cursor;
        
        public MediaRendererIterator() {
            this.cursor = MediaRendererDao.this.getAllMediaRendererRaw();
        }

        @Override
        public boolean hasNext() {
            return cursor.getCount()!=0 && !cursor.isLast();
        }

        @Override
        public MediaRenderer next() {
            cursor.moveToNext();
            String uuid = cursor.getString(KEY_UUID_COLUMN);
            String name = cursor.getString(KEY_NAME_COLUMN);
            String hash = cursor.getString(HASH_NAME_COLUMN);
            MediaRenderer mr = new MediaRenderer(uuid, name);
            mr.setConfigIdCloud(hash);
            return mr;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    
    public void deleteAll() {
        execute(DB_DELETE_ALL_MEDIA_RENDERER);
    }
    
    public void delete(String uuid) {
        execute(DB_DELETE_MEDIA_RENDERER, uuid);
    }
    
    public void insertOrUpdate( MediaRenderer mediaRenderer) {
    	Object[] args = new Object[] {mediaRenderer.getUuid(), mediaRenderer.getName(), mediaRenderer.getConfigIdCloud()};
        execute(DB_INSERT_OR_REPLACE_MEDIA_RENDERER, args);
    }
    public Iterable<MediaRenderer> getAll() {
        return new MediaRendererIterable();
    }
    public Cursor getAllMediaRendererRaw() {
        String[] columns = { KEY_UUID, KEY_NAME, HASH_NAME };
        return db.query(DB_MEDIA_RENDERER_TABLE, columns, null, null, null, null, null);
    }
    
    public MediaRenderer getOne(String uuid) {
        String[] columns = { KEY_UUID, KEY_NAME,HASH_NAME };
        String where = KEY_UUID + "='" + uuid+"'";
        Cursor cursor = db.query(DB_MEDIA_RENDERER_TABLE, columns, where, null, null, null, null);
        MediaRenderer mediaRenderer = null;
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(KEY_NAME_COLUMN);
            String hash = cursor.getString(HASH_NAME_COLUMN);
            mediaRenderer = new MediaRenderer(uuid, name);
            mediaRenderer.setConfigIdCloud(hash);
        }
        return mediaRenderer;
    }
}
