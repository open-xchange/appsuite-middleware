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

package com.openexchange.authentication.application.storage.rdb.passwords;

import static com.openexchange.java.Autoboxing.i;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;

/**
 * {@link AppPasswordChangeEventHandler} Handle user password change.
 * Notify application password storage of the change
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordChangeEventHandler implements EventHandler {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AppPasswordChangeEventHandler.class);

    private final AppPasswordStorageRDB storage;

    /**
     * Initializes a new {@link AppPasswordChangeEventHandler}.
     * 
     * @param services The underlying storage
     */
    public AppPasswordChangeEventHandler(AppPasswordStorageRDB storage) {
        super();
        this.storage = storage;
    }

    @Override
    public void handleEvent(Event event) {
        int contextId = i((Integer) event.getProperty("com.openexchange.passwordchange.contextId"));
        int userId = i((Integer) event.getProperty("com.openexchange.passwordchange.userId"));
        String oldPassword = (String) event.getProperty("com.openexchange.passwordchange.oldPassword");
        String newPassword = (String) event.getProperty("com.openexchange.passwordchange.newPassword");
        try {
            storage.changePassword(contextId, userId, newPassword, oldPassword);
        } catch (OXException ex) {
            LOG.error("Error changing application password", ex);
        }
    }

}
