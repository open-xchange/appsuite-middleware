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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.helper.BrowserDetector;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenRegistry;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * Attachment
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MailAttachment extends AJAXServlet {

    /**
     *
     */
    private static final long serialVersionUID = -3109402774466180271L;

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAttachment.class));

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        Tools.disableCaching(res);
        /*
         * Get attachment
         */
        boolean outSelected = false;
        try {
            final String id = req.getParameter(PARAMETER_ID);
            if (null == id) {
                throw MailExceptionCode.MISSING_PARAM.create(PARAMETER_ID);
            }
            final boolean saveToDisk;
            {
                final String saveParam = req.getParameter("save");
                saveToDisk = ((saveParam == null || saveParam.length() == 0) ? false : ((Integer.parseInt(saveParam)) > 0));
            }
            final boolean filter;
            {
                final String filterParam = req.getParameter(PARAMETER_FILTER);
                filter = Boolean.parseBoolean(filterParam) || "1".equals(filterParam);
            }
            Tools.removeCachingHeader(res);
            final AttachmentToken token = AttachmentTokenRegistry.getInstance().getToken(id);
            if (null == token) {
                throw MailExceptionCode.ATTACHMENT_EXPIRED.create();
            }
            /*-
             * Security check
             *
             * IP-Check appropriate for roaming mobile devices?
             */
            if (token.isCheckIp() && null != token.getClientIp() && !req.getRemoteAddr().equals(token.getClientIp())) {
                AttachmentTokenRegistry.getInstance().removeToken(id);
                throw MailExceptionCode.ATTACHMENT_EXPIRED.create();
            }
            /*
             * At least expect the same user agent as the one which created the attachment token
             */
            if (token.isOneTime() && null != token.getUserAgent()) {
                final String requestUserAgent = req.getHeader("user-agent");
                if (null == requestUserAgent) {
                    AttachmentTokenRegistry.getInstance().removeToken(id);
                    throw MailExceptionCode.ATTACHMENT_EXPIRED.create();
                }
                if (!new BrowserDetector(token.getUserAgent()).nearlyEquals(new BrowserDetector(requestUserAgent))) {
                    AttachmentTokenRegistry.getInstance().removeToken(id);
                    throw MailExceptionCode.ATTACHMENT_EXPIRED.create();
                }
            }
            /*
             * Write part to output stream
             */
            final MailPart mailPart = token.getAttachment();
            final File file = null;
            InputStream attachmentInputStream = null;
            try {
                if (filter && !saveToDisk && mailPart.getContentType().startsWithAny("text/htm", "text/xhtm", "text/xml")) {
                    /*
                     * Apply filter
                     */
                    final ContentType contentType = mailPart.getContentType();
                    final String cs =
                        contentType.containsCharsetParameter() ? contentType.getCharsetParameter() : MailProperties.getInstance().getDefaultMimeCharset();
                    String htmlContent = MessageUtility.readMailPart(mailPart, cs);
                    htmlContent = MessageUtility.simpleHtmlDuplicateRemoval(htmlContent);
                    final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                    attachmentInputStream = new UnsynchronizedByteArrayInputStream(sanitizeHtml(htmlContent, htmlService).getBytes(Charsets.forName(cs)));
                } else {
                    attachmentInputStream = mailPart.getInputStream();
                }
                /*
                 * Set Content-Type and Content-Disposition header
                 */
                final String fileName = mailPart.getFileName();
                if (saveToDisk) {
                    /*
                     * We are supposed to offer attachment for download. Therefore enforce application/octet-stream and attachment
                     * disposition.
                     */
                    res.setContentType("application/octet-stream");
                    final ContentDisposition cd = new ContentDisposition();
                    cd.setAttachment();
                    cd.addParameter("filename", fileName);
                    res.setHeader("Content-Disposition", cd.toString());
                } else {
                    final String userAgent = req.getHeader("user-agent");
                    final CheckedDownload checkedDownload =
                        DownloadUtility.checkInlineDownload(
                            attachmentInputStream,
                            fileName,
                            mailPart.getContentType().toString(),
                            userAgent);
                    res.setContentType(checkedDownload.getContentType());
                    res.setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                    attachmentInputStream = checkedDownload.getInputStream();
                    /*-
                     * Check for Android client
                     *
                    final boolean isAndroid = (null != userAgent && userAgent.toLowerCase(Locale.ENGLISH).indexOf("android") >= 0);
                    if (isAndroid) {
                        final ManagedFileManagement service = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
                        if (null != service) {
                            file = service.newTempFile();
                            final FileOutputStream fos = new FileOutputStream(file);
                            try {
                                final int buflen = 0xFFFF;
                                final byte[] buffer = new byte[buflen];
                                for (int len; (len = attachmentInputStream.read(buffer, 0, buflen)) != -1;) {
                                    fos.write(buffer, 0, len);
                                }
                                fos.flush();
                            } finally {
                                try {
                                    fos.close();
                                } catch (final Exception e) {
                                    // Ignore
                                }
                            }
                            // Set Content-Length header properly
                            res.setContentLength((int) file.length());
                            attachmentInputStream = new FileInputStream(file);
                        }
                    }
                     *
                     */
                }
                /*
                 * Reset response header values since we are going to directly write into servlet's output stream and then some browsers do
                 * not allow header "Pragma"
                 */
                Tools.removeCachingHeader(res);
                final OutputStream out = res.getOutputStream();
                outSelected = true;
                /*
                 * Write from content's input stream to response output stream
                 */
                {
                    final int buflen = 0xFFFF;
                    final byte[] buffer = new byte[buflen];
                    for (int len; (len = attachmentInputStream.read(buffer, 0, buflen)) > 0;) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                }
            } finally {
                token.close();
                Streams.close(attachmentInputStream);
                if (null != file) {
                    file.delete();
                }
            }
        } catch (final OXException e) {
            callbackError(res, outSelected, e);
        } catch (final Exception e) {
            final OXException exc = getWrappingOXException(e);
            LOG.error(exc.getMessage(), exc);
            callbackError(res, outSelected, exc);
        }
    }

    private static boolean isMSIEOnWindows(final String userAgent) {
        final BrowserDetector browserDetector = new BrowserDetector(userAgent);
        return (browserDetector.isMSIE() && browserDetector.isWindows());
    }

    /**
     * Generates a wrapping {@link AbstractOXException} for specified exception.
     *
     * @param cause The exception to wrap
     * @return The wrapping {@link AbstractOXException}
     */
    protected static final OXException getWrappingOXException(final Exception cause) {
        if (LOG.isWarnEnabled()) {
            final StringBuilder warnBuilder = new StringBuilder(140);
            warnBuilder.append("An unexpected exception occurred, which is going to be wrapped for proper display.\n");
            warnBuilder.append("For safety reason its original content is display here.");
            LOG.warn(warnBuilder.toString(), cause);
        }
        return new OXException(cause);
    }

    private static void callbackError(final HttpServletResponse resp, final boolean outSelected, final OXException e) {
        try {
            resp.setContentType("text/html; charset=UTF-8");
            final Writer writer;
            if (outSelected) {
                /*
                 * Output stream has already been selected
                 */
                Tools.disableCaching(resp);
                writer =
                    new PrintWriter(new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(), resp.getCharacterEncoding())), true);
            } else {
                writer = resp.getWriter();
            }
            resp.setHeader("Content-Disposition", null);
            final Response response = new Response();
            response.setException(e);
            writer.write(substituteJS(ResponseWriter.getJSON(response).toString(), "error"));
            writer.flush();
        } catch (final UnsupportedEncodingException uee) {
            uee.initCause(e);
            LOG.error(uee.getMessage(), uee);
        } catch (final IOException ioe) {
            ioe.initCause(e);
            LOG.error(ioe.getMessage(), ioe);
        } catch (final IllegalStateException ise) {
            ise.initCause(e);
            LOG.error(ise.getMessage(), ise);
        } catch (final JSONException je) {
            je.initCause(e);
            LOG.error(je.getMessage(), je);
        }
    }

    private static String sanitizeHtml(final String htmlContent, final HtmlService htmlService) {
        return htmlService.sanitize(htmlContent, null, false, null, null);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html; charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html; charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

}
