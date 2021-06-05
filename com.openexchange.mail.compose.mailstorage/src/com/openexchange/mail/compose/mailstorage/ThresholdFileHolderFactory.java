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

package com.openexchange.mail.compose.mailstorage;

import java.io.File;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ThresholdFileHolderFactory} - A factory for file holder instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ThresholdFileHolderFactory {

    private static final ThresholdFileHolderFactory INSTANCE = new ThresholdFileHolderFactory();

    /**
     * Gets the factory instance.
     *
     * @return The factory instance
     */
    public static ThresholdFileHolderFactory getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ThresholdFileHolderFactory}.
     */
    private ThresholdFileHolderFactory() {
        super();
    }

    /**
     * Create a new auto-managed file holder.
     *
     * @param session The session
     * @return The newly created file holder
     * @throws OXException If file holder cannot be created
     */
    public ThresholdFileHolder createFileHolder(Session session) throws OXException {
        return createFileHolder(session, true);
    }

    /**
     * Create a new file holder.
     *
     * @param session The session
     * @param automanaged Whether the file holder shall be auto-managed
     * @return The newly created file holder
     * @throws OXException If file holder cannot be created
     */
    public ThresholdFileHolder createFileHolder(Session session, boolean automanaged) throws OXException {
        if (session == null) {
            throw new IllegalArgumentException("Session must not be null");
        }
        return createFileHolder(session.getUserId(), session.getContextId(), automanaged);
    }

    /**
     * Create a new file holder.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param automanaged Whether the file holder shall be auto-managed
     * @return The newly created file holder
     * @throws OXException If file holder cannot be created
     */
    public ThresholdFileHolder createFileHolder(int userId, int contextId, boolean automanaged) throws OXException {
        MailStorageCompositionSpaceConfig config = MailStorageCompositionSpaceConfig.getInstance();
        int memoryThreshold = config.getInMemoryThreshold(userId, contextId);
        File spoolDirectory = config.getSpoolDirectory();
        return new ThresholdFileHolder(memoryThreshold, -1, automanaged, spoolDirectory);
    }

}
