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

import static com.openexchange.java.Autoboxing.I;
import org.slf4j.Logger;
import com.openexchange.push.imapidle.ImapIdlePushListener;

/**
 * {@link AbstractImapIdleControlTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public abstract class AbstractImapIdleControlTask {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractImapIdleControlTask.class);
    }

    /** The IMAP IDLE control */
    protected final ImapIdleControl control;

    /**
     * Initializes a new {@link AbstractImapIdleControlTask}.
     */
    protected AbstractImapIdleControlTask(ImapIdleControl control) {
        super();
        this.control = control;
    }

    /**
     * Handles specified expired IMAP IDLE registration.
     *
     * @param registration The expired registration
     */
    protected void handleExpired(ImapIdleRegistration registration) {
        ImapIdlePushListener pushListener = registration.getPushListener();
        if (pushListener.isIdling()) {
            try {
                pushListener.markInterrupted();
                registration.getImapFolder().close(false);
            } catch (Exception e) {
                LoggerHolder.LOGGER.warn("Failed to interrupt elapsed {}IMAP-IDLE listener for user {} in context {}.", pushListener.isPermanent() ? "permanent " : "", I(pushListener.getUserId()), I(pushListener.getContextId()), e);
            }
        }
    }

}
