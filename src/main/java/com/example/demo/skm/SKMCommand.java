package com.example.demo.skm;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;

@ShellComponent
public class SKMCommand {
    @Autowired
    private SkmConfig skmConfig;

    @ShellMethod(key = "getSecret", value = "기밀 데이터 조회")
    public String getSecret(@ShellOption(value = {"--env", "-e"}, help = "환경 (alpha, beta, real, ngsc, ninc, ncgn, ngsc-beta, gov-alpha, gov-beta, gov)", defaultValue = "real") String env,
                            @ShellOption(value = {"--appKey", "-a"}, help = "AppKey") String appKey,
                            @ShellOption(value = {"--keyId", "-k"}, help = "Key ID") String keyId,
                            @ShellOption(value = {"--mac", "-m"}, help = "Mac Address", defaultValue = "") String mac,
                            @ShellOption(value = {"--pwd", "-p"}, help = "인증서 Password", defaultValue = "") String password,
                            @ShellOption(value = {"--path", "-t"}, help = "인증서 경로", defaultValue = "") String path,
                            @ShellOption(value = {"--userkey", "-u"}, help = "User Access Key ID", defaultValue = "") String userAccessKey,
                            @ShellOption(value = {"--userSecret", "-s"}, help = "Secret Access Key", defaultValue = "") String userSecret) throws Exception {
        String domain = skmConfig.getDomains().get(env);
        String uri = "/keymanager/v1.0/appkey/" + appKey + "/secrets/" + keyId;
        if (StringUtils.isNotEmpty(userAccessKey) && StringUtils.isNotEmpty(userSecret)) {
            uri = "/keymanager/v1.2/appkey/" + appKey + "/secrets/" + keyId;
        }

        RestClient.Builder restClientBuilder = RestClient.builder()
            .baseUrl(domain);

        if (StringUtils.isNotEmpty(path) && StringUtils.isNotEmpty(password)) {
            restClientBuilder.requestFactory(createSslVerifyRequestFactory(path, password));
        }

        RestClient restClient = restClientBuilder.build();
        return restClient.get()
            .uri(uri)
            .headers(headers -> {
                headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                if (StringUtils.isNotEmpty(mac)) {
                    headers.set("X-TOAST-CLIENT-MAC-ADDR", mac);
                }
                if (StringUtils.isNotEmpty(userAccessKey)) {
                    headers.set("X-TC-AUTHENTICATION-ID", userAccessKey);
                }
                if (StringUtils.isNotEmpty(userSecret)) {
                    headers.set("X-TC-AUTHENTICATION-SECRET", userSecret);
                }
            })
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(String.class);
    }

    @ShellMethod(key = "getKmipVersion", value = "KMIP 버전 확인 메세지")
    public String getSecret(@ShellOption(value = {"--host", "-i"}, help = "호스트", defaultValue = "127.0.0.1") String host,
                            @ShellOption(value = {"--port", "-p"}, help = "포트", defaultValue = "5696") Integer port,
                            @ShellOption(value = {"--key-path", "-k"}, help = "포트") String keystorePath,
                            @ShellOption(value = {"--trust-path", "-t"}, help = "포트") String truststorePath) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", keystorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", "nhn!@#123");
        System.setProperty("javax.net.ssl.trustStore", truststorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", "nhn!@#123");

        String message = ClientSocket.DISCOVER_VERSIONS_MESSAGE;
        ClientSocket clientSocket = new ClientSocket();
        return clientSocket.send(message, host, port);
    }

    private ClientHttpRequestFactory createSslVerifyRequestFactory(String keyStorePath, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(keyStorePath), password.toCharArray());

        SSLContext sslContext = SSLContextBuilder
            .create()
            .loadKeyMaterial(keyStore, password.toCharArray())
            .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(
                SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .build()
            )
            .build();

        CloseableHttpClient httpClient = HttpClients
            .custom()
            .setConnectionManager(connectionManager)
            .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private String getPathFromTempFile(String fileName) throws Exception {
        InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream(fileName);
        File tempFile = File.createTempFile("client-keystore", ".jks");
        Files.copy(keystoreStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile.getAbsolutePath();
    }
}
