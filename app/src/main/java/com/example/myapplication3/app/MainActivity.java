package com.example.myapplication3.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import android.widget.ListView;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    TextView t1;
    Button btn1;
    EditText edt1;
    ListView list;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "0.1a";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    static final String SENDER_ID = "888200769122";
    static final String TAG = "GCMDemo"; // Log TAG
    static final int PICK_ACCOUNT_REQUEST = 1; // unique request identifier
    GoogleCloudMessaging gcm;
    String notification_id;
    String notification_type;
    String notification_course;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid;
    TextView mDisplay;
    public static String UserID = "";
    String accountName; // User's mail id
    private boolean calledNotification = false;
    public String token; // google oauth2 token
    String output = ""; // output from the common async task function
    public static String UserInfo;
    public static String NotificationsInfo;
    JSONArray notificationsinfo;
    JSONObject userinfo;
    SharedPreferences settings;
    Button loginButton;
    CookieManager cookieMan;
    ArrayList<String> posts;
    ArrayAdapter adp;
    URL url;
    private ProgressBar progressBar, progressBar2;
    static final String scope = "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t1 = (TextView) findViewById(R.id.text1);
        btn1 = (Button) findViewById((R.id.btn1));
        t1.setText("Android");
        edt1 = (EditText) findViewById(R.id.edit1);
        registerInBackground();
        list = (ListView) findViewById(R.id.list);
        posts = new ArrayList<String>();
        adp = new ArrayAdapter(this, android.R.layout.simple_list_item_1, posts);
        list.setAdapter(adp);
        try{
            url =  new URL("http://iiitddemo.appspot.com/json/all");
        }
        catch (MalformedURLException e){

        }
        new DownloadPostsTask().execute(url);
    }

    public void changetext(View v){
        String t = edt1.getText().toString();
        t1.setText(t);
        Toast.makeText(this,t,Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            return rootView;
//        }
 //   }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void registerInBackground() {
        // //Log.d(TAG, "registering in background");
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... arg0) {

                // //Log.d(TAG, "starting");
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplication());
                    }
                    if (gcm==null) Log.d("gcm", "null");
                    regid = gcm.register(SENDER_ID);
                    Log.d("gcm", regid);
                    msg = "Device registered, registration ID=" + regid;
                    sendRegistrationIdToBackend();
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
                // TODO Auto-generated method stub
            }
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use
     * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
     * since the device sends upstream messages to a server that echoes back the
     * message using the 'from' address in the message.
     */

    private void sendRegistrationIdToBackend() {
        // Your implementation here.
        // //Log.d(TAG, "Sending regid to backpack");
        getDataInAsyncTask("http://iiitddemo.appspot.com/register_gcm/" + regid);
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("GCM", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        return 1;
		/*
		 * try { PackageInfo packageInfo = context.getPackageManager()
		 * .getPackageInfo(context.getPackageName(), 0); return
		 * packageInfo.versionCode; } catch (NameNotFoundException e) { //
		 * should never happen throw new
		 * RuntimeException("Could not get package name: " + e); }
		 */
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context
     *            application's context.
     * @param regId
     *            registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("Play Services", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    void getDataInAsyncTask(final String url) {

        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected String doInBackground(Void... params) {
                String output = getData(url);
                return output;
            }

            @Override
            protected void onPostExecute(String v) {
               Toast.makeText(getApplication(),"registered",Toast.LENGTH_SHORT).show();
            }
        };
        task.execute();
    }

    /*
     * this method id called by above asynctask to get data from server
     */
    protected String getData(String url) {

        //String cookie = CookieManager.getInstance().getCookie(ActivityMain.ServerURL);
        URL urlObject;
        HttpURLConnection urlConn = null;
        String outputData = "";
        try {
            urlObject = new URL(url);

            urlConn = (HttpURLConnection) urlObject.openConnection();
            urlConn.setRequestMethod("GET");
//            urlConn.addRequestProperty("Authorization",
//                    "Token token=1d04aa222a62042b135b888dd2d61e75");
//            urlConn.addRequestProperty("Cookie", cookie);
//            urlConn.setDoOutput(true);

//            if (postData != null) {
////				urlConn.setDoInput(true);
//                OutputStreamWriter wr = new OutputStreamWriter(
//                        urlConn.getOutputStream());
//                wr.write(postData);
//                wr.flush();
//            }

            InputStream in = new BufferedInputStream(urlConn.getInputStream());
            BufferedReader buffin = new BufferedReader(
                    new InputStreamReader(in));
            StringBuilder responseStrBuilder = new StringBuilder();
            while ((outputData = buffin.readLine()) != null) {
                responseStrBuilder.append(outputData);
            }
            outputData = responseStrBuilder.toString();
            Log.d("Response code", urlConn.getResponseCode() + "    " + url);
        } catch (MalformedURLException e1) {

            e1.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            if (urlConn != null)
                urlConn.disconnect();
        }

        return outputData;
    }

    private class DownloadPostsTask extends AsyncTask<URL, Void, String> {
        protected String doInBackground(URL... urls) {
            String url = urls[0].toString();
            URL urlObject;
            HttpURLConnection urlConn = null;
            String outputData = "";
            try {
                urlObject = new URL(url);

                urlConn = (HttpURLConnection) urlObject.openConnection();
                urlConn.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(urlConn.getInputStream());
                BufferedReader buffin = new BufferedReader(
                        new InputStreamReader(in));
                StringBuilder responseStrBuilder = new StringBuilder();
                while ((outputData = buffin.readLine()) != null) {
                    responseStrBuilder.append(outputData);
                }
                outputData = responseStrBuilder.toString();
                Log.d("Response code", urlConn.getResponseCode() + "    " + url);
            } catch (MalformedURLException e1) {

                e1.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            } finally {
                if (urlConn != null)
                    urlConn.disconnect();
            }

            return outputData;

        }

        protected void onPostExecute(String result) {
            try {
                JSONArray js = new JSONArray(result);
                for (int i = 0; i < js.length(); i++) {
                    posts.add(js.getJSONObject(i).getString("user") + " posted " + js.getJSONObject(i).getString("content") + "\n" + js.getJSONObject(i).getString("likes") + " Likes");
                    adp.notifyDataSetChanged();
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
