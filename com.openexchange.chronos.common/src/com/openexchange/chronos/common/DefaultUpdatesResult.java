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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.util.Date;
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
	private Date timestamp;

    /**
     * Initializes a new {@link DefaultUpdatesResult}.
     *
     * @param newAndModifiedEvents The list of new/modified events
     * @param deletedEvents The list of deleted events
     */
	public DefaultUpdatesResult(List<Event> newAndModifiedEvents, List<Event> deletedEvents) {
		super();
		this.newAndModifiedEvents = newAndModifiedEvents;
		if (newAndModifiedEvents != null) {
			for (Event event : newAndModifiedEvents) {
				applyTimestamp(event.getLastModified());
			}
		}
		this.deletedEvents = deletedEvents;
		if (deletedEvents != null) {
			for (Event event : deletedEvents) {
				applyTimestamp(event.getLastModified());
			}
		}
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
    public String toString() {
        return "DefaultUpdatesResult [newAndModifiedEvents=" + newAndModifiedEvents + ", deletedEvents=" + deletedEvents + "]";
    }
    
    @Override
    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * Applies an updated server timestamp as used as new/updated last-modification date of the modified data in storage, which is usually
     * also returned to clients.
     * <p/>
     * The timestamp is taken over into the result in case no previous timestamp was set, or the passed timestamp is <i>after</i> the
     * previously set one.
     *
     * @param timestamp The timestamp to apply
     * @return A self reference
     */
    public void applyTimestamp(Date timestamp) {
        if (null == this.timestamp || null != timestamp && timestamp.after(this.timestamp)) {
            this.timestamp = timestamp;
        }
    }

}
