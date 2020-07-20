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

package com.openexchange.mail.compose;

import com.openexchange.java.Strings;
import com.openexchange.mail.mime.utils.MimeMessageUtility;

/**
 * {@link ContentId} - Helper class for <code>"Content-ID"</code> header.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ContentId {

    /**
     * Gets the Content-ID object for given Content-ID string.
     *
     * @param contentId The Content-ID as a string
     * @return The Content-ID object
     */
    public static ContentId valueOf(String contentId) {
        return Strings.isEmpty(contentId) ? null : new ContentId(contentId.trim());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String contentId;
    private String headerValue;
    private int hash;

    /**
     * Initializes a new {@link ContentId}.
     *
     * @param The Content-ID as a string
     * @throws IllegalArgumentException If Content-ID string is null or empty
     */
    public ContentId(String contentId) {
        super();
        if (Strings.isEmpty(contentId)) {
            throw new IllegalArgumentException("Content-ID string must not be null or empty");
        }
        this.contentId = MimeMessageUtility.trimContentId(contentId);
        hash = 0;
    }

    /**
     * Gets the Content-ID value.
     * <p>
     * <tt>"&lt;1234abcd&gt;"</tt> --&gt; <tt>"1234abcd"</tt>
     *
     * @return The Content-ID value
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * Gets the Content-Id value for being used as <code>"Content-ID"</code> header.
     * <p>
     * <tt>"1234abcd"</tt> --&gt; <tt>"&lt;1234abcd&gt;"</tt>
     *
     * @return The Content-ID header value
     */
    public String getContentIdForHeader() {
        String s = headerValue;
        if (s == null) {
            s = new StringBuilder(contentId.length() + 2).append('<').append(contentId).append('>').toString();
            this.headerValue = s;
        }
        return s;
    }


    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = prime * 1 + ((contentId == null) ? 0 : contentId.hashCode());
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContentId)) {
            return false;
        }
        ContentId other = (ContentId) obj;
        if (contentId == null) {
            if (other.contentId != null) {
                return false;
            }
        } else if (!contentId.equals(other.contentId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return contentId;
    }

}
