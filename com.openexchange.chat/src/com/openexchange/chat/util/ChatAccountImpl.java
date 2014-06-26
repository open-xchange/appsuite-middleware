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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.chat.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.chat.ChatAccount;
import com.openexchange.chat.ChatService;

/**
 * {@link ChatAccountImpl} - The basic {@link ChatAccount} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ChatAccountImpl implements ChatAccount {

    private final Map<String, Object> configuration;

    private String id;

    private String displayName;

    private ChatService chatService;

    /**
     * Initializes a new {@link ChatAccountImpl}.
     */
    public ChatAccountImpl() {
        super();
        id = ChatService.DEFAULT_ACCOUNT;
        displayName = id;
        configuration = new ConcurrentHashMap<String, Object>();
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public ChatService getChatService() {
        return chatService;
    }

    /**
     * Adds specified configuration property.
     *
     * @param name The property name
     * @param value The value; <code>null</code> means to remove associated value
     */
    public void addConfigProperty(final String name, final Object value) {
        if (null == name) {
            return;
        }
        if (null == value) {
            configuration.remove(name);
        } else {
            configuration.put(name, value);
        }
    }

    /**
     * Adds specified configuration properties.
     *
     * @param properties The properties to add
     */
    public void addConfigProperty(final Map<String, Object> properties) {
        if (null == properties) {
            return;
        }
        configuration.putAll(properties);
    }

    /**
     * Removes specified configuration property.
     *
     * @param name The property name
     */
    public void removeConfigProperty(final String name) {
        if (null == name) {
            return;
        }
        configuration.remove(name);
    }

    /**
     * Sets the id
     *
     * @param id The id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the display name
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the chat service
     *
     * @param chatService The chats ervice to set
     */
    public void setChatService(final ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("ChatAccountImpl {");
        if (configuration != null) {
            builder.append("configuration=").append(configuration).append(", ");
        }
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (displayName != null) {
            builder.append("displayName=").append(displayName).append(", ");
        }
        if (chatService != null) {
            builder.append("chatService=").append(chatService);
        }
        builder.append('}');
        return builder.toString();
    }

}
