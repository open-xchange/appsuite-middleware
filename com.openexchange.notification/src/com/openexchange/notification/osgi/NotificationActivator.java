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

package com.openexchange.notification.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.html.HtmlService;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.notification.FullNameBuilder;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.notification.mail.impl.NotificationMailFactoryImpl;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * {@link NotificationActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class NotificationActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, TemplateService.class, HtmlService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.set(this);

        trackService(MimeTypeMap.class);
        trackService(UserService.class);
        openTrackers();

        NotificationMailFactoryImpl notificationMailFactory = new NotificationMailFactoryImpl(
            getService(ConfigurationService.class),
            getService(TemplateService.class),
            getService(HtmlService.class));
        registerService(NotificationMailFactory.class, notificationMailFactory);
        registerService(FullNameBuilderService.class, FullNameBuilder.getInstance());
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.set(null);
        super.stopBundle();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

}
