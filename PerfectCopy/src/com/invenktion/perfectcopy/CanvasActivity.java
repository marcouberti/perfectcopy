package com.invenktion.perfectcopy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.invenktion.perfectcopy.manager.ApplicationManager;
import com.invenktion.perfectcopy.manager.CheckImageManager;
import com.invenktion.perfectcopy.utils.FileUtils;
import com.invenktion.perfectcopy.utils.FontFactory;
import com.invenktion.perfectcopy.utils.ImageUtils;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.spen.lib.image.SPenImageFilter;
import com.samsung.spen.lib.image.SPenImageFilterConstants;
import com.samsung.spen.lib.input.SPenEvent;
import com.samsung.spen.lib.input.SPenEventLibrary;
import com.samsung.spen.lib.input.SPenLibrary;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SPenHoverListener;
import com.samsung.spensdk.applistener.SPenTouchListener;
import com.samsung.spensdk.applistener.SettingStrokeChangeListener;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CanvasActivity extends FragmentActivity{
    /** Called when the activity is first created. */
	
	public static final String TAG = "CanvasActivity";
	
	private String mCurrentPhotoPath;
	private int selectedFilter = SPenImageFilterConstants.FILTER_ORIGINAL;
	
	private int ALPHA_BG = 130;
	private int currentEXIFOrientation = ExifInterface.ORIENTATION_NORMAL;
	int left,top,right,bottom = -1;
	float mZoomValue = 1;
	float mZoomValueY = 1;
	float mTransXValue = 0;
	float mTransYValue = 0;
	
	//private Bitmap bgBitmap;
	private Bitmap fgBitmap;
	
	private ImageView foregroundImage;
	private ImageView originalImage;
	
	private Paint paintAlpha = new Paint();
	private Paint paint = new Paint();
	private Paint whitePaint = new Paint();

	private SCanvasView mSCanvas;
	//private SCanvasView mSCanvasForeground;
	private Button mPenBtn;
	private Button mEraserBtn;
	private Button eyeBtn;
	private Button mUndoBtn;
	private Button mRedoBtn;
	private Button saveBtn;
	private Button shareBtn;
	private TextView scoreText;
	private ImageView colorTool;
	
	private ImageView imageLoading;
	
	private boolean eyeOn = false;
	private SPenEventLibrary mSPenEventLibrary;
	
	@Override
	protected void onDestroy() {
		
		//CheckImageManager.clearAll();//CAUSAVA UN PROBLEMA SI SIGNAL 3 CHE KILLAVA L'APP
		if(fgBitmap != null) {
			fgBitmap.recycle();
			fgBitmap = null;
		}
		if(mSCanvas != null) {
			mSCanvas.closeSCanvasView();
		}
		
		/*
		if(mSCanvasForeground != null) {
			mSCanvasForeground.closeSCanvasView();
		}
		*/
		super.onDestroy();
	}
	
	//Verifica se l'applicazione è stata killata dal sistema
	//in questo caso chiudo l'attività e torno in home page
	private boolean checkApplicationKill() {
		if(ApplicationManager.APPLICATION_KILLED_BY_SYSTEM == null) {
			finish();
			return true;
		}
		return false;
	}
	
	private boolean openArtwork = false;
	private String userDrawingFilePath = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean finish = checkApplicationKill();
        if(finish) return;
        Bundle extras = getIntent().getExtras();
        if(extras !=null){
        	mCurrentPhotoPath = extras.getString("imagepath");
        	selectedFilter = extras.getInt("filter");
        	if(selectedFilter == 0) {//se non c'è, come nel caso del disegno a mano
        		selectedFilter = SPenImageFilterConstants.FILTER_ORIGINAL;
        	}
        	//Se siamo in apertura di un lavoro salvato, flaggo la variabile relativa
        	if(extras.containsKey("open")) {
        		if(extras.getBoolean("open")) {
        			openArtwork = true;
        		}
        		userDrawingFilePath = extras.getString("open_user_canvas");
        	}
        }else {
        	Toast.makeText(getApplicationContext(), "Image not found!", Toast.LENGTH_SHORT).show();
        	finish();
        }
        
        //Ora che ho il path immagine, leggo se è orizzontale o verticale e fisso l'orientamento
        currentEXIFOrientation = ImageUtils.getImageOrientation(mCurrentPhotoPath);
        
        mSPenEventLibrary = new SPenEventLibrary();
        
        System.out.println("#### currentEXIFOrientation " +currentEXIFOrientation);
        //Leggo solo le dimensioni dell'immagine
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;
        
        if(currentEXIFOrientation == ExifInterface.ORIENTATION_UNDEFINED) {
        	if(photoW < photoH) {
        		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        	}else {
        		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        	}
        }
        else if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_90 || currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_270){
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else if(currentEXIFOrientation == ExifInterface.ORIENTATION_NORMAL && photoW < photoH){
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        
        setContentView(R.layout.dashboard);
        
        paintAlpha.setAlpha(ALPHA_BG);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setAlpha(ALPHA_BG);
        
        imageLoading = (ImageView)findViewById(R.id.image_loading);
        
        foregroundImage = (ImageView)findViewById(R.id.image_view_foreground);
        
        originalImage = (ImageView)findViewById(R.id.image_view_original);
        originalImage.setVisibility(View.INVISIBLE);
        
        colorTool = (ImageView)findViewById(R.id.settingBtnColor);
   
        /*
        mSPenEventLibrary.setSPenTouchListener(originalImage, new SPenTouchListener() {
			
			@Override
			public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onTouchPen(View arg0, MotionEvent event) {
				//Log.d("### ontouchpen","### ontouchpen");
				//SOLO SE siamo in modalità EYE ON abilito il picker color
				
				return false;
			}
			
			@Override
			public boolean onTouchFinger(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onTouchButtonUp(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onTouchButtonDown(View arg0, MotionEvent event) {
					
			}
		});
		*/
        
        originalImage.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				int x = (int)event.getX();
				int y = (int)event.getY();
				
				//Disegno la vista corrente dell'immagine originale (compreso lo zoom etc.)
				//su una bitmap dalla quale fare il pickering del colore
				Bitmap visibleBitmap = Bitmap.createBitmap(fgBitmap.getWidth(), fgBitmap.getHeight(), Config.ARGB_8888);
				Canvas visibleBitmapCanvas = new Canvas(visibleBitmap);
				
				originalImage.draw(visibleBitmapCanvas);
				
                int color = visibleBitmap.getPixel(x, y);
                
                int nCurMode = mSCanvas.getCanvasMode();
    			if(nCurMode==SCanvasConstants.SCANVAS_MODE_INPUT_PEN) {
    				SettingStrokeInfo strokeInfo = mSCanvas.getSettingViewStrokeInfo();
    				if(strokeInfo != null) {
    					strokeInfo.setStrokeColor(color);
    					mSCanvas.setSettingViewStrokeInfo(strokeInfo);	
    					colorTool.setColorFilter(color);
    				}	
    			}
    			
    			//tolgo la modalità picker
    			/*
				eyeOn = false;
				originalImage.setVisibility(View.INVISIBLE);
				eyeBtn.setBackgroundResource(R.drawable.colorpicker_deselected);
				*/
                
                visibleBitmap.recycle();
                visibleBitmap = null;
                System.gc();
				
				eyeOn = false;
				originalImage.setVisibility(View.INVISIBLE);
				eyeBtn.setBackgroundResource(R.drawable.colorpicker_deselected);
				
				
				return true;//faccio proseguire la catena dell'evento
				
			}
		});
        
        scoreText = (TextView)findViewById(R.id.scoreText);
        scoreText.setTypeface(FontFactory.getFont1(getApplicationContext()));
        mPenBtn = (Button) findViewById(R.id.settingBtn);
		mPenBtn.setOnClickListener(mBtnClickListener);
		mPenBtn.setTextColor(Color.WHITE);
		mEraserBtn = (Button) findViewById(R.id.eraseBtn);
		mEraserBtn.setOnClickListener(mBtnClickListener);
		saveBtn = (Button) findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(saveBtnClickListener);
		shareBtn = (Button) findViewById(R.id.shareBtn);
		shareBtn.setOnClickListener(shareBtnClickListener);
		
		//Bottone per mostrare l'immagine originale
		eyeBtn = (Button) findViewById(R.id.eyeBtn);
		eyeBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					if(!CheckImageManager.checkOriginalImageIntegrity()){
						originalImage.setImageBitmap(CheckImageManager.getOriginalBitmap());
					}
					eyeOn = !eyeOn;
					if(eyeOn) {
						originalImage.setVisibility(View.VISIBLE);
						eyeBtn.setBackgroundResource(R.drawable.colorpicker);
						
						//foregroundImage.setImageBitmap(CheckImageManager.getOriginalBitmap());
						//mSCanvas.setBitmap(CheckImageManager.getOriginalBitmap(), false);
						//mSCanvasForeground.setBGImage(ImageUtils.getOriginalBitmap());
					}
					else {
						originalImage.setVisibility(View.INVISIBLE);
						eyeBtn.setBackgroundResource(R.drawable.colorpicker_deselected);
						
						//mSCanvasForeground.setBGImage(fgBitmap);
						//mSCanvas.setBitmap(currentBitmapSaved, false);
						//foregroundImage.setImageBitmap(fgBitmap);
					}
				}
				return true;
			}
		});
		
		mUndoBtn = (Button) findViewById(R.id.undoBtn);
		mUndoBtn.setOnClickListener(undoNredoBtnClickListener);
		mRedoBtn = (Button) findViewById(R.id.redoBtn);
		mRedoBtn.setOnClickListener(undoNredoBtnClickListener);
		
		
		mSCanvas = (SCanvasView) findViewById(R.id.canvas_view);
		//mSCanvasForeground = (SCanvasView) findViewById(R.id.canvas_view_foreground);
		
		mSCanvas.setScrollDrawing(false);
		//mSCanvasForeground.setScrollDrawing(false);
		mSCanvas.setSCanvasHoverPointerStyle(SCanvasConstants.SCANVAS_HOVERPOINTER_STYLE_NONE);
		mSCanvas.setTouchEventDispatchMode(true);
		
		mSCanvas.setSPenTouchListener(new SPenTouchListener() {
			
			@Override
			public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {
				return false;
			}
			
			@Override
			public boolean onTouchPen(View arg0, MotionEvent arg1) {
				return handleMove(arg1);
			}
			
			@Override
			public boolean onTouchFinger(View arg0, MotionEvent arg1) {
				return handleMove(arg1);
			}
			
			@Override
			public void onTouchButtonUp(View arg0, MotionEvent arg1) {
			}
			
			@Override
			public void onTouchButtonDown(View arg0, MotionEvent arg1) {
			}
			
			private boolean handleMove(MotionEvent event) {
				//Log.d("### MUB ON TUCH","### addtouch event");
				//SOLO SE siamo in modalità EYE ON abilito il picker color
				if(!eyeOn) {
					if(event.getPointerCount() == 1) {
						int x = (int)event.getX();
						int y = (int)event.getY();
						
						//Se siamo con zoom attivo, devo calcolare le coordinate relative
						//all'immagine e non allo schermo.
						if(mZoomValue != 1) {
							x = (int)((Math.abs(mTransXValue)/mZoomValue) + ((float)x/mZoomValue));
							y = (int)((Math.abs(mTransYValue)/mZoomValue) + ((float)y/mZoomValue));
						}
						
						if(left == -1) {
							left = right = x;
							top = bottom = y;
						}else {
							if(x < left) left = x;
							if(x > right) right = x;
							if(y < top) top = y;
							if(y > bottom) bottom = y;
						}
						
						if(event.getAction() == MotionEvent.ACTION_UP) {
							//Forzo il flush sulla s pen canvas, altrimenti l'ultimo tratto non viene considerato
							//se il dito è ancora giu
							//NB. NECESSARIO PER QUANDO IL DITO ESCE DALLO SCHERMO, NON FLUSHA
							//Log.d("### addtouch event","### addtouch event");
							mSCanvas.addTouchEvent(MotionEvent.ACTION_POINTER_UP,0, 0, 1,SCanvasView.METASTATE_PEN,0,0);

							startChecking();
						}
						
				    }
				}else {
					//se sto zoomand con 2 dita permetto
					if(event.getPointerCount() > 1) {
						return false;//abilito lo zoom
					}else {
						return true;//non disegno in modalita picker con un solo dito
					}
				}
				
				return false;//faccio proseguire la catena e l'SDK gestisce
			}
		});
		
		mSCanvas.setSPenHoverListener(new SPenHoverListener() {
			
			@Override
			public void onHoverButtonUp(View arg0, MotionEvent arg1) {
			}
			
			@Override
			public void onHoverButtonDown(View arg0, MotionEvent event) {
					Log.d("onHoverButtonDown","onHoverButtonDown");
					if(!CheckImageManager.checkOriginalImageIntegrity()){
						originalImage.setImageBitmap(CheckImageManager.getOriginalBitmap());
					}
					eyeOn = true;

					
					originalImage.setVisibility(View.VISIBLE);
					eyeBtn.setBackgroundResource(R.drawable.colorpicker);
					
					
			}
			
			@Override
			public boolean onHover(View arg0, MotionEvent event) {
				/*
				Log.d("ACTION_HOVER_EXIT","ACTION_HOVER_EXIT");
				if(event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
					eyeOn = false;
					originalImage.setVisibility(View.INVISIBLE);
					return true;
				}
				*/
				return false;
			}
		});
		
		mSCanvas.setOnCanvasMatrixChangeListener(mOnCanvasMatrixChangeListener);
		
		// Resource
		HashMap<String,Integer> settingResourceMap = new HashMap<String, Integer>();
		// Layout 
		settingResourceMap.put(SCanvasConstants.LAYOUT_PEN_SPINNER, R.layout.mspinner);
		// Locale(Multi-Language Support)	
		settingResourceMap.put(SCanvasConstants.LOCALE_PEN_SETTING_TITLE, R.string.pen_settings);
		settingResourceMap.put(SCanvasConstants.LOCALE_PEN_SETTING_PRESET_EMPTY_MESSAGE, R.string.pen_settings_preset_empty);
		settingResourceMap.put(SCanvasConstants.LOCALE_PEN_SETTING_PRESET_DELETE_TITLE, R.string.pen_settings_preset_delete_title);
		settingResourceMap.put(SCanvasConstants.LOCALE_PEN_SETTING_PRESET_DELETE_MESSAGE, R.string.pen_settings_preset_delete_msg);
		settingResourceMap.put(SCanvasConstants.LOCALE_ERASER_SETTING_TITLE, R.string.eraser_settings);
		settingResourceMap.put(SCanvasConstants.LOCALE_ERASER_SETTING_CLEARALL, R.string.delete_all);
		settingResourceMap.put(SCanvasConstants.LOCALE_ERASER_SETTING_CLEARALL_MESSAGE, R.string.delete_all_msg);
		settingResourceMap.put(SCanvasConstants.LOCALE_TEXT_SETTING_TITLE, R.string.text_settings);
		settingResourceMap.put(SCanvasConstants.LOCALE_TEXT_SETTING_TAB_FONT, R.string.text_settings_tab_font);
		settingResourceMap.put(SCanvasConstants.LOCALE_TEXT_SETTING_TAB_PARAGRAPH, R.string.text_settings_tab_paragraph);
		settingResourceMap.put(SCanvasConstants.LOCALE_TEXT_SETTING_TAB_PARAGRAPH_ALIGN, R.string.text_settings_tab_paragraph_align);		
		settingResourceMap.put(SCanvasConstants.LOCALE_FILLING_SETTING_TITLE, R.string.filling_settings);
		settingResourceMap.put(SCanvasConstants.LOCALE_TEXTBOX_HINT, R.string.textbox_hint);
		// Create Setting View
		//boolean bClearAllVisibileInEraserSetting = false;
		RelativeLayout settingViewContainer = (RelativeLayout) findViewById(R.id.canvas_container);		
		// Resource Map for Custom font path
		//HashMap<String,String> settingResourceMapString = new HashMap<String, String>();
		//mSCanvas.createSettingView(settingViewContainer, settingResourceMap, settingResourceMapString);    	  
		mSCanvas.createSettingView(settingViewContainer, settingResourceMap);    	

		mSCanvas.setHistoryUpdateListener(mHistoryUpdateListener);    	
		mSCanvas.setSettingStrokeChangeListener(mSettingStrokeChangeListener);
		mSCanvas.setSCanvasInitializeListener(new SCanvasInitializeListener() {
			@Override
			public void onInitialized() {
				/*
				//Imposto di default la matina con grandezza media
				PenSettingInfo info = mSCanvas.getPenSettingInfo();
				info.setPenType(PenSettingInfo.PEN_TYPE_PENCIL);
		        info.setPenWidth(PenSettingInfo.MAX_PEN_WIDTH / 2);
		        info.setEraserWidth(PenSettingInfo.MAX_ERASER_WIDTH / 2);
		        */
				SettingStrokeInfo strokeInfo = mSCanvas.getSettingViewStrokeInfo();
				if(strokeInfo != null) {
					strokeInfo.setStrokeStyle(SObjectStroke.SAMM_STROKE_STYLE_CRAYON);
					strokeInfo.setStrokeWidth(SObjectStroke.SAMM_DEFAULT_MAX_STROKESIZE / 2);
					//strokeInfo.setEraserWidth(PenSettingInfo.MAX_ERASER_WIDTH / 3);
					mSCanvas.setSettingViewStrokeInfo(strokeInfo);	
				}	
				//Lancio la creazione delle immagini sui canvas
				new Thread(){
					public void run() {
						Bitmap bitmap = getSampledBitmap(mCurrentPhotoPath);
						initializeCanvases(bitmap);
						mSCanvas.post(updateCanvasUI);
					};
				}.start();
			}
		});
		mUndoBtn.setEnabled(false);
		mRedoBtn.setEnabled(false);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		showExitDialog();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
  
    private OnClickListener undoNredoBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.equals(mUndoBtn)) {
				mSCanvas.undo();
				//Forzo full checking
				left = 0;
				top = 0;
				right = 5000;
				bottom = 5000;
				startChecking();
			} else if (v.equals(mRedoBtn)) {
				mSCanvas.redo();
				//Forzo full checking
				left = 0;
				top = 0;
				right = 5000;
				bottom = 5000;
				startChecking();
			}

			mUndoBtn.setEnabled(mSCanvas.isUndoable());
			mRedoBtn.setEnabled(mSCanvas.isRedoable());
			
			if(mSCanvas.isUndoable()) {
				mUndoBtn.setBackgroundResource(R.drawable.undo);
			}else {
				mUndoBtn.setBackgroundResource(R.drawable.undo_disabled);
			}
			
			if(mSCanvas.isRedoable()) {
				mRedoBtn.setBackgroundResource(R.drawable.rendo);
			}else {
				mRedoBtn.setBackgroundResource(R.drawable.rendo_disabled);
			}
		}
	};

    SCanvasView.OnCanvasMatrixChangeListener mOnCanvasMatrixChangeListener = new SCanvasView.OnCanvasMatrixChangeListener() {

		@Override
		public void onMatrixChanged(Matrix arg0) {
			float[] matrixValues = new float[9];
			arg0.getValues(matrixValues);
			//Applico la matrice di trasformazione anche all'immagine in foreground
			if(arg0 != null) {
				foregroundImage.setImageMatrix(arg0);
				originalImage.setImageMatrix(arg0);
			}
			mZoomValue = matrixValues[Matrix.MSCALE_X];
			mZoomValueY = matrixValues[Matrix.MSCALE_Y];
		 	mTransXValue = matrixValues[Matrix.MTRANS_X];
			mTransYValue = matrixValues[Matrix.MTRANS_Y];
			//Log.d("","### MATRIX "+mZoomValue +" "+mZoomValueY+" "+mTransXValue+" "+ mTransYValue);
		}

		@Override
		public void onMatrixChangeEnd() {
			// TODO Auto-generated method stub

		}
	};
    
	OnClickListener mBtnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			int nBtnID = v.getId();
			// If the mode is not changed, open the setting view. If the mode is same, close the setting view. 
			if(nBtnID == mPenBtn.getId()){				
				if(mSCanvas.getCanvasMode()==SCanvasConstants.SCANVAS_MODE_INPUT_PEN){
					mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_EXT);
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN);
				}
				else{
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);					
					updateModeState();
				}
			}
			else if(nBtnID == mEraserBtn.getId()){
				if(mSCanvas.getCanvasMode()==SCanvasConstants.SCANVAS_MODE_INPUT_ERASER){
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, false);
					updateModeState();
				}
			}
		}
	};

	private void updateModeState(){
		int nCurMode = mSCanvas.getCanvasMode();
		if(nCurMode==SCanvasConstants.SCANVAS_MODE_INPUT_PEN) {
			mPenBtn.setTextColor(Color.WHITE);
			mEraserBtn.setTextColor(Color.BLACK);
			mPenBtn.setBackgroundResource(R.drawable.tools);
			mEraserBtn.setBackgroundResource(R.drawable.rubber_deselected);
		}
		else if(nCurMode==SCanvasConstants.SCANVAS_MODE_INPUT_ERASER) {
			mPenBtn.setTextColor(Color.BLACK);
			mEraserBtn.setTextColor(Color.WHITE);
			mPenBtn.setBackgroundResource(R.drawable.tools_deselected);
			mEraserBtn.setBackgroundResource(R.drawable.rubber);
		}
	}

	//------------------------------------------------
	// SettingView Listener 
	//------------------------------------------------				
	SettingStrokeChangeListener mSettingStrokeChangeListener = new SettingStrokeChangeListener() {

		@Override
		public void onClearAll(boolean bClearAllCompleted) {
			if(bClearAllCompleted) {
				Log.i(TAG, "Clear All is completed");
				//Forzo full checking
				left = 0;
				top = 0;
				right = 5000;
				bottom = 5000;
				startChecking();
			}
		}

		@Override
		public void onEraserWidthChanged(int eraserWidth) {				
			Log.i(TAG, "Eraser width is changed : " + eraserWidth);				
		}

		@Override
		public void onStrokeColorChanged(int strokeColor) {				
			Log.i(TAG, "Pen Color is changed : " + strokeColor);	
			colorTool.setColorFilter(strokeColor);
		}

		@Override
		public void onStrokeStyleChanged(int strokeStyle) {

			if (strokeStyle == SObjectStroke.SAMM_STROKE_STYLE_PENCIL)
				Log.i(TAG, "Stroke Style = Pen");
			else if (strokeStyle == SObjectStroke.SAMM_STROKE_STYLE_BRUSH)
				Log.i(TAG, "Stroke Style = Brush");
			else if (strokeStyle == SObjectStroke.SAMM_STROKE_STYLE_CRAYON)
				Log.i(TAG, "Stroke Style = Pencil Crayon");    		
			else if (strokeStyle == SObjectStroke.SAMM_STROKE_STYLE_MARKER)
				Log.i(TAG, "Stroke Style = Marker");	

		}

		@Override
		public void onStrokeWidthChanged(int strokeWidth) {					
			Log.i(TAG, "Pen width is changed : " + strokeWidth);				
		}

		@Override
		public void onStrokeAlphaChanged(int strokeAlpha) {					
			Log.i(TAG, "Pen alpha is changed : " + strokeAlpha);				
		}

	};

	private HistoryUpdateListener mHistoryUpdateListener = new HistoryUpdateListener() {

		@Override
		public void onHistoryChanged(boolean bUndoable, boolean bRedoable) {

			mUndoBtn.setEnabled(bUndoable);
			mRedoBtn.setEnabled(bRedoable);

			if(bUndoable) {
				mUndoBtn.setBackgroundResource(R.drawable.undo);
			}else {
				mUndoBtn.setBackgroundResource(R.drawable.undo_disabled);
			}
			
			if(bRedoable) {
				mRedoBtn.setBackgroundResource(R.drawable.rendo);
			}else{
				mRedoBtn.setBackgroundResource(R.drawable.rendo_disabled);
			}
		}
	};

	protected void updateUI(double percentage) {
		if(scoreText != null) {
			scoreText.setText((int)percentage+" %");
		}
	}
	//Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();
    //Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
			public void run() {
			   //Controllo se ho avuto richieste di checking mentre ero occupato con un altro
			   busy = false;
			   if(pendingRequest) {
				   pendingRequest = false;
				   startChecking();
			   }
		       updateUI(percentage);
	        }
    };
    
    boolean busy = false;
    boolean pendingRequest = false;
    double percentage;
	protected void startChecking() {
		try {
			if(!busy) {
				busy = true;
		        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
				if(mSCanvas != null) {
				final Bitmap canvasBitmap = mSCanvas.getBitmap(true);
					if(canvasBitmap != null && !canvasBitmap.isRecycled()) {
						 if(!CheckImageManager.checkOriginalImageIntegrity()) {
							 originalImage.setImageBitmap(CheckImageManager.getOriginalBitmap());
						 }
						 Thread t = new Thread() {
				            public void run() {
				            		//while(playingTime) {//Finchè il tempo non scade
				            			synchronized (mSCanvas) {
										Log.i("### LAUNCH RESULT"," ###LAUNCH RESULT");
											try {
												//CALCOLO LA PERCENTUALE DI CORRETTEZZA RISPETTO AL
												//DISEGNO ORIGINALE
												if(left != -1) {
													int left2 = left;
													int top2 = top;
													int right2 = right;
													int bottom2 = bottom;
													left = top = right = bottom = -1;
													percentage = CheckImageManager.checkPartial(canvasBitmap,left2,top2,right2,bottom2);
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
											mHandler.post(mUpdateResults);
				            			}
				            		//}
				            		//Log.e("EXIT THREAD","EXIT THREAD ####################");
				            }
				        };
				        t.start();
					}
				}
			}else {
				pendingRequest = true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//######## ATTENZIONE!!!
	//NB. NON TOCCARE LA VIEW, QUESTO è UN THREAD SEPARATO!!!!
	//#######
	private void initializeCanvases(Bitmap image) {
		if(image == null)
			return;
		
		if(fgBitmap != null) {
			fgBitmap.recycle();
			fgBitmap = null;
		}
		
		int imageW = image.getWidth();
		int imageH = image.getHeight();
		double proportion;
		Log.d("###","###mub4 imageW="+imageW+" imageH="+imageH);
		
		if(selectedFilter != SPenImageFilterConstants.FILTER_ORIGINAL) {
			image = SPenImageFilter.filterImageCopy(image, selectedFilter, SPenImageFilterConstants.FILTER_LEVEL_MEDIUM);
		}
		
		//Disegno sempre uno sfondo bianco, per le immagini con trasparenza
		//le immagini false portrait le giro direttamente
		
		//Creo la bitmap già girata giusta, per evitare complicazioni dopo
		Bitmap bg = null;
		if(imageW > imageH && (currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_90 || currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_270)) {
			proportion = (double)imageH/(double)imageW;
			bg = Bitmap.createBitmap(imageH, imageW, Config.ARGB_8888);
			Canvas canvasBg = new Canvas(bg);
			Matrix matrix = new Matrix();
			int rotation = 0;
			if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
				rotation = 90;
				matrix.setTranslate(((imageW/2)-(imageH/2)), ((imageW/2)-(imageH/2)));
			}else{
				rotation = 270;
				matrix.setTranslate(-((imageW/2)-(imageH/2)), -((imageW/2)-(imageH/2)));
			}
			
			matrix.postRotate(rotation,(float)imageW/(float)2,(float)imageH/(float)2);
			canvasBg.drawRect(0, 0, imageH, imageW, whitePaint);
			canvasBg.drawBitmap(image, matrix, paint);
		}
		else if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			proportion = (double)imageW/(double)imageH;
			bg = Bitmap.createBitmap(imageW, imageH, Config.ARGB_8888);
			Canvas canvasBg = new Canvas(bg);
			Matrix matrix = new Matrix();
			matrix.setRotate(180,(float)imageW/(float)2,(float)imageH/(float)2);
			canvasBg.drawRect(0, 0, imageW, imageH, whitePaint);
			canvasBg.drawBitmap(image, matrix, paint);
		}
		else {
			proportion = (double)imageW/(double)imageH;
			bg = Bitmap.createBitmap(imageW, imageH, Config.ARGB_8888);
			Canvas canvasBg = new Canvas(bg);
			canvasBg.drawRect(0, 0, imageW, imageH, whitePaint);
			canvasBg.drawBitmap(image, 0, 0, paint);
		}
	
		image.recycle();
		image = null;
		
		//Display display = getWindowManager().getDefaultDisplay();
		int screenW = mSCanvas.getWidth();
		int screenH = mSCanvas.getHeight();

		int finalH = screenH;
		int finalW = (int)(finalH * proportion);
		
		if(finalW > screenW) {
			finalW = screenW;
			finalH = (int)(finalW / proportion);
		}
		
		Log.d("#######","####### finalW = "+finalW+" finalH = "+finalH);
		
		/*
		//Caso di foto orizzontali, ma in realtà verticali
		if(imageW > imageH && (currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_90 || currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_270)) {
			finalW = screenH;
			finalH = (int)(finalW / proportion);
		}
		*/
		
		RectF dst = new RectF(((screenW/2)-(finalW/2)),((screenH/2)-(finalH/2)),((screenW/2)+(finalW/2)),((screenH/2)+(finalH/2)));
		Log.d("###","mubrect = "+dst.toString());
		//Bitmap wrappedImage = Bitmap.createBitmap(screenW, screenH, Config.ARGB_8888);
		Bitmap originalBitmap = Bitmap.createBitmap(screenW, screenH, Config.ARGB_8888);
		//Canvas canvas = new Canvas(wrappedImage);
		Canvas canvasOriginal = new Canvas(originalBitmap);
		/*
		if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_90 && imageW > imageH){
			canvasOriginal.save();
			canvasOriginal.rotate(90,screenW/2,screenH/2);
			canvasOriginal.drawBitmap(bg, null, dst, paint);
			canvasOriginal.restore();
		}else if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_270  && imageW > imageH){
			canvasOriginal.save();
			canvasOriginal.rotate(-90,screenW/2,screenH/2);
			canvasOriginal.drawBitmap(bg, null, dst, paint);
			canvasOriginal.restore();
		}else 
		
		if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_180){
			canvasOriginal.save();
			canvasOriginal.rotate(180,screenW/2,screenH/2);
			canvasOriginal.drawBitmap(bg, null, dst, paint);
			canvasOriginal.restore();
		}else {
			canvasOriginal.drawBitmap(bg, null, dst, paint);
		}
		*/
		canvasOriginal.drawBitmap(bg, null, dst, paint);

		CheckImageManager.init(originalBitmap, screenW, screenH, finalW, finalH);
		left = right = top = bottom = -1;
		//mSCanvas.setBackgroundImage(wrappedImage);
		//wrappedImage.recycle();
		//bgBitmap = wrappedImage;
		
		//#### FOREGROUND
		//Log.d("","### BG W="+bg.getWidth()+" H="+bg.getHeight());
		Bitmap filteredBitmap = SPenImageFilter.filterImageCopy(bg, SPenImageFilterConstants.FILTER_PENSKETCH, SPenImageFilterConstants.FILTER_LEVEL_VERYLARGE);
		//Log.d("","### filteredBitmap W="+bg.getWidth()+" H="+bg.getHeight());
		//Bitmap filteredBitmap = SPenImageFilter.filterImageCopy(bg, SPenImageFilterConstants.FILTER_PASTELSKETCH, SPenImageFilterConstants.FILTER_LEVEL_VERYLARGE);
		if(filteredBitmap == null)
			return;
		Bitmap edge = makeEdgeFromGrayImage(filteredBitmap);
		
		filteredBitmap.recycle();
		filteredBitmap = null;
		System.gc();
		
		//Adatto allo schermo

		Bitmap wrappedImage2 = Bitmap.createBitmap(screenW, screenH, Config.ARGB_8888);
		Canvas canvas2 = new Canvas(wrappedImage2);
		/*
		if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_90 && imageW > imageH){
			canvas2.save();
			canvas2.rotate(90,screenW/2,screenH/2);
			canvas2.drawBitmap(bg, null, dst, paintAlpha);
			canvas2.drawBitmap(edge, null, dst, paint);
			canvas2.restore();
		}else if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_270  && imageW > imageH){
			canvas2.save();
			canvas2.rotate(-90,screenW/2,screenH/2);
			canvas2.drawBitmap(bg, null, dst, paintAlpha);
			canvas2.drawBitmap(edge, null, dst, paint);
			canvas2.restore();
		}else 
		
		if(currentEXIFOrientation == ExifInterface.ORIENTATION_ROTATE_180){
			canvas2.save();
			canvas2.rotate(180,screenW/2,screenH/2);
			canvas2.drawBitmap(bg, null, dst, paintAlpha);
			canvas2.drawBitmap(edge, null, dst, paint);
			canvas2.restore();
		}else {
			canvas2.drawBitmap(bg, null, dst, paintAlpha);
			canvas2.drawBitmap(edge, null, dst, paint);
		}
		*/
		canvas2.drawBitmap(bg, null, dst, paintAlpha);
		canvas2.drawBitmap(edge, null, dst, paint);
		
		//mSCanvasForeground.setBackgroundImage(wrappedImage2);
		bg.recycle();
		bg = null;
		edge.recycle();
		edge = null;
		System.gc();
		//wrappedImage.recycle();
		fgBitmap = wrappedImage2;
	}

	protected Bitmap makeEdgeFromColorImage(Bitmap filteredBitmap) {
		int W = filteredBitmap.getWidth();
		int H = filteredBitmap.getHeight();
		Bitmap edge = Bitmap.createBitmap(W, H, Config.ARGB_8888);
		for(int w=0; w<W; w++) {
			for(int h=0; h<H; h++) {
				int color = filteredBitmap.getPixel(w, h);
				int r_intensity = Color.red(color);
				int g_intensity = Color.green(color);
				int b_intensity = Color.blue(color);
				
				int mean = (int)((double)(r_intensity+g_intensity+b_intensity)/(double)3);
				
				edge.setPixel(w, h, Color.argb(255-mean, r_intensity, g_intensity, b_intensity));
			}
		}
		return edge;
	}
	
	protected Bitmap makeEdgeFromGrayImage(Bitmap filteredBitmap) {
		int W = filteredBitmap.getWidth();
		int H = filteredBitmap.getHeight();
		Log.d("","### ALLOCO "+W+" X "+H);
		Bitmap edge = Bitmap.createBitmap(W, H, Config.ARGB_8888);
		for(int w=0; w<W; w++) {
			for(int h=0; h<H; h++) {
				int color = filteredBitmap.getPixel(w, h);
				int intensity = Color.red(color);
				edge.setPixel(w, h, Color.argb(255-intensity, 0, 0, 0));
			}
		}
		return edge;
	}
	
    Runnable updateCanvasUI = new Runnable() {
		@Override
		public void run() {
			//mSCanvas.setBGImage(bgBitmap);
			
			if(foregroundImage != null && fgBitmap != null && !fgBitmap.isRecycled()) {
				foregroundImage.setImageBitmap(fgBitmap);
			}
			if(originalImage != null && CheckImageManager.getOriginalBitmap() != null  && !CheckImageManager.getOriginalBitmap().isRecycled()) {
				originalImage.setImageBitmap(CheckImageManager.getOriginalBitmap());
			}
			
			if(openArtwork) {
				Bitmap userBitmap = BitmapFactory.decodeFile(userDrawingFilePath);
				mSCanvas.setBitmap(userBitmap, true);
				//Forzo full checking
				left = 0;
				top = 0;
				right = 5000;
				bottom = 5000;
				startChecking();
				
				if(userBitmap != null && !userBitmap.isRecycled()) {
					userBitmap.recycle();
					userBitmap = null;
					System.gc();
				}
			}
			
			if(imageLoading != null) {
				imageLoading.setVisibility(View.GONE);
			}
			//bgBitmap.recycle();
			//fgBitmap.recycle();
		}
	};
 
	private Bitmap getSampledBitmap(String imagePath) {
		// Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(imagePath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;
	    
	    double proportion = (double)photoW/(double)photoH;
	
	    //Display display = getWindowManager().getDefaultDisplay();
		int screenW = mSCanvas.getWidth();
		int screenH = mSCanvas.getHeight();
		
		Log.d("CANVAS SIZE","## CANVAS W="+screenW+" CANVASH = "+screenH);
		
		int finalH = screenH;
		int finalW = (int)(finalH * proportion);
		
		if(finalW > screenW) {
			finalW = screenW;
			finalH = (int)(finalW / proportion);
		}
		
	    // Determine how much to scale down the image
		//ORIGINALE ERA Math.min()-> targetW,targetH
	    int scaleFactor = Math.min((int)((double)photoW/(double)finalW), (int)((double)photoH/(double)finalH));
	    //if(scaleFactor == 3) scaleFactor = 4;
	    Log.d("scale factor","scale factor = "+scaleFactor);
	  
	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = false;

	    Bitmap capturedImage = BitmapFactory.decodeFile(imagePath, bmOptions);
	    Log.d("captured scale factor","captured orig scale factor w="+capturedImage.getWidth()+" h="+capturedImage.getHeight());
	    Log.d("captured scale factor","image original scale factor w="+photoW+" h="+photoH);
	    return capturedImage;
	}
	
	
	//Gestore del salvataggio del lavoro corrente
	OnClickListener shareBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//Salvo la bitmap nel canvas sul file system e passo il path all'activity successiva
			Bitmap bitmap = mSCanvas.getCanvasBitmap(false);
			Canvas canvas = new Canvas(bitmap);
			Paint traspPaint = new Paint();
			traspPaint.setAlpha(ALPHA_BG);
			if(fgBitmap != null && !fgBitmap.isRecycled()) {
				canvas.drawBitmap(fgBitmap, 0, 0, traspPaint);
			}
			try {
				
				File file = FileUtils.createTmpImageFile(true);
				boolean saved = FileUtils.saveBitmapPNG(file.getAbsolutePath(), bitmap);
				if(saved) {
					//Uri screenshotUri = Uri.parse(f.getAbsolutePath());
					Uri screenshotUri = Uri.fromFile(file);
					Log.d("SCREENSHOT URI","SCREENSHOT URI="+screenshotUri);
		        	final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		        	//emailIntent.setType("text/html");
		        	emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        	emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
		        	//emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Perfect Copy");
		        	//emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<br/><br/>This image was created with Perfect Copy.<br/><br/>" +
		        			//"Visit the web site: www.invenktion.com"));

		        	emailIntent.setType("image/png");

		        	startActivity(Intent.createChooser(emailIntent, getApplicationContext().getString(R.string.sharewith)));
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					if(bitmap != null) {
						bitmap.recycle();
						bitmap = null;
						System.gc();
					}
				}catch (Exception e) {e.printStackTrace();}
			}
		}
	};
	
	//Gestore del salvataggio del lavoro corrente
	OnClickListener saveBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final ProgressDialog dialog = ProgressDialog.show(CanvasActivity.this, "", getApplicationContext().getString(R.string.saving), true);
			dialog.setCancelable(true);
			new Thread(){
				public void run() {
					Bitmap canvasBitmap = null;
					Bitmap originalBitmapWithoutBorder = CheckImageManager.getOriginalBitmapWithoutBorder();
					try {
						//1) CREO UNA CHIAVE UNIVOCA PER QUESTA OPERA
						//	 SE SONO IN MODIFICA DI UN LAVORO PRECEDENTE SOVRASCRIVO
						String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
						if(openArtwork) {
							File f = new File(userDrawingFilePath);
							timeStamp = f.getName().replace(FileUtils.PREFIX, "").replace("_canvas", "").replace(FileUtils.IMAGE_EXTENSION_PNG, "");
						}
						//2) SALVO L'IMMAGINE "ORIGINALE" (in realta' quella gia' ridimensionata, ma senza i bordi vuoti ai lati)
						//	 con un nome del tipo [timestamp]_original.png
						File file = FileUtils.createImageFileForSave(timeStamp+"_original");
						boolean saved = FileUtils.saveBitmapPNG(file.getAbsolutePath(), originalBitmapWithoutBorder);
						File file2 = FileUtils.createImageFileForSave(timeStamp+"_originalborder");
						boolean saved2 = FileUtils.saveBitmapPNG(file2.getAbsolutePath(), CheckImageManager.getOriginalBitmap());
						if(saved && saved2) {
							//3) SALVO L'IMMAGINE DERIVANTE DAL CANVAS DOVE L'UTENTE STA DISEGNANDO
							//   con un nome del tipo [timestamp]_canvas.png
							final File fileCanvas = FileUtils.createImageFileForSave(timeStamp+"_canvas");
							boolean savedCanvas = FileUtils.saveBitmapPNG(fileCanvas.getAbsolutePath(), mSCanvas.getCanvasBitmap(true));
							if(savedCanvas) {
								File fileForeground = FileUtils.createImageFileForSave(timeStamp+"_foreground");
								boolean savedForeground = FileUtils.saveBitmapPNG(fileForeground.getAbsolutePath(), fgBitmap);
								if(savedForeground) {
									runOnUiThread(new Runnable() {
										public void run() {
											//aggiorno le variabili locali, cosi in caso di sovrascrittura sovrascrive e non
											//crea un nuovo salvataggio
											openArtwork = true;
											userDrawingFilePath = fileCanvas.getAbsolutePath();
											Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.artworksaved), Toast.LENGTH_SHORT).show();
										}
									});
								}
							}else {
								runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.impossibletosave), Toast.LENGTH_SHORT).show();
									}
								});
							}
						}else {
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.impossibletosave), Toast.LENGTH_SHORT).show();
								}
							});
						}
						//N.B. i contorni non serve salvarli perchè li ricalcolo al momento
						//N.B. la percentuale non la salvo perchè ricalcolo al momento, settando bene anche il manager di checking
					}catch (Exception e) {
						e.printStackTrace();
					}finally {
						try {dialog.dismiss();}catch (Exception e) {e.printStackTrace();}
						if(canvasBitmap != null && !canvasBitmap.isRecycled()) {
							canvasBitmap.recycle();
							canvasBitmap = null;
						}
						if(originalBitmapWithoutBorder != null && !originalBitmapWithoutBorder.isRecycled()) {
							originalBitmapWithoutBorder.recycle();
							originalBitmapWithoutBorder = null;
						}
						System.gc();
					}
				};
			}.start();
		}
	};
	
	public static class MyDialogFragment extends DialogFragment {
	    
	    /**
	     * Create a new instance of MyDialogFragment
	     */
	    static MyDialogFragment newInstance() {
	        MyDialogFragment f = new MyDialogFragment();

	        // Supply num input as an argument.
	        Bundle args = new Bundle();
	        //args.putInt("num", num);
	        f.setArguments(args);

	        return f;
	    }

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	       
	        // Pick a style
	        int style = DialogFragment.STYLE_NORMAL;
	        setStyle(style, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
	    }

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.fragment_dialog, container, false);
	        //Font
	        TextView textView = (TextView)v.findViewById(R.id.exittext);
	        textView.setTypeface(FontFactory.getFont1(getActivity().getApplicationContext()));

	        // Watch for button clicks.
	        Button yesButton = (Button)v.findViewById(R.id.yes);
	        Button noButton = (Button)v.findViewById(R.id.no);
	        yesButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	((CanvasActivity)getActivity()).finish();
	            }
	        });
	        noButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	dismiss();
	            }
	        });

	        return v;
	    }
	}
	
	public void showExitDialog() {
       
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = MyDialogFragment.newInstance();
        newFragment.show(ft, "dialog");
    }
}