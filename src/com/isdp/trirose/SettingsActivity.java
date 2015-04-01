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
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsActivity extends Activity
{
	SeekBar intervalSlider;
	SeekBar refineSlider;
	SeekBar nSlider;
	SeekBar dSlider;
	SeekBar delaySlider;
	Button drawBtn;
	Button randBtn;
	TextView intervalNumber;
	TextView refineNumber;
	TextView nNumber;
	TextView dNumber;
	TextView delayNumber;
	CheckBox intCheckbox;

	public static int delay; //17 - 100
	public static float interval; // 1-360 degrees
	public static int refine; //1-50
	public static float n; //0.001 - 100.0
	public static float d; //0.001 - 100.0

	public static int screenMinDimension;
	public static int screenMaxDimension;
	public static int screenWidth;
	public static int screenHeight;

	public static boolean useInts = false;

	public static Activity settingsActivity;

	int delayMin = 17;
	int delayMax = 100;

	float intervalMin = 0.1f;
	float intervalMax = 359.9f;

	int refineMin = 1;
	int refineMax = 50;

	float nMin = 0.001f;
	float nMax = 100.0f;

	float dMin = 0.001f;
	float dMax = 100.0f;

	SettingsSeekBarListener seekBarListener;
	DrawButtonListener drawButtonListener;
	RandButtonListener randButtonListener;
	IntCheckListener intCheckListener;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);

		seekBarListener = new SettingsSeekBarListener();
		drawButtonListener = new DrawButtonListener();
		randButtonListener = new RandButtonListener();
		intCheckListener = new IntCheckListener();

		intervalSlider = (SeekBar) findViewById(R.id.intervalSlider);
		intervalSlider.setOnSeekBarChangeListener(seekBarListener);
		refineSlider = (SeekBar) findViewById(R.id.refineSlider);
		refineSlider.setOnSeekBarChangeListener(seekBarListener);
		nSlider = (SeekBar) findViewById(R.id.nSlider);
		nSlider.setOnSeekBarChangeListener(seekBarListener);
		dSlider = (SeekBar) findViewById(R.id.dSlider);
		dSlider.setOnSeekBarChangeListener(seekBarListener);
		delaySlider = (SeekBar) findViewById(R.id.delaySlider);
		delaySlider.setOnSeekBarChangeListener(seekBarListener);
		drawBtn = (Button) findViewById(R.id.drawBtn);
		drawBtn.setOnClickListener(drawButtonListener);
		randBtn = (Button) findViewById(R.id.randBtn);
		randBtn.setOnClickListener(randButtonListener);

		intervalNumber = (TextView) findViewById(R.id.intervalNumber);
		refineNumber = (TextView) findViewById(R.id.refineNumber);
		nNumber = (TextView) findViewById(R.id.nNumber);
		dNumber = (TextView) findViewById(R.id.dNumber);
		delayNumber = (TextView) findViewById(R.id.delayNumber);

		intCheckbox = (CheckBox) findViewById(R.id.intCheckbox);
		intCheckbox.setOnCheckedChangeListener(intCheckListener);

		settingsActivity = this;

		//determine the width and height here
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int w = displaymetrics.widthPixels;
		int h = displaymetrics.heightPixels;

		screenMinDimension = Math.min(w, h);
		screenMaxDimension = Math.max(w, h);
		screenWidth = w;
		screenHeight = h;

		updateParams();

	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	float getTransformedProgress(int sliderProgress)
	{
		return (1f / 10000) * sliderProgress * sliderProgress;
	}

	void updateParams()
	{
		delay = (int) ((delaySlider.getProgress() / 10000f) * (delayMax - delayMin) + delayMin);
		interval = (getTransformedProgress(intervalSlider.getProgress()) / 10000f) * (intervalMax - intervalMin)
				+ intervalMin;
		refine = (int) ((getTransformedProgress(refineSlider.getProgress()) / 10000f) * (refineMax - refineMin) + refineMin);
		n = (getTransformedProgress(nSlider.getProgress()) / 10000f) * (nMax - nMin) + nMin;
		d = (getTransformedProgress(dSlider.getProgress()) / 10000f) * (dMax - dMin) + dMin;

		if (useInts)
		{
			n = (int) n;
			d = (int) d;

			if (d == 0)
				d = 0.0000001f;
		}

		delayNumber.setText(delay + "");
		intervalNumber.setText(String.format("%.2f", interval) + "");
		refineNumber.setText(refine + "");
		nNumber.setText(useInts ? (int) n + "" : String.format("%.2f", n) + "");
		dNumber.setText(useInts ? (int) d + "" : String.format("%.2f", d) + "");

		Log.d("updateParams",
				delaySlider.getProgress() + " " + intervalSlider.getProgress() + " " + refineSlider.getProgress() + " "
						+ nSlider.getProgress() + " " + dSlider.getProgress());
		//Log.d("updateParams", delay + " " + interval + " " + refine  + " " + n + " " + d);

	}

	class SettingsSeekBarListener implements OnSeekBarChangeListener
	{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			updateParams();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			// TODO Auto-generated method stub

		}
	}

	class DrawButtonListener implements android.view.View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(SettingsActivity.settingsActivity, DrawActivity.class);
			startActivity(intent);
		}
	}

	class RandButtonListener implements android.view.View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			intervalSlider.setProgress((int) (Math.random() * 10000f));
			refineSlider.setProgress((int) (Math.random() * 10000f));
			nSlider.setProgress((int) (Math.random() * 10000f));
			dSlider.setProgress((int) (Math.random() * 10000f));
			delaySlider.setProgress((int) (Math.random() * 10000f));
		}
	}

	class IntCheckListener implements OnCheckedChangeListener
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			useInts = isChecked;
			updateParams();
		}

	}

}
