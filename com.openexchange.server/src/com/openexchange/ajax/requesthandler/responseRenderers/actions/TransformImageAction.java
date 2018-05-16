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

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.lowerCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.BasicTransformedImage;
import com.openexchange.imagetransformation.Constants;
import com.openexchange.imagetransformation.ImageTransformationDeniedIOException;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.Utility;
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

    /**
     * {@link TransformedImageInputStreamClosure}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.8.1
     */
    protected static final class TransformedImageInputStreamClosure implements FileHolder.InputStreamClosure {

        /**
         * Initializes a new {@link TransformedImageInputStreamClosure}.
         */
        TransformedImageInputStreamClosure(BasicTransformedImage transformedImage) {
            m_transformedImage = transformedImage;
        }

        @Override
        public InputStream newStream() throws OXException, IOException {
            return m_transformedImage.getImageStream();
        }

        // - Members ---------------------------------------------------------------

        /**
         * m_transformedImage
         */
        private final BasicTransformedImage m_transformedImage;
    }

    /**
     * Initializes a new {@link TransformImageAction}.
     */
    public TransformImageAction() {
        super();
    }

    /**
     * Sets the scaler/image transformation service to use
     *
     * @param scaler The scaler
     */
    public void setScaler(ImageTransformationService scaler) {
        m_scalerReference.set(scaler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.requesthandler.responseRenderers.actions.IFileResponseRendererAction#call(com.openexchange.ajax.requesthandler.responseRenderers.actions.IDataWrapper)
     */
    @Override
    public void call(final IDataWrapper data) throws Exception {
        // closing of internal resources will be handled by FileHolder set at DataWrapper
        final IFileHolder file = transformIfImage(data.getRequestData(), data.getResult(), data.getFile(), data.getDelivery(), data.getTmpDirReference());

        if (null == file) {
            // Quit with 404
            throw new FileResponseRenderer.FileResponseRendererActionException(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }

        data.setFile(file);
    }

    /**
     * @param fileHolder
     * @param forceCreation
     * @return
     * @throws OXException
     * @throws IOException
     */
    protected static IFileHolder getRepetitiveFile(@NonNull final IFileHolder fileHolder) throws OXException, IOException {
        IFileHolder ret = (fileHolder.repetitive() ? fileHolder : null);

        if (null == ret) {
            // closing of internal resources will be handled by returned FileHolder
            final ThresholdFileHolder tmpThresholdFileHolder = new ThresholdFileHolder(fileHolder);

            fileHolder.close();
            ret = tmpThresholdFileHolder;
        }

        return ret;
    }

    /**
     * @param session
     * @param request
     * @param eTag
     * @param params
     * @return
     * @throws OXException
     */
    protected String getCacheKey(@NonNull final ServerSession session, @NonNull final AJAXRequestData request, @NonNull final AJAXRequestResult result, @SuppressWarnings("unused") @NonNull final IFileHolder repetitiveFile, @SuppressWarnings("unused") @NonNull final TransformImageParameters xformParams) throws OXException {
        final ResourceCache cache = ResourceCaches.getResourceCache();
        final String eTag = result.getHeader("ETag");
        String cacheKey = null;

        if ((null != cache) && !isEmpty(eTag) && cache.isEnabledFor(session.getContextId(), session.getUserId()) && AJAXRequestDataTools.parseBoolParameter("cache", request, true)) {

            cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, request, AbstractPreviewResultConverter.getUserLanguage(session));
        }

        return cacheKey;
    }

    /**
     * @param cacheKey
     * @param session
     * @return
     * @throws OXException
     */
    @SuppressWarnings("static-method")
    protected IFileHolder getCachedResource(@NonNull final ServerSession session, @NonNull final String cacheKey, @SuppressWarnings("unused") @NonNull final TransformImageParameters xformParams) throws OXException {
        final ResourceCache cache = ResourceCaches.getResourceCache();
        IFileHolder ret = null;

        if (isNotEmpty(cacheKey) && (null != cache)) {
            final CachedResource cachedResource = cache.get(cacheKey, 0, session.getContextId());

            if (null != cachedResource) {
                // Scaled version already cached
                // Create appropriate IFileHolder
                String cachedMimeType = cachedResource.getFileType();

                if (null == cachedMimeType) {
                    cachedMimeType = "image/jpeg";
                }

                // if valid. closing of inputStm will be handled by FileHolder
                final InputStream inputStm = cachedResource.getInputStream();

                if (null == inputStm) {
                    // closing of internal resouces is handled by FileHolder, no closing needed for ByteArrayFileHolder
                    final ByteArrayFileHolder responseFileHolder = new ByteArrayFileHolder(cachedResource.getBytes());

                    responseFileHolder.setContentType(cachedMimeType);
                    responseFileHolder.setName(cachedResource.getFileName());

                    ret = responseFileHolder;
                } else {
                    ret = new FileHolder(inputStm, cachedResource.getSize(), cachedMimeType, cachedResource.getFileName());
                }
            }
        }

        return ret;
    }

    /**
     * @param session
     * @param cacheKey
     * @param targetMimeType
     * @param transformedImage
     * @param transformedFile
     * @param fileName
     * @param size
     * @throws OXException
     * @throws IOException
     */
    protected void writeCachedResource(final ServerSession session, final String cacheKey, final String targetMimeType, final BasicTransformedImage transformedImage, final ThresholdFileHolder transformedFile, final String fileName, final long size) throws OXException, IOException {

        final ResourceCache cache = ResourceCaches.getResourceCache();

        if ((null != cache) && isNotEmpty(cacheKey)) {

            File tempFile = (null == transformedFile) ? null : transformedFile.getTempFile();

            if (null != tempFile) {
                // Copy to avoid preliminary file deletion; self care about file deletion
                final File newTempFile = TmpFileFileHolder.newTempFile(false);
                FileUtils.copyFile(tempFile, newTempFile, false);
                tempFile = newTempFile;
            }

            final File imgFile = tempFile;
            final byte[] imageData = (null == imgFile) ? ((null == transformedFile) ? transformedImage.getImageData() : transformedFile.toByteArray()) : null;

            tempFile = null;

            ThreadPools.submitElseExecute(new AbstractTask<Void>() {

                @Override
                public Void call() {
                    try {
                        if (null != imgFile) {
                            try (final FileInputStream fileInputStm = new FileInputStream(imgFile)) {
                                cache.save(cacheKey, new CachedResource(fileInputStm, fileName, targetMimeType, size), 0, session.getContextId());
                            }
                        } else if (null != imageData) {
                            cache.save(cacheKey, new CachedResource(imageData, fileName, targetMimeType, size), 0, session.getContextId());
                        }
                    } catch (Exception e) {
                        LOG.warn("Could not cache preview.", e);
                    } finally {
                        FileUtils.deleteQuietly(imgFile);
                    }

                    return null;
                }
            });
        }
    }

    /**
     * @param request
     * @param file
     * @param delivery
     * @param tmpDirRef
     * @return
     * @throws ImageTransformationDeniedIOException
     * @throws IOException
     */
    protected BasicTransformedImage performTransformImage(@NonNull final ServerSession session, @NonNull final IFileHolder file, @NonNull final TransformImageParameters transformParams, @SuppressWarnings("unused") final String cacheKey, @SuppressWarnings("unused") final String fileName) throws OXException, IOException {

        // existence of return data has been already checked in caller method
        final ImageTransformationService scaler = m_scalerReference.get();
        ImageTransformations transformations;

        try {
            transformations = scaler.transfom(file, session.getSessionID());
        } catch (ImageTransformationDeniedIOException e) {
            // Rethrow as ImageTransformationDeniedIOException is specially handled in FileResponseRenderer
            throw e;
        }

        // Rotate by default when not delivering as download
        if (transformParams.isAutoRotate()) {
            transformations.rotate();
        }

        if (transformParams.isCropping()) {
            transformations.crop(transformParams.getCropX(), transformParams.getCropX(), transformParams.getCropWidth(), transformParams.getCropHeight());
        }

        if (transformParams.isScaling()) {
            int maxWidth = transformParams.getWidth();

            if (maxWidth > Constants.getMaxWidth()) {
                throw AjaxExceptionCodes.BAD_REQUEST.create("Width " + maxWidth + " exceeds max. supported width " + Constants.getMaxWidth());
            }

            int maxHeight = transformParams.getHeight();

            if (maxHeight > Constants.getMaxHeight()) {
                throw AjaxExceptionCodes.BAD_REQUEST.create("Height " + maxHeight + " exceeds max. supported height " + Constants.getMaxHeight());
            }

            try {
                transformations.scale(transformParams.getWidth(), transformParams.getHeight(), transformParams.getScaleType(), transformParams.isShrinkOnly());
            } catch (final IllegalArgumentException e) {
                throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create(e, e.getMessage());
            }
        }

        if (transformParams.isCompress()) {
            transformations.compress();
        }

        // return transformed image
        return transformations.getTransformedImage(transformParams.getImageType().getShortName());
    }

    /**
     * @param request
     * @param result
     * @param file
     * @param delivery
     * @param tmpDirRef
     * @return
     * @throws IOException
     * @throws OXException
     * @throws FileResponseRendererActionException
     */
    private IFileHolder transformIfImage(final AJAXRequestData request, final AJAXRequestResult result, final IFileHolder file, final String delivery, AtomicReference<File> tmpDirReference) throws OXException, FileResponseRendererActionException, IOException {

        final String sourceMimeType = getSourceMimeType(file);
        final TransformImageParameters xformParams = new TransformImageParameters(getTargetMimeType(sourceMimeType));
        final boolean isViewDelivery = !IDataWrapper.DOWNLOAD.equalsIgnoreCase(delivery);
        IFileHolder resultFile = getResultFileIfNoTransformationNeeded(request, file, sourceMimeType, isViewDelivery, xformParams);

        // if no further transformation is possible or needed,
        // we have a valid file here and can return;
        // otherwise, carry on with the usual transformation path
        if (null == resultFile) {

            // we need a valid server session in order to transform
            final ServerSession session = request.getSession();

            if (null == session) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("session");
            }

            // Image access is needed => ensure initial source file is repetitive
            final IFileHolder repetitiveFile = getRepetitiveFile(file);

            // we need a valid file at following locations
            if (null == repetitiveFile) {
                throw new FileResponseRenderer.FileResponseRendererActionException(HttpServletResponse.SC_NOT_FOUND, "File not found.");
            }

            // determine cache key; might be null, in which
            // case caching should be disabled for this file
            final String cacheKey = getCacheKey(session, request, result, repetitiveFile, xformParams);

            // try to retrieve cached resource, if cache key is given
            if (null != cacheKey) {
                resultFile = getCachedResource(session, cacheKey, xformParams);
            }

            // if we got a result from the cache, we have a valid file here and can return;
            if (null == resultFile) {
                final String sourceFormatName = lowerCase(Utility.getImageFormat(sourceMimeType));

                // Check for an animated .gif or SVG image, if resultFile is not already valid
                // and return this, since no valid transformation is possible here
                if ("svg".equals(sourceFormatName)) {
                    resultFile = repetitiveFile;
                } else {
                    Boolean animatedGifResult = isAnimatedGif(sourceFormatName, repetitiveFile);
                    if (null == animatedGifResult) {
                        resultFile = repetitiveFile;
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("(Possible) Image file misses stream data");
                        }
                    } else if (animatedGifResult.booleanValue()) {
                        resultFile = repetitiveFile;
                    }
                }

                // no result file so far => use cache and transformation path
                if (null == resultFile) {
                    final String fileName = repetitiveFile.getName();
                    try {
                        // Image transformation path, if we don't have a valid result by now
                        BasicTransformedImage transformedImage = null;

                        try {
                            if (null == (transformedImage = performTransformImage(session, repetitiveFile, xformParams, cacheKey, fileName))) {
                                resultFile = repetitiveFile;
                            }
                        } catch (final IOException e) {
                            final String message = e.getMessage();

                            if (null != message) {
                                if ("Unsupported Image Type".equals(message) || message.indexOf("No image reader available for format") >= 0) {
                                    resultFile = repetitiveFile;
                                    return resultFile;
                                }
                            }

                            throw e;
                        }

                        if (null != transformedImage) {
                            final IFileHolder transformedImageFile = transformedImage.getImageFile();
                            final ThresholdFileHolder optImageFileHolder = ((transformedImageFile instanceof ThresholdFileHolder) ? (ThresholdFileHolder) transformedImageFile : null);
                            final long size = transformedImage.getSize();

                            if (transformedImage.getTransformationExpenses() == ImageTransformations.HIGH_EXPENSE) {
                                writeCachedResource(session, cacheKey, xformParams.getImageMimeType(), transformedImage, optImageFileHolder, fileName, size);
                            }

                            if (null == optImageFileHolder) {
                                // Returning  new file holder having image-transformation content
                                resultFile = new FileHolder(new TransformedImageInputStreamClosure(transformedImage), size, xformParams.getImageMimeType(), fileName);
                            } else {
                                // Returning already available result file holder
                                optImageFileHolder.setName(fileName);
                                optImageFileHolder.setContentType(xformParams.getImageMimeType());
                                resultFile = optImageFileHolder;
                            }
                        }
                    } catch (final RuntimeException e) {
                        if (LOG.isDebugEnabled()) {
                            try {
                                LOG.error("Unable to transform image from {}. Unparseable image file is written to disk at: {}", repetitiveFile.getName(), writeBrokenImage2Disk(repetitiveFile, tmpDirReference).getPath(), e);
                            } catch (final Exception excp) {
                                LOG.error("Unable to transform image from {}", repetitiveFile.getName(), e);
                            }
                        } else {
                            LOG.error("Unable to transform image from {}", repetitiveFile.getName(), e);
                        }

                        Streams.close(resultFile);
                        resultFile = repetitiveFile;
                    } finally {
                        if (resultFile != repetitiveFile) {
                            Streams.close(repetitiveFile);
                        }
                    }
                }

                if (resultFile != repetitiveFile) {
                    Streams.close(repetitiveFile);
                }
            }
        }

        return resultFile;
    }

    /**
     * @param request
     * @param fileHolder
     * @param sourceMimeType
     * @param delivery
     * @return
     * @throws OXException
     * @throws IOException
     */
    private IFileHolder getResultFileIfNoTransformationNeeded(final AJAXRequestData request, final IFileHolder fileHolder, final String sourceMimeType, final boolean isViewDelivery, final TransformImageParameters params) throws OXException, IOException {
        IFileHolder ret = null;

        if ((null == sourceMimeType) || !sourceMimeType.startsWith("image/") || (null == m_scalerReference.get())) {
            ret = fileHolder;
        } else {
            // the optional parameter "transformationNeeded" is set by the PreviewImageResultConverter if no transformation is needed.
            // This is done if the preview was generated by the com.openexchage.documentpreview.OfficePreviewDocument service
            Boolean transformationNeeded = request.getParameter("transformationNeeded", Boolean.class, true);

            if (null != transformationNeeded && !transformationNeeded.booleanValue()) {
                ret = fileHolder;
            }
        }

        if (null == ret) {
            final Boolean rotate = request.isSet("rotate") ? request.getParameter("rotate", Boolean.class) : null;
            boolean transform = false;

            // rotate
            if (((null == rotate) && isViewDelivery) || ((null != rotate) && rotate.booleanValue())) {
                params.setAutoRotate(true);
                transform = true;
            }

            // crop
            if (request.isSet("cropWidth") || request.isSet("cropHeight")) {
                params.setCropX(optIntParameter(request, "cropX"));
                params.setCropY(optIntParameter(request, "cropY"));
                params.setCropWidth(optIntParameter(request, "cropWidth"));
                params.setCropHeight(optIntParameter(request, "cropHeight"));
                transform = true;
            }

            if (request.isSet("width") || request.isSet("height")) {
                params.setWidth(optIntParameter(request, "width"));
                params.setHeight(optIntParameter(request, "height"));
                params.setScaleType(ScaleType.getType(request.getParameter("scaleType")));
                params.setShrinkOnly(request.isSet("shrinkOnly") && Boolean.parseBoolean(request.getParameter("shrinkOnly")));
                transform = true;
            }

            if (!transform) {
                // Rotation/compression only required for JPEG
                final String sourceFormatName = lowerCase(Utility.getImageFormat(sourceMimeType));

                if (("jpeg".equals(sourceFormatName) || "jpg".equals(sourceFormatName)) && isViewDelivery) {
                    ret = getRepetitiveFile(fileHolder);

                    try (final InputStream inputStm = ret.getStream()) {
                        if (null == inputStm) {
                            LOG.warn("(Possible) Image file misses stream data");
                        } else if (ImageTransformationUtility.requiresRotateTransformation(inputStm)) {
                            params.setAutoRotate(true);
                            transform = true;
                        }
                    } finally {
                        if (transform) {
                            Streams.close(ret);
                            ret = null;
                        }
                    }
                } else {
                    ret = fileHolder;
                }
            }
        }

        return ret;
    }

    /**
     * Optionally parses a specific numerical parameter from the supplied request data.
     *
     * @param request The request to get the parameter from
     * @param name The parameter name
     * @return The parameter, or <code>0</code> if not set
     */
    private static int optIntParameter(AJAXRequestData request, String name) throws OXException {
        if (request.isSet(name)) {
            Integer integer = request.getParameter(name, int.class);
            return null != integer ? integer.intValue() : 0;
        }
        return 0;
    }

    /**
     * @param fileHolder
     * @return
     */
    private static String getSourceMimeType(final IFileHolder fileHolder) {
        String sourceMimeType = lowerCase(fileHolder.getContentType());

        if ((null == sourceMimeType) || !sourceMimeType.startsWith("image/")) {
            String sourceMimeTypeByFileName = FileResponseRenderer.getContentTypeByFileName(fileHolder.getName());

            if (null != sourceMimeTypeByFileName) {
                sourceMimeType = lowerCase(sourceMimeTypeByFileName);
            }
        }

        return sourceMimeType;
    }

    /**
     * Gets the target content type to use for a transformed image, based on the type of the source image type.
     *
     * @param sourceMimeType The source image's content type file
     * @return The target content type, falling back to <code>image/jpeg</code> for unknown or mostly unsupported content types
     */
    private static String getTargetMimeType(String sourceMimeType) {
        return (Strings.isNotEmpty(sourceMimeType) && ("image/bmp".equals(sourceMimeType) || "image/gif".equals(sourceMimeType) || "image/png".equals(sourceMimeType))) ? sourceMimeType : "image/jpeg";
    }

    private static Boolean isAnimatedGif(String sourceFormatName, IFileHolder repetitiveFile) throws IOException, OXException {
        if (false == "gif".equals(sourceFormatName)) {
            return Boolean.FALSE;
        }

        InputStream repetitiveInputStm = repetitiveFile.getStream();
        if (null == repetitiveInputStm) {
            return null;
        }

        try {
            return ImageUtils.isAnimatedGif(repetitiveInputStm) ? Boolean.TRUE : Boolean.FALSE;
        } finally {
            Streams.close(repetitiveInputStm);
        }
    }

    /**
     * @param inputFile
     * @param tmpDirRef
     * @return
     * @throws IOException
     * @throws OXException
     * @throws FileNotFoundException
     */
    private static File writeBrokenImage2Disk(final IFileHolder inputFile, AtomicReference<File> tmpDirReference) throws IOException, OXException, FileNotFoundException {
        String suffix = null;
        final String name = inputFile.getName();

        // determine name
        if (null != name) {
            final int pos = name.lastIndexOf('.');

            if (pos > 0 && pos < name.length() - 1) {
                suffix = name.substring(pos);
            }
        }

        // determine suffix
        if (null == suffix) {
            final String contentType = inputFile.getContentType();

            if (null != contentType) {
                suffix = "." + MimeType2ExtMap.getFileExtension(contentType);
            }
        }

        // copy file
        final File retFile = File.createTempFile("brokenimage-", (null == suffix) ? ".tmp" : suffix, tmpDirReference.get());

        try (final InputStream inputStm = inputFile.getStream()) {
            FileUtils.copyInputStreamToFile(inputStm, retFile);
        }

        return retFile;
    }

    // - Members ---------------------------------------------------------------

    /**
     * the scaler Reference
     */
    private final AtomicReference<ImageTransformationService> m_scalerReference = new AtomicReference<>();

    // - Static members --------------------------------------------------------

    /** The logger constant */
    protected final static Logger LOG = LoggerFactory.getLogger(FileResponseRenderer.class);
}
