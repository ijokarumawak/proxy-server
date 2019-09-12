package com.rumawaks.proxy;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.ProxyAuthenticator;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ThreadPoolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

public class ProxyServer {
    private static HttpProxyServer proxyServer;
    private static HttpProxyServer proxyServerWithAuth;

    private static void startProxyServer() throws IOException {
        int proxyServerPort;
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            proxyServerPort = serverSocket.getLocalPort();
        }
        proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(proxyServerPort)
                .withAllowLocalOnly(true)
                // Use less threads to mitigate Gateway Timeout (504) with proxy test
                .withThreadPoolConfiguration(new ThreadPoolConfiguration()
                        .withAcceptorThreads(2)
                        .withClientToProxyWorkerThreads(4)
                        .withProxyToServerWorkerThreads(4))
                .start();
    }

    private static final String PROXY_USER = "proxy user";
    private static final String PROXY_PASSWORD = "proxy password";
    private static void startProxyServerWithAuth() throws IOException {
        int proxyServerPort;
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            proxyServerPort = serverSocket.getLocalPort();
        }
        proxyServerWithAuth = DefaultHttpProxyServer.bootstrap()
                .withPort(proxyServerPort)
                .withAllowLocalOnly(true)
                .withProxyAuthenticator(new ProxyAuthenticator() {
                    @Override
                    public boolean authenticate(String userName, String password) {
                        return PROXY_USER.equals(userName) && PROXY_PASSWORD.equals(password);
                    }

                    @Override
                    public String getRealm() {
                        return "NiFi Unit Test";
                    }
                })
                // Use less threads to mitigate Gateway Timeout (504) with proxy test
                .withThreadPoolConfiguration(new ThreadPoolConfiguration()
                        .withAcceptorThreads(2)
                        .withClientToProxyWorkerThreads(4)
                        .withProxyToServerWorkerThreads(4))
                .start();
    }

    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    public static void main(String[] args) throws Exception {
        startProxyServer();
        startProxyServerWithAuth();

        logger.info("proxyServer is running at {}", proxyServer.getListenAddress());
        logger.info("proxyServerWithAuth is running at {}", proxyServerWithAuth.getListenAddress());
    }
}
