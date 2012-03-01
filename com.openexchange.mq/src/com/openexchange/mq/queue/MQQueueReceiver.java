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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mq.queue;

import com.openexchange.exception.OXException;
import com.openexchange.mq.MQCloseable;

/**
 * {@link MQQueueReceiver} - A queue receiver intended to be re-used. Invoke {@link #close()} method when done.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MQQueueReceiver extends MQCloseable {

    /**
     * Receives the next text message produced for this receiver.
     * <p>
     * This call blocks indefinitely until a message is produced or until this receiver is closed.
     * 
     * @return The next text produced for this receiver, or <code>null</code> if this receiver is concurrently closed
     * @exception OXException If receiver fails to receive the next text
     */
    public String receiveText() throws OXException;

    /**
     * Receives the next message that arrives within the specified timeout interval.
     * <p>
     * This call blocks until a message arrives, the timeout expires, or this receiver is closed. A timeout of zero never expires, and the
     * call blocks indefinitely.
     * 
     * @param timeout The timeout value in milliseconds
     * @return The next text produced for this receiver, or null if the timeout expires or this receiver is concurrently closed
     * @throws OXException If receiver fails to receive the next text
     */
    public String receiveText(final long timeout) throws OXException;

    /**
     * Receives the next text if one is immediately available.
     * 
     * @return The next text produced for this message consumer, or null if one is not available
     * @throws OXException If the receiver fails to receive the next text
     */
    public String receiveTextNoWait() throws OXException;

    /**
     * Receives the next Java object produced for this receiver.
     * <p>
     * This call blocks indefinitely until a Java object is produced or until this receiver is closed.
     * 
     * @return The next Java object produced for this receiver, or <code>null</code> if this receiver is concurrently closed
     * @exception OXException If receiver fails to receive the next Java object
     */
    public Object receiveObject() throws OXException;

    /**
     * Receives the next Java object that arrives within the specified timeout interval.
     * <p>
     * This call blocks until a Java object arrives, the timeout expires, or this receiver is closed. A timeout of zero never expires, and
     * the call blocks indefinitely.
     * 
     * @param timeout The timeout value in milliseconds
     * @return The next Java object produced for this receiver, or <code>null</code> if the timeout expires or this receiver is concurrently
     *         closed
     * @throws OXException If receiver fails to receive the next Java object
     */
    public Object receiveObject(final long timeout) throws OXException;

    /**
     * Receives the next Java object if one is immediately available.
     * 
     * @return The next Java object produced for this receiver, or <code>null</code> if one is not available
     * @throws OXException If the receiver fails to receive the next Java object
     */
    public Object receiveObjectNoWait() throws OXException;

    /**
     * Receives the next bytes produced for this receiver.
     * <p>
     * This call blocks indefinitely until bytes are produced or until this receiver is closed.
     * 
     * @return The next bytes produced for this receiver, or <code>null</code> if this receiver is concurrently closed
     * @exception OXException If receiver fails to receive the next bytes
     */
    public byte[] receiveBytes() throws OXException;

    /**
     * Receives the next bytes that arrives within the specified timeout interval.
     * <p>
     * This call blocks until bytes arrive, the timeout expires, or this receiver is closed. A timeout of zero never expires, and the call
     * blocks indefinitely.
     * 
     * @param timeout The timeout value in milliseconds
     * @return The next bytes produced for this receiver, or <code>null</code> if the timeout expires or this receiver is concurrently
     *         closed
     * @throws OXException If receiver fails to receive the next bytes
     */
    public byte[] receiveBytes(final long timeout) throws OXException;

    /**
     * Receives the next bytes if one is immediately available.
     * 
     * @return The next bytes produced for this receiver, or <code>null</code> if one is not available
     * @throws OXException If the receiver fails to receive the next bytes
     */
    public byte[] receiveBytesNoWait() throws OXException;

}
