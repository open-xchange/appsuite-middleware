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

package com.openexchange.secret.recovery.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link WhiteboardEncryptedItemDetector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WhiteboardEncryptedItemDetector extends ServiceTracker<EncryptedItemDetectorService, EncryptedItemDetectorService> implements EncryptedItemDetectorService {

    /**
     * Initializes a new {@link WhiteboardEncryptedItemDetector}.
     *
     * @param context The bundle context
     */
    public WhiteboardEncryptedItemDetector(final BundleContext context) {
        super(context, EncryptedItemDetectorService.class, null);
    }

    @Override
    public boolean hasEncryptedItems(final ServerSession session) throws OXException {
        for (final EncryptedItemDetectorService detector : getTracked().values()) {
            if (detector == this) {
                continue;
            }
            if (detector.hasEncryptedItems(session)) {
                return true;
            }
        }
        return false;
    }

}
