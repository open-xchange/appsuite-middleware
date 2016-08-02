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

package com.openexchange.pns.transport.websocket.internal;

import java.util.List;
import com.openexchange.pns.PushSubscription;

/**
 * {@link Unsubscription} - An unsubscription from a Web Socket transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
final class Unsubscription {

    private final int userId;
    private final int contextId;
    private final String client;
    private final List<String> topics;
    private final PushSubscription subscription;
    private volatile Integer hash;

    /**
     * Initializes a new {@link Unsubscription}.
     *
     * @param subscription The subscription to unsubscribe
     */
    public Unsubscription(PushSubscription subscription) {
        super();
        this.subscription = subscription;
        this.userId = subscription.getUserId();
        this.contextId = subscription.getContextId();
        this.client = subscription.getClient();
        this.topics = subscription.getTopics();
    }

    /**
     * Gets the subscription
     *
     * @return The subscription
     */
    public PushSubscription getSubscription() {
        return subscription;
    }

    /**
     * Gets the client
     *
     * @return The client
     */
    public String getClient() {
        return client;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    @Override
    public int hashCode() {
        Integer tmp = hash;
        if (null == tmp) {
            int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            result = prime * result + ((client == null) ? 0 : client.hashCode());
            result = prime * result + ((topics == null) ? 0 : topics.hashCode());
            tmp = Integer.valueOf(result);
            hash = tmp;
        }
        return tmp.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Unsubscription)) {
            return false;
        }
        Unsubscription other = (Unsubscription) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (client == null) {
            if (other.client != null) {
                return false;
            }
        } else if (!client.equals(other.client)) {
            return false;
        }
        if (topics == null) {
            if (other.topics != null) {
                return false;
            }
        } else if (!topics.equals(other.topics)) {
            return false;
        }
        return true;
    }

}
