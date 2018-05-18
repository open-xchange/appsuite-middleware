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

package com.openexchange.chronos.common;

import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.UpdatesResult;

/**
 * {@link DefaultUpdatesResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultUpdatesResult implements UpdatesResult {

    private final List<Event> newAndModifiedEvents;
    private final List<Event> deletedEvents;
    private final long timestamp;
    private final boolean truncated;

    /**
     * Initializes a new {@link DefaultUpdatesResult}.
     *
     * @param newAndModifiedEvents The list of new/modified events
     * @param deletedEvents The list of deleted events
     * @param timestamp The maximum timestamp of all events in the update result
     * @param truncated <code>true</code> if the result is truncated, <code>false</code>, otherwise
     */
    public DefaultUpdatesResult(List<Event> newAndModifiedEvents, List<Event> deletedEvents, long timestamp, boolean truncated) {
        super();
        this.newAndModifiedEvents = newAndModifiedEvents;
        this.deletedEvents = deletedEvents;
        this.timestamp = timestamp;
        this.truncated = truncated;
    }

    /**
     * Initializes a new {@link DefaultUpdatesResult}.
     *
     * @param newAndModifiedEvents The list of new/modified events
     * @param deletedEvents The list of deleted events
     */
    public DefaultUpdatesResult(List<Event> newAndModifiedEvents, List<Event> deletedEvents) {
        this(newAndModifiedEvents, deletedEvents, Math.max(getMaximumTimestamp(newAndModifiedEvents), getMaximumTimestamp(deletedEvents)), false);
    }

    @Override
    public List<Event> getNewAndModifiedEvents() {
        return newAndModifiedEvents;
    }

    @Override
    public List<Event> getDeletedEvents() {
        return deletedEvents;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isTruncated() {
        return truncated;
    }

    @Override
    public boolean isEmpty() {
        return isNullOrEmpty(deletedEvents) && isNullOrEmpty(newAndModifiedEvents);
    }

    @Override
    public String toString() {
        return "DefaultUpdatesResult [newAndModifiedEvents=" + newAndModifiedEvents + ", deletedEvents=" + deletedEvents + "]";
    }

    private static long getMaximumTimestamp(List<Event> events) {
        long timestamp = 0L;
        if (null != events) {
            for (Event event : events) {
                timestamp = Math.max(timestamp, event.getTimestamp());
            }
        }
        return timestamp;
    }

}
