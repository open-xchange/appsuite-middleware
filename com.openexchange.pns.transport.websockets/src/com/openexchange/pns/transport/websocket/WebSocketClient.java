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

package com.openexchange.pns.transport.websocket;

/**
 * {@link WebSocketClient} - Provides the client identifier and the associated path filter expression to associate a Web Socket with that client.
 * <p>
 * {@link #equals(Object) equals()} method only considers client identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public final class WebSocketClient {

    private final String client;
    private final String pathFilter;
    private Integer hash;

    /**
     * Initializes a new {@link WebSocketClient}.
     *
     * @param client The identifier of the client; e.g. <code>"open-xchange-appsuite"</code>
     * @param pathFilter The path filter expression that applies to the client; e.g. <code>"/socket.io/*"</code>
     */
    public WebSocketClient(String client, String pathFilter) {
        super();
        this.client = client;
        this.pathFilter = pathFilter;
    }

    /**
     * Gets the client identifier; e.g. <code>"open-xchange-appsuite"</code>
     *
     * @return The client identifier
     */
    public String getClient() {
        return client;
    }

    /**
     * Gets the path filter expression that applies to the client; e.g. <code>"/socket.io/*"</code>
     *
     * @return The path filter expression
     */
    public String getPathFilter() {
        return pathFilter;
    }

    @Override
    public int hashCode() {
        Integer tmp = this.hash;
        if (null == tmp) {
            // No concurrency here. In worst case each thread computes its own hash code
            int prime = 31;
            int result = 1;
            result = prime * result + ((client == null) ? 0 : client.hashCode());
            tmp = Integer.valueOf(result);
            this.hash = tmp;
        }
        return tmp.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (WebSocketClient.class != obj.getClass()) {
            return false;
        }
        WebSocketClient other = (WebSocketClient) obj;
        if (client == null) {
            if (other.client != null) {
                return false;
            }
        } else if (!client.equals(other.client)) {
            return false;
        }
        return true;
    }

}
