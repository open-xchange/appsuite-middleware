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

package com.openexchange.caldav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.mixins.SupportedReportSet;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.DefaultEventsResult;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.dav.mixins.SyncToken;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CTagEventCollection}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class CTagEventCollection extends EventCollection {

    /**
     * Initializes a new {@link CTagEventCollection}.
     * 
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     * @param order The calendar order to use, or {@value #NO_ORDER} for no specific order
     */
    public CTagEventCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder, order);

        // Do not support 'sync-collection' and SyncToken.
        // @formatter:off
        mixins.removeIf(SupportedReportSet.class::isInstance);
        mixins.removeIf(SyncToken.class::isInstance);
        mixins.add(new SingleXMLPropertyMixin(Protocol.DAV_NS.getURI(), "supported-report-set") {

            @Override
            protected String getValue() {
                return "<D:supported-report><D:report><CAL:calendar-multiget/></D:report></D:supported-report>" +
                    "<D:supported-report><D:report><CAL:calendar-query/></D:report></D:supported-report>" +
                    "<D:supported-report><D:report><D:acl-principal-prop-set/></D:report></D:supported-report>" +
                    "<D:supported-report><D:report><D:principal-match/></D:report></D:supported-report>" +
                    "<D:supported-report><D:report><D:principal-property-search/></D:report></D:supported-report>" +
                    "<D:supported-report><D:report><D:expand-property/></D:report></D:supported-report>" +
                    "<D:supported-report><D:report><CS:calendarserver-principal-search/></D:report></D:supported-report>"
                ;
            }

        });
        // @formatter:on
    }

    @Override
    public String getCTag() throws WebdavProtocolException {
        try {
            return new CalendarAccessOperation<String>(factory) {

                @Override
                protected String perform(IDBasedCalendarAccess access) throws OXException {
                    return access.getCTag(folder.getID());
                }
            }.execute(factory.getSession());
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    @Override
    public String getSyncToken() throws WebdavProtocolException {
        return null;
    }

    @Override
    protected Event getObject(String resourceName) throws OXException {
        if (Strings.isEmpty(resourceName)) {
            return null;
        }
        return new CalendarAccessOperation<Event>(factory) {

            @Override
            protected Event perform(IDBasedCalendarAccess access) throws OXException {
                return access.getEvent(new EventID(folder.getID(), resourceName));
            }
        }.execute(factory.getSession());
    }

    @Override
    public Map<String, EventsResult> resolveEvents(List<String> resourceNames) throws OXException {
        return new CalendarAccessOperation<Map<String, EventsResult>>(factory) {

            @Override
            protected Map<String, EventsResult> perform(IDBasedCalendarAccess access) throws OXException {
                List<EventID> eventIDs = resourceNames.stream().map(name -> new EventID(folder.getID(), name)).collect(Collectors.toList());
                List<Event> events = access.getEvents(eventIDs);
                Map<String, EventsResult> retval = events.stream().collect(Collectors.toMap(Event::getId, e -> new DefaultEventsResult(Collections.singletonList(e))));

                return retval;
            }
        }.execute(factory.getSession());
    }

    @Override
    protected WebdavPath constructPathForChildResource(Event object) {
        return constructPathForChildResource(object.getId() + getFileExtension());
    }

    @Override
    protected SyncStatus<WebdavResource> getSyncStatus(Date since) throws OXException {
        throw new UnsupportedOperationException();
    }

}
