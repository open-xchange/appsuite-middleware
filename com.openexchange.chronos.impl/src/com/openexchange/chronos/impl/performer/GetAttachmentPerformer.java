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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link GetAttachmentPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetAttachmentPerformer extends AbstractQueryPerformer {

    /**
     * Initialises a new {@link GetAttachmentPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public GetAttachmentPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the get attachment operation.
     *
     * @param folderId The identifier of the parent folder the event is located in
     * @param eventId The {@link Event} identifier
     * @param managedId The managed identifier of the {@link Attachment}
     * @return The {@link IFileHolder} of the attachment
     */
    public IFileHolder performGetAttachment(String folderId, String eventId, int managedId) throws OXException {
        /*
         * load event data & check permissions
         */
        CalendarFolder folder = getFolder(session, folderId, false);
        EventField[] fields = getFields(session, EventField.ORGANIZER, EventField.ATTENDEES, EventField.ATTACHMENTS);
        Event event = storage.getEventStorage().loadEvent(eventId, fields);
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(eventId);
        }
        event = storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), event, fields);
        event = Check.eventIsVisible(folder, event);
        Check.eventIsInFolder(event, folder);

        // Search for the attachment with the specified managed id
        List<Attachment> attachments = event.getAttachments();
        if (attachments == null) {
            throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(I(managedId), eventId, folder.getId());
        }
        for (Attachment attachment : attachments) {
            if (attachment.getManagedId() == managedId) {
                FileHolder fileHolder = new FileHolder(storage.getAttachmentStorage().loadAttachmentData(managedId), attachment.getSize(), attachment.getFormatType(), attachment.getFilename());
                fileHolder.setDelivery("download");
                fileHolder.setDisposition("attachment");
                return fileHolder;
            }
        }
        throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(I(managedId), eventId, folder.getId());
    }

}
