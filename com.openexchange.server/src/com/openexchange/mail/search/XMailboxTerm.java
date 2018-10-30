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

package com.openexchange.mail.search;

import java.util.Collection;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.utils.MimeStorageUtility;

/**
 * {@link XMailboxTerm}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public final class XMailboxTerm extends SearchTerm<String> {

    private static final long serialVersionUID = -176183596923840685L;

    private final String fullName;

    /**
     * Initializes a new {@link XMailboxTerm}
     *
     * @param fullName The mailbox's full name to search for
     */
    public XMailboxTerm(final String fullName) {
        super();
        this.fullName = fullName;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getPattern() {
        return fullName;
    }

    @Override
    public void addMailField(final Collection<MailField> col) {
        col.add(MailField.ORIGINAL_FOLDER_ID);
    }

    @Override
    public boolean matches(final MailMessage mailMessage) {
        String fullName = mailMessage.getFolder();
        return null != fullName && fullName.regionMatches(true, 0, this.fullName, 0, this.fullName.length());
    }

    @Override
    public boolean matches(final Message msg) throws OXException {
        try {
            Folder folder = msg.getFolder();
            if (null == folder) {
                return false;
            }

            String fullName = folder.getFullName();
            return null != fullName && fullName.regionMatches(true, 0, this.fullName, 0, this.fullName.length());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(XMailboxTerm.class).warn("Error during search.", e);
            return false;
        }
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new javax.mail.search.XMailboxTerm(fullName);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return new javax.mail.search.XMailboxTerm(getNonWildcardPart(fullName));
    }


    @Override
    public boolean isAscii() {
        return isAscii(fullName);
    }

    @Override
    public boolean containsWildcard() {
        return null == fullName ? false :fullName.indexOf('*') >= 0 || fullName.indexOf('?') >= 0;
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(MimeStorageUtility.ORIGINAL_MAILBOX)) {
            fetchProfile.add(MimeStorageUtility.ORIGINAL_MAILBOX);
        }
    }

}
