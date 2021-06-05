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

package com.openexchange.contact.picture;

import java.util.Date;
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

    /**
     * Static reference that indicates that no picture was found and thus the ETag not found too
     */
    public static final String ETAG_NOT_FOUND = "NOT_FOUND";

    /** The date to indicate that a picture wasn't modified */
    public static final Date UNMODIFIED = new Date(0);

    /** Static reference that indicates that no picture was found */
    public static final ContactPicture NOT_FOUND = new ContactPicture(ETAG_NOT_FOUND, null, UNMODIFIED);

    private final String eTag;

    private final IFileHolder fileHolder;

    private final Date lastModified;

    /**
     * Initializes a new {@link ContactPicture}.
     *
     * @param eTag The associated eTag
     * @param fileHolder The file holder
     * @param lastModified The time the file was last modified
     */
    public ContactPicture(String eTag, IFileHolder fileHolder, Date lastModified) {
        this.eTag = eTag;
        this.fileHolder = fileHolder;
        this.lastModified = lastModified;
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
     * Get the time the picture was last modified
     *
     * @return The {@link Date} the picture was last modified or {@value #UNMODIFIED}
     */
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return new StringBuilder("ContactPicture ").append("[eTag=").append(eTag).append(", ").append("fileHolder=").append(null == fileHolder ? "<empty>" : Strings.isEmpty(fileHolder.getName()) ? fileHolder.getClass() : fileHolder.getName()).append(", ").append("lastModified=").append(lastModified).append(" ]").toString();
    }

}
