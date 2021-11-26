package org.techtown.recipe.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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
import org.techtown.recipe.login.LoginActivity;
import org.techtown.recipe.mypage.MyPageActivity;
import org.techtown.recipe.R;
import org.techtown.recipe.ranking.RankingActivity;
import org.techtown.recipe.search.SearchActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private MainAdapter adapter;
    private RecyclerView recyclerView;
    private BottomNavigationView navigation;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;

    static RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //단말에 저장된 token 받아오기
        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String accessToken = preferences.getString("accessToken", "");
        String refreshToken = preferences.getString("refreshToken", "");

        //헤더에 토큰 넣기
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accessToken", accessToken);
        headers.put("refreshToken", refreshToken);

        //버튼 세팅
        setContentView(R.layout.activity_main);
        searchView=findViewById(R.id.search_view);
        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);

        ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "",
                "추천 레시피들을 불러오고 있는 중입니다.", true);
        /*
        //최초 실행 여부를 판단
        boolean checkFirst= preferences.getBoolean("checkFirst",false);
        if(!checkFirst){//앱 최초 실행시
            Log.d("check","false");
            editor.putBoolean("checkFirst",true);
            editor.commit();

            View dialogView = getLayoutInflater().inflate(R.layout.first_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
            builder.setView(dialogView);

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            Button ok_btn = dialogView.findViewById(R.id.ok_button);
            ok_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

        }

         */

        int checkMainClick= preferences.getInt("checkMainClick",0);
        if(checkMainClick==0){//앱 최초 실행시
            checkMainClick++;
            editor.putInt("checkMainClick",checkMainClick);
            editor.commit();

            View dialogView = getLayoutInflater().inflate(R.layout.first_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
            builder.setView(dialogView);

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            Button ok_btn = dialogView.findViewById(R.id.ok_button);
            ok_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

        }
        else if(checkMainClick==2){
            checkMainClick++;
            editor.putInt("checkMainClick",checkMainClick);
            editor.commit();
            Log.d("checkmainclick",""+checkMainClick);
            ReviewManager manager = ReviewManagerFactory.create(MainActivity.this);
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
        else{
            checkMainClick++;
            editor.putInt("checkMainClick",checkMainClick);
            editor.commit();
        }

        //Recycler view 세팅
        recyclerView=findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MainAdapter();

        navigation = findViewById( R.id.navigation );

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ranking:
                        Intent intent1 = new Intent( MainActivity.this, RankingActivity.class );
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity( intent1 );
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.mypage:
                        Intent intent2 = new Intent( MainActivity.this, MyPageActivity.class );
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity( intent2 );
                        overridePendingTransition(0,0);
                        finish();
                        break;
                }
                return true;
            }
        });
        navigation.setSelectedItemId(R.id.home);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("search","검색");

                Intent intent = new Intent( MainActivity.this, SearchActivity.class );

                intent.putExtra("searchWord",query);

                startActivity( intent );

                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        //accessToken 인증 받기
        //url 받아오기
        MyApplication myApp = (MyApplication) getApplication();
        String url=myApp.getGlobalString();
        url += "/users/access";

        if(requestQueue==null){
            requestQueue=Volley.newRequestQueue(getApplicationContext());
        }
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
                    String url=myApp.getGlobalString();
                    url += "/users/reissuance";

                    if(requestQueue==null){
                        requestQueue=Volley.newRequestQueue(getApplicationContext());
                    }
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

                                headers.put("accessToken",newAccessToken);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse networkResponse=error.networkResponse;
                            if (networkResponse.statusCode == 411||networkResponse.statusCode == 412) {
                                //1. refresh token, access token과 일치하는 사용자가 없는 경우 411
                                //2. refresh token이 올바른 토큰 형태가 아닌 경우 412
                                Log.d("statuscode2", "" + networkResponse.statusCode);
                                Toast.makeText( getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT ).show();

                            }
                            else{
                                //서버 잘못되었을 때
                                Log.d("statuscode2", "" + networkResponse.statusCode);
                                Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
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
                    TokenReissueRequest.setShouldCache(false);
                    TokenReissueRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(TokenReissueRequest);
                }
                else if (networkResponse.statusCode == 401||networkResponse.statusCode == 402) {
                    //2. access token과 일치하는 사용자가 없는 경우 401
                    //3. 유효하지 않은 토큰 형태인 경우 402
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText( getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT ).show();

                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent2);
                    finish();
                }
                else{
                    //서버 잘못되었을 때
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        TokenValidateRequest.setShouldCache(false);
        TokenValidateRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(TokenValidateRequest);

        //main 리스트 받아오기
        //url 받아오기
        String Mainurl=myApp.getGlobalString();
        Mainurl += "/recipe/Main";

        if(requestQueue==null){
            requestQueue=Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest MainRequest = new StringRequest(Request.Method.GET, Mainurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mainResponse(response);
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse=error.networkResponse;
                if(networkResponse.statusCode==411){
                    //사용자와 맞는 accessToken이 없을 때(이 전에 클라이언트에서 검사했는데도 없는 경우니까 로그아웃 시켜야 됨)
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText( getApplicationContext(), "사용자 정보에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();

                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent2);
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
        MainRequest.setShouldCache(false);
        MainRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(MainRequest);

        recyclerView.setAdapter(adapter);

        //레시피 클릭했을 때
        adapter.setOnItemClickListener(new OnMainItemClickListener() {
            @Override
            public void onItemClick(MainAdapter.ViewHolder holder, View view, int position) {
                MainItem item=adapter.getItem(position);

                String modify_RId=item.getRId();
                String recipe_url=item.getRecipe_url();

                Intent intent = new Intent( MainActivity.this, RecipeActivity.class );

                intent.putExtra("modify_RId",modify_RId);
                intent.putExtra("recipe_url",recipe_url);

                startActivity( intent );
                //finish();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "",
                        "추천 레시피들을 불러오고 있는 중입니다.", true);

                /* swipe 시 진행할 동작 */
                //accessToken 인증 받기
                //url 받아오기
                MyApplication myApp = (MyApplication) getApplication();
                String url=myApp.getGlobalString();
                url += "/users/access";

                if(requestQueue==null){
                    requestQueue=Volley.newRequestQueue(getApplicationContext());
                }
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
                            String url=myApp.getGlobalString();
                            url += "/users/reissuance";

                            if(requestQueue==null){
                                requestQueue=Volley.newRequestQueue(getApplicationContext());
                            }
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

                                        headers.put("accessToken",newAccessToken);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    NetworkResponse networkResponse=error.networkResponse;
                                    if (networkResponse.statusCode == 411||networkResponse.statusCode == 412) {
                                        //1. refresh token, access token과 일치하는 사용자가 없는 경우 411
                                        //2. refresh token이 올바른 토큰 형태가 아닌 경우 412
                                        Log.d("statuscode2", "" + networkResponse.statusCode);
                                        Toast.makeText( getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT ).show();

                                    }
                                    else{
                                        //서버 잘못되었을 때
                                        Log.d("statuscode2", "" + networkResponse.statusCode);
                                        Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
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
                            TokenReissueRequest.setShouldCache(false);
                            TokenReissueRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            requestQueue.add(TokenReissueRequest);
                        }
                        else if (networkResponse.statusCode == 401||networkResponse.statusCode == 402) {
                            //2. access token과 일치하는 사용자가 없는 경우 401
                            //3. 유효하지 않은 토큰 형태인 경우 402
                            Log.d("statuscode1", "" + networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT ).show();

                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent2);
                            finish();
                        }
                        else{
                            //서버 잘못되었을 때
                            Log.d("statuscode1", "" + networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return headers;
                    }
                };
                TokenValidateRequest.setShouldCache(false);
                TokenValidateRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(TokenValidateRequest);

                //main 리스트 받아오기
                //url 받아오기
                String Mainurl=myApp.getGlobalString();
                Mainurl += "/recipe/Main";

                if(requestQueue==null){
                    requestQueue=Volley.newRequestQueue(getApplicationContext());
                }
                StringRequest MainRequest = new StringRequest(Request.Method.GET, Mainurl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mainResponse(response);
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse=error.networkResponse;
                        if(networkResponse.statusCode==411){
                            //사용자와 맞는 accessToken이 없을 때(이 전에 클라이언트에서 검사했는데도 없는 경우니까 로그아웃 시켜야 됨)
                            Log.d("statuscode", "" + networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "사용자 정보에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();

                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent2);
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
                MainRequest.setShouldCache(false);
                MainRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(MainRequest);

                recyclerView.setAdapter(adapter);

                /* 업데이트가 끝났음을 알림 */
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    public void mainResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray recipesArray=jsonObject.optJSONArray("recipes");
            JSONObject element;

            ArrayList<MainItem> items=new ArrayList<MainItem>();
            for(int i=0;i<recipesArray.length();i++){
                element=(JSONObject) recipesArray.opt(i);

                items.add(new MainItem(element.optString("rId")
                        ,element.optString("recipe_title")
                        ,element.optString("menu_img")
                        ,element.optString("recipe_url")
                        ));
            }
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }catch (JSONException e){

            e.printStackTrace();
        }
    }

}