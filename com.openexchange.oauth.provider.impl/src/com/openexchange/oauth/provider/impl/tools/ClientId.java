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
