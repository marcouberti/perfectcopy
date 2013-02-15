package com.invenktion.perfectcopy.manager;

import java.io.File;
import java.io.IOException;

import com.invenktion.perfectcopy.utils.FileUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

/**
 * @author mub
 * Manager che gestisce e calcola la percentuale corrente di correttezza del disegno.
 * Contiene al suo interno alcune variabili di stato.
 */
public class CheckImageManager {
	private static short[][] diff_map;
	private static int diff_sum;
	private static int IMAGE_WIDTH;
	private static int IMAGE_HEIGHT;
	private static Bitmap originalBitmap;
	private static int CANVAS_W,CANVAS_H;
	private static String originalBitmapFilePath;
	
	public static void clearAll() {
		if(CheckImageManager.originalBitmap != null) {
			CheckImageManager.originalBitmap.recycle();
			CheckImageManager.originalBitmap = null;
		}	
		System.gc();
	}
	
	public static Bitmap getOriginalBitmap() {
		return originalBitmap;
	}
	
	public static Bitmap getOriginalBitmapWithoutBorder() {
		int x = (CANVAS_W/2)-(IMAGE_WIDTH/2);
		int y = (CANVAS_H/2)-(IMAGE_HEIGHT/2);
		
		Log.d("####","x="+x+" y="+y+" imw="+IMAGE_WIDTH+" IMH="+IMAGE_HEIGHT);
		
		if(x < 0) x= 0;
		if(y < 0) y= 0;
		
		Bitmap bitmap = Bitmap.createBitmap(originalBitmap, x, y, IMAGE_WIDTH, IMAGE_HEIGHT);
		return bitmap;
	}

