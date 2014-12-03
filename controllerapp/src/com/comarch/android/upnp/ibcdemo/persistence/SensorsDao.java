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

import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SensorLight;
import com.comarch.android.upnp.ibcdemo.model.SensorTemperature;

public class SensorsDao extends BaseDao{

   private final static String TAG = SensorsDao.class.getCanonicalName();
   
   private final static String DB_TABLE_NAME = "sensors";

   private final static String KEY_UUID_NAME = "uuid_name";
   private final static String KEY_UUID_NAME_DEF = "TEXT PRIMARY KEY";
   @SuppressWarnings("unused")
private final int KEY_UUID_NAME_COLUMN = 0;  
   
   private final static String KEY_UUID = "uuid";
   private final static String KEY_UUID_DEF = "TEXT";
   private final int KEY_UUID_COLUMN = 1;

   private final static String KEY_NAME = "name";
   private final static String KEY_NAME_DEF = "TEXT";
   private final int KEY_NAME_COLUMN = 2;
   
   private final static String KEY_SENSOR_ID = "hash";
   private final static String KEY_SENSOR_ID_DEF = "TEXT";
   private final int KEY_SENSOR_ID_COLUMN = 3;
   
   private final static String KEY_CLASS = "class";
   private final static String KEY_CLASS_DEF = "TEXT";
   private final int KEY_CLASS_COLUMN = 4;
   
   private final static String DB_CREATE_TABLE = String.format(
           "CREATE TABLE %s ( %s %s, %s %s, %s %s, %s %s, %s %s)", DB_TABLE_NAME,KEY_UUID_NAME,KEY_UUID_NAME_DEF, KEY_UUID, KEY_UUID_DEF, KEY_NAME,
           KEY_NAME_DEF,KEY_SENSOR_ID, KEY_SENSOR_ID_DEF,KEY_CLASS, KEY_CLASS_DEF);

   private final static String DROP_DB_TABLE = "DROP TABLE IF EXISTS " + DB_TABLE_NAME;

   private final String DB_INSERT_OR_REPLACE = String.format(
           "INSERT OR REPLACE INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)", DB_TABLE_NAME,
           KEY_UUID_NAME, KEY_UUID, KEY_NAME, KEY_SENSOR_ID, KEY_CLASS);

   private final String DB_DELETE_ALL = String.format("DELETE FROM %s",
           DB_TABLE_NAME);

  
   
   public SensorsDao(Context context) {
       super(context,TAG);
   }
   
   
   public static void onCreate(SQLiteDatabase db) {
       Log.d(TAG, "Database creating table "+DB_TABLE_NAME);
       db.execSQL(DB_CREATE_TABLE);
       Log.d(TAG, "Table " + DB_TABLE_NAME + " created");
   }

   public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       Log.d(TAG, "Dropping table "+DB_TABLE_NAME);
       db.execSQL(DROP_DB_TABLE);
       onCreate(db);
       Log.d(TAG, "Table " + DB_TABLE_NAME + " updated from ver." + oldVersion + " to ver."
               + newVersion);
   }
   
   //===========================================
   class SensorsIterable implements Iterable<Sensor> {
       @Override
       public Iterator<Sensor> iterator() {
           return new SensorsIterator();
       }
   }

   class SensorsIterator implements Iterator<Sensor> {
       
       private Cursor cursor;
       
       public SensorsIterator() {
           this.cursor = SensorsDao.this.getAllSensorsRaw();
       }

       @Override
       public boolean hasNext() {
           return cursor.getCount()!=0 && !cursor.isLast();
       }

       @Override
       public Sensor next() {
           cursor.moveToNext();
           String uuid = cursor.getString(KEY_UUID_COLUMN);
           String name = cursor.getString(KEY_NAME_COLUMN);
           String sensorId = cursor.getString(KEY_SENSOR_ID_COLUMN);
           String clazz = cursor.getString(KEY_CLASS_COLUMN);
           Sensor sensor = null;
           if(clazz.equalsIgnoreCase(SensorLight.class.getSimpleName())){
        	   sensor = new SensorLight(uuid,name,null);
           }else if(clazz.equalsIgnoreCase(SensorTemperature.class.getSimpleName())){
        	   sensor = new SensorTemperature(uuid, name, null);
           }else{
        	   Log.e(TAG,"Unknow sensor type "+clazz);
           }
           if(sensor!=null){
        	   sensor.setSensorID(sensorId);
           }

           return sensor;
       }

       @Override
       public void remove() {
           throw new UnsupportedOperationException();
       }
   }
   
   public void deleteAll() {
       execute(DB_DELETE_ALL);
   }
   
   public void insertOrUpdate( Sensor sensor) {
	   String key = sensor.getUuid()+"/"+sensor.getName();
   	Object[] args = new String[] {key,sensor.getUuid(), sensor.getName(),sensor.getSensorID(),sensor.getClass().getSimpleName()};
       execute(DB_INSERT_OR_REPLACE, args);
   }
   
   public Iterable<Sensor> getAll() {
       return new SensorsIterable();
   }
   
   public Cursor getAllSensorsRaw() {
       String[] columns = {KEY_UUID_NAME, KEY_UUID, KEY_NAME,KEY_SENSOR_ID,KEY_CLASS };
       return db.query(DB_TABLE_NAME, columns, null, null, null, null, null);
   }
}

