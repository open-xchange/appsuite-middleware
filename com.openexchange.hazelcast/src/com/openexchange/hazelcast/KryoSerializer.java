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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.hazelcast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * {@link KryoSerializer} - Serializes/Deserialzes any object using <img src="http://kryo.googlecode.com/svn/wiki/kryo-logo.jpg" width="88"
 * height="37">.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class KryoSerializer {

    /**
     * Initializes a new {@link KryoSerializer}.
     */
    private KryoSerializer() {
        super();
    }

    /**
     * Serializes specified object to an byte array using given class loader.
     * 
     * @param obj The object to serialize
     * @param classLoader The class loader to use
     * @return The serialzed object's byte array
     */
    public static byte[] write(final Object obj, final ClassLoader classLoader) {
        final Kryo kryo = new Kryo();
        kryo.setClassLoader(classLoader);
        final Output output = new Output(8192);
        kryo.writeClassAndObject(output, obj);
        return output.toBytes();
    }

    /**
     * Deserializes specified object's byte array using given class loader.
     * 
     * @param bytes The serialzed object's byte array
     * @param classLoader The class loader to use
     * @return The desrialized object
     */
    public static Object read(final byte[] bytes, final ClassLoader classLoader) {
        final Kryo kryo = new Kryo();
        kryo.setClassLoader(classLoader);
        return kryo.readClassAndObject(new Input(bytes));
    }

}
