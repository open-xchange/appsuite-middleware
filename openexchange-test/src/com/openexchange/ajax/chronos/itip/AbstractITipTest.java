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

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.jdom2.IllegalDataException;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.ICalFacotry;
import com.openexchange.ajax.chronos.factory.ITipMailFactory;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ConversionDataSource;
import com.openexchange.testing.httpclient.models.DeleteBody;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailImportResponse;
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

    /** All available data sources for iTIP calls */
    enum DataSources {

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
    enum DescriptionFormat {
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

    private static final String FOLDER_HUMAN_READABLE = "default0/INBOX";

    private static final String FOLDER_MACHINE_READABLE = "default0%2FINBOX";

    private String session;

    protected String mailFromUser;

    protected String mailToUser;

    protected UserResponse userResponseC2;

    protected UserResponse userResponseC1;

    protected ApiClient apiClientC2;

    protected TestUser testUserC2;

    protected TestContext context2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        session = getApiClient().getSession();

        UserApi api = new UserApi(getApiClient());
        userResponseC1 = api.getUser(getApiClient().getSession(), String.valueOf(getClient().getValues().getUserId()));

        context2 = TestContextPool.acquireContext(AbstractITipTest.class.getName());
        testUserC2 = context2.acquireUser();
        apiClientC2 = generateApiClient(testUserC2);
        UserApi anotherUserApi = new UserApi(apiClientC2);
        userResponseC2 = anotherUserApi.getUser(apiClientC2.getSession(), String.valueOf(apiClientC2.getUserId()));
        // Validate
        if (null == userResponseC1 || null == userResponseC2) {
            throw new IllegalDataException("Need both users for iTIP tests!");
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (null != context2) {
            if (null != testUserC2) {
                context2.backUser(testUserC2);
            }
            TestContextPool.backContext(context2);
        }
        if (null != apiClientC2) {
            logoutClient(apiClientC2);
        }
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
        return chronosApi.accept(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
    }

    /**
     * @See {@link ChronosApi#acceptAndIgnoreConflicts(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse acceptAndIgnoreConflicts(ConversionDataSource body) throws ApiException {
        return chronosApi.acceptAndIgnoreConflicts(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
    }

    /**
     * @See {@link ChronosApi#tentative(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse tentative(ConversionDataSource body) throws ApiException {
        return chronosApi.tentative(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
    }

    /**
     * @See {@link ChronosApi#decline(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse decline(ConversionDataSource body) throws ApiException {
        return chronosApi.decline(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
    }

    /**
     * @See {@link ChronosApi#update(String, String, String, ConversionDataSource)}
     */
    protected ActionResponse update(ConversionDataSource body) throws ApiException {
        return chronosApi.update(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body);
    }

    /**
     * @See {@link ChronosApi#analyze(String, String, String, ConversionDataSource, String)}
     */
    protected AnalyzeResponse analyze(ConversionDataSource body) throws ApiException {
        return chronosApi.analyze(session, DataSources.MAIL.getDataSource(), DescriptionFormat.HTML.getFormat(), body, null);
    }

    /**
     * Uploads a mail to the INBOX
     *
     * @param data The event to send iTip mail for
     * @return {@link MailDestinationData} with set mail ID and folder ID
     * @throws Exception In case of error
     */
    protected MailDestinationData createMailInInbox(List<EventData> data) throws Exception {
        return createMailInInbox(new ITipMailFactory(userResponseC2.getData().getEmail1(), userResponseC1.getData().getEmail1(), new ICalFacotry(data).build()).build());
    }

    /**
     * Uploads a mail to the INBOX
     *
     * @param eml The mail to upload
     * @return {@link MailDestinationData} with set mail ID and folder ID
     * @throws Exception In case of error
     */
    protected MailDestinationData createMailInInbox(String eml) throws Exception {
        File tmpFile = File.createTempFile("test", ".eml");
        FileWriterWithEncoding writer = new FileWriterWithEncoding(tmpFile, "ASCII");
        writer.write(eml);
        writer.close();

        MailApi mailApi = new MailApi(getApiClient());
        MailImportResponse importMail = mailApi.importMail(session, FOLDER_MACHINE_READABLE, tmpFile, null, Boolean.TRUE);
        return importMail.getData().get(0);
    }

    protected ConversionDataSource constructBody(String mailId) {
        return constructBody(mailId, "1.3");
    }

    protected ConversionDataSource constructBody(String mailId, String sequenceId) {
        ConversionDataSource body = new ConversionDataSource();
        body.setComOpenexchangeMailConversionFullname(FOLDER_HUMAN_READABLE);
        body.setComOpenexchangeMailConversionMailid(mailId);
        body.setComOpenexchangeMailConversionSequenceid(sequenceId);
        return body;
    }

    protected Attendee createAttendee(TestUser convertee, ApiClient converteeClient) {
        Attendee attendee = AttendeeFactory.createAttendee(converteeClient.getUserId().intValue(), CuTypeEnum.INDIVIDUAL);

        attendee.cn(convertee.getUser());
        attendee.comment("Comment for user " + convertee.getUser());
        attendee.email(convertee.getLogin());
        attendee.setUri("mailto:" + convertee.getLogin());
        return attendee;
    }

    protected void removeMail(MailDestinationData data) throws Exception {
        MailApi mailApi = new MailApi(getApiClient());
        MailListElement elm = new MailListElement();
        elm.setId(data.getId());
        elm.setFolder(data.getFolderId());
        mailApi.deleteMails(getApiClient().getSession(), Collections.singletonList(elm), now());
    }

    protected EventData createEvent(EventData event) throws ApiException {
        return chronosApi.createEvent(session, defaultFolderId, event, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, null, null, Boolean.FALSE).getData().getCreated().get(0);
    }

    protected void deleteEvent(EventData data) throws Exception {
        EventId id = new EventId();
        id.setFolder(data.getFolder());
        id.setId(data.getId());
        DeleteBody body  = new DeleteBody();
        body.addEventsItem(id);
        chronosApi.deleteEvent(session, now(), body, null, null, Boolean.FALSE, Boolean.FALSE);
    }

    private Long now() {
        return Long.valueOf(System.currentTimeMillis());
    }

}
