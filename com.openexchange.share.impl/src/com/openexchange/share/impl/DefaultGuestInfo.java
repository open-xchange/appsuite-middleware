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

package com.openexchange.share.impl;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.Date;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.user.User;

/**
 * {@link DefaultGuestInfo}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DefaultGuestInfo implements GuestInfo {

    private final ServiceLookup services;
    private final User guestUser;
    private final int contextID;
    private final String token;
    private final ShareTarget linkTarget;

    /**
     * Initializes a new {@link DefaultGuestInfo}.
     *
     * @param services A service lookup reference
     * @param contextID The identifier of the context this guest user belongs to
     * @param linkTarget The link target if this guest is anonymous; otherwise <code>null</code>
     * @param guestUser The guest user
     * @throws OXException If the guest users token is invalid
     */
    public DefaultGuestInfo(ServiceLookup services, int contextID, User guestUser, ShareTarget linkTarget) throws OXException {
        this(services, guestUser, new ShareToken(contextID, guestUser), linkTarget);
    }

    /**
     * Initializes a new {@link DefaultGuestInfo}.
     *
     * @param services A service lookup reference
     * @param guestUser The guest user
     * @param shareToken The share token
     * @param linkTarget The link target if this guest is anonymous; otherwise <code>null</code>
     */
    public DefaultGuestInfo(ServiceLookup services, User guestUser, ShareToken shareToken, ShareTarget linkTarget) {
        super();
        this.services = services;
        this.guestUser = guestUser;
        this.contextID = shareToken.getContextID();
        this.token = shareToken.getToken();
        this.linkTarget = linkTarget;
    }

    /**
     * Gets the guest user.
     *
     * @return The guest user
     */
    public User getUser() {
        return guestUser;
    }

    @Override
    public AuthenticationMode getAuthentication() {
        return ShareTool.getAuthenticationMode(guestUser);
    }

    @Override
    public String getBaseToken() {
        return token;
    }

    @Override
    public String getEmailAddress() {
        if (RecipientType.GUEST == getRecipientType()) {
            return guestUser.getMail();
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return guestUser.getDisplayName();
    }

    @Override
    public String getPassword() {
        if (AuthenticationMode.ANONYMOUS_PASSWORD == getAuthentication()) {
            String cryptedPassword = guestUser.getUserPassword();
            if (Strings.isNotEmpty(cryptedPassword)) {
                try {
                    return services.getService(PasswordMechRegistry.class).get(guestUser.getPasswordMech()).decode(cryptedPassword, guestUser.getSalt());
                } catch (OXException e) {
                    getLogger(DefaultGuestInfo.class).error("Error decrypting password '{}' for guest user {} in context {}",
                        cryptedPassword, Integer.valueOf(getGuestID()), Integer.valueOf(contextID), e);
                    return cryptedPassword;
                }
            }
        }
        return null;
    }

    @Override
    public Date getExpiryDate() {
        if (RecipientType.ANONYMOUS.equals(getRecipientType())) {
            String expiryDateValue = ShareTool.getUserAttribute(guestUser, ShareTool.EXPIRY_DATE_USER_ATTRIBUTE);
            if (Strings.isNotEmpty(expiryDateValue)) {
                try {
                    return new Date(Long.parseLong(expiryDateValue));
                } catch (NumberFormatException e) {
                    getLogger(DefaultGuestInfo.class).warn("Invalid value for {}: {}", ShareTool.EXPIRY_DATE_USER_ATTRIBUTE, expiryDateValue, e);
                }
            }
        }
        return null;
    }

    @Override
    public RecipientType getRecipientType() {
        switch (getAuthentication()) {
        case ANONYMOUS:
        case ANONYMOUS_PASSWORD:
            return RecipientType.ANONYMOUS;
        case GUEST:
        case GUEST_PASSWORD:
            return RecipientType.GUEST;
        default:
            throw new UnsupportedOperationException("Unknown authentication mode: " + getAuthentication());
        }
    }

    @Override
    public int getGuestID() {
        return guestUser.getId();
    }

    @Override
    public int getContextID() {
        return contextID;
    }

    @Override
    public int getCreatedBy() {
        return guestUser.getCreatedBy();
    }

    @Override
    public Locale getLocale() {
        return guestUser.getLocale();
    }

    @Override
    public ShareTarget getLinkTarget() {
        return linkTarget;
    }

    @Override
    public String generateLink(HostData hostData, ShareTargetPath targetPath) {
        return ShareLinks.generateExternal(hostData, getBaseToken(), targetPath);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefaultGuestInfo)) {
            return false;
        }
        DefaultGuestInfo other = (DefaultGuestInfo) obj;
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DefaultGuestInfo [guestID=" + getGuestID() + ", baseToken=" + getBaseToken() + ", eMailAddress=" + getEmailAddress()
            + ", contextID=" + contextID + "]";
    }

}
