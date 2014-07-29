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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.flywaytest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import org.flywaydb.core.Flyway;
import org.osgi.framework.BundleContext;
import com.openexchange.flywaytest.osgi.OXResolver;

/**
 * {@link FlywayTest}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class FlywayTest {

    private final Flyway flyway;

    public FlywayTest(BundleContext context, OXResolver resolver) {

        ClassLoader thisClassLoader = getClass().getClassLoader();

        this.flyway = new Flyway();
        if (!flyway.getClassLoader().equals(thisClassLoader)) {
            flyway.setClassLoader(thisClassLoader);
        }
        flyway.setResolvers(resolver);

        Properties props = new Properties();
        InputStream in = null;
        try {
            Enumeration<URL> url = context.getBundle().findEntries("/conf/", "flyway.properties", false);
            if (url.hasMoreElements()) {
                in = url.nextElement().openStream();
                props.load(in);
                flyway.configure(props);
            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }

        flyway.setInitOnMigrate(true);
        int successful = flyway.migrate();
        if (successful > 0) {
            System.out.println(successful + " successful migrations");
        }

    }

}
