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

package com.openexchange.flywaytest.osgi;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.flywaytest.migrations.V1_2_0__TestMigration;
import com.openexchange.flywaytest.migrations.V1_2_1__AddColumToTable;
import com.openexchange.flywaytest.migrations.V1_2_2__FillUUID;


/**
 * {@link Activator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class Activator implements BundleActivator {
    
    private ServiceRegistration<JdbcMigration> reg1, reg2, reg3;

    @Override
    public void start(BundleContext context) throws Exception {
        JdbcMigration mig1 = new V1_2_1__AddColumToTable();
        JdbcMigration mig2 = new V1_2_2__FillUUID();
        JdbcMigration mig3 = new V1_2_0__TestMigration();
        reg1 = context.registerService(JdbcMigration.class, mig1, null);
        reg2 = context.registerService(JdbcMigration.class, mig2, null);
        reg3 = context.registerService(JdbcMigration.class, mig3, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (null != reg1) {
            context.ungetService(reg1.getReference());
        }
        if (null != reg2) {
            context.ungetService(reg2.getReference());
        }
        if (null != reg3) {
            context.ungetService(reg3.getReference());
        }
    }

}
