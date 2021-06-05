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

package com.openexchange.subscribe.json.actions;

import static com.openexchange.java.Autoboxing.B;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link AbstractSubscribeActionTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.1
 */
public class AbstractSubscribeActionTest {

    private Subscription subscription;

    @Mock
    private SubscriptionSource source;

    @Mock
    private ServiceLookup services;

    @Mock
    private ConfigurationService configurationService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        subscription = new Subscription();
        subscription.setSource(source);
        Mockito.when(source.getId()).thenReturn(AbstractSubscribeAction.MICROFORMATS_ID);
        Mockito.when(services.getService(ConfigurationService.class)).thenReturn(configurationService);
    }

     @Test
     public void testCheckCreateModifyEnabled_enabledViaConfig_doNotThrowException() throws OXException {
        Mockito.when(B(configurationService.getBoolProperty("com.openexchange.subscribe.microformats.createModifyEnabled", false))).thenReturn(Boolean.TRUE);

        NewSubscriptionAction abstractSubscribeAction = new NewSubscriptionAction(services);
        abstractSubscribeAction.checkAllowed(subscription);

    }

    @Test(expected = OXException.class)
     public void testCheckCreateModifyEnabled_disabledViaConfig_doThrowException() throws Exception {
        Mockito.when(B(configurationService.getBoolProperty("com.openexchange.subscribe.microformats.createModifyEnabled", false))).thenReturn(Boolean.FALSE);

        NewSubscriptionAction abstractSubscribeAction = new NewSubscriptionAction(services);
        abstractSubscribeAction.checkAllowed(subscription);
    }

     @Test
     public void testCheckCreateModifyEnabled_noOXMFSubscription_doNotThrowException() throws OXException {
        Mockito.when(B(configurationService.getBoolProperty("com.openexchange.subscribe.microformats.createModifyEnabled", false))).thenReturn(Boolean.TRUE);
        Mockito.when(source.getId()).thenReturn("not_the_OXMF_type");

        NewSubscriptionAction abstractSubscribeAction = new NewSubscriptionAction(services);
        abstractSubscribeAction.checkAllowed(subscription);
    }

     @Test
     public void testCheckCreateModifyEnabled_noOXMFSubscriptionButDisabled_doNotThrowException() throws OXException {
        Mockito.when(B(configurationService.getBoolProperty("com.openexchange.subscribe.microformats.createModifyEnabled", false))).thenReturn(Boolean.FALSE);
        Mockito.when(source.getId()).thenReturn("not_the_OXMF_type");

        NewSubscriptionAction abstractSubscribeAction = new NewSubscriptionAction(services);
        abstractSubscribeAction.checkAllowed(subscription);
    }
}
