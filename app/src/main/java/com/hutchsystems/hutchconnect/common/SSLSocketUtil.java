package com.hutchsystems.hutchconnect.common;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Deepak Sharma on 5/25/2018.
 * purpose: to get ssl context
 */

public class SSLSocketUtil {

    Context context;

    SSLSocketUtil(Context context) {
        this.context = context;
    }

    // Created By: Deepak Sharma
    // Created Date: 25 May 2018
    // Purpose: load key store for specific type of certificate present in asset folder
    public SSLContext getSSLContext(String certificateType, String certificate, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        // load key store instance
        KeyStore keyStore = KeyStore.getInstance(certificateType);

        // load certificate file into input stream
        InputStream inputStream = context.getAssets().open(certificate);

        // load certificate into key store
        keyStore.load(inputStream, password.toCharArray());

        // get Key Manager factory instance of type X509
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");

        // initialize key manager factory
        keyManagerFactory.init(keyStore, null);

        // load key manager from key manager factory
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        // get instance of ssl context
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // initialize trust manager factory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // load key store in trust manager factory
        trustManagerFactory.init(keyStore);

        // get trust manager from trust maanger factory
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        // initialize sslContext from key manager and trust manager
        sslContext.init(keyManagers, null, null);

        return sslContext;
    }

}
