/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.requesthandler.converters.preview;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PreviewImageGenerator} - A cancellable asynchronous generator for preview images.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class PreviewImageGenerator extends FutureTask<PreviewDocument> {

    private static final Logger LOG = LoggerFactory.getLogger(PreviewImageGenerator.class);

    /** Does the actual work */
    private final PreviewDocumentCallable callable;

    /**
     * Initializes a new {@link PreviewImageGenerator}.
     *
     * @param callable The Callable that does the actual preview generation
     */
    public PreviewImageGenerator(PreviewDocumentCallable callable) {
        super(callable);
        this.callable = callable;
    }

    /**
     * Cleanup resources used by PreviewDocumentCallable and cancel preview image generation.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            callable.interrupt();
        } catch (IOException ioe) {
            LOG.error("Error while trying to cancel.", ioe);
        }
        return super.cancel(mayInterruptIfRunning);
    }

    /**
     * Detect the recommended await threshold for the {@link PreviewService} or use the given default threshold.
     *
     * @param defaultThreshold The default threshold in milliseconds to use if the {@link PreviewService} doesn't specify any.
     * @return The recommended await threshold for the {@link PreviewService} or the given default threshold in milliseconds.
     */
    public long getAwaitThreshold(long defaultThreshold) {
        return callable == null ? defaultThreshold : callable.getAwaitThreshold(defaultThreshold);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------- //

    /** Used to prevent multiple requests from generating and caching the same preview */
    private static final ConcurrentMap<String, PreviewImageGenerator> RUNNING = new ConcurrentHashMap<String, PreviewImageGenerator>(32, 0.9f, 1);

    /**
     * Try to generate a {@link PreviewDocument} for the given request.
     *
     * @param result The {@link AJAXRequestResult} for the current request
     * @param requestData The {@link AJAXRequestData} from the current request
     * @param session The current {@link ServerSession}
     * @param previewService The previewService to use for document generation
     * @param threshold The await threshold (in Milliseconds) to use if the {@link PreviewService} doesn't specify one
     * @param respectLanguage Should the preferredLanguage be respected for preview creation?
     * @return null or the generated PreviewDocument
     * @throws OXException if the preview generation was interrupted, timed out or simply failed
     */
    public static PreviewDocument getPreviewDocument(AJAXRequestResult result, AJAXRequestData requestData, ServerSession session, PreviewService previewService, long threshold, boolean respectLanguage) throws OXException {
        return getPreviewDocument(result, requestData, session, previewService, threshold, respectLanguage, null);
    }

    /**
     * Try to generate a {@link PreviewDocument} for the given request.
     *
     * @param result The {@link AJAXRequestResult} for the current request
     * @param requestData The {@link AJAXRequestData} from the current request
     * @param session The current {@link ServerSession}
     * @param previewService The previewService to use for document generation
     * @param threshold The await threshold (in Milliseconds) to use if the {@link PreviewService} doesn't specify one
     * @param respectLanguage Should the preferredLanguage be respected for preview creation?
     * @param cacheKey Used to prevent multiple requests from generating and caching the same preview, can be <code>null</code> in case you don't want to
     *            prevent previews being generated in parallel (e.g. there is no cache configured and previews have to be created in a
     *            blocking fashion)
     * @return <code>null</code> or the generated {@link PreviewDocument} instance
     * @throws OXException If the preview generation was interrupted, timed out or simply failed
     */
    public static PreviewDocument getPreviewDocument(AJAXRequestResult result, AJAXRequestData requestData, ServerSession session, PreviewService previewService, long threshold, boolean respectLanguage, String cacheKey) throws OXException {
        // Prevent multiple caching requests/allow multiple blocking
        ExecutorService executorService = ThreadPools.getExecutorService();
        if (cacheKey != null) {
            boolean removeFromRunning = false;
            try {
                PreviewImageGenerator previewFuture = RUNNING.get(cacheKey);
                if (null == previewFuture) {
                    PreviewImageGenerator newFuture = new PreviewImageGenerator(new PreviewDocumentCallable(result, requestData, PreviewOutput.IMAGE, session, previewService, respectLanguage));
                    previewFuture = RUNNING.putIfAbsent(cacheKey, newFuture);
                    if (null == previewFuture) {
                        previewFuture = newFuture;
                        executorService.execute(previewFuture);
                        removeFromRunning = true;
                    }
                }
                return removeFromRunning ? getFrom(previewFuture, threshold) : null;
            } finally {
                if (removeFromRunning) {
                    RUNNING.remove(cacheKey);
                }
            }
        }

        // Just do it...
        PreviewImageGenerator previewFuture = new PreviewImageGenerator(new PreviewDocumentCallable(result, requestData, PreviewOutput.IMAGE, session, previewService, respectLanguage));
        if (null == executorService) {
            return null;
        }
        executorService.execute(previewFuture);
        return getFrom(previewFuture, threshold);
    }

    private static PreviewDocument getFrom(PreviewImageGenerator previewFuture, long threshold) throws OXException {
        boolean error = true;
        try {
            PreviewDocument previewDocument = previewFuture.get(previewFuture.getAwaitThreshold(threshold), TimeUnit.MILLISECONDS);
            error = false;
            return previewDocument;
        } catch (InterruptedException ie) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            throw PreviewExceptionCodes.ERROR.create(ie, ie.getMessage());
        } catch (ExecutionException ee) {
            // Failed to generate preview image
            throw ThreadPools.launderThrowable(ee, OXException.class);
        } catch (TimeoutException te) {
            // Preview image has not been generated in time
            // throw PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE.create("Preview image has not been generated in time");
            return null;
        } finally {
            if (error) {
                previewFuture.cancel(true);
            }
        }
    }

}
