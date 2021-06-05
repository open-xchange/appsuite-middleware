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
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link SubjectTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SubjectTerm extends SearchTerm<String> {

    private static final long serialVersionUID = 1462060457742619720L;

    private final String unicodeSubject;
    private String lowerCaseAddr;

    /**
     * Initializes a new {@link SubjectTerm}
     */
    public SubjectTerm(String unicodeSubject) {
        super();
        this.unicodeSubject = unicodeSubject;
    }

    private String getLowerCaseAddr() {
        String s = lowerCaseAddr;
        if (null == s) {
            s = Strings.asciiLowerCase(unicodeSubject);
            lowerCaseAddr = s;
        }
        return s;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return The unicode representation of the subject
     */
    @Override
    public String getPattern() {
        return unicodeSubject;
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.SUBJECT);
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        final String subject;
        try {
            subject = msg.getSubject();
        } catch (MessagingException e) {
            org.slf4j.LoggerFactory.getLogger(SubjectTerm.class).warn("Error during search.", e);
            return false;
        }
        if (subject != null) {
            if (containsWildcard()) {
                return toRegex(unicodeSubject).matcher(subject).find();
            }
            return (Strings.asciiLowerCase(subject).indexOf(getLowerCaseAddr()) >= 0);
        }
        return false;
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        final String subject = mailMessage.getSubject();
        if (subject == null) {
            if (null == unicodeSubject) {
                return true;
            }
            return false;
        }
        if (null == unicodeSubject) {
            return false;
        }
        if (containsWildcard()) {
            return toRegex(unicodeSubject).matcher(subject).find();
        }
        return (Strings.asciiLowerCase(subject).indexOf(getLowerCaseAddr()) >= 0);
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new javax.mail.search.SubjectTerm(unicodeSubject);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return new javax.mail.search.SubjectTerm(getNonWildcardPart(unicodeSubject));
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(FetchProfile.Item.ENVELOPE)) {
            fetchProfile.add(FetchProfile.Item.ENVELOPE);
        }
    }

    @Override
    public boolean isAscii() {
        return isAscii(unicodeSubject);
    }

    @Override
    public boolean containsWildcard() {
        return null == unicodeSubject ? false : unicodeSubject.indexOf('*') >= 0 || unicodeSubject.indexOf('?') >= 0;
    }
}
