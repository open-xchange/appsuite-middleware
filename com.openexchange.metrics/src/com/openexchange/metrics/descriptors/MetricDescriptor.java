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

package com.openexchange.metrics.descriptors;

/**
 * 
 * {@link MetricDescriptor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class MetricDescriptor {

    private String group;
    private String name;

    /**
     * Initialises a new {@link MetricDescriptor}.
     */
    public MetricDescriptor() {
        super();
    }

    /**
     * Returns the group of this metric
     * 
     * @return the group of this metric
     */
    public String getGroup() {
        return group;
    }

    /**
     * Returns the name of this metric
     * 
     * @return the name of this metric
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the group for this metric
     * 
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Sets the name for this metric
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /////////////////////////////// Builder /////////////////////////////////

    /**
     * 
     * {@link AbstractBuilder}
     *
     * @param <T> The {@link MetricDescriptor} type
     */
    static abstract class AbstractBuilder<T extends MetricDescriptor> {

        protected static final String MISSING_FIELD = "A %s must be set!";

        protected final String group;
        protected final String name;

        /**
         * Initialises a new {@link AbstractBuilder}.
         * 
         * @param group The group for the metric
         * @param name The name for the metric
         */
        public AbstractBuilder(final String group, final String name) {
            super();
            this.group = group;
            this.name = name;
        }

        /**
         * Builds and returns the {@link MetricDescriptor}
         * 
         * @return the {@link MetricDescriptor}
         */
        public T build() {
            checkNotNull(group, "group");
            checkNotNull(name, "name");
            T descriptor = prepare();
            fill(descriptor);
            return descriptor;
        }

        /**
         * Performs a preliminary check of the descriptor's values
         */
        protected abstract void check();

        /**
         * Prepares the {@link MetricDescriptor}
         * 
         * @return The prepared {@link MetricDescriptor} as type {@link T}
         */
        protected abstract T prepare();

        /**
         * Fills values of the specified descriptor
         * 
         * @param descriptor The descriptor of which the values shall be filled
         */
        protected abstract void fill(T descriptor);

        /**
         * Check for <code>null</code> reference
         * 
         * @param reference The reference to check for <code>null</code>
         * @param errorMessage The error message for the {@link IllegalArgumentException}
         * @return The reference if not <code>null</code>
         * @throws IllegalArgumentException if the specified reference is <code>null</code>
         */
        static <T> T checkNotNull(T reference, String errorMessage) {
            if (reference == null) {
                throw new IllegalArgumentException(errorMessage);
            }
            return reference;
        }
    }
}
