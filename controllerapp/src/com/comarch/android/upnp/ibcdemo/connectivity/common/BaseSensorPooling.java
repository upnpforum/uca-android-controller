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
package com.comarch.android.upnp.ibcdemo.connectivity.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.connectivity.busevent.IServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SensorLight;
import com.comarch.android.upnp.ibcdemo.model.SensorTemperature;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.UICurrentDevice;
import com.google.common.collect.Lists;

import de.greenrobot.event.EventBus;

public abstract class BaseSensorPooling {

	public interface SensorPoolingObserver {
		public void onDevicePropertiesChanged(DeviceUpnp device,
				Map<String, Object> changes);
	}

	private final String TAG = getClass().getSimpleName();

	private final int DELAY = 1000; // 1s
	private final int UPDATE_DELAY = 15000; // 15s
	private final int REQUEST_TIMEOUT = 40000; // 40s

	private SensorPoolingObserver observer;
	private final HashMap<String, Sensor> mKnownSensors;
	private final Map<Sensor, Long> mLastUpdateTime;
	private final HashMap<String, Long> mSentRequests;
	
	private EventBus mEventBus;
	private PoolingThread mPoolingThread;

	private DeviceUpnp mCurrentDevice;
	
	private Object monitor = new Object();
	private Long mLastPool = 0L;

	private class PoolingThread {
		private Thread mThread;
		private boolean mIsRunning;

		public void start() {
			mIsRunning = true;
			mThread.start();
		}

		public void stop() {
			mIsRunning = false;
		}

