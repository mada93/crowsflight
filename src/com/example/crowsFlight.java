package com.example;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.util.Config;
import android.util.Log;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.graphics.*;
import android.view.View.OnClickListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.location.Address;

import com.example.searchProvider;



public class crowsFlight extends Activity implements LocationListener {
	//geocoder
	private View compassBox;

	private TextView info;
	private Button searchBttn;
	private EditText addressText;
	private ViewGroup mainView;

	private Geocoder gc;

	boolean mAnimate=true;
	
	//compass sensor
	private static final String TAG = "Compass";

	private static final int SEARCH = 0;
	private SensorManager mSensorManager;
	private CompassView mView;
	private float[] mValues;
	private LocationManager lm;
	String myLatString;
	String myLonString;
	double myLat;
	double myLon;
	String aLatString;
	String aLonString;
	double aLat;
	double aLon;
	String street="";
	
	//private FrameLayout compassFrame;
public float distance=10;
public float initialDist=10;
boolean initialDistSet=false;
double bearing=0;

	
    	/** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        
	        mainView = (AbsoluteLayout) findViewById(R.id.mainView);
	        
	        info=(TextView)findViewById(R.id.infoView);
	        searchBttn=(Button)findViewById(R.id.searchButton);
	        addressText=(EditText) findViewById(R.id.address);

	        
	        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1l,1l, this);
	        
	        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	        mView = new CompassView(this);

	        mainView.addView(mView);
	        
	        //geocoder
			gc = new Geocoder(this); // create new geocoder instance
	        searchBttn.setOnClickListener(buttonListener);
	    
	    }
	    
	    
	    /* Creates the menu items */
	    public boolean onCreateOptionsMenu(Menu menu) {
	        menu.add(0, SEARCH, 0, "Search");
	        return true;
	    }

