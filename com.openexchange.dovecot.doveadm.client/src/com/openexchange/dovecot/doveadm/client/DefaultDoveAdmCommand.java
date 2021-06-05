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

package com.openexchange.dovecot.doveadm.client;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.ImmutableMap;

/**
 * {@link DefaultDoveAdmCommand} - The default implementation for a command for the Dovecot DoveAdm REST interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultDoveAdmCommand implements DoveAdmCommand {

    /**
     * Creates a new builder instance with optional identifier default to <code>"1"</code>.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a DoveAdm command */
    public static final class Builder {

        private String command;
        private String optionalIdentifier;
        private final ImmutableMap.Builder<String, Object> mapBuilder;

        /**
         * Initializes a new {@link DefaultDoveAdmCommand.Builder} with optional identifier default to <code>"1"</code>.
         */
        Builder() {
            super();
            optionalIdentifier = "1";
            mapBuilder = ImmutableMap.builder();
        }

        /**
         * Sets the command identifier.
         *
         * @param command The command
         * @return This builder
         */
        public Builder command(String command) {
            this.command = command;
            return this;
        }

        /**
         * Sets the optional identifier.
         *
         * @param optionalIdentifier The optional identifier
         * @return This builder
         */
        public Builder optionalIdentifier(String optionalIdentifier) {
            this.optionalIdentifier = optionalIdentifier;
            return this;
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
         * @param name The parameter name
         * @param value The parameter value
         * @return This builder
         */
        public Builder setParameter(String name, boolean value) {
            if (null != name) {
                mapBuilder.put(name, Boolean.valueOf(value));
            }
            return this;
        }

        /**
         * Sets the specified parameter.
         *
         * @param name The parameter name
         * @param value The parameter value
         * @return This builder
         */
        public Builder setParameter(String name, int value) {
            if (null != name) {
                mapBuilder.put(name, Integer.valueOf(value));
            }
            return this;
        }

        /**
         * Sets the specified parameter.
         *
         * @param name The parameter name
         * @param value The parameter value
         * @return This builder
         * @throws IllegalArgumentException If value is neither of type <code>Boolean</code>, <code>Character</code>, <code>Number</code> nor <code>String</code>
         */
        public Builder setParameter(String name, Object value) {
            if (null != name && null != value) {
                if (isInvalidParameterValue(value)) {
                    throw new IllegalArgumentException("Value itself or one of contained ones is neither of type Boolean, Character, Number nor String");
                }
                mapBuilder.put(name, value);
            }
            return this;
        }

        /**
         * Sets the specified parameter.
         *
         * @param entry The parameter entry
         * @return This builder
         * @throws IllegalArgumentException If entry's value is neither of type <code>Boolean</code>, <code>Character</code>, <code>Number</code> nor <code>String</code>
         */
        public Builder setParameter(Entry<? extends String, ? extends Object> entry) {
            if (null != entry && null != entry.getKey() && null != entry.getValue()) {
                if (isInvalidParameterValue(entry.getValue())) {
                    throw new IllegalArgumentException("Value itself or one of contained ones is neither of type Boolean, Character, Number nor String");
                }
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
                List<Entry<? extends String, ? extends String>> toInsert = new ArrayList<>(parameters.size());
                for (Entry<? extends String, ? extends String> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (null != key && null != value) {
                        if (isInvalidParameterValue(value)) {
                            throw new IllegalArgumentException("Value itself or one of contained ones is neither of type Boolean, Character, Number nor String");
                        }
                        toInsert.add(entry);
                    }
                }

                for (Entry<? extends String,? extends String> entry : toInsert) {
                    mapBuilder.put(entry.getKey(), entry.getValue());
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
                List<Entry<? extends String, ? extends String>> toInsert = new LinkedList<>();
                for (Entry<? extends String, ? extends String> entry : parameters) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (null != key && null != value) {
                        if (isInvalidParameterValue(value)) {
                            throw new IllegalArgumentException("Value itself or one of contained ones is neither of type Boolean, Character, Number nor String");
                        }
                        toInsert.add(entry);
                    }
                }

                for (Entry<? extends String,? extends String> entry : toInsert) {
                    mapBuilder.put(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        private boolean isInvalidParameterValue(Object value) {
            if (value.getClass().isArray()) {
                for (int i = Array.getLength(value); i-- > 0;) {
                    Object elem = Array.get(value, i);
                    if (isInvalidParameterValue(elem)) {
                        return true;
                    }
                }
                return false;
            }

            if (value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                for (Object elem : collection) {
                    if (isInvalidParameterValue(elem)) {
                        return true;
                    }
                }
                return false;
            }

            if (value instanceof Map) {
                try {
                    Map<String, Object> map = (Map<String, Object>) value;
                    for (Object elem : map.values()) {
                        if (isInvalidParameterValue(elem)) {
                            return true;
                        }
                    }
                    return false;
                } catch (ClassCastException e) {
                    return true;
                }
            }

            return (String.class != value.getClass() && Boolean.class != value.getClass() && Character.class != value.getClass() && !Number.class.isInstance(value));
        }

        /**
         * Builds the DoveAdm command from this builder's arguments.
         *
         * @return The DoveAdm command
         */
        public DefaultDoveAdmCommand build() {
            return new DefaultDoveAdmCommand(command, mapBuilder.build(), optionalIdentifier);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private final String command;
    private final ImmutableMap<String, Object> parameters;
    private final String optionalIdentifier;
    private int hash;

    /**
     * Initializes a new {@link DefaultDoveAdmCommand}.
     */
    DefaultDoveAdmCommand(String command, ImmutableMap<String, Object> parameters, String optionalIdentifier) {
        super();
        this.command = command;
        this.parameters = parameters;
        this.optionalIdentifier = optionalIdentifier;
        hash = 0;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String getOptionalIdentifier() {
        return optionalIdentifier;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + ((command == null) ? 0 : command.hashCode());
            result = prime * result + ((optionalIdentifier == null) ? 0 : optionalIdentifier.hashCode());
            result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefaultDoveAdmCommand)) {
            return false;
        }
        DefaultDoveAdmCommand other = (DefaultDoveAdmCommand) obj;
        if (command == null) {
            if (other.command != null) {
                return false;
            }
        } else if (!command.equals(other.command)) {
            return false;
        }
        if (optionalIdentifier == null) {
            if (other.optionalIdentifier != null) {
                return false;
            }
        } else if (!optionalIdentifier.equals(other.optionalIdentifier)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("{");
        if (command != null) {
            sb.append("command=").append(command).append(", ");
        }
        if (parameters != null) {
            sb.append("parameters=").append(parameters).append(", ");
        }
        if (optionalIdentifier != null) {
            sb.append("optionalIdentifier=").append(optionalIdentifier);
        }
        sb.append("}");
        return sb.toString();
    }

}
