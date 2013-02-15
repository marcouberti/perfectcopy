package com.invenktion.perfectcopy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.invenktion.perfectcopy.beans.Artwork;
import com.invenktion.perfectcopy.manager.ApplicationManager;
import com.invenktion.perfectcopy.utils.FileUtils;
import com.invenktion.perfectcopy.utils.FontFactory;
import com.samsung.spen.lib.image.SPenImageFilterConstants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SavedArtworkActivity extends Activity {
	private static final String TAG = "SavedArtworkActivity";
	float DENSITY = 1.0f;
	ArtworkAdapter myAdapter;
	ImageView noSavedImage;

	private boolean checkApplicationKilled() {
		if(ApplicationManager.APPLICATION_KILLED_BY_SYSTEM == null) {
			finish();
			return true;
		}
		return false;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean killed = checkApplicationKilled();
        if(killed) return;
        setContentView(R.layout.savedwork);
 
        this.DENSITY = getApplicationContext().getResources().getDisplayMetrics().density;
  
        final ListView lv = (ListView)findViewById(R.id.artwork_list);
        noSavedImage = (ImageView)findViewById(R.id.image_nosaved);
        noSavedImage.setVisibility(View.VISIBLE);
        //lv.sets
        /*
        lv.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			public void onScroll(AbsListView view,int firstVisible, int visibleCount, int totalCount) {
				    
			}
		});
		*/
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				/*
				Object  o = myAdapter.getItem(position);
				Log.d("ITEM ###","### ITEM :"+o);
				if(o != null) {
					Artwork artwork = (Artwork)o;
					Intent myIntent = new Intent(SavedArtworkActivity.this, CanvasActivity.class);
					myIntent.putExtra("open", true);
					myIntent.putExtra("imagepath", artwork.getOriginalImagePath());
					myIntent.putExtra("open_original_with_border", artwork.getOriginalWithBorderImagePath());
					myIntent.putExtra("open_user_canvas", artwork.getCanvasImagePath());
					myIntent.putExtra("filter", SPenImageFilterConstants.FILTER_ORIGINAL);//volendo si applica un filtro
					SavedArtworkActivity.this.startActivity(myIntent);
					//Chiudo questa activity
					finish();
				}
				*/
			}
		});

        ArrayList<Artwork> artworks = FileUtils.getArtworks();
        if(artworks!=null && artworks.size() > 0) {
        	if(noSavedImage != null) {
        		noSavedImage.setVisibility(View.INVISIBLE);
        	}
        }
        myAdapter = new ArtworkAdapter(this, R.layout.list_item, artworks);
        lv.setAdapter(myAdapter);
        lv.setTextFilterEnabled(true);

    }
    
    private class ArtworkAdapter extends ArrayAdapter<Artwork> {

        private ArrayList<Artwork> items = new ArrayList<Artwork>();

        public ArtworkAdapter(Context context, int textViewResourceId, ArrayList<Artwork> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        public Artwork getItem(int pos) {
        	if(pos >=0 && pos <items.size()) {
        		return items.get(pos);
        	}else return null;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	final ListView lv = (ListView)findViewById(R.id.artwork_list);
        	View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }
            final Artwork o = items.get(position);
            if (o != null) {
            	final ImageView originalImage = (ImageView)v.findViewById(R.id.original);
            	final ImageView canvasImage = (ImageView)v.findViewById(R.id.userdrawing);
            	//TextView openText = (TextView)v.findViewById(R.id.openitem);
            	Button deleteText = (Button)v.findViewById(R.id.removeitem);
            	//openText.setTypeface(FontFactory.getFont1(getApplicationContext()));
            	deleteText.setTypeface(FontFactory.getFont1(getApplicationContext()));
            	
            	originalImage.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent myIntent = new Intent(SavedArtworkActivity.this, CanvasActivity.class);
						myIntent.putExtra("open", true);
						myIntent.putExtra("imagepath", o.getOriginalImagePath());
						myIntent.putExtra("open_original_with_border", o.getOriginalWithBorderImagePath());
						myIntent.putExtra("open_user_canvas", o.getCanvasImagePath());
						myIntent.putExtra("filter", SPenImageFilterConstants.FILTER_ORIGINAL);//volendo si applica un filtro
						SavedArtworkActivity.this.startActivity(myIntent);
						//Chiudo questa activity
						finish();
					}
				});
				
            	
            	deleteText.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						try {
							//Provo a cancellare l'artwork
							File file = new File(o.getCanvasImagePath());
							File file2 = new File(o.getOriginalImagePath());
							File file3 = new File(o.getOriginalWithBorderImagePath());
							File file4 = new File(o.getForegroundImagePath());
							
							if(file.exists()){
								file.delete();
							}
							if(file2.exists()){
								file2.delete();
							}
							if(file3.exists()){
								file3.delete();
							}
							if(file4.exists()){
								file4.delete();
							}
							items.remove(o);
							//Aggiorno la lista
							myAdapter.notifyDataSetChanged();
							if(items!=null && items.size() == 0) {
					        	if(noSavedImage != null) {
					        		noSavedImage.setVisibility(View.VISIBLE);
					        	}
					        }
							Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.artworkdeleted), Toast.LENGTH_SHORT).show();
						}catch (Exception e) {
							Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.impossibletodelete), Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					}
				});
            	
            	final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(final Message message) {
						try {
							originalImage.setImageBitmap((Bitmap) ((Map)(message.obj)).get("foreground"));
							canvasImage.setImageBitmap((Bitmap) ((Map)(message.obj)).get("canvas"));
						}catch (Exception e) {
							e.printStackTrace();
						}
                    }
                };

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                    	BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    	bmOptions.inJustDecodeBounds = false;
                 	    bmOptions.inSampleSize = 4;
                 	    bmOptions.inPurgeable = false;
                    	
                    	Bitmap bitmap = BitmapFactory.decodeFile(o.getForegroundImagePath(),bmOptions);
                    	//originalImage.setImageBitmap(bitmap);
                    	
                    	Bitmap bitmapCanvas = BitmapFactory.decodeFile(o.getCanvasImagePath(),bmOptions);
                    	//canvasImage.setImageBitmap(bitmapCanvas);
                    	
                    	//Notifico la view
                    	Map<String,Bitmap> map = new HashMap<String,Bitmap>();
                    	map.put("foreground",bitmap);
                    	map.put("canvas",bitmapCanvas);
                        Message message = handler.obtainMessage(1, map);
                        handler.sendMessage(message);
                    }
                };
                thread.start();
            }
            return v;
        }
    }
}