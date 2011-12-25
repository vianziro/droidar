package gamelogic;

import gui.SimpleCustomView;
import worldData.Entity;
import worldData.UpdateTimer;
import worldData.Updateable;
import worldData.Visitor;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewParent;
import de.rwth.R;

public class GameElementView extends SimpleCustomView implements Entity {

	private static final int DEFAULT_VIEW_SIZE_IN_DIP = 80;
	private static final int MARGIN = 4;
	private static final float DEFAULT_UPDATE_SPEED = 0.1f;
	private static final String LOG_TAG = "GameElementView";

	private Paint paint;
	private Paint loadingPaint;
	private Paint loadingLinePaint;

	float myLoadingAngle = 160;

	private UpdateTimer myTimer;
	private float myUpdateSpeed = DEFAULT_UPDATE_SPEED;
	private double myTouchScaleFactor = 5;
	private Bitmap myIcon;
	private Bitmap mutable;
	private Canvas stampCanvas;

	private int myWidth;
	private int myHalfWidth;
	private int myHeight;
	private int myHalfHeight;

	// private String debug;

	public GameElementView(Context context, int iconid) {
		super(context);
		init((int) dipToPixels(DEFAULT_VIEW_SIZE_IN_DIP),
				loadBitmapFromId(context, iconid));
	}

