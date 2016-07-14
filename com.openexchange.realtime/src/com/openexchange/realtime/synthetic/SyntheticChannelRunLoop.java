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

package com.openexchange.realtime.synthetic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.packet.ID;
import com.openexchange.threadpool.RunLoop;


/**
 * {@link SyntheticChannelRunLoop}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SyntheticChannelRunLoop extends RunLoop<MessageDispatch> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyntheticChannelRunLoop.class);

   /**
    * NOOP Dispatch Does nothing and is only used for stopping a {@link SyntheticChannelRunLoop} that is waiting for the next
    * element being offered to it's {@link BlockingQueue}
    */
    private final static MessageDispatch NOOP = new MessageDispatch(null, null) {
        @Override
        public void tick() {/*NOOP*/}
    };

    public SyntheticChannelRunLoop(String name) {
        super(name);
    }

    @Override
    protected void handle(MessageDispatch element) {
        element.tick();
    }

    /**
     * Remove all {@link MessageDispatch}s that were destined for the given handle. This will pause the RunLoop, causing it to refuse any
     * Elements offered. Matching elements are removed from the RunLoop. Finally handling continues and the matching elements are returned.
     * @param destination The handle to match against
     * @return All {@link MessageDispatch}s that were destined for the given handle
     */
    public Collection<MessageDispatch> removeMessagesForHandle(ID destination) {
        List<MessageDispatch> matchingElements = new ArrayList<MessageDispatch>();
        // Pause handling for MessageDispatch inspection but make sure to enable it again
        try {
            pauseHandling();
            // Check currently handled element(leave?) first. Set to null so it doesn't get handled if it isdestinedForID
            MessageDispatch currentElement = this.currentElementReference.get();
            if (currentElement != null && isDestinedForID(destination, currentElement)) {
                this.currentElementReference.compareAndSet(currentElement, null);
            }

            // remove remaining messages from queue
            for (Iterator<MessageDispatch> iterator = queue.iterator(); iterator.hasNext();) {
                MessageDispatch next = iterator.next();
                if (isDestinedForID(destination, next)) {
                    matchingElements.add(next);
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            LOG.warn("", e);
        } finally {
            continueHandling();
        }
        return matchingElements;
    }

    private boolean isDestinedForID(ID destination, MessageDispatch messageDispatch) {
        ComponentHandle handle = messageDispatch.getHandle();
        ID currentID = handle.getId();
        return currentID.equals(destination);
    }

    @Override
    protected void unblock() {
        queue.offer(NOOP);
    }

}
