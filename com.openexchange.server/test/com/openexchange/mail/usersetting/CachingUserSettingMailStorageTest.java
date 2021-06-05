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

package com.openexchange.mail.usersetting;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link CachingUserSettingMailStorageTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServerServiceRegistry.class, CachingUserSettingMailStorage.class })
public class CachingUserSettingMailStorageTest {

    @Mock
    private ServerServiceRegistry serverServiceRegistry;

    @Mock
    private CacheService cacheService;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    @Mock
    private ComposedConfigProperty<Boolean> composedConfigProperty;

    private UserSettingMail userSettingMail;

    private UserImpl user;

    private ContextImpl context;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        user = new UserImpl();
        user.setId(0);
        context = new ContextImpl(1);
        userSettingMail = new UserSettingMail(user.getId(), context.getContextId());

        PowerMockito.mockStatic(ServerServiceRegistry.class);
        PowerMockito.when(ServerServiceRegistry.getInstance()).thenReturn(serverServiceRegistry);
        PowerMockito.when(serverServiceRegistry.getService(ConfigViewFactory.class)).thenReturn(configViewFactory);
        PowerMockito.when(serverServiceRegistry.getService(CacheService.class, true)).thenReturn(cacheService);
        PowerMockito.when(configViewFactory.getView(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(configView);
        PowerMockito.when(configView.property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class)).thenReturn(composedConfigProperty);
        PowerMockito.when(B(composedConfigProperty.isDefined())).thenReturn(Boolean.FALSE);
    }

    @Test
    public void testApplyConfigCascadeSettings_configViewFactoryNotAvailable_doNothing() throws OXException {
        CachingUserSettingMailStorage storage = new CachingUserSettingMailStorage();
        PowerMockito.when(serverServiceRegistry.getService(ConfigViewFactory.class)).thenReturn(null);

        storage.applyConfigCascadeSettings(userSettingMail, user.getId(), context);

        Mockito.verify(configViewFactory, Mockito.never()).getView(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
    }

    @Test
    public void testApplyConfigCascadeSettings_configViewNotAvailable_doNothing() throws OXException {
        CachingUserSettingMailStorage storage = new CachingUserSettingMailStorage();
        PowerMockito.when(configViewFactory.getView(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(null);

        storage.applyConfigCascadeSettings(userSettingMail, user.getId(), context);

        Mockito.verify(configViewFactory, Mockito.times(1)).getView(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
        Mockito.verify(configView, Mockito.never()).get(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class);
        Mockito.verify(configView, Mockito.never()).property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class);
    }

    @Test
    public void testUpdateSpamSetting_notSetViaConfigCascade_doNothing() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        userSettingMail.setSpamEnabled(true);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(storageSpy, Mockito.never()).saveUserSettingMail(userSettingMail, user.getId(), context);
        Mockito.verify(configView, Mockito.times(1)).property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class);
        assertTrue(userSettingMail.isSpamOptionEnabled());
    }

    @Test
    public void testUpdateSpamSetting_notSetViaConfigCascade_doNothing2() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        userSettingMail.setSpamEnabled(false);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(storageSpy, Mockito.never()).saveUserSettingMail(userSettingMail, user.getId(), context);
        Mockito.verify(configView, Mockito.times(1)).property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class);
        assertFalse(userSettingMail.isSpamOptionEnabled());
    }

    @Test
    public void testUpdateSpamSetting_setViaConfigCascade_updateToNewConfig() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        PowerMockito.when(B(composedConfigProperty.isDefined())).thenReturn(Boolean.TRUE);
        PowerMockito.when(composedConfigProperty.get()).thenReturn(Boolean.FALSE);

        PowerMockito.doNothing().when(storageSpy).saveUserSettingMail((UserSettingMail) ArgumentMatchers.any(), ArgumentMatchers.anyInt(), (Context) ArgumentMatchers.any());
        userSettingMail.setSpamEnabled(true);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(storageSpy, Mockito.times(1)).saveUserSettingMail(userSettingMail, user.getId(), context);
        assertFalse(userSettingMail.isSpamOptionEnabled());
    }

    @Test
    public void testUpdateSpamSetting_setViaConfigCascade_updateToNewConfig2() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        PowerMockito.when(B(composedConfigProperty.isDefined())).thenReturn(Boolean.TRUE);
        PowerMockito.when(composedConfigProperty.get()).thenReturn(Boolean.TRUE);
        PowerMockito.doNothing().when(storageSpy).saveUserSettingMail((UserSettingMail) ArgumentMatchers.any(), ArgumentMatchers.anyInt(), (Context) ArgumentMatchers.any());
        userSettingMail.setSpamEnabled(false);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(storageSpy, Mockito.times(1)).saveUserSettingMail(userSettingMail, user.getId(), context);
        assertTrue(userSettingMail.isSpamOptionEnabled());
    }

    @Test
    public void testUpdateSpamSetting_newValueEqual_doNotUpdate() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        PowerMockito.doNothing().when(storageSpy).saveUserSettingMail((UserSettingMail) ArgumentMatchers.any(), ArgumentMatchers.anyInt(), (Context) ArgumentMatchers.any());
        userSettingMail.setSpamEnabled(false);
        PowerMockito.when(B(composedConfigProperty.isDefined())).thenReturn(Boolean.TRUE);
        PowerMockito.when(composedConfigProperty.get()).thenReturn(Boolean.FALSE);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(configView, Mockito.times(1)).property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class);
        Mockito.verify(storageSpy, Mockito.never()).saveUserSettingMail(userSettingMail, user.getId(), context);
        assertFalse(userSettingMail.isSpamOptionEnabled());
    }

    @Test
    public void testUpdateSpamSetting_newValueEqual_doNotUpdate2() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        PowerMockito.doNothing().when(storageSpy).saveUserSettingMail((UserSettingMail) ArgumentMatchers.any(), ArgumentMatchers.anyInt(), (Context) ArgumentMatchers.any());
        userSettingMail.setSpamEnabled(true);
        PowerMockito.when(B(composedConfigProperty.isDefined())).thenReturn(Boolean.TRUE);
        PowerMockito.when(composedConfigProperty.get()).thenReturn(Boolean.TRUE);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(configView, Mockito.times(1)).property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class);
        Mockito.verify(storageSpy, Mockito.never()).saveUserSettingMail(userSettingMail, user.getId(), context);
        assertTrue(userSettingMail.isSpamOptionEnabled());
    }
}
