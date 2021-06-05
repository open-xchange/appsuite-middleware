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

package com.openexchange.ajax.requesthandler.converters.preview.cache;

import static com.google.common.net.HttpHeaders.ETAG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
import com.openexchange.java.Strings;

/**
 * {@link PreviewThumbCacheKeyGenerator} - A cache key generator for preview thumbnails considering specified width and height parameters
 * used during creation.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PreviewThumbCacheKeyGenerator implements CacheKeyGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(PreviewThumbCacheKeyGenerator.class);

    private final AJAXRequestResult result;

    private final String width;

    private final String height;

    private final String scaleType;

    private final AJAXRequestData requestData;

    private String cachedCacheKey = null;

    public PreviewThumbCacheKeyGenerator(AJAXRequestResult result, AJAXRequestData requestData) {
        this.result = result;
        this.requestData = requestData;
        this.width = requestData.getParameter("width");
        this.height = requestData.getParameter("height");
        this.scaleType = requestData.getParameter("scaleType");
    }

    @Override
    public String generateCacheKey() {
        if (cachedCacheKey == null) {
            final String eTag = result.getHeader(ETAG);
            if (Strings.isNotEmpty(eTag)) {
                cachedCacheKey = ResourceCaches.generateDefaultThumbnailCacheKey(eTag, requestData);
                LOG.debug("Generated cacheKey {} based on etag {}, width {}, height {} and scaleType {}", cachedCacheKey, eTag, width, height, scaleType);
            }
        }
        return cachedCacheKey;
    }

}
