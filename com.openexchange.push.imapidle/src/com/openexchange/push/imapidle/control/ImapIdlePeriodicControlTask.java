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

package com.openexchange.push.imapidle.control;

import java.util.List;

/**
 * {@link ImapIdlePeriodicControlTask} - Responsible for interrupting expired threads currently performing IMAP IDLE.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ImapIdlePeriodicControlTask extends AbstractImapIdleControlTask implements Runnable {

    /**
     * Initializes a new {@link ImapIdlePeriodicControlTask}.
     */
    public ImapIdlePeriodicControlTask(ImapIdleControl control) {
        super(control);
    }

    @Override
    public void run() {
        try {
            List<ImapIdleRegistration> expired = control.removeExpired();
            for (ImapIdleRegistration registration : expired) {
                // Idl'ing for too long
                handleExpired(registration);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

}
