package com.example.demo.skm;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;

@ShellComponent
public class SKMCommand {
    @Value("${skm.base-url}")
    private String skmBaseUrl;

    @ShellMethod(key = "getSecret", value = "Get the secret with appKey and keyId")
    public String getSecret(@ShellOption(help = "Application Key") String appKey,
                            @ShellOption(help = "Key ID") String keyId,
                            @ShellOption(help = "Mac Address", defaultValue = "") String mac,
                            @ShellOption(help = "keyStorePassword", defaultValue = "") String password,
                            @ShellOption(help = "keyStorePath", defaultValue = "") String keyStorePath) throws Exception {
        RestClient.Builder restClientBuilder = RestClient.builder()
            .baseUrl(skmBaseUrl);
        if (StringUtils.isNotEmpty(keyStorePath) && StringUtils.isNotEmpty(password)) {
            restClientBuilder.requestFactory(createSslVerifyRequestFactory(keyStorePath, password));
        }
        RestClient restClient = restClientBuilder.build();

        return restClient.get()
            .uri("/keymanager/v1.0/appkey/" + appKey + "/secrets/" + keyId)
            .headers(headers -> {
                headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                if (StringUtils.isNotEmpty(mac)) {
                    headers.set("X-TOAST-CLIENT-MAC-ADDR", mac);
                }
            })
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(String.class);
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
}
