package org.techtown.recipe;

import android.app.Application;

public class MyApplication extends Application
{
    private String mGlobalString="http://1338-182-222-218-49.ngrok.io";//url을 전역변수로 만듦

    public String getGlobalString()
    {
        return mGlobalString;
    }
}