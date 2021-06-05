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

package com.openexchange.mail.search;

import java.util.Collection;
import javax.mail.FetchProfile;
import javax.mail.Message;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.utils.MimeStorageUtility;
import com.sun.mail.imap.IMAPMessage;

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
    public XMailboxTerm(String fullName) {
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
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.ORIGINAL_FOLDER_ID);
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        FullnameArgument originalFolder = mailMessage.getOriginalFolder();
        if (originalFolder == null) {
            return false;
        }

        String fullName = originalFolder.getFullName();
        return null != fullName && fullName.equalsIgnoreCase(this.fullName);
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        try {
            if (!(msg instanceof IMAPMessage)) {
                return false;
            }

            String xMailbox = (String) ((IMAPMessage) msg).getItem("X-MAILBOX");
            return null != xMailbox && xMailbox.equalsIgnoreCase(this.fullName);
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
