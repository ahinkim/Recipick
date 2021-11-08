package org.techtown.recipe;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;


public class SecessionRequest extends StringRequest {
    //회원 탈퇴
    //서버 URL 설정 /userSecession
    static private String URL = "http://2a7a-182-222-218-49.ngrok.io/users/secession";
    private final Map<String, String> headers;

    public SecessionRequest(Map<String, String> headers, Response.Listener<String> listener, Response.ErrorListener errorListener ){
        super(Method.DELETE, URL, listener, errorListener);
        this.headers=headers;
    }

    @Override
    public Map<String, String>getHeaders() throws AuthFailureError {
        return headers!=null?headers:super.getHeaders();
    }

}
