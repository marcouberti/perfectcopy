package com.invenktion.perfectcopy.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.invenktion.perfectcopy.beans.Artwork;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * @author mub
 * La struttura del file system dell'applicazione e' la seguente:
 * 		EXTERNAL_STORAGE_ROOT
 * 			PerfectCopy
 * 				...
 */
public class FileUtils {
	
	private final static String ROOT_APP_DIRECTORY = "PerfectCopy";
	private final static String TMP_IMAGE_DIR = "tmp";
	private final static String SAVE_IMAGE_DIR = "saved";
	public final static String IMAGE_EXTENSION_JPEG = ".jpeg";
	public final static String IMAGE_EXTENSION_PNG = ".png";
	public final static String PREFIX = "PerfectCopy";
	public final static String PREFIX_SHARE = "PerfectCopyShare";
	
	public static File createTmpImageFile() throws IOException {
		return createTmpImageFile(false);
	}
	
	public static File createTmpImageFile(boolean share) throws IOException {
		if(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			// Create an image file name
	        String timeStamp = 
	            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	        
	        String prefix = PREFIX;
	        if(share) {
	        	prefix = PREFIX_SHARE;
	        }
	        String imageFileName = prefix + timeStamp + "_";
	        File image = File.createTempFile(
	            imageFileName, 
	            IMAGE_EXTENSION_JPEG, 
	            getTempDir()
	        );
	        //mCurrentPhotoPath = image.getAbsolutePath();
	        return image;
		}else {
			throw new IOException("MEDIA UNMOUNTED");
		}
    }
	
	public static File createImageFileForSave(String key) throws IOException {
		if(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			// Create an image file name
			 Log.d("### key"," key="+key);
	        String imageFileName = PREFIX + key + IMAGE_EXTENSION_PNG;
	        Log.d("### imageFileName"," imageFileName="+imageFileName);
	        File image = new File(getSaveDir(),imageFileName);
	        if(image!= null && !image.exists()) {
	        	image.createNewFile();
	        }
	        /*
	        File image = File.createTempFile(
	            imageFileName, 
	            IMAGE_EXTENSION_PNG, 
	            getSaveDir()
	        );
	        */
	        //mCurrentPhotoPath = image.getAbsolutePath();
	        return image;
		}else {
			throw new IOException("MEDIA UNMOUNTED");
		}
    }
	
	public static void cleanTmpDir() {
		File tmpDir = getTempDir();
		if(tmpDir != null && tmpDir.exists()) {
			for(File file:tmpDir.listFiles()) {
				if(file.exists() && file.getName().endsWith(IMAGE_EXTENSION_JPEG)) {
					Log.d("timestamp","timestamp "+ (System.currentTimeMillis() - file.lastModified()));
					if(System.currentTimeMillis() - file.lastModified() > 300000) {
						file.delete();
					}
				}
			}
		}
	}
	
	//la root e' sempre l'external storage
    private static File getTempDir() {
    	String applicationTmpDirName = ROOT_APP_DIRECTORY+"/"+TMP_IMAGE_DIR;
    	if(createDirIfNotExists(applicationTmpDirName)) {
    		File tempDir = new File(Environment.getExternalStorageDirectory(),applicationTmpDirName);
    		//Metto dentro il file .nomedia per evitare che la gallery mostri questi files
    		File nomediaFile = new File(tempDir, ".nomedia");
    		if(!nomediaFile.exists()) {
    			try {nomediaFile.createNewFile();} catch (IOException e) {e.printStackTrace();}
    		}
    		return tempDir;
    	}
    	return null;
	}
    
