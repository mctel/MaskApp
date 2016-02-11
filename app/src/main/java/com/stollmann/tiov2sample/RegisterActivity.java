package com.stollmann.tiov2sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by marcoscandelaboti on 09/02/16.
 */
public class RegisterActivity extends Activity {

    private TextView title;
    private Button reg_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        title = (TextView)findViewById(R.id.app_title);
        reg_btn = (Button)findViewById(R.id.reg_btn);

        title.setTypeface(Typeface.createFromAsset(getAssets(), "run.ttf"));
        reg_btn.setTypeface(Typeface.createFromAsset(getAssets(), "GeosansLight.ttf"));

    }

    public void onClickRegister (View v){
        Intent i = new Intent(this, ManagerActivity.class );
        startActivity(i);
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }

}
