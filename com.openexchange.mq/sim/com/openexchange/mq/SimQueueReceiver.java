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

package com.openexchange.mq;

import java.util.PriorityQueue;
import com.openexchange.exception.OXException;
import com.openexchange.mq.queue.MQQueueReceiver;

/**
 * {@link SimQueueReceiver}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SimQueueReceiver implements MQQueueReceiver {

    private static final class QueueElement implements Comparable<QueueElement> {

        public final Object value;

        public final int priority;

        public QueueElement(final Object value, final int priority) {
            super();
            this.value = value;
            this.priority = priority;
        }

        @Override
        public int compareTo(final QueueElement anotherElement) {
            final int thisPrio = this.priority;
            final int anotherPrio = anotherElement.priority;
            return (thisPrio < anotherPrio ? -1 : (thisPrio == anotherPrio ? 0 : 1));
        }
    }

    private final PriorityQueue<QueueElement> priorityQueue;

    /**
     * Initializes a new {@link SimQueueReceiver}.
     */
    public SimQueueReceiver() {
        super();
        priorityQueue = new PriorityQueue<QueueElement>();
    }

    @Override
    public void close() {
        // Nothing to close
    }

    @Override
    public String receiveText() throws OXException {
        final QueueElement queueElement = priorityQueue.poll();
        if (null == queueElement) {
            return null;
        }
        return queueElement.value.toString();
    }

    @Override
    public String receiveText(final long timeout) throws OXException {
        return receiveText();
    }

    @Override
    public String receiveTextNoWait() throws OXException {
        return receiveText();
    }

    @Override
    public Object receiveObject() throws OXException {
        final QueueElement queueElement = priorityQueue.poll();
        if (null == queueElement) {
            return null;
        }
        return queueElement.value;
    }

    @Override
    public Object receiveObject(final long timeout) throws OXException {
        return receiveObject();
    }

    @Override
    public Object receiveObjectNoWait() throws OXException {
        return receiveObject();
    }

    @Override
    public byte[] receiveBytes() throws OXException {
        final QueueElement queueElement = priorityQueue.poll();
        if (null == queueElement) {
            return null;
        }
        return (byte[]) queueElement.value;
    }

    @Override
    public byte[] receiveBytes(final long timeout) throws OXException {
        return receiveBytes();
    }

    @Override
    public byte[] receiveBytesNoWait() throws OXException {
        return receiveBytes();
    }

}
