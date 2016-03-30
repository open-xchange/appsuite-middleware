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

package com.openexchange.mail.dataobjects;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;

/**
 * {@link MailStructure} - Represents a mail structure
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailStructure {

    private final ContentType contentType;

    private final List<MailStructure> subStructures;

    /**
     * Initializes a new {@link MailStructure}
     */
    public MailStructure() {
        super();
        contentType = new ContentType();
        subStructures = new ArrayList<MailStructure>(4);
    }

    /**
     * Initializes a new {@link MailStructure}
     *
     * @param contentType The content type
     */
    public MailStructure(final ContentType contentType) {
        super();
        this.contentType = new ContentType();
        this.contentType.setContentType(contentType);
        subStructures = new ArrayList<MailStructure>(4);
    }

    /**
     * Gets the content type
     *
     * @return The content type
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * Sets the content type
     *
     * @param contentType The content type
     */
    public void setContentType(final ContentType contentType) {
        this.contentType.setContentType(contentType);
    }

    /**
     * Adds a sub-structure
     *
     * @param mailStructure The sub-structure to add
     */
    public void addSubStructure(final MailStructure mailStructure) {
        subStructures.add(mailStructure);
    }

    /**
     * Gets this mail structure's sub-structures
     *
     * @return The sub-structures
     */
    public MailStructure[] getSubStructures() {
        return subStructures.toArray(new MailStructure[subStructures.size()]);
    }

    /**
     * Generates the corresponding mail structure for specified mail message.
     *
     * @param mail The mail message
     * @return The corresponding mail structure
     * @throws OXException If a mail error occurs
     */
    public static MailStructure getMailStructure(final MailMessage mail) throws OXException {
        return getMailStructure0(mail);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(contentType.getBaseType().toString());
        final MailStructure[] subStructures = getSubStructures();
        if (subStructures.length > 0) {
            sb.append(" (").append(subStructures[0].toString());
            for (int i = 1; i < subStructures.length; i++) {
                sb.append(", ").append(subStructures[i].toString());
            }
            sb.append(')');
        }
        return sb.toString();
    }

    private static MailStructure getMailStructure0(final MailPart part) throws OXException {
        final MailStructure retval;
        final ContentType ct = part.getContentType();
        if (ct.isMimeType(MimeTypes.MIME_MULTIPART_ALL)) {
            final MailStructure multi = new MailStructure(ct);
            final int count = part.getEnclosedCount();
            for (int i = 0; i < count; i++) {
                multi.addSubStructure(getMailStructure0(part.getEnclosedMailPart(i)));
            }
            retval = multi;
        } else if (ct.isMimeType(MimeTypes.MIME_MESSAGE_RFC822)) {
            final MailStructure nested = new MailStructure(ct);
            nested.addSubStructure(getMailStructure0((MailMessage) part.getContent()));
            retval = nested;
        } else {
            retval = new MailStructure(ct);
        }
        return retval;
    }
}
