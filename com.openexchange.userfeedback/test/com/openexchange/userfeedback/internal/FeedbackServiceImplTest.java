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

package com.openexchange.userfeedback.internal;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.FeedbackStoreListener;
import com.openexchange.userfeedback.FeedbackType;
import com.openexchange.userfeedback.export.ExportResultConverter;
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
    
    private ServiceSet<FeedbackStoreListener> serviceSet;

    private FeedbackServiceImpl feedbackService;

    private final int userId = 111;

    private final int contextId = 10;

    private final String type = "star-rating-v1";

    private final String hostname = "localhost";

    private JSONObject feedback = null;

    //@formatter:off
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
    //@formatter:on

    @Mock
    private Session session;

    @Mock
    private ServerConfigService serverConfigService;

    @Mock
    private LeanConfigurationService leanConfigurationService;

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

    @Mock
    private FeedbackStoreListener storeListener;

    private Map<String, String> storeParams;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        MockUtils.injectValueIntoPrivateField(FeedbackTypeRegistryImpl.getInstance(), "map", new ConcurrentHashMap<String, FeedbackType>(1));

        PowerMockito.mockStatic(Services.class);

        PowerMockito.when(Services.getService(ServerConfigService.class)).thenReturn(serverConfigService);
        PowerMockito.when(Services.getService(LeanConfigurationService.class)).thenReturn(leanConfigurationService);

        PowerMockito.when(Services.getService(ConfigViewFactory.class)).thenReturn(configViewFactory);
        PowerMockito.when(configViewFactory.getView(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(configView);
        PowerMockito.when(configView.opt("com.openexchange.context.group", String.class, "default")).thenReturn(null);

        PowerMockito.when(Services.getService(DatabaseService.class)).thenReturn(databaseService);
        PowerMockito.when(B(databaseService.isGlobalDatabaseAvailable())).thenReturn(Boolean.TRUE);
        PowerMockito.when(databaseService.getWritableForGlobal(ArgumentMatchers.anyString())).thenReturn(connection);
        PowerMockito.when(databaseService.getWritableForGlobal(ArgumentMatchers.isNull())).thenReturn(connection);

        PowerMockito.when(feedbackType.getType()).thenReturn("star-rating-v1");
        PowerMockito.when(L(feedbackType.storeFeedback(ArgumentMatchers.any(), (Connection) ArgumentMatchers.any()))).thenReturn(L(1L));
        PowerMockito.when(feedbackType.getFeedbacks(ArgumentMatchers.anyList(), (Connection) ArgumentMatchers.any())).thenReturn(resultConverter);
        PowerMockito.when(feedbackType.getFeedbacks(ArgumentMatchers.anyList(), (Connection) ArgumentMatchers.any(), ArgumentMatchers.anyMap())).thenReturn(resultConverter);

        feedback = new JSONObject(validFeedbackStr);

        Mockito.when(I(session.getUserId())).thenReturn(I(userId));
        Mockito.when(I(session.getContextId())).thenReturn(I(contextId));

        storeParams = new HashMap<>();
        storeParams.put("type", type);
        storeParams.put("hostname", hostname);
        
        serviceSet = new ServiceSet<FeedbackStoreListener>();
        feedbackService = new FeedbackServiceImpl(serviceSet);
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
        PowerMockito.when(B(databaseService.isGlobalDatabaseAvailable())).thenReturn(Boolean.FALSE);

        feedbackService.store(session, feedback, storeParams);

        fail();
    }

    @Test(expected = OXException.class)
    public void testStore_feedbackTypeNotAbleToPersist_throwException() throws OXException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        PowerMockito.when(L(feedbackType.storeFeedback(ArgumentMatchers.any(), (Connection) ArgumentMatchers.any()))).thenReturn(L(-1L));

        feedbackService.store(session, feedback, storeParams);

        fail();
    }

    @Test
    public void testStore_feedbackTypeNotAbleToPersist_ensureNotSaved() throws SQLException, OXException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        PowerMockito.when(L(feedbackType.storeFeedback(ArgumentMatchers.any(), (Connection) ArgumentMatchers.any()))).thenReturn(L(-1L));
        feedbackService = Mockito.spy(new FeedbackServiceImpl(serviceSet));
        Mockito.doNothing().when(feedbackService).saveFeedBackInternal((Connection) ArgumentMatchers.any(), (FeedbackMetaData) ArgumentMatchers.any(), ArgumentMatchers.anyString());

        boolean exceptionThrown = false;
        try {
            feedbackService.store(session, feedback, storeParams);
        } catch (OXException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        Mockito.verify(feedbackService, Mockito.never()).saveFeedBackInternal((Connection) ArgumentMatchers.any(), (FeedbackMetaData) ArgumentMatchers.any(), ArgumentMatchers.anyString());
    }

    @Test
    public void testStore_ok_ensureSavedInternally() throws OXException, SQLException {
        PowerMockito.when(leanConfigurationService.getProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.any())).thenReturn("star-rating-v1");

        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        feedbackService = Mockito.spy(new FeedbackServiceImpl(serviceSet));
        Mockito.doNothing().when(feedbackService).saveFeedBackInternal((Connection) ArgumentMatchers.any(), (FeedbackMetaData) ArgumentMatchers.any(), ArgumentMatchers.anyString());

        feedbackService.store(session, feedback, storeParams);

        Mockito.verify(feedbackService, Mockito.times(1)).saveFeedBackInternal((Connection) ArgumentMatchers.any(), (FeedbackMetaData) ArgumentMatchers.any(), ArgumentMatchers.anyString());
    }

    @Test
    public void testStore_onAfterStore() throws OXException, SQLException {
        PowerMockito.when(leanConfigurationService.getProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.any())).thenReturn("star-rating-v1");

        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        
        ServiceSet<FeedbackStoreListener> serviceSetWithFeedbackStoreListener = PowerMockito.spy(new ServiceSet<FeedbackStoreListener>());
        ServiceReference<FeedbackStoreListener> ref = PowerMockito.mock(ServiceReference.class);
        Mockito.when(ref.getProperty(Constants.SERVICE_ID)).thenReturn(L(1));
        Mockito.when(ref.getProperty(Constants.SERVICE_RANKING)).thenReturn(I(1));
        serviceSetWithFeedbackStoreListener.added(ref, storeListener);
        
        feedbackService = Mockito.spy(new FeedbackServiceImpl(serviceSetWithFeedbackStoreListener));
        
        Mockito.doNothing().when(feedbackService).saveFeedBackInternal((Connection) ArgumentMatchers.any(), (FeedbackMetaData) ArgumentMatchers.any(), ArgumentMatchers.anyString());
       
        feedbackService.store(session, feedback, storeParams);

        Mockito.verify(storeListener, Mockito.times(1)).onAfterStore(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
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
        feedbackService = Mockito.spy(new FeedbackServiceImpl(serviceSet));
        Mockito.doReturn(Collections.EMPTY_LIST).when(feedbackService).loadFeedbackMetaData((Connection) ArgumentMatchers.any(), (FeedbackFilter) ArgumentMatchers.any(), ArgumentMatchers.anyString());

        feedbackService.export("default", FeedbackFilter.DEFAULT_FILTER);

        fail();
    }

    @Test(expected = OXException.class)
    public void testExport_globalDatabaseNotConfigured_throwException() throws OXException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        PowerMockito.when(B(databaseService.isGlobalDatabaseAvailable())).thenReturn(Boolean.FALSE);

        feedbackService.export("default", FeedbackFilter.DEFAULT_FILTER);

        fail();
    }

    @Test
    public void testExport_noDataAvailable_returnEmptyResult() throws OXException, SQLException {
        FeedbackTypeRegistryImpl.getInstance().registerType(feedbackType);
        feedbackService = Mockito.spy(new FeedbackServiceImpl(serviceSet));
        Mockito.doReturn(Collections.EMPTY_LIST).when(feedbackService).loadFeedbackMetaData((Connection) ArgumentMatchers.any(), (FeedbackFilter) ArgumentMatchers.any(), ArgumentMatchers.anyString());

        ExportResultConverter export = feedbackService.export("default", FeedbackFilter.DEFAULT_FILTER, Collections.<String, String> emptyMap());

        assertEquals(resultConverter, export);
    }
}
