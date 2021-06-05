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

package com.openexchange.groupware.upload.osgi;

import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link UploadActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class UploadActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link UploadActivator}.
     */
    public UploadActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        UploadListenerTracker uploadListenerTracker = new UploadListenerTracker(context);
        rememberTracker(uploadListenerTracker);

        StreamedUploadListenerTracker streamedUploadListenerTracker = new StreamedUploadListenerTracker(context);
        rememberTracker(streamedUploadListenerTracker);
        openTrackers();

        UploadUtility.setUploadFileListenerLsting(uploadListenerTracker);
        UploadUtility.setStreamedUploadFileListenerLsting(streamedUploadListenerTracker);
    }

    @Override
    protected void stopBundle() throws Exception {
        UploadUtility.setStreamedUploadFileListenerLsting(null);
        UploadUtility.setUploadFileListenerLsting(null);
        super.stopBundle();
    }

}
