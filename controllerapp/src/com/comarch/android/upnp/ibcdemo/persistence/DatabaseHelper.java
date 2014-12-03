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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = getClass().getCanonicalName();
    private final static int DB_VERSION = 15;
    private final static String DB_NAME = "def.db";
    
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database creating...");
        DimmableLightDao.onCreate(db);
        MediaServerDao.onCreate(db);
        MediaRendererDao.onCreate(db);
        AccompanyingDevicesDao.onCreate(db);
        ChatDao.onCreate(db);
        ControlPointDao.onCreate(db);
        SensorsDao.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Dropping database...");
        DimmableLightDao.onUpgrade(db, oldVersion, newVersion);
        MediaServerDao.onUpgrade(db, oldVersion, newVersion);
        MediaRendererDao.onUpgrade(db, oldVersion, newVersion);
        AccompanyingDevicesDao.onUpgrade(db, oldVersion, newVersion);
        ChatDao.onUpgrade(db, oldVersion, newVersion);
        ControlPointDao.onUpgrade(db, oldVersion, newVersion);
        SensorsDao.onUpgrade(db, oldVersion, newVersion);
    }
    
    public static void clearAll(Context context){
        MediaRendererDao renderersDao = new MediaRendererDao(context);
        MediaServerDao serversDao = new MediaServerDao(context);
        DimmableLightDao dimmableLightDao = new DimmableLightDao(context);
        ControlPointDao controlPointDao = new ControlPointDao(context);
        ChatDao chatDao = new ChatDao(context);
        AccompanyingDevicesDao accompanyingDao = new AccompanyingDevicesDao(context);
        SensorsDao sensorsDao = new SensorsDao(context);
        try {
            renderersDao.open();
            renderersDao.deleteAll();
            serversDao.open();
            serversDao.deleteAll();
            dimmableLightDao.open();
            dimmableLightDao.deleteAll();
            controlPointDao.open();
            controlPointDao.deleteAll();
            chatDao.open();
            chatDao.deleteAll();
            accompanyingDao.open();
            accompanyingDao.deleteAll();
            sensorsDao.open();
            sensorsDao.deleteAll();
        } finally {
            renderersDao.close();
            serversDao.close();
            dimmableLightDao.close();
            controlPointDao.close();
            chatDao.close();
            accompanyingDao.close();
            sensorsDao.close();
        }
    }

}
