/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
