package com.group5.android.fd.helper;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;

import com.group5.android.fd.FdConfig;

/**
 * Behavior setup manager for common use elements
 * 
 * @author Dao Hoang Son
 * 
 */
abstract public class BehaviorHelper {

	protected static boolean flingPrepared = false;
	protected static int distanceMin = -1;
	protected static int velocityThreshold = -1;
	protected static int offPathMax = -1;

	/**
	 * Setups a dialog
	 * 
	 * @param dialog
	 * @return the dialog (the same one)
	 */
	public static Dialog setup(Dialog dialog) {
		// make our dialog to be a little more friendly
		dialog.setCanceledOnTouchOutside(true);

		return dialog;
	}

	/**
	 * Setups fling listeners and dependencies for it
	 * 
	 * @param context
	 * @param flingReady
	 */
	public static void setupFling(Context context, final FlingReady flingReady) {
		if (!BehaviorHelper.flingPrepared) {
			// only set these things up once
			BehaviorHelper.flingPrepared = true;

			DisplayMetrics dm = context.getResources().getDisplayMetrics();
			// BehaviorHelper.distanceMin = (int) (120 * dm.density);
			// BehaviorHelper.velocityThreshold = (int) (200 * dm.density);
			BehaviorHelper.offPathMax = (int) (250 * dm.density);

			ViewConfiguration vc = ViewConfiguration.get(context);
			BehaviorHelper.distanceMin = vc.getScaledTouchSlop();
			BehaviorHelper.velocityThreshold = vc
					.getScaledMinimumFlingVelocity();
		}

		final GestureDetector gestureDetector = new GestureDetector(
				new SimpleOnGestureListener() {
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						if (e1 == null || e2 == null) {
							return false;
						}

						if (Math.abs(e1.getY() - e2.getY()) < BehaviorHelper.offPathMax
								&& Math.abs(velocityX) > BehaviorHelper.velocityThreshold) {
							if (e1.getX() - e2.getX() > BehaviorHelper.distanceMin) {
								Log.i(FdConfig.DEBUG_TAG, flingReady.getClass()
										.getSimpleName()
										+ " is getting a LEFT FLING");

								flingReady.onFlingLeft();
								return true;
							} else if (e2.getX() - e1.getX() > BehaviorHelper.distanceMin) {
								Log.i(FdConfig.DEBUG_TAG, flingReady.getClass()
										.getSimpleName()
										+ " is getting a RIGHT FLING");

								flingReady.onFlingRight();
								return true;
							}
						} else if (Math.abs(e1.getX() - e2.getX()) < BehaviorHelper.offPathMax
								&& Math.abs(velocityY) > BehaviorHelper.velocityThreshold) {
							if (e1.getY() - e2.getY() > BehaviorHelper.distanceMin) {
								Log.i(FdConfig.DEBUG_TAG, flingReady.getClass()
										.getSimpleName()
										+ " is getting a UP FLING");

								flingReady.onFlingUp();
								return true;
							} else if (e2.getY() - e1.getY() > BehaviorHelper.distanceMin) {
								Log.i(FdConfig.DEBUG_TAG, flingReady.getClass()
										.getSimpleName()
										+ " is getting a DOWN FLING");

								flingReady.onFlingDown();
								return true;
							}
						}

						return false;
					}
				});

		View.OnTouchListener gestureListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};

		flingReady.addFlingListeners(gestureListener);
	}

	/**
	 * Object wants to support fling operations must implement this interface
	 * and call {@link BehaviorHelper#setupFling(Context, FlingReady)} to setup
	 * stuff
	 * 
	 * @author Dao Hoang Son
	 * 
	 */
	public interface FlingReady {
		/**
		 * Subclass must use
		 * {@link View#setOnTouchListener(android.view.View.OnTouchListener)}
		 * with its subview to enable them to listen to gesture events
		 * 
		 * @param gestureListener
		 *            the ready to use listener
		 */
		public void addFlingListeners(View.OnTouchListener gestureListener);

		/**
		 * Subclass should implement this method to do action againts a left
		 * fling
		 */
		public void onFlingLeft();

		/**
		 * Subclass should implement this method to do action againts a right
		 * fling
		 */
		public void onFlingRight();

		/**
		 * Subclass should implement this method to do action againts an up
		 * fling
		 */
		public void onFlingUp();

		/**
		 * Subclass should implement this method to do action againts a down
		 * fling
		 */
		public void onFlingDown();
	}
}
