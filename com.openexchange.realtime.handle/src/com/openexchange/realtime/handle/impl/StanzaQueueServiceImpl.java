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

package com.openexchange.realtime.handle.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.openexchange.realtime.handle.StanzaQueueService;
import com.openexchange.realtime.packet.IQ;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Stanza;

/**
 * {@link StanzaQueueServiceImpl} - Used by Channel dependend handlers after receiving and converting a Stanza from the Channel specific
 * format to the internally used Stanza POJO. The StanzaQueueServiceImpl takes care of the Stanzas by sorting them into the proper Queue
 * The designated StanzaHandler takes care of the Stanza by removing it from the Queue and handling it.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class StanzaQueueServiceImpl implements StanzaQueueService {

    private final BlockingQueue<Presence> presenceQueue = new LinkedBlockingQueue<Presence>(Integer.MAX_VALUE);

    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>(Integer.MAX_VALUE);

    private final BlockingQueue<IQ> iqQueue = new LinkedBlockingQueue<IQ>(Integer.MAX_VALUE);

    @Override
    public boolean enqueueStanza(Stanza stanza) {
        if (stanza == null) {
            throw new IllegalArgumentException("Parameter 'stanza' must not be null!");
        }
        if (stanza instanceof Presence) {
            return presenceQueue.offer((Presence) stanza);
        } else if (stanza instanceof Message) {
            return messageQueue.offer((Message) stanza);
        } else if (stanza instanceof IQ) {
            return iqQueue.offer((IQ) stanza);
        } else {
            throw new IllegalArgumentException("Parameter 'stanza' must be of type Presence, Message or IQ!");
        }
    }

    public BlockingQueue<Presence> getPresenceQueue() {
        return presenceQueue;
    }

    public BlockingQueue<Message> getMessageQueue() {
        return messageQueue;
    }

    public BlockingQueue<IQ> getIqQueue() {
        return iqQueue;
    }

}
