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

package com.openexchange.chronos.json.action;

import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.chronos.json.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAttachment}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@DispatcherNotes(defaultFormat = "file")
public class GetAttachment extends ChronosAction {

    /**
     * Initializes a new {@link GetAttachment}.
     *
     * @param services
     */
    protected GetAttachment(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        // Gather the parameters
        EventID eventId = parseIdParameter(requestData);
        int managedId = parseAttachmentId(requestData);

        // Get the attachment and prepare the response
        IFileHolder fileHolder = null;
        try {
            fileHolder = calendarAccess.getAttachment(eventId, managedId);
            ServerSession session = requestData.getSession();
            if (session == null) {
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Missing user session!");
            }
            int contextId = session.getContextId();
            boolean scanned = scan(requestData, fileHolder, getUniqueId(contextId, eventId, Integer.toString(managedId)));
            if (scanned && false == fileHolder.repetitive()) {
                fileHolder = calendarAccess.getAttachment(eventId, managedId);
            }
            // Compose & return result
            AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            result.setHeader("ETag", calendarAccess.getSession().getContextId() + "-" + managedId);
            fileHolder = null; // Avoid premature closing
            return result;
        } finally {
            Streams.close(fileHolder);
        }
    }

}
