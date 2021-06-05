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
import com.openexchange.mail.mime.MimeType2ExtMap;

/**
 * {@link Attachments} - A utility class for attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class Attachments {

    /**
     * Initializes a new {@link Attachments}.
     */
    private Attachments() {
        super();
    }

    /**
     * Checks if given attachment is <b>not</b> an image.
     *
     * @param attachment The attachment to check
     * @return <code>true</code> if attachment is <b>not</b> an image; otherwise <code>false</code>
     */
    public static boolean isNoImage(Attachment attachment) {
        return false == isImage(attachment);
    }

    /**
     * Checks if given attachment is an image.
     *
     * @param attachment The attachment to check
     * @return <code>true</code> if attachment is an image; otherwise <code>false</code>
     */
    public static boolean isImage(Attachment attachment) {
        return (mimeTypeIndicatesImage(attachment.getMimeType()) || mimeTypeIndicatesImage(getMimeTypeByFileName(attachment.getName())));
    }

    private static boolean mimeTypeIndicatesImage(String mimeType) {
        // Starts with "image/"
        return (null != mimeType && Strings.asciiLowerCase(mimeType).startsWith("image/"));
    }

    private static String getMimeTypeByFileName(String fileName) {
        return MimeType2ExtMap.getContentType(fileName, null);
    }

}
