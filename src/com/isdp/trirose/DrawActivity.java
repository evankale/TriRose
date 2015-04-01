/*
 * Copyright (c) 2015 Evan Kale
 * Email: EvanKale91@gmail.com
 * Website: www.ISeeDeadPixel.com
 *
 * This file is part of TriRose.
 *
 * TriRose is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.isdp.trirose;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class DrawActivity extends Activity
{

	MyView view;
	static DrawTask drawTask;
	public static boolean isPaused = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().getDecorView().setBackgroundColor(Color.BLACK);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		isPaused = false;

		view = new MyView(this);
		setContentView(view);

		if (drawTask != null && drawTask.running)
		{
			drawTask.running = false;
		}

		if (soundPool == null)
		{
			soundPool = new SoundPool(2, AudioManager.STREAM_NOTIFICATION, 0);
			shutterSound = soundPool.load(this, R.raw.shutter, 0);
			clickSound = soundPool.load(this, R.raw.click, 0);
		}

		drawTask = new DrawTask();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		Thread t = new Thread(drawTask);
		t.start();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		drawTask.running = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (bmp == null)
			return false;

		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		int x = (int) event.getX();
		int y = (int) event.getY();

		//only care about DOWN events
		if (actionCode != MotionEvent.ACTION_DOWN)
		{
			return false;
		}

		if (Util.intersects(playPauseArea, x, y))
		{
			isPaused = !isPaused;
			soundPool.play(clickSound, 1f, 1f, 0, 0, 1);
		}
		else if (Util.intersects(screenshotArea, x, y))
		{
			soundPool.play(shutterSound, 1f, 1f, 0, 0, 1);
			boolean status = Util.savePicture(bmp);

			if (status)
			{
				Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(this, "Failed to write image", Toast.LENGTH_SHORT).show();
			}
		}

		return true;
	}

	class DrawTask implements Runnable
	{
		public boolean running = false;

		@Override
		public void run()
		{
			try
			{
				Thread.sleep(500);
			}
			catch (Exception e)
			{
			}

			running = true;

			while (running)
			{
				view.postInvalidate();
				try
				{
					Thread.sleep(SettingsActivity.delay);
				}
				catch (Exception e)
				{
				}
			}
		}
	}

	public static Bitmap bmp = null;
	public static Canvas canvas;
	public static Paint paint;
	public static Bitmap pauseBtnBmp;
	public static Bitmap playBtnBmp;
	public static Bitmap screenshotBtnBmp;
	public static int btnSize;
	public static int btnPadSize;

	public static Rect playPauseArea;
	public static Rect screenshotArea;

	public static SoundPool soundPool = null;
	public static int shutterSound;
	public static int clickSound;

	float TO_RAD = 0.01745329251994329576923690768489F;

	public class MyView extends View
	{

		public boolean startedDrawing = false;

		public MyView(Context c)
		{
			super(c);

			if (bmp == null)
			{
				paint = new Paint();
				paint.setAntiAlias(true);
				paint.setFilterBitmap(true);
				paint.setDither(true);
				paint.setStrokeWidth(2);

				bmp = Bitmap.createBitmap(SettingsActivity.screenMinDimension, SettingsActivity.screenMinDimension,
						Bitmap.Config.ARGB_8888);

				canvas = new Canvas();
				canvas.setBitmap(bmp);

				btnSize = bmp.getWidth() / 7;
				btnPadSize = btnSize / 3;

				pauseBtnBmp = Util.loadScaledBitmap(R.drawable.pause, btnSize, btnSize, false);
				playBtnBmp = Util.loadScaledBitmap(R.drawable.play, btnSize, btnSize, false);
				screenshotBtnBmp = Util.loadScaledBitmap(R.drawable.screenshot, btnSize, btnSize, false);

				playPauseArea = new Rect(btnPadSize, btnPadSize, btnPadSize + btnSize, btnPadSize + btnSize);
				screenshotArea = new Rect(SettingsActivity.screenWidth - btnPadSize - screenshotBtnBmp.getWidth(),
						btnPadSize, SettingsActivity.screenWidth - btnPadSize - screenshotBtnBmp.getWidth() + btnSize,
						btnPadSize + btnSize);

			}

			angle = 0;
			startedDrawing = false;

			canvas.drawColor(Color.BLACK);

		}

		int px1 = 0, px2 = 0, py1 = 0, py2 = 0;
		int ox = SettingsActivity.screenMinDimension / 2;
		int oy = SettingsActivity.screenMinDimension / 2;
		float angle = 0;
		float angleInterval = SettingsActivity.interval;
		int ctr = 0;
		float k = SettingsActivity.n / SettingsActivity.d;
		float hue = 0;

		@Override
		public void onDraw(Canvas viewCanvas)
		{

			if (!drawTask.running)
				return;

			if (!isPaused)
			{
				for (int i = 0; i < SettingsActivity.refine; ++i)
				{
					angle += (angleInterval * TO_RAD) / SettingsActivity.refine;

					float radius = (float) (Math.cos(k * angle));
					radius *= (Math.min(SettingsActivity.screenMinDimension, SettingsActivity.screenMaxDimension) / 2) * 0.85f;
					float x = (float) (radius * Math.cos(angle));
					float y = (float) (radius * Math.sin(angle));

					px1 = px2;
					py1 = py2;

					px2 = (int) (ox + x);
					py2 = (int) (oy - y);

					if (px1 == 0 && py1 == 0)
						continue;

					hue += 0.0005f;
					while (hue > 1)
						hue -= 1;
					int color = Util.HSLToRGB(hue, 1f, 0.75f);
					paint.setColor(color);

					canvas.drawLine(px1, py1, px2, py2, paint);

					++ctr;
				}
			}

			if (SettingsActivity.screenWidth > SettingsActivity.screenHeight)
				viewCanvas.drawBitmap(bmp, (SettingsActivity.screenWidth - bmp.getWidth()) / 2, 0, paint);
			else
				viewCanvas.drawBitmap(bmp, 0, (SettingsActivity.screenHeight - bmp.getHeight()) / 2, paint);

			if (!isPaused)
				viewCanvas.drawBitmap(pauseBtnBmp, playPauseArea.left, playPauseArea.top, paint);
			else
				viewCanvas.drawBitmap(playBtnBmp, playPauseArea.left, playPauseArea.top, paint);

			viewCanvas.drawBitmap(screenshotBtnBmp, screenshotArea.left, screenshotArea.top, paint);

		}

	}

}