	@Deprecated
	public GameElementView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init((int) dipToPixels(DEFAULT_VIEW_SIZE_IN_DIP),
				loadBitmapFromId(context, R.drawable.hippopotamus64));
	}

	public void setUpdateSpeed(float myUpdateSpeed) {
		this.myUpdateSpeed = myUpdateSpeed;
	}

	public void setIcon(Bitmap icon) {
		myIcon = icon;
		resizeIconToViewSize();
	}

	private void resizeIconToViewSize() {
		if (myIcon != null) {
			myIcon = resizeBitmap(myIcon, myHeight, myWidth);
			myIcon = createBitmapWithRoundCorners(myIcon, 8f);
		}
	}

	private void drawLoadingCircle(Canvas canvas, int width, int heigth,
			Paint paint) {
		float x = width * 0.5f;
		RectF arcRect = new RectF(-x, -x, width + x, heigth + x);
		// Draw the Minutes-Arc into that rectangle
		canvas.drawArc(arcRect, -90, myLoadingAngle, true, paint);
	}

	private void init(int viewSizeInPixels, Bitmap icon) {

		paint = new Paint();

		loadingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		loadingPaint.setColor(Color.RED);
		loadingPaint.setAlpha(100);

		loadingLinePaint = new Paint();
		loadingLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		loadingLinePaint.setColor(Color.BLACK);
		loadingLinePaint.setStyle(Paint.Style.STROKE);
		loadingLinePaint.setStrokeWidth(3);

		setSize(viewSizeInPixels, icon);

		if (isInEditMode())
			loadDemoValues();
		myTimer = new UpdateTimer(myUpdateSpeed, null);
		setIcon(icon);
	}

	public int setSize(int recommendedWidth, Bitmap icon) {
		myWidth = recommendedWidth;
		myHalfWidth = myWidth / 2;
		if (icon != null) {
			myHeight = (int) ((float) (icon.getHeight())
					/ (float) (icon.getWidth()) * (float) (myWidth));
		} else {
			myHeight = myWidth;
		}
		myHalfHeight = myHeight / 2;

		if (myHeight <= 0 || myWidth <= 0) {
			Log.e(LOG_TAG, "height or width were 0!");
			Log.w(LOG_TAG, "   > icon=" + icon);
			Log.w(LOG_TAG, "   > icon.getHeight()=" + icon.getHeight());
			Log.w(LOG_TAG, "   > icon.getWidth()=" + icon.getWidth());
			Log.w(LOG_TAG, "   > recommendedWidth=" + recommendedWidth);
			showDebugInfos();
		}

		mutable = Bitmap.createBitmap(myWidth, myHeight,
				Bitmap.Config.ARGB_8888);
		stampCanvas = new Canvas(mutable);
		resizeIconToViewSize();
		return myHeight;
	}

	public void showDebugInfos() {
		Log.w(LOG_TAG, "   > myHeight=" + myHeight);
		Log.w(LOG_TAG, "   > myWidth=" + myWidth);
		Log.w(LOG_TAG, "   > myIcon=" + myIcon);
		Log.w(LOG_TAG, "   > myLoadingAngle=" + myLoadingAngle);
	}

	/**
	 * This method will only be called when the view is displayed in the eclipse
	 * xml layout editor
	 */
	private void loadDemoValues() {
		setLoadingAngle(160);
	}

	public void setLoadingAngle(float myLoadingAngle) {
		this.myLoadingAngle = myLoadingAngle;
		this.postInvalidate();
	}

	@Override
	public void onResizeEvent(int recommendedHeight, int recommendedWidth) {
		int width = Math.min(recommendedHeight, recommendedHeight);
		int height = setSize(width, myIcon);
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas onDrawCanvas) {

		// stampCanvas.drawARGB(0, 0, 0, 0);
		stampCanvas.drawBitmap(myIcon, 0, 0, paint);
		// Bitmap i2 = generateDebugImage2(getContext());
		// canvas.drawBitmap(i2, 0, 0, paint);
		drawLoadingCircle(stampCanvas, myWidth, myHeight, loadingPaint);
		drawLoadingCircle(stampCanvas, myWidth, myHeight, loadingLinePaint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		stampCanvas.drawBitmap(myIcon, 0, 0, paint);
		paint.setXfermode(null);

		onDrawCanvas.drawBitmap(mutable, 0, 0, paint);

		// if (debug != null) { // TODO remove this
		// paint.setColor(Color.RED);
		// canvas.drawText(debug, 0, myHalfSize, paint);
		// }
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// return onTouch(event.getX() - myHalfWidth, event.getY() - myHalfHeight);
	// }
	//
	// private boolean onTouch(float x, float y) {
	// double distFromCenter = Math.sqrt(x * x + y * y);
	// distFromCenter *= myTouchScaleFactor;
	// setLoadingAngle((float) (Math.random() * 359));
	// postInvalidate();
	// return true;
	// }

	@Override
	public boolean update(float timeDelta, Updateable parent) {
		if (myTimer.update(timeDelta, parent)) {
			if (parent instanceof GameAction) {
				GameAction a = (GameAction) parent;
				float prog = a
						.getStatValue(ActionThrowFireball.COOLDOWN_PROGRESS);
				float max = a.getStatValue(ActionThrowFireball.COOLDOWN_TIME);
				if (prog != Float.NaN && max != Float.NaN) {
					if (prog + timeDelta < max) {
						a.setStatValue(ActionThrowFireball.COOLDOWN_PROGRESS,
								prog + timeDelta);
						this.setLoadingAngle((prog + timeDelta) / max * 360);
					} else {
						a.setStatValue(ActionThrowFireball.COOLDOWN_PROGRESS,
								max);
						this.setLoadingAngle(360);
					}
				} else {
					Log.e(LOG_TAG,
							"The parent action has not the required values");
					this.setLoadingAngle(360);
				}

			}
		}
		/*
		 * TODO if view was removed from parent it can return false here!
		 */
		return true;
	}

	@Override
	public Updateable getMyParent() {
		Log.e(LOG_TAG, "Get parent called which is not "
				+ "implemented for this component!");
		return null;
	}

	@Override
	public void setMyParent(Updateable parent) {
		// can't have children so the parent does not have to be stored
		Log.e(LOG_TAG, "Set parent called which is not "
				+ "implemented for this component!");
	}

	@Override
	public boolean accept(Visitor visitor) {
		return false;
	}

}
