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

package com.openexchange.user.copy.internal.oauth;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.id.SimIDGenerator;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;


/**
 * {@link OAuthCopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class OAuthCopyTest extends AbstractUserCopyTest {
    
    private int srcUsrId;
    
    private int srcCtxId;
    
    private int dstCtxId;

    private Connection srcCon;

    private Connection dstCon;

    private int dstUsrId;

    /**
     * Initializes a new {@link OAuthCopyTest}.
     * @param name
     */
    public OAuthCopyTest(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcUsrId = getSourceUserId();
        dstUsrId = getDestinationUserId();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtxId = getSourceContext().getContextId();
        dstCtxId = getDestinationContext().getContextId(); 
    }
    
    public void testOAuthCopy() throws Exception {
        final IDGeneratorService idService = new SimIDGenerator();
        final OAuthCopyTask copyTask = new OAuthCopyTask(idService);
        final List<OAuthAccount> originAccounts = copyTask.loadOAuthAccountsFromDB(srcCon, srcUsrId, srcCtxId);
        
        try {     
            disableForeignKeyChecks(dstCon);
            DBUtils.startTransaction(dstCon);
            final Map<String, ObjectMapping<?>> map = getObjectMappingWithFolders();
            copyTask.copyUser(map);
            dstCon.commit();
        } catch (final OXException e) {
            DBUtils.rollback(dstCon);
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        } finally {
            enableForeignKeyChecks(dstCon);
            dstCon.commit();
        }
        
        final List<OAuthAccount> targetAccounts = copyTask.loadOAuthAccountsFromDB(dstCon, dstUsrId, dstCtxId);
        checkAndGetMatchingObjects(originAccounts, targetAccounts);
    }

    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return null;
    }
    
    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtxId, "cid", dstCon, "oauthAccounts");
        super.tearDown();
    }

}
