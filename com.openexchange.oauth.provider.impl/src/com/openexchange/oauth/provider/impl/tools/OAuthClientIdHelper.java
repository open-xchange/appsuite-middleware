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
    private final String uuidRegex = "[0-9a-fA-F]{8}[0-9a-fA-F]{4}[34][0-9a-fA-F]{3}[89ab][0-9a-fA-F]{3}[0-9a-fA-F]{12}";

    /**
     * Returns the client id consisting of the encoded context group identifier and the base token
     *
     * @param groupId - the group id the client is assigned to
     * @return the generated client id
     * @throws OXException
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
     * @throws OXException
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
     * @throws OXException
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
     * @throws OXException
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
        return new String();
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
