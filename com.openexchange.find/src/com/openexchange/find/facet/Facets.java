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
 * {@link Facets}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class Facets {

    public static SimpleFacetBuilder newSimpleBuilder(FacetType type) {
        return new SimpleFacetBuilder(type);
    }

    public static DefaultFacetBuilder newDefaultBuilder(FacetType type) {
        return new DefaultFacetBuilder(type);
    }

    public static ExclusiveFacetBuilder newExclusiveBuilder(FacetType type) {
        return new ExclusiveFacetBuilder(type);
    }

    public static abstract class FacetBuilder<F extends AbstractFacet> {

        protected static final String MISSING_FIELD = "A %s must be set!";

        protected final FacetType type;

        private final List<String> flags = new LinkedList<String>();

        public FacetBuilder(final FacetType type) {
            super();
            this.type = type;
        }

        /**
         * Adds a flag
         * 
         * @param flag The flag
         * @return The facet builder
         */
        public FacetBuilder<F> addFlag(String flag) {
            return null;
        }

        public F build() {
            checkNotNull(type, MISSING_FIELD, FacetType.class.getSimpleName());
            F facet = prepare();
            facet.setType(type);
            for (String flag : flags) {
                facet.addFlag(flag);
            }

            fill(facet);
            return facet;
        }

        protected abstract void check();

        protected abstract F prepare();

        protected abstract void fill(F facet);

    }


    public static final class SimpleFacetBuilder extends FacetBuilder<SimpleFacet> {

        private DisplayItem displayItem;

        private Filter filter;

        public SimpleFacetBuilder(FacetType type) {
            super(type);
        }

        public SimpleFacetBuilder withFilter(Filter filter) {
            this.filter = filter;
            return this;
        }

        public SimpleFacetBuilder withDisplayItem(DisplayItem displayItem) {
            this.displayItem = displayItem;
            return this;
        }

        public SimpleFacetBuilder withSimpleDisplayItem(String displayName) {
            this.displayItem = new SimpleDisplayItem(displayName);
            return this;
        }

        public SimpleFacetBuilder withLocalizableDisplayItem(String displayName) {
            this.displayItem = new SimpleDisplayItem(displayName, true);
            return this;
        }

        public SimpleFacetBuilder withFormattableDisplayItem(String suffix, String arg) {
            this.displayItem = new FormattableDisplayItem(suffix, arg);
            return this;
        }

        @Override
        protected void check() {
            checkNotNull(displayItem, MISSING_FIELD, DisplayItem.class.getSimpleName());
            checkNotNull(filter, MISSING_FIELD, Filter.class.getSimpleName());
        }

        @Override
        protected SimpleFacet prepare() {
            return new SimpleFacet();
        }

        @Override
        protected void fill(SimpleFacet facet) {
            facet.setDisplayItem(displayItem);
            facet.setFilter(filter);
        }
    }

    public static class DefaultFacetBuilder extends FacetBuilder<DefaultFacet> {

        private List<FacetValue> values = new LinkedList<FacetValue>();

        public DefaultFacetBuilder(FacetType type) {
            super(type);
        }

        public DefaultFacetBuilder addValue(FacetValue value) {
            values.add(value);
            return this;
        }

        public DefaultFacetBuilder withValues(List<FacetValue> values) {
            this.values = values;
            return this;
        }

        @Override
        protected void check() {
            checkNotNull(values, MISSING_FIELD, FacetValue.class.getSimpleName());
            checkArgument(!values.isEmpty(), "At least one " + FacetValue.class.getSimpleName() + " must be set!");
        }

        @Override
        protected DefaultFacet prepare() {
            return new DefaultFacet();
        }

        @Override
        protected void fill(DefaultFacet facet) {
            facet.setValues(values);
        }
    }

    public static final class ExclusiveFacetBuilder extends DefaultFacetBuilder {

        public ExclusiveFacetBuilder(FacetType type) {
            super(type);
        }

        @Override
        protected ExclusiveFacet prepare() {
            return new ExclusiveFacet();
        }

    }
}
