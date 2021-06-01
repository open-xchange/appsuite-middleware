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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.LinkedList;
import java.util.List;


/**
 * A {@link DefaultFacet} contains multiple {@link FacetValue}s and may be present
 * multiple times in search requests to filter results by a combination of different
 * values (e.g. "mails with 'foo' and 'bar' in subject").
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class DefaultFacet extends AbstractFacet {

    private static final long serialVersionUID = 7955913533889391522L;

    private List<FacetValue> values;

    public DefaultFacet(final FacetType type) {
        super(type);
        this.values = new LinkedList<FacetValue>();
    }

    public DefaultFacet(final FacetType type, final List<FacetValue> values) {
        super(type);
        checkNotNull(values);
        checkArgument(!values.isEmpty());
        setValues(new LinkedList<FacetValue>(values));
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    DefaultFacet() {
        super();
        this.values = new LinkedList<FacetValue>();
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    void setValues(List<FacetValue> values) {
        this.values = values;
    }

    @Override
    public String getStyle() {
        return "default";
    }

    /**
     * Returns a list of possible values.
     * E.g. "John Doe", "Jane Doe".
     */
    public List<FacetValue> getValues() {
        return values;
    }

    public void addValue(FacetValue value) {
        values.add(value);
    }

    @Override
    public void accept(FacetVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultFacet other = (DefaultFacet) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Facet [type=" + getType() + ", style=" + getStyle() + ", values=" + values + "]";
    }
}
