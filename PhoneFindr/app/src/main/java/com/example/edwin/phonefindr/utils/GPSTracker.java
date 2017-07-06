package com.example.edwin.phonefindr.utils;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

public class GPSTracker extends Service implements LocationListener{

	private final Context context;
	
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	
	Location location;
	
	 double latitude;
	 double longitude;
	
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60;
	
	protected LocationManager locationManager;

	
	public GPSTracker(Context context) {
		this.context = context;
		getLocation();

	}
	
	public Location getLocation() {
		try {
			locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
			
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if(!isGPSEnabled && !isNetworkEnabled) {
				// cannot get location
			} else {
				this.canGetLocation = true;
				
				if (isNetworkEnabled) {
					try {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					}catch(SecurityException e){

					}

					if (locationManager != null) {
						try {
							location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						}catch(SecurityException e){

						}

						if (location != null) {
							
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}

				}
				
				if(isGPSEnabled) {
					if(location == null) {
						try {
							locationManager.requestLocationUpdates(
									LocationManager.GPS_PROVIDER,
									MIN_TIME_BW_UPDATES,
									MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						}catch(SecurityException e){

						}
						
						if(locationManager != null) {
							try {
								location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							}catch(SecurityException e){

							}
							
							if(location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
 			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return location;
	}
	
	
	public void stopUsingGPS() {
		if(locationManager != null) {
			try {
				locationManager.removeUpdates(GPSTracker.this);
			}catch(SecurityException e){

			}
		}
	}
	
	public double getLatitude() {
		if(location != null) {
			latitude = location.getLatitude();
		}
		return latitude;
	}
	
	public double getLongitude() {
		if(location != null) {
			longitude = location.getLongitude();
		}
		
		return longitude;
	}
	
	public boolean canGetLocation() {
		return this.canGetLocation;
	}
	
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		
		alertDialog.setTitle("Location services turned off");
		
		alertDialog.setMessage("PhoneFindr would like to use GPS");
		
		alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				context.startActivity(intent);
			}
		});
		
		alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

			}
		});
		
		alertDialog.show();
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
