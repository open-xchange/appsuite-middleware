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

package com.openexchange.contact.picture;

import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.java.Strings;

/**
 * 
 * {@link ContactPicture}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a> Original 'Picture' class (c.o.halo)
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> MW-926
 * @since v7.10.1
 */
public class ContactPicture {

    public static final int HIGHEST_RANKING = 1;

    public static final ContactPicture FALLBACK_PICTURE;

    static {
        ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(new byte[] { 71, 73, 70, 56, 57, 97, 1, 0, 1, 0, -128, 0, 0, 0, 0, 0, -1, -1, -1, 33, -7, 4, 1, 0, 0, 0, 0, 44, 0, 0, 0, 0, 1, 0, 1, 0, 0, 2, 1, 68, 0, 59 });
        fileHolder.setContentType("image/gif");
        fileHolder.setName("image.gif");
        /*
         * If default changes, increment ETag for a quick invalidation.
         * (char-by-char comparison, see https://tools.ietf.org/html/rfc7232#section-2.3.2)
         */
        FALLBACK_PICTURE = new ContactPicture("1-fallback-image", fileHolder);
    }

    private final String eTag;

    private final IFileHolder fileHolder;

    /**
     * Initializes a new {@link ContactPicture}.
     *
     * @param eTag The associated eTag
     */
    public ContactPicture(String eTag) {
        this(eTag, null);
    }

    /**
     * Initializes a new {@link ContactPicture}.
     *
     * @param eTag The associated eTag
     * @param fileHolder The file holder
     */
    public ContactPicture(String eTag, IFileHolder fileHolder) {
        this.eTag = eTag;
        this.fileHolder = fileHolder;
    }

    /**
     * Gets the eTag
     *
     * @return The eTag
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the file holder
     *
     * @return The file holder
     */
    public IFileHolder getFileHolder() {
        return fileHolder;
    }

    /**
     * Gets a value indicating if this objects holds a valid contact picture
     * 
     * @return <code>true</code> if this objects contains a contact picture,
     *         <code>false</code> otherwise
     */
    public boolean containsContactPicture() {
        return Strings.isNotEmpty(eTag) || null != fileHolder;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eTag == null) ? 0 : eTag.hashCode());
        if (fileHolder == null) {
            result = prime * result;
        } else {
            result = prime * result + Long.valueOf(fileHolder.getLength()).intValue();
            result = prime * result + ((fileHolder.getContentType() == null) ? 0 : fileHolder.getContentType().hashCode());
            result = prime * result + ((fileHolder.getDisposition() == null) ? 0 : fileHolder.getDisposition().hashCode());
            result = prime * result + ((fileHolder.getDelivery() == null) ? 0 : fileHolder.getDelivery().hashCode());
            result = prime * result + ((fileHolder.getName() == null) ? 0 : fileHolder.getName().hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ContactPicture other = (ContactPicture) obj;
        if (eTag == null) {
            if (other.eTag != null) {
                return false;
            }
        } else if (!eTag.equals(other.eTag)) {
            return false;
        }
        if (fileHolder == null) {
            if (other.fileHolder != null) {
                return false;
            }
        } else if (other.fileHolder == null) {
            return false;
        }
        if (fileHolder.getLength() != other.fileHolder.getLength()) {
            return false;
        }
        if (fileHolder.getContentType() == null) {
            if (other.fileHolder.getContentType() != null) {
                return false;
            }
        } else if (!fileHolder.getContentType().equals(other.fileHolder.getContentType())) {
            return false;
        }
        if (fileHolder.getDisposition() == null) {
            if (other.fileHolder.getDisposition() != null) {
                return false;
            }
        } else if (!fileHolder.getDisposition().equals(other.fileHolder.getDisposition())) {
            return false;
        }
        if (fileHolder.getDelivery() == null) {
            if (other.fileHolder.getDelivery() != null) {
                return false;
            }
        } else if (!fileHolder.getDelivery().equals(other.fileHolder.getDelivery())) {
            return false;
        }
        if (fileHolder.getName() == null) {
            if (other.fileHolder.getName() != null) {
                return false;
            }
        } else if (!fileHolder.getName().equals(other.fileHolder.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("ContactPicture ").append("[eTag=").append(eTag).append(", ").append("fileHolder=").append(null == fileHolder ? "<empty>" : Strings.isEmpty(fileHolder.getName()) ? fileHolder.getClass() : fileHolder.getName()).append(" ]").toString();
    }

}
