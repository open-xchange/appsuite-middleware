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

package com.openexchange.mail.loginhandler;

import java.sql.Connection;
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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;

/**
 * {@link SpamConfigurationHandlerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ UserSettingMailStorage.class })
public class SpamConfigurationHandlerTest {

    private SpamConfigurationHandler spamEnabledHandler;

    private UserSettingMailStorage userSettingMailStorage;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    private UserImpl user;

    private ContextImpl context;

    private UserSettingMail userSettingMail;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.when(this.configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(configView);
        spamEnabledHandler = new SpamConfigurationHandler(configViewFactory);

        user = new UserImpl();
        user.setId(0);
        context = new ContextImpl(1);
        userSettingMail = new UserSettingMail(user.getId(), context.getContextId());

        PowerMockito.mockStatic(UserSettingMailStorage.class);
        userSettingMailStorage = PowerMockito.mock(UserSettingMailStorage.class);
        Mockito.when(UserSettingMailStorage.getInstance()).thenReturn(userSettingMailStorage);
        Mockito.when(userSettingMailStorage.loadUserSettingMail(user.getId(), context)).thenReturn(userSettingMail);
        Mockito.when(userSettingMailStorage.loadUserSettingMail(user.getId(), context, null)).thenReturn(userSettingMail);
        Mockito.doNothing().when(userSettingMailStorage).saveUserSettingMail((UserSettingMail) Matchers.any(), Matchers.anyInt(), (Context) Matchers.any(), (Connection) Matchers.any());
    }

    @Test
    public void testHandleLogin_loginResultNull_abort() throws OXException {
        spamEnabledHandler.handleLogin(null);

        Mockito.verify(userSettingMailStorage, Mockito.never()).loadUserSettingMail(user.getId(), context);
        Mockito.verify(userSettingMailStorage, Mockito.never()).loadUserSettingMail(user.getId(), context, null);
        Mockito.verify(userSettingMailStorage, Mockito.never()).getUserSettingMail(user.getId(), context);
        Mockito.verify(userSettingMailStorage, Mockito.never()).getUserSettingMail(user.getId(), context.getContextId());
        verifyNotUpdated();
    }

    @Test
    public void testHandleLogin_loginResultUserNull_abort() throws OXException {
        LoginResultImpl resultImpl = new LoginResultImpl();
        resultImpl.setContext(context);

        spamEnabledHandler.handleLogin(resultImpl);

        Mockito.verify(userSettingMailStorage, Mockito.never()).loadUserSettingMail(Matchers.anyInt(), (Context) Matchers.any());
        Mockito.verify(userSettingMailStorage, Mockito.never()).loadUserSettingMail(Matchers.anyInt(), (Context) Matchers.any(), (Connection) Matchers.any());
        Mockito.verify(userSettingMailStorage, Mockito.never()).getUserSettingMail(user.getId(), context);
        Mockito.verify(userSettingMailStorage, Mockito.never()).getUserSettingMail(user.getId(), context.getContextId());
        verifyNotUpdated();
    }

    @Test
    public void testHandleLogin_loginResultContextNull_abort() throws OXException {
        LoginResultImpl resultImpl = new LoginResultImpl();
        resultImpl.setUser(user);

        spamEnabledHandler.handleLogin(resultImpl);

        Mockito.verify(userSettingMailStorage, Mockito.never()).loadUserSettingMail(Matchers.anyInt(), (Context) Matchers.any());
        Mockito.verify(userSettingMailStorage, Mockito.never()).loadUserSettingMail(Matchers.anyInt(), (Context) Matchers.any(), (Connection) Matchers.any());
        Mockito.verify(userSettingMailStorage, Mockito.never()).getUserSettingMail(user.getId(), context);
        Mockito.verify(userSettingMailStorage, Mockito.never()).getUserSettingMail(user.getId(), context.getContextId());
        verifyNotUpdated();
    }

    @Test
    public void testHandleLogin_enabledSpamViaBit_doNotChange() throws OXException {
        userSettingMail.parseBits(512775); // enabled spam

        LoginResultImpl resultImpl = new LoginResultImpl();
        resultImpl.setUser(user);
        resultImpl.setContext(context);

        spamEnabledHandler.handleLogin(resultImpl);

        Mockito.verify(configViewFactory, Mockito.never()).getView(user.getId(), context.getContextId());
        verifyNotUpdated();
    }

    @Test
    public void testHandleLogin_disabledBitButNoConfigCascadeValue_doNotChange() throws OXException {
        userSettingMail.parseBits(508679); // disabled spam

        LoginResultImpl resultImpl = new LoginResultImpl();
        resultImpl.setUser(user);
        resultImpl.setContext(context);

        spamEnabledHandler.handleLogin(resultImpl);

        verifyNotUpdated();
    }

    @Test
    public void testHandleLogin_disabledBitButConfigCascadeAlsoFalse_doNotChange() throws OXException {
        userSettingMail.parseBits(508679); // disabled spam
        Mockito.when(configView.get(SpamConfigurationHandler.SPAM_ENABLED, Boolean.class)).thenReturn(Boolean.FALSE);

        LoginResultImpl resultImpl = new LoginResultImpl();
        resultImpl.setUser(user);
        resultImpl.setContext(context);

        spamEnabledHandler.handleLogin(resultImpl);

        verifyNotUpdated();
    }

    @Test
    public void testHandleLogin_disabledBitButConfigCascadeAlsoTrue_doChange() throws OXException {
        userSettingMail.parseBits(508679); // disabled spam
        Mockito.when(configView.get(SpamConfigurationHandler.SPAM_ENABLED, Boolean.class)).thenReturn(Boolean.TRUE);

        LoginResultImpl resultImpl = new LoginResultImpl();
        resultImpl.setUser(user);
        resultImpl.setContext(context);

        spamEnabledHandler.handleLogin(resultImpl);

        Mockito.verify(userSettingMailStorage, Mockito.times(1)).saveUserSettingMail((UserSettingMail) Matchers.any(), Matchers.anyInt(), (Context) Matchers.any());
    }

    private void verifyNotUpdated() throws OXException {
        Mockito.verify(userSettingMailStorage, Mockito.never()).saveUserSettingMail((UserSettingMail) Matchers.any(), Matchers.anyInt(), (Context) Matchers.any());
        Mockito.verify(userSettingMailStorage, Mockito.never()).saveUserSettingMail((UserSettingMail) Matchers.any(), Matchers.anyInt(), (Context) Matchers.any(), (Connection) Matchers.any());
    }

}
