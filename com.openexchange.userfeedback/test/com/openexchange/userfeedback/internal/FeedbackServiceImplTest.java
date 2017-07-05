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

package com.openexchange.userfeedback.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.FeedbackType;
import com.openexchange.userfeedback.filter.FeedbackFilter;
import com.openexchange.userfeedback.osgi.Services;

/**
 * {@link FeedbackServiceImplTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, FeedbackTypeRegistryImpl.class })
public class FeedbackServiceImplTest {

    private FeedbackServiceImpl feedbackService = new FeedbackServiceImpl();

    private int userId = 111;

    private int contextId = 10;

    private String type = "star-rating-v1";

    private String hostname = "localhost";

    private JSONObject feedback = null;

    // @formatter:off
    private final String validFeedbackStr = new String("{ " +
        "\"score\":\"3\","+
        "\"app\":\"app\","+
        "\"entry_point\":\"entry\","+
        "\"Comment\": \"s\u00FC\u00DFer die gl\u00F6cken nie klingen !|\u00A7$%&/()=?@\","+
        "\"Operating_System\": \"Mac OS X 10.10\","+
        "\"Browser\":\"Chrome\","+
        "\"Browser_version\": \"77.0\","+
        "\"User_agent\": \"Chrome/55.0.2883.87\","+
        "\"Screen_Resolution\":\"1600x900\","+
        "\"Language\": \"de_de\""+
        "}");

    @Mock
    private Session session;

    @Mock
    private ServerConfigService serverConfigService;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    @Mock
    private FeedbackType feedbackType;

    @Mock
    private DatabaseService databaseService;

    @Mock
    private Connection connection;
    
    @Mock
    private ExportResultConverter resultConverter;

    private Map<String, String> storeParams;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        MockUtils.injectValueIntoPrivateField(FeedbackTypeRegistryImpl.getInstance(), "map", new ConcurrentHashMap<String, FeedbackType>(1));

        PowerMockito.mockStatic(Services.class);
        
        PowerMockito.when(Services.getService(ServerConfigService.class)).thenReturn(serverConfigService);
        
        PowerMockito.when(Services.getService(ConfigViewFactory.class)).thenReturn(configViewFactory);
        PowerMockito.when(configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(configView);
        PowerMockito.when(configView.opt("com.openexchange.context.group", String.class, "default")).thenReturn(null);
        
        PowerMockito.when(Services.getService(DatabaseService.class)).thenReturn(databaseService);
        PowerMockito.when(databaseService.isGlobalDatabaseAvailable()).thenReturn(true);
        PowerMockito.when(databaseService.getWritableForGlobal(Matchers.anyString())).thenReturn(connection);

        PowerMockito.when(feedbackType.getType()).thenReturn("star-rating-v1");
        PowerMockito.when(feedbackType.storeFeedback(Matchers.any(), (Connection)Matchers.any())).thenReturn(1L);
        PowerMockito.when(feedbackType.getFeedbacks(Matchers.anyList(), (Connection)Matchers.any())).thenReturn(resultConverter);
        PowerMockito.when(feedbackType.getFeedbacks(Matchers.anyList(), (Connection)Matchers.any(), Matchers.anyMap())).thenReturn(resultConverter);

        feedback = new JSONObject(validFeedbackStr);

        Mockito.when(session.getUserId()).thenReturn(userId);
        Mockito.when(session.getContextId()).thenReturn(contextId);
        
        storeParams = new HashMap<>();
        storeParams.put("type", type);
        storeParams.put("hostname", hostname);
    }

    @Test(expected = OXException.class)
    public void testStore_feedbackNull_throwException() throws OXException {
        feedbackService.store(session, null, storeParams);

        fail();
    }

    @Test(expected = OXException.class)
    public void testStore_storeParamsNull_throwException() throws OXException {
        feedbackService.store(session, feedback, storeParams);

        fail();
    }

    @Test(expected = OXException.class)
    public void testStore_configViewNotAvailable_throwException() throws OXException {
        PowerMockito.when(Services.getService(ConfigViewFactory.class)).thenReturn(null);

        feedbackService.store(session, feedback, storeParams);

        fail();
    }

    @Test(expected = OXException.class)
    public void testStore_feedbackTypeNotRegistered_throwException() throws OXException {
        feedbackService.store(session, feedback, storeParams);

        fail();
    }

    @Test(expected = OXException.class)
    public void testStore_globalDatabaseNotConfigured_throwException() throws OXException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        PowerMockito.when(databaseService.isGlobalDatabaseAvailable()).thenReturn(false);
        
        feedbackService.store(session, feedback, storeParams);

        fail();
    }

    @Test(expected = OXException.class)
    public void testStore_feedbackTypeNotAbleToPersist_throwException() throws OXException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        PowerMockito.when(feedbackType.storeFeedback(Matchers.any(), (Connection)Matchers.any())).thenReturn(-1L);

        feedbackService.store(session, feedback, storeParams);

        fail();
    }

    @Test
    public void testStore_feedbackTypeNotAbleToPersist_ensureNotSaved() throws SQLException, OXException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        PowerMockito.when(feedbackType.storeFeedback(Matchers.any(), (Connection)Matchers.any())).thenReturn(-1L);
        feedbackService = Mockito.spy(new FeedbackServiceImpl());
        Mockito.doNothing().when(feedbackService).saveFeedBackInternal((Connection)Matchers.any(), (FeedbackMetaData)Matchers.any(), Matchers.anyString());

        boolean exceptionThrown = false;
        try {
            feedbackService.store(session, feedback, storeParams);
        } catch (OXException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        Mockito.verify(feedbackService, Mockito.never()).saveFeedBackInternal((Connection)Matchers.any(), (FeedbackMetaData)Matchers.any(), Matchers.anyString());
    }

    @Test
    public void testStore_ok_ensureSavedInternally() throws OXException, SQLException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        feedbackService = Mockito.spy(new FeedbackServiceImpl());
        Mockito.doNothing().when(feedbackService).saveFeedBackInternal((Connection)Matchers.any(), (FeedbackMetaData)Matchers.any(), Matchers.anyString());

        feedbackService.store(session, feedback, storeParams);

        Mockito.verify(feedbackService, Mockito.times(1)).saveFeedBackInternal((Connection)Matchers.any(), (FeedbackMetaData)Matchers.any(), Matchers.anyString());
    }
    
    @Test(expected = OXException.class)
    public void testExport_contextTypeNull_throwException() throws OXException {
        feedbackService.export(null, FeedbackFilter.DEFAULT_FILTER);

        fail();
    }

    @Test(expected = OXException.class)
    public void testExport_contextTypeEmpty_throwException() throws OXException {
        feedbackService.export("", FeedbackFilter.DEFAULT_FILTER);

        fail();
    }

    @Test(expected = OXException.class)
    public void testExport_filterNull_throwException() throws OXException {
        feedbackService.export("default", null);

        fail();
    }

    @Test(expected = OXException.class)
    public void testExport_feedbackTypeNotRegistered_throwException() throws OXException, SQLException {
        feedbackService = Mockito.spy(new FeedbackServiceImpl());
        Mockito.doReturn(Collections.EMPTY_LIST).when(feedbackService).loadFeedbackMetaData((Connection)Matchers.any(), (FeedbackFilter)Matchers.any(), Matchers.anyString());

        feedbackService.export("default", FeedbackFilter.DEFAULT_FILTER);

        fail();
    }

    @Test(expected = OXException.class)
    public void testExport_globalDatabaseNotConfigured_throwException() throws OXException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        PowerMockito.when(databaseService.isGlobalDatabaseAvailable()).thenReturn(false);
        
        feedbackService.export("default", FeedbackFilter.DEFAULT_FILTER);

        fail();
    }

    @Test
    public void testExport_noDataAvailable_returnEmptyResult() throws OXException, SQLException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        feedbackService = Mockito.spy(new FeedbackServiceImpl());
        Mockito.doReturn(Collections.EMPTY_LIST).when(feedbackService).loadFeedbackMetaData((Connection)Matchers.any(), (FeedbackFilter)Matchers.any(), Matchers.anyString());
        
        ExportResultConverter export = feedbackService.export("default", FeedbackFilter.DEFAULT_FILTER,  Collections.<String, String> emptyMap());
        
        assertEquals(resultConverter, export);
    }
}
