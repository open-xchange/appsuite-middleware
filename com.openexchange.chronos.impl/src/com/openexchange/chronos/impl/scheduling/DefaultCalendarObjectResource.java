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

package com.openexchange.chronos.impl.scheduling;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.scheduling.CalendarObjectResource;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DefaultCalendarObjectResource}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class DefaultCalendarObjectResource implements CalendarObjectResource {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultCalendarObjectResource.class);

    private final ServiceLookup serviceLookup;
    private final List<Event> events;
    private final int contextId;

    /**
     * Initializes a new {@link DefaultCalendarObjectResource}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     * @param contextId The context identifier
     * @param event The event
     */
    public DefaultCalendarObjectResource(ServiceLookup serviceLookup, int contextId, Event event) {
        this(serviceLookup, contextId, Collections.singletonList(event));
    }

    /**
     * Initializes a new {@link DefaultCalendarObjectResource}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     * @param contextId The context identifier
     * @param events The list of events
     */
    public DefaultCalendarObjectResource(ServiceLookup serviceLookup, int contextId, Collection<Event> events) {
        this.serviceLookup = serviceLookup;
        this.contextId = contextId;
        this.events = new LinkedList<>(events);
    }

    @SuppressWarnings("null")
    @Override
    @NonNull
    public List<Event> getCalendarObject() {
        return events;
    }

    @Override
    @NonNull
    public InputStream getAttachmentData(int managedId) throws OXException {
        Check.containsAttachment(getCalendarObject(), managedId);
        return new OSGiCalendarStorageOperation<InputStream>(serviceLookup, contextId, Utils.ACCOUNT_ID) {

            @Override
            protected InputStream call(CalendarStorage storage) throws OXException {
                return storage.getAttachmentStorage().loadAttachmentData(managedId);
            }
        }.executeQuery();
    }

}
