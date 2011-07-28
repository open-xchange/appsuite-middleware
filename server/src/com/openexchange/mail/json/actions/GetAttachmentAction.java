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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.json.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.html.HTMLService;
import com.openexchange.log.Log;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link GetAttachmentAction}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetAttachmentAction extends AbstractMailAction {

    private static final org.apache.commons.logging.Log LOG =
        Log.valueOf(org.apache.commons.logging.LogFactory.getLog(GetAttachmentAction.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link GetAttachmentAction}.
     * 
     * @param services
     */
    public GetAttachmentAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        boolean outSelected = false;
        boolean saveToDisk = false;
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderPath = req.checkParameter(Mail.PARAMETER_FOLDERID);
            final String uid = req.checkParameter(Mail.PARAMETER_ID);
            final String sequenceId = req.getParameter(Mail.PARAMETER_MAILATTCHMENT);
            final String imageContentId = req.getParameter(Mail.PARAMETER_MAILCID);
            {
                final String saveParam = req.getParameter(Mail.PARAMETER_SAVE);
                saveToDisk = ((saveParam == null || saveParam.length() == 0) ? false : ((Integer.parseInt(saveParam)) > 0));
            }
            final boolean filter;
            {
                final String filterParam = req.getParameter(Mail.PARAMETER_FILTER);
                filter = Boolean.parseBoolean(filterParam) || "1".equals(filterParam);
            }
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            if (sequenceId == null && imageContentId == null) {
                throw MailExceptionCode.MISSING_PARAM.create(new StringBuilder().append(Mail.PARAMETER_MAILATTCHMENT).append(" | ").append(
                    Mail.PARAMETER_MAILCID).toString());
            }
            final MailPart mailPart;
            InputStream attachmentInputStream;
            if (imageContentId == null) {
                mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, !saveToDisk);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }
                if (filter && !saveToDisk && mailPart.getContentType().isMimeType(MIMETypes.MIME_TEXT_HTM_ALL)) {
                    /*
                     * Apply filter
                     */
                    final ContentType contentType = mailPart.getContentType();
                    final String cs =
                        contentType.containsCharsetParameter() ? contentType.getCharsetParameter() : MailProperties.getInstance().getDefaultMimeCharset();
                    final String htmlContent = MessageUtility.readMailPart(mailPart, cs);
                    final HTMLService htmlService = ServerServiceRegistry.getInstance().getService(HTMLService.class);
                    attachmentInputStream =
                        new UnsynchronizedByteArrayInputStream(htmlService.filterWhitelist(
                            htmlService.getConformHTML(htmlContent, contentType.getCharsetParameter())).getBytes(cs));
                } else {
                    attachmentInputStream = mailPart.getInputStream();
                }
                /*-
                 * TODO: Does not work, yet.
                 * 
                 * if (!saveToDisk &amp;&amp; mailPart.getContentType().isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
                 *     // Treat as a mail get
                 *     final MailMessage mail = (MailMessage) mailPart.getContent();
                 *     final Response response = new Response();
                 *     response.setData(MessageWriter.writeMailMessage(mail, true, session));
                 *     response.setTimestamp(null);
                 *     ResponseWriter.write(response, resp.getWriter());
                 *     return;
                 * }
                 */
            } else {
                mailPart = mailInterface.getMessageImage(folderPath, uid, imageContentId);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }
                attachmentInputStream = mailPart.getInputStream();
            }
            /*
             * Read from stream
             */
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream();
            outSelected = true;
            /*
             * Write from content's input stream to byte array output stream
             */
            try {
                final int buflen = 0xFFFF;
                final byte[] buffer = new byte[buflen];
                for (int len; (len = attachmentInputStream.read(buffer, 0, buflen)) != -1;) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            } finally {
                attachmentInputStream.close();
            }
            /*
             * Create file holder
             */
            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(out.toByteArray());
            fileHolder.setName(mailPart.getFileName());
            fileHolder.setContentType(saveToDisk ? "application/octet-stream" : mailPart.getContentType().toString());
            return new AJAXRequestResult(fileHolder, "file");
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
