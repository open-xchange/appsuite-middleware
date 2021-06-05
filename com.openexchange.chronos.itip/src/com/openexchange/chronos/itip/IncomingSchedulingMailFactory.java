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

package com.openexchange.chronos.itip;

import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;

/**
 * {@link IncomingSchedulingMailFactory}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public interface IncomingSchedulingMailFactory {

    /**
     * Creates an {@link IncomingSchedulingMessage} based on the given data by loading an e-mail.
     * <p>
     * The calendar data from the mail is copied <b>unchanged</b> meaning no additional adjustments or patches
     * has been applied. Use {@link #createPatched(CalendarSession, IncomingSchedulingMailData)}
     * for an patched version adjusted to our internal model.
     * 
     * @param session The users session
     * @param data The data of the mail
     * @return The request parsed to an {@link IncomingSchedulingMessage}
     * @throws OXException In case the request can't be parsed
     */
    IncomingSchedulingMessage create(CalendarSession session, IncomingSchedulingMailData data) throws OXException;

    /**
     * Creates an {@link IncomingSchedulingMessage} based on the given data by loading an e-mail and purifies specific
     * fields in the calendar object(s) to work with our internal model.
     *
     * @param session The users session
     * @param data The data of the mail
     * @return The request parsed to an {@link IncomingSchedulingMessage}
     * @throws OXException In case the request can't be parsed
     */
    IncomingSchedulingMessage createPatched(CalendarSession session, IncomingSchedulingMailData data) throws OXException;

}
