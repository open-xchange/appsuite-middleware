/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.snippet.json.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
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
@Action(
    name = "getattachment"
    , description = "Gets a specific snippet attachment."
    , method = RequestMethod.GET
    , parameters = {
        @Parameter(name = "id", description = "The identifier of the snippet."),
        @Parameter(name = "attachmentid", description = "The identifier of the snippet attachment.")
    }
)
public final class GetAttachmentAction extends SnippetAction implements ETagAwareAJAXActionService {

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
        } catch (final Exception e) {
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
            } catch (final NumberFormatException e) {
                saveToDisk = "true".equalsIgnoreCase(saveParam) || "yes".equalsIgnoreCase(saveParam) || "on".equalsIgnoreCase(saveParam);
            }
        }
        // Get service
        final SnippetService snippetService = getSnippetService(snippetRequest.getSession());
        final Snippet snippet = snippetService.getManagement(snippetRequest.getSession()).getSnippet(id);
        final List<Attachment> attachments = snippet.getAttachments();
        if (null == attachments) {
            throw SnippetExceptionCodes.ATTACHMENT_NOT_FOUND.create(attachmentId, id);
        }
        final int length = attachments.size();
        Attachment attachment = null;
        for (int i = 0; null == attachment && i < length; i++) {
            final Attachment cur = attachments.get(i);
            if (attachmentId.equals(cur.getId())) {
                attachment = cur;
            }
        }
        if (null == attachment) {
            throw SnippetExceptionCodes.ATTACHMENT_NOT_FOUND.create(attachmentId, id);
        }
        InputStream attachmentInputStream = null;
        try {
            attachmentInputStream = attachment.getInputStream();
            /*
             * Read from stream
             */
            final ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            /*
             * Write from content's input stream to byte array output stream
             */
            fileHolder.write(attachmentInputStream);
            /*
             * Parameterize file holder
             */
            snippetRequest.getRequestData().setFormat("file");
            fileHolder.setName(extractFilename(attachment));
            fileHolder.setContentType(saveToDisk ? "application/octet-stream" : attachment.getContentType());
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            /*
             * Set ETag
             */
            setETag(UUID.randomUUID().toString(), AJAXRequestResult.YEAR_IN_MILLIS * 50, result);
            /*
             * Return result
             */
            return result;
        } catch (final IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(attachmentInputStream);
        }
    }

    @Override
    public String getAction() {
        return "getattachment";
    }

}
