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

package com.openexchange.websockets.monitoring.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import com.openexchange.java.Strings;
import com.openexchange.management.AnnotatedStandardMBean;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketInfo;
import com.openexchange.websockets.WebSocketService;
import com.openexchange.websockets.monitoring.WebSocketMBean;


/**
 * {@link WebSocketMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketMBeanImpl extends AnnotatedStandardMBean implements WebSocketMBean {

    private final WebSocketService webSocketService;

    /**
     * Initializes a new {@link WebSocketMBeanImpl}.
     */
    public WebSocketMBeanImpl(WebSocketService webSocketService) throws NotCompliantMBeanException {
        super("Management Bean for Web Sockets", WebSocketMBean.class);
        this.webSocketService = webSocketService;
    }

    @Override
    public long getNumberOfWebSockets() throws MBeanException {
        try {
            return webSocketService.getNumberOfWebSockets();
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public long getNumberOfBufferedMessages() throws MBeanException {
        try {
            return webSocketService.getNumberOfBufferedMessages();
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public List<List<String>> listClusterWebSocketInfo() throws MBeanException {
        try {
            List<WebSocketInfo> infos = webSocketService.listClusterWebSocketInfo();
            Collections.sort(infos);

            List<List<String>> list = new ArrayList<List<String>>(infos.size());
            for (WebSocketInfo info : infos) {
                if (null != info) {
                    String path = info.getPath();
                    list.add(Arrays.asList(Integer.toString(info.getContextId()), Integer.toString(info.getUserId()), info.getAddress(), null == path ? "<none>" : path, info.getConnectionId().getId()));
                }
            }

            return list;
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public List<List<String>> listWebSockets() throws MBeanException {
        try {
            List<WebSocketInfo> infos;
            {
                List<WebSocket> webSockets = webSocketService.listLocalWebSockets();
                if (webSockets.isEmpty()) {
                    return Collections.emptyList();
                }

                infos = new ArrayList<>(webSockets.size());
                String address = "localhost";
                for (WebSocket webSocket : webSockets) {
                    WebSocketInfo info = WebSocketInfo.builder()
                        .address(address)
                        .connectionId(webSocket.getConnectionId())
                        .contextId(webSocket.getContextId())
                        .path(webSocket.getPath())
                        .userId(webSocket.getUserId())
                        .build();
                    infos.add(info);
                }
                Collections.sort(infos);
            }

            List<List<String>> list = new ArrayList<List<String>>(infos.size());
            for (WebSocketInfo info : infos) {
                if (null != info) {
                    String path = info.getPath();
                    list.add(Arrays.asList(Integer.toString(info.getContextId()), Integer.toString(info.getUserId()), null == path ? "<none>" : path, info.getConnectionId().getId()));
                }
            }

            return list;
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void closeWebSockets(int userId, int contextId, String pathFilter) throws MBeanException {
        try {
            webSocketService.closeWebSockets(userId, contextId, Strings.isEmpty(pathFilter) ? null : pathFilter);
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

}
