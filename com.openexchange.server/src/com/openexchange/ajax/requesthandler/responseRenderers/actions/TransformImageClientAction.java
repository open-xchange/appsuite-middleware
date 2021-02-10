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

import static com.openexchange.java.Strings.isNotEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import com.google.common.base.Throwables;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.imageconverter.api.IImageClient;
import com.openexchange.imageconverter.api.ImageConverterException;
import com.openexchange.imagetransformation.BasicTransformedImage;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.java.Streams;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link TransformImageClientAction}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public class TransformImageClientAction extends TransformImageAction {

    /**
     * Initializes a new {@link TransformImageClientAction}.
     */
    public TransformImageClientAction() {
        super();
    }

    /**
     * Sets the IImageClient service to use
     *
     * @param imageClient The image client service interface
     */
    public void setImageClient(final IImageClient imageClient) {
        m_imageClient.set(imageClient);
    }

    /**
     * Gets the IImageClient service to use (if any available and if connected).
     *
     * @return The IImageClient service (if any available and if connected) or <code>null</code>
     */
    public IImageClient getImageClientIfValid() {
        final IImageClient imageClient = this.m_imageClient.get();

        try {
            if ((null != imageClient) && imageClient.isConnected()) {
                return imageClient;
            }
        } catch (@SuppressWarnings("unused") OXException e) {
            // OK, an IC server is currently not available
        }

        // use fallback
        return null;
    }

    @Override
    protected String getCacheKey(@NonNull final ServerSession session, @NonNull final AJAXRequestData request, @NonNull final AJAXRequestResult result, @NonNull final IFileHolder repetitiveFile, @NonNull final TransformImageParameters xformParams) throws OXException {
        final long size = repetitiveFile.getLength();

        if (null != getImageClientIfValid()) {
            // calculate Adler32 of image
            final Checksum crcImage = new Adler32();

            try (final InputStream inputStm = repetitiveFile.getStream()) {
                if (null != inputStm) {
                    try (CheckedInputStream checkedInputStm = new CheckedInputStream(inputStm, crcImage); OutputStream nullSink = new NullOutputStream()) {
                        IOUtils.copy(checkedInputStm, nullSink);
                    }

                    // set IC cache key at given xFormParams to be used for following IC related requests
                    xformParams.setICCacheKey(new StringBuilder(32).append(size).append('-').append(crcImage.getValue()).toString());
                }
            } catch (IOException e) {
                LOG.error(Throwables.getRootCause(e).getMessage());
            }
        }

        // return the fallback cache key in every case as standard key
        return super.getCacheKey(session, request, result, repetitiveFile, xformParams);
    }

    @Override
    protected IFileHolder getCachedResource(@NonNull final ServerSession session, @NonNull final String cacheKey, @NonNull final TransformImageParameters xformParams) throws OXException {
        final String icCacheKey = xformParams.getICCacheKey();
        IImageClient imageClient = null;

        // try to get an IC cached image using the IC specific cache key first
        if (isNotEmpty(icCacheKey) && (null != (imageClient = getImageClientIfValid()))) {
            try {
                final String requestFormatString = getRequestFormatString(xformParams, "auto");
                final InputStream imageInputStm = imageClient.getImage(icCacheKey, requestFormatString, Integer.toString(session.getContext().getContextId()));

                if (null != imageInputStm) {
                    LOG.debug("Returning cached image (IC)");
                    return new FileHolder(imageInputStm, -1, xformParams.getImageMimeType(), icCacheKey);
                }
            } catch (ImageConverterException e) {
                // OK, we just didn't get a result => fallback behavior follows
                LOG.trace("TransformImageClientAction received an exception when trying to get a cached resource from the Image Server {}", Throwables.getRootCause(e).getMessage());
            }
        }

        // if we didn't return an IC server cached image so far, return the the fallback cache result using the standard cache key
        return super.getCachedResource(session, cacheKey, xformParams);
    }

    @Override
    protected BasicTransformedImage performTransformImage(@NonNull final ServerSession session, @NonNull final IFileHolder file, @NonNull final TransformImageParameters xformParams, final String cacheKey, final String fileName)
        throws OXException, IOException {

        // check for valid IImageClient interface and use this one for transformation of image =>
        // if not successful, rely on ImageTransformation implementation and call super class method
        final String icCacheKey = xformParams.getICCacheKey();
        IImageClient imageClient = null;

        if (isNotEmpty(icCacheKey) && (null != (imageClient = getImageClientIfValid()))) {
            InputStream srcImageStm = file.getStream();
            if (null != srcImageStm) {
                try {
                    String requestFormatString = getRequestFormatString(xformParams, "auto");
                    InputStream resultImageStm = null;
                    try {
                        resultImageStm = imageClient.cacheAndGetImage(icCacheKey, requestFormatString, srcImageStm, getContextIdString(session));
                        if (null != resultImageStm) {
                            ThresholdFileHolder imageData = new ThresholdFileHolder();
                            try {
                                imageData.write(resultImageStm);
                                imageData.setContentType(xformParams.getImageMimeType());
                                imageData.setName(icCacheKey);
                                BasicTransformedImage ret = new FileHolderBasicTransformedImage(imageData, xformParams);
                                imageData = null; // Avoid premature closing
                                LOG.debug("Returning transformed image (IC)");
                                return ret;
                            } finally {
                                Streams.close(imageData);
                            }
                        }
                    } catch (ImageConverterException e) {
                        // OK, we just didn't get a result => fallback behavior follows
                        LOG.trace("TransformImageClientAction received an exception when trying to transform an image via the Image Server {}", Throwables.getRootCause(e).getMessage());
                    } finally {
                        Streams.close(resultImageStm);
                    }
                } finally {
                    Streams.close(srcImageStm);
                }
            }
        }

        // not able to return the IC based transformation so far => reset icCacheKey
        // and use the fallback transformation with the standard fallback cache key
        xformParams.setICCacheKey(null);

        return super.performTransformImage(session, file, xformParams, cacheKey, fileName);
    }

    // - Implementation --------------------------------------------------------

    private static String getContextIdString(@NonNull final ServerSession session) {
        return Integer.toString(session.getContext().getContextId());
    }

    private static final Pattern PATTERN_FORMAT_STRING = Pattern.compile("^[a-zA-Z]*:");

    /**
     * @param xformParams
     * @param targetFormat
     * @return
     */
    private static String getRequestFormatString(@NonNull final TransformImageParameters xformParams, final String targetFormat) {
        String ret = xformParams.getFormatString();
        return (isNotEmpty(targetFormat)) ? PATTERN_FORMAT_STRING.matcher(ret).replaceFirst(targetFormat + ':') : ret;
    }

    // - Members ---------------------------------------------------------------

    private final AtomicReference<IImageClient> m_imageClient = new AtomicReference<>(null);

    // - Helper classes --------------------------------------------------------

    /**
     * A <tt>BasicTransformedImage</tt> implementation backed by a file holder.
     */
    private static class FileHolderBasicTransformedImage implements BasicTransformedImage {

        private final ThresholdFileHolder imageData;
        private final TransformImageParameters xformParams;

        /**
         * Initializes a new {@link BasicTransformedImageImplementation}.
         */
        FileHolderBasicTransformedImage(ThresholdFileHolder imageData, TransformImageParameters xformParams) {
            this.imageData = imageData;
            this.xformParams = xformParams;
        }

        @Override
        public long getSize() {
            return imageData.getLength();
        }

        @Override
        public String getFormatName() {
            return xformParams.getFormatShortName();
        }

        @Override
        public byte[] getImageData() throws OXException {
            return imageData.toByteArray();

        }

        @Override
        public InputStream getImageStream() throws OXException {
            return imageData.getStream();
        }

        @Override
        public IFileHolder getImageFile() {
            return imageData;
        }

        @Override
        public int getTransformationExpenses() {
            return ImageTransformations.HIGH_EXPENSE;
        }

        @Override
        public void close() {
            imageData.close();
        }
    }
}
