package org.techtown.recipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class MyPageActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    Button withdraw_button;
    Button favorite_button;

    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //단말에 저장된 token 받아오기
        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
        String accessToken = preferences.getString("accessToken", "");
        String refreshToken = preferences.getString("refreshToken", "");

        //헤더에 토큰 넣기
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accessToken", accessToken);
        headers.put("refreshToken", refreshToken);

        //버튼 세팅
        setContentView(R.layout.activity_mypage);
        withdraw_button=findViewById(R.id.withdraw_button);
        favorite_button=findViewById(R.id.favorite_button);

        navigation = findViewById( R.id.navigation );

        navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent1 = new Intent( MyPageActivity.this, MainActivity.class );
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity( intent1 );
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.ranking:
                        Intent intent2 = new Intent( MyPageActivity.this, RankingActivity.class );
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity( intent2 );
                        overridePendingTransition(0,0);
                        finish();
                        break;
                }
                return true;
            }
        });
        navigation.setSelectedItemId(R.id.mypage);

        //회원탈퇴 버튼
        withdraw_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Response.Listener<String> responseListener= new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //login 화면으로 돌아가기
                        Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                        startActivity( intent );
                        finish();
                    }
                };
                Response.ErrorListener errorListener=new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse.statusCode == 401) {
                            //1. access token 유효기간 지났을 때 reissueance
                            Log.d("statuscode", "" + networkResponse.statusCode);

                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);

                                        //access token 재발급 성공
                                        String newAccessToken = jsonObject.getString("newAccessToken");
                                        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString("accessToken", newAccessToken);
                                        editor.commit();

                                        headers.put("accessToken", newAccessToken);

                                        //reissueance 성공했을 때
                                        Response.Listener<String> responseListener= new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                //login 화면으로 돌아가기
                                                Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                                                startActivity( intent );
                                                finish();
                                            }
                                        };
                                        Response.ErrorListener errorListener = new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                error.printStackTrace();
                                                NetworkResponse networkResponse = error.networkResponse;
                                                if (networkResponse.statusCode == 401) {
                                                    //또다시 access token 문제 생기면
                                                    Toast.makeText( getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT ).show();

                                                    //login 화면으로 돌아가기
                                                    Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                                                    startActivity( intent );
                                                    finish();
                                                }
                                                else{
                                                    //서버 잘못되었을 때
                                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                                    Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
                                                }
                                            }
                                        };
                                        //헤더에 토큰 넣기
                                        SecessionRequest secessionRequest = new SecessionRequest(headers,responseListener, errorListener);
                                        RequestQueue queue = Volley.newRequestQueue( MyPageActivity.this );
                                        queue.add( secessionRequest );

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            Response.ErrorListener errorListener = new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //access token 재발급 실패하면 로그인으로 돌아감
                                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent2);
                                    finish();
                                }
                            };
                            //서버로 Volley를 이용해서 요청
                            TokenReissueRequest tokenReissueRequest = new TokenReissueRequest(headers, responseListener, errorListener);
                            RequestQueue queue1 = Volley.newRequestQueue(MyPageActivity.this);
                            queue1.add(tokenReissueRequest);
                        }
                        else{
                            //서버 잘못되었을 때
                            Log.d("statuscode", "" + networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();

                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent2);
                            finish();
                        }
                    }
                };
                SecessionRequest secessionRequest = new SecessionRequest(headers,responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue( MyPageActivity.this );
                queue.add( secessionRequest );
            }
        });

    }
}