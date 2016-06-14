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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.container.TmpFileFileHolder;
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
import com.openexchange.imagetransformation.BasicTransformedImage;
import com.openexchange.imagetransformation.Constants;
import com.openexchange.imagetransformation.ImageTransformationDeniedIOException;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.images.ImageTransformationUtility;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TransformImageAction} transforms the image if necessary
 *
 * Influence the following IDataWrapper attributes:
 * <ul>
 * <li>File
 * </ul>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class TransformImageAction implements IFileResponseRendererAction {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileResponseRenderer.class);

    private final AtomicReference<ImageTransformationService> scalerReference;

    /**
     * Initializes a new {@link TransformImageAction}.
     */
    public TransformImageAction() {
        super();
        scalerReference = new AtomicReference<ImageTransformationService>();
    }

    /**
     * Sets the scaler/image transformation service to use
     *
     * @param scaler The scaler
     */
    public void setScaler(ImageTransformationService scaler) {
        scalerReference.set(scaler);
    }

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
        // Check input
        String sourceContentType = fileHolder.getContentType();
        if (null == sourceContentType || false == Strings.toLowerCase(sourceContentType).startsWith("image/")) {
            String contentTypeByFileName = FileResponseRenderer.getContentTypeByFileName(fileHolder.getName());
            if (null != contentTypeByFileName) {
                sourceContentType = contentTypeByFileName;
            }
        }
        if (null == sourceContentType || false == sourceContentType.startsWith("image/")) {
            return fileHolder;
        }
        String sourceFormatName = Strings.toLowerCase(ImageTransformationUtility.getImageFormat(sourceContentType));

        // Check availability of scaler instance
        ImageTransformationService scaler = scalerReference.get();
        if (null == scaler) {
            return fileHolder;
        }

        // the optional parameter "transformationNeeded" is set by the PreviewImageResultConverter if no transformation is needed.
        // This is done if the preview was generated by the com.openexchage.documentpreview.OfficePreviewDocument service
        {
            Boolean transformationNeeded = request.getParameter("transformationNeeded", Boolean.class, true);
            if (null != transformationNeeded && transformationNeeded.booleanValue() == false) {
                return fileHolder;
            }
        }

        // Check if there is any need left to trigger image transformation
        IFileHolder file = fileHolder;
        {
            boolean transform = false;
            if (request.isSet("cropWidth") || request.isSet("cropHeight")) {
                transform = true;
            }
            if (!transform && (request.isSet("width") || request.isSet("height"))) {
                transform = true;
            }
            if (!transform) {
                final Boolean rotate = request.isSet("rotate") ? request.getParameter("rotate", Boolean.class) : null;
                if (null != rotate && rotate.booleanValue()) {
                    transform = true;
                }
            }
            final Boolean compress = request.isSet("compress") ? request.getParameter("compress", Boolean.class) : null;
            if (!transform && (null != compress && compress.booleanValue())) {
                transform = true;
            }
            // Rotation/compression only required for JPEG
            if (!transform) {
                if (("jpeg".equals(sourceFormatName) || "jpg".equals(sourceFormatName)) && !IDataWrapper.DOWNLOAD.equalsIgnoreCase(delivery)) {
                    // Ensure IFileHolder is repetitive
                    if (!file.repetitive()) {
                        file = new ThresholdFileHolder(file);
                    }

                    // Acquire stream and check for possible compression
                    InputStream stream = file.getStream();
                    if (null == stream) {
                        // Huh...?
                        LOG.warn("(Possible) Image file misses stream data");
                        return file;
                    }
                    try {
                        if (ImageTransformationUtility.requiresRotateTransformation(stream)) {
                            transform = true;
                        }
                    } finally {
                        Streams.close(stream);
                    }
                }
            }

            if (!transform) {
                return file;
            }
        }

        // Require session
        final ServerSession session = request.getSession();
        if (null == session) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("session");
        }

        // Check cache first
        final ResourceCache resourceCache;
        {
            final ResourceCache tmp = ResourceCaches.getResourceCache();
            resourceCache = null == tmp ? null : (tmp.isEnabledFor(session.getContextId(), session.getUserId()) ? tmp : null);
        }

        // Get eTag from result that provides the IFileHolder
        final String eTag = result.getHeader("ETag");
        final boolean isValidEtag = !isEmpty(eTag);
        final String previewLanguage = AbstractPreviewResultConverter.getUserLanguage(session);
        if (null != resourceCache && isValidEtag && AJAXRequestDataTools.parseBoolParameter("cache", request, true)) {
            final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, request, previewLanguage);
            final CachedResource cachedResource = resourceCache.get(cacheKey, 0, session.getContextId());
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
        // Ensure IFileHolder is repetitive
        if (!file.repetitive()) {
            ThresholdFileHolder tmp = new ThresholdFileHolder(file);
            file.close();
            file = tmp;
        }

        // Validate...
        {
            InputStream stream = file.getStream();
            try {
                if (null == stream) {
                    LOG.warn("(Possible) Image file misses stream data");
                    return file;
                }

                // Check for an animated .gif or svg image
                if ("svg".equals(sourceFormatName) || "gif".equals(sourceFormatName) && ImageUtils.isAnimatedGif(stream)) {
                    return fileHolder;
                }
            } finally {
                Streams.close(stream);
            }
        }

        // Start transformations: scale, rotate, ...
        ImageTransformations transformations;
        try {
            transformations = scaler.transfom(file, session.getSessionID());
        } catch (ImageTransformationDeniedIOException e) {
            // Rethrow as ImageTransformationDeniedIOException is specially handled in FileResponseRenderer
            throw e;
        }

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
            boolean shrinkOnly = request.isSet("shrinkOnly") && Boolean.parseBoolean(request.getParameter("shrinkOnly"));
            ScaleType scaleType = ScaleType.getType(request.getParameter("scaleType"));
            try {
                transformations.scale(maxWidth, maxHeight, scaleType, shrinkOnly);
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
            String targetContentType = getTargetContentType(sourceContentType);
            final BasicTransformedImage transformedImage;
            try {
                transformedImage = transformations.getTransformedImage(ImageTransformationUtility.getImageFormat(targetContentType));
                if (null == transformedImage) {
                    // ImageIO.read() returned null...
                    return file.repetitive() ? file : null;
                }

                int expenses = transformedImage.getTransformationExpenses();
                if (expenses >= ImageTransformations.HIGH_EXPENSE) {
                    cachingAdvised = true;
                }
            } catch (final IOException ioe) {
                if ("Unsupported Image Type".equals(ioe.getMessage())) {
                    return handleFailure(file);
                }
                // Rethrow...
                throw ioe;
            }

            ThresholdFileHolder optImageFileHolder = null;
            {
                IFileHolder fh = transformedImage.getImageFile();
                if (null != fh) {
                    optImageFileHolder = (ThresholdFileHolder) fh;
                }
            }
            final int size = (int) transformedImage.getSize();
            final String contentType = targetContentType;
            final String fileName = file.getName();


            // Check whether to add to cache
            if (cachingAdvised && null != resourceCache && isValidEtag && AJAXRequestDataTools.parseBoolParameter("cache", request, true)) {
                // (Asynchronously) Add to cache if possible
                final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, request, previewLanguage);

                final File imgFile;
                {
                    File tempFile = null == optImageFileHolder ? null : optImageFileHolder.getTempFile();
                    if (null != tempFile) {
                        // Copy to avoid preliminary file deletion; self care about file deletion
                        File newTempFile = TmpFileFileHolder.newTempFile(false);
                        FileUtils.copyFile(tempFile, newTempFile, false);
                        tempFile = newTempFile;
                    }
                    imgFile = tempFile;
                }

                final byte[] bytes;
                if (null == imgFile) {
                    bytes = (null == optImageFileHolder ? transformedImage.getImageData() : optImageFileHolder.toByteArray());
                } else {
                    // Do not need bytes if file is available
                    bytes = null;
                }

                AbstractTask<Void> task = new AbstractTask<Void>() {

                    @Override
                    public Void call() {
                        try {
                            CachedResource preview;
                            if (null != imgFile) {
                                preview = new CachedResource(new FileInputStream(imgFile), fileName, contentType, size);
                            } else {
                                preview = new CachedResource(bytes, fileName, contentType, size);
                            }

                            resourceCache.save(cacheKey, preview, 0, session.getContextId());
                        } catch (Exception e) {
                            LOG.warn("Could not cache preview.", e);
                        } finally {
                            if (null != imgFile) {
                                imgFile.delete();
                            }
                        }
                        return null;
                    }
                };
                ThreadPools.submitElseExecute(task);
            }

            IFileHolder imageFile;
            if (null == optImageFileHolder) {
                // Create new file having image-transformation content
                FileHolder.InputStreamClosure closure = new TransformedImageInputStreamClosure(transformedImage);
                imageFile = new FileHolder(closure, size, contentType, fileName);
            } else {
                optImageFileHolder.setName(fileName);
                optImageFileHolder.setContentType(contentType);
                imageFile = optImageFileHolder;
            }

            // Cleanse old one
            Streams.close(file);

            return imageFile;
        } catch (final RuntimeException e) {
            if (LOG.isDebugEnabled() && file.repetitive()) {
                try {
                    final File tmpFile = writeBrokenImage2Disk(file, tmpDirReference);
                    LOG.error("Unable to transform image from {}. Unparseable image file is written to disk at: {}", file.getName(), tmpFile.getPath(), e);
                } catch (final Exception x) {
                    LOG.error("Unable to transform image from {}", file.getName(), e);
                }
            } else {
                LOG.error("Unable to transform image from {}", file.getName(), e);
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
     * @param request The request to get the parameter from
     * @param name The parameter name
     * @return The parameter, or <code>0</code> if not set
     */
    private int optIntParameter(AJAXRequestData request, String name) throws OXException {
        if (request.isSet(name)) {
            Integer integer = request.getParameter(name, int.class);
            return null != integer ? integer.intValue() : 0;
        }
        return 0;
    }

    private IFileHolder handleFailure(IFileHolder file) {
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

    private final class TransformedImageInputStreamClosure implements FileHolder.InputStreamClosure {

        private final BasicTransformedImage transformedImage;

        TransformedImageInputStreamClosure(BasicTransformedImage transformedImage) {
            this.transformedImage = transformedImage;
        }

        @Override
        public InputStream newStream() throws OXException, IOException {
            return transformedImage.getImageStream();
        }
    }

    /**
     * Gets the target content type to use for a transformed image, based on the type of the source image type.
     *
     * @param sourceContentType The source image's content type file
     * @return The target content type, falling back to <code>image/jpeg</code> for unknown or mostly unsupported content types
     */
    private static String getTargetContentType(String sourceContentType) {
        if (Strings.isNotEmpty(sourceContentType) && (
                "image/bmp".equals(sourceContentType) ||
                "image/gif".equals(sourceContentType) ||
                "image/png".equals(sourceContentType))) {
            return sourceContentType;
        }
        return "image/jpeg";
    }

}
