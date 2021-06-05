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

package com.openexchange.notification.mail;

import java.io.File;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.notification.mail.impl.ByteArrayMailAttachment;
import com.openexchange.notification.mail.impl.FileHolderMailAttachment;
import com.openexchange.notification.mail.impl.FileMailAttachment;

/**
 * {@link MailAttachments} - Utility class for mail attachments of notification mails.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MailAttachments {

    /**
     * Initializes a new {@link MailAttachments}.
     */
    private MailAttachments() {
        super();
    }

    /**
     * Creates a new <code>MailAttachment</code> instance backed by specified byte array.
     *
     * @param bytes The byte array
     * @return A new <code>MailAttachment</code> instance
     */
    public static MailAttachment newMailAttachment(byte[] bytes) {
        return new ByteArrayMailAttachment(bytes);
    }

    /**
     * Creates a new <code>MailAttachment</code> instance backed by specified file.
     *
     * @param file The file
     * @return A new <code>MailAttachment</code> instance
     */
    public static MailAttachment newMailAttachment(File file) {
        return new FileMailAttachment(file);
    }

    /**
     * Creates a new <code>MailAttachment</code> instance backed by specified file holder.
     *
     * @param fileHolder The file holder
     * @return A new <code>MailAttachment</code> instance
     */
    public static MailAttachment newMailAttachment(IFileHolder fileHolder) {
        return new FileHolderMailAttachment(fileHolder);
    }

}
