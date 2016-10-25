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

package com.openexchange.ajax.requesthandler.responseRenderers.actions;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.java.HTMLDetector;
import com.openexchange.mail.mime.ContentType;

/**
 * {@link PrepareResponseHeaderAction} prepares the header of the response object
 *
 * Influence the following IDataWrapper attributes:
 * <ul>
 * <li>response
 * <li>length
 * <li>closeables
 * <li>documentData
 * <li>contentType
 * </ul>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class PrepareResponseHeaderAction implements IFileResponseRendererAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileResponseRenderer.class);

    @Override
    public void call(IDataWrapper data) throws OXException, IOException {
        if (IDataWrapper.DOWNLOAD.equalsIgnoreCase(data.getDelivery()) || (IDataWrapper.SAVE_AS_TYPE.equals(data.getContentType()) && !IDataWrapper.VIEW.equalsIgnoreCase(data.getDelivery()))) {
            // Write as a common file download: application/octet-stream
            final StringBuilder sb = new StringBuilder(32);
            sb.append(isEmpty(data.getContentDisposition()) ? "attachment" : checkedContentDisposition(data.getContentDisposition().trim(), data.getFile()));
            DownloadUtility.appendFilenameParameter(data.getFileName(), null, data.getUserAgent(), sb);
            data.getResponse().setHeader("Content-Disposition", sb.toString());
            data.getResponse().setContentType(IDataWrapper.SAVE_AS_TYPE);
            data.setLength(data.getFile().getLength());
            setContentLengthHeader(data.getLength(), data.getResponse());
        } else {
            // Determine what Content-Type is indicated by file name
            String contentTypeByFileName = FileResponseRenderer.getContentTypeByFileName(data.getFileName());
            // Generate checked download
            final CheckedDownload checkedDownload;
            {
                long fileLength = data.getFile().getLength();
                String cts;
                if (null == data.getFileContentType() || IDataWrapper.SAVE_AS_TYPE.equals(data.getFileContentType())) {
                    if (null == contentTypeByFileName) {
                        // Let Tika detect the Content-Type
                        ThresholdFileHolder temp = new ThresholdFileHolder(IDataWrapper.DEFAULT_IN_MEMORY_THRESHOLD, IDataWrapper.INITIAL_CAPACITY);
                        data.addCloseable(temp);
                        temp.write(data.getDocumentData());
                        // We know for sure
                        fileLength = temp.getLength();
                        data.setDocumentData(temp.getClosingRandomAccess());
                        cts = detectMimeType(temp.getStream());
                        if ("text/plain".equals(cts)) {
                            cts = HTMLDetector.containsHTMLTags(temp.getStream(), true) ? "text/html" : cts;
                        }
                    } else {
                        cts = contentTypeByFileName;
                    }
                } else {
                    if ((null != contentTypeByFileName) && !equalPrimaryTypes(data.getFileContentType(), contentTypeByFileName)) {
                        // Differing Content-Types sources
                        final ThresholdFileHolder temp = new ThresholdFileHolder(IDataWrapper.DEFAULT_IN_MEMORY_THRESHOLD, IDataWrapper.INITIAL_CAPACITY);
                        data.addCloseable(temp);
                        temp.write(data.getDocumentData());
                        // We know for sure
                        fileLength = temp.getLength();
                        data.setDocumentData(temp.getClosingRandomAccess());
                        cts = detectMimeType(temp.getStream());
                        if ("text/plain".equals(cts)) {
                            cts = HTMLDetector.containsHTMLTags(temp.getStream(), true) ? "text/html" : cts;
                        }
                    } else {
                        cts = data.getFileContentType();
                    }
                }
                checkedDownload = DownloadUtility.checkInlineDownload(data.getDocumentData(), fileLength, data.getFileName(), cts, data.getContentDisposition(), data.getUserAgent(), data.getRequestData().getSession());
            }
            /*
             * Set stream
             */
            data.setDocumentData(checkedDownload.getInputStream());
            /*
             * Set headers...
             */
            if (data.getDelivery() == null || !data.getDelivery().equalsIgnoreCase(IDataWrapper.VIEW)) {
                if (isEmpty(data.getContentDisposition())) {
                    data.getResponse().setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                } else {
                    if (data.getContentDisposition().indexOf(';') >= 0) {
                        data.getResponse().setHeader("Content-Disposition", data.getContentDisposition().trim());
                    } else {
                        final String disposition = checkedDownload.getContentDisposition();
                        final int pos = disposition.indexOf(';');
                        if (pos >= 0) {
                            data.getResponse().setHeader("Content-Disposition", data.getContentDisposition().trim() + disposition.substring(pos));
                        } else {
                            data.getResponse().setHeader("Content-Disposition", data.getContentDisposition().trim());
                        }
                    }
                }
            } else if (checkedDownload.isAttachment()) {
                // Force attachment download
                data.getResponse().setHeader("Content-Disposition", checkedDownload.getContentDisposition());
            } else if (data.getDelivery().equalsIgnoreCase(IDataWrapper.VIEW) && null != data.getFileName()) {
                final StringBuilder sb = new StringBuilder(32);
                sb.append("inline");
                DownloadUtility.appendFilenameParameter(data.getFileName(), null, data.getUserAgent(), sb);
                data.getResponse().setHeader("Content-Disposition", sb.toString());
            }
            /*
             * Set Content-Length if possible
             */
            data.setLength(checkedDownload.getSize());
            setContentLengthHeader(data.getLength(), data.getResponse());

            /*-
             * Determine preferred Content-Type
             *
             * 1. Ensure contentTypeByFileName has a valid value
             *
             * 2. Reset checkedContentType if
             *   - it is set to "application/octet-stream"
             *   - it's primary type is not equal to contentTypeByFileName's primary type; e.g. both start with "text/"
             */
            String preferredContentType = checkedDownload.getContentType();
            if (null != contentTypeByFileName && !checkedDownload.isAttachment()) {
                if (preferredContentType.startsWith(IDataWrapper.SAVE_AS_TYPE) || !equalPrimaryTypes(preferredContentType, contentTypeByFileName)) {
                    try {
                        final ContentType tmp = new ContentType(preferredContentType);
                        tmp.setBaseType(contentTypeByFileName);
                        preferredContentType = tmp.toString();
                    } catch (final Exception e) {
                        preferredContentType = contentTypeByFileName;
                    }
                }
            }

            if (!data.getContentTypeByParameter().booleanValue() || data.getContentType() == null || IDataWrapper.SAVE_AS_TYPE.equals(data.getContentType())) {
                // Either no Content-Type parameter specified or set to "application/octet-stream"
                data.getResponse().setContentType(preferredContentType);
                data.setContentType(preferredContentType);
            } else {
                // A Content-Type parameter is specified...
                if (IDataWrapper.SAVE_AS_TYPE.equals(preferredContentType) || equalTypes(preferredContentType, data.getContentType())) {
                    // Set if sanitize-able
                    if (!trySetSanitizedContentType(data.getContentType(), preferredContentType, data.getResponse())) {
                        data.setContentType(preferredContentType);
                    }
                } else {
                    // Specified Content-Type does NOT match file's real MIME type. Ignore it due to security reasons (see bug #25343)
                    final StringBuilder sb = new StringBuilder(128);
                    sb.append("Denied parameter \"").append(IDataWrapper.PARAMETER_CONTENT_TYPE);
                    sb.append("\" due to security constraints (requested \"");
                    sb.append(data.getContentType()).append("\" , but is \"").append(preferredContentType).append("\").");
                    LOG.warn(sb.toString());
                    data.getResponse().setContentType(preferredContentType);
                    data.setContentType(preferredContentType);
                }
            }
        }
    }

    /**
     * Checks specified <i>Content-Disposition</i> value against passed {@link IFileHolder file}.
     * <p>
     * E.g. <code>"inline"</code> is not allowed for <code>"text/html"</code> MIME type.
     *
     * @param contentDisposition The <i>Content-Disposition</i> value to check
     * @param file The file
     * @return The checked <i>Content-Disposition</i> value
     */
    private String checkedContentDisposition(final String contentDisposition, final IFileHolder file) {
        final String ct = com.openexchange.java.Strings.toLowerCase(file.getContentType()); // null-safe
        if (null == ct || ct.startsWith("text/htm")) {
            final int pos = contentDisposition.indexOf(';');
            return pos > 0 ? "attachment" + contentDisposition.substring(pos) : "attachment";
        }
        return contentDisposition;
    }

    private void setContentLengthHeader(final long length, final HttpServletResponse resp) {
        if (length > 0) {
            resp.setHeader("Accept-Ranges", "bytes");
            resp.setHeader("Content-Length", Long.toString(length));
        } else {
            resp.setHeader("Accept-Ranges", "none");
        }
    }

    private String detectMimeType(final InputStream in) throws IOException {
        return AJAXUtility.detectMimeType(in);
    }

    private boolean equalTypes(final String contentType1, final String contentType2) {
        if (null == contentType1 || null == contentType2) {
            return false;
        }
        return com.openexchange.java.Strings.toLowerCase(getType(contentType1)).startsWith(com.openexchange.java.Strings.toLowerCase(getType(contentType2)));
    }

    private String getType(final String contentType) {
        if (isEmpty(contentType)) {
            return contentType;
        }
        final int pos = contentType.indexOf(';');
        return pos > 0 ? contentType.substring(0, pos) : contentType;
    }

    private boolean equalPrimaryTypes(final String contentType1, final String contentType2) {
        if (null == contentType1 || null == contentType2) {
            return false;
        }
        return com.openexchange.java.Strings.toLowerCase(getPrimaryType(contentType1)).startsWith(com.openexchange.java.Strings.toLowerCase(getPrimaryType(contentType2)));
    }

    private String getPrimaryType(final String contentType) {
        if (isEmpty(contentType)) {
            return contentType;
        }
        final int pos = contentType.indexOf('/');
        return pos > 0 ? contentType.substring(0, pos) : contentType;
    }

    /** Attempts to set a sanitized <code>Content-Type</code> header value to given HTTP response. */
    private boolean trySetSanitizedContentType(final String contentType, final String fallbackContentType, final HttpServletResponse resp) {
        try {
            resp.setContentType(new ContentType(contentType).getBaseType());
            return true;
        } catch (final Exception e) {
            // Ignore
            resp.setContentType(fallbackContentType);
        }
        return false;
    }

}
