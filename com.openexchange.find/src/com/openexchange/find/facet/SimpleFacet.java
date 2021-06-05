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

/**
 * A {@link SimpleFacet} is a special facet that has exactly one value. The facets
 * type and its value are strictly coupled, in a way that a display name for both,
 * facet and value would be redundant. A simple facet generally denotes a logical field like
 * 'phone number'. Internally this logical field can map to several internal fields
 * (e.g. 'phone_private', 'phone_mobile', 'phone_business'). In clients the facet as
 * a whole can be displayed as a single item. Example: "Search for 'term' in field 'phone
 * number'".
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SimpleFacet extends AbstractFacet{

    private static final long serialVersionUID = 6623209495601676515L;

    private DisplayItem displayItem;

    private Filter filter;

    public SimpleFacet(FacetType type, DisplayItem displayItem, Filter filter) {
        super(type);
        this.displayItem = displayItem;
        this.filter = filter;
    }

    public SimpleFacet(FacetType type, DisplayItem displayItem, String filterField, String filterValue) {
        super(type);
        this.displayItem = displayItem;
        filter = Filter.of(filterField, filterValue);
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    SimpleFacet() {
        super();
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    void setDisplayItem(DisplayItem displayItem) {
        this.displayItem = displayItem;
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public String getStyle() {
        return "simple";
    }

    public DisplayItem getDisplayItem() {
        return displayItem;
    }

    public Filter getFilter() {
        return filter;
    }

    @Override
    public void accept(FacetVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((displayItem == null) ? 0 : displayItem.hashCode());
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
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
        SimpleFacet other = (SimpleFacet) obj;
        if (displayItem == null) {
            if (other.displayItem != null)
                return false;
        } else if (!displayItem.equals(other.displayItem))
            return false;
        if (filter == null) {
            if (other.filter != null)
                return false;
        } else if (!filter.equals(other.filter))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SimpleFacet [displayItem=" + displayItem + ", filter=" + filter + ", style=" + getStyle() + ", type=" + getType() + "]";
    }

}
