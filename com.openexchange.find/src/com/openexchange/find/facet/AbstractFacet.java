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

package com.openexchange.find.facet;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


/**
 * Abstract super class that proides some common logic for all {@link Facet}
 * styles.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public abstract class AbstractFacet implements Facet, Serializable {

    private static final long serialVersionUID = 3011527528866807119L;

    private FacetType type;

    private final List<String> flags = new LinkedList<String>();

    protected AbstractFacet(final FacetType type) {
        super();
        checkNotNull(type);
        setType(type);
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    AbstractFacet() {
        super();
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    void setType(final FacetType type) {
        this.type = type;
        for (FacetType conflicting : type.getConflictingFacets()) {
            flags.add("conflicts:" + conflicting.getId());
        }
    }

    @Override
    public FacetType getType() {
        return type;
    }

    @Override
    public List<String> getFlags() {
        return flags;
    }

    /**
     * Adds a flag to this facet.
     *
     * @param flag The flag.
     */
    public void addFlag(String flag) {
        flags.add(flag);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractFacet other = (AbstractFacet) obj;
        if (flags == null) {
            if (other.flags != null)
                return false;
        } else if (!flags.equals(other.flags))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
