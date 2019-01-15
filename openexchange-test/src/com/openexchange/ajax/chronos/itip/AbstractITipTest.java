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

package com.openexchange.ajax.chronos.itip;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jdom2.IllegalDataException;
import org.junit.Assert;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ConversionDataSource;
import com.openexchange.testing.httpclient.models.DeleteEventBody;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailListElement;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.MailApi;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link AbstractITipTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public abstract class AbstractITipTest extends AbstractChronosTest {

    /** Participant status */
    public static enum PartStat {
        ACCEPTED("ACCEPTED"),
        TENTATIVE("TENTATIVE"),
        DECLINED("DECLINED"),
        NEEDS_ACTION("NEEDS-ACTION");

        protected final String status;

        private PartStat(String status) {
            this.status = status;
        }

        /**
         * Gets the status
         *
         * @return The status
         */
        public String getStatus() {
            return status;
        }

    }

    /** All available data sources for iTIP calls */
    public static enum DataSources {

        /** Currently the only valid input for the field 'dataSoure' on iTIP actions */
        MAIL("com.openexchange.mail.ical");

        private final String source;

        DataSources(String source) {
            this.source = source;
        }

        String getDataSource() {
            return source;
        }
    }

    /** All available output formats for iTIP calls */
    public static enum DescriptionFormat {
        /** Currently the only valid input for the field 'descriptionFormat' on iTIP actions */
        HTML("html");

        private final String format;

        DescriptionFormat(String format) {
            this.format = format;
        }

        String getFormat() {
            return format;
        }
    }

    private String session;

    protected UserResponse userResponseC1;

    protected UserResponse userResponseC2;

    protected ApiClient apiClientC2;

    protected TestUser testUserC2;

    protected TestContext context2;

    protected String folderIdC2;

    private Map<ApiClient, EventId> eventsToDelete = new HashMap<ApiClient, EventId>(3);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        session = getApiClient().getSession();

        UserApi api = new UserApi(getApiClient());
        userResponseC1 = api.getUser(getApiClient().getSession(), String.valueOf(getClient().getValues().getUserId()));

        context2 = TestContextPool.acquireContext(AbstractITipTest.class.getName());
        testUserC2 = context2.acquireUser();
        apiClientC2 = generateApiClient(testUserC2);
        rememberClient(apiClientC2);
        UserApi anotherUserApi = new UserApi(apiClientC2);
        userResponseC2 = anotherUserApi.getUser(apiClientC2.getSession(), String.valueOf(apiClientC2.getUserId()));
        // Validate
        if (null == userResponseC1 || null == userResponseC2) {
            throw new IllegalDataException("Need both users for iTIP tests!");
        }

        folderIdC2 = getDefaultFolder(apiClientC2.getSession(), apiClientC2);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (null != context2) {
                if (null != testUserC2) {
                    context2.backUser(testUserC2);
                }
                TestContextPool.backContext(context2);
            }
            for (Entry<ApiClient, EventId> entry : eventsToDelete.entrySet()) {
                DeleteEventBody body = new DeleteEventBody();
                body.addEventsItem(entry.getValue());
                new ChronosApi(entry.getKey()).deleteEvent(session, now(), body, null, null, Boolean.FALSE, Boolean.FALSE, null, null);
            }
        } catch (Exception e) {
            // Ignore
        }
        super.tearDown();
    }

    /*
     * =========================
     * ========SHORTCUTS========
     * =========================
     */

    /**
     * @See {@link ChronosApi#accept(String,String, String, ConversionDataSource)}
     */
    protected ActionResponse accept(ConversionDataSource body) throws ApiException {
        return accept(apiClient, body);
    }

    /**
     * @See {@link ChronosApi#accept(String,String, String, ConversionDataSource)}
     */
    protected ActionResponse accept(ApiClient apiClient, ConversionDataSource body) throws ApiException {
        ActionResponse response = new ChronosApi(apiClient).accept(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
        validateActionResponse(response);
        return response;
    }

    /**
     * @See {@link ChronosApi#acceptAndIgnoreConflicts(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse acceptAndIgnoreConflicts(ConversionDataSource body) throws ApiException {
        ActionResponse response = chronosApi.acceptAndIgnoreConflicts(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
        validateActionResponse(response);
        return response;
    }

    /**
     * @See {@link ChronosApi#tentative(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse tentative(ConversionDataSource body) throws ApiException {
        ActionResponse response = chronosApi.tentative(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
        validateActionResponse(response);
        return response;
    }

    /**
     * @See {@link ChronosApi#decline(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse decline(ConversionDataSource body) throws ApiException {
        ActionResponse response = chronosApi.decline(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
        validateActionResponse(response);
        return response;
    }

    /**
     * @See {@link ChronosApi#update(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse update(ConversionDataSource body) throws ApiException {
        return update(apiClient, body);
    }

    /**
     * @See {@link ChronosApi#update(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse update(ApiClient apiClient, ConversionDataSource body) throws ApiException {
        ActionResponse response = new ChronosApi(apiClient).update(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
        validateActionResponse(response);
        return response;
    }

    private void validateActionResponse(ActionResponse response) {
        Assert.assertThat("Excpected analyze-data", response.getData(), is(not(empty())));
    }

    /**
     * @See {@link ChronosApi#analyze(String, String, String, ConversionDataSource, String)}
     */
    protected AnalyzeResponse analyze(ConversionDataSource body) throws ApiException {
        return chronosApi.analyze(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, null);
    }

    protected static AnalyzeResponse analyze(ApiClient apiClient, MailData mailData) throws Exception {
        ConversionDataSource body = ITipUtil.constructBody(mailData);
        return new ChronosApi(apiClient).analyze(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, null);
    }

    protected void removeMail(MailData data) throws Exception {
        removeMail(getApiClient(), data);
    }

    protected void removeMail(ApiClient apiClient, MailData data) throws Exception {
        MailApi mailApi = new MailApi(apiClient);
        MailListElement elm = new MailListElement();
        elm.setId(data.getId());
        elm.setFolder(data.getFolderId());
        mailApi.deleteMails(getApiClient().getSession(), Collections.singletonList(elm), now());
    }

    protected void removeMail(ApiClient apiClient, MailDestinationData data) throws Exception {
        MailApi mailApi = new MailApi(apiClient);
        MailListElement elm = new MailListElement();
        elm.setId(data.getId());
        elm.setFolder(data.getFolderId());
        mailApi.deleteMails(getApiClient().getSession(), Collections.singletonList(elm), now());
    }

    protected EventData createEvent(EventData event) throws ApiException {
        EventData createEvent = createEvent(apiClient, event, defaultFolderId);
        rememberForCleanup(createEvent);
        return createEvent;
    }

    protected EventData createEvent(ApiClient apiClient, EventData event, String folderId) throws ApiException {
        ChronosCalendarResultResponse response = new ChronosApi(apiClient).createEvent(apiClient.getSession(), folderId, event, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, null, null, null, Boolean.FALSE, null);
        assertNotNull(response);
        assertNull(response.getError());
        return response.getData().getCreated().get(0);
    }

    protected void deleteEvent(EventData data) throws Exception {
        deleteEvent(apiClient, data);
    }

    protected void deleteEvent(ApiClient apiClient, EventData data) throws Exception {
        EventId id = new EventId();
        id.setFolder(data.getFolder());
        id.setId(data.getId());
        DeleteEventBody body = new DeleteEventBody();
        body.addEventsItem(id);
        new ChronosApi(apiClient).deleteEvent(session, now(), body, null, null, Boolean.FALSE, Boolean.FALSE, null, null);
    }

    protected Long now() {
        return Long.valueOf(System.currentTimeMillis());
    }

    protected void rememberForCleanup(EventData eventData) {
        if (null != eventData) {
            EventId eventId = new EventId();
            eventId.setId(eventData.getId());
            eventId.setFolder(eventData.getFolder());
            rememberEventId(eventId);
        }
    }

    protected void rememberForCleanup(ApiClient apiClient, EventData eventData) {
        if (null != eventData) {
            EventId eventId = new EventId();
            eventId.setId(eventData.getId());
            eventId.setFolder(eventData.getFolder());
            eventsToDelete.put(apiClient, eventId);
        }
    }

}
