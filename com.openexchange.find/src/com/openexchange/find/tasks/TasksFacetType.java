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

package com.openexchange.find.tasks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.find.facet.FacetType;
import com.openexchange.java.Strings;

/**
 * {@link TasksFacetType} - Facet types for the drive module.
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public enum TasksFacetType implements FacetType {

    TASK_PARTICIPANTS(TasksStrings.FACET_TASK_PARTICIPANTS),
    TASK_TITLE(TasksStrings.FACET_TASK_TITLE),
    TASK_DESCRIPTION(TasksStrings.FACET_TASK_DESCRIPTION),
    TASK_ATTACHMENT_NAME(TasksStrings.FACET_TASK_ATTACHMENT_NAME),
    TASK_TYPE(TasksStrings.FACET_TASK_TYPE),
    TASK_STATUS(TasksStrings.FACET_TASK_STATUS), ;

    private static final Map<String, TasksFacetType> typesById = new HashMap<String, TasksFacetType>();
    static {
        for (TasksFacetType type : values()) {
            typesById.put(type.getId(), type);
        }
    }

    private final String displayName;

    private final List<FacetType> conflictingFacets = new LinkedList<FacetType>();

    private TasksFacetType(final String displayName) {
        this.displayName = displayName;
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
     * Gets a {@link TasksFacetType} by its id.
     * @return The type or <code>null</code>, if the id is invalid.
     */
    public static TasksFacetType getById(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }

        return typesById.get(id);
    }

}
