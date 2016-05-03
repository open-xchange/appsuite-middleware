/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.find.basic.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.Constants;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.find.basic.Services;
import com.openexchange.find.basic.calendar.BasicCalendarDriver;
import com.openexchange.find.basic.contacts.AutocompleteFields;
import com.openexchange.find.basic.contacts.BasicContactsDriver;
import com.openexchange.find.basic.drive.BasicDriveDriver;
import com.openexchange.find.basic.mail.BasicMailDriver;
import com.openexchange.find.basic.tasks.BasicTasksDriver;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.resource.ResourceService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link FindBasicActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class FindBasicActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContactService.class, FolderService.class, MailService.class,
            IDBasedFileAccessFactory.class, UnifiedInboxManagement.class,
            AppointmentSqlFactoryService.class, CalendarCollectionService.class, ThreadPoolService.class,
            IDBasedFolderAccessFactory.class, ResourceService.class, ConfigurationService.class, InfostoreSearchEngine.class,
            FileStorageServiceRegistry.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        ConfigurationService configService = getService(ConfigurationService.class);
        String virtualAllMessagesFolder = configService.getProperty("com.openexchange.find.basic.mail.allMessagesFolder");
        boolean searchMailBody = configService.getBoolProperty("com.openexchange.find.basic.mail.searchmailbody", false);
        registerService(ModuleSearchDriver.class, new BasicMailDriver(virtualAllMessagesFolder, searchMailBody), defaultProperties());
        registerService(ModuleSearchDriver.class, new BasicDriveDriver(), defaultProperties());
        registerService(ModuleSearchDriver.class, new BasicContactsDriver(), defaultProperties());
        registerService(ModuleSearchDriver.class, new BasicCalendarDriver(), defaultProperties());
        registerService(ModuleSearchDriver.class, new BasicTasksDriver(), defaultProperties());
        registerService(PreferencesItemService.class, AutocompleteFields.class.newInstance());
    }

    private Dictionary<String, Object> defaultProperties() {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put(Constants.SERVICE_RANKING, Integer.valueOf(0));
        return properties;
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
