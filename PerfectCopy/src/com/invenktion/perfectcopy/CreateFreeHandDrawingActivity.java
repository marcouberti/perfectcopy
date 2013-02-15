package com.invenktion.perfectcopy;

import java.io.File;
import java.util.HashMap;

import com.invenktion.perfectcopy.manager.ApplicationManager;
import com.invenktion.perfectcopy.utils.FileUtils;
import com.invenktion.perfectcopy.utils.FontFactory;
import com.samsung.samm.common.SObjectImage;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.spen.lib.image.SPenImageFilterConstants;
import com.samsung.spen.lib.input.SPenEventLibrary;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SettingStrokeChangeListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CreateFreeHandDrawingActivity extends FragmentActivity{
	
	public static final String TAG = "CreateFreeHandDrawingActivity";
	
	private SCanvasView mSCanvas;
	//private SCanvasView mSCanvasForeground;
	private Button mPenBtn;
	private Button mEraserBtn;
	private Button mUndoBtn;
	private Button mRedoBtn;
	private Button clipartBtn;
	private Button okBtn;
	private ImageView colorTool;
	
	@Override
	protected void onDestroy() {
		if(mSCanvas != null) {
			mSCanvas.closeSCanvasView();
		}
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean finish = checkApplicationKill();
        if(finish) return;
		setContentView(R.layout.freehanddrawing);
		
		mPenBtn = (Button) findViewById(R.id.free_hand_settingBtn);
		mEraserBtn = (Button) findViewById(R.id.free_hand_eraseBtn);
		mUndoBtn = (Button) findViewById(R.id.free_hand_undoBtn);
		mRedoBtn = (Button) findViewById(R.id.free_hand_redoBtn);
		clipartBtn = (Button) findViewById(R.id.clipartBtn);
		okBtn = (Button) findViewById(R.id.free_hand_okBtn);
		
		mPenBtn.setOnClickListener(mBtnClickListener);
		mPenBtn.setTextColor(Color.WHITE);
		mEraserBtn.setOnClickListener(mBtnClickListener);
		
		mUndoBtn.setOnClickListener(undoNredoBtnClickListener);
		mRedoBtn.setOnClickListener(undoNredoBtnClickListener);
		
		okBtn.setOnClickListener(okBtnClickListener);
		clipartBtn.setOnClickListener(clipartBtnClickListener);
		
		colorTool = (ImageView)findViewById(R.id.settingBtnColor);
		
		mSCanvas = (SCanvasView) findViewById(R.id.free_hand_canvas_view);
		mSCanvas.setSCanvasHoverPointerStyle(SCanvasConstants.SCANVAS_HOVERPOINTER_STYLE_NONE);
		//mSCanvas.setscanvas
		mSCanvas.setScrollDrawing(false);
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
		HashMap<String,String> settingResourceMapString = new HashMap<String, String>();
		mSCanvas.createSettingView(settingViewContainer, settingResourceMap, settingResourceMapString);  	  

		mSCanvas.setHistoryUpdateListener(mHistoryUpdateListener);    	
		mSCanvas.setSettingStrokeChangeListener(mSettingStrokeChangeListener);
		mSCanvas.setSCanvasInitializeListener(new SCanvasInitializeListener() {
			@Override
			public void onInitialized() {
				//Imposto di default la matita con grandezza media
				/*
				PenSettingInfo info = mSCanvas.getPenSettingInfo();
				info.setPenType(PenSettingInfo.PEN_TYPE_SOLID);
		        info.setPenWidth(PenSettingInfo.MAX_PEN_WIDTH / 3);
		        info.setEraserWidth(PenSettingInfo.MAX_ERASER_WIDTH / 3);
		        */
				SettingStrokeInfo strokeInfo = mSCanvas.getSettingViewStrokeInfo();
				if(strokeInfo != null) {
					strokeInfo.setStrokeStyle(SObjectStroke.SAMM_STROKE_STYLE_SOLID);
					strokeInfo.setStrokeWidth(SObjectStroke.SAMM_DEFAULT_MAX_STROKESIZE / 3);
					//strokeInfo.setEraserWidth(PenSettingInfo.MAX_ERASER_WIDTH / 3);
					mSCanvas.setSettingViewStrokeInfo(strokeInfo);	
				}	
			}
		});
		mUndoBtn.setEnabled(false);
		mRedoBtn.setEnabled(false);
	}
	
	 private OnClickListener undoNredoBtnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.equals(mUndoBtn)) {
					mSCanvas.undo();
				} else if (v.equals(mRedoBtn)) {
					mSCanvas.redo();
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
		
	private OnClickListener clipartBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			showClipArtDialog();			
		}
	};	
		
	private OnClickListener okBtnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Salvo la bitmap nel canvas sul file system e passo il path all'activity successiva
				Bitmap bitmap = mSCanvas.getCanvasBitmap(false);
				try {
					
					File file = FileUtils.createTmpImageFile();
					boolean saved = FileUtils.saveBitmapPNG(file.getAbsolutePath(), bitmap);
					if(saved) {
						//Lancio l'activity dalla dashboard di disegno
						Intent myIntent = new Intent(CreateFreeHandDrawingActivity.this, CanvasActivity.class);
						myIntent.putExtra("imagepath", file.getAbsolutePath());
						myIntent.putExtra("filter", SPenImageFilterConstants.FILTER_ORIGINAL);
						CreateFreeHandDrawingActivity.this.startActivity(myIntent);
						finish();
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
		

	    SCanvasView.OnCanvasMatrixChangeListener mOnCanvasMatrixChangeListener = new SCanvasView.OnCanvasMatrixChangeListener() {

			@Override
			public void onMatrixChanged(Matrix arg0) {
				float[] matrixValues = new float[9];
				arg0.getValues(matrixValues);
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
				if(bClearAllCompleted) Log.i(TAG, "Clear All is completed");
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
		
		public static class ClipArtDialogFragment extends DialogFragment {
		    private Activity activity;
		    private float DENSITY;
		    private int pixelCorrispondenti;
		    /**
		     * Create a new instance of MyDialogFragment
		     */
		    static ClipArtDialogFragment newInstance(Activity context) {
		    	
		    	ClipArtDialogFragment f = new ClipArtDialogFragment(context);

		        // Supply num input as an argument.
		        Bundle args = new Bundle();
		        //args.putInt("num", num);
		        f.setArguments(args);

		        return f;
		    }

		    public ClipArtDialogFragment(Activity context) {
				this.activity = context;
				this.DENSITY = activity.getResources().getDisplayMetrics().density;
				
				Display display = activity.getWindowManager().getDefaultDisplay();
				
				int W = display.getWidth();
				if(display.getHeight() < W) {
					W = display.getHeight();
				}
				//this.pixelCorrispondenti = (int)(200*DENSITY+0.5f);
				this.pixelCorrispondenti = W/3;
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
		        View v = inflater.inflate(R.layout.fragment_clipart_dialog, container, false);
		        GridView grid = (GridView)v.findViewById(R.id.clipart_grid_view);
		        
		        grid.setColumnWidth(pixelCorrispondenti);
		        
		        final ImageAdapter adapter = new ImageAdapter(getActivity().getApplicationContext());
		        grid.setAdapter(adapter);
		        grid.setOnItemClickListener(new OnItemClickListener() {
		            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		                int resId = adapter.mThumbIds[position];
		                
		    			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
		    			RectF rectF = new RectF(250,250,pixelCorrispondenti+250,pixelCorrispondenti+250);
		    			// Create image object 
		    			SObjectImage sImageObject = new SObjectImage(); 
		    			sImageObject.setRect(rectF); 
		    			sImageObject.setImageBitmap(bitmap);
		    			//sImageObject.setImagePath(imagePath); 
		    			// Register image object 
		    			((CreateFreeHandDrawingActivity)activity).mSCanvas.insertSAMMImage(sImageObject, true);
		    			
		    			if(bitmap != null && !bitmap.isRecycled()) {
		    				bitmap.recycle();
		    				bitmap = null;
		    				System.gc();
		    			}
		    			dismiss();
		            }
		        });

		        return v;
		    }
		    
		    public class ImageAdapter extends BaseAdapter {
		        private Context mContext;
		        //references to our images
		        public Integer[] mThumbIds = {
		                R.drawable.cane, R.drawable.cappello,
		                R.drawable.capra, R.drawable.corna,
		                R.drawable.cuore, R.drawable.cuoricini,
		                R.drawable.drink, R.drawable.gatto,
		                R.drawable.gelato, R.drawable.lumaca,
		                R.drawable.occhiali, R.drawable.pistola,
		                R.drawable.pulcino, R.drawable.stella,
		                R.drawable.topo,R.drawable.crazyhat,
		                R.drawable.face,R.drawable.fastcar,
		                R.drawable.footprint,R.drawable.hand,
		                R.drawable.pig
		        };
		        public ImageAdapter(Context c) {
		            mContext = c;
		        }

		        public int getCount() {
		            return mThumbIds.length;
		        }

		        public Object getItem(int position) {
		            return null;
		        }

		        public long getItemId(int position) {
		            return 0;
		        }

		        // create a new ImageView for each item referenced by the Adapter
		        public View getView(int position, View convertView, ViewGroup parent) {
		            ImageView imageView;
		            if (convertView == null) {  // if it's not recycled, initialize some attributes
		                imageView = new ImageView(mContext);
		                
		                imageView.setLayoutParams(new GridView.LayoutParams(pixelCorrispondenti, pixelCorrispondenti));
		                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		                imageView.setPadding(8, 8, 8, 8);
		            } else {
		                imageView = (ImageView) convertView;
		            }

		            imageView.setImageResource(mThumbIds[position]);
		            return imageView;
		        }
		    }
		}
		
		public void showClipArtDialog() {
	       
	        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        Fragment prev = getSupportFragmentManager().findFragmentByTag("clipartdialog");
	        if (prev != null) {
	            ft.remove(prev);
	        }
	        ft.addToBackStack(null);

	        // Create and show the dialog.
	        DialogFragment newFragment = ClipArtDialogFragment.newInstance(this);
	        newFragment.show(ft, "clipartdialog");
	    }
}


