package com.example.demo.skm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

@AllArgsConstructor
@Slf4j
public class ClientSocket {
    public static final String DISCOVER_VERSIONS_MESSAGE = "42007801000000604200770100000038420069010000002042006A0200000004000000010000000042006B0200000004000000010000000042000D0200000004000000010000000042000F010000001842005C05000000040000001E000000004200790100000000";

    public String send(String message, String host, int port) {
        String result = "";

        try {
            // 1. 일반 소켓 생성
            Socket underlyingSocket = new Socket();

            // 2. Connect timeout 설정 (예: 3초)
            int connectTimeout = 3000;
            SocketAddress endpoint = new InetSocketAddress(host, port);
            underlyingSocket.connect(endpoint, connectTimeout);

            // 3. SSL 업그레이드
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(underlyingSocket, host, port, true);

            // 4. Read timeout 설정 (예: 5초)
            sslSocket.setSoTimeout(5000);

            // 5. 입출력 스트림 설정 및 통신
            try (DataInputStream dataInputStream = new DataInputStream(sslSocket.getInputStream()); DataOutputStream dataOutputStream = new DataOutputStream(sslSocket.getOutputStream());) {
                dataOutputStream.write(Hex.decodeHex(message));
                byte[] bytes = dataInputStream.readAllBytes();
                result = Hex.encodeHexString(bytes, false);
            }
        } catch (SocketTimeoutException e) {
            result = "Timeout Error: " + e.getMessage();
        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }

        return result;
    }
}
