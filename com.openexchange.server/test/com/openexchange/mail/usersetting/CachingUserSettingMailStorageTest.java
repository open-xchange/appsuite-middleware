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

package com.openexchange.mail.usersetting;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        PowerMockito.when(serverServiceRegistry.getService(CacheService.class)).thenReturn(cacheService);
        PowerMockito.when(configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(configView);
        PowerMockito.when(configView.property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class)).thenReturn(composedConfigProperty);
        PowerMockito.when(composedConfigProperty.isDefined()).thenReturn(Boolean.FALSE);
    }

    @Test
    public void testApplyConfigCascadeSettings_configViewFactoryNotAvailable_doNothing() throws OXException {
        CachingUserSettingMailStorage storage = new CachingUserSettingMailStorage();
        PowerMockito.when(serverServiceRegistry.getService(ConfigViewFactory.class)).thenReturn(null);

        storage.applyConfigCascadeSettings(userSettingMail, user.getId(), context);

        Mockito.verify(configViewFactory, Mockito.never()).getView(Matchers.anyInt(), Matchers.anyInt());
    }

    @Test
    public void testApplyConfigCascadeSettings_configViewNotAvailable_doNothing() throws OXException {
        CachingUserSettingMailStorage storage = new CachingUserSettingMailStorage();
        PowerMockito.when(configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(null);

        storage.applyConfigCascadeSettings(userSettingMail, user.getId(), context);

        Mockito.verify(configViewFactory, Mockito.times(1)).getView(Matchers.anyInt(), Matchers.anyInt());
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
        PowerMockito.when(composedConfigProperty.isDefined()).thenReturn(Boolean.TRUE);
        PowerMockito.when(composedConfigProperty.get()).thenReturn(Boolean.FALSE);

        PowerMockito.doNothing().when(storageSpy).saveUserSettingMail((UserSettingMail) Matchers.any(), Matchers.anyInt(), (Context) Matchers.any());
        userSettingMail.setSpamEnabled(true);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(storageSpy, Mockito.times(1)).saveUserSettingMail(userSettingMail, user.getId(), context);
        assertFalse(userSettingMail.isSpamOptionEnabled());
    }

    @Test
    public void testUpdateSpamSetting_setViaConfigCascade_updateToNewConfig2() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        PowerMockito.when(composedConfigProperty.isDefined()).thenReturn(Boolean.TRUE);
        PowerMockito.when(composedConfigProperty.get()).thenReturn(Boolean.TRUE);
        PowerMockito.doNothing().when(storageSpy).saveUserSettingMail((UserSettingMail) Matchers.any(), Matchers.anyInt(), (Context) Matchers.any());
        userSettingMail.setSpamEnabled(false);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(storageSpy, Mockito.times(1)).saveUserSettingMail(userSettingMail, user.getId(), context);
        assertTrue(userSettingMail.isSpamOptionEnabled());
    }

    @Test
    public void testUpdateSpamSetting_newValueEqual_doNotUpdate() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        PowerMockito.doNothing().when(storageSpy).saveUserSettingMail((UserSettingMail) Matchers.any(), Matchers.anyInt(), (Context) Matchers.any());
        userSettingMail.setSpamEnabled(false);
        PowerMockito.when(composedConfigProperty.isDefined()).thenReturn(Boolean.TRUE);
        PowerMockito.when(composedConfigProperty.get()).thenReturn(Boolean.FALSE);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(configView, Mockito.times(1)).property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class);
        Mockito.verify(storageSpy, Mockito.never()).saveUserSettingMail(userSettingMail, user.getId(), context);
        assertFalse(userSettingMail.isSpamOptionEnabled());
    }

    @Test
    public void testUpdateSpamSetting_newValueEqual_doNotUpdate2() throws OXException {
        CachingUserSettingMailStorage storageSpy = PowerMockito.spy(new CachingUserSettingMailStorage());
        PowerMockito.doNothing().when(storageSpy).saveUserSettingMail((UserSettingMail) Matchers.any(), Matchers.anyInt(), (Context) Matchers.any());
        userSettingMail.setSpamEnabled(true);
        PowerMockito.when(composedConfigProperty.isDefined()).thenReturn(Boolean.TRUE);
        PowerMockito.when(composedConfigProperty.get()).thenReturn(Boolean.TRUE);

        storageSpy.updateSpamSetting(userSettingMail, user.getId(), context, configView);

        Mockito.verify(configView, Mockito.times(1)).property(CachingUserSettingMailStorage.SPAM_ENABLED, Boolean.class);
        Mockito.verify(storageSpy, Mockito.never()).saveUserSettingMail(userSettingMail, user.getId(), context);
        assertTrue(userSettingMail.isSpamOptionEnabled());
    }
}
