package codefidence.droidpal;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Droidpal extends Activity implements OnTouchListener, GestureDetector.OnGestureListener {
	protected WebView webview;
    protected GestureDetector gestures;
    protected NotificationManager notifier;
    protected boolean quitting;
    
	/* Deal with links */
	private class DroidpalWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	}

	/** Deal with the option menu */
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}

	/* Save web view URL */
	protected void onSaveInstanceState(Bundle state) {
		webview.saveState(state);
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.refresh:
	      // RestClient.connect(this.getResources().getString(R.string.json_url));
	    	webview.clearView(); 
	    	webview.reload();
	        return true;
	    case R.id.quit:
	    	quitting = true;
	        finish();
	        /* Not Reached */
	    }
	    return false;
	}
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quitting = false;
        gestures = new GestureDetector(this); 
        gestures.setIsLongpressEnabled(false); 
        setContentView(R.layout.main);
        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        
        if(savedInstanceState == null) {
        	webview.loadUrl(this.getResources().getString(R.string.gui_url));
        } else {
        	webview.restoreState(savedInstanceState);
        	webview.reload();
        }
        webview.setWebViewClient(new DroidpalWebViewClient());
        webview.setOnTouchListener(this);
        webview.setFocusableInTouchMode(true);
        Log.d("DROIDPAL", "activity (re)started");

        // Get the notification manager service.
        notifier = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifier.cancel(R.drawable.icon);

        stopService(new Intent(this, DroidpalService.class));
    }
    
    protected void onPause() {
    	super.onPause();
    	if(!quitting) {
    		startService( new Intent(this, DroidpalService.class));
    	}
    }
    
    protected void onResume() {
    	super.onResume();
    	stopService(new Intent(this, DroidpalService.class));
    	notifier.cancel(R.drawable.icon);
    	webview.reload();
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX < 0 &&  webview.canGoBack()){
        	webview.goBack();
        	return true;
        }
        return false;
    }

    public void onLongPress(MotionEvent e) {
    	return;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
}

    public void onShowPress(MotionEvent e) {
        return;

	}

    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    } 
    
    /* Deal with touch events */
    public boolean onTouch(View v, MotionEvent event) {
       
    	// return gestures.onTouchEvent(event);
    	return false;
    }

    /** Deal with Back key events */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}