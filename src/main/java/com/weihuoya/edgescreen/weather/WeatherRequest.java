package com.weihuoya.edgescreen.weather;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;


public class WeatherRequest extends StringRequest {

    public WeatherRequest(int method, String url, Response.Listener<String> listener,
                          Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public WeatherRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String content;
        String ContentEncoding = response.headers.get("Content-Encoding");

        if(ContentEncoding != null && ContentEncoding.equals("gzip")) {
            StringBuilder output = new StringBuilder();
            try {
                GZIPInputStream gStream = new GZIPInputStream(new ByteArrayInputStream(response.data));
                InputStreamReader reader = new InputStreamReader(gStream);
                BufferedReader in = new BufferedReader(reader, 16384);

                String read;

                while ((read = in.readLine()) != null) {
                    output.append(read).append("\n");
                }
                reader.close();
                in.close();
                gStream.close();
            } catch (IOException e) {
                return Response.error(new ParseError());
            }

            content = output.toString();
        } else {
            content = new String(response.data, StandardCharsets.UTF_8);
        }

        return Response.success(content, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String>  params = new HashMap<String, String>();
        params.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
        params.put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
        params.put("Accept-Encoding", "gzip");
        params.put("Accept", "*/*");
        params.put("Referer", "http://m.weather.com.cn/");
        return params;
    }
}