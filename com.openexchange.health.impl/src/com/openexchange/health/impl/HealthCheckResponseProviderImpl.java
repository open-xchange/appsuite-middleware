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

package com.openexchange.health.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.State;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.spi.HealthCheckResponseProvider;


/**
 * {@link HealthCheckResponseProviderImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class HealthCheckResponseProviderImpl implements HealthCheckResponseProvider {

    @Override
    public HealthCheckResponseBuilder createResponseBuilder() {
        return new HealthCheckResponseBuilderImpl();
    }

    private static class HealthCheckResponseBuilderImpl extends HealthCheckResponseBuilder {

        private String name = "unknown";
        private State state = State.UP;
        private Map<String, Object> data;

        /**
         * Initializes a new {@link HealthCheckResponseBuilderImpl}.
         */
        HealthCheckResponseBuilderImpl() {
            super();
        }

        @Override
        public HealthCheckResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, String value) {
            return putData(key, value);
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, long value) {
            return putData(key, value);
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, boolean value) {
            return putData(key, value);
        }

        @Override
        public HealthCheckResponseBuilder up() {
            this.state = State.UP;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder down() {
            this.state = State.DOWN;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder state(boolean up) {
            return up ? up() : down();
        }

        @Override
        public HealthCheckResponse build() {
            return new HealthCheckResponse() {

                @Override
                public State getState() {
                    return state;
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public Optional<Map<String, Object>> getData() {
                    return Optional.ofNullable(data);
                }
            };
        }

        private HealthCheckResponseBuilder putData(String key, Object value) {
            if (null == data) {
                data = new ConcurrentHashMap<>();
            }
            data.put(key, value);
            return this;
        }

    }

}
