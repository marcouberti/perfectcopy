package com.invenktion.perfectcopy.utils;

import java.io.IOException;

import android.media.ExifInterface;
import android.util.Log;

public class ImageUtils {

	public static int getImageOrientation(String imagePath) {
    	//Leggo le info EXIF per capire se è girata correttamente vertical o orizzontale
		int currentEXIFOrientation = ExifInterface.ORIENTATION_NORMAL;
		try {
			ExifInterface exif = new ExifInterface(imagePath);
			currentEXIFOrientation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
			Log.d("","### ORIENTATION = "+currentEXIFOrientation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return currentEXIFOrientation;
	}
}
