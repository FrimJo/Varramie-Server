package com.spots.varramie;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SpotSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
	private SurfaceHolder sh;
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private SpotThread thread;
	private Context ctx;
	
	private float initX = 0;
	private float initY = 0;
	
	private List<Point> pointsToBeDrawn = new ArrayList<Point>();

	
	public SpotSurfaceView(Context context) {
		super(context);
		sh = getHolder();
		sh.addCallback(this);
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.FILL);
		paint.setTextSize(24.0f);
		ctx = context;
	    setFocusable(true); // make sure we get key events
	}
	
	public SpotThread getThread() {
	    return thread;
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		thread = new SpotThread(sh, ctx, new Handler());
		Client.INSTANCE.init(thread);
	    thread.setRunning(true);
	    thread.start();
		
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		thread.setSurfaceSize(width, height);
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
	    boolean retry = true;
	    thread.setRunning(false);
	    while (retry) {
	      try {
	        thread.join();
	        retry = false;
	      } catch (InterruptedException e) {
	      }
	    }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();
		if (action==MotionEvent.ACTION_DOWN){
			int x = Math.round(event.getX());
			int y = Math.round(event.getY());
			this.pointsToBeDrawn.add(new Point(x,y));
			
			try {
				Client.INSTANCE.sendTouch(x, y);
			} catch (IOException e) {
				Client.INSTANCE.println("IOException in SpotSurfaceView");
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	class SpotThread extends Thread implements IGUI{
		  private int canvasWidth = 200;
		  private int canvasHeight = 400;
		  private boolean run = false;
		    
		  private ArrayList<String> console = new ArrayList<String>(){{add("");}};
		  
		  private float bubbleX;
		  private float bubbleY;
		  private float headingX;
		  private float headingY;
		    
		  public SpotThread(SurfaceHolder surfaceHolder, Context context,
		         Handler handler) {
		    sh = surfaceHolder;
		    ctx = context;
		  }
		  public void doStart() {
		    synchronized (sh) {
		      // Start bubble in centre and create some random motion
		      bubbleX = canvasWidth / 2;
		      bubbleY = canvasHeight / 2;
		      headingX = (float) (-1 + (Math.random() * 2));
		      headingY = (float) (-1 + (Math.random() * 2));
		    }
		  }
		  public void run() {
		    while (run) {
		      Canvas c = null;
		      try {
		        c = sh.lockCanvas(null);
		        synchronized (sh) {
		          doDraw(c);
		        }
		      } finally {
		        if (c != null) {
		          sh.unlockCanvasAndPost(c);
		        }
		      }
		    }
		  }
		    
		  public void setRunning(boolean b) { 
		    run = b;
		  }
		  public void setSurfaceSize(int width, int height) {
		    synchronized (sh) {
		      canvasWidth = width;
		      canvasHeight = height;
		      doStart();
		    }
		  }
		  private void doDraw(Canvas canvas) {
			  canvas.save();
				canvas.restore();
				canvas.drawColor(Color.BLACK);
				for(int i = 0; i < console.size(); i++){
					String str = console.get(i);
					canvas.drawText(str, 20, 20+i*20, paint);
				}
				
				for(int i = 0; i < pointsToBeDrawn.size(); i++){
					Point p = pointsToBeDrawn.get(i);
					canvas.drawCircle(p.x, p.y, 50, paint);
				}
				
		  }
		  
			@Override
			public void print(String str) {
				this.console.add(str);
				
			}
			@Override
			public void println(String str) {
				this.console.add(str);
			}
			@Override
			public String getInput() {
				return "";
			}
			
			@Override
			public void receiveTouch(int x, int y){
				pointsToBeDrawn.add(new Point(x, y));
			}
		}
	
}
