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

import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.images.ImageScalingService;
import com.openexchange.tools.images.ScaleType;
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
    
    private final String DELIVERY = "delivery";
    
    private final String DOWNLOAD = "download";
    
    private final String VIEW = "view";

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
        return result.getResultObject() instanceof IFileHolder;
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest req, final HttpServletResponse resp) {
        IFileHolder file = (IFileHolder) result.getResultObject();

        final String contentType = req.getParameter(PARAMETER_CONTENT_TYPE);
        String delivery = req.getParameter(DELIVERY);
        if (delivery == null) {
            delivery = file.getDelivery();
        }
        String contentDisposition = req.getParameter(PARAMETER_CONTENT_DISPOSITION);
        if (null == contentDisposition) {
            contentDisposition = file.getDisposition();
        }

        InputStream documentData = null;
        try {
            file = rotateIfImage(file);
            file = cropIfImage(request, file);
            file = scaleIfImage(request, file);
            documentData = new BufferedInputStream(file.getStream());
            final String userAgent = req.getHeader("user-agent");
            if (SAVE_AS_TYPE.equals(contentType) || (delivery != null && delivery.equalsIgnoreCase(DOWNLOAD))) {
                Tools.setHeaderForFileDownload(userAgent, resp, file.getName(), contentDisposition);
                resp.setContentType(contentType);
            } else {
                final CheckedDownload checkedDownload = DownloadUtility.checkInlineDownload(documentData, file.getName(), file.getContentType(), contentDisposition, userAgent);
                if (delivery == null || !delivery.equalsIgnoreCase(VIEW)) {
                    if (contentDisposition == null) {
                        resp.setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                    } else {
                        if (contentDisposition.indexOf(';') < 0) {
                            final String disposition = checkedDownload.getContentDisposition();
                            final int pos = disposition.indexOf(';');
                            if (pos >= 0) {
                                resp.setHeader("Content-Disposition", contentDisposition + disposition.substring(pos));
                            } else {
                                resp.setHeader("Content-Disposition", contentDisposition);
                            }
                        } else {
                            resp.setHeader("Content-Disposition", contentDisposition);
                        }
                    }
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
            if (delivery == null || !delivery.equalsIgnoreCase(DOWNLOAD)) {
                /*
                 * ETag present and caching?
                 */
                final String eTag = result.getHeader("ETag");
                if (null != eTag) {
                    final long expires = result.getExpires();
                    Tools.setETag(eTag, expires > 0 ? new Date(System.currentTimeMillis() + expires) : null, resp);
                } else {
                    final long expires = result.getExpires();
                    if (expires < 0) {
                        Tools.setExpiresInOneYear(resp);
                    } else if (expires > 0) {
                        Tools.setExpires(new Date(System.currentTimeMillis() + expires), resp);
                    }
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
            close(file);
            close(documentData);
        }
    }

    private IFileHolder rotateIfImage(IFileHolder file) throws IOException, OXException {
        final ImageScalingService scaler = this.scaler;
        if (scaler == null) {
            return file;
        }

        if (!isImage(file)) {
            return file;
        }

        InputStream rotated = scaler.rotateAccordingExif(file.getStream(), file.getContentType());
        file = new FileHolder(rotated, -1, file.getContentType(), "");
        return file;
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

        if (!isImage(file)) {
            return file;
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
        try {
            /*
             * Scale to new input stream
             */
            final InputStream scaled = scaler.scale(file.getStream(), width, height, ScaleType.getType(request.getParameter("scaleType")));
            return new FileHolder(scaled, -1, "image/png", "");
        } finally {
            Streams.close(file);
        }
    }
    
    private IFileHolder cropIfImage(AJAXRequestData request, IFileHolder file) throws IOException, OXException {
    	if (null == this.scaler || false == isImage(file) || false == request.isSet("cropWidth") || false == request.isSet("cropHeight")) {
    		return file;
    	}
    	/*
    	 * get crop parameters
    	 */
    	int cropX = request.isSet("cropX") ? request.getParameter("cropX", int.class).intValue() : 0;
    	int cropY = request.isSet("cropY") ? request.getParameter("cropY", int.class).intValue() : 0;
    	int cropWidth = request.getParameter("cropWidth", int.class).intValue();
    	int cropHeight = request.getParameter("cropHeight", int.class).intValue();
    	/*
    	 * crop to new input stream
    	 */
        try {
            InputStream croppedImage = scaler.crop(file.getStream(), cropX, cropY, cropWidth, cropHeight, file.getContentType());
            return new FileHolder(croppedImage, -1, file.getContentType(), file.getName());
        } finally {
            Streams.close(file);
        }    
    }

    private boolean isImage(IFileHolder file) {
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            String fileName = file.getName();
            if (fileName == null || !(contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName)).startsWith("image/")) {
                return false;
            }
        }
        return true;
    }

}
