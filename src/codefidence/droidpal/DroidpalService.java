package codefidence.droidpal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class DroidpalService extends Service {
	
	private NotificationManager notifier;
	private volatile Looper srv_loop;
    private volatile ServiceHandler srv_handler;
    private Intent action_intent;
    private HandlerThread thread;
    private boolean quitting;
    private int comments;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
        	
            while(!quitting) {
            	
            	int new_comments = RestClient.get_comment_count(getResources().getString(R.string.json_url));
        		
        		if(comments != -1 && new_comments > comments) {
        			send_notification(new_comments);
        		}
        		
        		if(new_comments != -1) {
        			comments = new_comments;
        		}
            	
            	long endTime = System.currentTimeMillis() + 2*1000;
            	while (System.currentTimeMillis() < endTime && (!quitting)) {
            		synchronized (this) {
            			try {
            				wait(endTime - System.currentTimeMillis());
            			} catch (Exception e) {
            			}
            		}
            	}
            	
            };

            Log.i("ServiceStartArguments", "Done with #" + msg.arg1);
            notifier.cancel(R.drawable.icon);
            stopSelf(msg.arg1);
        }

    };

	
	@Override
	public IBinder onBind(Intent arg0) {
		// Empty stub
		return null;
	}
	
	@Override
    public void onCreate() {
		super.onCreate();
		Log.i("DROIDPALSERVICE", "Service created");
		
		quitting = false;
		comments = -1;
		
		// Get the notification manager service.
        notifier = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        // Set up intent for calling our action when needed
        action_intent = new Intent(this, Droidpal.class);
        
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        thread = new HandlerThread("Droidpal Service");
        thread.start();

        srv_loop = thread.getLooper();
        srv_handler = new ServiceHandler(srv_loop);

		
	}
	
	private void send_notification(int num_events) {
	
		String tickerText = num_events + " new comments in Druapl.";
		String titleText = "Driodpal";
		
		Notification notification = new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
	
		// The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, action_intent, 0);
        
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, titleText, tickerText, contentIntent);
        
        // notification.defaults = Notification.DEFAULT_SOUND;
        
        notification.number = num_events;

        notification.vibrate = new long[] {100, 250, 100, 500}; 
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xFF0000FF;
        notification.ledOffMS = 250;
        notification.ledOnMS = 250;
        notification.defaults = Notification.DEFAULT_SOUND;
        
		notifier.notify(R.drawable.icon, notification);
	}
	
	@Override 
	public void onStart(Intent intent, int start_id) {
		super.onStart(intent, start_id);
		Log.i("DROIDPALSERVICE", "Service started");
        
		Message msg = srv_handler.obtainMessage();
        msg.arg1 = start_id;
        msg.obj = intent.getExtras();
        srv_handler.sendMessage(msg);

	}
	
	public void onDestroy() {
		super.onDestroy();
		srv_loop.quit();
		quitting = true;
		thread.interrupt();

		Log.i("DROIDPALSERVICE", "Service destryoed");
		
		
	}
}