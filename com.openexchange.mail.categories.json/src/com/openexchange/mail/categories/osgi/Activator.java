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

package com.openexchange.mail.categories.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.json.MailCategoriesActionFactory;
import com.openexchange.mail.categories.json.MailCategoriesConfigConverter;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { MailCategoriesConfigService.class, CapabilityService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerModule(MailCategoriesActionFactory.initMailCategoriesActionFactory(this), "mail/categories");
        registerService(ResultConverter.class, MailCategoriesConfigConverter.getInstance());
    }

    void registerModule(AJAXActionServiceFactory factory, String module) {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(4);
        properties.put("module", module);
        properties.put("multiple", "true");
        registerService(AJAXActionServiceFactory.class, factory, properties);
    }

}
