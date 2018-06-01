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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.oauth.impl.internal.groupware;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link OAuthAddIdentityColumnTaskV2}
 * Adds the 'identity' column and an index for the identity (cid,identity(191))
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.0
 */
public class OAuthAddIdentityColumnTaskV2 extends AbstractOAuthUpdateTask {

    private static final String IDENTITY_NAME = "identity";

    /**
     * Initialises a new {@link OAuthAddIdentityColumnTaskV2}.
     */
    public OAuthAddIdentityColumnTaskV2() {
        super();
    }

    @Override
    void innerPerform(Connection connection, PerformParameters performParameters) throws OXException, SQLException {
        if (!Tools.columnExists(connection, CreateOAuthAccountTable.TABLE_NAME, IDENTITY_NAME)) {
            Tools.addColumns(connection, CreateOAuthAccountTable.TABLE_NAME, new Column(IDENTITY_NAME, "varchar(767)"));
        }
        String indexName = Tools.existsIndex(connection, CreateOAuthAccountTable.TABLE_NAME, new String[] { "identity" });
        if (indexName != null) {
            Tools.dropIndex(connection, CreateOAuthAccountTable.TABLE_NAME, indexName);
        }
        Tools.createIndex(connection, CreateOAuthAccountTable.TABLE_NAME, IDENTITY_NAME, new String[] { "cid", "`identity`(191)" }, false);
    }

    @Override
    public String[] getDependencies() {
        return new String[] { DropForeignKeyFromOAuthAccountTask.class.getName() };
    }

}
