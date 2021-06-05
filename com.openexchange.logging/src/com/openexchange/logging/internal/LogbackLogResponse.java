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

package com.openexchange.logging.internal;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.logging.LogResponse;
import com.openexchange.logging.MessageType;

/**
 * {@link LogbackLogResponse}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class LogbackLogResponse implements LogResponse {

    private static final long serialVersionUID = 1904333462927163550L;

    private static final List<String> EMPTY = Collections.emptyList();

    private final List<String> errors;
    private final List<String> warnings;
    private final List<String> infos;

    /**
     * Initializes a new {@link LogbackLogResponse}.
     */
    LogbackLogResponse(List<String> errors, List<String> warnings, List<String> infos) {
        super();
        this.errors = errors;
        this.warnings = warnings;
        this.infos = infos;
    }

    @Override
    public List<String> getMessages(MessageType type) {
        switch (type) {
            case ERROR:
                return errors;
            case WARNING:
                return warnings;
            case INFO:
                return infos;
            default:
                return EMPTY;
        }
    }

    //////////////////////////////////// BUILDER /////////////////////////////////////

    /**
     * Creates a new builder instance.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>DefaultLogbackResponse</code> */
    public static final class Builder {

        private final ImmutableList.Builder<String> errors;
        private final ImmutableList.Builder<String> warnings;
        private final ImmutableList.Builder<String> infos;

        /**
         * Initializes a new {@link LogbackLogResponse.Builder}.
         */
        Builder() {
            super();
            errors = ImmutableList.builder();
            warnings = ImmutableList.builder();
            infos = ImmutableList.builder();
        }

        /**
         * Adds a message to the response builder.
         *
         * @param message The message
         * @param type The {@link MessageType}
         */
        public Builder withMessage(String message, MessageType type) {
            switch (type) {
                case ERROR:
                    errors.add(message);
                    break;
                case WARNING:
                    warnings.add(message);
                    break;
                case INFO:
                    infos.add(message);
                    break;
            }
            return this;
        }

        /**
         * Builds the response
         *
         * @return the response
         */
        public LogbackLogResponse build() {
            return new LogbackLogResponse(errors.build(), warnings.build(), infos.build());
        }
    }
}
