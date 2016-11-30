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

package com.openexchange.net.ssl.config.impl.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.user.UserService;

/**
 * {@link UserAwareSSLConfigurationImplTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class UserAwareSSLConfigurationImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ContextService contextService;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    @Mock
    private ComposedConfigProperty<Boolean> booleanProp;

    @Mock
    private Context context;

    private UserAwareSSLConfigurationService userAwareSSLConfigurationService;

    private int userId = 111;

    private int contextId = 2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.when(this.configViewFactory.getView(userId, contextId)).thenReturn(configView);
        Mockito.when(this.configView.property("com.openexchange.net.ssl.user.configuration.enabled", Boolean.class)).thenReturn(booleanProp);
        Mockito.when(this.contextService.getContext(contextId)).thenReturn(this.context);
        Mockito.when(this.context.getContextId()).thenReturn(this.contextId);
    }

    @Test
    public void testIsAllowedToDefineTrustLevel_contextIdInvalid_returnFalse() {
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory);

        boolean trustAll = this.userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(userId, 0);

        assertFalse(trustAll);
    }

    @Test
    public void testIsAllowedToDefineTrustLevel_userIdInvalid_returnFalse() {
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory);

        boolean trustAll = this.userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(0, contextId);

        assertFalse(trustAll);
    }

    @Test
    public void testIsAllowedToDefineTrustLevel_propertyNotAvailabled_returnFalse() throws OXException {
        Mockito.when(booleanProp.get()).thenReturn(null);
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory);

        boolean trustAll = this.userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(userId, contextId);

        assertFalse(trustAll);
    }

    @Test
    public void testIsAllowedToDefineTrustLevel_disabledByConfig_returnFalse() throws OXException {
        Mockito.when(booleanProp.get()).thenReturn(Boolean.FALSE);
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory);

        boolean trustAll = this.userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(userId, contextId);

        assertFalse(trustAll);
    }

    @Test
    public void testIsAllowedToDefineTrustLevel_enabledByConfig_returnTrue() throws OXException {
        Mockito.when(booleanProp.get()).thenReturn(Boolean.TRUE);
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory);

        boolean trustAll = this.userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(userId, contextId);

        assertTrue(trustAll);
    }

    @Test
    public void testIsTrustAll_userNotAllowed_returnFalse() {
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory) {

            @Override
            public boolean isAllowedToDefineTrustLevel(int userId, int contextId) {
                return false;
            }
        };

        boolean trustAll = this.userAwareSSLConfigurationService.isTrustAll(this.userId, this.contextId);

        assertFalse(trustAll);
    }

    @Test
    public void testIsTrustAll_userAttributeNotSet_returnFalse() throws OXException {
        Mockito.when(this.userService.getUserAttribute(UserAwareSSLConfigurationService.USER_ATTRIBUTE_NAME, userId, this.context)).thenReturn(null);
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory) {

            @Override
            public boolean isAllowedToDefineTrustLevel(int userId, int contextId) {
                return true;
            }
        };

        boolean trustAll = this.userAwareSSLConfigurationService.isTrustAll(this.userId, this.contextId);

        assertFalse(trustAll);
    }

    @Test
    public void testIsTrustAll_userAttributeSetToFalse_returnFalse() throws OXException {
        Mockito.when(this.userService.getUserAttribute(UserAwareSSLConfigurationService.USER_ATTRIBUTE_NAME, userId, this.context)).thenReturn("false");
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory) {

            @Override
            public boolean isAllowedToDefineTrustLevel(int userId, int contextId) {
                return true;
            }
        };

        boolean trustAll = this.userAwareSSLConfigurationService.isTrustAll(this.userId, this.contextId);

        assertFalse(trustAll);
    }

    @Test
    public void testIsTrustAll_userAttributeSetToTrue_returnTrue() throws OXException {
        Mockito.when(this.userService.getUserAttribute(UserAwareSSLConfigurationService.USER_ATTRIBUTE_NAME, userId, this.context)).thenReturn("true");
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory) {

            @Override
            public boolean isAllowedToDefineTrustLevel(int userId, int contextId) {
                return true;
            }
        };

        boolean trustAll = this.userAwareSSLConfigurationService.isTrustAll(this.userId, this.contextId);

        assertTrue(trustAll);
    }

    @Test
    public void testSetTrustAll_userIdNotCorrect_notSet() throws OXException {
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory) {

            @Override
            public boolean isAllowedToDefineTrustLevel(int userId, int contextId) {
                return false;
            }
        };
        this.userAwareSSLConfigurationService.setTrustAll(0, context, true);

        Mockito.verify(this.userService, Mockito.never()).setUserAttribute(Matchers.anyString(), Matchers.anyString(), Matchers.anyInt(), (Context) Matchers.any());
    }

    @Test
    public void testSetTrustAll_contextIdNotCorrect_notSet() throws OXException {
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory) {

            @Override
            public boolean isAllowedToDefineTrustLevel(int userId, int contextId) {
                return true;
            }
        };
        this.userAwareSSLConfigurationService.setTrustAll(userId, null, true);

        Mockito.verify(this.userService, Mockito.never()).setUserAttribute(Matchers.anyString(), Matchers.anyString(), Matchers.anyInt(), (Context) Matchers.any());
    }

    @Test
    public void testSetTrustAll_everythingOk_set() throws OXException {
        this.userAwareSSLConfigurationService = new UserAwareSSLConfigurationImpl(userService, contextService, configViewFactory) {

            @Override
            public boolean isAllowedToDefineTrustLevel(int userId, int contextId) {
                return true;
            }
        };

        this.userAwareSSLConfigurationService.setTrustAll(userId, context, true);

        Mockito.verify(this.userService, Mockito.times(1)).setUserAttribute(Matchers.anyString(), Matchers.anyString(), Matchers.anyInt(), (Context) Matchers.any());
    }
}