		public PoolingThread(String threadName) {
			mThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (mIsRunning) {
						try {
							
							Long now = System.currentTimeMillis();
							if (!mKnownSensors.isEmpty() && now - mLastPool > DELAY) {
								updateSensors(now);
								mLastPool = now;
							} else {
								Thread.sleep(100);
							}
						} catch (InterruptedException e) {
						}
					}
				}
			}, threadName);
		}

	}

	public BaseSensorPooling(EventBus eventBus) {
		mEventBus = eventBus;
		mKnownSensors = new HashMap<String, Sensor>();
		mLastUpdateTime = new HashMap<Sensor, Long>();
		mSentRequests = new HashMap<String, Long>();
	}

	protected abstract String getThreadName();

	protected void updateSensors(Long now) {
		synchronized (monitor) {

			for (Sensor sensor : mKnownSensors.values()) {
				boolean isCurrent = mCurrentDevice!=null && mCurrentDevice.getKey().equals(sensor.getKey());
				Long lastTime = mLastUpdateTime.get(sensor);
				if (isCurrent || lastTime == null || now - lastTime > UPDATE_DELAY) {
					mLastUpdateTime.put(sensor, now);
					updateSensor(sensor);
				}
			}
		}
	}

	private void updateSensor(Sensor sensor) {
		if (sensor instanceof SensorTemperature) {
			updateTemperatureSensor((SensorTemperature) sensor);
		} else if (sensor instanceof SensorLight) {
			updateLightSensor((SensorLight) sensor);
		}
	}

	protected abstract IServiceActionEvent createServiceActionEvent(
			DeviceUpnp device, String serviceName, String actionName,
			Map<String, String> args, UpnpActionCallback callback);

	private boolean canSendRequest(String sentRequestKey) {
		boolean result = true;
		long now = System.currentTimeMillis();
		
		if (mSentRequests.containsKey(sentRequestKey)) {
			long lastRequestTime = mSentRequests.get(sentRequestKey);
			if (now - lastRequestTime > REQUEST_TIMEOUT) {
				mSentRequests.remove(sentRequestKey);
			} else {
				result = false;
			}
		}
		
		return result;
	}
	
	private void updateLightSensor(final SensorLight sensor) {
		long now = System.currentTimeMillis();
		
		String sentRequestKey = getSentRequestKey(sensor, SensorLight.BRIGHTNESS_SENSOR_URN);
		boolean sendRequestNow = canSendRequest(sentRequestKey);
		if (sendRequestNow) {
			mSentRequests.put(sentRequestKey, now);
			String sensorUrn = sensor.getSensorURNWhichBegin(SensorLight.BRIGHTNESS_SENSOR_URN);
			Map<String, String> args = sensor.prepareReadMap(sensorUrn,Lists.newArrayList(SensorLight.BRIGHTNES_SENSOR_ARG_NAME));
			mEventBus.post(createServiceActionEvent(sensor,
					Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,
					Sensor.READ_SENSOR_ACTION, args, new ReadSensorCallback(sensor,SensorLight.BRIGHTNESS_SENSOR_URN) {
						@Override
						public void updateSensor(Map<String, Object> dataRecords) {
							if (dataRecords
									.containsKey(SensorLight.BRIGHTNES_SENSOR_ARG_NAME)) {
								int temp = (Integer) dataRecords
										.get(SensorLight.BRIGHTNES_SENSOR_ARG_NAME);
								double newB = temp / 100.0;
								if (!sensor.getBrightness().equals(temp)) {
									sensor.setBrightness(newB);
									if (observer != null) {
										observer.onDevicePropertiesChanged(sensor,
												dataRecords);
									}
								}
							}
						}
					}));
		}
		
		sentRequestKey = getSentRequestKey(sensor, SensorLight.SWITCH_SENSOR_URN);
		sendRequestNow = canSendRequest(sentRequestKey);
		if (sendRequestNow) {
			mSentRequests.put(sentRequestKey, now);
			String sensorUrn = sensor.getSensorURNWhichBegin(SensorLight.SWITCH_SENSOR_URN);
			Map<String, String> args = sensor.prepareReadMap(sensorUrn,Lists.newArrayList(SensorLight.SWITCH_SENSOR_ARG_NAME));
			mEventBus.post(createServiceActionEvent(sensor,
					Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,
					Sensor.READ_SENSOR_ACTION, args, new ReadSensorCallback(sensor,SensorLight.SWITCH_SENSOR_URN) {
						@Override
						public void updateSensor(Map<String, Object> dataRecords) {
							if (dataRecords
									.containsKey(SensorLight.SWITCH_SENSOR_ARG_NAME)) {
								boolean temp = (Boolean) dataRecords
										.get(SensorLight.SWITCH_SENSOR_ARG_NAME);
								if (!sensor.isSwitched().equals(temp)) {
									sensor.setSwitched(temp);
									if (observer != null) {
										observer.onDevicePropertiesChanged(sensor,
												dataRecords);
									}
								}
							}
						}
					}));
		}
	}

	private void updateTemperatureSensor(final SensorTemperature sensor) {
		long now = System.currentTimeMillis();
		String sentRequestKey = getSentRequestKey(sensor, SensorTemperature.TEMPERATURE_SENSOR_URN);
		boolean sendRequestNow = canSendRequest(sentRequestKey);
		if (sendRequestNow) {
			mSentRequests.put(sentRequestKey, now);
			String sensorUrn = sensor
					.getSensorURNWhichBegin(SensorTemperature.TEMPERATURE_SENSOR_URN);
			Map<String, String> args = sensor.prepareReadMap(sensorUrn,
					Lists.newArrayList("Temperature Sensor"));
			mEventBus.post(createServiceActionEvent(sensor,
					Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,
					Sensor.READ_SENSOR_ACTION, args, new ReadSensorCallback(sensor,SensorTemperature.TEMPERATURE_SENSOR_URN) {
						@Override
						public void updateSensor(Map<String, Object> dataRecords) {
							if (dataRecords.containsKey("Temperature Sensor")) {
								int temp = (Integer) dataRecords
										.get("Temperature Sensor");
								if (sensor.getThemperature() != temp) {
									sensor.setThemperature(temp);
									if (observer != null) {
										observer.onDevicePropertiesChanged(sensor,
												dataRecords);
									}
								}
							}
						}
					}));
		}
	}

	public void start() {
		mKnownSensors.clear();
		mLastUpdateTime.clear();
		if (mPoolingThread != null) {
			mPoolingThread.stop();
		}
		mPoolingThread = new PoolingThread(getThreadName());
		mPoolingThread.start();
		mEventBus.registerSticky(this);

	}

	public void stop() {
		if (mPoolingThread != null) {
			mPoolingThread.stop();
		}
		mPoolingThread = null;
		mEventBus.unregister(this);
	}

	public void onEvent(UICurrentDevice event){
		mCurrentDevice = event.getDevice();
	}
	public void setObserver(SensorPoolingObserver observer) {
		this.observer = observer;
	}

	public void addSensor(Sensor sensor) {
		synchronized (monitor) {
			mKnownSensors.put(sensor.getKey(), sensor);
		}
	}

	public void removeSensor(Sensor sensor) {
		synchronized (monitor) {
			mKnownSensors.remove(sensor.getKey());
		}
	}
	private String getSentRequestKey(Sensor sensor,String sensorUrn){
		return sensor.getKey()+"/"+sensorUrn;
	}
	abstract class ReadSensorCallback extends UpnpActionCallback {
		private Sensor mSensor;
		private String mSensorUrn;
		public ReadSensorCallback(Sensor sensor, String sensorUrn) {
			mSensor = sensor;
			mSensorUrn = sensorUrn;
		}

		public abstract void updateSensor(Map<String, Object> dataRecords);

		@Override
		public void run() {
			Map<String, Object> response = getResponse();
			String dataRecords = (String) response.get("DataRecords");
			mSentRequests.remove(getSentRequestKey(mSensor,mSensorUrn));
			if (dataRecords != null) {
				Map<String, Object> records;
				try {
					records = Sensor.parseDataRecords(dataRecords);
					updateSensor(records);
				} catch (XmlPullParserException e) {
					Log.e(TAG, "Exception " + e);
				} catch (IOException e) {
					Log.e(TAG, "Exception " + e);
				}
			}
		}
	}
}
