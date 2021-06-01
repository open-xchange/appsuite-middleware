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

package com.openexchange.sessiond.impl;

import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.EventAdmin;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigInterface;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigRegistry;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigRegistry.UserType;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link Bug22838Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class Bug22838Test {

    @Mock
    private UserTypeSessiondConfigRegistry registry;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        SessionStorageService sessionStorageService = Mockito.mock(SessionStorageService.class);
        serviceLookup.add(SessionStorageService.class, sessionStorageService);
        EventAdmin eventAdmin = Mockito.mock(EventAdmin.class);
        serviceLookup.add(EventAdmin.class, eventAdmin);
        com.openexchange.sessiond.osgi.Services.setServiceLookup(serviceLookup);

        UserTypeSessiondConfigInterface sessiondConfigInterface = new UserTypeSessiondConfigInterface() {

            @Override
            public UserType getUserType() {
                return UserType.USER;
            }

            @Override
            public int getMaxSessionsPerUserType() {
                return 0;
            }
        };

        Mockito.when(registry.getConfigFor(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(sessiondConfigInterface);

        SessionHandler.init(new SessiondConfigImpl(new SimConfigurationService()), registry);
    }

    @After
    public void tearDown() {
        SessionHandler.close();
    }

    @Test
    public void testMergeEmptyArrayWithNull() {
        Session[] retval = SessionHandler.removeUserSessions(0, 0);
        assertEquals("Array length not 0", 0, retval.length);
    }

}
