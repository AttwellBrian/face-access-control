package com.googlecode.javacv.facepreview;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class CompletedAuthorization extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.completed_authorization);

        TextView textView = (TextView)findViewById(R.id.beginButton);
        textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent myIntent = new Intent(CompletedAuthorization.this, FacePreview.class);
				//CompletedAuthorization.this.startActivity(myIntent);				
			}
		});
    }
}