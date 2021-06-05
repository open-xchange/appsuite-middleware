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

package com.openexchange.test.fixtures;

import java.io.File;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mailaccount.internal.CustomMailAccount;
import com.openexchange.resource.Resource;
import com.openexchange.subscribe.Subscription;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class FixtureLoaderFactory {

    public static FixtureLoader getLoader() {//TODO add datapath to method signature
        File datapath = null;
        final YAMLFixtureLoader loader = new YAMLFixtureLoader();

        loader.addFixtureFactory(new TaskFixtureFactory(null, loader), Task.class);
        // TODO: create and use groupResolver
        loader.addFixtureFactory(new AppointmentFixtureFactory(null, loader), Appointment.class);
        loader.addFixtureFactory(new ContactFixtureFactory(loader), Contact.class);
        loader.addFixtureFactory(new InfoItemFixtureFactory(loader), InfoItem.class);
        // TODO: create and use TestUserConfigFactory
        // TODO: create and use ContactFinder
        loader.addFixtureFactory(new CredentialFixtureFactory(null, null, loader), SimpleCredentials.class);
        loader.addFixtureFactory(new GroupFixtureFactory(loader), Group.class);
        loader.addFixtureFactory(new ResourceFixtureFactory(loader), Resource.class);
        loader.addFixtureFactory(new EMailFixtureFactory(datapath, loader), MailMessage.class);
        loader.addFixtureFactory(new DocumentFixtureFactory(datapath, loader), Document.class);
        loader.addFixtureFactory(new SubscriptionFixtureFactory(loader), Subscription.class);
        loader.addFixtureFactory(new MultiMailFixtureFactory(loader), CustomMailAccount.class);
        // TODO: configdata for selenium
        return loader;
    }
}
