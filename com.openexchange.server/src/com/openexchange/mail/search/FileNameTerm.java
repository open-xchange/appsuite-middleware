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
    public boolean matches(Message msg) throws OXException {
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
    public boolean matches(MailMessage mailMessage) throws OXException {
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
