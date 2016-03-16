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

import static com.openexchange.ajax.requesthandler.converters.preview.AbstractPreviewResultConverter.getResourceCache;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.converters.preview.cache.CacheKeyGenerator;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PreviewAndCacheTask} - Generate a {@link PreviewDocument} and put the resulting thumbnail to cache using the given
 * {@link CacheKeyGenerator}.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PreviewAndCacheTask extends AbstractTask<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(PreviewAndCacheTask.class);

    private final PreviewService previewService;
    private final long threshold;
    private final AJAXRequestResult result;
    private final AJAXRequestData requestData;
    private final ServerSession session;
    private final boolean respectLanguage;
    private final CacheKeyGenerator cacheKeyGenerator;

    /**
     * Initializes a new {@link PreviewAndCacheTask}.
     *
     * @param result The current {@link AJAXRequestResult}
     * @param requestData The current {@link AJAXRequestData}
     * @param session The current {@link ServerSession}
     * @param previewService The {@link PreviewService to use}
     * @param threshold Maximum time we are willing to wait for preview generation
     * @param respectLanguage Use true if the language can influence the preview (e.g. date formats in documents)
     * @param cacheKeyGenerator The {@link CacheKeyGenerator} to use when adding the generated preview to cache
     * @throws IllegalArgumentException if any parameter is null
     */
    public PreviewAndCacheTask(AJAXRequestResult result, AJAXRequestData requestData, ServerSession session, PreviewService previewService, long threshold, boolean respectLanguage, CacheKeyGenerator cacheKeyGenerator) {
        super();
        Validate.notNull(result);
        Validate.notNull(requestData);
        Validate.notNull(session);
        Validate.notNull(previewService);
        Validate.notNull(cacheKeyGenerator);
        this.result = result;
        this.requestData = requestData;
        this.session = session;
        this.previewService = previewService;
        this.threshold = threshold;
        this.respectLanguage = respectLanguage;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Override
    public Void call() throws Exception {
        int contextId = session.getContextId();
        int userId = session.getUserId();

        ResourceCache resourceCache = getResourceCache(contextId, userId);
        if (resourceCache == null) {
            LOG.error("Unable to get ResourceCache for user {} in context {}");
            return null;
        }

        String cacheKey = cacheKeyGenerator.generateCacheKey();
        if (cacheKey == null) {
            LOG.error("Refusing to generate and cache preview without valid cache key");
            return null;
        }

        try {
            PreviewDocument previewDocument;
            try {
                previewDocument = PreviewImageGenerator.getPreviewDocument(result, requestData, session, previewService, threshold, respectLanguage, cacheKey);
            } catch (OXException e) {
                if (PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE.equals(e)) {
                    // Thumbnail has not been generated in time
                    LOG.debug("Thumbnail has not been generated in time.", e);
                    return null;
                }
                if (PreviewExceptionCodes.NO_PREVIEW_SERVICE.equals(e)) {
                    // Unable to handle file
                    LOG.debug("", e);
                    return null;
                }
                throw e;
            }

            if (previewDocument != null && previewDocument != PreviewConst.DEFAULT_PREVIEW_DOCUMENT) {
                InputStream thumbnail = previewDocument.getThumbnail();
                if (thumbnail != null) {
                    try {
                        byte[] thumbnailBytes = Streams.stream2bytes(thumbnail);
                        String fileName = previewDocument.getMetaData().get("resourcename");
                        syncToCache(thumbnailBytes, cacheKey, fileName, resourceCache, session);
                    } catch (IOException ioex) {
                        throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(ioex, ioex.getMessage());
                    }
                }
            }
        } catch (java.util.concurrent.CancellationException e) {
            // Cancelled...
            LOG.debug("Thumbnail generation has been cancelled.", e);
        } catch (Exception e) {
            LOG.error("Error while trying to get PreviewDocument.", e);
        }
        return null;
    }

    /**
     * Puts a resource into the {@link ResourceCache}.
     * @param input The resource we are caching
     * @param fileName The name of the resource we are caching
     * @param resourceCache The {@link ResourceCache to use}
     * @param session The current {@link ServerSession}
     *
     * @throws OXException if caching fails
     */
    private void syncToCache(byte[] input, String cacheKey, String fileName, ResourceCache resourceCache, ServerSession session) throws OXException {
        if (null != resourceCache && null != cacheKey) {
            try {
                final CachedResource preview = new CachedResource(input, fileName, "image/jpeg", input.length);
                resourceCache.save(cacheKey, preview, 0, session.getContextId());
                LOG.debug("Cached resource {} with key {}", fileName, cacheKey);
            } catch (OXException e) {
                throw CacheExceptionCode.FAILED_PUT.create(e);
            }
        }
    }

}
