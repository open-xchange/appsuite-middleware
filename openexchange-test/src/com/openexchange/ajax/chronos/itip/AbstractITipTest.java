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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.exparity.hamcrest.date.DateMatchers;
import org.jdom2.IllegalDataException;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.EnhancedApiClient;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
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

    protected UserResponse userResponseC1;

    protected UserResponse userResponseC2;

    protected ApiClient apiClientC2;

    protected TestUser testUserC2;

    protected TestContext context2;

    protected String folderIdC2;

    protected EventManager eventManagerC2;

    protected EnhancedApiClient enhancedApiClientC2;

    private List<TearDownOperation> operations = new LinkedList<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        UserApi api = new UserApi(getApiClient());
        userResponseC1 = api.getUser(getApiClient().getSession(), String.valueOf(getClient().getValues().getUserId()));

        context2 = TestContextPool.acquireContext(AbstractITipTest.class.getName());
        addTearDownOperation(() -> TestContextPool.backContext(context2));

        testUserC2 = context2.acquireUser();
        addTearDownOperation(() -> {
            if (null != context2) {
                context2.backUser(testUserC2);
            }
        });

        apiClientC2 = generateApiClient(testUserC2);
        rememberClient(apiClientC2);
        UserApi anotherUserApi = new UserApi(apiClientC2);
        userResponseC2 = anotherUserApi.getUser(apiClientC2.getSession(), String.valueOf(apiClientC2.getUserId()));
        // Validate
        if (null == userResponseC1 || null == userResponseC2) {
            throw new IllegalDataException("Need both users for iTIP tests!");
        }

        folderIdC2 = getDefaultFolder(apiClientC2.getSession(), apiClientC2);

        enhancedApiClientC2 = generateEnhancedClient(testUser);
        rememberClient(enhancedApiClientC2);
        eventManagerC2 = new EventManager(new com.openexchange.ajax.chronos.UserApi(apiClientC2, enhancedApiClientC2, testUserC2, false), folderIdC2);
    }

    @Override
    public void tearDown() throws Exception {
        /*
         * Call operations from last added item to first added item (FIFO)
         * to avoid premature closing of e.g. API clients before all relevant
         * operations for this client has been called
         */
        for (int i = operations.size() - 1; i >= 0; i--) {
            operations.get(i).safeTearDown();
        }
        eventManagerC2.cleanUp();
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
    protected ActionResponse accept(ConversionDataSource body, String comment) throws ApiException {
        return accept(apiClient, body, comment);
    }

    /**
     * @See {@link ChronosApi#accept(String,String, String, ConversionDataSource)}
     */
    protected ActionResponse accept(ApiClient apiClient, ConversionDataSource body, String comment) throws ApiException {
        ActionResponse response = new ChronosApi(apiClient).accept(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, comment);
        validateActionResponse(response);
        return response;
    }

    /**
     * @See {@link ChronosApi#acceptAndIgnoreConflicts(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse acceptAndIgnoreConflicts(ConversionDataSource body, String comment) throws ApiException {
        ActionResponse response = chronosApi.acceptAndIgnoreConflicts(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, comment);
        validateActionResponse(response);
        return response;
    }

    /**
     * @See {@link ChronosApi#tentative(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse tentative(ConversionDataSource body, String comment) throws ApiException {
        ActionResponse response = chronosApi.tentative(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, comment);
        validateActionResponse(response);
        return response;
    }

    /**
     * @See {@link ChronosApi#decline(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse decline(ConversionDataSource body, String comment) throws ApiException {
        ActionResponse response = chronosApi.decline(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, comment);
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

    /**
     * @See {@link ChronosApi#cancel(String, String, String, ConversionDataSource)}
     * @param expectData A value indicating whether it is expected that event data is returned or not. If set to <code>false</code> only the timestamp will be checked
     */
    protected ActionResponse cancel(ConversionDataSource body, String comment, boolean expectData) throws ApiException {
        return cancel(apiClient, body, comment, expectData);
    }

    /**
     * @See {@link ChronosApi#cancel(String, String, String, ConversionDataSource)}
     * @param expectData A value indicating whether it is expected that event data is returned or not. If set to <code>false</code> only the timestamp will be checked
     */
    protected ActionResponse cancel(ApiClient apiClient, ConversionDataSource body, String comment, boolean expectData) throws ApiException {
        ActionResponse response = new ChronosApi(apiClient).cancel(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, comment);
        assertThat(response.getTimestamp(), is(not(nullValue())));
        assertThat("Only timestamp should be returned", new Date(response.getTimestamp().longValue()), DateMatchers.within(3, ChronoUnit.SECONDS, new Date()));
        if (expectData) {
            validateActionResponse(response);
        } else {
            assertThat("Only timestamp should be returned", response.getData(), is(empty()));
        }
        return response;
    }

    private void validateActionResponse(ActionResponse response) {
        assertThat("Excpected analyze-data", response.getData(), is(not(empty())));
    }

    /**
     * @See {@link ChronosApi#analyze(String, String, String, ConversionDataSource, String)}
     */
    protected AnalyzeResponse analyze(ConversionDataSource body) throws ApiException {
        return chronosApi.analyze(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, null);
    }

    protected static AnalyzeResponse analyze(ApiClient apiClient, MailData mailData) throws Exception {
        ConversionDataSource body = ITipUtil.constructBody(mailData);
        return new ChronosApi(apiClient).analyze(apiClient.getSession(), DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, null);
    }

    protected Long now() {
        return Long.valueOf(System.currentTimeMillis());
    }

    /*
     * =========================
     * =========CLEANUP=========
     * =========================
     */

    protected void rememberMail(MailData data) {
        rememberMail(getApiClient(), data);
    }

    protected void rememberMail(ApiClient apiClient, MailData data) {
        if (null == apiClient || null == data) {
            return;
        }
        MailApi mailApi = new MailApi(apiClient);
        MailListElement elm = new MailListElement();
        elm.setId(data.getId());
        elm.setFolder(data.getFolderId());
        operations.add(() -> {
            mailApi.deleteMails(apiClient.getSession(), Collections.singletonList(elm), now(), Boolean.TRUE, Boolean.FALSE);
        });
    }

    protected void rememberMail(ApiClient apiClient, MailDestinationData data) {
        if (null == apiClient || null == data) {
            return;
        }
        MailApi mailApi = new MailApi(apiClient);
        MailListElement elm = new MailListElement();
        elm.setId(data.getId());
        elm.setFolder(data.getFolderId());
        operations.add(() -> {
            mailApi.deleteMails(apiClient.getSession(), Collections.singletonList(elm), now(), Boolean.TRUE, Boolean.FALSE);
        });
    }

    protected void rememberForCleanup(EventData eventData) {
        rememberForCleanup(apiClient, eventData);
    }

    protected void rememberForCleanup(ApiClient apiClient, EventData eventData) {
        if (null == apiClient || null == eventData) {
            return;
        }
        EventId eventId = new EventId();
        eventId.setId(eventData.getId());
        eventId.setFolder(eventData.getFolder());

        addTearDownOperation(() -> {
            DeleteEventBody body = new DeleteEventBody();
            body.addEventsItem(eventId);
            new ChronosApi(apiClient).deleteEvent(apiClient.getSession(), now(), body, null, null, Boolean.FALSE, Boolean.FALSE, null, null, "none");
        });
    }

    /**
     * Adds a new {@link TearDownOperation} to call in this classes {@link #tearDown()} method
     * <p>
     * Note: Operations will be remembered in order and will be executed with the first-in first-out (FIFO)
     * principal. Therefore e.g. first add removal the test client afterwards the calendar event to remove.
     *
     * @param operation A {@link TearDownOperation} to execute with {@link TearDownOperation#safeTearDown()}
     */
    protected void addTearDownOperation(TearDownOperation operation) {
        if (null != operation) {
            operations.add(operation);
        }
    }

    @FunctionalInterface
    public interface TearDownOperation {

        void tearDown() throws Exception;

        default void safeTearDown() {
            try {
                tearDown();
            } catch (Exception e) {
                // Ignore
            }
        }

    }
}
