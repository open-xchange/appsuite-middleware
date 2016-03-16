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

package com.openexchange.messaging.generic.internet;

import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MultipartContent;

/**
 * {@link MimeMultipartContent} - The implementation of the {@link MultipartContent} using MIME conventions for the multipart data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeMultipartContent implements MultipartContent {

    /**
     * The underlying {@link MimeMultipart} instance.
     */
    final MimeMultipart mimeMultipart;

    /**
     * This multipart's section identifier.
     */
    private String sectionId;

    /**
     * The sub-type.
     */
    private final String subtype;

    /**
     * Initializes a new {@link MimeMultipartContent}.
     */
    public MimeMultipartContent() {
        this("mixed");
    }

    /**
     * Initializes a new {@link MimeMultipartContent}.
     *
     * @param subtype The multipart sub-type; e.g. "mixed", "alternative", or "related"
     */
    public MimeMultipartContent(final String subtype) {
        super();
        this.subtype = subtype;
        mimeMultipart = new MimeMultipart(subtype);
    }

    @Override
    public String getSubType() {
        return subtype;
    }

    /**
     * Gets this multipart's content type.
     *
     * @return The content type
     */
    public String getContentType() {
        return mimeMultipart.getContentType();
    }

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    public String getSectionId() {
        return sectionId;
    }

    /**
     * Sets the identifier.
     *
     * @param sectionId The identifier to set
     */
    public void setSectionId(final String sectionId) {
        this.sectionId = sectionId;
    }

    /**
     * Initializes a new {@link MimeMultipartContent}.
     *
     * @param mimeMultipart The MIME multipart
     */
    protected MimeMultipartContent(final MimeMultipart mimeMultipart) {
        super();
        final String contentType = mimeMultipart.getContentType();
        if (contentType.startsWith("multipart/mixed", 0)) {
            this.subtype = "mixed";
        } else if (contentType.startsWith("multipart/alternative", 0)) {
            this.subtype = "alternative";
        } else if (contentType.startsWith("multipart/related", 0)) {
            this.subtype = "related";
        } else {
            final int spos = contentType.indexOf('/');
            final int epos = contentType.indexOf(';');
            this.subtype = (epos < 0 ? contentType.substring(spos + 1) : contentType.substring(spos + 1, epos)).trim();
        }
        this.mimeMultipart = mimeMultipart;
    }

    @Override
    public MessagingBodyPart get(final int index) throws OXException {
        try {
            final MimeMessagingBodyPart bodyPart = new MimeMessagingBodyPart((MimePart) mimeMultipart.getBodyPart(index), this);
            bodyPart.setSectionId(sectionId == null ? Integer.toString(index + 1) : new StringBuilder(8).append(sectionId).append('.').append(
                Integer.toString(index + 1)).toString());
            return bodyPart;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public int getCount() throws OXException {
        try {
            return mimeMultipart.getCount();
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Adds a part to the multipart. The part is appended to the list of existing parts.
     *
     * @param part The part to be appended
     * @throws OXException If part cannot be appended
     */
    public void addBodyPart(final MimeMessagingBodyPart part) throws OXException {
        try {
            mimeMultipart.addBodyPart((BodyPart) part.part);
            part.setParent(this);
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Adds a part at position <code>index</code>. If <code>index</code> is not the last one in the list, the subsequent parts are shifted
     * up. If <code>index</code> is larger than the number of parts present, the part is appended to the end.
     *
     * @param part The part to be inserted
     * @param index The index where to insert the part
     * @exception OXException If part cannot be inserted
     */
    public void addBodyPart(final MimeMessagingBodyPart part, final int index) throws OXException {
        try {
            mimeMultipart.addBodyPart((BodyPart) part.part, index);
            part.setParent(this);
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Remove the specified part from the multipart. Shifts all the parts after the removed part down one.
     *
     * @param part The part to remove
     * @return <code>true</code> if part removed, <code>false</code> otherwise
     * @exception OXException If removing part fails
     */
    public boolean removeBodyPart(final MimeMessagingBodyPart part) throws OXException {
        try {
            return mimeMultipart.removeBodyPart((BodyPart) part.part);
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Remove the part at specified location (starting from 0). Shifts all the parts after the removed part down one.
     *
     * @param index Index of the part to remove
     * @exception OXException If removing part fails
     */
    public void removeBodyPart(final int index) throws OXException {
        try {
            mimeMultipart.removeBodyPart(index);
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IndexOutOfBoundsException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
