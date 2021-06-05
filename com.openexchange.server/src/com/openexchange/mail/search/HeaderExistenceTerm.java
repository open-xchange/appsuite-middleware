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
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link HeaderExistenceTerm} - Checks for existence of a certain header regardless of its value.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderExistenceTerm extends SearchTerm<String> {

    private static final long serialVersionUID = -167353933722555256L;

    private final String headerName;

    /**
     * Initializes a new {@link HeaderExistenceTerm}
     */
    public HeaderExistenceTerm(String headerName) {
        super();
        this.headerName = headerName;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Gets the header pattern: A {@link String} for header name.
     *
     * @return The header pattern
     */
    @Override
    public String getPattern() {
        return headerName;
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.HEADERS);
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        String val = mailMessage.getHeader(headerName, ", ");
        return val != null;
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        String[] val;
        try {
            val = msg.getHeader(headerName);
        } catch (MessagingException e) {
            org.slf4j.LoggerFactory.getLogger(HeaderExistenceTerm.class).warn("Error during search.", e);
            return false;
        }
        return val != null;
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new javax.mail.search.HeaderExistenceTerm(headerName);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return new javax.mail.search.HeaderExistenceTerm(headerName);
    }

    @Override
    public boolean isAscii() {
        return true;
    }

    @Override
    public boolean containsWildcard() {
        return false;
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
            fetchProfile.add(IMAPFolder.FetchProfileItem.HEADERS);
        }
    }

}
