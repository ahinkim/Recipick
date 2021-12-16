package org.techtown.recipe.ranking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.main.MainAdapter;
import org.techtown.recipe.main.MainItem;
import org.techtown.recipe.main.OnMainItemClickListener;
import org.techtown.recipe.main.RecipeActivity;
import org.techtown.recipe.mypage.MyPageActivity;
import org.techtown.recipe.R;
import org.techtown.recipe.main.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class RankingActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private RankingAdapter adapter;
    private RecyclerView recyclerView;
    private BottomNavigationView navigation;
    private SwipeRefreshLayout swipeRefreshLayout;

    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        //단말에 저장된 token 받아오기
        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String accessToken = preferences.getString("accessToken", "");
        String refreshToken = preferences.getString("refreshToken", "");

        //헤더에 토큰 넣기
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accessToken", accessToken);
        headers.put("refreshToken", refreshToken);

        ProgressDialog dialog = ProgressDialog.show(RankingActivity.this, "",
                "랭킹을 불러오고 있는 중입니다.", true);

        //Recycler view 세팅
        recyclerView=findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RankingAdapter();

        navigation = findViewById( R.id.navigation );

        navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent1 = new Intent( RankingActivity.this, MainActivity.class );
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity( intent1 );
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.mypage:
                        Intent intent2 = new Intent( RankingActivity.this, MyPageActivity.class );
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity( intent2 );
                        overridePendingTransition(0,0);
                        finish();
                        break;
                }
                return true;
            }
        });
        navigation.setSelectedItemId(R.id.ranking);

        //Default 리스트 받아오기

        //url 받아오기
        MyApplication myApp = (MyApplication) getApplication();
        String url=myApp.getGlobalString();
        url += "/recipe/Ranking";

        StringRequest DefaultRankingRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //access token 유효하면 diaries jsonArray 받아오기
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray recipesArray=jsonObject.optJSONArray("recipes");
                    JSONObject element;

                    ArrayList<RankingItem> items=new ArrayList<RankingItem>();
                    for(int i=0;i<recipesArray.length();i++){
                        element=(JSONObject) recipesArray.opt(i);

                        JSONObject rIdArray=element.optJSONObject("rId");
                        String rank=Integer.toString(i+1);
                        items.add(new RankingItem(rank
                                ,rIdArray.optString("rId")
                                ,rIdArray.optString("recipe_title")
                                ,rIdArray.optString("menu_img")
                                ,rIdArray.optString("recipe_url")));
                        Log.d("menu_img",rIdArray.optString("menu_img"));
                    }
                    adapter.setItems(items);
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
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
        RequestQueue requestQueue = Volley.newRequestQueue(RankingActivity.this);
        requestQueue.add(DefaultRankingRequest);

        recyclerView.setAdapter(adapter) ;

        //레시피 클릭했을 때
        adapter.setOnItemClickListener(new OnRankingItemClickListener() {
            @Override
            public void onItemClick(RankingAdapter.ViewHolder holder, View view, int position) {
                RankingItem item=adapter.getItem(position);

                String modify_RId=item.getRId();
                String recipe_url=item.getRecipe_url();

                Intent intent = new Intent( RankingActivity.this, RecipeActivity.class );

                intent.putExtra("modify_RId",modify_RId);
                intent.putExtra("recipe_url",recipe_url);

                startActivity( intent );
                //finish();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Default 리스트 받아오기

                //url 받아오기
                MyApplication myApp = (MyApplication) getApplication();
                String url=myApp.getGlobalString();
                url += "/recipe/Ranking";

                StringRequest DefaultRankingRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //access token 유효하면 diaries jsonArray 받아오기
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray recipesArray=jsonObject.optJSONArray("recipes");
                            JSONObject element;

                            ArrayList<RankingItem> items=new ArrayList<RankingItem>();
                            for(int i=0;i<recipesArray.length();i++){
                                element=(JSONObject) recipesArray.opt(i);

                                JSONObject rIdArray=element.optJSONObject("rId");
                                String rank=Integer.toString(i+1);
                                items.add(new RankingItem(rank
                                        ,rIdArray.optString("rId")
                                        ,rIdArray.optString("recipe_title")
                                        ,rIdArray.optString("menu_img")
                                        ,rIdArray.optString("recipe_url")));
                                Log.d("menu_img",rIdArray.optString("menu_img"));
                            }
                            adapter.setItems(items);
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
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
                RequestQueue requestQueue = Volley.newRequestQueue(RankingActivity.this);
                requestQueue.add(DefaultRankingRequest);

                recyclerView.setAdapter(adapter) ;

                /* 업데이트가 끝났음을 알림 */
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
