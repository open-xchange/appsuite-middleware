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

package com.openexchange.mail.compose.mailstorage.storage;

import java.util.Date;
import java.util.Objects;
import com.openexchange.mail.compose.MessageDescription;

/**
 * Encapsulates a {@link MessageDescription} along with some metadata about the
 * according draft message.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class MessageInfo {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageInfo.class);
    }

    protected final MessageDescription message;
    protected final long size;
    protected final Date lastModified;

    /**
     * Initializes a new {@link MessageInfo}.
     *
     * @param message The message
     * @param size The raw size of the message in bytes
     * @param lastModified The last-modified time stamp
     */
    MessageInfo(MessageDescription message, long size, Date lastModified) {
        super();
        this.message = Objects.requireNonNull(message);

        if (size < 0) {
            LoggerHolder.LOG.warn("", new Exception("MessageInfo was initialized without message size"));
            this.size = -1L;
        } else {
            this.size = size;
        }

        if (lastModified == null) {
            LoggerHolder.LOG.warn("", new Exception("MessageInfo was initialized without last-modified time stamp"));
            this.lastModified = new Date();
        } else {
            this.lastModified = lastModified;
        }
    }

    /**
     * Gets the message
     *
     * @return The message
     */
    public MessageDescription getMessage() {
        return message;
    }

    /**
     * Gets the raw size of the message in bytes
     *
     * @return The size or <code>-1</code>
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the last-modified time stamp, which is the number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return The last-modified time stamp
     */
    public long getLastModified() {
        return lastModified.getTime();
    }

}
