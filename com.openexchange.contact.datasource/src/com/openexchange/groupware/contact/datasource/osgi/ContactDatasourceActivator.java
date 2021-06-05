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

package com.openexchange.groupware.contact.datasource.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.conversion.DataSource;
import com.openexchange.groupware.contact.datasource.ContactDataSource;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.contact.datasource.UserImageDataSource;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ContactDatasourceActivator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
@SuppressWarnings("deprecation")
public class ContactDatasourceActivator extends HousekeepingActivator{

    private static final String STR_IDENTIFIER = "identifier";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ContactService.class, ContactPictureService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.contact");
            ContactDataSource dataSource = new ContactDataSource(this);
            registerService(DataSource.class, dataSource, props);
        }
        {
            ContactImageDataSource contactDataSource = new ContactImageDataSource(this);
            Dictionary<String, Object> contactProps = new Hashtable<String, Object>(1);
            contactProps.put(STR_IDENTIFIER, contactDataSource.getRegistrationName());
            registerService(DataSource.class, contactDataSource, contactProps);
            ImageActionFactory.addMapping(contactDataSource.getRegistrationName(), contactDataSource.getAlias());
        }
        {
            UserImageDataSource userDataSource = new UserImageDataSource(this);
            Dictionary<String, Object> contactProps = new Hashtable<String, Object>(1);
            contactProps.put(STR_IDENTIFIER, userDataSource.getRegistrationName());
            registerService(DataSource.class, userDataSource, contactProps);
            ImageActionFactory.addMapping(userDataSource.getRegistrationName(), userDataSource.getAlias());
        }
    }

}
