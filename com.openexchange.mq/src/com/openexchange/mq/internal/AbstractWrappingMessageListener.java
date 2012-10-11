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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mq.internal;

import java.io.ByteArrayOutputStream;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.java.UnsynchronizedByteArrayOutputStream;

/**
 * {@link AbstractWrappingMessageListener}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractWrappingMessageListener {

    /**
     * Initializes a new {@link AbstractWrappingMessageListener}.
     */
    protected AbstractWrappingMessageListener() {
        super();
    }

    /**
     * Reads the bytes from specified {@link BytesMessage}.
     * 
     * @param bytesMessage The byte message to read from
     * @return The read bytes
     * @throws JMSException If reading bytes fails
     */
    protected static byte[] readBytesFrom(final BytesMessage bytesMessage) throws JMSException {
        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
        final int buflen = 2048;
        final byte[] buf = new byte[buflen];
        for (int read; (read = bytesMessage.readBytes(buf, buflen)) > 0;) {
            out.write(buf, 0, read);
        }
        return out.toByteArray();
    }

    protected static void acknowledge(final Message message) {
        if (null == message) {
            return;
        }
        try {
            message.acknowledge();
        } catch (final JMSException e) {
            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractWrappingMessageListener.class));
            logger.error(message);
        } catch (final RuntimeException e) {
            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractWrappingMessageListener.class));
            logger.error(message);
        }
    }

}
