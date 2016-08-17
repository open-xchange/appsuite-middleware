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
