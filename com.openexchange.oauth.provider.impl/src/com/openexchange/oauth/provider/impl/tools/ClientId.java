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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.oauth.provider.impl.tools;

import com.openexchange.exception.OXException;

/**
 * {@link ClientId} consisting of context group identifier and base token
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class ClientId {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ClientId.class);

    /**
     * Initializes a new {@link ClientId}, based on the supplied client token or returns <code>null</code> if the given client identifier is not valid.
     *
     * @param clientId The client identifier
     * @throws OXException
     */
    public static ClientId parse(String clientId) {
        try {
            String groupId = OAuthClientIdHelper.getInstance().getGroupIdFrom(clientId);
            String baseToken = OAuthClientIdHelper.getInstance().getBaseTokenFrom(clientId);
            return new ClientId(groupId, baseToken);
        } catch (OXException oxException) {
            LOG.debug("Given client identifier {} is not valid.", clientId, oxException);
            return null;
        }
    }

    /**
     * Initializes a new {@link ClientId} with the given group and client identifier
     *
     * @param groupId The context group identifier
     * @param baseToken The base token
     */
    public static ClientId generate(String groupId, String baseToken) {
        return new ClientId(groupId, baseToken);
    }

    // ---------------------------------------------------------------------------------------------------

    private final String baseToken;
    private final String groupId;

    /**
     * Initializes a new {@link ClientId}. Don't generate new base tokens on your own,
     * always use {@link ClientId#generate(String, String)}.
     *
     * @param groupId The group identifier
     * @param baseToken The base token
     */
    private ClientId(String groupId, String baseToken) {
        super();
        this.groupId = groupId;
        this.baseToken = baseToken;
    }

    /**
     * Gets the baseToken
     *
     * @return The baseToken
     */
    public String getBaseToken() {
        return baseToken;
    }

    /**
     * Gets the groupId
     *
     * @return The groupId
     */
    public String getGroupId() {
        return groupId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + baseToken.hashCode();
        result = prime * result + groupId.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClientId)) {
            return false;
        }
        ClientId other = (ClientId) obj;
        if (baseToken != other.baseToken) {
            return false;
        }
        if (groupId != other.groupId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClientId [baseToken=" + baseToken + ", groupId=" + groupId + "]";
    }
}
