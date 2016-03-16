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
