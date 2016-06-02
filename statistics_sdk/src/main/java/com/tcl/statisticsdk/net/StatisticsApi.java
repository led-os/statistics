package com.tcl.statisticsdk.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tcl.statisticsdk.util.LogUtils;
import com.tcl.statisticsdk.util.MetaUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import com.orhanobut.logger.Logger;

//import com.orhanobut.logger.Logger;

/**
 * 位置信息
 *
 * @author jixiang.chen
 */
public class StatisticsApi {

    // 服务器URL for china
    // private final static String SERVER_URL_CHINA = "http://gw.csp.cn.tclclouds.com/api/log";

    private final static String SERVER_URL_CHINA = "http://gw.csp.cn.tclclouds.com/api/log";
    // 服务器URL for global
    private final static String SERVER_URL_GLOBAL = "https://gstest.udc.cn.tclclouds.com/api/device/log";
    static final String DOMAIN_GLOBAL = "global";
    static final String DOMAIN_CHINA = "china";
    private static String SERVER_URL = SERVER_URL_GLOBAL;

    private static final Proxy a = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.172", 80));
    private static final Proxy b = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.200", 80));
    private static final String HTTPS_TEST_SEVER = "https://gstest.udc.cn.tclclouds.com/api/gs/log";

    /**
     * 发送日志
     *
     * @param context
     * @param json
     * @param connectTimeout
     * @param readTimeout
     * @return
     */
    public static boolean sendLog(Context context, String json, int connectTimeout, int readTimeout) {
        initServerUrl(context);
        try {

            LogUtils.I("日志发送原始长度：" + json.length());

//            HttpURLConnection httpUrlConnection = getURLConnection(context, "http://gstest.udc.cn.tclclouds.com/api/device/log", connectTimeout, readTimeout);
            HttpURLConnection httpUrlConnection = getURLConnectionWithHttps(context, "https://gstest.udc.cn.tclclouds.com/api/gs/log", connectTimeout, readTimeout);

            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setInstanceFollowRedirects(false);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Content-Type", "gzip");
            // 关闭连接
            httpUrlConnection.setRequestProperty("Connection", "close");
            httpUrlConnection.setRequestProperty("Content-encoding", "gzip");
            httpUrlConnection.connect();
            LogUtils.I("sendLog.httpPost connected");
            StringBuilder sb = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(httpUrlConnection.getOutputStream()), "UTF-8"));

                bufferedWriter.write(json);
                // bufferedWriter.flush();
                bufferedWriter.close();
                bufferedWriter = null;

                bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));

                String str;
                while ((str = bufferedReader.readLine()) != null) {
                    sb.append(str);
                }
                bufferedReader.close();
                bufferedReader = null;

                // int i = httpUrlConnection.getContentLength();
                httpUrlConnection.disconnect();
                SERVER_URL = null;

                if ((httpUrlConnection.getResponseCode() != 200)
                // || (i != 0)
                )
                    throw new ClassNotFoundException("http code =" + httpUrlConnection.getResponseCode() + "& contentResponse=" + sb);
            } catch (IOException Exception) {
                Exception.printStackTrace();
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                httpUrlConnection.disconnect();
                throw Exception;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 初始化服务器URL
     *
     * @param context
     */
    private static void initServerUrl(Context context) {
        String serverDomain = MetaUtils.getServerDomain(context);
        if (DOMAIN_GLOBAL.equalsIgnoreCase(serverDomain))
            SERVER_URL = HTTPS_TEST_SEVER;
        else if (DOMAIN_CHINA.equalsIgnoreCase(serverDomain))
            SERVER_URL = HTTPS_TEST_SEVER;
        else
            throw new RuntimeException("you should set your Server URL in your AndroidManifest.xml");
    }

    /**
     * 得到URL的连接
     *
     * @param context
     * @param urlStr
     * @param connectTimeOut
     * @param readTimeOut
     * @return
     */
    public static HttpURLConnection getURLConnection(Context context, String urlStr, int connectTimeOut, int readTimeOut) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlStr);
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo1 = connectivityManager.getNetworkInfo(0);
            NetworkInfo networkInfo2 = connectivityManager.getNetworkInfo(1);

            if ((networkInfo2 != null) && (networkInfo2.isAvailable())) {

                LogUtils.D("WIFI is available");
                httpURLConnection = (HttpURLConnection) url.openConnection();

            } else if ((networkInfo1 != null) && (networkInfo1.isAvailable())) {

                String str = networkInfo1.getExtraInfo();

                if (str != null)

                    str = str.toLowerCase();
                else {
                    str = "";
                }
                LogUtils.D("current APN:" + str);

                if ((str.startsWith("cmwap")) || (str.startsWith("uniwap")) || (str.startsWith("3gwap")))
                    httpURLConnection = (HttpURLConnection) url.openConnection(a);
                else if (str.startsWith("ctwap"))
                    httpURLConnection = (HttpURLConnection) url.openConnection(b);
                else
                    httpURLConnection = (HttpURLConnection) url.openConnection();
            } else {
                LogUtils.D("getConnection:not wifi and mobile");
                httpURLConnection = (HttpURLConnection) url.openConnection();
            }
            httpURLConnection.setConnectTimeout(connectTimeOut);
            httpURLConnection.setReadTimeout(readTimeOut);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpURLConnection;
    }


    public static HttpURLConnection getURLConnectionWithHttps(Context context, String urlStr, int connectTimeOut, int readTimeOut) throws NoSuchAlgorithmException, IOException,
            KeyManagementException {

        HttpURLConnection httpURLConnection = null;
        URL url = new URL(HTTPS_TEST_SEVER);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] {new TrustAllManager()}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
//                Logger.d("un verify!.......");
                return true;
            }
        });
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo1 = connectivityManager.getNetworkInfo(0);
        NetworkInfo networkInfo2 = connectivityManager.getNetworkInfo(1);

        if ((networkInfo2 != null) && (networkInfo2.isAvailable())) {
            LogUtils.D("WIFI is available");
            httpURLConnection = (HttpURLConnection) url.openConnection();

        } else if ((networkInfo1 != null) && (networkInfo1.isAvailable())) {
            String str = networkInfo1.getExtraInfo();
            if (str != null)
                str = str.toLowerCase();
            else {
                str = "";
            }
            LogUtils.D("current APN:" + str);
            if ((str.startsWith("cmwap")) || (str.startsWith("uniwap")) || (str.startsWith("3gwap")))
                httpURLConnection = (HttpURLConnection) url.openConnection(a);
            else if (str.startsWith("ctwap"))
                httpURLConnection = (HttpURLConnection) url.openConnection(b);
            else
                httpURLConnection = (HttpURLConnection) url.openConnection();
        } else {
            LogUtils.D("getConnection:not wifi and mobile");
            httpURLConnection = (HttpURLConnection) url.openConnection();
        }
        httpURLConnection.setConnectTimeout(connectTimeOut);
        httpURLConnection.setReadTimeout(readTimeOut);

        return httpURLConnection;
    }

    static class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
