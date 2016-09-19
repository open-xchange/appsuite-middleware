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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.dovecot.doveadm.client.internal;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.openexchange.dovecot.doveadm.client.DefaultDoveAdmCommand;
import com.openexchange.dovecot.doveadm.client.Result;


/**
 * {@link ResultImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ResultImpl implements Result {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a DoveAdm command */
    public static final class Builder {

        private final ImmutableMap.Builder<String, String> mapBuilder;

        /**
         * Initializes a new {@link DefaultDoveAdmCommand.Builder}.
         */
        Builder() {
            super();
            mapBuilder = ImmutableMap.builder();
        }

        /**
         * Sets the specified parameter.
         *
         * @param name The parameter name
         * @param value The parameter value
         * @return This builder
         */
        public Builder setParameter(String name, String value) {
            if (null != name && null != value) {
                mapBuilder.put(name, value);
            }
            return this;
        }

        /**
         * Sets the specified parameter.
         *
         * @param entry The parameter entry
         * @return This builder
         */
        public Builder setParameter(Entry<? extends String, ? extends String> entry) {
            if (null != entry && null != entry.getKey() && null != entry.getValue()) {
                mapBuilder.put(entry);
            }
            return this;
        }

        /**
         * Sets the specified parameters.
         *
         * @param parameters The parameters
         * @return This builder
         */
        public Builder setParameters(Map<? extends String, ? extends String> parameters) {
            if (null != parameters) {
                for (Entry<? extends String, ? extends String> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (null != key && null != value) {
                        mapBuilder.put(key, value);
                    }
                }
            }
            return this;
        }

        /**
         * Sets the specified parameters.
         *
         * @param parameters The parameters
         * @return This builder
         */
        public Builder setParameters(Iterable<? extends Entry<? extends String, ? extends String>> parameters) {
            if (null != parameters) {
                for (Entry<? extends String, ? extends String> entry : parameters) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (null != key && null != value) {
                        mapBuilder.put(key, value);
                    }
                }
            }
            return this;
        }

        /**
         * Builds the ResultImpl instance from this builder's arguments.
         *
         * @return The ResultImpl instance
         */
        public ResultImpl build() {
            return new ResultImpl(mapBuilder.build());
        }
    }

    // ----------------------------------------------------

    private final Map<String, String> map;

    /**
     * Initializes a new {@link ResultImpl}.
     *
     * @param map The backing map
     */
    public ResultImpl(Map<String, String> map) {
        super();
        this.map = map;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return Iterators.unmodifiableIterator(map.entrySet().iterator());
    }

    @Override
    public String getValue(String name) {
        return map.get(name);
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
