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
 * {@link HeaderTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderTerm extends SearchTerm<String[]> {

    private static final long serialVersionUID = -167353933722555256L;

    private final String[] hdr;

    /**
     * Initializes a new {@link HeaderTerm}
     */
    public HeaderTerm(final String headerName, final String headerValue) {
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
    public void addMailField(final Collection<MailField> col) {
        col.add(MailField.HEADERS);
    }

    @Override
    public boolean matches(final MailMessage mailMessage) {
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
    public boolean matches(final Message msg) throws OXException {
        final String[] val;
        try {
            val = msg.getHeader(hdr[0]);
        } catch (final MessagingException e) {
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
