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

package com.openexchange.ajax.requesthandler.converters.preview;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
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
        if (cacheKey != null) {
            boolean removeFromRunning = false;
            try {
                PreviewImageGenerator previewFuture = RUNNING.get(cacheKey);
                if (null == previewFuture) {
                    PreviewImageGenerator newFuture = new PreviewImageGenerator(new PreviewDocumentCallable(result, requestData, PreviewOutput.IMAGE, session, previewService, respectLanguage));
                    previewFuture = RUNNING.putIfAbsent(cacheKey, newFuture);
                    if (null == previewFuture) {
                        previewFuture = newFuture;
                        ThreadPools.getExecutorService().execute(previewFuture);
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
        ThreadPools.getExecutorService().execute(previewFuture);
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
