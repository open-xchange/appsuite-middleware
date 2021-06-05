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

package com.openexchange.messaging.json.actions.messages;

import com.openexchange.messaging.SimAccountAccess;
import com.openexchange.messaging.SimAccountManager;
import com.openexchange.messaging.SimMessageAccess;
import com.openexchange.messaging.SimMessagingAccount;
import com.openexchange.messaging.SimMessagingService;
import com.openexchange.messaging.SimMessagingTransport;
import com.openexchange.messaging.SimpleMessagingMessage;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.messaging.registry.SimMessagingServiceRegistry;


/**
 * {@link TestRegistryBuilder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TestRegistryBuilder {

    // Builds a service registry.
    public static MessagingServiceRegistry buildTestRegistry() {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        // Service 1
        final SimMessagingService service1 = new SimMessagingService();
        service1.setId("com.openexchange.test1");

        final SimAccountManager accManager1 = new SimAccountManager();

        // Account 1.1
        final SimMessagingAccount account11 = new SimMessagingAccount();
        account11.setDisplayName("acc1.1");
        account11.setId(11);
        account11.setMessagingService(service1);

        final SimpleMessagingMessage message11 = new SimpleMessagingMessage();
        message11.setId("msg11");

        final SimAccountAccess accAccess11 = new SimAccountAccess();
        final SimMessageAccess access11 = new SimMessageAccess();
        access11.setTemplateMessage(message11);
        accAccess11.setMessageAccess(access11);

        service1.setAccountAccess(11, accAccess11);
        service1.setAccountTransport(11, new SimMessagingTransport());

        // Account 1.2
        final SimMessagingAccount account12 = new SimMessagingAccount();
        account12.setDisplayName("acc1.2");
        account12.setId(12);
        account12.setMessagingService(service1);

        final SimpleMessagingMessage message12 = new SimpleMessagingMessage();
        message12.setId("msg12");

        final SimAccountAccess accAccess12 = new SimAccountAccess();
        final SimMessageAccess access12 = new SimMessageAccess();
        access12.setTemplateMessage(message12);
        accAccess12.setMessageAccess(access12);

        service1.setAccountAccess(12, accAccess12);
        service1.setAccountTransport(12, new SimMessagingTransport());


        accManager1.setAllAccounts(account11, account12);
        service1.setAccountManager(accManager1);

        // Service 2
        final SimMessagingService service2 = new SimMessagingService();
        service2.setId("com.openexchange.test2");

        final SimAccountManager accManager2 = new SimAccountManager();

        // Account 2.1
        final SimMessagingAccount account21 = new SimMessagingAccount();
        account21.setDisplayName("acc2.1");
        account21.setId(21);
        account21.setMessagingService(service2);

        final SimpleMessagingMessage message21 = new SimpleMessagingMessage();
        message21.setId("msg21");

        final SimAccountAccess accAccess21 = new SimAccountAccess();
        final SimMessageAccess access21 = new SimMessageAccess();
        access21.setTemplateMessage(message21);
        accAccess21.setMessageAccess(access21);

        service2.setAccountAccess(21, accAccess21);
        service2.setAccountTransport(21, new SimMessagingTransport());

        // Account 2.2
        final SimMessagingAccount account22 = new SimMessagingAccount();
        account22.setDisplayName("acc2.2");
        account22.setId(22);
        account22.setMessagingService(service2);

        final SimpleMessagingMessage message22 = new SimpleMessagingMessage();
        message21.setId("msg22");

        final SimAccountAccess accAccess22 = new SimAccountAccess();
        final SimMessageAccess access22 = new SimMessageAccess();
        access22.setTemplateMessage(message22);
        accAccess22.setMessageAccess(access22);

        service2.setAccountAccess(22, accAccess22);

        accManager2.setAllAccounts(account21, account22);
        service2.setAccountManager(accManager2);
        service2.setAccountTransport(22, new SimMessagingTransport());

        registry.add(service1);
        registry.add(service2);

        return registry;
    }

}
