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

package com.openexchange.find.drive;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.facet.FacetType;
import com.openexchange.java.Strings;


/**
 * {@link DriveFacetType} - Facet types for the drive module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DriveFacetType implements FacetType {

    FILE_NAME(null),
    FILE_TYPE(DriveStrings.FACET_FILE_TYPE),
    FILE_DESCRIPTION(null),
    FILE_CONTENT(null),
    FILE_SIZE(DriveStrings.FACET_FILE_SIZE),
    FOLDER_NAME(DriveStrings.FACET_FOLDER_NAME, FILE_SIZE, FILE_TYPE, CommonFacetType.FOLDER_TYPE),
    ;

    private static final Map<String, DriveFacetType> typesById = new HashMap<String, DriveFacetType>();
    static {
        for (DriveFacetType type : values()) {
            typesById.put(type.getId(), type);
        }
    }

    private final String displayName;

    private final List<FacetType> conflictingFacets;

    private DriveFacetType(final String displayName) {
        this.displayName = displayName;
        this.conflictingFacets = new LinkedList<FacetType>();
    }

    private DriveFacetType(String displayName, FacetType... conflictingFacet) {
        this.displayName = displayName;
        this.conflictingFacets = Arrays.asList(conflictingFacet);
    }

    @Override
    public String getId() {
        return toString().toLowerCase();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public List<FacetType> getConflictingFacets() {
        return conflictingFacets;
    }

    /**
     * Gets a {@link DriveFacetType} by its id.
     * @return The type or <code>null</code>, if the id is invalid.
     */
    public static DriveFacetType getById(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }
        return typesById.get(id);
    }

}
