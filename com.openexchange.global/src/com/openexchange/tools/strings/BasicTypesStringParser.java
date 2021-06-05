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

package com.openexchange.tools.strings;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link BasicTypesStringParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class BasicTypesStringParser implements StringParser {

    private static interface Parser<T> {

        T parse(String s);
    }

    private final Map<Class<?>, Parser<?>> parsers;

    public BasicTypesStringParser() {
        super();
        Map<Class<?>, Parser<?>> parsers = new HashMap<Class<?>, Parser<?>>(16, 0.9F);

        // Integer
        {
            Parser<Integer> parser = new Parser<Integer>() {

                @Override
                public Integer parse(String s) {
                    try {
                        return Integer.valueOf(s.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            };
            parsers.put(Integer.class, parser);
            parsers.put(int.class, parser);
        }

        // Long
        {
            Parser<Long> parser = new Parser<Long>() {

                @Override
                public Long parse(String s) {
                    try {
                        return Long.valueOf(s.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            };
            parsers.put(Long.class, parser);
            parsers.put(long.class, parser);
        }

        // Short
        {
            Parser<Short> parser = new Parser<Short>() {

                @Override
                public Short parse(String s) {
                    try {
                        return Short.valueOf(s.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            };
            parsers.put(Short.class, parser);
            parsers.put(short.class, parser);
        }

        // Float
        {
            Parser<Float> parser = new Parser<Float>() {

                @Override
                public Float parse(String s) {
                    try {
                        return Float.valueOf(s.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            };
            parsers.put(Float.class, parser);
            parsers.put(float.class, parser);
        }

        // Double
        {
            Parser<Double> parser = new Parser<Double>() {

                @Override
                public Double parse(String s) {
                    try {
                        return Double.valueOf(s.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            };
            parsers.put(Double.class, parser);
            parsers.put(double.class, parser);
        }

        // Byte
        {
            Parser<Byte> parser = new Parser<Byte>() {

                @Override
                public Byte parse(String s) {
                    try {
                        return Byte.valueOf(s.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            };
            parsers.put(Byte.class, parser);
            parsers.put(byte.class, parser);
        }

        // Boolean
        {
            Parser<Boolean> parser = new Parser<Boolean>() {

                @Override
                public Boolean parse(String s) {
                    return Boolean.valueOf(s.trim());
                }
            };
            parsers.put(Boolean.class, parser);
            parsers.put(boolean.class, parser);
        }

        this.parsers = parsers;
    }

    @Override
    public <T> T parse(final String s, final Class<T> t) {
        if (s == null) {
            return null;
        }
        if (t == String.class) {
            @SuppressWarnings("unchecked") T val = (T) s;
            return val;
        }

        Parser<?> parser = parsers.get(t);
        if (null == parser) {
            return null;
        }
        @SuppressWarnings("unchecked") T parsed = (T) parser.parse(s);
        return parsed;
    }

}
