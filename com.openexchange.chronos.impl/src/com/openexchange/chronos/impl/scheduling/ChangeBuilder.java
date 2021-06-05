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

package com.openexchange.chronos.impl.scheduling;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.scheduling.changes.Change;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link ChangeBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ChangeBuilder {

    protected RecurrenceId recurrenceId;
    protected List<Description> descriptions = new LinkedList<>();

    /**
     * Initializes a new {@link ChangeBuilder}.
     */
    public ChangeBuilder() {
        super();
    }

    /**
     * Sets the recurrenceId
     *
     * @param recurrenceId The recurrenceId to set
     * @return This {@link ChangeBuilder} instance
     */
    public ChangeBuilder setRecurrenceId(RecurrenceId recurrenceId) {
        this.recurrenceId = recurrenceId;
        return this;
    }

    /**
     * Sets the descriptions
     *
     * @param descriptions The descriptions to set
     * @return This {@link ChangeBuilder} instance
     */
    public ChangeBuilder setDescriptions(List<Description> descriptions) {
        this.descriptions.addAll(descriptions);
        return this;
    }

    /**
     * Sets the descriptions
     *
     * @param description The description to add
     * @return This {@link ChangeBuilder} instance
     */
    public ChangeBuilder addDescriptions(Description description) {
        this.descriptions.add(description);
        return this;
    }

    public Change build() {
        return new ChangeImpl(this);
    }

}

class ChangeImpl implements Change {

    private final RecurrenceId recurrenceId;
    private final List<Description> descriptions;

    /**
     * Initializes a new {@link ChangeImpl}.
     * 
     * @param builder The builder
     */
    public ChangeImpl(ChangeBuilder builder) {
        super();
        this.recurrenceId = builder.recurrenceId;
        this.descriptions = builder.descriptions;
    }

    @Override
    @Nullable
    public RecurrenceId getRecurrenceId() {
        return recurrenceId;
    }

    @Override
    public List<Description> getDescriptions() {
        return descriptions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((descriptions == null) ? 0 : descriptions.hashCode());
        result = prime * result + ((recurrenceId == null) ? 0 : recurrenceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChangeImpl)) {
            return false;
        }
        ChangeImpl other = (ChangeImpl) obj;
        if (descriptions == null) {
            if (other.descriptions != null) {
                return false;
            }
        } else if (!descriptions.equals(other.descriptions)) {
            return false;
        }
        if (recurrenceId == null) {
            if (other.recurrenceId != null) {
                return false;
            }
        } else if (recurrenceId.compareTo(other.recurrenceId) != 0) {
            return false;
        }
        return true;
    }

}
