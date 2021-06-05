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

package com.openexchange.groupware.infostore;

import com.openexchange.groupware.infostore.osgi.InfostoreActivator;


/**
 * {@link InfostoreFacades} - Utility class for InfoStore.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InfostoreFacades {

    /**
     * Initializes a new {@link InfostoreFacades}.
     */
    private InfostoreFacades() {
        super();
    }

    /**
     * Checks if needed InfoStore bundle(s) is/are available.
     *
     * @return <code>true</code> if available; otherwise <code>false</code>
     */
    public static boolean isInfoStoreAvailable() {
        final InfostoreAvailable available = InfostoreActivator.INFOSTORE_FILE_STORAGE_AVAILABLE.get();
        return (null == available || available.available());
    }

    /**
     * Performs a safe rollback for each {@link InfostoreFacade} instance
     *
     * @param infostoreFacades The instances to be rolled-back
     */
    public static void rollback(InfostoreFacade... infostoreFacades) {
        if (null != infostoreFacades) {
            for (InfostoreFacade infostoreFacade : infostoreFacades) {
                if (null != infostoreFacade) {
                    try {
                        infostoreFacade.rollback();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
    }

    /**
     * Performs a safe finish for each {@link InfostoreFacade} instance
     *
     * @param infostoreFacades The instances to be finished
     */
    public static void finish(InfostoreFacade... infostoreFacades) {
        if (null != infostoreFacades) {
            for (InfostoreFacade infostoreFacade : infostoreFacades) {
                if (null != infostoreFacade) {
                    try {
                        infostoreFacade.finish();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
    }

}
