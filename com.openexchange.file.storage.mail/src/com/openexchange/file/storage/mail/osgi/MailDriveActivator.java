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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.mail.osgi;

import org.slf4j.Logger;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.mail.MailDriveFileStorageService;
import com.openexchange.file.storage.mail.MailMetadataField;
import com.openexchange.file.storage.mail.find.MailDriveDriver;
import com.openexchange.file.storage.mail.settings.AbstractMailDriveSetting;
import com.openexchange.file.storage.mail.settings.AllAttachmentsFolder;
import com.openexchange.file.storage.mail.settings.ReceivedAttachmentsFolder;
import com.openexchange.file.storage.mail.settings.SentAttachmentsFolder;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

/**
 * {@link MailDriveActivator} - Activator for Mail Drive bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MailDriveActivator}.
     */
    public MailDriveActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MimeTypeMap.class, ConfigViewFactory.class, UserService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(MailDriveActivator.class);
        try {
            Services.setServices(this);

            MailDriveFileStorageService service = MailDriveFileStorageService.newInstance();
            rememberTracker(new MailDriveDriver(service, context));
            openTrackers();

            registerService(FileStorageService.class, service, null);

            AbstractMailDriveSetting setting = new AllAttachmentsFolder(service);
            registerService(PreferencesItemService.class, setting, null);
            registerService(ConfigTreeEquivalent.class, setting, null);

            setting = new ReceivedAttachmentsFolder(service);
            registerService(PreferencesItemService.class, setting, null);
            registerService(ConfigTreeEquivalent.class, setting, null);

            setting = new SentAttachmentsFolder(service);
            registerService(PreferencesItemService.class, setting, null);
            registerService(ConfigTreeEquivalent.class, setting, null);
            /*
             * register an additional file field providing additional mail metadata
             */
            registerService(AdditionalFileField.class, new MailMetadataField());
        } catch (Exception e) {
            logger.error("", e);
            throw e;
        }
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServices(null);
    }

}
