package com.example;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.widget.AbsoluteLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.*;
import android.view.View.OnClickListener;
import android.location.Address;
import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;


public class crowsFlight extends ListActivity implements LocationListener {
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
	String locality="";
	
	//private FrameLayout compassFrame;
public float distance=0;
public float initialDist=1;
boolean initialDistSet=false;
double bearing=0;
double gpsAccuracy=0;
float heading=0;
	


private dbAdapter db;

public int mNoteNumber=0;

public static final int INSERT_ID = Menu.FIRST;

    	/** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        db = new dbAdapter(this);
		        db.open();
	        fillData();

	        //layout
	          
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
	    
	    
	    private SQLiteDatabase openDatabase(String mYDATABASENAME, Object object) {
			// TODO Auto-generated method stub
			return null;
		}


		private void createDatabase(String mYDATABASENAME, int i,
				int modePrivate, Object object) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
		    boolean result = super.onCreateOptionsMenu(menu);
		    menu.add(0, INSERT_ID, 0, "Add Item");
		    return result;
		}

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case INSERT_ID:
	            //createNote();
	            return true;
	        }
	       
	        return super.onOptionsItemSelected(item);
	    }
	    
	    private void createNote(String note) {
	        db.createNote(note, "");
	        fillData();
	    }
	    
	    
	    private void fillData() {
	        // Get all of the notes from the database and create the item list
	        Cursor c = db.fetchAllNotes();
	        startManagingCursor(c);

	        String[] from = new String[] { dbAdapter.KEY_TITLE };
	        int[] to = new int[] { R.id.text1 };
	        
	        // Now create an array adapter and set it to display using our row
	        SimpleCursorAdapter notes =new SimpleCursorAdapter(this, R.layout.row, c, from, to);
	        setListAdapter(notes);
	    }
	    
	   

	  public static InputStream getInputStreamFromUrl(String url) {  
	    	     InputStream content = null;  
	    	     try {  
	    	       HttpClient httpclient = new DefaultHttpClient();  
	    	       HttpResponse response = httpclient.execute(new HttpGet(url));  
	    	       content = response.getEntity().getContent();  
	    	     } 
	    	     
	    	     catch (Exception e) {  
	    	       Log.e("[GET REQUEST]", "Network exception");  
	    	     }  
	    	       return content;  
	    	   }  
	    
	    private OnClickListener buttonListener = new OnClickListener() {
	        public void onClick(View v) {
	        	String addressInput = addressText.getText().toString(); // Get input text
				try {
					List<Address> foundAdresses = gc.getFromLocationName(addressInput, 5); // Search addresses
					
					//searchProvider.saveRecentQuery(addressInput,null);
					
					String center=myLat+","+myLon;
					String url="http://ajax.googleapis.com/ajax/services/search/local?v=1.0&rsz=small&q="+addressInput+"&sll="+center+"";
					//etInputStreamFromUrl(url);
					
					
					
					if (foundAdresses==null) { // if no address found,
						// display an error
					} else { // else display address on map
						for (int i = 0; i < foundAdresses.size(); ++i) {
							//dropdown list of found addresses
							Address x = foundAdresses.get(i);
							street=x.getAddressLine(0);
							locality=x.getLocality();
			                if(locality==null)locality="";

							aLat = (float)x.getLatitude();
							aLon = (float)x.getLongitude();
							
					        
						}
						initialDistSet=false;
		                
						bearing();
		                updateInfo();
		                
		                
		                db.createNote(street+", "+locality, "");
		                fillData();
		                
						//navigateToLocation((lat * 1000000), (lon * 1000000),myMap); // display the found address
					}
				} catch (Exception e) {
					// @todo: Show error message
					//getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				 
				}
	        }
	    };


	    void updateInfo(){
  	        info.setText("accuracy: "+gpsAccuracy+"\nlat: "+myLat+", lon:"+myLon+"\nalat: "+aLat+", alon: "+aLon+"\nheading: "+heading+"\nbearing: "+bearing+"\ndistance: "+distance+"/"+initialDist);

	    	
	    }
	    
        public void onLocationChanged(Location arg0) {
                myLatString = String.valueOf(arg0.getLatitude());
                myLonString = String.valueOf(arg0.getLongitude());
                
                myLat = (float) arg0.getLatitude();
                myLon = (float) arg0.getLongitude();
               
                gpsAccuracy=arg0.getAccuracy();
                
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
            int compassRadius=110;
            int triangleSize=30;
            int distPathRadius=100;

            
	        public CompassView(Context context) {
	            super(context);

	            // Construct a wedge-shaped path

	            mPath.moveTo(0, compassRadius);
	            mPath.lineTo(triangleSize/2, compassRadius);  
	            mPath.lineTo(0, compassRadius+triangleSize);  
	            mPath.lineTo(-triangleSize/2, compassRadius);         
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
	            paint.setColor(Color.argb(100,120,120,120));
	            paint.setStyle(Paint.Style.STROKE);
	            canvas.drawCircle(0, 0, distPathRadius,mPaint);

	            
	            //northarrow
	            canvas.save();
	            if (mValues != null) {            
	                canvas.rotate(-heading+180);
	            }
	            paint.setColor(Color.argb(255, 150, 0, 0));
	            paint.setStyle(Paint.Style.FILL);
	            //canvas.drawPath(mPath, mPaint);
	            paint.setStyle(Paint.Style.STROKE);
	            paint.setStrokeWidth(3);
	            canvas.drawLine(0, distPathRadius-20, 0, distPathRadius, mPaint);
	            canvas.restore();

	            
	            
	            //pointer
	            canvas.save();
	            if (mValues != null) {            
	                canvas.rotate((float) (-heading+bearing+180));
	            }


	            
	            
	            
	            
	            
	            //distance arc
	            canvas.save();
	            canvas.rotate(90);
	            paint.setStrokeWidth(4);
	            paint.setStrokeCap(Paint.Cap.SQUARE);	            
	            paint.setStyle(Paint.Style.STROKE);
	            int radius=100;
	            RectF rect=new RectF(-radius,-radius,radius,radius);

	            paint.setColor(Color.rgb(20, 20, 20));
	            canvas.drawArc(rect,0,360,false,mPaint);

	            paint.setColor(Color.WHITE);
	            float arc=distance/initialDist;
	            if(distance==0)arc=1;
	            if(arc>1)arc=1;
	           
//	            distPath.moveTo(distPathRadius+10,0);
//	            canvas.drawLine( distPathRadius+10, 0, distPathRadius,0, mPaint);
	            distPath.moveTo(distPathRadius,0);
	            canvas.drawArc(rect,0,(float)(arc*355.0),false,mPaint);

	            canvas.restore();

	            
	            //arrow

				int ac=(int) (gpsAccuracy);
				if(ac>200)ac=200;
				
				
				paint.setColor(Color.rgb(255-ac,255-ac,255-ac));
				paint.setStyle(Paint.Style.FILL);
				
				
				canvas.save();
				canvas.drawPath(mPath, mPaint);
				
				
				//overlay Arrow
				//canvas.translate(0, (float) (-45+(ac*40)));
				//canvas.drawPath(mPath, mPaint);
				
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
	            
	
	            String distanceString;
	            if(distance<1000){
	            distanceString=Float.toString( distance)+" meters";
	            }
	            else{
		            distanceString=Float.toString( distance/1000) + " km";
 	
	            }
	            paint.setTextSize(30);
	            paint.setTypeface(type);
	            canvas.drawText(distanceString,textX,15,mPaint);
	           
	            paint.setTextSize(20);
	            canvas.drawText(street,textX,35,mPaint);
	            
	            paint.setTextSize(15);
	            canvas.drawText(locality,textX,45,mPaint);

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
 	            heading=mValues[0];
 	            
                updateInfo();

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
 	        
	        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1l,1l, this);

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



