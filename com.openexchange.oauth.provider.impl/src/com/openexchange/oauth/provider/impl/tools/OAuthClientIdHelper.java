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

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;
import com.google.common.io.BaseEncoding;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;

/**
 * {@link OAuthClientIdHelper}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class OAuthClientIdHelper {

    private static final OAuthClientIdHelper INSTANCE = new OAuthClientIdHelper();

    private static final String EMPTY_STRING = "";

    /**
     * Gets the {@link OAuthClientIdHelper instance}.
     *
     * @return The instance
     */
    public static OAuthClientIdHelper getInstance() {
        return INSTANCE;
    }

    protected static final String SEPERATOR = "/";

    private static final Pattern ENCODED_GID = Pattern.compile("[A-Za-z0-9-_]+");

    /*-
     * ----------------------------------------------------------------------------------
     * --------------------------------- MEMBER SECTION ---------------------------------
     * ----------------------------------------------------------------------------------
     */
    private final static String uuidRegex = "[0-9a-fA-F]{8}[0-9a-fA-F]{4}[34][0-9a-fA-F]{3}[89ab][0-9a-fA-F]{3}[0-9a-fA-F]{12}";

    /**
     * Returns the client id consisting of the encoded context group identifier and the base token
     *
     * @param groupId - the group id the client is assigned to
     * @return the generated client id
     */
    public String generateClientId(String groupId) {
        String encodedGroupId = encode(groupId);

        return encodedGroupId + SEPERATOR + UUIDs.getUnformattedString(UUID.randomUUID()) + UUIDs.getUnformattedString(UUID.randomUUID());
    }

    /**
     * Returns the decoded context group identifier that is encoded within the given client id
     *
     * @param clientId - the id assigned to the client containing encoded context group id and base token
     * @return the context group id the client is assigned to
     * @throws OXException See {@link #extractEncodedGroupId(String)}
     */
    public String getGroupIdFrom(String clientId) throws OXException {
        String groupId = extractEncodedGroupId(clientId);

        return decode(groupId);
    }

    /**
     * Returns the (unique) base token of the client
     *
     * @param clientId - the id assigned to the client containing encoded context group id and base token
     * @return the base token for that client
     * @throws OXException {@link OAuthProviderExceptionCodes#BAD_BASE_TOKEN_IN_CLIENT_ID}
     */
    public String getBaseTokenFrom(String clientId) throws OXException {
        int lastIndexOfSeperator = clientId.lastIndexOf(SEPERATOR);
        if (lastIndexOfSeperator == -1) {
            throw OAuthProviderExceptionCodes.BAD_BASE_TOKEN_IN_CLIENT_ID.create(SEPERATOR, clientId);
        }
        String baseToken = clientId.substring(lastIndexOfSeperator + 1, clientId.length());

        return baseToken;
    }

    /**
     * Extracts the encoded group identifier out of the complete client identifier
     *
     * @param clientId - client identifier to extract the encoded group id from
     * @return String with the encoded group id
     * @throws OXException {@link OXException OAuthProviderExceptionCodes#BAD_BASE_TOKEN_IN_CLIENT_ID} or
     *             {@link OAuthProviderExceptionCodes#BAD_CONTEXT_GROUP_IN_CLIENT_ID}
     */
    protected String extractEncodedGroupId(String clientId) throws OXException {
        int lastIndexOfSeperator = clientId.lastIndexOf(SEPERATOR);
        if (lastIndexOfSeperator == -1) {
            throw OAuthProviderExceptionCodes.BAD_BASE_TOKEN_IN_CLIENT_ID.create(SEPERATOR, clientId);
        }

        String groupId = clientId.substring(0, lastIndexOfSeperator);
        if (!ENCODED_GID.matcher(groupId).matches()) {
            throw OAuthProviderExceptionCodes.BAD_CONTEXT_GROUP_IN_CLIENT_ID.create(clientId);
        }

        String toEvaluate = clientId.substring(lastIndexOfSeperator + 1, clientId.length());
        int toEvaluateLength = toEvaluate.length();

        String firstUuid = toEvaluate.substring(0, toEvaluateLength / 2);
        String secondUuid = toEvaluate.substring((toEvaluateLength / 2), toEvaluateLength);

        if (firstUuid.matches(uuidRegex) && secondUuid.matches(uuidRegex)) {
            return groupId;
        }
        return EMPTY_STRING;
    }

    /**
     * Helper method to encode the given context group identifier.
     *
     * @param groupId - group id to encode
     * @return encoded context group
     */
    protected final String encode(final String groupId) {
        return BaseEncoding.base64Url().omitPadding().encode(groupId.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Helper method to decode the given context group identifier.
     *
     * @param groupId - group id to decode
     * @return decoded context group
     */
    protected final String decode(final String groupId) {
        return new String(BaseEncoding.base64Url().omitPadding().decode(groupId), StandardCharsets.UTF_8);
    }
}
