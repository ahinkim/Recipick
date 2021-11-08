package org.techtown.recipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private EditText login_id, login_password;
    private Button login_button, join_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_login );

        login_id = findViewById( R.id.login_id );
        login_password = findViewById( R.id.login_password );

        join_button = findViewById( R.id.join_button );
        login_button = findViewById( R.id.login_button );

        join_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( LoginActivity.this, RegisterActivity.class );
                startActivity( intent );
            }
        });

        login_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = login_id.getText().toString();
                String password = login_password.getText().toString();

                //헤더에 아이디 넣기
                HashMap<String, String> headers = new HashMap<>();
                headers.put("userId", userId);
                headers.put("password",password);

                Response.Listener<String> responseListener= new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String accessToken=jsonObject.getString("accessToken");
                            String refreshToken=jsonObject.getString("refreshToken");

                            //getSharedPreferences("파일이름",'모드')
                            //모드 => 0 (읽기,쓰기가능)
                            //모드 => MODE_PRIVATE (이 앱에서만 사용가능)
                            preferences = getSharedPreferences("UserToken", MODE_PRIVATE);

                            //Editor를 preferences에 쓰겠다고 연결
                            SharedPreferences.Editor editor = preferences.edit();

                            //putString(KEY,VALUE)
                            editor.putString("accessToken", accessToken);
                            editor.putString("refreshToken",refreshToken);

                            //항상 commit & apply 를 해주어야 저장이 된다.
                            editor.commit();

                            Toast.makeText( getApplicationContext(), "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT ).show();
                            Intent intent = new Intent( LoginActivity.this, MainActivity.class );
                            startActivity( intent );
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse.statusCode == 401) {
                            //아이디나 비밀번호 다를 때
                            Log.d("statuscode", "" + networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "아이디나 비밀번호가 다릅니다.", Toast.LENGTH_SHORT ).show();
                        }
                        else{
                            //서버 잘못되었을 때
                            Log.d("statuscode", "" + networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT ).show();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest( headers, responseListener, errorListener );
                RequestQueue queue = Volley.newRequestQueue( LoginActivity.this );
                queue.add( loginRequest );
            }
        });
    }
}