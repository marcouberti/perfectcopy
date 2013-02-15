package com.invenktion.perfectcopy;

import java.io.File;
import java.net.URL;

import com.invenktion.perfectcopy.constants.AppConstants;
import com.invenktion.perfectcopy.manager.ApplicationManager;
import com.invenktion.perfectcopy.utils.FileUtils;
import com.samsung.spen.lib.image.SPenImageFilterConstants;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @author mub
 * Home dell'applicazione. Da qui l'utente può scegliere da dove iniziare
 * a colorare: da una fotografia, da una immagine della galleria, da un disegno libero o da un 
 * lavoro precedentemente salvato e non terminato.
 */
public class HomeActivity extends Activity{
	
	ImageView shotBtn;
	ImageView galleryBtn;
	ImageView freeHandDrawingBtn;
	Button creditsBtn;
	Button tutorialBtn;
	Button openBtn;
	String mCurrentPhotoPath;
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		FileUtils.cleanTmpDir();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		ApplicationManager.APPLICATION_KILLED_BY_SYSTEM = "APPLICATION_RUNNING";
		
		shotBtn = (ImageView)findViewById(R.id.shotBtn);
		galleryBtn = (ImageView)findViewById(R.id.galleryBtn);
		freeHandDrawingBtn= (ImageView)findViewById(R.id.freeHandBtn);
		creditsBtn = (Button)findViewById(R.id.credits);
		tutorialBtn = (Button)findViewById(R.id.tutorial);
		openBtn = (Button)findViewById(R.id.open);
		
		creditsBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(HomeActivity.this, CreditsActivity.class);
				HomeActivity.this.startActivity(myIntent);
			}
		});
		
		tutorialBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(HomeActivity.this, TutorialActivity.class);
				HomeActivity.this.startActivity(myIntent);
			}
		});
		
		openBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(HomeActivity.this, SavedArtworkActivity.class);
				HomeActivity.this.startActivity(myIntent);
			}
		});
		
		shotBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    File f;
				try {
					f = FileUtils.createTmpImageFile();
					mCurrentPhotoPath = f.getAbsolutePath();
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				    startActivityForResult(takePictureIntent, AppConstants.IMAGE_CAPTURE);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Unable to create file!", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});
		galleryBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
				intent.setType("image/*");
				startActivityForResult(intent, AppConstants.GALLERY_IMAGE);
			}
		});
		freeHandDrawingBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(HomeActivity.this, CreateFreeHandDrawingActivity.class);
				HomeActivity.this.startActivity(myIntent);
			}
		});
		
		//Se in input abbiamo una immagine mandata con "SHARE VIA"
    	if(getIntent().getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
	    	Bundle b = getIntent().getExtras();
	    	String filePath = b.get(Intent.EXTRA_STREAM).toString();
	    	Log.d("FilePath = ","FilePath = "+filePath);
	    	if(filePath != null) {
	    		
	    		if(filePath.contains(FileUtils.PREFIX_SHARE)){
	    			finish();
	    			return;
	    		}
	    		
	    		if(filePath.contains("content://")) {
	    			final Uri selectedImage = Uri.parse(filePath);
	    			filePath = FileUtils.getRealPathFromURI(selectedImage,this);
	    		}else {
	    			filePath = Uri.decode(filePath).replace("file://","");
	    		}
	    		
	    		if(!new File(filePath).exists()) {
					Toast.makeText(getApplicationContext(), "Impossible to find image.", Toast.LENGTH_SHORT).show();
					return;
				}
	    		
				Intent myIntent = new Intent(HomeActivity.this, CanvasActivity.class);
				myIntent.putExtra("imagepath", filePath);
				myIntent.putExtra("filter", SPenImageFilterConstants.FILTER_ORIGINAL);//volendo si applica un filtro
				HomeActivity.this.startActivity(myIntent);
	    		
				finish();//chiudo l'attivita di Home dato che vengo da uno "Share with..."
			}
    	}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(mCurrentPhotoPath != null) {
			outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
		}
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState.getString("mCurrentPhotoPath") != null) {
			mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath");
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			String imagePath = null;
			if(requestCode == AppConstants.IMAGE_CAPTURE) {	
				imagePath = mCurrentPhotoPath;
			}else if(requestCode == AppConstants.GALLERY_IMAGE) {
				final Uri selectedImage = data.getData();
				Log.d("selectedimage","selectedimage ="+selectedImage);
				//In caso di problemi con la scelta di una immagini da terze applicazioni segnalo ed esco
				if(selectedImage == null) {
					Toast.makeText(getApplicationContext(), "Impossible to find image.", Toast.LENGTH_SHORT).show();
					return;
				}
				if(selectedImage.toString().startsWith("file://")) {
					imagePath = Uri.decode(selectedImage.toString()).replace("file://","");
				}else {
					imagePath = FileUtils.getRealPathFromURI(selectedImage,this);
				}
				Log.d("image path","image path= "+ imagePath);
				//Se il file non esiste segnalo ed esco
				if(!new File(imagePath).exists()) {
					Toast.makeText(getApplicationContext(), "Impossible to find image.", Toast.LENGTH_SHORT).show();
					return;
				}
			}
			Log.d("image path","image path= "+ imagePath);
			/*
			Intent myIntent = new Intent(HomeActivity.this, ImageFilterActivity.class);
			myIntent.putExtra("imagepath", imagePath);
			HomeActivity.this.startActivity(myIntent);
			*/
			if(imagePath != null) {
				Intent myIntent = new Intent(HomeActivity.this, CanvasActivity.class);
				myIntent.putExtra("imagepath", imagePath);
				myIntent.putExtra("filter", SPenImageFilterConstants.FILTER_ORIGINAL);//volendo si applica un filtro
				HomeActivity.this.startActivity(myIntent);
			}
		}
	}
}
