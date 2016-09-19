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

package com.openexchange.dovecot.doveadm.client;

import java.util.ArrayList;
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
                    throw new IllegalArgumentException("Value is neither of type Boolean, Character, Number nor String");
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
                    throw new IllegalArgumentException("Value is neither of type Boolean, Character, Number nor String");
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
                            throw new IllegalArgumentException("Value is neither of type Boolean, Character, Number nor String");
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
                            throw new IllegalArgumentException("Value is neither of type Boolean, Character, Number nor String");
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

    /**
     * Initializes a new {@link DefaultDoveAdmCommand}.
     */
    DefaultDoveAdmCommand(String command, ImmutableMap<String, Object> parameters, String optionalIdentifier) {
        super();
        this.command = command;
        this.parameters = parameters;
        this.optionalIdentifier = optionalIdentifier;
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

}
