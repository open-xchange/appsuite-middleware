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


package com.openexchange.admin.console;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * {@link CLIOption} - A command-line option.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class CLIOption {

    private final String shortForm;

    private final String longForm;

    private final boolean wantsValue;

    /**
     * Initializes a new {@link CLIOption}.
     *
     * @param longForm The long form
     * @param wantsValue <code>true</code> if this option expects a value; otherwise <code>false</code>
     */
    protected CLIOption(final String longForm, final boolean wantsValue) {
        this(null, longForm, wantsValue);
    }

    /**
     * Initializes a new {@link CLIOption}.
     *
     * @param shortForm The short form
     * @param longForm The long form
     * @param wantsValue <code>true</code> if this option expects a value; otherwise <code>false</code>
     */
    protected CLIOption(final char shortForm, final String longForm, final boolean wantsValue) {
        this(String.valueOf(shortForm), longForm, wantsValue);
    }

    /**
     * Initializes a new {@link CLIOption}.
     *
     * @param shortForm The short form
     * @param longForm The long form
     * @param wantsValue <code>true</code> if this option expects a value; otherwise <code>false</code>
     */
    protected CLIOption(final String shortForm, final String longForm, final boolean wantsValue) {
        super();
        if (null == longForm) {
            throw new IllegalArgumentException("longForm must not be null");
        }
        this.shortForm = shortForm;
        this.longForm = longForm;
        this.wantsValue = wantsValue;
    }

    /**
     * Gets option's short form.
     *
     * @return The short form
     */
    public String shortForm() {
        return shortForm;
    }

    /**
     * Gets option's long form.
     *
     * @return The long form
     */
    public String longForm() {
        return longForm;
    }

    /**
     * Tells whether or not this option wants a value
     */
    public boolean wantsValue() {
        return wantsValue;
    }

    /**
     * Parses the specified option's value.
     *
     * @param value The option's value
     * @param locale The locale
     * @return The option's value
     * @throws CLIIllegalOptionValueException If an illegal or missing value is experienced
     */
    public Object parseValue(final String value, final Locale locale) throws CLIIllegalOptionValueException {
        if (!wantsValue) {
            return Boolean.TRUE;
        }
        if (value == null) {
            throw new CLIIllegalOptionValueException(this, "");
        }
        return this.parseValueInternal(value, locale);
    }

    /**
     * Override to parse option's value.
     *
     * @param value The option's value
     * @param locale The locale
     * @return The parsed option's value
     * @throws CLIIllegalOptionValueException If an illegal or missing value is experienced
     */
    @SuppressWarnings("unused")
    protected Object parseValueInternal(final String value, final Locale locale) throws CLIIllegalOptionValueException {
        return null;
    }

    /**
     * A boolean option.
     */
    public static class CLIBooleanOption extends CLIOption {

        public CLIBooleanOption(final char shortForm, final String longForm) {
            super(shortForm, longForm, false);
        }

        public CLIBooleanOption(final String longForm) {
            super(longForm, false);
        }

    }

    /**
     * A settable boolean option.
     */
    public static class CLISettableBooleanOption extends CLIOption {

        private static final String[] VALUES_FALSE = new String[] { "no", "false", "0" };

        private static final String[] VALUES_TRUE = new String[] { "", "yes", "true", "1" };

        public CLISettableBooleanOption(final char shortForm, final String longForm) {
            super(shortForm, longForm, true);
        }

        public CLISettableBooleanOption(final String longForm) {
            super(longForm, true);
        }

        @Override
        protected Object parseValueInternal(final String value, final Locale locale) throws CLIIllegalOptionValueException {
            // Check for true value
            for (final String next : VALUES_TRUE) {
                if (value.equalsIgnoreCase(next)) {
                    return Boolean.TRUE;
                }
            }
            // Check for false value
            for (final String next : VALUES_FALSE) {
                if (value.equalsIgnoreCase(next)) {
                    return Boolean.FALSE;
                }
            }
            // Throw exception since nothing matched so far
            throw new CLIIllegalOptionValueException(this, value);
        }
    }

    /**
     * An integer option.
     */
    public static class CLIIntegerOption extends CLIOption {

        public CLIIntegerOption(final char shortForm, final String longForm) {
            super(shortForm, longForm, true);
        }

        public CLIIntegerOption(final String longForm) {
            super(longForm, true);
        }

        @Override
        protected Object parseValueInternal(final String value, final Locale locale) throws CLIIllegalOptionValueException {
            if (null == value || value.length() == 0) {
                return null;
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                throw new CLIIllegalOptionValueException(this, value, e);
            }
        }
    }

    /**
     * A long option.
     */
    public static class CLILongOption extends CLIOption {

        public CLILongOption(final char shortForm, final String longForm) {
            super(shortForm, longForm, true);
        }

        public CLILongOption(final String longForm) {
            super(longForm, true);
        }

        @Override
        protected Object parseValueInternal(final String value, final Locale locale) throws CLIIllegalOptionValueException {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
                throw new CLIIllegalOptionValueException(this, value, e);
            }
        }
    }

    /**
     * A double option.
     */
    public static class CLIDoubleOption extends CLIOption {

        public CLIDoubleOption(final char shortForm, final String longForm) {
            super(shortForm, longForm, true);
        }

        public CLIDoubleOption(final String longForm) {
            super(longForm, true);
        }

        @Override
        protected Object parseValueInternal(final String value, final Locale locale) throws CLIIllegalOptionValueException {
            try {
                final Number num = NumberFormat.getNumberInstance(locale).parse(value);
                return Double.valueOf(num.doubleValue());
            } catch (java.text.ParseException e) {
                throw new CLIIllegalOptionValueException(this, value, e);
            }
        }
    }

    /**
     * A string option.
     */
    public static class CLIStringOption extends CLIOption {

        public CLIStringOption(final char shortForm, final String longForm) {
            super(shortForm, longForm, true);
        }

        public CLIStringOption(final String longForm) {
            super(longForm, true);
        }

        @Override
        protected Object parseValueInternal(final String value, final Locale locale) {
            return value;
        }
    }

}
