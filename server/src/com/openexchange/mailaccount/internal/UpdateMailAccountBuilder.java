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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
 * {@link UpdateMailAccountBuilder}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateMailAccountBuilder implements AttributeSwitch {

    private static final Set<Attribute> KNOWN_ATTRIBUTES = EnumSet.complementOf(EnumSet.of(
        Attribute.ID_LITERAL,
        Attribute.TRANSPORT_URL_LITERAL,
        Attribute.TRANSPORT_LOGIN_LITERAL,
        Attribute.TRANSPORT_PASSWORD_LITERAL));

    public static boolean needsUpdate(final Set<Attribute> attributes) {
        for (final Attribute attribute : attributes) {
            if (KNOWN_ATTRIBUTES.contains(attribute)) {
                return true;
            }
        }
        return false;
    }

    public boolean handles(final Attribute attribute) {
        return KNOWN_ATTRIBUTES.contains(attribute);
    }

    private final StringBuilder bob = new StringBuilder("UPDATE user_mail_account SET ");

    public String getUpdateQuery() {
        bob.setLength(bob.length() - 1);
        bob.append(" WHERE cid = ? AND id = ? AND user = ?");
        return bob.toString();
    }

    @Override
    public String toString() {
        return getUpdateQuery();
    }

    public Object confirmedHam() {
        bob.append("confirmed_ham = ?,");
        return null;
    }

    public Object confirmedSpam() {
        bob.append("confirmed_spam = ?,");
        return null;
    }

    public Object drafts() {
        bob.append("drafts = ?,");
        return null;
    }

    public Object id() {
        return null;
    }

    public Object login() {
        bob.append("login = ?,");
        return null;
    }

    public Object mailURL() {
        bob.append("url = ?,");
        return null;
    }

    public Object name() {
        bob.append("name = ?,");
        return null;
    }

    public Object password() {
        bob.append("password = ?,");
        return null;
    }

    public Object primaryAddress() {
        bob.append("primary_addr = ?,");
        return null;
    }

    public Object sent() {
        bob.append("sent = ?,");
        return null;
    }

    public Object spam() {
        bob.append("spam = ?,");
        return null;
    }

    public Object spamHandler() {
        bob.append("spam_handler = ?,");
        return null;
    }

    public Object transportURL() {
        return null;
    }

    public Object trash() {
        bob.append("trash = ?,");
        return null;
    }

    public Object mailPort() {
        return null;
    }

    public Object mailProtocol() {
        return null;
    }

    public Object mailSecure() {
        return null;
    }

    public Object mailServer() {
        return null;
    }

    public Object transportPort() {
        return null;
    }

    public Object transportProtocol() {
        return null;
    }

    public Object transportSecure() {
        return null;
    }

    public Object transportServer() {
        return null;
    }

    public Object transportLogin() {
        return null;
    }

    public Object transportPassword() {
        return null;
    }

    public Object unifiedINBOXEnabled() {
        bob.append("unified_inbox = ?,");
        return null;
    }

    public Object confirmedHamFullname() {
        bob.append("confirmed_ham_fullname = ?,");
        return null;
    }

    public Object confirmedSpamFullname() {
        bob.append("confirmed_spam_fullname = ?,");
        return null;
    }

    public Object draftsFullname() {
        bob.append("drafts_fullname = ?,");
        return null;
    }

    public Object sentFullname() {
        bob.append("sent_fullname = ?,");
        return null;
    }

    public Object spamFullname() {
        bob.append("spam_fullname = ?,");
        return null;
    }

    public Object trashFullname() {
        bob.append("trash_fullname = ?,");
        return null;
    }

    public Object pop3DeleteWriteThrough() {
        return null;
    }

    public Object pop3ExpungeOnQuit() {
        return null;
    }

    public Object pop3RefreshRate() {
        return null;
    }

    public Object pop3Path() {
        return null;
    }

    public Object pop3Storage() {
        return null;
    }

}
