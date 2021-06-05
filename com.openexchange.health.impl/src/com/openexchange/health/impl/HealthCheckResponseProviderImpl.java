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

package com.openexchange.health.impl;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.Status;
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
        private Status status = Status.UP;
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
            return putData(key, L(value));
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, boolean value) {
            return putData(key, B(value));
        }

        @Override
        public HealthCheckResponseBuilder up() {
            this.status = Status.UP;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder down() {
            this.status = Status.DOWN;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder status(boolean up) {
            return up ? up() : down();
        }

        @Override
        public HealthCheckResponse build() {
            String name = this.name;
            Status status = this.status;
            Map<String, Object> data = this.data;
            return new HealthCheckResponseImpl(status, data, name);
        }

        private HealthCheckResponseBuilder putData(String key, Object value) {
            if (null == data) {
                data = new ConcurrentHashMap<>();
            }
            data.put(key, value);
            return this;
        }
    }

    private static class HealthCheckResponseImpl extends HealthCheckResponse {

        private final Status status;
        private final Map<String, Object> data;
        private final String name;

        /**
         * Initializes a new {@link HealthCheckResponseImpl}.
         */
        HealthCheckResponseImpl(Status status, Map<String, Object> data, String name) {
            this.status = status;
            this.data = data;
            this.name = name;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Optional<Map<String, Object>> getData() {
            return Optional.ofNullable(data);
        }
    }

}
