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

package com.openexchange.gdpr.dataexport.impl.storage;

import java.sql.Connection;
import java.util.Collection;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.java.Strings;

/**
 * {@link GlobalDbDataExportSql} - The SQL access using global database.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class GlobalDbDataExportSql extends AbstractDataExportSql<String> {

    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link GlobalDbDataExportSql}.
     *
     * @param databaseService The database service
     * @param configViewFactory The factory for config views
     * @param config The configuration
     */
    public GlobalDbDataExportSql(DatabaseService databaseService, ConfigViewFactory configViewFactory, DataExportConfig config) {
        super(databaseService, config);
        this.configViewFactory = configViewFactory;
    }

    @Override
    protected void backReadOnly(String group, Connection con) {
        if (con != null) {
            databaseService.backReadOnlyForGlobal(group, con);
        }
    }

    @Override
    protected void backWritable(boolean modified, String group, Connection con) {
        if (con != null) {
            if (modified) {
                databaseService.backWritableForGlobal(group, con);
            } else {
                databaseService.backWritableForGlobalAfterReading(group, con);
            }
        }
    }

    @Override
    protected Connection getReadOnly(String group) throws OXException {
        return databaseService.getReadOnlyForGlobal(group);
    }

    @Override
    protected Connection getWritable(String group) throws OXException {
        return databaseService.getWritableForGlobal(group);
    }

    @Override
    protected Collection<String> getSchemaReferences() throws OXException {
        return databaseService.getDistinctGroupsPerSchema();
    }

    /** The name used for the special "default" context group */
    private static final String DEFAULT_GROUP = "default";

    @Override
    protected String getSchemaReference(int contextId) throws OXException {
        ConfigView view = configViewFactory.getView(-1, contextId);
        String group = view.opt("com.openexchange.context.group", String.class, null);
        return Strings.isEmpty(group) ? DEFAULT_GROUP : group;
    }

}
