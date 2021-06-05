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

package com.openexchange.preview.cache;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.preview.PreviewDocument;

/**
 * {@link CachedPreviewDocument} - A {@link PreviewDocument} generated from a cache entry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CachedPreviewDocument implements PreviewDocument {

    private final Map<String, String> map;
    private final List<String> content;

    /**
     * Initializes a new {@link CachedPreviewDocument}.
     *
     * @param content The content
     * @param map The meta data map
     */
    public CachedPreviewDocument(final List<String> content, final Map<String, String> map) {
        super();
        this.content = content;
        this.map = Collections.unmodifiableMap(map);
    }

    @Override
    public boolean hasContent() {
        return null != content;
    }

    @Override
    public Map<String, String> getMetaData() {
        return map;
    }

    @Override
    public List<String> getContent() {
        return content;
    }

    @Override
    public InputStream getThumbnail() {
        return null;
    }

    @Override
    public Boolean isMoreAvailable() {
        return null;
    }

}
