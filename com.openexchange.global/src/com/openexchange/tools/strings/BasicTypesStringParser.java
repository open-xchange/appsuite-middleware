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
            return (T) s;
        }

        Parser<?> parser = parsers.get(t);
        if (null == parser) {
            return null;
        }
        return (T) parser.parse(s);
    }

}
