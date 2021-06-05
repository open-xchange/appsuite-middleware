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

package com.openexchange.rss.preprocessors;

import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.rss.RssResult;
import com.openexchange.rss.osgi.Services;

/**
 * {@link SanitizingPreprocessor} - Sanitizes HTML content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SanitizingPreprocessor extends AbstractPreprocessor {

    private final boolean dropExternalImages;

    /**
     * Initializes a new {@link SanitizingPreprocessor}.
     */
    public SanitizingPreprocessor(boolean dropExternalImages) {
        super();
        this.dropExternalImages = dropExternalImages;
    }

    @Override
    protected String innerProcess(String payload, RssResult rssResult) throws OXException {
        final HtmlService htmlService = Services.getService(HtmlService.class);
        if (null == htmlService) {
            return payload;
        }

        boolean[] modified = new boolean[1];
        modified[0] = false;
        String sanitized = htmlService.sanitize(payload, null, dropExternalImages, modified, null);

        if (modified[0]) {
            rssResult.markExternalImagesDropped();
        }

        return sanitized;
    }

}
