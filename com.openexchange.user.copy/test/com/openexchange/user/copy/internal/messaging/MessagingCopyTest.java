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

package com.openexchange.user.copy.internal.messaging;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.genconf.ConfAttribute;
import com.openexchange.user.copy.internal.oauth.OAuthAccount;
import com.openexchange.user.copy.internal.oauth.OAuthCopyTest;


/**
 * {@link MessagingCopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MessagingCopyTest extends AbstractUserCopyTest {

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
    public MessagingCopyTest(final String name) {
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
    
    public void testMessagingAccountCopy() throws Exception {
        final MessagingCopyTask copyTask = new MessagingCopyTask();
        final List<MessagingAccount> originAccounts = copyTask.loadMessagingAccountsFromDB(srcCon, srcCtxId, srcUsrId);
        copyTask.fillMessagingAccountsWithConfig(originAccounts, srcCon, srcCtxId);
        
        try {     
            disableForeignKeyChecks(dstCon);
            DBUtils.startTransaction(dstCon);
            final Map<String, ObjectMapping<?>> map = getObjectMappingWithFolders();
            addOAuthAccounts(map, originAccounts);
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
        
        final List<MessagingAccount> targetAccounts = copyTask.loadMessagingAccountsFromDB(dstCon, dstCtxId, dstUsrId);
        checkAndGetMatchingObjects(originAccounts, targetAccounts);
    }
    
    private List<Integer> prepareAccountIds(final List<MessagingAccount> accounts) {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final MessagingAccount account : accounts) {
            final List<ConfAttribute> stringAttributes = account.getStringAttributes();
            if (stringAttributes != null) {
                for (final ConfAttribute attribute : stringAttributes) {
                    if (attribute.getName() != null && attribute.getName().equals("account")) {
                        try {
                            final int id = Integer.parseInt(attribute.getValue());
                            ids.add(id);
                        } catch (final NumberFormatException e) {
                            // Skip this one
                        }

                        break;
                    }
                }
            }
        }
        
        return ids;
    }
    
    private void addOAuthAccounts(final Map<String, ObjectMapping<?>> map, final List<MessagingAccount> accounts) {
        final List<Integer> ids = prepareAccountIds(accounts);
        final IntegerMapping mapping = new IntegerMapping();
        for (final Integer id : ids) {
            mapping.addMapping(id, id);
        }
        
        map.put(OAuthAccount.class.getName(), mapping);
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
        deleteAllFromTablesForCid(dstCtxId, "cid", dstCon, "messagingAccount");
        super.tearDown();
    }

}
