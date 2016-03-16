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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.find.SearchRequest;

/**
 *
 * A {@link FacetValue} is a possible value for a given {@link Facet}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class FacetValue implements Serializable {

    private static final long serialVersionUID = -7719065379433828901L;

    public static final int UNKNOWN_COUNT = -1;

    private final String id;

    private final DisplayItem displayItem;

    private final int count;

    private final Filter filter;

    private final List<Option> options;


    /**
     * Initializes a new {@link FacetValue}.
     *
     * @param id
     *   The values id which identifies it uniquely within all values of a facet.
     * @param displayItem
     *   The display item. May be {@link FacetValue#NO_DISPLAY_ITEM} if
     *   and only if this value is the only one for its facet.
     * @param count
     *   The number of result documents that apply to the given filter.
     *   {@link FacetValue#UNKNOWN_COUNT} if unknown.
     * @param filter
     *   The filter.
     */
    public FacetValue(String id, DisplayItem displayItem, int count, Filter filter) {
        super();
        checkNotNull(id);
        checkNotNull(displayItem);
        checkNotNull(filter);
        this.id = id;
        this.displayItem = displayItem;
        this.count = count;
        this.filter = filter;
        this.options = null;
    }

    /**
     * Initializes a new {@link FacetValue}.
     *
     * @param id
     *   The values id which identifies it uniquely within all values of a facet.
     * @param displayItem
     *   The display item. May be {@link FacetValue#NO_DISPLAY_ITEM} if
     *   and only if this value is the only one for its facet.
     * @param count
     *   The number of result documents that apply to the given filter.
     *   {@link FacetValue#UNKNOWN_COUNT} if unknown.
     * @param options
     *   The options.
     */
    public FacetValue(String id, DisplayItem displayItem, int count, List<Option> options) {
        super();
        checkNotNull(id);
        checkNotNull(displayItem);
        checkNotNull(options);
        checkArgument(options.size() > 0);
        this.id = id;
        this.displayItem = displayItem;
        this.count = count;
        this.options = options;
        this.filter = null;
    }

    private FacetValue(FacetValueBuilder builder) {
        super();
        this.id = builder.id;
        this.displayItem = builder.displayItem;
        this.count = builder.count;
        this.filter = builder.filter;
        this.options = builder.options;
    }

    public static FacetValueBuilder newBuilder(String valueId) {
        return new FacetValueBuilder(valueId);
    }

    /**
     * Gets the values id which identifies it uniquely within all values of a facet.
     * @return The id, never <code>null</code>.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The display item. Never <code>null</code>.
     */
    public DisplayItem getDisplayItem() {
        return displayItem;
    }

    /**
     * @return The number of results to which this value applies.
     * May be {@link FacetValue#UNKNOWN_COUNT} if unknown. if unknown.
     */
    public int getCount() {
        return count;
    }

    /**
     * Whether this value has options or a single filter.
     *
     * @return <code>true</code> if {@link FacetValue#getOptions()} will return a
     * non-empty list with options. Otherwise <code>false</code>, what guarantees
     * that {@link FacetValue#getFilter()} returns a filter object.
     */
    public boolean hasOptions() {
        if (options == null) {
            return false;
        }

        return true;
    }

    /**
     * The list of options of which one can be chosen to filter on.
     *
     * @return A list of options if {@link FacetValue#hasOptions()} returns <code>true</code>,
     * otherwise <code>null</code>.
     */
    public List<Option> getOptions() {
        return options;
    }

    /**
     * Gets the filter that has to be applied to {@link SearchRequest}s to filter on this value.
     *
     * @return A filter object if {@link FacetValue#hasOptions()} returns <code>false</code>,
     * otherwise <code>null</code>.
     */
    public Filter getFilter() {
        return filter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + count;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        result = prime * result + ((displayItem == null) ? 0 : displayItem.hashCode());
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
        FacetValue other = (FacetValue) obj;
        if (!id.equals(other.id))
            return false;
        if (count != other.count)
            return false;
        if (filter == null) {
            if (other.filter != null)
                return false;
        } else if (!filter.equals(other.filter))
            return false;
        if (options == null) {
            if (other.options != null)
                return false;
        } else if (!options.equals(other.options))
            return false;
        if (displayItem == null) {
            if (other.displayItem != null)
                return false;
        } else if (!displayItem.equals(other.displayItem))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FacetValue [id=" + id + ", name=" + displayItem + ", count=" + count + ", filter=" + filter + ", options=" + options + "]";
    }

    public static final class FacetValueBuilder {

        private static final String MISSING_FIELD = "A %s must be set!";

        private final String id;

        private DisplayItem displayItem;

        private int count = UNKNOWN_COUNT;

        private Filter filter;

        private List<Option> options;

        public FacetValueBuilder(String id) {
            super();
            this.id = id;
        }

        public FacetValueBuilder withDisplayItem(DisplayItem displayItem) {
            this.displayItem = displayItem;
            return this;
        }

        public FacetValueBuilder withSimpleDisplayItem(String displayName) {
            this.displayItem = new SimpleDisplayItem(displayName);
            return this;
        }

        public FacetValueBuilder withLocalizableDisplayItem(String displayName) {
            this.displayItem = new SimpleDisplayItem(displayName, true);
            return this;
        }

        public FacetValueBuilder withFormattableDisplayItem(String suffix, String arg) {
            this.displayItem = new FormattableDisplayItem(suffix, arg);
            return this;
        }

        public FacetValueBuilder withCount(int count) {
            this.count = count;
            return this;
        }

        public FacetValueBuilder withFilter(Filter filter) {
            this.filter = filter;
            return this;
        }

        public FacetValueBuilder addOption(Option option) {
            if (options == null) {
                options = new LinkedList<Option>();
            }

            options.add(option);
            return this;
        }

        public FacetValueBuilder withOptions(List<Option> options) {
            this.options = options;
            return this;
        }

        public FacetValue build() {
            checkNotNull(id, MISSING_FIELD, "id");
            checkNotNull(displayItem, MISSING_FIELD, DisplayItem.class.getSimpleName());
            if (options == null) {
                checkNotNull(filter, "A filter or at least two options must be set!");
            } else {
                checkArgument(options.size() > 1, "At least two " + Option.class.getSimpleName() + "s must be set!");
            }

            return new FacetValue(this);
        }
    }
}
