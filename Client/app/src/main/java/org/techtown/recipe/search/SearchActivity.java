package org.techtown.recipe.search;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.R;
import org.techtown.recipe.main.MainActivity;
import org.techtown.recipe.main.RecipeActivity;
import org.techtown.recipe.mypage.OnWishListItemClickListener;
import org.techtown.recipe.mypage.WishListAdapter;
import org.techtown.recipe.mypage.WishListItem;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private ImageButton exit_button;
    private RecyclerView recyclerView;
    private WishListAdapter adapter;
    private SearchView searchView;

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

        ProgressDialog dialog = ProgressDialog.show(SearchActivity.this, "",
                "검색 결과를 불러오고 있는 중입니다.", true);

        //버튼 세팅
        setContentView(R.layout.activity_search);
        exit_button = findViewById(R.id.exit_button);
        searchView=findViewById(R.id.search_page_view);

        //검색어 받아오기
        Intent intent = getIntent();
        String searchWord = intent.getStringExtra("searchWord");
        searchView.setQuery(searchWord,true);//검색창에 검색어 넣는것.

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("search","검색");

                Intent intent = new Intent( SearchActivity.this, SearchActivity.class );
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

        //검색 결과 보여주기
        MyApplication myApp = (MyApplication) getApplication();
        String SearchUrl = myApp.getGlobalString();
        SearchUrl += "/recipe/search";

        JSONObject SearchData = new JSONObject();
        try {
            SearchData.put("searchWord", searchWord);
            Log.d("searchWord", searchWord);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest SearchRequest = new JsonObjectRequest(Request.Method.POST, SearchUrl, SearchData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray wishlistArray = response.optJSONArray("recipes");
                JSONObject element;

                ArrayList<WishListItem> items = new ArrayList<WishListItem>();//없애야함
                for (int i = 0; i < wishlistArray.length(); i++) {
                    element = (JSONObject) wishlistArray.opt(i);
                    items.add(new WishListItem(element.optString("rId")
                            , element.optString("recipe_title")
                            , element.optString("menu_img")
                            ,element.optString("recipe_url")));
                }
                adapter.setItems(items);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                NetworkResponse networkResponse = error.networkResponse;

                if (networkResponse.statusCode == 421) {
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "요청 형태가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    //서버에러(serialize에러)
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        RequestQueue requestQueue2 = Volley.newRequestQueue(SearchActivity.this);
        requestQueue2.add(SearchRequest);

        recyclerView.setAdapter(adapter);

        //레시피 클릭했을 때
        adapter.setOnItemClickListener(new OnWishListItemClickListener() {
            @Override
            public void onItemClick(WishListAdapter.ViewHolder holder, View view, int position) {
                WishListItem item = adapter.getItem(position);

                String modify_RId = item.getRId();
                String recipe_url=item.getRecipe_url();

                Intent intent = new Intent(SearchActivity.this, RecipeActivity.class);

                intent.putExtra("modify_RId", modify_RId);
                intent.putExtra("recipe_url",recipe_url);

                startActivity(intent);
                //finish();
            }
        });
    }
}
