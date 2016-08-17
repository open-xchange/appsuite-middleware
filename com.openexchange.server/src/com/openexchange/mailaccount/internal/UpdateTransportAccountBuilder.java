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
        Attribute.TRANSPORT_STARTTLS_LITERAL);

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

    /**
     * Initializes a new {@link UpdateTransportAccountBuilder}.
     */
    public UpdateTransportAccountBuilder() {
        super();
        bob = new StringBuilder("UPDATE user_transport_account SET ");
        valid = false;
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
        return null;
    }

    @Override
    public Object transportPassword() {
        bob.append("password = ?,");
        valid = true;
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
    public Object rootFolder() {
        return null;
    }

}
