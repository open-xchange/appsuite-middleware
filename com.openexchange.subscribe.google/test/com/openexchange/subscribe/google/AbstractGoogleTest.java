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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.subscribe.google;

import java.util.LinkedList;
import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.google.GoogleOAuthServiceMetaData;
import com.openexchange.session.Session;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.google.mocks.MockConfigurationService;
import com.openexchange.subscribe.google.mocks.MockFolderUpdateService;
import com.openexchange.subscribe.google.mocks.MockFolderUpdaterRegistry;
import com.openexchange.subscribe.google.mocks.MockServiceLookup;
import com.openexchange.subscribe.google.mocks.MockThreadPoolService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.SimServerSession;


/**
 * {@link AbstractGoogleTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({GoogleApiClients.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public abstract class AbstractGoogleTest extends TestCase {

    private static final String REDIRECT_URL = "";
    private static final String GOOGLE_API_KEY = "";
    private static final String GOOGLE_API_SECRET = "";
    private static final String ACCESS_TOKEN = "";

    private Subscription subscription;
    private SubscribeService subscribeService;
    private OAuthServiceMetaData oasdm;
    private MockServiceLookup sl;

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        //Mocks
        ConfigurationService cs = new MockConfigurationService(GOOGLE_API_KEY, GOOGLE_API_SECRET, REDIRECT_URL);
        ThreadPoolService tps = new MockThreadPoolService();
        FolderUpdaterRegistry fur = new MockFolderUpdaterRegistry<Object>(new MockFolderUpdateService());
        sl = new MockServiceLookup(cs, tps, fur);

        oasdm = new GoogleOAuthServiceMetaData(sl);

        SimServerSession simServer = new SimServerSession(1, 1);
        subscription = new Subscription();
        subscription.setSession(simServer);

        prepareMockitoMocks();

        prepareAdditionalMocks();
    }

    protected LinkedList<CalendarDataObject> getGoogleCalendarObjects() throws OXException {
        subscribeService = new GoogleCalendarSubscribeService(oasdm, sl);
        return (LinkedList<CalendarDataObject>) subscribeService.getContent(subscription);
    }

    protected LinkedList<Contact> getGoogleContacts() throws OXException {
        subscribeService = new GoogleContactSubscribeService(oasdm, sl);
        return (LinkedList<Contact>) subscribeService.getContent(subscription);
    }

    private void prepareMockitoMocks() throws Exception {
        NetHttpTransport transport = new NetHttpTransport.Builder().doNotValidateCertificate().build();
        JsonFactory jsonFactory = new JacksonFactory();

        GoogleCredential credential = new GoogleCredential.Builder()
        .setTransport(transport)
        .setJsonFactory(jsonFactory).build();
        credential.setAccessToken(ACCESS_TOKEN).setRefreshToken(null);

        PowerMockito.mockStatic(GoogleApiClients.class);
        PowerMockito.doReturn(credential).when(GoogleApiClients.class, "getCredentials", Matchers.any(Session.class));
    }

    protected void prepareAdditionalMocks() throws Exception {

    }
}
