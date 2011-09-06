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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.java.Streams.close;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.tools.images.ImageScalingService;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link FileResponseRenderer}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileResponseRenderer implements ResponseRenderer {

    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(FileResponseRenderer.class));

    private static final int BUFLEN = 2048;

    private static final String PARAMETER_CONTENT_DISPOSITION = "content_disposition";

    private static final String PARAMETER_CONTENT_TYPE = "content_type";

    protected static final String SAVE_AS_TYPE = "application/octet-stream";

    private volatile ImageScalingService scaler;

    /**
     * Initializes a new {@link FileResponseRenderer}.
     */
    public FileResponseRenderer() {
        super();
    }

    @Override
    public int getRanking() {
        return 0;
    }

    /**
     * Sets the image scaler.
     * 
     * @param scaler The image scaler
     */
    public void setScaler(final ImageScalingService scaler) {
        this.scaler = scaler;
    }

    @Override
    public boolean handles(final AJAXRequestData request, final AJAXRequestResult result) {
        final Object data = result.getResultObject();
        if (data == null) {
            return false;
        }
        return IFileHolder.class.isAssignableFrom(data.getClass());
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest req, final HttpServletResponse resp) {
        IFileHolder file = (IFileHolder) result.getResultObject();

        final String contentType = req.getParameter(PARAMETER_CONTENT_TYPE);
        final String userAgent = req.getHeader("user-agent");
        String contentDisposition = req.getParameter(PARAMETER_CONTENT_DISPOSITION);
        if (null == contentDisposition) {
            contentDisposition = file.getDisposition();
        }
        final String name = file.getName();

        InputStream documentData = null;
        try {
            file = scaleIfImage(request, file);
            documentData = new BufferedInputStream(file.getStream());
            if (SAVE_AS_TYPE.equals(contentType)) {
                Tools.setHeaderForFileDownload(userAgent, resp, name, contentDisposition);
                resp.setContentType(contentType);
            } else {
                final CheckedDownload checkedDownload =
                    DownloadUtility.checkInlineDownload(documentData, name, file.getContentType(), contentDisposition, userAgent);
                if (contentDisposition == null) {
                    resp.setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                } else {
                    resp.setHeader("Content-Disposition", contentDisposition);
                }
                if (contentType == null) {
                    resp.setContentType(checkedDownload.getContentType());
                } else {
                    resp.setContentType(contentType);
                }
                documentData = checkedDownload.getInputStream();
            }
            /*
             * Browsers don't like the Pragma header the way we usually set this. Especially if files are sent to the browser. So removing
             * pragma header.
             */
            Tools.removeCachingHeader(resp);
            /*
             * ETag present?
             */
            final String eTag = result.getHeader("ETag");
            if (null != eTag) {
                final long expires = result.getExpires();
                if (expires > 0) {
                    final long millis = System.currentTimeMillis() + expires;
                    Tools.setETag(eTag, new Date(millis), resp);
                } else {
                    Tools.setETag(eTag, null, resp);
                }
            }
            /*
             * Output binary content
             */
            final ServletOutputStream outputStream = resp.getOutputStream();
            final int len = BUFLEN;
            final byte[] buf = new byte[len];
            for (int read; (read = documentData.read(buf, 0, len)) > 0;) {
                outputStream.write(buf, 0, read);
            }
            outputStream.flush();
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        } finally {
             close(documentData);
        }
    }

    /**
     * Scale possible image data.
     * 
     * @param request The request data
     * @param file The file holder
     * @return The possibly scaled file holder
     * @throws IOException If an I/O error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    private IFileHolder scaleIfImage(final AJAXRequestData request, final IFileHolder file) throws IOException, OXException {
        final ImageScalingService scaler = this.scaler;
        if (scaler == null) {
            return file;
        }
        /*
         * Check content type
         */
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file.getName());
            if (!contentType.startsWith("image/")) {
                return file;
            }
        }
        /*
         * Start scaling if appropriate parameters are present
         */
        int width = -1, height = -1;
        if (request.isSet("width")) {
            width = request.getParameter("width", int.class).intValue();
        }
        if (request.isSet("height")) {
            height = request.getParameter("height", int.class).intValue();
        }
        if (width == -1 && height == -1) {
            return file;
        }
        /*
         * Scale to new input stream
         */
        final InputStream scaled = scaler.scale(file.getStream(), width, height);
        return new FileHolder(scaled, -1, "image/png", "");
    }

}
