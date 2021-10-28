package org.techtown.recipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private EditText join_id, join_password, join_pwck;
    private Button join_button, check_button;
    private AlertDialog dialog;
    private boolean validate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_join );

        //아이디값 찾아주기
        join_id = findViewById( R.id.join_id );
        join_password = findViewById( R.id.join_password );
        join_pwck = findViewById(R.id.join_pwck);

        //아이디 중복 체크
        check_button = findViewById(R.id.check_button);
        check_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String id = join_id.getText().toString();
                if (validate) {
                    return; //검증 완료
                }

                if (id.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디를 입력하세요.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //POST 요청
                String url = "http://81a0-182-222-218-49.ngrok.io/userValidate";
                RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);

                JSONObject postData = new JSONObject();
                try {
                    postData.put("userId", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                        dialog = builder.setMessage("사용할 수 있는 아이디입니다.").setPositiveButton("확인", null).create();
                        dialog.show();
                        join_id.setEnabled(false); //아이디값 고정
                        validate = true; //검증 완료
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse.statusCode == 401) {
                            //이미 있는 아이디
                            Log.d("statuscode", "" + networkResponse.statusCode);
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            dialog = builder.setMessage("이미 존재하는 아이디입니다.").setNegativeButton("확인", null).create();
                            dialog.show();
                        }
                    }
                });
                requestQueue.add(jsonObjectRequest);

            }
        });

        //회원가입 버튼 클릭 시 수행
        join_button = findViewById( R.id.join_button );
        join_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userId = join_id.getText().toString();
                final String password = join_password.getText().toString();
                final String PassCk = join_pwck.getText().toString();

                //아이디 중복체크 했는지 확인
                if (!validate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("중복된 아이디가 있는지 확인하세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //한 칸이라도 입력 안했을 경우
                if (userId.equals("") || password.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("모두 입력해주세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //비밀번호가 다르게 입력되었을 경우
                if(!password.equals(PassCk)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호가 동일하지 않습니다.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                //startActivity(intent);

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                        dialog = builder.setMessage("회원가입이 완료되었습니다.").setNegativeButton("확인", null).create();
                        dialog.show();
                    }

                };
                Response.ErrorListener errorListener=new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse=error.networkResponse;
                        if(networkResponse.statusCode==401){
                            //이미 있는 아이디
                            Log.d("statuscode",""+networkResponse.statusCode);
                        }
                        else if(networkResponse.statusCode==400){
                            //요청 형태 잘못됨
                            Log.d("statuscode1",""+networkResponse.statusCode);
                        }
                        else{
                            //알 수 없는 문제
                            Log.d("statuscode2",""+networkResponse.statusCode);
                        }
                    }
                };
                try {
                    JSONObject sObject = new JSONObject();//배열 내에 들어갈 json
                    sObject.put("userId", userId);
                }catch(JSONException e){
                    e.printStackTrace();
                }

                //서버로 Volley를 이용해서 요청
                //RegisterRequest registerRequest = new RegisterRequest( sObject, password, responseListener,errorListener);
                RequestQueue queue = Volley.newRequestQueue( RegisterActivity.this );
                //queue.add( registerRequest );
            }
        });
    }
}