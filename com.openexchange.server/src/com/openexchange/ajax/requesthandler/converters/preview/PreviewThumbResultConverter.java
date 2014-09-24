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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.converters.preview;

import static com.google.common.net.HttpHeaders.RETRY_AFTER;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.converters.preview.cache.PreviewThumbCacheKeyGenerator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PreviewThumbResultConverter} - Tries to quickly deliver thumbnail_images from cache or fail fast so the response returns to the
 * client and doesn't occupy a precious client-server connection.
 * <ol>
 *   <li>Deliver the existing thumbnail from cache if possible and not forbidden by client</li>
 *   <li>If the image isn't in the cache already trigger the asynchronous {@link PreviewAndCacheTask} while quickly returning a 202 Accepted
 *       to the client so he can try again later</li>
 *   <li>If there is no cache available at all or the client fobids cache usage decide based on
 *       <code>com.openexchange.preview.thumbnail.blockingWorker</code> property</li>
 *   <ol>
 *     <li>If true: Block the current thread(client request) until the thumbnail was generated</li>
 *     <li>If false: Fail with PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE</li>
 *   </ol>
 * </ol>
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PreviewThumbResultConverter extends AbstractPreviewResultConverter {

    private static final Logger LOG = LoggerFactory.getLogger(PreviewThumbResultConverter.class);

    private final static String BLOCKING_WORKER_NAME = "com.openexchange.preview.thumbnail.blockingWorker";

    /** Maximum time we are willing to wait for preview generation */
    private static final long THRESHOLD = 10000;

    private static final String WIDTH = "width";

    private static final int DEFAULT_THUMB_WIDTH = 160;

    private static final String HEIGHT = "height";

    private static final int DEFAULT_THUMB_HEIGHT = 160;

    private final boolean isBlockingWorkerAllowed;

    /**
     * Initializes a new {@link PreviewThumbResultConverter}.
     *
     * @param configService The {@link ConfigurationService} to use for initialization, if null the defaults will be used.
     */
    public PreviewThumbResultConverter(ConfigurationService configService) {
        super();
        isBlockingWorkerAllowed = configService == null ? false : configService.getBoolProperty(BLOCKING_WORKER_NAME, false);
    }

    @Override
    public String getOutputFormat() {
        return "thumbnail_image";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public PreviewOutput getOutput() {
        return PreviewOutput.IMAGE;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        // validate request data before using it for cacheKeyGenerator and further processing
        validateRequest(requestData);

        // Use the generator for cache lookup and store operations
        PreviewThumbCacheKeyGenerator cacheKeyGenerator = new PreviewThumbCacheKeyGenerator(result, requestData);
        PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);

        try {
            // do we have a usable resource cache and are we allowed to use it?
            if (useCache(requestData)) {
                ResourceCache resourceCache = getResourceCache(contextId, userId);
                if (resourceCache != null) {
                    CachedResource cachedPreview = getCachedResourceForContext(session, cacheKeyGenerator.generateCacheKey(), resourceCache);
                    if (cachedPreview != null) {
                        // apply to request/response
                        applyCachedPreview(requestData, result, cachedPreview);
                        preventTransformations(requestData);
                        LOG.debug(
                            "Applied cached preview for file {} with MIME type {} from cache for user {} in context {}",
                            cachedPreview.getFileName(),
                            cachedPreview.getFileType(),
                            userId,
                            contextId);
                    } else {
                        // generate async and put to cache
                        PreviewAndCacheTask previewAndCache = new PreviewAndCacheTask(result, requestData, session, previewService, THRESHOLD, false, cacheKeyGenerator);
                        ThreadPools.getExecutorService().submit(previewAndCache);
                        indicateRequestAccepted(result);
                    }
                    return;
                }
            }

            if (isBlockingWorkerAllowed) {
                // there is no cached resource but we are allowed to wait for thumbnail generation
                // do as callable anyway
                PreviewDocument previewDocument = PreviewImageGenerator.getPreviewDocument(
                    result,
                    requestData,
                    session,
                    previewService,
                    THRESHOLD,
                    false);
                if (previewDocument == null || previewDocument.getThumbnail() == null) {
                    throw PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE.create();
                }

                InputStream thumbnail = previewDocument.getThumbnail();
                final String fileName = previewDocument.getMetaData().get("resourcename");
                byte[] thumbnailBytes;
                try {
                    thumbnailBytes = Streams.stream2bytes(thumbnail);
                } catch (IOException ioex) {
                    throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(ioex, ioex.getMessage());
                }

                // apply to request/response
                final FileHolder responseFileHolder = new FileHolder(thumbnail, thumbnailBytes.length, "image/jpeg", fileName);
                result.setResultObject(responseFileHolder, "file");
                preventTransformations(requestData, previewDocument);
            } else {
                throw PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE.create();
            }
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Applies the cached preview image to the {@link AJAXRequestData} and {@link AJAXRequestResult}.
     * This sets the format of the requestData to <cod>file</code>
     *
     * @param cachedPreview
     */
    protected void applyCachedPreview(AJAXRequestData requestData, AJAXRequestResult result, CachedResource cachedPreview) {
        if (null != cachedPreview) {
            requestData.setFormat("file");

            // Determine MIME type
            String contentType = cachedPreview.getFileType();
            if (null == contentType) {
                contentType = "image/jpeg";
            }

            // Create appropriate IFileHolder
            IFileHolder responseFileHolder;
            {
                InputStream inputStream = cachedPreview.getInputStream();
                if (null == inputStream) {
                    @SuppressWarnings("resource")// see implementation of close
                    final ByteArrayFileHolder bafh = new ByteArrayFileHolder(cachedPreview.getBytes());
                    bafh.setContentType(contentType);
                    bafh.setName(cachedPreview.getFileName());
                    responseFileHolder = bafh;
                } else {
                    responseFileHolder = new FileHolder(inputStream, cachedPreview.getSize(), contentType, cachedPreview.getFileName());
                }
            }

            // Apply result
            result.setResultObject(responseFileHolder, "file");
        }
    }

    /**
     * Validate the incoming {@link AJAXRequestData} before starting the preview process.
     * <ul>
     * <li>Set default thumbnail dimensions if width and height are missing from the request</li>
     * <li>...</li>
     * </ul>
     *
     * @param requestData The incoming {@link AJAXRequestData}
     */
    protected void validateRequest(AJAXRequestData requestData) {
        if (Strings.isEmpty(requestData.getParameter(WIDTH))) {
            requestData.putParameter(WIDTH, String.valueOf(DEFAULT_THUMB_WIDTH));
        }
        if (Strings.isEmpty(requestData.getParameter(HEIGHT))) {
            requestData.putParameter(HEIGHT, String.valueOf(DEFAULT_THUMB_HEIGHT));
        }
    }

    /**
     * Indicate that the request has been accepted for processing, but the processing has not been completed
     * @param result The current {@link AJAXRequestResult}
     */
    protected void indicateRequestAccepted(AJAXRequestResult result) {
        //can't remove Content-Type, see DispatcherServlet handleError
        result.setHttpStatusCode(202);
        result.setHeader(RETRY_AFTER, String.valueOf(THRESHOLD / 1000));
    }
}
