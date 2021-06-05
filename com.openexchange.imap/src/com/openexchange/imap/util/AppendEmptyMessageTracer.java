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

package com.openexchange.imap.util;

import javax.mail.Message;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.utils.MessageUtility;


/**
 * {@link AppendEmptyMessageTracer} - Helper class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AppendEmptyMessageTracer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppendEmptyMessageTracer.class);

    /**
     * Initializes a new {@link AppendEmptyMessageTracer}.
     */
    private AppendEmptyMessageTracer() {
        super();
    }

    /**
     * Checks for an empty message and outputs a DEBUG logging.
     *
     * @param message The message to check
     * @param destFullName The mailbox to append to
     * @param imapConfig The associated IMAP configuration
     */
    public static void checkForEmptyMessage(Message message, String destFullName, IMAPConfig imapConfig) {
        try {
            if (LOG.isDebugEnabled()) {
                String lcct = Strings.asciiLowerCase(message.getContentType());
                if (null == lcct || lcct.startsWith("text/")) {
                    String content = null == lcct ? MessageUtility.readMimePart(message, "US-ASCII") : MessageUtility.readMimePart(message, new ContentType(lcct));
                    if (Strings.isEmpty(content)) {
                        LOG.debug("{} appends an empty message to mailbox '{}' on server {}", imapConfig.getLogin(), destFullName, imapConfig.getServer(), new Throwable("Append of empty message"));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Could not check for an empty message to mailbox '{}' on server {} for login {}", destFullName, imapConfig.getServer(), imapConfig.getLogin(), e);
        }
    }

}
