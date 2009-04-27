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

package com.openexchange.unifiedinbox;

import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.utils.MailFolderUtility;

/**
 * {@link UnifiedINBOXUID} - The Unified INBOX UID.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXUID {

    private int accountId;

    private String fullname;

    private String id;

    /**
     * Initializes an empty {@link UnifiedINBOXUID}.
     */
    public UnifiedINBOXUID() {
        super();
    }

    /**
     * Initializes a new {@link UnifiedINBOXUID}.
     * 
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     */
    public UnifiedINBOXUID(final int accountId, final String fullname, final String id) {
        super();
        setUID(accountId, fullname, id);
    }

    /**
     * Initializes a new {@link UnifiedINBOXUID}.
     * 
     * @param unifiedINBOXUID The Unified INBOX UID as a string
     * @throws MailException If parsing Unified INBOX UID fails
     */
    public UnifiedINBOXUID(final String unifiedINBOXUID) throws MailException {
        super();
        setUIDString(unifiedINBOXUID);
    }

    /**
     * Sets the UID of this {@link UnifiedINBOXUID}.
     * 
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @return This {@link UnifiedINBOXUID} with new UID applied.
     */
    public UnifiedINBOXUID setUID(final int accountId, final String fullname, final String id) {
        this.accountId = accountId;
        this.fullname = fullname;
        this.id = id;
        return this;
    }

    /**
     * Sets the UID of this {@link UnifiedINBOXUID}.
     * 
     * @param unifiedINBOXUID The Unified INBOX UID as a string
     * @throws MailException If parsing Unified INBOX UID fails
     * @return This {@link UnifiedINBOXUID} with new UID applied.
     */
    public UnifiedINBOXUID setUIDString(final String unifiedINBOXUID) throws MailException {
        final MailPath mailPath = new MailPath(unifiedINBOXUID);
        final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(mailPath.getFolder());
        this.accountId = fa.getAccountId();
        this.fullname = fa.getFullname();
        this.id = mailPath.getUid();
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
     * Gets the folder fullname.
     * 
     * @return The folder fullname
     */
    public String getFullname() {
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
        return MailPath.getMailPath(MailFolderUtility.prepareFullname(accountId, fullname), id).toString();
    }
}
