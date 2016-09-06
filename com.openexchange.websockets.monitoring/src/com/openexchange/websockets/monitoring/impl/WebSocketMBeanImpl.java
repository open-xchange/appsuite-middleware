/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
