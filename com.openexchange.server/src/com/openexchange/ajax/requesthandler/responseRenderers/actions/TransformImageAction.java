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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.ByteArrayInputStreamClosure;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.helper.ImageUtils;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
import com.openexchange.ajax.requesthandler.converters.preview.AbstractPreviewResultConverter;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer.FileResponseRendererActionException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.images.Constants;
import com.openexchange.tools.images.ImageTransformationService;
import com.openexchange.tools.images.ImageTransformationUtility;
import com.openexchange.tools.images.ImageTransformations;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.images.TransformedImage;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TransformImageAction} transforms the image if necessary
 *
 * Influence the following IDataWrapper attributes:
 * -File
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class TransformImageAction implements IFileResponseRendererAction {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileResponseRenderer.class);

    private ImageTransformationService scaler;

    @Override
    public void call(IDataWrapper data) throws Exception {
        IFileHolder file = transformIfImage(data.getRequestData(), data.getResult(), data.getFile(), data.getDelivery(), data.getTmpDirReference());
        if (null == file) {
            // Quit with 404
            throw new FileResponseRenderer.FileResponseRendererActionException(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }
        data.setFile(file);
    }

    private IFileHolder transformIfImage(final AJAXRequestData request, final AJAXRequestResult result, final IFileHolder fileHolder, final String delivery, AtomicReference<File> tmpDirReference) throws IOException, OXException, FileResponseRendererActionException {
        /*
         * check input
         */
        final ImageTransformationService scaler = this.scaler;
        if (null == scaler || false == isImage(fileHolder)) {
            return fileHolder;
        }

        // the optional parameter "transformationNeeded" is set by the PreviewImageResultConverter if no transformation is needed.
        // This is done if the preview was generated by the com.openexchage.documentpreview.OfficePreviewDocument service
        if (request.isSet("transformationNeeded") && (request.getParameter("transformationNeeded", Boolean.class).booleanValue() == false)) {
            return fileHolder;
        }

        // Check if there is any need left to trigger image transformation
        {
            boolean transform = false;
            if (request.isSet("cropWidth") || request.isSet("cropHeight")) {
                transform = true;
            }
            if (!transform && (request.isSet("width") || request.isSet("height"))) {
                transform = true;
            }
            final Boolean rotate = request.isSet("rotate") ? request.getParameter("rotate", Boolean.class) : null;
            if (!transform && (null != rotate && rotate.booleanValue())) {
                transform = true;
            }
            final Boolean compress = request.isSet("compress") ? request.getParameter("compress", Boolean.class) : null;
            if (!transform && (null != compress && compress.booleanValue())) {
                transform = true;
            }
            // Rotation/compression only required for JPEG
            if (!transform) {
                final String formatName = com.openexchange.java.Strings.toLowerCase(ImageTransformationUtility.getImageFormat(fileHolder.getContentType()));
                if (("jpeg".equals(formatName) || "jpg".equals(formatName)) && !IDataWrapper.DOWNLOAD.equalsIgnoreCase(delivery)) {
                    // Check for possible compression
                    transform = true;
                }
            }

            if (!transform) {
                return fileHolder;
            }
        }

        // Check cache first
        final ResourceCache resourceCache;
        {
            final ResourceCache tmp = ResourceCaches.getResourceCache();
            resourceCache = null == tmp ? null : (tmp.isEnabledFor(request.getSession().getContextId(), request.getSession().getUserId()) ? tmp : null);
        }

        // Get eTag from result that provides the IFileHolder
        final String eTag = result.getHeader("ETag");
        final boolean isValidEtag = !isEmpty(eTag);
        final String previewLanguage = AbstractPreviewResultConverter.getUserLanguage(request.getSession());
        if (null != resourceCache && isValidEtag && AJAXRequestDataTools.parseBoolParameter("cache", request, true)) {
            final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, request, previewLanguage);
            final CachedResource cachedResource = resourceCache.get(cacheKey, 0, request.getSession().getContextId());
            if (null != cachedResource) {
                // Scaled version already cached
                // Create appropriate IFileHolder
                String contentType = cachedResource.getFileType();
                if (null == contentType) {
                    contentType = "image/jpeg";
                }
                final IFileHolder ret;
                {
                    final InputStream inputStream = cachedResource.getInputStream();
                    if (null == inputStream) {
                        @SuppressWarnings("resource") final ByteArrayFileHolder responseFileHolder = new ByteArrayFileHolder(cachedResource.getBytes());
                        responseFileHolder.setContentType(contentType);
                        responseFileHolder.setName(cachedResource.getFileName());
                        ret = responseFileHolder;
                    } else {
                        // From stream
                        ret = new FileHolder(inputStream, cachedResource.getSize(), contentType, cachedResource.getFileName());
                    }
                }
                return ret;
            }
        }

        // OK, so far we assume image transformation is needed
        IFileHolder file = fileHolder;

        // Build transformations
        InputStream stream = file.getStream();
        if (null == stream) {
            LOG.warn("(Possible) Image file misses stream data");
            return file;
        }

        // Check for an animated .gif image
        {
            if (file.repetitive()) {
                if (ImageUtils.isAnimatedGif(stream)) {
                    return fileHolder;
                }
                stream = file.getStream();
            } else {
                final AtomicReference<InputStream> ref = new AtomicReference<InputStream>();
                if (ImageUtils.isAnimatedGif(stream, ref)) {
                    return new FileHolder(ref.get(), -1, file.getContentType(), file.getName());
                }
                stream = ref.get();
            }
        }

        // Mark stream if possible
        final boolean markSupported = file.repetitive() ? false : stream.markSupported();
        if (markSupported) {
            stream.mark(131072); // 128KB
        }

        // Start transformations: scale, rotate, ...
        ImageTransformations transformations = scaler.transfom(stream, request.getSession().getSessionID());

        // Rotate by default when not delivering as download
        Boolean rotate = request.isSet("rotate") ? request.getParameter("rotate", Boolean.class) : null;
        if (null == rotate && false == IDataWrapper.DOWNLOAD.equalsIgnoreCase(delivery) || null != rotate && rotate.booleanValue()) {
            transformations.rotate();
        }
        if (request.isSet("cropWidth") || request.isSet("cropHeight")) {
            int cropX = optIntParameter(request, "cropX");
            int cropY = optIntParameter(request, "cropY");
            int cropWidth = optIntParameter(request, "cropWidth");
            int cropHeight = optIntParameter(request, "cropHeight");
            transformations.crop(cropX, cropY, cropWidth, cropHeight);
        }
        if (request.isSet("width") || request.isSet("height")) {
            int maxWidth = optIntParameter(request, "width");
            if (maxWidth > Constants.getMaxWidth()) {
                throw AjaxExceptionCodes.BAD_REQUEST.create("Width " + maxWidth + " exceeds max. supported width " + Constants.getMaxWidth());
            }
            int maxHeight = optIntParameter(request, "height");
            if (maxHeight > Constants.getMaxHeight()) {
                throw AjaxExceptionCodes.BAD_REQUEST.create("Height " + maxHeight + " exceeds max. supported height " + Constants.getMaxHeight());
            }
            ScaleType scaleType = ScaleType.getType(request.getParameter("scaleType"));
            try {
                transformations.scale(maxWidth, maxHeight, scaleType);
            } catch (final IllegalArgumentException e) {
                throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create(e, e.getMessage());
            }
        }

        // Compress by default when not delivering as download
        Boolean compress = request.isSet("compress") ? request.getParameter("compress", Boolean.class) : null;
        if ((null == compress && false == IDataWrapper.DOWNLOAD.equalsIgnoreCase(delivery)) || (null != compress && compress.booleanValue())) {
            transformations.compress();
        }

        // Transform
        boolean cachingAdvised = false;
        try {
            String fileContentType = file.getContentType();
            if (null == fileContentType || !Strings.toLowerCase(fileContentType).startsWith("image/")) {
                final String contentTypeByFileName = FileResponseRenderer.getContentTypeByFileName(file.getName());
                if (null != contentTypeByFileName) {
                    fileContentType = contentTypeByFileName;
                }
            }
            final byte[] transformed;
            try {
                TransformedImage transformedImage = transformations.getTransformedImage(fileContentType);
                int expenses = transformedImage.getTransformationExpenses();
                if (expenses >= ImageTransformations.HIGH_EXPENSE) {
                    cachingAdvised = true;
                }

                transformed = transformedImage.getImageData();
            } catch (final IOException ioe) {
                if ("Unsupported Image Type".equals(ioe.getMessage())) {
                    return handleFailure(file, stream, markSupported);
                }
                // Rethrow...
                throw ioe;
            }
            if (null == transformed) {
                LOG.debug("Got no resulting input stream from transformation, trying to recover original input");
                return handleFailure(file, stream, markSupported);
            }

            // Return immediately if not cacheable
            if (!cachingAdvised || null == resourceCache || !isValidEtag || !AJAXRequestDataTools.parseBoolParameter("cache", request, true)) {
                return new FileHolder(Streams.newByteArrayInputStream(transformed), -1, fileContentType, file.getName());
            }

            // (Asynchronously) Add to cache if possible
            final int size = transformed.length;
            final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, request, previewLanguage);
            final ServerSession session = request.getSession();
            final String fileName = file.getName();
            final String contentType = fileContentType;
            final AbstractTask<Void> task = new AbstractTask<Void>() {

                @Override
                public Void call() {
                    try {
                        final CachedResource preview = new CachedResource(transformed, fileName, contentType, size);
                        resourceCache.save(cacheKey, preview, 0, session.getContextId());
                    } catch (OXException e) {
                        LOG.warn("Could not cache preview.", e);
                    }

                    return null;
                }
            };

            // Acquire thread pool service
            final ThreadPoolService threadPool = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
            if (null == threadPool) {
                final Thread thread = Thread.currentThread();
                boolean ran = false;
                task.beforeExecute(thread);
                try {
                    task.call();
                    ran = true;
                    task.afterExecute(null);
                } catch (final Exception ex) {
                    if (!ran) {
                        task.afterExecute(ex);
                    }
                    // Else the exception occurred within
                    // afterExecute itself in which case we don't
                    // want to call it again.
                    throw (ex instanceof OXException ? (OXException) ex : AjaxExceptionCodes.UNEXPECTED_ERROR.create(ex, ex.getMessage()));
                }
            } else {
                threadPool.submit(task);
            }
            // Return
            return new FileHolder(new ByteArrayInputStreamClosure(transformed), size, contentType, fileName);
        } catch (final RuntimeException e) {
            if (LOG.isDebugEnabled() && file.repetitive()) {
                try {
                    final File tmpFile = writeBrokenImage2Disk(file, tmpDirReference);
                    LOG.error("Unable to transform image from {}. Unparseable image file is written to disk at: {}", file.getName(), tmpFile.getPath());
                } catch (final Exception x) {
                    LOG.error("Unable to transform image from {}", file.getName());
                }
            } else {
                LOG.error("Unable to transform image from {}", file.getName());
            }

            IFileHolder returnValue = file.repetitive() ? file : null;
            if (returnValue == null) {
                // Quit with 404
                throw new FileResponseRenderer.FileResponseRendererActionException(HttpServletResponse.SC_NOT_FOUND, "File not found.");
            }
            return returnValue;
        }
    }

    /**
     * Optionally parses a specific numerical parameter from the supplied request data.
     *
     * @param request The request to get the paramter from
     * @param name The parameter, or <code>0</code> if not set
     * @return
     */
    private int optIntParameter(AJAXRequestData request, String name) throws OXException {
        if (request.isSet(name)) {
            Integer integer = request.getParameter("width", int.class);
            return null != integer ? integer.intValue() : 0;
        }
        return 0;
    }

    private boolean isImage(final IFileHolder file) {
        if (0 == file.getLength()) {
            // File signals no available data
            return false;
        }
        String contentType = file.getContentType();
        if (null == contentType || !contentType.startsWith("image/")) {
            final String fileName = file.getName();
            if (fileName == null || !(contentType = MimeType2ExtMap.getContentType(fileName)).startsWith("image/")) {
                return false;
            }
        }
        return true;
    }

    private IFileHolder handleFailure(final IFileHolder file, final InputStream stream, final boolean markSupported) {
        if (markSupported) {
            try {
                stream.reset();
                return file;
            } catch (final Exception e) {
                LOG.warn("Error resetting input stream", e);
            }
        }
        LOG.warn("Unable to transform image from {}", file.getName());
        return file.repetitive() ? file : null;
    }

    private File writeBrokenImage2Disk(final IFileHolder file, AtomicReference<File> tmpDirReference) throws IOException, OXException, FileNotFoundException {
        String suffix = null;
        {
            final String name = file.getName();
            if (null != name) {
                final int pos = name.lastIndexOf('.');
                if (pos > 0 && pos < name.length() - 1) {
                    suffix = name.substring(pos);
                }
            }
            if (null == suffix) {
                final String contentType = file.getContentType();
                if (null != contentType) {
                    suffix = "." + MimeType2ExtMap.getFileExtension(contentType);
                }
            }
        }
        return write2Disk(file, "brokenimage-", suffix, tmpDirReference);
    }

    private File write2Disk(final IFileHolder file, final String prefix, final String suffix, AtomicReference<File> tmpDirReference) throws IOException, OXException, FileNotFoundException {
        final File directory = tmpDirReference.get();
        final File newFile = File.createTempFile(null == prefix ? "open-xchange-" : prefix, null == suffix ? ".tmp" : suffix, directory);
        final InputStream is = file.getStream();
        final OutputStream out = new FileOutputStream(newFile);
        try {
            final int len = 8192;
            final byte[] buf = new byte[len];
            for (int read; (read = is.read(buf, 0, len)) > 0;) {
                out.write(buf, 0, read);
            }
            out.flush();
        } finally {
            Streams.close(is, out);
        }
        return newFile;
    }

    /**
     * Sets the scaler/image transformation service to use
     *
     * @param scaler The scaler
     */
    public void setScaler(ImageTransformationService scaler) {
        this.scaler = scaler;
    }

}
