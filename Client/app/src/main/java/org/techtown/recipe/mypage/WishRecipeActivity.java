package org.techtown.recipe.mypage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.R;
import org.techtown.recipe.login.AutoLoginActivity;
import org.techtown.recipe.login.LoginActivity;
import org.techtown.recipe.login.RegisterActivity;
import org.techtown.recipe.main.MainActivity;
import org.techtown.recipe.main.MainAdapter;
import org.techtown.recipe.main.MainItem;
import org.techtown.recipe.main.OrderAdapter;
import org.techtown.recipe.main.OrderItem;
import org.techtown.recipe.ranking.RankingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WishRecipeActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private RecyclerView orderRecyclerView;
    private OrderAdapter adapter;

    private ImageView RecipeImageView;
    private TextView recipeTitleTextView;
    private TextView recipePeopleTextView;
    private TextView recipeMinuteTextView;
    private TextView recipeDifficultyTextView;
    private TextView recipeSourceTextView;

    private ImageButton exit_button;
    private ImageButton delete_button;

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
        setContentView(R.layout.activity_wish_recipe);

        RecipeImageView=findViewById(R.id.RecipeImageView);
        recipeTitleTextView=findViewById(R.id.recipeTitleTextView);
        recipePeopleTextView=findViewById(R.id.recipePeopleTextView);
        recipeMinuteTextView=findViewById(R.id.recipeMinuteTextView);
        recipeDifficultyTextView=findViewById(R.id.recipeDifficultyTextView);
        recipeSourceTextView=findViewById(R.id.recipeSourceTextView);

        exit_button=findViewById(R.id.exit_button);
        delete_button=findViewById(R.id.delete_button);

        //Recycler view 세팅
        orderRecyclerView=findViewById(R.id.orderRecyclerView);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        orderRecyclerView.setLayoutManager(layoutManager);

        adapter = new OrderAdapter();

        Intent intent=getIntent();
        String modify_RId=intent.getStringExtra("modify_RId");

        //url 받아오기
        MyApplication myApp = (MyApplication) getApplication();
        String url=myApp.getGlobalString();
        url+="/recipe/?rId="+modify_RId;

        StringRequest RecipeShowRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray recipesArray=jsonObject.optJSONArray("recipes");
                    JSONObject element;

                    for(int i=0;i<recipesArray.length();i++){
                        element=(JSONObject) recipesArray.opt(i);

                        recipeTitleTextView.setText(element.optString("recipe_title"));
                        recipePeopleTextView.setText(element.optString("serving"));
                        recipeMinuteTextView.setText(element.optString("cookingTime"));
                        recipeDifficultyTextView.setText(element.optString("difficult"));
                        recipeSourceTextView.setText(element.optString("recipe_source"));

                        String imgPath=element.optString("menu_img");

                        if(imgPath!=null&&!imgPath.equals("")){
                            Glide.with(RecipeImageView).load(imgPath).into(RecipeImageView);
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse=error.networkResponse;
                //서버 잘못되었을 때
                Log.d("statuscode", "" + networkResponse.statusCode);
                Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(WishRecipeActivity.this);
        requestQueue.add(RecipeShowRequest);

        //url 받아오기
        String RecipeOrderUrl=myApp.getGlobalString();
        RecipeOrderUrl+="/recipe/order/?rId="+modify_RId;

        StringRequest RecipeOrderRequest = new StringRequest(Request.Method.GET, RecipeOrderUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray orderArray=jsonObject.optJSONArray("r_order");
                    JSONObject element;

                    ArrayList<OrderItem> items=new ArrayList<OrderItem>();

                    for(int i=0;i<orderArray.length();i++){
                        element=(JSONObject) orderArray.opt(i);
                        items.add(new OrderItem(element.optString("rId")
                                ,element.optString("recipe_order")
                                ,element.optString("description")));
                        Log.d("ordernumber",element.optString("recipe_order"));
                    }
                    adapter.setItems(items);
                    adapter.notifyDataSetChanged();

                }catch (JSONException e){

                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse=error.networkResponse;
                //서버 잘못되었을 때
                if(networkResponse.statusCode==421){
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText( getApplicationContext(), "해당하는 레시피가 없습니다.", Toast.LENGTH_SHORT ).show();
                }
                else{
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();

                }
            }
        });
        RequestQueue requestQueue2 = Volley.newRequestQueue(WishRecipeActivity.this);
        requestQueue2.add(RecipeOrderRequest);

        orderRecyclerView.setAdapter(adapter) ;

        //뒤로 가기
        exit_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( WishRecipeActivity.this, WishListActivity.class );
                startActivity( intent );
                finish();
            }
        });

        //삭제하기
        delete_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                //url 받아오기
                MyApplication myApp = (MyApplication) getApplication();
                String url=myApp.getGlobalString();
                url += "/users/access";

                StringRequest TokenValidateRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //url 받아오기
                        String WishListUrl=myApp.getGlobalString();
                        WishListUrl+="/recipe/wishlist/?rId="+modify_RId;

                        StringRequest WishListDeleteRequest = new StringRequest(Request.Method.DELETE, WishListUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText( getApplicationContext(), "찜 목록에서 레시피가 삭제되었습니다.", Toast.LENGTH_SHORT ).show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse networkResponse=error.networkResponse;
                                if(networkResponse.statusCode==411){
                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                    Toast.makeText( getApplicationContext(), "사용자의 정보에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
                                }
                                else if(networkResponse.statusCode==421){
                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                    Toast.makeText( getApplicationContext(), "찜 목록으로 등록되지 않은 레시피입니다.", Toast.LENGTH_SHORT ).show();
                                }
                                else{
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
                        RequestQueue requestQueue2 = Volley.newRequestQueue(WishRecipeActivity.this);
                        requestQueue2.add(WishListDeleteRequest);
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

                                        //url 받아오기
                                        String WishListUrl=myApp.getGlobalString();
                                        WishListUrl+="/recipe/wishlist/?rId="+modify_RId;

                                        StringRequest RecipeOrderRequest = new StringRequest(Request.Method.DELETE, WishListUrl, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Toast.makeText( getApplicationContext(), "찜 목록에서 레시피가 삭제되었습니다.", Toast.LENGTH_SHORT ).show();
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                NetworkResponse networkResponse=error.networkResponse;
                                                if(networkResponse.statusCode==411){
                                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                                    Toast.makeText( getApplicationContext(), "사용자의 정보에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
                                                }
                                                else if(networkResponse.statusCode==421){
                                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                                    Toast.makeText( getApplicationContext(), "찜 목록으로 등록되지 않은 레시피입니다.", Toast.LENGTH_SHORT ).show();
                                                }
                                                else{
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
                                        RequestQueue requestQueue2 = Volley.newRequestQueue(WishRecipeActivity.this);
                                        requestQueue2.add(RecipeOrderRequest);

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
                            RequestQueue requestQueue = Volley.newRequestQueue(WishRecipeActivity.this);
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
                RequestQueue requestQueue = Volley.newRequestQueue(WishRecipeActivity.this);
                requestQueue.add(TokenValidateRequest);

            }
        });
    }
}