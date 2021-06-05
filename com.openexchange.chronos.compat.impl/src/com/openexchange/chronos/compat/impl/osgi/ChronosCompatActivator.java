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

package com.openexchange.chronos.compat.impl.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.compat.impl.attachments.CalendarAttachmentHandler;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ChronosCompatActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ChronosCompatActivator extends HousekeepingActivator {

    private CalendarAttachmentHandler attachmentHandler;

    /**
     * Initializes a new {@link ChronosCompatActivator}.
     */
    public ChronosCompatActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { CalendarService.class, FolderService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Logger logger = LoggerFactory.getLogger(ChronosCompatActivator.class);
        try {
            logger.info("starting bundle {}", context.getBundle());
            /*
             * inject legacy calendar attachment authorization & listener
             */
            CalendarAttachmentHandler attachmentHandler = new CalendarAttachmentHandler(this);
            Attachments.getAuthorizationChooserForModule(Types.APPOINTMENT).registerForEverything(attachmentHandler, 18);
            Attachments.getListenerChooserForModule(Types.APPOINTMENT).registerForEverything(attachmentHandler, 18);
            this.attachmentHandler = attachmentHandler;
            openTrackers();
        } catch (Exception e) {
            logger.error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        LoggerFactory.getLogger(ChronosCompatActivator.class).info("stopping bundle {}", context.getBundle());
        CalendarAttachmentHandler attachmentHandler = this.attachmentHandler;
        if (null != attachmentHandler) {
            Attachments.getListenerChooserForModule(Types.APPOINTMENT).removeForEverything(attachmentHandler);
            Attachments.getAuthorizationChooserForModule(Types.APPOINTMENT).removeForEverything(attachmentHandler);
            this.attachmentHandler = null;
        }
        super.stopBundle();
    }

}
