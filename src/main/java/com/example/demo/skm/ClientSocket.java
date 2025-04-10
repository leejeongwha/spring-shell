package com.example.demo.skm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

@AllArgsConstructor
@Slf4j
public class ClientSocket {
    public static final String DISCOVER_VERSIONS_MESSAGE = "42007801000000604200770100000038420069010000002042006A0200000004000000010000000042006B0200000004000000010000000042000D0200000004000000010000000042000F010000001842005C05000000040000001E000000004200790100000000";

    public String send(String message, String host, int port) throws Exception {
        String result = "";

        // 1. 일반 소켓 생성
        Socket underlyingSocket = new Socket();
        try {
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
            try (DataInputStream dataInputStream = new DataInputStream(sslSocket.getInputStream());
                 DataOutputStream dataOutputStream = new DataOutputStream(sslSocket.getOutputStream());
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                dataOutputStream.write(Hex.decodeHex(message));

                byte[] header = new byte[8]; // 예시, 실제 KMIP에서는 보통 최소 8~24바이트의 헤더
                dataInputStream.readFully(header);
                // Length 필드 파싱 (예: 4~7번째 바이트에 길이 정보 있음)
                int length = ByteBuffer.wrap(header, 4, 4).getInt(); // 또는 ByteBuffer.order(ByteOrder.BIG_ENDIAN)
                System.out.println("Body Length: " + length);

                // 전체 메시지를 읽기
                byte[] body = new byte[length];
                dataInputStream.readFully(body);
                result = Hex.encodeHexString(header, false) + Hex.encodeHexString(body, false);

                throw new SocketTimeoutException("강제로 타임아웃 Exception 발생");
            }
        } catch (SocketTimeoutException e) {
            if (StringUtils.isEmpty(result)) {
                result = "Timeout Error: " + e.getMessage();
            }
        } catch (Exception e) {
            if (StringUtils.isEmpty(result)) {
                result = "Error: " + e.getMessage();
            }
        }

        return result;
    }
}
