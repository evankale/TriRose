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

import java.io.File;
import java.io.FileOutputStream;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;

public class Util
{

	public static float hue2rgb(float p, float q, float t)
	{
		if (t < 0)
			t += 1;
		if (t > 1)
			t -= 1;
		if (t < 1f / 6f)
			return p + (q - p) * 6 * t;
		if (t < 1f / 2f)
			return q;
		if (t < 2f / 3f)
			return p + (q - p) * (2f / 3f - t) * 6;
		return p;
	}

	public static int HSLToRGB(float h, float s, float l)
	{
		float r, g, b;

		if (s == 0)
		{
			r = 1;
			g = 1;
			b = 1;
		}
		else
		{
			float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
			float p = 2 * l - q;
			r = hue2rgb(p, q, h + 1f / 3f);
			g = hue2rgb(p, q, h);
			b = hue2rgb(p, q, h - 1f / 3f);
		}

		int red = (int) (r * 255);
		int green = (int) (g * 255);
		int blue = (int) (b * 255);

		int rgb = 0xFF000000 | (red << 16) | (green << 8) | (blue);

		return rgb;
	}

	private static Resources res = SettingsActivity.settingsActivity.getResources();

	public static Bitmap loadScaledBitmap(int resID, int width, int height, boolean useRGB565)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = useRGB565 ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;
		Bitmap tempBmp = BitmapFactory.decodeResource(res, resID, options);

		//if no scaling needed, then return the bmp right away
		if (tempBmp.getWidth() == width && tempBmp.getHeight() == height)
			return tempBmp;

		//otherwise, create a scaled bmp, recycling the old one
		Bitmap retBmp = Bitmap.createScaledBitmap(tempBmp, width, height, true);
		tempBmp.recycle();

		return retBmp;
	}

	public static boolean intersects(Rect rect, int x, int y)
	{
		return (rect.left < x && x < rect.right && rect.top < y && y < rect.bottom);
	}

	public static boolean savePicture(Bitmap bmp)
	{
		String pngName = "/TriRose" + System.currentTimeMillis() + ".png";

		String filename[] = new String[4];
		filename[0] = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
				+ Environment.DIRECTORY_PICTURES + "/Screenshots" + pngName;
		filename[1] = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
				+ Environment.DIRECTORY_PICTURES + pngName;
		filename[2] = "/sdcard/ScreenCapture" + pngName;
		filename[3] = "/sdcard/pictures/screenshots" + pngName;

		for (int i = 0; i < filename.length; ++i)
		{
			try
			{
				FileOutputStream out = new FileOutputStream(filename[i]);
				bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.close();
				scanMediaFile(filename[i]);
				return true;
			}
			catch (Exception e)
			{
			}
		}

		return false;
	}

	public static void writePref(String key, boolean val)
	{
		SharedPreferences settings = SettingsActivity.settingsActivity.getSharedPreferences("Prefs", 0);
		Editor prefEditor = settings.edit();
		prefEditor.putBoolean(key, val);
		prefEditor.commit();
	}

	public static boolean readPref(String key, boolean defVal)
	{
		SharedPreferences settings = SettingsActivity.settingsActivity.getSharedPreferences("Prefs", 0);
		return settings.getBoolean(key, defVal);
	}

	public static void scanMediaFile(String filename)
	{
		new SingleMediaScanner(new File(filename));
	}

}

class SingleMediaScanner implements MediaScannerConnectionClient
{

	private final MediaScannerConnection mMs;
	private final File mFile;

	public SingleMediaScanner(File f)
	{
		mFile = f;
		mMs = new MediaScannerConnection(SettingsActivity.settingsActivity, this);
		mMs.connect();
	}

	@Override
	public void onMediaScannerConnected()
	{
		mMs.scanFile(mFile.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri)
	{
		mMs.disconnect();
	}

}
