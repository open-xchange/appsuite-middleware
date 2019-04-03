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

package com.openexchange.mail.compose.impl.storage.inmemory;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.java.BufferingQueue;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.MessageDescription;

/**
 * {@link InMemoryCompositionSpace}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class InMemoryCompositionSpace implements CompositionSpace {

    private final UUID id;
    private final AtomicLong lastModifiedStamp;
    private final InMemoryMessage message;
    private final int userId;
    private final int contextId;

    /**
     * Initializes a new {@link InMemoryCompositionSpace}.
     */
    public InMemoryCompositionSpace(UUID id, MessageDescription initialMessageDesc, BufferingQueue<InMemoryMessage> bufferingQueue, int userId, int contextId) {
        super();
        this.id = id;
        this.userId = userId;
        this.contextId = contextId;

        lastModifiedStamp = new AtomicLong(System.currentTimeMillis());
        message = new InMemoryMessage(id, initialMessageDesc, bufferingQueue, userId, contextId);
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public long getLastModified() {
        return lastModifiedStamp.get();
    }

    @Override
    public InMemoryMessage getMessage() {
        return message;
    }

    /**
     * Atomically updates the last-modified time stamp.
     *
     * @param clientStamp The option client-side stamp
     * @return <code>true</code> if successfully updates; otherwise <code>false</code> on update conflict
     */
    public boolean updateLastModifiedStamp(Date clientStamp) {
        if (null == clientStamp) {
            lastModifiedStamp.set(System.currentTimeMillis());
            return true;
        }

        long stamp;
        do {
            stamp = lastModifiedStamp.get();
            if (stamp != clientStamp.getTime()) {
                // Current stamp is not equal to client-side stamp
                return false;
            }
        } while (!lastModifiedStamp.compareAndSet(stamp, System.currentTimeMillis()));
        return true;
    }

}