	public static void init(Bitmap bi1, int canvasW, int canvasH, int imageW, int imageH) {
		
		//Log.d("####","####MUB3 canvasW="+canvasW+" canvasH="+canvasH+" imageW="+imageW+" imageH="+imageH);
		//BUG FIX salvo la bitmap dell'immagine originale per recuperarla quando sparisce misteriosamente...
		File originalBitmapState = null;
		try {
			originalBitmapState = FileUtils.createFileForOriginalBitmapState();
			originalBitmapFilePath = originalBitmapState.getAbsolutePath();
			boolean saved = FileUtils.saveBitmapPNG(originalBitmapState.getAbsolutePath(), bi1);
			Log.d("BUG FIX","BUG FIX saved ="+saved + " path ="+originalBitmapFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		if(CheckImageManager.originalBitmap != null) {
			CheckImageManager.originalBitmap.recycle();
			CheckImageManager.originalBitmap = null;
			System.gc();
		}
		
		CheckImageManager.originalBitmap = bi1;
		CheckImageManager.IMAGE_WIDTH = imageW;
		CheckImageManager.IMAGE_HEIGHT = imageH;
		CheckImageManager.CANVAS_W = canvasW;
		CheckImageManager.CANVAS_H = canvasH;
		
		CheckImageManager.diff_map = null;
		System.gc();
		CheckImageManager.diff_map = new short[IMAGE_WIDTH][IMAGE_HEIGHT];
		CheckImageManager.diff_sum =  255 * IMAGE_HEIGHT *IMAGE_WIDTH;
		for(int w=0; w<IMAGE_WIDTH; w++) {
			for(int h=0; h<IMAGE_HEIGHT; h++) {
				CheckImageManager.diff_map[w][h] = (short)255;
			}
		}
	}
	
	//BUG che ogni tanto la bitmap originale sparisce e diventa tutta trasparente
	public static boolean checkOriginalImageIntegrity() {
		//Log.d("BUG ORIGINAL", "BUG ORIGINAL alpha="+Color.alpha(originalBitmap.getPixel(originalBitmap.getWidth()/2, originalBitmap.getHeight()/2)));
		//Non deve mai essere 0 l'alpha al centro dell'immagine. Ai bordi potrebbe perchè l'immagine non riempe tutto il canvas sempre
		if(Color.alpha(originalBitmap.getPixel(originalBitmap.getWidth()/2, originalBitmap.getHeight()/2)) == 0) {
			Log.d("BUG FIX ORIGINAL IMAGE SPARITA","BUG FIX ORIGINAL IMAGE SPARITA: la ricarico dal file salvato appositamente.");
			//La ricarico dal file temporaneo salvato appositamente
			CheckImageManager.originalBitmap = BitmapFactory.decodeFile(originalBitmapFilePath);
			Log.d("BUG ORIGINAL AFTER", "BUG ORIGINAL AFTER alpha="+Color.alpha(originalBitmap.getPixel(originalBitmap.getWidth()/2, originalBitmap.getHeight()/2)));
			return false;
		}
		return true;
	}
	
	//Check globale senza TUNING
	public static double checkPartial(Bitmap bi2, int left, int top, int right, int bottom) {
		try {
			//Log.i(""," PRE = LEFT TOP RIGHT BOTTOM = "+left+"-"+top+"-"+right+"-"+bottom);

			left -= 20;
			right += 20;
			top -= 20;
			bottom += 20;
	
			
			//NORMALIZZO LE COORDINATE ASSOLUTE IN COORDINATE RELATIVE ALL'IMMAGINE CENTRATA
			left = left -(CANVAS_W/2-IMAGE_WIDTH/2);
			right =right -(CANVAS_W/2-IMAGE_WIDTH/2);
			top = top-(CANVAS_H/2-IMAGE_HEIGHT/2);
			bottom =bottom-(CANVAS_H/2-IMAGE_HEIGHT/2);
			
			//Log.d("","BOUNDS = W="+IMAGE_WIDTH+ " H="+IMAGE_HEIGHT);
			
			//BOUNDS DELL'ARRAY[][]
			if(left < 0) left = 0;
			if(left >= IMAGE_WIDTH) left = IMAGE_WIDTH -1;
			if(right < 0) right = 0;
			if(right >= IMAGE_WIDTH) right = IMAGE_WIDTH -1;
			if(top < 0) top = 0;
			if(top >= IMAGE_HEIGHT) top = IMAGE_HEIGHT -1;
			if(bottom < 0) bottom = 0;
			if(bottom >= IMAGE_HEIGHT) bottom = IMAGE_HEIGHT -1;
			
			//Log.i(""," NORMALIZED = LEFT TOP RIGHT BOTTOM = "+left+"-"+top+"-"+right+"-"+bottom);
			int absCoordW, absCoordH;
			double r1,r2,g1,g2,b1,b2;
			double diff_r,diff_b,diff_g;
			double max1,max2;
			short pre,current;
			for(int w = left; w <right; w++) {
				for(int h = top; h <bottom; h++) {
					absCoordW = w + ((CANVAS_W/2-IMAGE_WIDTH/2));
					absCoordH = h +(CANVAS_H/2-IMAGE_HEIGHT/2);
					if(absCoordW < 0) absCoordW = 0;
					if(absCoordH < 0) absCoordH = 0;
					if(absCoordW >= originalBitmap.getWidth()) absCoordW = originalBitmap.getWidth() -1;
					if(absCoordH >= originalBitmap.getHeight()) absCoordH = originalBitmap.getHeight() -1;
					
					//Se non è stato ancora pitturato = massimo errore
					if(Color.alpha(originalBitmap.getPixel(absCoordW, absCoordH)) == 0) {//DOVE NON C'è IL MODELLO NON CONSIDERO = ZERO ERRORE
						pre = diff_map[w][h];
						current = (short)0;
						diff_map[w][h] = current;
						//Log.d("PRE VS CURRENT","PRE VS CURRENT = "+pre +" - "+current);
						diff_sum = diff_sum - (int)pre + (int)current;
					}
					else if(Color.alpha(bi2.getPixel(absCoordW, absCoordH)) == 0) {//DOVE L'UTENTE NON HA ANCORA PITTURATO = ERRORE MASSIMO
						pre = diff_map[w][h];
						current = (short)255;
						diff_map[w][h] = current;
						//Log.d("PRE VS CURRENT","PRE VS CURRENT = "+pre +" - "+current);
						diff_sum = diff_sum - (int)pre + (int)current;
					}else {		
						r1 = Color.red(originalBitmap.getPixel(absCoordW, absCoordH));
						r2 = Color.red(bi2.getPixel(absCoordW, absCoordH));
						
						g1 = Color.green(originalBitmap.getPixel(absCoordW, absCoordH));
						g2 = Color.green(bi2.getPixel(absCoordW, absCoordH));
						
						b1 = Color.blue(originalBitmap.getPixel(absCoordW, absCoordH));
						b2 = Color.blue(bi2.getPixel(absCoordW, absCoordH));
						
						diff_r = Math.abs(r1 - r2);
						diff_g = Math.abs(g1 - g2);
						diff_b = Math.abs(b1 - b2);
						
						//CONTA LA MASSIMA DIFFERENZA SUI 3 CANALI
						max1 = Math.max(diff_r, diff_g);
						max2 = Math.max(diff_b, max1);
						
						//SOGLIA DI TOLLERANZA
						if(max2 > 125) max2 = 255;
						
						pre = diff_map[w][h];
						current = (short)(int)max2;
						//Log.d("PRE VS CURRENT","PRE VS CURRENT = "+pre +" - "+current);
						diff_map[w][h] = current;
						diff_sum = diff_sum - (int)pre + (int)current;
					}
				}
			}
			
			double avarage = diff_sum / (IMAGE_WIDTH*IMAGE_HEIGHT);
			double percentage = 100 - avarage * 100 / 255;
			//System.out.println("PERCENTUALE CORRETTEZZA = "+percentage+"%");
			
			bi2.recycle();
			bi2= null;
			System.gc();
			return percentage;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	

	//Check globale senza TUNING
	public static double check(Bitmap bi2) {
		int W = originalBitmap.getWidth();
		int H = originalBitmap.getHeight();
		int W2 = bi2.getWidth();
		int H2 = bi2.getHeight();
		Log.d("SIZES = ","w="+W+" h="+H+"W2="+W2+"H2="+H2);
		
		double diff_sum = 0;
		for(int w = 0; w <W; w++) {
			for(int h = 0; h <H; h++) {
				//Se non è stato ancora pitturato = massimo errore
				if(Color.alpha(bi2.getPixel(w, h)) == 0) {
					diff_sum += 255;
				}else {		
					double r1 = Color.red(originalBitmap.getPixel(w, h));
					double r2 = Color.red(bi2.getPixel(w, h));
					
					double g1 = Color.green(originalBitmap.getPixel(w, h));
					double g2 = Color.green(bi2.getPixel(w, h));
					
					double b1 = Color.blue(originalBitmap.getPixel(w, h));
					double b2 = Color.blue(bi2.getPixel(w, h));
					
					double diff_r = Math.abs(r1 - r2);
					double diff_g = Math.abs(g1 - g2);
					double diff_b = Math.abs(b1 - b2);
					
					//CONTA LA MASSIMA DIFFERENZA SUI 3 CANALI
					double max1 = Math.max(diff_r, diff_g);
					double max2 = Math.max(diff_b, max1);
					
					
					//System.out.println("DIFF= "+ (max2));
					diff_sum += max2;
				}
			}
		}
		
		double avarage = diff_sum / (W*H);
		//System.out.println("AVARAGE = "+ avarage);
		double percentage = 100 - avarage * 100 / 255;
		System.out.println("PERCENTUALE CORRETTEZZA = "+percentage+"%");
		
		bi2.recycle();
		bi2= null;
		System.gc();
		return percentage;
	}

}
