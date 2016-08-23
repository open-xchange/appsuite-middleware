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

package com.openexchange.caldav.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.mail.internet.AddressException;
import javax.mail.internet.idn.IDNA;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.mixins.ScheduleOutboxURL;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.FreeBusyInformation;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.mixins.ResourceId;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.DefaultPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.service.FreeBusyService;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link ScheduleOutboxCollection} - A resource at which busy time
 * information requests are targeted.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ScheduleOutboxCollection extends DAVCollection {

    private static final String CALDAV_NS = CaldavProtocol.CAL_NS.getURI();
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ScheduleOutboxCollection.class);

    private final GroupwareCaldavFactory factory;

    private List<FreeBusyInformation> freeBusyRequest = null;

    /**
     * Initializes a new {@link ScheduleOutboxCollection}.
     *
     * @param factory The factory
     */
    public ScheduleOutboxCollection(GroupwareCaldavFactory factory) {
        super(factory, new WebdavPath(ScheduleOutboxURL.SCHEDULE_OUTBOX));
        this.factory = factory;
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[] {
            new DefaultPermission(getFactory().getUser().getId(), false, Permissions.createPermissionBits(
                Permission.CREATE_OBJECTS_IN_FOLDER, Permission.READ_ALL_OBJECTS, Permission.WRITE_ALL_OBJECTS, Permission.DELETE_ALL_OBJECTS, false))
        };
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return super.getResourceType() + "<CAL:schedule-outbox />";
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
        freeBusyRequest = parseFreeBusyRequest(body);
    }

    @Override
    public boolean hasBody() {
        return true;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(getScheduleResponse());
        XMLOutputter outputter = new XMLOutputter();
        try {
            outputter.output(document, outputStream);
        } catch (IOException e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private List<FreeBusyInformation> parseFreeBusyRequest(InputStream inputStream) throws WebdavProtocolException {
        try {
            return factory.getIcalParser().parseFreeBusy(inputStream, TimeZone.getTimeZone("UTC"), factory.getContext(),
                new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());
        } catch (ConversionError e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private Element getScheduleResponse() {
        Element scheduleResponse = new Element("schedule-response", CALDAV_NS);
        if (null != freeBusyRequest) {
            for (FreeBusyInformation freeBusyInformation : freeBusyRequest) {
                String[] requestedAttendees = freeBusyInformation.getAttendees();
                String[] resolvedAttendees = resolveCalendarUsers(requestedAttendees);
                Map<String, FreeBusyData> freeBusy = null;
                try {
                    freeBusy = factory.requireService(FreeBusyService.class).getMergedFreeBusy(factory.getSession(),
                        Arrays.asList(resolvedAttendees), freeBusyInformation.getStartDate(), freeBusyInformation.getEndDate());
                } catch (OXException e) {
                    LOG.error("error getting free/busy information", e);
                }
                if (null != freeBusy) {
                    for (int i = 0; i < requestedAttendees.length; i++) {
                        scheduleResponse.addContent(getResponse(requestedAttendees[i], freeBusyInformation.getUid(), freeBusy.get(resolvedAttendees[i])));
                    }
                }
            }
        }
        return scheduleResponse;
    }

    private String[] resolveCalendarUsers(String[] attendees) {
        String[] resolvedAttendees = new String[attendees.length];
        for (int i = 0; i < attendees.length; i++) {
            /*
             * try principal URL
             */
            PrincipalURL principalURL = PrincipalURL.parse(attendees[i]);
            if (null != principalURL) {
                resolvedAttendees[i] = String.valueOf(principalURL.getPrincipalID());
                continue;
            }
            /*
             * try resource ID
             */
            ResourceId resourceId = ResourceId.parse(attendees[i]);
            if (null != resourceId) {
                resolvedAttendees[i] = String.valueOf(resourceId.getPrincipalID());
                continue;
            }
            /*
             * try e-mail address
             */
            try {
                URI uri = new URI(attendees[i]);
                String specificPart = uri.getSchemeSpecificPart();
                if (false == Strings.isEmpty(specificPart)) {
                    String mail = null;
                    if ("mailto".equalsIgnoreCase(uri.getScheme())) {
                        /*
                         * mailto-scheme -> e-mail address
                         */
                        mail = uri.getSchemeSpecificPart();
                    } else {
                        /*
                         * try and parse value as quoted internet address (best effort)
                         */
                        try {
                            mail = new QuotedInternetAddress(specificPart).getAddress();
                        } catch (AddressException e) {
                            // ignore
                        }
                    }
                    /*
                     * add iCal participant if parsed successfully
                     */
                    if (Strings.isNotEmpty(mail)) {
                        resolvedAttendees[i] = IDNA.toIDN(mail);
                        continue;
                    }
                }
            } catch (URISyntaxException e) {
                LOG.error("error parsing attendee URL", e);
            }
            /*
             * take over attendee as-is
             */
            resolvedAttendees[i] = attendees[i];
        }
        return resolvedAttendees;
    }

    private Element getResponse(String attendee, String uid, FreeBusyData freeBusyData) {
        Element response = new Element("response", CALDAV_NS);
        /*
         * prepare recipient
         */
        Element recipient = new Element("recipient", CALDAV_NS);
        response.addContent(recipient);
        Element href = new Element("href", Protocol.DAV_NS);
        href.addContent(attendee);
        recipient.addContent(href);
        /*
         * prepare request status
         */
        Element requestStatus = new Element("request-status", CALDAV_NS);
        response.addContent(requestStatus);
        if (null != freeBusyData) {
            /*
             * add freebusy info for this entity
             */
            final Element calendarData = new Element("calendar-data", CALDAV_NS);
            response.addContent(calendarData);
            try {
                if (false == freeBusyData.hasData() && freeBusyData.hasWarnings()) {
                    requestStatus.addContent("3.7;Invalid calendar user");
                } else {
                    calendarData.addContent(getVFreeBusy(uid, freeBusyData, attendee));
                    requestStatus.addContent("2.0;Success");
                }
            } catch (OXException e) {
                LOG.warn("error getting freebusy", e);
                requestStatus.addContent("5.1;Service unavailable");
            }
        } else {
            /*
             * no info for this entity
             */
            requestStatus.addContent("3.7;Invalid calendar user");
        }
        /*
         * add response description
         */
        Element responseDescription = new Element("responsedescription", Protocol.DAV_NS);
        responseDescription.addContent("OK");
        response.addContent(responseDescription);
        return response;
    }

    private String getVFreeBusy(String uid, FreeBusyData freeBusyData, String attendee) throws OXException {
        /*
         * generate free busy information
         */
        FreeBusyInformation fbInfo = new FreeBusyInformation();
        fbInfo.setAttendee(attendee);
        fbInfo.setUid(uid);
        fbInfo.setFreeBusyIntervals(freeBusyData.getIntervals());
        fbInfo.setUid(uid);
        fbInfo.setStartDate(freeBusyData.getFrom());
        fbInfo.setEndDate(freeBusyData.getUntil());
        /*
         * serialize as free/busy reply
         */
        return factory.getIcalEmitter().writeFreeBusyReply(
                fbInfo, factory.getContext(), new LinkedList<ConversionError>(), new LinkedList<ConversionWarning>());
    }

    @Override
    public String getContentType() throws WebdavProtocolException {
        return "text/xml; charset=UTF-8";
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return "Schedule Outbox";
    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void delete() throws WebdavProtocolException {
    }

    @Override
    public void setLanguage(String language) throws WebdavProtocolException {
    }

    @Override
    public void setLength(Long length) throws WebdavProtocolException {
    }

    @Override
    public void setContentType(String type) throws WebdavProtocolException {
    }

    @Override
    public String getSource() throws WebdavProtocolException {
        return null;
    }

    @Override
    public void setSource(String source) throws WebdavProtocolException {
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return null;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return null;
    }

    @Override
    public AbstractResource getChild(String name) throws WebdavProtocolException {
        return null;
    }

}
