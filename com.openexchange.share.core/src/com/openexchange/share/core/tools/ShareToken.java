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

package com.openexchange.share.core.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.ShareExceptionCodes;

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
        baseToken = token.substring(16, 48);
        contextID = Integer.parseInt(token.substring(0, 8), 16) ^ getContextObfuscator(baseToken);
        userID = Integer.parseInt(token.substring(8, 16), 16) ^ getUserObfuscator(baseToken);
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
            contextID ^ getContextObfuscator(baseToken), userID ^ getUserObfuscator(baseToken), baseToken);
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
