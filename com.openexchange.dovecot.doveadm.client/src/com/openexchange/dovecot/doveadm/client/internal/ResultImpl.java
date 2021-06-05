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
