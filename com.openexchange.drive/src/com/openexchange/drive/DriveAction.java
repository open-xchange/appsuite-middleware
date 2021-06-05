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

package com.openexchange.drive;

import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link DriveAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveAction<T extends DriveVersion> extends Comparable<DriveAction<T>> {

    static final String PARAMETER_PATH = "path";
    static final String PARAMETER_MODIFIED = "modified";
    static final String PARAMETER_CREATED = "created";
    static final String PARAMETER_TOTAL_LENGTH = "totalLength";
    static final String PARAMETER_OFFSET = "offset";
    static final String PARAMETER_CONTENT_TYPE = "contentType";
    static final String PARAMETER_ERROR = "error";
    static final String PARAMETER_QUARANTINE = "quarantine";
    static final String PARAMETER_RESET = "reset";
    static final String PARAMETER_LENGTH = "length";
    static final String PARAMETER_STOP = "stop";
    static final String PARAMETER_ACKNOWLEDGE = "acknowledge";
    static final String PARAMETER_ROOT = "root";
    static final String PARAMETER_NO_CHUNKS = "noChunks";

    static final String PARAMETER_DIRECT_LINK = "directLink";
    static final String PARAMETER_DIRECT_LINK_FRAGMENTS = "directLinkFragments";
    static final String PARAMETER_PREVIEW_LINK = "previewLink";
    static final String PARAMETER_THUMBNAIL_LINK = "thumbnailLink";

    static final String PARAMETER_DATA = "data";

    static final Set<String> PARAMETER_NAMES = ImmutableSet.of(
        PARAMETER_PATH, PARAMETER_TOTAL_LENGTH, PARAMETER_OFFSET, PARAMETER_CONTENT_TYPE, PARAMETER_ERROR, PARAMETER_QUARANTINE,
        PARAMETER_MODIFIED, PARAMETER_CREATED, PARAMETER_RESET, PARAMETER_LENGTH, PARAMETER_STOP, PARAMETER_ACKNOWLEDGE,
        PARAMETER_DIRECT_LINK, PARAMETER_DIRECT_LINK_FRAGMENTS, PARAMETER_PREVIEW_LINK, PARAMETER_THUMBNAIL_LINK, PARAMETER_DATA,
        PARAMETER_ROOT, PARAMETER_NO_CHUNKS
    );

    /**
     * Gets the action.
     *
     * @return The action
     */
    Action getAction();

    /**
     * Gets the version.
     *
     * @return The version, or <code>null</code> if not applicable
     */
    T getVersion();

    /**
     * Gets the new version.
     *
     * @return The new version, or <code>null</code> if not applicable
     */
    T getNewVersion();

    /**
     * Gets a map of additional parameters; possible parameters are defined in {@link DriveAction#PARAMETER_NAMES}.
     *
     * @return The parameters map
     */
    Map<String, Object> getParameters();

}

