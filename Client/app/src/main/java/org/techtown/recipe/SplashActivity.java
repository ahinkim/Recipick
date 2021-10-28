package org.techtown.recipe;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {
    private static final String Tag="SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);
        //setContentView(R.layout.activity_join);
        //setContentView(R.layout.activity_login);
        //setContentView(R.layout.activity_mypage);
        //setContentView(R.layout.favorite_item);
        //setContentView(R.layout.ranking_item);
        //setContentView(R.layout.main_item);
        setContentView(R.layout.order_item);


        /*
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(Tag,"Application Running");
                //자동 로그인
                //Intent intent = new Intent(getApplicationContext(), AutoLoginActivity.class);
                //startActivity(intent);
                finish();
            }
        },1000);
        */
    }
}
