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


import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.similarity.ContactSimilarityService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.importexport.ImportExportService;
import com.openexchange.importexport.actions.ExportActionFactory;
import com.openexchange.importexport.actions.ImportActionFactory;
import com.openexchange.importexport.impl.ImportExportServiceImpl;

/**
 * {@link ImportExportActivator}
 *
 * @author tobiasp
 */
public class ImportExportActivator extends AJAXModuleActivator{

    public ImportExportActivator() {
        super();
    }

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{
		    ContactService.class,
			FolderUpdaterRegistry.class,
			ICalParser.class,
			ConfigurationService.class,
			ICalEmitter.class,
			ConfigViewFactory.class,
			VCardService.class,
			FolderService.class,
			IDBasedCalendarAccessFactory.class,
			ICalService.class,
			CalendarService.class,
			CalendarUtilities.class
		};
	}

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { ContactSimilarityService.class };
    }

	@Override
	protected void startBundle() throws Exception {
		ImportExportServices.LOOKUP.set(this);
		registerService(ImportExportService.class, new ImportExportServiceImpl(this));
		registerModule(new ImportActionFactory(this), "import");
		registerModule(new ExportActionFactory(this), "export");

		track(VCardStorageFactory.class);
		openTrackers();
	}

	@Override
	protected void stopBundle() throws Exception {
	    ImportExportServices.LOOKUP.set(null);
	    super.stopBundle();
	}

}
