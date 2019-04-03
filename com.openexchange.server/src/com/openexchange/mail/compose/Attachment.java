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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose;

import java.io.InputStream;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link Attachment} - Represents an attachment associated with a message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface Attachment {

    /** The Content-Disposition for an attachment's content */
    public static enum ContentDisposition {
        /**
         * The <code>"attachment"</code> disposition type.
         */
        ATTACHMENT("attachment"),
        /**
         * The <code>"inline"</code> disposition type.
         */
        INLINE("inline");

        private final String id;

        private ContentDisposition(String id) {
            this.id = id;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the Content-Disposition for given identifier
         *
         * @param disposition The identifier to look-up
         * @return The associated Content-Disposition or <code>null</code>
         */
        public static ContentDisposition dispositionFor(String disposition) {
            if (Strings.isEmpty(disposition)) {
                return null;
            }

            String lk = disposition.trim();
            for (ContentDisposition d : ContentDisposition.values()) {
                if (lk.equalsIgnoreCase(d.id)) {
                    return d;
                }
            }
            return null;
        }

    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    UUID getId();

    /**
     * Gets the identifier of the composition space, this attachment is associated with
     *
     * @return The composition space identifier
     */
    UUID getCompositionSpaceId();

    /**
     * Gets the reference to the persisted attachment; e.g. <code>"fs://517616721"</code> or <code>"db://167215176"</code>
     *
     * @return The storage reference
     */
    AttachmentStorageReference getStorageReference();

    /**
     * Gets the attachment's data.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: The input stream is supposed to be generated "on the fly"
     * </div>
     *
     * @return The data
     * @throws OXException If data cannot be returned
     */
    InputStream getData() throws OXException;

    /**
     * Gets the file name
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the size in bytes.
     *
     * @return The size in bytes or <code>-1</code> if unknown
     */
    long getSize();

    /**
     * Gets the MIME type
     *
     * @return The MIME type or <code>null</code>
     */
    String getMimeType();

    /**
     * Gets the content identifier reference
     *
     * @return The content identifier
     */
    String getContentId();

    /**
     * Gets the content disposition
     *
     * @return The content disposition
     */
    ContentDisposition getContentDisposition();

    /**
     * Gets the attachment's origin.
     *
     * @return The origin
     */
    AttachmentOrigin getOrigin();
}
