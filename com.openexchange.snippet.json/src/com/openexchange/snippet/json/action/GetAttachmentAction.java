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

package com.openexchange.snippet.json.action;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAttachmentAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction()
public final class GetAttachmentAction extends SnippetAction implements ETagAwareAJAXActionService {

    private static final Logger LOG = LoggerFactory.getLogger(GetAttachmentAction.class);

    private static String extractFilename(final Attachment attachment) {
        if (null == attachment) {
            return null;
        }
        try {
            final String sContentDisposition = attachment.getContentDisposition();
            String fn = null == sContentDisposition ? null : new ContentDisposition(sContentDisposition).getFilenameParameter();
            if (fn == null) {
                final String sContentType = attachment.getContentType();
                fn = null == sContentType ? null : new ContentType(sContentType).getNameParameter();
            }
            return fn;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Initializes a new {@link GetAttachmentAction}.
     *
     * @param services The service look-up
     */
    public GetAttachmentAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
    }

    @Override
    public boolean checkETag(final String clientETag, final AJAXRequestData request, final ServerSession session) throws OXException {
        if (clientETag == null || clientETag.length() == 0) {
            return false;
        }
        /*
         * Any ETag is valid because an attachment cannot change
         */
        return true;
    }

    @Override
    public void setETag(final String eTag, final long expires, final AJAXRequestResult result) throws OXException {
        result.setExpires(expires);
        result.setHeader("ETag", eTag);
    }

    @Override
    protected AJAXRequestResult perform(final SnippetRequest snippetRequest) throws OXException {
        final String id = snippetRequest.checkParameter("id");
        final String attachmentId = snippetRequest.checkParameter("attachmentid");
        boolean saveToDisk;
        {
            final String saveParam = snippetRequest.getParameter(Mail.PARAMETER_SAVE, String.class, true);
            try {
                saveToDisk = ((saveParam == null || saveParam.length() == 0) ? false : ((Integer.parseInt(saveParam)) > 0));
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                saveToDisk = "true".equalsIgnoreCase(saveParam) || "yes".equalsIgnoreCase(saveParam) || "on".equalsIgnoreCase(saveParam);
            }
        }

        // Get service & load snippet
        SnippetService snippetService = getSnippetService(snippetRequest.getSession());
        Snippet snippet = snippetService.getManagement(snippetRequest.getSession()).getSnippet(id);

        // Get list of attachments
        List<Attachment> attachments = snippet.getAttachments();
        if (null == attachments) {
            throw SnippetExceptionCodes.ATTACHMENT_NOT_FOUND.create(attachmentId, id);
        }

        // Look-up the referenced one
        Attachment attachment = null;
        {
            int length = attachments.size();
            for (int i = 0; null == attachment && i < length; i++) {
                Attachment cur = attachments.get(i);
                if (attachmentId.equals(cur.getId())) {
                    attachment = cur;
                }
            }
            if (null == attachment) {
                throw SnippetExceptionCodes.ATTACHMENT_NOT_FOUND.create(attachmentId, id);
            }
        }

        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        boolean error = true;
        try {
            // Write to file holder
            fileHolder.write(attachment.getInputStream());

            // Parameterize file holder
            snippetRequest.getRequestData().setFormat("file");
            fileHolder.setName(extractFilename(attachment));
            fileHolder.setContentType(saveToDisk ? "application/octet-stream" : attachment.getContentType());

            // Compose & return result
            AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            setETag(UUID.randomUUID().toString(), AJAXRequestResult.YEAR_IN_MILLIS * 50, result);
            error = false;
            return result;
        } catch (IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (error) {
                Streams.close(fileHolder);
            }
        }
    }

    @Override
    public String getAction() {
        return "getattachment";
    }

}
