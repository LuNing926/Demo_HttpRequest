package com.example.root.demo_httprequest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
//    String hostUri = "http://166.111.120.235/";
//    String registeUri = "http://166.111.120.235/id/u/register";
//    String loginGetUri = "http://166.111.120.235/oauth/login/";
//    String loginPostUri = "http://166.111.120.235/id/user/login?destination=oauth2/authorize";
//    String referer = "http://166.111.120.235/accounts/login/?next=/";
    String hostUri = "http://dev.openthos.org/";
    String registeUri = "http://dev.openthos.org/id/u/register";
    String loginGetUri = "http://dev.openthos.org/oauth/login/";
    String loginPostUri = "http://dev.openthos.org/id/user/login?destination=oauth2/authorize";
    String referer = "http://dev.openthos.org/accounts/login/?next=/";
    String redirect = "http.protocol.handle-redirects";
    String location, csrftoken, sessionid, sess, form_build_id, username, name, contact_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread() {
            @Override
            public void run() {
                try {

//                    registeStep1Post();
                    loginStep1Get();
                } catch (Exception e) {
                    System.out.println("Error=" + e.toString());
                }
            }
        }.start();
    }

    private void showErrorToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registeStep1Post() throws Exception {
        URI uri = new URI(registeUri);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(redirect, true);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpPost post = new HttpPost(uri);

        List<NameValuePair> valuePairList = new ArrayList<>();
        valuePairList.add(new BasicNameValuePair("id", "sdfsdfdsf"));
        valuePairList.add(new BasicNameValuePair("email", "sdfsdfdsf@openthos.org"));
        valuePairList.add(new BasicNameValuePair("passwd", "1"));

        post.setEntity(new UrlEncodedFormEntity(valuePairList, HTTP.UTF_8));
        post.setParams(httpParams);

        HttpResponse httpResponse = httpClient.execute(post);
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            Header[] allHeaders = httpResponse.getAllHeaders();
            for (int i = 0; i < allHeaders.length; i++) {
                android.util.Log.i("registeStep1Post_head", allHeaders[i].toString());
            }
            HttpEntity entity = httpResponse.getEntity();
            InputStream in = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            String temp = "";

            try {
                while ((line = reader.readLine()) != null) {
//                    if (line.contains("Sorry, unrecognized username or password")) {
//                        android.util.Log.i("step5Post_entity", line);
//                    }
                    android.util.Log.i("registeStep1Post_entity", line);

                }
            } catch (IOException e) {
                System.out.println("Error=" + e.toString());
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Error=" + e.toString());
                }
            }
        }
    }

    private void loginStep1Get() throws Exception{
        URI uri = new URI(hostUri);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(redirect, true);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpUriRequest get = new HttpGet(uri);
        get.setParams(httpParams);
        HttpResponse httpResponse = httpClient.execute(get);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("loginStep1Get", statusCode + "");

        if (statusCode == 200) {
            Header[] allHeaders = httpResponse.getAllHeaders();
            for (int i = 0; i < allHeaders.length; i++) {
                if (allHeaders[i].toString().contains("csrftoken")) {
                    String[] strings = allHeaders[i].toString().split("=");
                    csrftoken = strings[1].split(";")[0];
                }
                if (allHeaders[i].toString().contains("sessionid")) {
                    String[] strings = allHeaders[i].toString().split("=");
                    sessionid = strings[1].split(";")[0];
                    break;
                }
            }
            loginStep2Get(csrftoken, sessionid);
        }
    }

    private void loginStep2Get(String csrftoken, String sessionid) throws Exception{
        URI uri = new URI(loginGetUri);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(redirect, false);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpUriRequest get = new HttpGet(uri);
        get.setHeader("Referer",referer);
        get.setHeader("Cookie","csrftoken=" + csrftoken + ";" +
                " django_language=zh-cn; has_js=1; sessionid=" +  sessionid);

        get.setParams(httpParams);
        HttpResponse httpResponse = httpClient.execute(get);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("loginStep2Get", statusCode + "");

        if (statusCode == 302) {
            Header[] allHeaders = httpResponse.getAllHeaders();
            for (int i = 0; i < allHeaders.length; i++) {
                if (allHeaders[i].toString().contains("Location")) {
                    String[] strings = allHeaders[i].toString().split("Location: ");
                    location = strings[1];
                    break;
                }
            }
            loginStep3Get(location, csrftoken, sessionid);
        }
    }

    private void loginStep3Get(String location, String csrftoken, String sessionid) throws Exception{
        URI uri = new URI(location);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(redirect, false);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpUriRequest get = new HttpGet(uri);
        get.setHeader("Referer",referer);
        get.setHeader("Cookie","csrftoken=" + csrftoken + ";" +
                " django_language=zh-cn; has_js=1; sessionid=" +  sessionid);

        get.setParams(httpParams);
        HttpResponse httpResponse = httpClient.execute(get);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("loginStep3Get", statusCode + "");

        if (statusCode == 302) {
            Header[] allHeaders = httpResponse.getAllHeaders();
            for (int i = 0; i < allHeaders.length; i++) {
                if (allHeaders[i].toString().contains("Location")) {
                    String[] strings = allHeaders[i].toString().split("Location: ");
                    location = strings[1];
                }
                if (allHeaders[i].toString().contains("Set-Cookie:")) {
                    String[] strings = allHeaders[i].toString().split("; ");
                    sess = strings[0].split("Set-Cookie: ")[1];
                    break;
                }
            }
            loginStep4Get(location, csrftoken, sessionid, sess);
        }
    }

    private void loginStep4Get(String location, String csrftoken,
                          String sessionid, String sess) throws Exception{
        URI uri = new URI(location);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(redirect, false);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpUriRequest get = new HttpGet(uri);
        get.setHeader("Referer",referer);
        get.setHeader("Cookie","csrftoken=" + csrftoken + ";" +
                " django_language=zh-cn; has_js=1; sessionid=" +  sessionid + "; " + sess);

        get.setParams(httpParams);
        HttpResponse httpResponse = httpClient.execute(get);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("loginStep4Get", statusCode + "");

        if (statusCode == 200) {
            HttpEntity entity = httpResponse.getEntity();
            InputStream in = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.contains("form_build_id")) {
                        String[] split = line.split("=");
                        String s = split[split.length - 1];
                        form_build_id = s.substring(0, s.length() - 3);
                        break;
                    }
                }
                loginStep5Post(csrftoken, sessionid, sess, form_build_id);
            } catch (IOException e) {
                System.out.println("Error=" + e.toString());
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Error=" + e.toString());
                }
            }
        }
    }

    private void loginStep5Post(String csrftoken, String sessionid, String sess,
                           String form_build_id) throws Exception {
        HttpParams httpParams = new BasicHttpParams();
//        httpParams.setParameter(redirect, true);
        httpParams.setParameter(redirect, false);
        HttpResponse httpResponse = null;
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        URI url = new URI(loginPostUri);
        HttpPost post = new HttpPost(url);
//        post.setHeader("Host", host);
        post.setHeader("Referer",
                loginPostUri);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("Cookie","csrftoken=" + csrftoken + ";" +
                " django_language=zh-cn; has_js=1; sessionid=" +  sessionid + "; " + sess);

        List<NameValuePair> valuePairList = new ArrayList<>();
        valuePairList.add(new BasicNameValuePair("name", "ccc"));
        valuePairList.add(new BasicNameValuePair("pass", "123"));
        valuePairList.add(new BasicNameValuePair("form_build_id", form_build_id));
        valuePairList.add(new BasicNameValuePair("form_id", "user_login"));
        valuePairList.add(new BasicNameValuePair("op", "Log+in"));
        post.setEntity(new UrlEncodedFormEntity(valuePairList, HTTP.UTF_8));
        post.setParams(httpParams);


        httpResponse = httpClient.execute(post);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("loginStep5Post", statusCode + "");

        if (statusCode == 302) {
            Header[] allHeaders = httpResponse.getAllHeaders();
            for (int i = 0; i < allHeaders.length; i++) {
                if (allHeaders[i].toString().contains("Location")) {
                    String[] strings = allHeaders[i].toString().split("Location: ");
                    location = strings[1];
                }
                if (allHeaders[i].toString().contains("Set-Cookie:")) {
                    String[] strings = allHeaders[i].toString().split("; ");
                    sess = strings[0].split("Set-Cookie: ")[1];
                    break;
                }
            }
            step6Get(location, csrftoken, sessionid, sess);
        }

        if (statusCode == 200) {
            HttpEntity entity = httpResponse.getEntity();
            InputStream in = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            String temp = "";

            try {
                while ((line = reader.readLine()) != null) {
                    android.util.Log.i("step5Post_entity", line);
                    if (line.contains("Sorry, unrecognized username or password")) {
                        android.util.Log.i("step5Post_entity", line);
                        return;
                    }
                }
                android.util.Log.i("step5Post_entity", "login success!");
            } catch (IOException e) {
                System.out.println("Error=" + e.toString());
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Error=" + e.toString());
                }
            }
        }
    }

    private void step6Get(String location, String csrftoken,
                          String sessionid, String sess) throws Exception{
        URI uri = new URI(location);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(redirect, false);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpUriRequest get = new HttpGet(uri);
        get.setHeader("Referer",
                loginPostUri);
        get.setHeader("Cookie","csrftoken=" + csrftoken + ";" +
                " django_language=zh-cn; has_js=1; sessionid=" +  sessionid + "; " + sess);
        get.setParams(httpParams);
        HttpResponse httpResponse = httpClient.execute(get);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("step6Get", statusCode + "");

        if (statusCode == 302) {
            Header[] allHeaders = httpResponse.getAllHeaders();
            for (int i = 0; i < allHeaders.length; i++) {
                if (allHeaders[i].toString().contains("Location")) {
                    String[] strings = allHeaders[i].toString().split("Location: ");
                    location = strings[1];
                    break;
                }
            }
            step7Get(location, csrftoken, sessionid, sess);
        }
    }

    private void step7Get(String location, String csrftoken,
                          String sessionid, String sess) throws Exception{
        URI uri = new URI(location);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(redirect, false);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpUriRequest get = new HttpGet(uri);
        get.setHeader("Referer",
                "http://166.111.120.235/id/user/login?destination=oauth2/authorize");
        get.setHeader("Cookie","csrftoken=" + csrftoken + ";" +
                " django_language=zh-cn; has_js=1; sessionid=" +  sessionid + "; " + sess);

        get.setParams(httpParams);
        HttpResponse httpResponse = httpClient.execute(get);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("step7Get", statusCode + "");
        if (statusCode == 302) {
            Header[] allHeaders = httpResponse.getAllHeaders();
            for (int i = 0; i < allHeaders.length; i++) {
                if (allHeaders[i].toString().contains("Location")) {
                    String[] strings = allHeaders[i].toString().split("Location: ");
                    location = strings[1];
                }
                if (allHeaders[i].toString().contains("sessionid")) {
                    String[] strings = allHeaders[i].toString().split("=");
                    sessionid = strings[1].split(";")[0];
                    break;
                }
            }
            step8Get(location, csrftoken, sessionid, sess);
        }
    }

    private void step8Get(String location, String csrftoken,
                          String sessionid, String sess) throws Exception{
        URI uri = new URI(location);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(redirect, false);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpUriRequest get = new HttpGet(uri);
        get.setHeader("Referer",
                "http://166.111.120.235/id/user/login?destination=oauth2/authorize");
        get.setHeader("Cookie","csrftoken=" + csrftoken + ";" +
                " django_language=zh-cn; has_js=1; sessionid=" +  sessionid + "; " + sess);

        get.setParams(httpParams);
        HttpResponse httpResponse = httpClient.execute(get);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("step8Get", statusCode + "");

        if (statusCode == 200) {
            HttpEntity entity = httpResponse.getEntity();
            InputStream in = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            String temp = "";

            try {
                while ((line = reader.readLine()) != null) {
                    android.util.Log.i("step8Get", "" + line);
//                    step9Get(uriEnd, csrftoken, sessionid, sess);

                    /*if (line.contains("name: ")) {
                        if (line.contains("username: ")) {
                            temp = line.split("name: ")[1];
                            username = temp.substring(0, temp.length() - 1);
                            android.util.Log.i("step8Get", username);
                        } else {
                            temp = line.split("name: ")[1];
                            name = temp.substring(0, temp.length() - 1);
                            android.util.Log.i("step8Get", name);
                        }
                    }
                    if (line.toString().contains("contact_email: ")) {
                        temp = line.split("contact_email: ")[1];
                        contact_email = temp.substring(0, temp.length() - 1);
                        android.util.Log.i("step8Get", contact_email);
                        break;
                    }*/
//                    android.util.Log.i("step8Get", line);
                }
            } catch (IOException e) {
                System.out.println("Error=" + e.toString());
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Error=" + e.toString());
                }
            }
        }
    }

    int uriEnd = 30;
    private void step9Get(int uriEnd, String csrftoken,
                          String sessionid, String sess) throws Exception{
        URI uri = new URI("http://166.111.120.235/api/v2.1/notifications/?_=15249830637");
        HttpParams httpParams = new BasicHttpParams();
//        httpParams.setParameter(redirect, false);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpUriRequest get = new HttpGet(uri);
        get.setHeader("Host","166.111.120.235");
        get.setHeader("Referer", "http://166.111.120.235/");
        get.setHeader("X-Requested-With","XMLHttpRequest");

        get.setHeader("Cookie","csrftoken=" + csrftoken + ";" +
                " has_js=1; sessionid=" +  sessionid + "; " + sess);

        get.setParams(httpParams);
        HttpResponse httpResponse = httpClient.execute(get);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        android.util.Log.i("step9Get", statusCode + "");

        if (statusCode == 200) {
            HttpEntity entity = httpResponse.getEntity();
            InputStream in = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            String temp = "";

            try {
                while ((line = reader.readLine()) != null) {
                    android.util.Log.i("step9Get", line);
                    if (line.contains("unseen_count")) {


                        break;
                    }
                    /*if (line.contains("name: ")) {
                        if (line.contains("username: ")) {
                            temp = line.split("name: ")[1];
                            username = temp.substring(0, temp.length() - 1);
                            android.util.Log.i("step8Get", username);
                        } else {
                            temp = line.split("name: ")[1];
                            name = temp.substring(0, temp.length() - 1);
                            android.util.Log.i("step8Get", name);
                        }
                    }
                    if (line.toString().contains("contact_email: ")) {
                        temp = line.split("contact_email: ")[1];
                        contact_email = temp.substring(0, temp.length() - 1);
                        android.util.Log.i("step8Get", contact_email);
                        break;
                    }*/

                }
            } catch (IOException e) {
                System.out.println("Error=" + e.toString());
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Error=" + e.toString());
                }
            }
        }
    }

    enum Mark {
        REGISTE, LOGIN
    }
}