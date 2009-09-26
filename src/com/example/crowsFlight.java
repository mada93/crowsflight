
package com.example;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.graphics.*;


public class crowsFlight extends Activity implements LocationListener {
	//compass sensor
	private static final String TAG = "Compass";

	private SensorManager mSensorManager;
	private SampleView mView;
	private float[] mValues;
	
	
	//location
	
		private LocationManager lm;

    	/** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //setContentView(R.layout.main);
	
	        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1l,1l, this);
	    
	        
	        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	        mView = new SampleView(this);
	        setContentView(mView);
	        
	    }

        public void onLocationChanged(Location arg0) {
                 String lat = String.valueOf(arg0.getLatitude());
                 String lon = String.valueOf(arg0.getLongitude());
                 Log.e("GPS", "location changed: lat="+lat+", lon="+lon);
                 TextView tv = new TextView(this);
                 tv.setText(lat+","+lon);
                 //setContentView(tv);

                 
         }
        


	    private final SensorListener mListener = new SensorListener() {

	        public void onSensorChanged(int sensor, float[] values) {
	            if (Config.LOGD) Log.d(TAG, "sensorChanged (" + values[0] + ", " + values[1] + ", " + values[2] + ")");
	            mValues = values;
	            if (mView != null) {
	                mView.invalidate();
	            }
	        }

	        public void onAccuracyChanged(int sensor, int accuracy) {
	            // TODO Auto-generated method stub

	        }
	    };
	    
	    public class SampleView extends View {
	        private Paint   mPaint = new Paint();
	        private Path    mPath = new Path();
	        private boolean mAnimate;
	        private long    mNextTime;

	        public SampleView(Context context) {
	            super(context);

	            // Construct a wedge-shaped path
	            int compassNorthTip=-100;
	            
	            mPath.moveTo(0, compassNorthTip);
	            mPath.lineTo(10, 0);  
	            mPath.lineTo(0, 20);
	            mPath.lineTo(-10, 0);
	            mPath.lineTo(0, compassNorthTip);         
	            mPath.close();
	        }

	        

       
	        
	        
	        
	        @Override protected void onDraw(Canvas canvas) {
	            Paint paint = mPaint;

	            canvas.drawColor(Color.BLACK);

	            paint.setAntiAlias(true);
	            paint.setColor(Color.WHITE);
	            paint.setStyle(Paint.Style.FILL);

	            int w = canvas.getWidth();
	            int h = canvas.getHeight();
	            int cx = w / 2;
	            int cy = h / 2;

	            canvas.translate(cx, cy);
	            if (mValues != null) {            
	                canvas.rotate(-mValues[0]);
	            }
	            canvas.drawPath(mPath, mPaint);
	        }
	        
	  	   @Override
	       protected void onAttachedToWindow() {
	           mAnimate = true;
	           super.onAttachedToWindow();
	       }

	       @Override
	       protected void onDetachedFromWindow() {
	           mAnimate = false;
	           super.onDetachedFromWindow();
	       }

	    }
	        
	        
	        
	        
	    
         public void onProviderDisabled(String arg0) {
                 Log.e("GPS", "provider disabled " + arg0);
         }
         public void onProviderEnabled(String arg0) {
                 Log.e("GPS", "provider enabled " + arg0);
         }
         public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                 Log.e("GPS", "status changed to " + arg0 + " [" + arg1 + "]");
         }
         
         
         

 		@Override
 	    protected void onResume()
 	    {
 	        if (Config.LOGD) Log.d(TAG, "onResume");
 	        super.onResume();
 	        mSensorManager.registerListener(mListener, 
 	        		SensorManager.SENSOR_ORIENTATION,
 	        		SensorManager.SENSOR_DELAY_GAME);
 	    }

 	    @Override
 	    protected void onStop()
 	    {
 	        if (Config.LOGD) Log.d(TAG, "onStop");
 	        mSensorManager.unregisterListener(mListener);
 	        super.onStop();
 	    }
 	    

       

 }

