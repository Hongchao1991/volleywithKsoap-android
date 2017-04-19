package com.zhang.testvk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zhang.volleywithksoap.base.ksoap2.SoapEnvelope;
import com.zhang.volleywithksoap.base.ksoap2.serialization.SoapObject;
import com.zhang.volleywithksoap.base.ksoap2.serialization.SoapSerializationEnvelope;
import com.zhang.volleywithksoap.volleywithksoap.RequestQueue;
import com.zhang.volleywithksoap.volleywithksoap.Response;
import com.zhang.volleywithksoap.volleywithksoap.VolleyError;
import com.zhang.volleywithksoap.volleywithksoap.toolbox.KsoapRequest;
import com.zhang.volleywithksoap.volleywithksoap.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String namespace = "http://WebXml.com.cn/";
        String urlAddress = "http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx";
        String method = "getMobileCodeInfo";
        String soapAction = "http://WebXml.com.cn/getMobileCodeInfo";

        Map<String,Object> map = new HashMap<String, Object>();
        map.put("mobileCode","phone number");
        map.put("userID","");

        getMessage(namespace,method,soapAction,urlAddress,map);
    }

    private void getMessage(String namespace, final String method, String soapAction, String urlAddress, Map<String,Object> parameters){
        RequestQueue queue = Volley.newRequestQueue(this);
        SoapObject soapObject = new SoapObject(namespace,method);

        Iterator<Map.Entry<String,Object>> iterator = parameters.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,Object> entry = iterator.next();
            if (entry.getValue() instanceof ArrayList) {
                SoapObject parameterValue = new SoapObject();
                for (String str : (ArrayList<String>) entry.getValue()) {
                    parameterValue.addProperty("long", Long.valueOf(str));
                }

                soapObject.addProperty(entry.getKey(), parameterValue);
            } else soapObject.addProperty(entry.getKey(), entry.getValue());
            Log.e("request=====","key:"+entry.getKey()+",value:"+entry.getValue());
        }

        SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut=soapObject;
        envelope.dotNet=true;
        envelope.setOutputSoapObject(soapObject);

        Response.Listener<SoapSerializationEnvelope> listener =
                new Response.Listener<SoapSerializationEnvelope>(){

                    @Override
                    public void onResponse(SoapSerializationEnvelope soapSerializationEnvelope) {
                        SoapObject object = (SoapObject) soapSerializationEnvelope.bodyIn;
                        System.out.println("----------->"+object.getProperty(method+"Result"));
                    }
                };
        Response.ErrorListener errorListener = new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("exception",volleyError.getMessage());
            }
        };


        KsoapRequest request = new KsoapRequest(soapAction,envelope,urlAddress,listener,errorListener);
        queue.add(request);
    }
}
