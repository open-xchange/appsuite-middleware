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

package com.openexchange.mailaccount.internal;

import java.util.EnumSet;
import java.util.Set;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.AttributeSwitch;

/**
 * {@link UpdateTransportAccountBuilder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateTransportAccountBuilder implements AttributeSwitch {

    private static final Set<Attribute> KNOWN_ATTRIBUTES = EnumSet.of(
        Attribute.NAME_LITERAL,
        Attribute.TRANSPORT_LOGIN_LITERAL,
        Attribute.TRANSPORT_PASSWORD_LITERAL,
        Attribute.TRANSPORT_URL_LITERAL,
        Attribute.PRIMARY_ADDRESS_LITERAL,
        Attribute.PERSONAL_LITERAL,
        Attribute.REPLY_TO_LITERAL,
        Attribute.TRANSPORT_STARTTLS_LITERAL,
        Attribute.TRANSPORT_OAUTH_LITERAL,
        Attribute.TRANSPORT_DISABLED);

    private static final Set<Attribute> PROPERTY_ATTRIBUTES = EnumSet.of(
        Attribute.POP3_DELETE_WRITE_THROUGH_LITERAL,
        Attribute.POP3_EXPUNGE_ON_QUIT_LITERAL,
        Attribute.POP3_PATH_LITERAL,
        Attribute.POP3_REFRESH_RATE_LITERAL,
        Attribute.POP3_STORAGE_LITERAL,
        Attribute.TRANSPORT_AUTH_LITERAL);

    public static boolean needsUpdate(final Set<Attribute> attributes) {
        for (final Attribute attribute : attributes) {
            if (KNOWN_ATTRIBUTES.contains(attribute)) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------

    private final StringBuilder bob;
    private boolean valid;
    private boolean injectClearingFailAuthCount;
    private boolean injectClearingDisabled;

    /**
     * Initializes a new {@link UpdateTransportAccountBuilder}.
     */
    public UpdateTransportAccountBuilder(boolean clearFailAuthCount) {
        super();
        bob = new StringBuilder("UPDATE user_transport_account SET ");
        valid = clearFailAuthCount;
        injectClearingFailAuthCount = clearFailAuthCount;
        injectClearingDisabled = clearFailAuthCount;
    }

    /**
     * Checks if this SQL builder has valid content
     *
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Checks if this SQL builder handles given attribute.
     *
     * @param attribute The attribute to check
     * @return <code>true</code> if able to handle; otherwise <code>false</code>
     */
    public boolean handles(final Attribute attribute) {
        return KNOWN_ATTRIBUTES.contains(attribute) && !PROPERTY_ATTRIBUTES.contains(attribute);
    }

    /**
     * Gets the prepared SQL statement.
     *
     * @return The prepared SQL statement
     * @see #isValid()
     */
    public String getUpdateQuery() {
        if (injectClearingFailAuthCount) {
            bob.append("failed_auth_count=0,failed_auth_date=0,");
        }
        if (injectClearingDisabled) {
            bob.append("disabled=0,");
        }
        bob.setLength(bob.length() - 1);
        bob.append(" WHERE cid = ? AND id = ? and user = ?");
        return bob.toString();
    }

    @Override
    public String toString() {
        return bob.toString();
    }

    @Override
    public Object name() {
        bob.append("name = ?,");
        valid = true;
        return null;
    }

    @Override
    public Object login() {
        return null;
    }

    @Override
    public Object password() {
        return null;
    }

    @Override
    public Object transportURL() {
        bob.append("url = ?,");
        valid = true;
        injectClearingFailAuthCount = true;
        injectClearingDisabled = true;
        return null;
    }

    @Override
    public Object primaryAddress() {
        bob.append("send_addr = ?,");
        valid = true;
        return null;
    }

    @Override
    public Object personal() {
        bob.append("personal = ?,");
        valid = true;
        return null;
    }

    @Override
    public Object replyTo() {
        bob.append("replyTo = ?,");
        valid = true;
        return null;
    }

    @Override
    public Object confirmedHam() {
        return null;
    }

    @Override
    public Object confirmedSpam() {
        return null;
    }

    @Override
    public Object drafts() {
        return null;
    }

    @Override
    public Object id() {
        return null;
    }

    @Override
    public Object mailURL() {
        return null;
    }

    @Override
    public Object sent() {
        return null;
    }

    @Override
    public Object spam() {
        return null;
    }

    @Override
    public Object spamHandler() {
        return null;
    }

    @Override
    public Object trash() {
        return null;
    }

    @Override
    public Object archive() {
        return null;
    }

    @Override
    public Object mailPort() {
        return null;
    }

    @Override
    public Object mailProtocol() {
        return null;
    }

    @Override
    public Object mailSecure() {
        return null;
    }

    @Override
    public Object mailServer() {
        return null;
    }

    @Override
    public Object transportPort() {
        return null;
    }

    @Override
    public Object transportProtocol() {
        return null;
    }

    @Override
    public Object transportSecure() {
        return null;
    }

    @Override
    public Object transportServer() {
        return null;
    }

    @Override
    public Object transportLogin() {
        bob.append("login = ?,");
        valid = true;
        injectClearingFailAuthCount = true;
        injectClearingDisabled = true;
        return null;
    }

    @Override
    public Object transportPassword() {
        bob.append("password = ?,");
        valid = true;
        injectClearingFailAuthCount = true;
        injectClearingDisabled = true;
        return null;
    }

    @Override
    public Object unifiedINBOXEnabled() {
        return null;
    }

    @Override
    public Object confirmedHamFullname() {
        return null;
    }

    @Override
    public Object confirmedSpamFullname() {
        return null;
    }

    @Override
    public Object draftsFullname() {
        return null;
    }

    @Override
    public Object sentFullname() {
        return null;
    }

    @Override
    public Object spamFullname() {
        return null;
    }

    @Override
    public Object trashFullname() {
        return null;
    }

    @Override
    public Object archiveFullname() {
        return null;
    }

    @Override
    public Object pop3DeleteWriteThrough() {
        return null;
    }

    @Override
    public Object pop3ExpungeOnQuit() {
        return null;
    }

    @Override
    public Object pop3RefreshRate() {
        return null;
    }

    @Override
    public Object pop3Path() {
        return null;
    }

    @Override
    public Object pop3Storage() {
        return null;
    }

    @Override
    public Object addresses() {
        return null;
    }

    @Override
    public Object transportAuth() {
        return null;
    }

    @Override
    public Object mailStartTls() {
        return null;
    }

    @Override
    public Object transportStartTls() {
        bob.append("starttls = ?,");
        valid = true;
        return null;
    }

    @Override
    public Object mailOAuth() {
        return null;
    }

    @Override
    public Object transportOAuth() {
        bob.append("oauth = ?,");
        valid = true;
        injectClearingFailAuthCount = true;
        injectClearingDisabled = true;
        return null;
    }

    @Override
    public Object rootFolder() {
        return null;
    }

    @Override
    public Object mailDisabled() {
        return null;
    }

    @Override
    public Object transportDisabled() {
        bob.append("disabled = ?,");
        injectClearingDisabled = false;
        valid = true;
        return null;
    }

}
