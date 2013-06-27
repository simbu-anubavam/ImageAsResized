/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package org.selfkleptomaniac.ti.imageasresized;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;

import org.appcelerator.titanium.TiBlob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.content.res.AssetManager;

@Kroll.module(name="Imageasresized", id="org.selfkleptomaniac.ti.imageasresized")
public class ImageasresizedModule extends KrollModule
{

  // Standard Debugging variables
  private static final String LCAT = "ImageasresizedModule";
  // You can define constants with @Kroll.constant, for example:
  // @Kroll.constant public static final String EXTERNAL_NAME = value;
  
  public ImageasresizedModule()
  {
    super();
  }

  @Kroll.onAppCreate
  public static void onAppCreate(TiApplication app)
  {
    Log.d(LCAT, "inside onAppCreate");
    // put module init code that needs to run when the application is created
  }

  // Methods
  @Kroll.method
  public String example()
  {
    Log.d(LCAT, "example called");
    return "hello world";
  }
  
  // Properties
  @Kroll.getProperty
  public String getExampleProp()
  {
    Log.d(LCAT, "get example property");
    return "hello world";
  }
    
  @Kroll.setProperty
  public void setExampleProp(String value) {
    Log.d(LCAT, "set example property: " + value);
  }

    @Kroll.method
    public TiBlob cameraImageAsCropped(TiBlob image, int width, int height, int rotate, int x, int y){
      return cameraResizer(image, width, height, rotate, x, y);
    }

    @Kroll.method
    public TiBlob cameraImageAsResized(TiBlob image, int width, int height, int rotate){
      return cameraResizer(image, width, height, rotate, 0, 0);
    }

    private TiBlob cameraResizer(TiBlob image, int width, int height, int rotate, int x, int y){
      byte[] image_data = image.getBytes();

      try{
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(image_data, 0, image_data.length, opts);

        opts.inSampleSize = calcSampleSize(opts, width, height);

        opts.inJustDecodeBounds = false;

        Bitmap image_base = BitmapFactory.decodeByteArray(image_data, 0, image_data.length, opts);
        Matrix matrix = getScaleMatrix(opts.outWidth, opts.outHeight, image_base.getWidth(), image_base.getHeight());
        if(rotate > 0){
          matrix.postRotate(rotate);
        }

        return returnBlob(opts, image_base, matrix, width, height, x, y);
      }catch(NullPointerException e){
        return null;
      }
    }

    @Kroll.method
    public TiBlob imageAsCropped(int width, int height, String path, int rotate, int x, int y){
      return resizer(width, height, path, rotate, x, y);
    }

    @Kroll.method
    public TiBlob imageAsResized(int width, int height, String path, int rotate){
      return resizer(width, height, path, rotate, 0, 0);
    }

    public TiBlob resizer(int width, int height, String path, int rotate, int x, int y){
      Activity activity = getActivity();
      AssetManager as = activity.getResources().getAssets();

      String fpath = null;
      String save_path = null;

      if(path.startsWith("file://") || path.startsWith("content://")){
        fpath = path;
      }else{
        if(path.startsWith("app://")){
          path = path.replaceFirst("app://", "Resources/");
        }else if(path.startsWith("Resources") == false){
          if(path.startsWith("/")){
            path = "Resources" + path;
          }else{
            path = "Resources/" + path;
          }
        }
        fpath =  path;
      }
      File save_path_base = new File(path);
      save_path = "camera/" + save_path_base.getName();

      String toFile = "/data/data/"+ TiApplication.getInstance().getPackageName() +"/app_appdata/" + save_path;

      try{
        // File must be copied to /data/data. you can't handle files under Resouces dir.
        // Who knows? Not me.
        InputStream is = as.open(fpath);
        copyFile(is, toFile);

        // Load image file data, not image file it self.
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(toFile, opts);
    
        try{
          // Load image
          opts.inJustDecodeBounds = false;
          opts.inSampleSize = calcSampleSize(opts, width, height);
          Bitmap image_base = BitmapFactory.decodeFile(toFile, opts);

          // Calc scale.
          int w = image_base.getWidth();
          int h = image_base.getHeight();

          Matrix matrix = getScaleMatrix(opts.outWidth, opts.outHeight, w, h);

          if(rotate > 0){
            matrix.postRotate(rotate);
          }

          // Voila!
          return returnBlob(opts, image_base, matrix, width, height, x, y);
        }catch(NullPointerException e){
          Log.d(LCAT, "Bitmap IOException:" + e);
          return null;
        }
      }catch(IOException e){
        Log.d(LCAT, "Bitmap IOException:" + e);
        return null;
      }
    }
    
    // Copy from inputstream to file
    private static void copyFile(InputStream input, String dstFilePath) throws IOException{
      File dstFile = new File(dstFilePath);
     
      String parent_dir = dstFile.getParent();
      File dir = new File(parent_dir);
      dir.mkdirs();
     
      OutputStream output = null;
      output = new FileOutputStream(dstFile);
     
      int DEFAULT_BUFFER_SIZE = 1024 * 4;
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      int n = 0;
      while (-1 != (n = input.read(buffer))) {
        output.write(buffer, 0, n);
      }
      input.close();
      output.close();
    }

    private Matrix getScaleMatrix(int orig_w, int orig_h, int w, int h){
      int scale = Math.min((int)orig_w/w, (int)orig_h/h);
      Matrix matrix = new Matrix();
      matrix.postScale(scale, scale);
      return matrix;
    }

    private TiBlob returnBlob(BitmapFactory.Options opts, Bitmap image_base, Matrix matrix, int w, int h, int x, int y)
      throws NullPointerException{
//      Log.d(LCAT, "returnBlob w:" + w);
//      Log.d(LCAT, "returnBlob h:" + h);
//      Log.d(LCAT, "returnBlob x:" + x);
//      Log.d(LCAT, "returnBlob y:" + y);
      Bitmap scaled_image = Bitmap.createBitmap(image_base, x, y, w, h, matrix, true);
      TiBlob blob = TiBlob.blobFromImage(scaled_image);
      image_base.recycle();
      image_base = null;
      scaled_image.recycle();
      scaled_image = null;
      return blob;
    }

    private int calcSampleSize(BitmapFactory.Options opts, int width, int height){
      int scaleW = Math.max(1, opts.outWidth / width);
      int scaleH = Math.max(1, opts.outHeight / height);
      int sampleSize = (int)Math.min(scaleW, scaleH);
      return sampleSize;
    }
}

