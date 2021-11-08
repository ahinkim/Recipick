package org.techtown.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class RankingActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BottomNavigationView navigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

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
    }
}
