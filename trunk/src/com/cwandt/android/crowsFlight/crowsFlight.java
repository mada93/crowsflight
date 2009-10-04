package com.cwandt.android.crowsFlight;




import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;

import android.location.Address;
import android.app.ListActivity;


public class crowsFlight extends ListActivity implements LocationListener {
	private TextView info;
	private Button searchBttn;
	private EditText addressText;
	private ViewGroup mainView;
	private ListView listCover;

	private Geocoder gc;

	boolean mAnimate=true;
	
	//compass sensor
	private static final String TAG = "Compass";

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
	double gpsAccuracy=150;
	float heading=0;
		


	private NotesDbAdapter mDbHelper;
	private Cursor mNotesCursor;
	
	public int mNoteNumber=0;
	
	private static final int ACTIVITY_CREATE=0;
	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;

    	/** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	      
	        mDbHelper = new NotesDbAdapter(this);
	        mDbHelper.open();
	        fillData();

	        //layout
	        setContentView(R.layout.main);
	        
	        mainView = (AbsoluteLayout) findViewById(R.id.mainView);        
	        info=(TextView)findViewById(R.id.infoView);
	        searchBttn=(Button)findViewById(R.id.searchButton);
	        addressText=(EditText) findViewById(R.id.address);
	        //listCover=(ListView)findViewById(R.id.listCover);
	        //listCover.setVisibility(listCover.VISIBLE);
	       
	        searchBttn.setOnClickListener(buttonListener);
	        addressText.setOnClickListener(textBoxListener);

//	        addressText.setOnKeyListener(new OnKeyListener() {
//	            public boolean onKey(View v, int keyCode, KeyEvent event) {
//	                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
//
//					listCover.setVisibility(listCover.INVISIBLE);
//	                return true;
//	                }
//					return false;
//				}
//	        });
	        
	        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1l,1l, this);
	        
	        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	        mView = new CompassView(this);

	        mainView.addView(mView);
	        
	        //geocoder
			gc = new Geocoder(this); // create new geocoder instance
	        
	        registerForContextMenu(getListView());
	    }
	    
	    //list
	    private void fillData() {
	        // Get all of the rows from the database and create the item list
	        mNotesCursor = mDbHelper.fetchAllNotes();
	        startManagingCursor(mNotesCursor);
	        
	        // Create an array to specify the fields we want to display in the list (only TITLE)
	        String[] from = new String[]{NotesDbAdapter.KEY_TITLE,NotesDbAdapter.KEY_LAT,NotesDbAdapter.KEY_LON};
	        
	        // and an array of the fields we want to bind those fields to (in this case just text1)
	        int[] to = new int[]{R.id.text1,R.id.textlat,R.id.textlon};
	        
	        // Now create a simple cursor adapter and set it to display
	        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.notes_row, mNotesCursor, from, to);
	        setListAdapter(notes);
	    }
	    
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        super.onCreateOptionsMenu(menu);
	        menu.add(0, INSERT_ID,0, "Mark Here");
	        return true;
	    }
	    
	    @Override
	    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	        switch(item.getItemId()) {
	        case INSERT_ID:
	            markHere();
	            return true;
		    }        
	        return super.onMenuItemSelected(featureId, item);
	    }

	    @Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
	        menu.add(0, DELETE_ID, 0, "Delete");
		}

	    @Override
		public boolean onContextItemSelected(MenuItem item) {
			switch(item.getItemId()) {
	    	case DELETE_ID:
	    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		        mDbHelper.deleteNote(info.id);
		        fillData();
		        return true;
			}
			return super.onContextItemSelected(item);
		}

		private void createNote() {
	    	Intent i = new Intent(this, NoteEdit.class);
	    	startActivityForResult(i, ACTIVITY_CREATE);
	    }
	    
	    @Override
	    protected void onListItemClick(ListView l, View v, int position, long id) {
	        super.onListItemClick(l, v, position, id);
	        Cursor c = mNotesCursor;
	        c.moveToPosition(position);
	        
//	        Intent i = new Intent(this, NoteEdit.class);
//	        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
//	        startActivityForResult(i, ACTIVITY_EDIT);
	        
	        street=c.getString( c.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
	        //search(searchString,false);
	        
	        aLatString=c.getString( c.getColumnIndexOrThrow(NotesDbAdapter.KEY_LAT));
	        aLonString=c.getString( c.getColumnIndexOrThrow(NotesDbAdapter.KEY_LON));
 
	        aLat=Float.valueOf(aLatString).floatValue();
	        aLon=Float.valueOf(aLonString).floatValue();
			
	        initialDistSet=false;
            
			bearing();
            updateInfo();
            	           
         
	    }


	    @Override
	    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
	        super.onActivityResult(requestCode, resultCode, intent);
	        fillData();
	    }
	    
//end list
	    
	    
	   

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
	    
	  
	  
	  
	  void markHere(){
          mDbHelper.createNote("here", aLatString, aLonString); 
          fillData();
  
	  }
	  
	  
	  

	  
	  
	  
	  void search(String searchInput, boolean save){

			try {
				List<Address> foundAdresses = gc.getFromLocationName(searchInput, 5); // Search addresses
						
				
//				String center=myLat+","+myLon;
//				String url="http://ajax.googleapis.com/ajax/services/search/local?v=1.0&rsz=small&q="+searchInput+"&sll="+center+"";
//				InputStream source=getInputStreamFromUrl(url);

				

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
						
						aLatString=Float.toString((float) aLat);
						aLonString=Float.toString((float) aLon);

					}
					initialDistSet=false;
	                
					bearing();
	                updateInfo();
	                	                	
	                if(save==true){
	                mDbHelper.createNote(searchInput, aLatString, aLonString); 
	                fillData();
	            	}
	                

				}
			} catch (Exception e) {
				// @todo: Show error message
				//getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			}
      
		  
	  }
	    private OnClickListener buttonListener = new OnClickListener() {
	        public void onClick(View v) {
	        	
	          	String addressInput = addressText.getText().toString(); // Get input text
	          	search(addressInput,true);
	  
	        	
	        	
	        }
	    };

	    private OnClickListener textBoxListener = new OnClickListener() {
	        public void onClick(View v) {
	        	
				//listCover.setVisibility(listCover.INVISIBLE);

	        	
	        	
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
				if(ac>150)ac=150;
				
				
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
	            Typeface.create("Helvetica", 1);
	            
	

	            
	            String distanceString;
	            DecimalFormat df2 = new DecimalFormat("0.##");

	            if(distance<1000){
		            double meters = new Double(df2.format(distance)).doubleValue();

	            	distanceString=Double.toString( meters)+" meters";
	            }

	            else{
	            	double  kmDist=distance/1000;
		            double km = new Double(df2.format(kmDist)).doubleValue();

		            distanceString=Double.toString(km) + " km";
	            }
	            
	            paint.setTextSize(30);
	            paint.setTypeface(type);
	            canvas.drawText(distanceString,textX,15,mPaint);
	           
	            paint.setTextSize(15);
	            canvas.drawText(street,textX,35,mPaint);
	            
	            paint.setTextSize(10);
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


