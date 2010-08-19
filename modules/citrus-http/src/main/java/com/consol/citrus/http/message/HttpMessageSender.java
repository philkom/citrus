/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.http.message;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.util.HttpConstants;
import com.consol.citrus.http.util.HttpUtils;
import com.consol.citrus.message.*;
import com.consol.citrus.util.MessageUtils;

/**
 * Message sender implementation sending messages over Http.
 * 
 * Note: Message sender is only using POST request method to publish
 * messages to the service endpoint.
 * 
 * @author Christoph Deppisch
 */
public class HttpMessageSender implements MessageSender {
    /** Http url as service destination */
    private String requestUrl;

    /** Request method */
    private String requestMethod = HttpConstants.HTTP_POST;

    /** Http socket */
    private Socket socket;
    
    /** The reply message handler */
    private ReplyMessageHandler replyMessageHandler;
    
    /** The reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpMessageSender.class);
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.core.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        Writer writer = null;
        BufferedReader reader = null;

        try {
            log.info("Sending message to: " + getRequestUrl());

            if (log.isDebugEnabled()) {
                log.debug("Message to be sent:");
                log.debug(message.getPayload().toString());
            }

            Map<String, Object> requestHeaders = new HashMap<String, Object>();
            
            requestHeaders.put("HTTPVersion", HttpConstants.HTTP_VERSION);
            requestHeaders.put("HTTPMethod", requestMethod);
            requestHeaders.put("HTTPUri", getUri());
            requestHeaders.put("HTTPHost", getHost());
            requestHeaders.put("HTTPPort", getPort());

            /* before sending set header values */
            for (Entry<String, Object> headerEntry : message.getHeaders().entrySet()) {
                final String key = headerEntry.getKey();
                
                if(MessageUtils.isSpringInternalHeader(key)) {
                    continue;
                }
                
                final String value = (String) headerEntry.getValue();

                if (log.isDebugEnabled()) {
                    log.debug("Setting message property: " + key + " to: " + value);
                }

                requestHeaders.put(key, value);
            }

            Message<?> request;
            if (requestMethod.equals(HttpConstants.HTTP_POST)) {
                request = MessageBuilder.withPayload(message.getPayload()).copyHeaders(requestHeaders).build();
            } else if (requestMethod.equals(HttpConstants.HTTP_GET)) {
                //TODO: implement GET method
                request = MessageBuilder.withPayload("").build();
            } else {
                throw new CitrusRuntimeException("Unsupported request method: " + requestMethod);
            }

            InetAddress addr = InetAddress.getByName(getHost());
            socket = new Socket(addr, Integer.valueOf(getPort()));

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8"));
            writer.write(HttpUtils.generateRequest(request));
            writer.flush();

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append(HttpConstants.LINE_BREAK);
            }

            if(replyMessageHandler != null) {
                if(correlator != null) {
                    replyMessageHandler.onReplyMessage(MessageBuilder.withPayload(buffer.toString()).build(),
                            correlator.getCorrelationKey(message));
                } else {
                    replyMessageHandler.onReplyMessage(MessageBuilder.withPayload(buffer.toString()).build());
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error("Error while closing OutputStream", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing InputStream", e);
                }
            }
        }
    }

    /**
     * Get the port of the destination endpoint.
     * @return
     */
    private String getPort() {
        Assert.isTrue(StringUtils.hasText(requestUrl),
                        "You must specify a requestUrl (e.g. http://localhost:8080/test");
        
        String port = requestUrl.substring("http://".length());
        
        if(port.contains(":")) {
            port = port.substring(port.indexOf(':')+1);
            
            if(port.contains("/")) {
                port = port.substring(0, port.indexOf('/'));
            }
            
            return port;
        } else {
            return "8080";
        }
    }

    /**
     * Get the host of the destination endpoint.
     * @return
     */
    private String getHost() {
        Assert.isTrue(StringUtils.hasText(requestUrl),
                        "You must specify a requestUrl (e.g. http://localhost:8080/test");
        
        String host = requestUrl.substring("http://".length());
        if(host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        } else {
            host = host.substring(0, host.indexOf('/'));
        }
        
        return host;
    }

    /**
     * Get the request URI.
     * @return
     */
    private String getUri() {
        Assert.isTrue(StringUtils.hasText(requestUrl),
                        "You must specify a requestUrl (e.g. http://localhost:8080/test");
        
        String uri = requestUrl.substring("http://".length());
        
        if(uri.contains("/")) {
            uri = uri.substring(uri.indexOf("/"));
            return uri;
        } else {
            return "";
        }
    }

    /**
     * Get the complete request URL.
     * @return the urlPath
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Set the complete request URL.
     * @param url the url to set
     */
    public void setRequestUrl(String url) {
        this.requestUrl = url;
    }

    /**
     * Get the socket.
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Set the socket.
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Set the request method.
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
}
