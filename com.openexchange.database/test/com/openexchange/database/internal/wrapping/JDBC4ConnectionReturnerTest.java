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

package com.openexchange.database.internal.wrapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;
import com.openexchange.database.internal.AssignmentImpl;
import com.openexchange.database.internal.Pools;
import com.openexchange.database.internal.ReplicationMonitor;

/**
 * Tests methods in class {@link JDBC4ConnectionReturner}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class JDBC4ConnectionReturnerTest {

    /**
     * The delegate is null if a connection is returned 2 times to the pool. The second return should give an SQLException to detect coding
     * problems.
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("static-method")
    @Test(expected = SQLException.class)
    public final void testForBug22113() throws SQLException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException {
        Connection delegate = null;
        Object object = getConstructor().newInstance(null, null, null, delegate, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
        Connection con = (Connection) object;
        con.close();
    }

    private static Constructor<?> getConstructor() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Class<?> clazz;
        if (isJDBC41()) {
            try {
                clazz = Class.forName("com.openexchange.database.internal.wrapping.JDBC41ConnectionReturner");
            } catch (ClassNotFoundException e) {
                // maybe test executing JVM is newer. Try to load old class.
                clazz = Class.forName("com.openexchange.database.internal.wrapping.JDBC4ConnectionReturner");
            }
        } else {
            clazz = Class.forName("com.openexchange.database.internal.wrapping.JDBC4ConnectionReturner");
        }
        Constructor<?> constructor = clazz.getConstructor(Pools.class, ReplicationMonitor.class, AssignmentImpl.class, Connection.class, boolean.class, boolean.class, boolean.class);
        return constructor;
    }

    private static boolean isJDBC41() {
        for (Method method : Connection.class.getMethods()) {
            if ("abort".equals(method.getName())) {
                return true;
            }
        }
        return false;
    }
}
