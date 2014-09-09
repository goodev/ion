package com.koushikdutta.ion.font;

import android.widget.TextView;

import com.koushikdutta.async.future.SimpleFuture;

public interface TypefaceFutureBuilder {
    public SimpleFuture<TextView> intoTextView(TextView textView);
}
