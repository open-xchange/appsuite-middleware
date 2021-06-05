/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.caldav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.util.Collections;
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
            final UserizedFolder folder = this.folder;
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
        final UserizedFolder folder = this.folder;
        return new CalendarAccessOperation<Event>(factory) {

            @Override
            protected Event perform(IDBasedCalendarAccess access) throws OXException {
                return access.getEvent(new EventID(folder.getID(), resourceName));
            }
        }.execute(factory.getSession());
    }

    @Override
    public Map<String, EventsResult> resolveEvents(List<String> resourceNames) throws OXException {
        final UserizedFolder folder = this.folder;
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
    protected SyncStatus<WebdavResource> getSyncStatus(com.openexchange.dav.resources.SyncToken syncToken) throws OXException {
        throw new UnsupportedOperationException();
    }

}
