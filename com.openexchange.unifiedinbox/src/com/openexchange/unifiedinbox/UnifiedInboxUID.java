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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.unifiedinbox;

import static com.openexchange.mail.utils.MailPasswordUtil.decrypt;
import static com.openexchange.mail.utils.MailPasswordUtil.encrypt;
import java.security.GeneralSecurityException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;

/**
 * {@link UnifiedInboxUID} - The Unified INBOX UID.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxUID {

    private static final String KEY = "unifiedm";

    private int accountId;

    private String fullname;

    private String id;

    private String uidl;

    /**
     * Initializes an empty {@link UnifiedInboxUID}.
     */
    public UnifiedInboxUID() {
        super();
    }

    /**
     * Initializes a new {@link UnifiedInboxUID}.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     */
    public UnifiedInboxUID(final int accountId, final String fullname, final String id) {
        super();
        setUID(accountId, fullname, id);
    }

    /**
     * Initializes a new {@link UnifiedInboxUID}.
     *
     * @param unifiedINBOXUID The Unified INBOX UID as a string
     * @throws OXException If parsing Unified INBOX UID fails
     */
    public UnifiedInboxUID(final String unifiedINBOXUID) throws OXException {
        super();
        setUIDString(unifiedINBOXUID);
    }

    /**
     * Sets the UID of this {@link UnifiedInboxUID}.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @return This {@link UnifiedInboxUID} with new UID applied.
     */
    public UnifiedInboxUID setUID(final int accountId, final String fullname, final String id) {
        this.accountId = accountId;
        this.fullname = fullname;
        this.id = id;
        final String mailPath = MailPath.getMailPath(accountId, fullname, id);
        try {
            uidl = encrypt(mailPath, KEY);
            return this;
        } catch (final GeneralSecurityException e) {
            // Must not occur
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(UnifiedInboxUID.class)).warn("\n\tPBE (password-based-encryption) failed.\n", e);
            uidl = mailPath;
        }
        return this;
    }

    /**
     * Sets the UID of this {@link UnifiedInboxUID}.
     *
     * @param unifiedINBOXUID The Unified INBOX UID as a string
     * @throws MailException If parsing Unified INBOX UID fails
     * @return This {@link UnifiedInboxUID} with new UID applied.
     */
    public UnifiedInboxUID setUIDString(final String unifiedINBOXUID) throws OXException {
        String decoded;
        try {
            decoded = decrypt(unifiedINBOXUID, KEY);
        } catch (final GeneralSecurityException e) {
            decoded = unifiedINBOXUID;
        }
        final MailPath mailPath = new MailPath(decoded);
        accountId = mailPath.getAccountId();
        fullname = mailPath.getFolder();
        id = mailPath.getMailID();
        uidl = decoded;
        return this;
    }

    /**
     * Gets the account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the folder full name.
     *
     * @return The folder full name
     */
    public String getFullName() {
        return fullname;
    }

    /**
     * Gets the mail ID.
     *
     * @return The mail ID
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return uidl;
    }
}
