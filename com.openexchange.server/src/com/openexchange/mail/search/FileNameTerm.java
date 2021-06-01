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

import java.io.IOException;
import java.util.Collection;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;

/**
 * {@link FileNameTerm}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class FileNameTerm extends SearchTerm<String> {

    private static final long serialVersionUID = 2515235086017062070L;

    private final String pattern;
    private String lowerCasePattern;

    /**
     * Initializes a new {@link FileNameTerm}.
     */
    public FileNameTerm(String pattern) {
        super();
        this.pattern = pattern;
    }

    private String getLowerCasePattern() {
        String s = lowerCasePattern;
        if (null == s) {
            s = Strings.asciiLowerCase(pattern);
            lowerCasePattern = s;
        }
        return s;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.ATTACHMENT_NAME);

    }

    @Override
    public boolean matches(Message msg) {
        try {
            return checkPart(msg);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(FileNameTerm.class).warn("Error during search.", e);
            return false;
        }
    }

    private boolean checkPart(Part p) throws OXException, MessagingException, IOException {
        String contentType = p.getContentType();
        if (null != contentType) {
            contentType = Strings.asciiLowerCase(contentType).trim();
            if (contentType.startsWith("multipart/")) {
                Multipart m = (Multipart) p.getContent();
                int count = m.getCount();
                for (int i = 0, k = count; k-- > 0; i++) {
                    BodyPart bodyPart = m.getBodyPart(i);
                    if (checkPart(bodyPart)) {
                        return true;
                    }
                }
            } else {
                String fileName = p.getFileName();

                if (null == fileName) {
                    // Check by "Content-Disposition" header
                    String[] header = p.getHeader("Content-Disposition");
                    if (null != header && header.length > 0) {
                        ContentDisposition cd = new ContentDisposition(header[0]);
                        fileName = cd.getFilenameParameter();
                    }
                }

                if (null == fileName) {
                    // Check by "Content-Type" header
                    String[] header = p.getHeader("Content-Type");
                    if (null != header && header.length > 0) {
                        ContentType ct = new ContentType(header[0]);
                        fileName = ct.getNameParameter();
                    }
                }

                if (null != fileName) {
                    if (containsWildcard()) {
                        return toRegex(pattern).matcher(fileName).find();
                    }
                    return (Strings.asciiLowerCase(fileName).indexOf(getLowerCasePattern()) >= 0);
                }
            }
        }

        return false;
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        try {
            return checkPart(mailMessage);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(FileNameTerm.class).warn("Error during search.", e);
            return false;
        }
    }

    private boolean checkPart(MailPart p) throws OXException {
        int count = p.getEnclosedCount();
        if (count != MailPart.NO_ENCLOSED_PARTS) {
            for (int i = 0, k = count; k-- > 0; i++) {
                MailPart enclosedPart = p.getEnclosedMailPart(i);
                if (checkPart(enclosedPart)) {
                    return true;
                }
            }
        } else {
            String fileName = p.getFileName();
            if (null != fileName) {
                if (containsWildcard()) {
                    return toRegex(pattern).matcher(fileName).find();
                }
                return (Strings.asciiLowerCase(fileName).indexOf(getLowerCasePattern()) >= 0);
            }
        }

        return false;
    }


    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new javax.mail.search.FileNameTerm(this.pattern);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return new javax.mail.search.FileNameTerm(getNonWildcardPart(this.pattern));
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(FetchProfile.Item.CONTENT_INFO)) {
            fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
        }
    }

    @Override
    public boolean isAscii() {
        return isAscii(pattern);
    }

    @Override
    public boolean containsWildcard() {
        return null == pattern ? false : pattern.indexOf('*') >= 0 || pattern.indexOf('?') >= 0;
    }

}
