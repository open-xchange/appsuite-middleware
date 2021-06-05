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

package com.openexchange.mail.compose.json.action;

import static com.openexchange.mail.compose.Attachments.isNoImage;
import java.util.UUID;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentResult;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.json.AttachmentFileHolder;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetAttachmentMailComposeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true, publicSessionAuth = true)
public class GetAttachmentMailComposeAction extends AbstractMailComposeAction {

    /**
     * Initializes a new {@link GetAttachmentMailComposeAction}.
     *
     * @param services The service look-up
     */
    public GetAttachmentMailComposeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        // Require composition space identifier
        String sId = requestData.requireParameter("id");
        CompositionSpaceId compositionSpaceId = parseCompositionSpaceId(sId);

        // Require attachment identifier
        String sAttachmentId = requestData.requireParameter("attachmentId");
        UUID attachmentUuid = parseAttachmentId(sAttachmentId);

        CompositionSpaceService compositionSpaceService = getCompositionSpaceService(compositionSpaceId.getServiceId(), session);
        AttachmentResult attachmentResult = compositionSpaceService.getAttachment(compositionSpaceId.getId(), attachmentUuid);
        Attachment attachment = attachmentResult.getFirstAttachment();

        if (isNoImage(attachment)) {
            // Require session parameter for non-image contents
            String sessionId = requestData.requireParameter(AJAXServlet.PARAMETER_SESSION);
            if (!sessionId.equals(session.getSessionID())) {
                throw SessionExceptionCodes.WRONG_SESSION.create();
            }
        }

        AttachmentFileHolder fileHolder = new AttachmentFileHolder(attachment, compositionSpaceService, session);
        return new AJAXRequestResult(fileHolder, "file").addWarnings(compositionSpaceService.getWarnings());
    }

}
