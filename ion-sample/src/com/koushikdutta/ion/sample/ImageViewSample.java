package com.koushikdutta.ion.sample;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;

/**
 * Created by koush on 6/9/13.
 */
public class ImageViewSample extends Activity {
    public void loadCenterCrop() {
        Ion.with(this)
        .load("http://media.salon.com/2013/05/original.jpg")
        .withBitmap()
        .resize(512, 512)
        .centerCrop()
        .intoImageView(imageView);
    }

    public void loadCenterInside() {
        Ion.with(this)
        .load("http://media.salon.com/2013/05/original.jpg")
        .withBitmap()
        .resize(512, 512)
        .centerInside()
        .intoImageView(imageView);
    }

    public void loadGifCenterCrop() {
        Ion.with(this)
        .load("https://raw2.github.com/koush/ion/master/ion-sample/mark.gif")
        .withBitmap()
        .resize(512, 512)
        .centerCrop()
        .intoImageView(imageView);
    }

    public void loadGifCenterInside() {
        Ion.with(this)
        .load("https://raw2.github.com/koush/ion/master/ion-sample/mark.gif")
        .withBitmap()
        .resize(512, 512)
        .centerInside()
        .intoImageView(imageView);
    }

    public void loadGifResource() {
        Ion.with(this)
        .load("android.resource://" + getPackageName() + "/" + R.drawable.borg)
        .withBitmap()
        .resize(512, 512)
        .centerInside()
        .intoImageView(imageView);
    }

    public void loadExifRotated() {
        Ion.with(this)
        .load("https://raw.github.com/koush/ion/master/ion-test/testdata/exif.jpg")
        .intoImageView(imageView)
        ;
    }

    public void loadTwitterResource() {
        Ion.with(this)
        .load("android.resource://" + getPackageName() + "/drawable/twitter")
        .intoImageView(imageView)
        ;
    }
    
    public void loadText(int id) {
        String uri = "http://ftp.jaist.ac.jp/pub/sourceforge/a/ab/abucket/pre/ee79ac74bc5b0d19175d1d992bcf12aa.zip";
        String uri2 = "http://ftp.jaist.ac.jp/pub/sourceforge/a/ab/abucket/pre/05c3164d5c6bdceb7b249f0e1771ed00.zip";
        String uri3 = "http://ftp.jaist.ac.jp/pub/sourceforge/a/ab/abucket/pre/fb3c1efba20a0c7e406978370d7136fd.zip";
        String uri4 = "http://ftp.jaist.ac.jp/pub/sourceforge/a/ab/abucket/pre/45cc032eadf858058698731ea5ae6412.zip";
        String uri5 = "http://ftp.jaist.ac.jp/pub/sourceforge/a/ab/abucket/pre/d16ff0a5446b360ade089b1e23798c2c.zip";
        Ion.with(this)
        .load(uri)
        .addHeader("Connection", "close")
        .setLogging("tf", Log.VERBOSE)
        .withTypeface()
        .intoTextView(textView);
        
        Ion.with(this)
        .load(uri2)
        .addHeader("Connection", "close")
        .setLogging("tf", Log.VERBOSE)
        .withTypeface()
        .intoTextView(textView2);
        
        Ion.with(this)
        .load(uri3)
        .addHeader("Connection", "close")
        .setLogging("tf", Log.VERBOSE)
        .withTypeface()
        .intoTextView(textView3);
        
        Ion.with(this)
        .load(uri4)
        .addHeader("Connection", "close")
        .setLogging("tf", Log.VERBOSE)
        .withTypeface()
        .intoTextView(textView5);
        
        Ion.with(this)
        .load(uri5)
        .addHeader("Connection", "close")
        .setLogging("tf", Log.VERBOSE)
        .withTypeface()
        .intoTextView(textView4);
    }

    Spinner fitChoices;
    ImageView imageView;
    FontTextView textView;
    FontTextView textView2;
    FontTextView textView3;
    FontTextView textView4;
    FontTextView textView5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view);

        textView = (FontTextView) findViewById(R.id.image1);
        textView2 = (FontTextView) findViewById(R.id.image2);
        textView3 = (FontTextView) findViewById(R.id.image3);
        textView4 = (FontTextView) findViewById(R.id.image4);
        textView5 = (FontTextView) findViewById(R.id.image5);
        imageView = (ImageView)findViewById(R.id.image);
        fitChoices = (Spinner)findViewById(R.id.fit_choices);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.add("centerCrop");
        adapter.add("centerInside");
        adapter.add("gif centerCrop");
        adapter.add("gif centerInside");
        adapter.add("gif resource");
        adapter.add("exif rotated");
        adapter.add("twitter drawable resource");
        fitChoices.setAdapter(adapter);
        fitChoices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0)
//                    loadCenterCrop();
//                else if (position == 1)
//                    loadCenterInside();
//                else if (position == 2)
//                    loadGifCenterCrop();
//                else if (position == 3)
//                    loadGifCenterInside();
//                else if (position == 4)
//                    loadGifResource();
//                else if (position == 5)
//                    loadExifRotated();
//                else if (position == 6)
//                    loadTwitterResource();
                
                loadText(1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
