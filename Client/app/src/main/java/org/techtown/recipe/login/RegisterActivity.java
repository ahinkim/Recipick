package org.techtown.recipe.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private EditText join_id, join_password, join_pwck;
    private Button join_button, check_button;
    private ImageButton exit_button;
    private AlertDialog dialog;
    private boolean validate = false;
    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_join );

        //아이디값 찾아주기
        join_id = findViewById( R.id.join_id );
        join_password = findViewById( R.id.join_password );
        join_pwck = findViewById(R.id.join_pwck);

        exit_button=findViewById(R.id.exit_button);

        //아이디 중복 체크
        check_button = findViewById(R.id.check_button);
        check_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String userId = join_id.getText().toString();

                //헤더에 아이디 넣기
                HashMap<String, String> headers = new HashMap<>();
                headers.put("userId", userId);

                if (validate) {
                    return; //검증 완료
                }

                if ("".equals(userId)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디를 입력하세요.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }
                //영어 소문자와 숫자 외에 다른 문자가 들어갔을 때
                if(!Pattern.matches("^[0-9a-z]*$", userId)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디에는 영문 소문자와 숫자만 사용 가능합니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }
                //아이디에 숫자만 있을 때
                if(Pattern.matches("^[0-9]*$",userId)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디에는 영문 소문자가 반드시 포함되어야 합니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                if(userId.length()<4){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디는 최소 4글자 이상이어야 합니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }
                //아이디 공백 체크
                if(userId.replace(" ","").length()!=userId.length()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디에 공백을 사용할 수 없습니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }



                //POST 요청
                //url 받아오기
                MyApplication myApp = (MyApplication) getApplication();
                String url=myApp.getGlobalString();
                url += "/users/validate";

                JSONObject postData = new JSONObject();
                try {
                    postData.put("userId", userId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest ValidateRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
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
                RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
                requestQueue.add(ValidateRequest);
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

                //헤더에 아이디 넣기
                HashMap<String, String> headers = new HashMap<>();
                headers.put("userId", userId);
                headers.put("password",password);

                //아이디 중복체크 했는지 확인
                if (!validate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("중복된 아이디가 있는지 확인해주세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //한 칸이라도 입력 안했을 경우
                if ("".equals("userId")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디를 입력해 주세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }
                if ("".equals(password)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호를 입력해 주세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }
                if ("".equals(PassCk)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호를 확인해 주세요.").setNegativeButton("확인", null).create();
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

                //아이디에 영어 소문자와 숫자 외에 다른 문자가 들어갔을 때
                if(!Pattern.matches("^[0-9a-z]*$", userId)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디에는 영문 소문자와 숫자만 사용 가능합니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //아이디에 숫자만 있을 때
                if(Pattern.matches("^[0-9]*$",userId)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디에는 영문 소문자가 반드시 포함되어야 합니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //비밀번호에 한글이 들어갔을 때
                if(Pattern.matches("^[가-힣]*$", password)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호에는 영문, 숫자, 특수문자만 사용 가능합니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }
                //아이디 글자수
                if(userId.length()<4){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디는 최소 4글자 이상이어야 합니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //비밀번호 글자수
                if(password.length()<4){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호는 최소 4글자 이상이어야 합니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //아이디 공백 체크
                if(userId.replace(" ","").length()!=userId.length()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디에 공백을 사용할 수 없습니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //비밀번호 공백 체크
                if(password.replace(" ","").length()!=password.length()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호에 공백을 사용할 수 없습니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //url 받아오기
                MyApplication myApp = (MyApplication) getApplication();
                String url=myApp.getGlobalString();
                url += "/users";

                StringRequest RegisterRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText( getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT ).show();

                        Intent intent = new Intent( RegisterActivity.this, LoginActivity.class );
                        startActivity( intent );
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse=error.networkResponse;
                        if(networkResponse.statusCode==401){
                            //이미 있는 아이디
                            Log.d("statuscode",""+networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT ).show();

                        }
                        else if(networkResponse.statusCode==400){
                            //요청 형태 잘못됨
                            Log.d("statuscode",""+networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "요청 형태가 잘못되었습니다. 다시 시도해주십시오.", Toast.LENGTH_SHORT ).show();
                        }
                        else{
                            //서버 문제
                            Log.d("statuscode",""+networkResponse.statusCode);
                            Toast.makeText( getApplicationContext(), "서버에 문제가 생겼습니다. 다시 시도해주십시오.", Toast.LENGTH_SHORT ).show();
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return headers;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
                requestQueue.add(RegisterRequest);

                overridePendingTransition(0,0);
                finish();
            }
        });

        //뒤로 가기
        exit_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}