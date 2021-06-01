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

import static com.openexchange.mail.compose.CompositionSpaces.buildConsoleTableFor;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.Type;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link OpenCompositionSpaceAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class OpenCompositionSpaceAction extends AbstractMailComposeAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OpenCompositionSpaceAction.class);

    /**
     * Initializes a new {@link OpenCompositionSpaceAction}.
     *
     * @param services The service look-up
     */
    public OpenCompositionSpaceAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        // Acquire needed service
        CompositionSpaceService compositionSpaceService = getHighestRankedService(session);

        // Determine type
        Type type = Type.NEW;
        {
            String sType = requestData.getParameter("type");
            if (Strings.isNotEmpty(sType)) {
                type = Type.typeFor(sType.trim());
                if (null == type) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type);
                }
            }
        }

        UserSettingMail usm = session.getUserSettingMail();
        if (null == usm) {
            if (session.getUser().isGuest() && !session.getUserPermissionBits().hasWebMail()) {
                throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("mail");
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Failed to load user mail settings");
        }
        usm.setNoSave(true);

        // Build parameters
        OpenCompositionSpaceParameters.Builder parameters;
        switch (type.getId()) {
            case "copy":
                {
                    MailPath copyFor = requireReferencedMail(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForCopy(copyFor, usm);
                }
                break;
            case "edit":
                {
                    MailPath editFor = requireReferencedMail(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForEdit(editFor, usm);
                }
                break;
            case "forward":
                {
                    List<MailPath> forwardsFor = requireReferencedMails(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForForward(forwardsFor, usm);
                }
                break;
            case "new":
                //$FALL-THROUGH$
            case "sms":
                //$FALL-THROUGH$
            case "fax":
                parameters = OpenCompositionSpaceParameters.builderForNew(type, usm);
                break;
            case "reply":
                //$FALL-THROUGH$
            case "replyall":
                {
                    MailPath replyFor = requireReferencedMail(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForReply(type == Type.REPLY_ALL, replyFor, usm);
                }
                break;
            case "resend":
                {
                    MailPath resendtFor = requireReferencedMail(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForResend(resendtFor, usm);
                }
                break;
            default:
                parameters = OpenCompositionSpaceParameters.builderForNew(type, usm);
        }

        if (AJAXRequestDataTools.parseBoolParameter("vcard", requestData)) {
            parameters.withAppendVCard(true);
        }

        if (AJAXRequestDataTools.parseBoolParameter("originalAttachments", requestData)) {
            parameters.withAppendOriginalAttachments(true);
        }

        if (AJAXRequestDataTools.parseBoolParameter("requestReadReceipt", requestData)) {
            parameters.withRequestReadReceipt(true);
        }

        // Check for priority
        {
            String priority = requestData.getParameter("priority");
            if (Strings.isNotEmpty(priority)) {
                Priority p = Priority.priorityFor(priority);
                if (null != p) {
                    parameters.withPriority(p);
                }
            }
        }

        // Check for Content-Type
        {
            String contentType = requestData.getParameter("contentType");
            if (Strings.isNotEmpty(contentType)) {
                ContentType ct = ContentType.contentTypeFor(contentType);
                if (null == ct) {
                    // Found no such Content-Type for given identifier
                    parameters.withContentType(usm.isDisplayHtmlInlineContent() ? ContentType.TEXT_HTML : ContentType.TEXT_PLAIN);
                } else {
                    parameters.withContentType(ct);
                }
            } else if (Type.NEW == type) {
                int msgFormat = usm.getMsgFormat();
                if (UserSettingMail.MSG_FORMAT_BOTH == msgFormat) {
                    parameters.withContentType(ContentType.MULTIPART_ALTERNATIVE);
                } else if (UserSettingMail.MSG_FORMAT_TEXT_ONLY == msgFormat) {
                    parameters.withContentType(ContentType.TEXT_PLAIN);
                } else {
                    parameters.withContentType(ContentType.TEXT_HTML);
                }
            }
        }

        ClientToken clientToken = getClaimedClientToken(requestData);
        if (clientToken.isPresent()) {
            parameters.withClientToken(clientToken);
        }

        // Does not hold since a JSON array is expected for the mail paths...
        //        JSONObject jMessage = (JSONObject) requestData.getData();
        //        if (null != jMessage) {
        //            // Security settings
        //            {
        //                JSONObject jSecurity = jMessage.optJSONObject("security");
        //                if (null != jSecurity) {
        //                    parameters.withSecurity(toSecurity(jSecurity));
        //                }
        //            }
        //
        //            // Shared attachments information
        //            {
        //                JSONObject jSharedAttachments = jMessage.optJSONObject("sharedAttachments");
        //                if (null != jSharedAttachments) {
        //                    parameters.withSharedAttachmentsInfo(toSharedAttachmentsInfo(jSharedAttachments));
        //                }
        //            }
        //        }

        CompositionSpace compositionSpace = compositionSpaceService.openCompositionSpace(parameters.build());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Opened composition space '{}':{}{}", compositionSpace.getId(), Strings.getLineSeparator(), buildConsoleTableFor(compositionSpace, Optional.ofNullable(requestData.getUserAgent())));
        }
        return new AJAXRequestResult(compositionSpace, "compositionSpace").addWarnings(compositionSpaceService.getWarnings());
    }

}
