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

package com.openexchange.dav.caldav.tests.chronos;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.oauth.provider.protocol.Grant;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.dav.Config;
import com.openexchange.dav.Headers;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.WebDAVClient;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.methods.MkCalendarMethod;
import com.openexchange.dav.caldav.reports.CalendarMultiGetReportInfo;
import com.openexchange.dav.reports.SyncCollectionReportInfo;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.component.VAvailability;

/**
 * {@link AbstractChronosCaldavTest} - Common base class for CalDAV tests
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public abstract class AbstractChronosCaldavTest extends AbstractChronosTest {

    protected static final int TIMEOUT = 10000;

    private String folderId;

    @Parameter(value = 0)
    public String authMethod;

    @Parameters(name = "AuthMethod={0}")
    public static Iterable<Object[]> params() {
        return availableAuthMethods();
    }

    private static final boolean AUTODISCOVER_AUTH = true;

    protected static final String AUTH_METHOD_BASIC = "Basic Auth";

    protected static final String AUTH_METHOD_OAUTH = "OAuth";

    private Map<Long, WebDAVClient> webDAVClients;

    protected static Grant oAuthGrant;


    @SuppressWarnings("unused")
    protected static Iterable<Object[]> availableAuthMethods() {
        if (false == AUTODISCOVER_AUTH) {
            List<Object[]> authMethods = new ArrayList<>(2);
            authMethods.add(new Object[] { AUTH_METHOD_BASIC });
            authMethods.add(new Object[] { AUTH_METHOD_OAUTH });
            return authMethods;
        }
        List<Object[]> authMethods = new ArrayList<Object[]>(2);
        PropFindMethod propFind = null;
        try {
            AJAXConfig.init();
            DavPropertyNameSet props = new DavPropertyNameSet();
            props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
            propFind = new PropFindMethod(Config.getBaseUri() + "/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
            if (HttpServletResponse.SC_UNAUTHORIZED == new HttpClient().executeMethod(propFind)) {
                for (Header header : propFind.getResponseHeaders("WWW-Authenticate")) {
                    if (header.getValue().startsWith("Bearer")) {
                        authMethods.add(new Object[] { AUTH_METHOD_OAUTH });
                    } else if (header.getValue().startsWith("Basic")) {
                        authMethods.add(new Object[] { AUTH_METHOD_BASIC });
                    }
                }
            }
        } catch (OXException | IOException e) {
            fail(e.getMessage());
        } finally {
            release(propFind);
        }
        return authMethods;
    }

    protected static void release(HttpMethodBase method) {
        if (null != method) {
            method.releaseConnection();
        }
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.webDAVClients = new HashMap<Long, WebDAVClient>();
        folderId = getDefaultFolder();
        changeTimezone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Gets the personal calendar folder id
     *
     * @return
     */
    protected String getDefaultFolderID() {
        return folderId;
    }

    protected String getDefaultUserAgent() {
        return UserAgents.MACOS_10_7_3;
    }

    private String fetchSyncTokenInternal(String relativeUrl) throws Exception {
        PropFindMethod propFind = null;
        try {
            DavPropertyNameSet props = new DavPropertyNameSet();
            props.add(PropertyNames.SYNC_TOKEN);
            propFind = new PropFindMethod(getBaseUri() + relativeUrl, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
            MultiStatusResponse response = assertSingleResponse(this.getWebDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS));
            return this.extractTextContent(PropertyNames.SYNC_TOKEN, response);
        } finally {
            release(propFind);
        }
    }

    public static MultiStatusResponse assertSingleResponse(MultiStatusResponse[] responses) {
        assertNotNull("got no multistatus responses", responses);
        assertTrue("got zero multistatus responses", 0 < responses.length);
        assertTrue("got more than one multistatus responses", 1 == responses.length);
        final MultiStatusResponse response = responses[0];
        assertNotNull("no multistatus response", response);
        return response;
    }

    protected String extractTextContent(final DavPropertyName propertyName, final MultiStatusResponse response) {
        assertNotEmpty(propertyName, response);
        final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
        assertTrue("value is not a string in " + propertyName, value instanceof String);
        return (String) value;
    }

    public static void assertNotEmpty(DavPropertyName propertyName, MultiStatusResponse response) {
        assertIsPresent(propertyName, response);
        final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
        assertNotNull("no value for " + propertyName, value);
    }

    public static void assertIsPresent(DavPropertyName propertyName, MultiStatusResponse response) {
        final DavProperty<?> property = response.getProperties(StatusCodes.SC_OK).get(propertyName);
        assertNotNull("property " + propertyName + " not found", property);
    }

    protected static String getBaseUri() throws OXException {
        return getProtocol() + "://" + getHostname();
    }

    protected static String getHostname() throws OXException {
        final String hostname = AJAXConfig.getProperty(Property.HOSTNAME);
        if (null == hostname) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.HOSTNAME.getPropertyName());
        }
        return hostname;
    }

    protected static String getProtocol() throws OXException {
        final String hostname = AJAXConfig.getProperty(Property.PROTOCOL);
        if (null == hostname) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.PROTOCOL.getPropertyName());
        }
        return hostname;
    }

    protected WebDAVClient getWebDAVClient() throws Exception {
        Long threadID = Long.valueOf(Thread.currentThread().getId());
        if (false == this.webDAVClients.containsKey(threadID)) {
            WebDAVClient webDAVClient = new WebDAVClient(testUser, getDefaultUserAgent(), oAuthGrant);
            this.webDAVClients.put(threadID, webDAVClient);
            return webDAVClient;
        }
        return this.webDAVClients.get(threadID);
    }

    protected String fetchSyncToken(String folderID) throws Exception {
        return fetchSyncTokenInternal("/caldav/" + folderID);
    }

    protected String fetchSyncToken() throws Exception {
        return fetchSyncTokenInternal(getCaldavFolder());
    }

    protected SyncCollectionResponse syncCollection(SyncToken syncToken, String folderID) throws Exception {
        return syncCollectionInternal(syncToken, "/caldav/" + folderID);
    }

    protected SyncCollectionResponse syncCollectionInternal(SyncToken syncToken, String relativeUrl) throws Exception {
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        SyncCollectionReportInfo reportInfo = new SyncCollectionReportInfo(syncToken.getToken(), props);
        SyncCollectionResponse syncCollectionResponse = this.getWebDAVClient().doReport(reportInfo, getBaseUri() + relativeUrl);
        syncToken.setToken(syncCollectionResponse.getSyncToken());
        return syncCollectionResponse;
    }

    protected SyncCollectionResponse syncCollection(SyncToken syncToken) throws Exception {
        return this.syncCollection(syncToken, getCaldavFolder());
    }

    protected List<ICalResource> calendarMultiget(Collection<String> hrefs) throws Exception {
        return calendarMultiget(getCaldavFolder(), hrefs);
    }

    protected List<ICalResource> calendarMultiget(String folderID, Collection<String> hrefs) throws Exception {
        List<ICalResource> calendarData = new ArrayList<ICalResource>();
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        props.add(PropertyNames.CALENDAR_DATA);
        ReportInfo reportInfo = new CalendarMultiGetReportInfo(hrefs.toArray(new String[hrefs.size()]), props);
        MultiStatusResponse[] responses = this.getWebDAVClient().doReport(reportInfo, getBaseUri() + "/caldav/" + folderID + "/");
        for (MultiStatusResponse response : responses) {
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.GETETAG)) {
                String href = response.getHref();
                assertNotNull("got no href from response", href);
                String data = this.extractTextContent(PropertyNames.CALENDAR_DATA, response);
                assertNotNull("got no address data from response", data);
                String eTag = this.extractTextContent(PropertyNames.GETETAG, response);
                assertNotNull("got no etag data from response", eTag);
                calendarData.add(new ICalResource(data, href, eTag));
            }
        }
        return calendarData;
    }

    protected int putICal(String resourceName, String iCal) throws Exception {

        return putICal(getCaldavFolder(), resourceName, iCal);
    }

    private String getCaldavFolder(){
        String defaultFolderID = getDefaultFolderID();
        if(defaultFolderID.indexOf("/")!=-1){
            defaultFolderID = defaultFolderID.substring(defaultFolderID.lastIndexOf("/")+1, defaultFolderID.length());
        }
        return defaultFolderID;
    }

    protected int putICal(String folderID, String resourceName, String iCal) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Headers.IF_NONE_MATCH, "*");

        return putICal(folderID, resourceName, iCal, headers);
    }

    protected int putICal(String folderID, String resourceName, String iCal, Map<String, String> headers) throws Exception {
        PutMethod put = null;
        try {
            String href = "/caldav/" + folderID + "/" + urlEncode(resourceName) + ".ics";
            put = new PutMethod(getBaseUri() + href);
            for (String key : headers.keySet()) {
                put.addRequestHeader(key, headers.get(key));
            }
            put.setRequestEntity(new StringRequestEntity(iCal, "text/calendar", null));
            return getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
    }

    /**
     * Puts the specified iCal (containing one or multiple VAvailability components) via the
     * PROPATCH method to the server
     *
     * @param iCal The ical as string
     * @return The response code of the operation
     */
    protected int propPatchICal(String iCal) throws Exception {
        String resource = "schedule-inbox";
        // Set props
        DavProperty<String> set = new DefaultDavProperty<>(DavPropertyName.create("calendar-availability", PropertyNames.NS_CALENDARSERVER), iCal);
        DavPropertySet setProps = new DavPropertySet();
        setProps.add(set);

        // Unset props
        DavPropertyNameSet unsetProps = new DavPropertyNameSet();

        // Execute
        return propPatchICal(resource, setProps, unsetProps, Collections.<String, String> emptyMap());
    }

    /**
     * Sets and unsets the specified DAV properties from the specified resource using the PROPATCH method
     *
     * @param resource The resource name
     * @param setProps The properties to set
     * @param unsetProps The properties to unset
     * @param headers The headers
     * @return The response code of the operation
     */
    protected int propPatchICal(String resource, DavPropertySet setProps, DavPropertyNameSet unsetProps, Map<String, String> headers) throws Exception {
        PropPatchMethod propPatch = null;
        try {
            String href = "/caldav/" + resource + "/";
            propPatch = new PropPatchMethod(getBaseUri() + href, setProps, unsetProps);
            for (String key : headers.keySet()) {
                propPatch.addRequestHeader(key, headers.get(key));
            }
            return getWebDAVClient().executeMethod(propPatch);
        } finally {
            release(propPatch);
        }
    }

    /**
     *
     * @param property
     * @return
     * @throws Exception
     */
    protected List<ICalResource> propFind(String property) throws Exception {
        String resource = "schedule-inbox";

        DavPropertyNameSet queryProps = new DavPropertyNameSet();
        queryProps.add(DavPropertyName.create(property, PropertyNames.NS_CALENDARSERVER));

        return propFind(resource, queryProps);
    }

    /**
     *
     * @return
     * @throws Exception
     */
    protected List<ICalResource> propFind(String resource, DavPropertyNameSet queryProps) throws Exception {
        PropFindMethod propFind = null;
        try {
            String href = "/caldav/" + resource + "/";
            propFind = new PropFindMethod(getBaseUri() + href, queryProps, DavConstants.DEPTH_0);

            Assert.assertEquals("response code wrong", 207, getWebDAVClient().executeMethod(propFind));
            MultiStatus multiStatus = propFind.getResponseBodyAsMultiStatus();
            assertNotNull("got no response body", multiStatus);

            List<ICalResource> resources = new ArrayList<ICalResource>();
            MultiStatusResponse[] responses = multiStatus.getResponses();
            for (MultiStatusResponse response : responses) {
                String data = extractTextContent(PropertyNames.CALENDAR_AVAILABILITY, response);
                resources.add(new ICalResource(data));
            }
            return resources;
        } finally {
            release(propFind);
        }
    }

    protected int move(ICalResource iCalResource, String targetFolderID) throws Exception {
        MoveMethod move = null;
        try {
            String targetHref = "/caldav/" + targetFolderID + "/" + iCalResource.getHref().substring(1 + iCalResource.getHref().lastIndexOf('/'));
            move = new MoveMethod(getBaseUri() + iCalResource.getHref(), getBaseUri() + targetHref, false);
            if (null != iCalResource.getETag()) {
                move.addRequestHeader(Headers.IF_MATCH, iCalResource.getETag());
            }
            int status = getWebDAVClient().executeMethod(move);
            if (StatusCodes.SC_CREATED == status) {
                iCalResource.setHref(targetHref);
            }
            return status;
        } finally {
            release(move);
        }
    }

    protected void mkCalendar(String targetResourceName, DavPropertySet setProperties) throws Exception {
        MkCalendarMethod mkCalendar = null;
        try {
            String targetHref = "/caldav/" + targetResourceName + '/';
            mkCalendar = new MkCalendarMethod(getBaseUri() + targetHref, setProperties);
            Assert.assertEquals("response code wrong", StatusCodes.SC_CREATED, getWebDAVClient().executeMethod(mkCalendar));
        } finally {
            release(mkCalendar);
        }
    }

    protected ICalResource get(String resourceName) throws Exception {
        return get(getCaldavFolder(), resourceName, null, null);
    }

    protected ICalResource get(String folderID, String resourceName) throws Exception {
        return get(folderID, resourceName, null, null);
    }

    protected ICalResource get(String folderID, String resourceName, String ifMatchEtag) throws Exception {
        return get(folderID, resourceName, null, ifMatchEtag);
    }

    protected ICalResource get(String folderID, String resourceName, String ifNoneMatchEtag, String ifMatchEtag) throws Exception {
        GetMethod get = null;
        try {
            String href = "/caldav/" + folderID + "/" + urlEncode(resourceName) + ".ics";
            get = new GetMethod(getBaseUri() + href);
            if (null != ifNoneMatchEtag) {
                get.addRequestHeader(Headers.IF_NONE_MATCH, ifNoneMatchEtag);
            }
            if (null != ifMatchEtag) {
                get.addRequestHeader(Headers.IF_MATCH, ifMatchEtag);
            }
            Assert.assertEquals("response code wrong", StatusCodes.SC_OK, getWebDAVClient().executeMethod(get));
            byte[] responseBody = get.getResponseBody();
            assertNotNull("got no response body", responseBody);
            return new ICalResource(new String(responseBody, Charsets.UTF_8), href, get.getResponseHeader("ETag").getValue());
        } finally {
            release(get);
        }
    }

    private static String urlEncode(String name) throws URISyntaxException {
        return new URI(null, name, null).toString();
    }

    protected int putICalUpdate(String resourceName, String iCal, String ifMatchEtag) throws Exception {
        return this.putICalUpdate(getCaldavFolder(), resourceName, iCal, ifMatchEtag);
    }

    protected int putICalUpdate(String folderID, String resourceName, String iCal, String ifMatchEtag) throws Exception {
        PutMethod put = null;
        try {
            String href = "/caldav/" + folderID + "/" + urlEncode(resourceName) + ".ics";
            put = new PutMethod(getBaseUri() + href);
            if (null != ifMatchEtag) {
                put.addRequestHeader(Headers.IF_MATCH, ifMatchEtag);
            }
            put.setRequestEntity(new StringRequestEntity(iCal, "text/calendar", null));
            return getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
    }

    protected int putICalUpdate(ICalResource iCalResource) throws Exception {
        PutMethod put = null;
        try {
            put = new PutMethod(getBaseUri() + iCalResource.getHref());
            if (null != iCalResource.getETag()) {
                put.addRequestHeader(Headers.IF_MATCH, iCalResource.getETag());
            }
            put.setRequestEntity(new StringRequestEntity(iCalResource.toString(), "text/calendar", null));
            return getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
    }

    protected static int parse(String id) {
        return Integer.parseInt(id);
    }

    protected static String format(Date date, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00'");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    protected static String format(Date date, String timeZoneID) {
        return format(date, TimeZone.getTimeZone(timeZoneID));
    }

    protected static String formatAsUTC(final Date date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    protected static String formatAsDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    protected static String generateICal(Date start, Date end, String uid, String summary, String location) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("BEGIN:VCALENDAR").append("\r\n").append("VERSION:2.0").append("\r\n").append("PRODID:-//Apple Inc.//iCal 5.0.2//EN").append("\r\n").append("CALSCALE:GREGORIAN").append("\r\n").append("BEGIN:VTIMEZONE").append("\r\n").append("TZID:Europe/Amsterdam").append("\r\n").append("BEGIN:DAYLIGHT").append("\r\n").append("TZOFFSETFROM:+0100").append("\r\n").append("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU").append("\r\n").append("DTSTART:19810329T020000").append("\r\n").append("TZNAME:CEST").append("\r\n").append("TZOFFSETTO:+0200").append("\r\n").append("END:DAYLIGHT").append("\r\n").append("BEGIN:STANDARD").append("\r\n").append("TZOFFSETFROM:+0200").append("\r\n").append("RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU").append("\r\n").append("DTSTART:19961027T030000").append("\r\n").append("TZNAME:CET").append("\r\n").append("TZOFFSETTO:+0100").append("\r\n").append("END:STANDARD").append("\r\n").append("END:VTIMEZONE").append("\r\n").append("BEGIN:VEVENT").append("\r\n").append("CREATED:").append(formatAsUTC(new Date())).append("\r\n");
        if (null != uid) {
            stringBuilder.append("UID:").append(uid).append("\r\n");
        }
        if (null != end) {
            stringBuilder.append("DTEND;TZID=Europe/Amsterdam:").append(format(end, "Europe/Amsterdam")).append("\r\n");
        }
        stringBuilder.append("TRANSP:OPAQUE").append("\r\n");
        if (null != summary) {
            stringBuilder.append("SUMMARY:").append(summary).append("\r\n");
        }
        if (null != location) {
            stringBuilder.append("LOCATION:").append(location).append("\r\n");
        }
        if (null != start) {
            stringBuilder.append("DTSTART;TZID=Europe/Amsterdam:").append(format(start, "Europe/Amsterdam")).append("\r\n");
        }
        stringBuilder.append("DTSTAMP:").append(formatAsUTC(new Date())).append("\r\n").append("SEQUENCE:0").append("\r\n").append("END:VEVENT").append("\r\n").append("END:VCALENDAR").append("\r\n");

        return stringBuilder.toString();
    }

    protected static String generateVTodo(Date start, Date due, String uid, String summary, String location) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("BEGIN:VCALENDAR").append("\r\n").append("VERSION:2.0").append("\r\n").append("PRODID:-//Apple Inc.//iCal 5.0.2//EN").append("\r\n").append("CALSCALE:GREGORIAN").append("\r\n").append("BEGIN:VTIMEZONE").append("\r\n").append("TZID:Europe/Amsterdam").append("\r\n").append("BEGIN:DAYLIGHT").append("\r\n").append("TZOFFSETFROM:+0100").append("\r\n").append("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU").append("\r\n").append("DTSTART:19810329T020000").append("\r\n").append("TZNAME:CEST").append("\r\n").append("TZOFFSETTO:+0200").append("\r\n").append("END:DAYLIGHT").append("\r\n").append("BEGIN:STANDARD").append("\r\n").append("TZOFFSETFROM:+0200").append("\r\n").append("RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU").append("\r\n").append("DTSTART:19961027T030000").append("\r\n").append("TZNAME:CET").append("\r\n").append("TZOFFSETTO:+0100").append("\r\n").append("END:STANDARD").append("\r\n").append("END:VTIMEZONE").append("\r\n").append("BEGIN:VTODO").append("\r\n").append("CREATED:").append(formatAsUTC(new Date())).append("\r\n");
        if (null != uid) {
            stringBuilder.append("UID:").append(uid).append("\r\n");
        }
        if (null != due) {
            stringBuilder.append("DUE;TZID=Europe/Amsterdam:").append(format(due, "Europe/Amsterdam")).append("\r\n");
        }
        stringBuilder.append("TRANSP:OPAQUE").append("\r\n");
        if (null != summary) {
            stringBuilder.append("SUMMARY:").append(summary).append("\r\n");
        }
        if (null != location) {
            stringBuilder.append("LOCATION:").append(location).append("\r\n");
        }
        if (null != start) {
            stringBuilder.append("DTSTART;TZID=Europe/Amsterdam:").append(format(start, "Europe/Amsterdam")).append("\r\n");
        }
        stringBuilder.append("DTSTAMP:").append(formatAsUTC(new Date())).append("\r\n").append("SEQUENCE:0").append("\r\n").append("END:VTODO").append("\r\n").append("END:VCALENDAR").append("\r\n");

        return stringBuilder.toString();
    }

    /**
     * Generates a {@link VAvailability} with one {@link Available} block
     *
     * @param start The start of the available block
     * @param end The end of the available block
     * @param uid The uid
     * @param summary The summary
     * @param location The location
     * @return The {@link VAvailability} as {@link String} iCal
     */
    protected static String generateVAvailability(Date start, Date end, String uid, String summary, String location) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR").append("\r\n").append("VERSION:2.0").append("\r\n");
        sb.append("PRODID:-//Apple Inc.//Mac OS X 10.12.6//EN").append("\r\n").append("CALSCALE:GREGORIAN").append("\r\n");
        sb.append("BEGIN:VTIMEZONE").append("\r\n").append("TZID:Europe/Berlin").append("\r\n");
        sb.append("BEGIN:DAYLIGHT").append("\r\n").append("TZOFFSETFROM:+0100").append("\r\n");
        sb.append("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU").append("\r\n");
        sb.append("DTSTART:19810329T020000").append("\r\n").append("TZNAME:GMT+2").append("\r\n").append("TZOFFSETTO:+0200").append("\r\n");
        sb.append("END:DAYLIGHT").append("\r\n").append("BEGIN:STANDARD").append("\r\n").append("TZOFFSETFROM:+0200").append("\r\n");
        sb.append("RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU").append("\r\n").append("DTSTART:19961027T030000").append("\r\n");
        sb.append("TZNAME:CET").append("\r\n").append("TZOFFSETTO:+0100").append("\r\n").append("END:STANDARD").append("\r\n");
        sb.append("END:VTIMEZONE").append("\r\n");
        sb.append("BEGIN:VAVAILABILITY").append("\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(formatAsUTC(new Date())).append("\r\n").append("\r\n");
        sb.append("SUMMARY:").append(summary).append("\r\n");
        sb.append("LOCATION:").append(location).append("\r\n");
        sb.append("BEGIN:AVAILABLE").append("\r\n");
        sb.append("DTSTART;TZID=Europe/Berlin:").append(format(start, "Europe/Berlin")).append("\r\n");
        sb.append("RRULE:FREQ=WEEKLY;BYDAY=WE").append("\r\n");
        sb.append("DTEND;TZID=Europe/Berlin:").append(format(end, "Europe/Berlin")).append("\r\n");
        sb.append("END:AVAILABLE").append("\r\n");
        sb.append("END:VAVAILABILITY").append("\r\n");
        sb.append("END:VCALENDAR").append("\r\n");

        return sb.toString();
    }

    public static ICalResource assertContains(String uid, Collection<ICalResource> iCalResources) {
        ICalResource match = null;
        for (ICalResource iCalResource : iCalResources) {
            if (uid.equals(iCalResource.getVEvent().getUID())) {
                assertNull("duplicate match for UID '" + uid + "'", match);
                match = iCalResource;
            }
        }
        assertNotNull("no iCal resource with UID '" + uid + "' found", match);
        return match;
    }

    protected static String randomUID() {
        return UUID.randomUUID().toString();
    }

    protected static void assertDummyAlarm(Component component) {
        List<Component> vAlarms = component.getVAlarms();
        Assert.assertEquals("Expected exactly one VAlarm.", 1, vAlarms.size());
        Component vAlarm = vAlarms.get(0);
        Assert.assertEquals("Expected dummy trigger.", "19760401T005545Z", vAlarm.getProperty("TRIGGER").getValue());
        Assert.assertEquals("Expected dummy property.", "TRUE", vAlarm.getProperty("X-APPLE-LOCAL-DEFAULT-ALARM").getValue());
        Assert.assertEquals("Expected dummy property.", "TRUE", vAlarm.getProperty("X-APPLE-DEFAULT-ALARM").getValue());
    }

    protected static void assertAcknowledgedOrDummyAlarm(Component component, String expectedAcknowledged) {
        List<Component> vAlarms = component.getVAlarms();
        Assert.assertEquals("Expected exactly one VAlarm.", 1, vAlarms.size());
        Component vAlarm = vAlarms.get(0);
        if ("19760401T005545Z".equals(vAlarm.getPropertyValue("TRIGGER"))) {
            Assert.assertEquals("Expected dummy trigger", "19760401T005545Z", vAlarm.getProperty("TRIGGER").getValue());
            Assert.assertEquals("Expected dummy property", "TRUE", vAlarm.getProperty("X-APPLE-LOCAL-DEFAULT-ALARM").getValue());
            Assert.assertEquals("Expected dummy property", "TRUE", vAlarm.getProperty("X-APPLE-DEFAULT-ALARM").getValue());
        } else {
            Assert.assertEquals("ACKNOWLEDGED wrong", expectedAcknowledged, vAlarm.getPropertyValue("ACKNOWLEDGED"));
        }
    }

}