    //la root e' sempre l'external storage
    private static File getSaveDir() {
    	String applicationTmpDirName = ROOT_APP_DIRECTORY+"/"+SAVE_IMAGE_DIR;
    	if(createDirIfNotExists(applicationTmpDirName)) {
    		File tempDir = new File(Environment.getExternalStorageDirectory(),applicationTmpDirName);
    		//Metto dentro il file .nomedia per evitare che la gallery mostri questi files
    		File nomediaFile = new File(tempDir, ".nomedia");
    		if(!nomediaFile.exists()) {
    			try {nomediaFile.createNewFile();} catch (IOException e) {e.printStackTrace();}
    		}
    		return tempDir;
    	}
    	return null;
	}
    
    public static boolean isFileInTmpFolder(String path) {
    	File file = new File(path);
    	if(file.exists()) {
    		File parent = file.getParentFile();
    		if(parent.exists()) {
    			if(parent.getName().equals(TMP_IMAGE_DIR)) {
    				Log.d("CE","Ok posso cancellare il file.");
    				return true;
    			}
    		}else {
    			return false;
    		}
    	}else {
    		return false;
    	}
    	return false;
    }
    
    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("ColorEverything :: ", "Problem creating folder");
                ret = false;
            }
        }
        return ret;
    }

    
    public static boolean saveBitmapPNG(String strFileName, Bitmap bitmap){		
		if(strFileName==null || bitmap==null)
			return false;

		boolean bSuccess1 = false;	
		boolean bSuccess2;
		boolean bSuccess3;
		File saveFile = new File(strFileName);			

		if(saveFile.exists()) {
			if(!saveFile.delete())
				return false;
		}

		try {
			bSuccess1 = saveFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		OutputStream out = null;
		try {
			out = new FileOutputStream(saveFile);
			bSuccess2 = bitmap.compress(CompressFormat.PNG, 100, out);			
		} catch (Exception e) {
			e.printStackTrace();			
			bSuccess2 = false;
		}
		try {
			if(out!=null)
			{
				out.flush();
				out.close();
				bSuccess3 = true;
			}
			else
				bSuccess3 = false;

		} catch (IOException e) {
			e.printStackTrace();
			bSuccess3 = false;
		}finally
		{
			if(out != null)
			{
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}			
		}		

		return (bSuccess1 && bSuccess2 && bSuccess3);
	}	
    
    public static String getRealPathFromURI(Uri contentUri, Activity context) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

	public static ArrayList<Artwork> getArtworks() {
		ArrayList<Artwork> out = new ArrayList<Artwork>();
		
		File saveDir = getSaveDir();
		if(saveDir != null && saveDir.isDirectory()) {
			File[] files = saveDir.listFiles();
			for(File file: files) {
				if(file.getName().contains("_original")) {//ok ho trovato l'_original
					//ora cerco la _canvas
					File canvasFile = new File(file.getAbsolutePath().replace("_original", "_canvas"));
					File originalBorderFile = new File(file.getAbsolutePath().replace("_original", "_originalborder"));
					File foregroundFile = new File(file.getAbsolutePath().replace("_original", "_foreground"));
					if(canvasFile.exists() && originalBorderFile.exists() && foregroundFile.exists()){
						Artwork artwork = new Artwork();
							artwork.setCanvasImagePath(canvasFile.getAbsolutePath());
							artwork.setOriginalImagePath(file.getAbsolutePath());
							artwork.setOriginalWithBorderImagePath(originalBorderFile.getAbsolutePath());
							artwork.setForegroundImagePath(foregroundFile.getAbsolutePath());
						out.add(artwork);
					}
				}
			}
		}
		
		return out;
	}

	public static File createFileForOriginalBitmapState() throws IOException{
		if(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			// Create an image file name
	        String timeStamp = 
	            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	        
	        String prefix = PREFIX;

	        String imageFileName = prefix + timeStamp + "_";
	        File image = File.createTempFile(
	            imageFileName, 
	            IMAGE_EXTENSION_PNG, 
	            getTempDir()
	        );
	        //mCurrentPhotoPath = image.getAbsolutePath();
	        return image;
		}else {
			throw new IOException("MEDIA UNMOUNTED");
		}
	}
}
