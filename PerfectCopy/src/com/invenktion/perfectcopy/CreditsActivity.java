package com.invenktion.perfectcopy;

import com.invenktion.perfectcopy.manager.ApplicationManager;
import com.invenktion.perfectcopy.utils.FontFactory;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;


public class CreditsActivity extends Activity{
	//Typeface font; 
	float DENSITY = 1.0f;
    private boolean exit = false;
    private boolean fingerDown = false;
    ScrollView scrollView;
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	exit = true;
			finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

    private boolean checkApplicationKill() {
		if(ApplicationManager.APPLICATION_KILLED_BY_SYSTEM == null) {
			finish();
			return true;
		}
		return false;
	}
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean finish = checkApplicationKill();
        if(finish) return;
        setContentView(R.layout.credits);
        this.DENSITY = getApplicationContext().getResources().getDisplayMetrics().density;
      
        
        TextView leadartisttext = (TextView)findViewById(R.id.creativedesignertext);
        TextView leadprogrammertext = (TextView)findViewById(R.id.leadprogrammertext);
        TextView gamedesigntext = (TextView)findViewById(R.id.gamedesigntext);
        TextView leadartisttextvalue = (TextView)findViewById(R.id.creativedesignertextvalue);
        TextView leadprogrammertextvalue = (TextView)findViewById(R.id.leadprogrammertextvalue);
        TextView gamedesigntextvalue = (TextView)findViewById(R.id.gamedesigntextvalue);
        TextView gamedesigntextvalue2 = (TextView)findViewById(R.id.gamedesigntextvalue2);
        //TextView dev1 = (TextView)findViewById(R.id.developed1);
        TextView dev2 = (TextView)findViewById(R.id.developed2);
        
        TextView follow1 = (TextView)findViewById(R.id.follow1);
        TextView whois1 = (TextView)findViewById(R.id.whois1);
        
        leadartisttext.setTypeface(FontFactory.getFont1(getApplicationContext()));
        leadprogrammertext.setTypeface(FontFactory.getFont1(getApplicationContext()));
        gamedesigntext.setTypeface(FontFactory.getFont1(getApplicationContext()));
        leadartisttextvalue.setTypeface(FontFactory.getFont1(getApplicationContext()));
        leadprogrammertextvalue.setTypeface(FontFactory.getFont1(getApplicationContext()));
        gamedesigntextvalue.setTypeface(FontFactory.getFont1(getApplicationContext()));
        gamedesigntextvalue2.setTypeface(FontFactory.getFont1(getApplicationContext()));
        //dev1.setTypeface(FontFactory.getFont1(getApplicationContext()));
        dev2.setTypeface(FontFactory.getFont1(getApplicationContext()));
        follow1.setTypeface(FontFactory.getFont1(getApplicationContext()));
        whois1.setTypeface(FontFactory.getFont1(getApplicationContext()));

        scrollView = (ScrollView)findViewById(R.id.scrollcredits);
      
        //Imposto l'altezza delle immagini di inizio e fine trasparenti nella scrollview
        ImageView topTrasp = (ImageView)findViewById(R.id.scrolltrasptop);
        ImageView bottomTrasp = (ImageView)findViewById(R.id.scrolltraspbottom);
        
        Display display = getWindowManager().getDefaultDisplay(); 
        int width = display.getWidth();
        int height = display.getHeight();
        
        topTrasp.setLayoutParams(new LinearLayout.LayoutParams(10,height));
        bottomTrasp.setLayoutParams(new LinearLayout.LayoutParams(10,height));
        
        dev2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.invenktion.com"));
		        startActivity(myIntent);
			}
		});
        whois1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.invenktion.com"));
		        startActivity(myIntent);
			}
		});
        startScrollingThread();
    }
    
    //Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();
    //Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
	        public void run() {
	            updateUI();
	        }
    };
    
	private void updateUI() {
		if(!exit && !fingerDown) {
			//Log.d("","Y = "+scrollView.getScrollY());
			//Log.d("","MAXAUMOUNT  ="+scrollView.getMaxScrollAmount());
			//Log.d("","CHILD = "+scrollView.getChildAt(0).getHeight());
			if(scrollView.getScrollY() >= (scrollView.getChildAt(0).getHeight()-scrollView.getMeasuredHeight())) {
				scrollView.smoothScrollTo(0,0);
			}else {
				scrollView.smoothScrollTo(0,scrollView.getScrollY()+1);
			}
		}
	}
    
    protected void startScrollingThread() {
        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            public void run() {
            	while(!exit) {
            		try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					mHandler.post(mUpdateResults);
            	}
            }
        };
        t.start();
    }
}