	    /* Handles item selections */
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case SEARCH:
	            search();
	            return true;
	        }
	        return false;
	    }

	    
	    public void search(){
	    	//addressText.draw(mView);
	    	
	    }
	    
	    public boolean onSearchRequested(){
	    	
	    	
			return initialDistSet;

	    }


	    
	    private OnClickListener buttonListener = new OnClickListener() {
	        public void onClick(View v) {
	        	String addressInput = addressText.getText().toString(); // Get input text
				try {
					List<Address> foundAdresses = gc.getFromLocationName(addressInput, 5); // Search addresses
					//searchProvider.saveRecentQuery(addressInput,null);
					
					if (foundAdresses==null) { // if no address found,
						// display an error
					} else { // else display address on map
						for (int i = 0; i < foundAdresses.size(); ++i) {
							//dropdown list of found addresses
							Address x = foundAdresses.get(i);
							street=x.getAddressLine(0);
						
							aLat = (float)x.getLatitude();
							aLon = (float)x.getLongitude();
						}
						initialDistSet=false;
		                
						bearing();
		                updateInfo();

						//navigateToLocation((lat * 1000000), (lon * 1000000),myMap); // display the found address
					}
				} catch (Exception e) {
					// @todo: Show error message
					//getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				 
				}
	        }
	    };


	    void updateInfo(){
  	        info.setText("lat: "+myLat+", lon:"+myLon+"\nalat: "+aLat+", alon: "+aLon+"\nbearing: "+bearing+"\ndistance: "+distance+"/"+initialDist);

	    	
	    }
	    
        public void onLocationChanged(Location arg0) {
                myLatString = String.valueOf(arg0.getLatitude());
                myLonString = String.valueOf(arg0.getLongitude());
                
                myLat = (float) arg0.getLatitude();
                myLon = (float) arg0.getLongitude();
               
                 bearing();
                updateInfo();
         }
        
        
        public void bearing(){
        	

        	double atanx=(Math.cos(Math.toRadians(aLat))*Math.sin(Math.toRadians(myLat))-Math.sin(Math.toRadians(aLat))*Math.cos(Math.toRadians(myLat))*Math.cos(Math.toRadians(myLon-aLon)));
        	double atany=(Math.sin(Math.toRadians(myLon-aLon))*Math.cos(Math.toRadians(myLat)));
        	 
         	
        	bearing=Math.toDegrees(Math.atan2(atany, atanx));
        	bearing=(bearing+180)%360;
        	
        	
        	
            float[] distanceArray = new float[3];
            if(aLat!=0){
            Location.distanceBetween(myLat, myLon, aLat, aLon, distanceArray);
            
            distance=distanceArray[0];
            //bearing=distanceArray[1];
            }
            
            if(initialDistSet==false){
            	initialDist=distance;
            	initialDistSet=true;
            }
            
            
        }
        
	    public class CompassView extends View {
	        private Paint   mPaint = new Paint();
	        private Path    mPath = new Path();
	        private Path    distPath = new Path();
            int compassRadius=-135;
            int triangleSize=30;
            int distPathRadius=-100;

            
	        public CompassView(Context context) {
	            super(context);

	            // Construct a wedge-shaped path

	            mPath.moveTo(0, compassRadius);
	            mPath.lineTo(triangleSize/2, compassRadius+triangleSize);  
	            mPath.lineTo(-triangleSize/2, compassRadius+triangleSize);         
	            mPath.lineTo(0, compassRadius);         
	            mPath.close();
	            
	            
//	            distPath.moveTo(distPathRadius,0);
//	            for(float i=0;i<distance/initialDist*2*Math.PI;i+=.1){
//	            distPath.lineTo( distPathRadius*(float)Math.cos(i), distPathRadius*(float)Math.sin(i));
//	            }
	            

	        }

	        @Override protected void onDraw(Canvas canvas) {
	            
	            Paint paint = mPaint;
	            canvas.setViewport(250, 250);
	            //canvas.drawColor(Color.BLACK);	
	            paint.setAntiAlias(true);

	            int w = canvas.getWidth();
	            int h = canvas.getHeight();
	            int cx = w / 2;
	            int cy = h / 2;

	            canvas.translate(cx, cy);

	            //circle
	            paint.setStrokeWidth(5);
	            paint.setColor(Color.argb(50, 100, 100, 100));
	            paint.setStyle(Paint.Style.STROKE);
	            canvas.drawCircle(0, 0, distPathRadius,mPaint);

	            
	            //northarrow
	            canvas.save();
	            if (mValues != null) {            
	                canvas.rotate(-mValues[0]);
	            }
	            paint.setColor(Color.argb(255, 150, 0, 0));
	            paint.setStyle(Paint.Style.FILL);
	            //canvas.drawPath(mPath, mPaint);
	            paint.setStyle(Paint.Style.STROKE);
	            paint.setStrokeWidth(3);
	            canvas.drawLine(0, distPathRadius+20, 0, distPathRadius, mPaint);
	            canvas.restore();

	            
	            
	            //pointer
	            canvas.save();
	            if (mValues != null) {            
	                canvas.rotate((float) (-mValues[0]+bearing));
	            }

	            //arrow
	            paint.setColor(Color.WHITE);
	            paint.setStyle(Paint.Style.FILL);
	            canvas.drawPath(mPath, mPaint);
	            
	            //distance arc
	            canvas.save();
	            canvas.rotate(90);
	            paint.setColor(Color.WHITE);
	            paint.setStrokeWidth(4);
	            paint.setStrokeCap(Paint.Cap.SQUARE);	            
	            paint.setStyle(Paint.Style.STROKE);
	            distPath.moveTo(distPathRadius,0);
//	            for(float i=0;i<distance/initialDist*2*Math.PI;i+=.5){
//	            distPath.lineTo( distPathRadius*(float)Math.cos(i), distPathRadius*(float)Math.sin(i));
//	            }
//	            canvas.drawPath(distPath, mPaint);
	            int radius=100;
	            RectF rect=new RectF(-radius,-radius,radius,radius);
	            canvas.drawArc(rect,0,(float) (distance/initialDist*360),false,mPaint);
	            
	            canvas.restore();
	            
	            //centerpoint
	            paint.setStyle(Paint.Style.FILL);
	            paint.setColor(Color.argb(50, 255, 0, 0));
	            canvas.drawCircle(0, 0, 2, mPaint);
	            canvas.restore();
	            


	            paint.setColor(Color.WHITE);
	            int textX=-80;
	            
	            Typeface type=null;
	            type.create("Helvetica", 1);
	            
	            paint.setTextSize(30);
	            paint.setTypeface(type);
	            canvas.drawText(Integer.toString((int) distance)+" meters",textX,15,mPaint);
	            paint.setTextSize(15);
	            canvas.drawText(street,textX,35,mPaint);

	            //if (lat!=null) canvas.drawText("lat: "+lat,textX,10,mPaint);
	            //if (lon!=null) canvas.drawText("lon: "+lon,textX,20,mPaint);

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
 	        lm.removeUpdates(this);

super.onStop();
 	    }
 	    

       

 }



