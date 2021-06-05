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

package com.openexchange.importexport.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.server.ServiceLookup;

public class ImportExportServices {

    public static final AtomicReference<ServiceLookup> LOOKUP = new AtomicReference<>();

    public static ContactService getContactService() {
        return LOOKUP.get().getService(ContactService.class);
    }

    public static FolderUpdaterRegistry getUpdaterRegistry() {
        return LOOKUP.get().getService(FolderUpdaterRegistry.class);
    }

    public static ICalParser getIcalParser() {
        return LOOKUP.get().getService(ICalParser.class);
    }

    public static ConfigurationService getConfigurationService() {
        return LOOKUP.get().getService(ConfigurationService.class);
    }

    public static ICalEmitter getICalEmitter() {
        return LOOKUP.get().getService(ICalEmitter.class);
    }

    public static VCardService getVCardService() {
        return LOOKUP.get().getService(VCardService.class);
    }

    public static VCardStorageService getVCardStorageService(int contextId) {
        VCardStorageFactory vCardStorageFactory = LOOKUP.get().getOptionalService(VCardStorageFactory.class);
        if (vCardStorageFactory != null) {
            return vCardStorageFactory.getVCardStorageService(LOOKUP.get().getService(ConfigViewFactory.class), contextId);
        }
        return null;
    }

    public static FolderService getFolderService() {
        return LOOKUP.get().getService(FolderService.class);
    }

    public static ICalService getICalService() {
        return LOOKUP.get().getService(ICalService.class);
    }

    public static IDBasedCalendarAccessFactory getIDBasedCalendarAccessFactory() {
        return LOOKUP.get().getService(IDBasedCalendarAccessFactory.class);
    }

    public static CalendarService getCalendarService() {
        return LOOKUP.get().getService(CalendarService.class);
    }

    public static CalendarUtilities getCalendarUtilities() {
        return LOOKUP.get().getService(CalendarUtilities.class);
    }

}
