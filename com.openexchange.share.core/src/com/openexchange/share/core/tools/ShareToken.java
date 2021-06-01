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

package com.openexchange.share.core.tools;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.user.User;

/**
 * {@link ShareToken}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareToken {

    private static final String SHARE_BASE_TOKEN_ATTRIBUTE = "com.openexchange.shareBaseToken";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-f0-9]{48}", Pattern.CASE_INSENSITIVE);

    private final int contextID;
    private final int userID;
    private final String baseToken;

    /**
     * Generates a new base token and sets it as user attribute in the supplied guest user.
     *
     * @param user The user to assign the base token for
     */
    public static void assignBaseToken(UserImpl user) {
        Map<String, String> existingAttributes = user.getAttributes();
        Map<String, String> attributes;
        if (null != existingAttributes) {
            attributes = new HashMap<String, String>(existingAttributes);
        } else {
            attributes = new HashMap<String, String>();
        }
        attributes.put(SHARE_BASE_TOKEN_ATTRIBUTE, UUIDs.getUnformattedString(UUID.randomUUID()));
        user.setAttributes(attributes);
    }

    /**
     * Initializes a new {@link ShareToken}, based on the supplied share token. Any appended path fragments are swallowed.
     *
     * @param token The token
     */
    public ShareToken(String token) throws OXException {
        super();
        if (null == token || 48 > token.length() || false == TOKEN_PATTERN.matcher(token.substring(0, 48)).matches()) {
            throw ShareExceptionCodes.INVALID_TOKEN.create(token);
        }
        try {
            baseToken = token.substring(16, 48);
            contextID = Integer.parseInt(token.substring(0, 8), 16) ^ getContextObfuscator(baseToken);
            userID = Integer.parseInt(token.substring(8, 16), 16) ^ getUserObfuscator(baseToken);
        } catch (NumberFormatException e) {
            throw ShareExceptionCodes.INVALID_TOKEN.create(token, e);
        }
    }

    /**
     * Initializes a new {@link ShareToken} for an exiting guest user.
     *
     * @param contextID The context ID
     * @param guestUser the guest user
     * @throws OXException If the guest users token is invalid
     * @see ShareExceptionCodes#INVALID_TOKEN
     */
    public ShareToken(int contextID, User guestUser) throws OXException {
        super();
        this.contextID = contextID;
        userID = guestUser.getId();
        baseToken = ShareTool.getUserAttribute(guestUser, SHARE_BASE_TOKEN_ATTRIBUTE);
        if (Strings.isEmpty(baseToken)) {
            throw ShareExceptionCodes.INVALID_TOKEN.create(baseToken);
        }
    }

    /**
     * Gets the context identifier.
     *
     * @return The context ID
     */
    public int getContextID() {
        return contextID;
    }

    /**
     * Gets the user identifier.
     *
     * @return The user ID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Gets the token.
     *
     * @return The token
     */
    public String getToken() {
        return String.format("%1$08x%2$08x%3$s",
            I(contextID ^ getContextObfuscator(baseToken)), I(userID ^ getUserObfuscator(baseToken)), baseToken);
    }

    /**
     * Verifies that this token belongs to the given guest user.
     *
     * @param contextID The context identifier of the guest user
     * @param guestUser The guest user
     * @throws OXException {@link ShareExceptionCodes#INVALID_TOKEN} if this token does not belong to the passed guest user.
     */
    public void verifyGuest(int contextID, User guestUser) throws OXException {
        if (!equals(new ShareToken(contextID, guestUser))) {
            throw ShareExceptionCodes.INVALID_TOKEN.create(getToken());
        }
    }

    private static int getContextObfuscator(String baseToken) {
        return Integer.parseInt(baseToken.substring(0, 7), 16);
    }

    private static int getUserObfuscator(String baseToken) {
        return Integer.parseInt(baseToken.substring(7, 14), 16);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseToken == null) ? 0 : baseToken.hashCode());
        result = prime * result + contextID;
        result = prime * result + userID;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ShareToken)) {
            return false;
        }
        ShareToken other = (ShareToken) obj;
        if (contextID != other.contextID) {
            return false;
        }
        if (userID != other.userID) {
            return false;
        }
        if (baseToken == null) {
            if (other.baseToken != null) {
                return false;
            }
        } else if (!baseToken.equals(other.baseToken)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ShareToken [contextID=" + contextID + ", userID=" + userID + ", baseToken=" + baseToken + "]";
    }

}
