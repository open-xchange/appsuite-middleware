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

package com.openexchange.user.json.osgi;

import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.guest.GuestService;
import com.openexchange.mail.service.MailService;
import com.openexchange.share.ShareService;
import com.openexchange.user.UserService;
import com.openexchange.user.json.Constants;
import com.openexchange.user.json.actions.UserActionFactory;
import com.openexchange.user.json.actions.UserMeActionFactory;
import com.openexchange.user.json.anonymizer.ContactAnonymizerService;
import com.openexchange.user.json.anonymizer.UserAnonymizerService;
import com.openexchange.user.json.converter.MeResultConverter;
import com.openexchange.user.json.converter.UserContactResultConverter;

/**
 * {@link UserJSONActivator} - Activator for JSON user interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserJSONActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link UserJSONActivator}.
     */
    public UserJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            registerModule(new UserActionFactory(this), Constants.MODULE);
            registerModule(new UserMeActionFactory(this), Constants.MODULE_ME);

            /*
             * Register result converter
             */
            registerService(ResultConverter.class, new UserContactResultConverter());
            registerService(ResultConverter.class, new MeResultConverter());
            registerService(AnonymizerService.class.getName(), new UserAnonymizerService());
            registerService(AnonymizerService.class.getName(), new ContactAnonymizerService());

            trackService(DispatcherPrefixService.class);
            trackService(UserService.class);
            trackService(ContactService.class);
            trackService(DatabaseService.class);
            trackService(ContactUserStorage.class);
            trackService(ShareService.class);
            trackService(GuestService.class);
            trackService(MailService.class);
            openTrackers();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(UserJSONActivator.class).error("Failed to start bundle {}", context.getBundle().getSymbolicName(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

}
