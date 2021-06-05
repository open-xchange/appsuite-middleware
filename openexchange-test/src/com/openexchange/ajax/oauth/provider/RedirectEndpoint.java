/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.oauth.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicRequestLine;

/**
 * {@link RedirectEndpoint}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class RedirectEndpoint extends Thread {

    private static final Pattern REQUEST_LINE = Pattern.compile("^([A-Z]+)\\s([^\\s]+)\\sHTTP/([0-9])\\.([0-9])$");

    private final Queue<HttpRequest> requests = new LinkedList<>();

    private volatile boolean started;

    private ServerSocket ss;

    private RedirectEndpoint() {
        super();
    }

    public static RedirectEndpoint create() {
        RedirectEndpoint endpoint = new RedirectEndpoint();
        endpoint.start();
        return endpoint;
    }

    public void shutdown() throws IOException, InterruptedException {
        started = false;
        ss.close();
        join();
    }

    /**
     * Returns the last received request.
     *
     * @return The request or <code>null</code> if the request queue is empty
     */
    public HttpRequest pollRequest() {
        return requests.poll();
    }

    public String getLocation() {
        return "http://localhost:8080";
    }

    @Override
    public void run() {
        started = true;
        try {
            ss = new ServerSocket(8080);
            System.out.println("Server up and running...");
            while (started && !isInterrupted()) {
                Socket client = ss.accept();
                InputStream inputStream = client.getInputStream();
                OutputStream outputStream = client.getOutputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "ASCII"));
                String line = br.readLine();
                if (line == null) {
                    sendBadRequest(outputStream);
                    continue;
                }

                Matcher rlMatcher = REQUEST_LINE.matcher(line);
                if (!rlMatcher.matches()) {
                    sendBadRequest(outputStream);
                    continue;
                }

                RequestLine requestLine = new BasicRequestLine(rlMatcher.group(1), rlMatcher.group(2), new ProtocolVersion("HTTP", Integer.parseInt(rlMatcher.group(3)), Integer.parseInt(rlMatcher.group(4))));
                Map<String, String> headers = new HashMap<>();
                String contentTypeHeader = null;
                while (!"".equals(line)) {
                    int delimIdx = line.indexOf(':');
                    if (delimIdx > 0 && line.length() > delimIdx) {
                        String headerName = line.substring(0, delimIdx).trim();
                        String headerValue = line.substring(delimIdx + 1).trim();
                        headers.put(headerName, headerValue);

                        if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                            contentTypeHeader = headerValue;
                        }
                    }

                    line = br.readLine();
                }

                BasicHttpRequest request;
                byte[] contentBytes = org.apache.commons.io.IOUtils.toByteArray(inputStream);
                if (contentBytes.length > 0) {
                    ContentType contentType;
                    if (contentTypeHeader == null) {
                        contentType = ContentType.APPLICATION_OCTET_STREAM;
                    } else {
                        contentType = ContentType.parse(contentTypeHeader);
                    }

                    BasicHttpEntityEnclosingRequest entityRequest = new BasicHttpEntityEnclosingRequest(requestLine);
                    entityRequest.setEntity(new ByteArrayEntity(contentBytes, contentType));
                    request = entityRequest;
                } else {
                    request = new BasicHttpRequest(requestLine);
                }

                for (Entry<String, String> header : headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }

                requests.add(request);
                sendOK(outputStream);
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Shutting down server...");
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBadRequest(OutputStream outputStream) throws UnsupportedEncodingException {
        sendEmptyReponse("HTTP/1.1 400 Bad Request", outputStream);
    }

    private void sendOK(OutputStream outputStream) throws UnsupportedEncodingException {
        sendEmptyReponse("HTTP/1.1 200 OK", outputStream);
    }

    private void sendEmptyReponse(String statusLine, OutputStream outputStream) throws UnsupportedEncodingException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        PrintWriter headers = new PrintWriter(new OutputStreamWriter(outputStream, "ASCII"));
        headers.print(statusLine);
        headers.print("\r\n");
        headers.print("Date: " + dateFormat.format(new Date()));
        headers.print("\r\n");
        headers.print("Server: localhost");
        headers.print("\r\n");
        headers.print("Content-Length: 0");
        headers.print("\r\n");
        headers.print("\r\n");
        headers.flush();
    }

}
