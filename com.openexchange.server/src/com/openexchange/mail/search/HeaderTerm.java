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
import java.util.regex.Pattern;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link HeaderTerm} - Checks if the value of a certain header matches a given string.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderTerm extends SearchTerm<String[]> {

    private static final long serialVersionUID = -167353933722555256L;

    private final String[] hdr;

    /**
     * Initializes a new {@link HeaderTerm}
     */
    public HeaderTerm(String headerName, String headerValue) {
        super();
        hdr = new String[] { headerName, headerValue };
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Gets the header pattern: An array of {@link String} with length <code>2</code> with header name and header name-
     *
     * @return The header pattern
     */
    @Override
    public String[] getPattern() {
        return hdr;
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.HEADERS);
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        final String val = mailMessage.getHeader(hdr[0], ", ");
        if (val == null) {
            if (hdr[1] == null) {
                return true;
            }
            return false;
        }
        if (containsWildcard()) {
            return toRegex(hdr[1]).matcher(val).find();
        }
        return (Strings.asciiLowerCase(val).contains(Strings.asciiLowerCase(hdr[1])));
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        final String[] val;
        try {
            val = msg.getHeader(hdr[0]);
        } catch (MessagingException e) {
            org.slf4j.LoggerFactory.getLogger(HeaderTerm.class).warn("Error during search.", e);
            return false;
        }
        if (val == null || val.length == 0) {
            if (hdr[1] == null) {
                return true;
            }

            return false;
        }
        if (containsWildcard()) {
            final Pattern p = toRegex(hdr[1]);
            boolean found = false;
            for (int i = 0; i < val.length && !found; i++) {
                found = p.matcher(val[i]).find();
            }
            return found;
        }
        boolean found = false;
        for (int i = 0; i < val.length && !found; i++) {
            found = (Strings.asciiLowerCase(val[i]).contains(Strings.asciiLowerCase(hdr[1])));
        }
        return found;
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new javax.mail.search.HeaderTerm(hdr[0], hdr[1]);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return new javax.mail.search.HeaderTerm(hdr[0], getNonWildcardPart(hdr[1]));
    }

    @Override
    public boolean isAscii() {
        return isAscii(hdr[1]);
    }

    @Override
    public boolean containsWildcard() {
        return null == hdr || null == hdr[1] ? false : hdr[1].indexOf('*') >= 0 || hdr[1].indexOf('?') >= 0;
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
            fetchProfile.add(IMAPFolder.FetchProfileItem.HEADERS);
        }
    }

}
