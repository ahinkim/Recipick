package org.techtown.recipe.mypage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.R;
import org.techtown.recipe.login.AutoLoginActivity;
import org.techtown.recipe.login.LoginActivity;
import org.techtown.recipe.main.MainActivity;
import org.techtown.recipe.main.MainAdapter;
import org.techtown.recipe.main.MainItem;
import org.techtown.recipe.main.OnMainItemClickListener;
import org.techtown.recipe.main.RecipeActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WishListActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private ImageButton exit_button;
    private RecyclerView recyclerView;
    private WishListAdapter adapter;
    static RequestQueue requestQueue;
    //private ArrayList<String> rIdArray = new ArrayList<String>();

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
        setContentView(R.layout.activity_favorite);
        exit_button = findViewById(R.id.exit_button);

        //Recycler view 세팅
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new WishListAdapter();

        //뒤로 가기
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //accessToken 인증 받기
        //url 받아오기
        MyApplication myApp = (MyApplication) getApplication();
        String url = myApp.getGlobalString();
        url += "/users/access";

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
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
                    RequestQueue requestQueue = Volley.newRequestQueue(WishListActivity.this);
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
        RequestQueue requestQueue = Volley.newRequestQueue(WishListActivity.this);
        requestQueue.add(TokenValidateRequest);

        //WishList 리스트 받아오기
        //url 받아오기
        String wishListUrl = myApp.getGlobalString();
        wishListUrl += "/recipe/wishlist";

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest WishListRequest = new StringRequest(Request.Method.GET, wishListUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //access token 유효하면 diaries jsonArray 받아오기
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray wishlistArray = jsonObject.optJSONArray("wish_list");
                    JSONObject element;

                    ArrayList<WishListItem> items = new ArrayList<WishListItem>();//없애야함
                    for (int i = 0; i < wishlistArray.length(); i++) {
                        element=(JSONObject) wishlistArray.opt(i);
                        JSONObject wishList=element.optJSONObject("rId");

                        items.add(new WishListItem(wishList.optString("rId")
                                , wishList.optString("recipe_title")
                                , wishList.optString("menu_img")));
                    }
                    adapter.setItems(items);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse.statusCode == 411) {
                    Log.d("statuscode4", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "사용자 정보에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent2);
                    finish();
                } else {
                    //서버 잘못되었을 때
                    Log.d("statuscode4", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        WishListRequest.setShouldCache(false);
        WishListRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(WishListRequest);

        recyclerView.setAdapter(adapter);

        //레시피 클릭했을 때
        adapter.setOnItemClickListener(new OnWishListItemClickListener() {
            @Override
            public void onItemClick(WishListAdapter.ViewHolder holder, View view, int position) {
                WishListItem item = adapter.getItem(position);

                String modify_RId = item.getRId();

                Intent intent = new Intent(WishListActivity.this, WishRecipeActivity.class);

                intent.putExtra("modify_RId", modify_RId);

                startActivity(intent);
                finish();
            }
        });
    }
}
