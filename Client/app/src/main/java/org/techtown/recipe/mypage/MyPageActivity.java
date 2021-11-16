package org.techtown.recipe.mypage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.R;
import org.techtown.recipe.grade.GradeActivity;
import org.techtown.recipe.login.LoginActivity;
import org.techtown.recipe.main.MainActivity;
import org.techtown.recipe.main.OrderItem;
import org.techtown.recipe.main.RecipeActivity;
import org.techtown.recipe.ranking.RankingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPageActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    Button withdraw_button;
    Button favorite_button;
    TextView idTextView;
    Button review_button;

    private BottomNavigationView navigation;

    static RequestQueue requestQueue;

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
        idTextView=findViewById(R.id.idTextView);
        review_button=findViewById(R.id.review_button);

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

        //accessToken 인증 받기
        //url 받아오기
        MyApplication myApp = (MyApplication) getApplication();
        String url = myApp.getGlobalString();
        url += "/users/access";

        StringRequest TokenValidateRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse.statusCode == 419) {
                    //1. access token과 일치하는 사용자가 있지만 만료기간이 지난 경우
                    Log.d("statuscode1", "" + networkResponse.statusCode);

                    //url 받아오기
                    MyApplication myApp = (MyApplication) getApplication();
                    String url = myApp.getGlobalString();
                    url += "/users/reissuance";

                    StringRequest TokenReissueRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
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

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse.statusCode == 411 || networkResponse.statusCode == 412) {
                                //1. refresh token, access token과 일치하는 사용자가 없는 경우 411
                                //2. refresh token이 올바른 토큰 형태가 아닌 경우 412
                                Log.d("statuscode2", "" + networkResponse.statusCode);
                                Toast.makeText(getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show();

                            } else {
                                //서버 잘못되었을 때
                                Log.d("statuscode2", "" + networkResponse.statusCode);
                                Toast.makeText(getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent2);
                            finish();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            return headers;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
                    requestQueue.add(TokenReissueRequest);
                } else if (networkResponse.statusCode == 401 || networkResponse.statusCode == 402) {
                    //2. access token과 일치하는 사용자가 없는 경우 401
                    //3. 유효하지 않은 토큰 형태인 경우 402
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show();

                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent2);
                    finish();
                } else {
                    //서버 잘못되었을 때
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();

                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent2);
                    finish();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
        requestQueue.add(TokenValidateRequest);

        //레시피 순서 받아오기
        //url 받아오기
        String IdUrl = myApp.getGlobalString();
        IdUrl += "/users/access/id";

        StringRequest IdRequest = new StringRequest(Request.Method.GET, IdUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String UserId = jsonObject.optString("userId")+"님 반갑습니다.";
                    idTextView.setText(UserId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                //사용자와 맞는 accessToken이 없을 때(이 전에 클라이언트에서 검사했는데도 없는 경우니까 로그아웃 시켜야 됨)
                if (networkResponse.statusCode == 411) {
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "사용자 정보에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                    //login 화면으로 돌아가기
                    Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                    startActivity( intent );
                    finish();
                } else {
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        RequestQueue requestQueue2 = Volley.newRequestQueue(MyPageActivity.this);
        requestQueue2.add(IdRequest);

        favorite_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //찜목록
                Intent intent = new Intent( MyPageActivity.this, WishListActivity.class );
                startActivity( intent );
            }
        });

        review_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //인앱 리뷰 연결
                ReviewManager manager = ReviewManagerFactory.create(MyPageActivity.this);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // We can get the ReviewInfo object
                        ReviewInfo reviewInfo = task.getResult();
                    } else {
                        // There was some problem, log or handle the error code.
                    }
                });
            }
        });

        //회원탈퇴 버튼
        withdraw_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);

                builder.setTitle("회원탈퇴").setMessage("회원 탈퇴를 하시려면 확인을 눌러주세요.");

                builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //url 받아오기
                        MyApplication myApp = (MyApplication) getApplication();
                        String url=myApp.getGlobalString();
                        url += "/users/secession";

                        StringRequest SecessionRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //토큰 정보 삭제
                                preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.remove("accessToken").commit();
                                editor.remove("refreshToken").commit();

                                Toast.makeText(getApplicationContext(), "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                                //login 화면으로 돌아가기
                                Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                                startActivity( intent );
                                finish();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse networkResponse=error.networkResponse;
                                if (networkResponse.statusCode == 401) {
                                    //1. access token 유효기간 지났을 때 reissueance
                                    Log.d("statuscode", "" + networkResponse.statusCode);

                                    //url 받아오기
                                    MyApplication myApp = (MyApplication) getApplication();
                                    String url=myApp.getGlobalString();
                                    url += "/users/reissuance";

                                    StringRequest TokenReissueRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
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

                                                //url 받아오기
                                                MyApplication myApp = (MyApplication) getApplication();
                                                String url=myApp.getGlobalString();
                                                url += "/users/secession";

                                                StringRequest SecessionRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.remove("accessToken").commit();
                                                        editor.remove("refreshToken").commit();
                                                        editor.commit();

                                                        Toast.makeText(getApplicationContext(), "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                                                        //login 화면으로 돌아가기
                                                        Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                                                        startActivity( intent );
                                                        finish();
                                                    }
                                                }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        NetworkResponse networkResponse=error.networkResponse;
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
                                                }) {
                                                    @Override
                                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                                        return headers;
                                                    }
                                                };
                                                RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
                                                requestQueue.add(SecessionRequest);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            NetworkResponse networkResponse=error.networkResponse;
                                            //access token 재발급 실패하면 로그인으로 돌아감
                                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                                            startActivity(intent2);
                                            finish();
                                        }
                                    }) {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            return headers;
                                        }
                                    };
                                    RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
                                    requestQueue.add(TokenReissueRequest);

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
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return headers;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
                        requestQueue.add(SecessionRequest);
                    }
                });

                builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

    }
}