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

import static com.openexchange.mail.utils.StorageUtility.getAllAddresses;
import java.util.Collection;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FromStringTerm;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.utils.MimeMessageUtility;

/**
 * {@link FromTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FromTerm extends AbstractAddressTerm {

    private static final long serialVersionUID = -1266840509034246826L;

    /**
     * Initializes a new {@link FromTerm}
     */
    public FromTerm(String pattern) {
        super(pattern);
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return The pattern of the from address
     */
    @Override
    public String getPattern() {
        return addr;
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.FROM);
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        if (containsWildcard()) {
            return toRegex(addr).matcher(getAllAddresses(mailMessage.getFrom())).find();
        }
        return (Strings.asciiLowerCase(getAllAddresses(mailMessage.getFrom())).indexOf(getLowerCaseAddr()) >= 0);
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        try {
            /*
             * Get plain headers
             */
            final String[] headers = msg.getHeader("From");
            if (null == headers || headers.length == 0) {
                return false;
            }
            /*
             * Parse addresses
             */
            final InternetAddress[] addresses = MimeMessageUtility.parseAddressList(MimeMessageUtility.decodeMultiEncodedHeader(headers[0]), false, false);
            if (containsWildcard()) {
                return toRegex(addr).matcher(getAllAddresses(addresses)).find();
            }
            return (Strings.asciiLowerCase(getAllAddresses(addresses)).indexOf(getLowerCaseAddr()) >= 0);
        } catch (MessagingException e) {
            org.slf4j.LoggerFactory.getLogger(FromTerm.class).warn("Error during search.", e);
            return false;
        }
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new FromStringTerm(addr);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return new FromStringTerm(getNonWildcardPart(addr));
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(FetchProfile.Item.ENVELOPE)) {
            fetchProfile.add(FetchProfile.Item.ENVELOPE);
        }
    }

    @Override
    public boolean isAscii() {
        return isAscii(addr);
    }

    @Override
    public boolean containsWildcard() {
        return null == addr ? false : addr.indexOf('*') >= 0 || addr.indexOf('?') >= 0;
    }
}
