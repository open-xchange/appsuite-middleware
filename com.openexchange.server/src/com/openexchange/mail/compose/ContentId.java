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
