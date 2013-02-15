package com.invenktion.perfectcopy;

import com.invenktion.perfectcopy.manager.ApplicationManager;
import com.invenktion.perfectcopy.utils.ImageUtils;
import com.samsung.spen.lib.image.SPenImageFilter;
import com.samsung.spen.lib.image.SPenImageFilterConstants;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class TutorialActivity extends Activity{

	//Verifica se l'applicazione è stata killata dal sistema
	//in questo caso chiudo l'attività e torno in home page
	private boolean checkApplicationKill() {
		if(ApplicationManager.APPLICATION_KILLED_BY_SYSTEM == null) {
			finish();
			return true;
		}
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean finish = checkApplicationKill();
        if(finish) return;
		setContentView(R.layout.tutorial);
	}
}
