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

package com.openexchange.user.copy.internal.subscription;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.genconf.ConfAttribute;
import com.openexchange.user.copy.internal.oauth.OAuthAccount;


/**
 * {@link SubscriptionCopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SubscriptionCopyTest extends AbstractUserCopyTest {

    private int srcUsrId;
    
    private int srcCtxId;
    
    private int dstCtxId;

    private Connection srcCon;

    private Connection dstCon;

    private int dstUsrId;
    
    
    /**
     * Initializes a new {@link SubscriptionCopyTest}.
     * @param name
     */
    public SubscriptionCopyTest(final String name) {
        super(name);
    }
    
    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#setUp()
     */
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
    
    public void testSubscriptionCopy() throws Exception {
        final SubscriptionCopyTask copyTask = new SubscriptionCopyTask();
        final Map<Integer, Subscription> originSubscriptions = copyTask.loadSubscriptionsFromDB(srcCon, srcUsrId, srcCtxId);
        copyTask.fillSubscriptionsWithAttributes(originSubscriptions, srcCon, srcCtxId);
        
        try {     
            disableForeignKeyChecks(dstCon);
            DBUtils.startTransaction(dstCon);
            final Map<String, ObjectMapping<?>> map = getObjectMappingWithFolders();
            addOAuthAccounts(map, originSubscriptions);
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
        
        final Map<Integer, Subscription> targetSubscriptions = copyTask.loadSubscriptionsFromDB(dstCon, dstUsrId, dstCtxId);
        copyTask.fillSubscriptionsWithAttributes(targetSubscriptions, dstCon, dstCtxId);
        
        checkSubscriptions(originSubscriptions, targetSubscriptions);
    }
    
    private List<Integer> prepareAccountIds(final Map<Integer, Subscription> subscriptions) {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final Subscription subscription : subscriptions.values()) {
            final List<ConfAttribute> stringAttributes = subscription.getStringAttributes();
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
    
    private void addOAuthAccounts(final Map<String, ObjectMapping<?>> map, final Map<Integer, Subscription> originSubscriptions) {
        final List<Integer> ids = prepareAccountIds(originSubscriptions);
        final IntegerMapping mapping = new IntegerMapping();
        for (final Integer id : ids) {
            mapping.addMapping(id, id);
        }
        
        map.put(OAuthAccount.class.getName(), mapping);
    }
    
    private void checkSubscriptions(final Map<Integer, Subscription> originSubscriptions, final Map<Integer, Subscription> targetSubscriptions) {
        checkAndGetMatchingObjects(originSubscriptions.values(), targetSubscriptions.values(), new SubscriptionComparator());
    }
    
    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtxId, "cid", dstCon, "subscriptions", "genconf_attributes_strings", "genconf_attributes_bools");
        super.tearDown();
    }   

    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return new String[] { "sequence_subscriptions", "sequence_genconf" };
    }
    
    private static final class SubscriptionComparator implements Comparator<Subscription> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(final Subscription o1, final Subscription o2) {
            if (o1.equals(o2)) {
                final List<ConfAttribute> o1BoolAttributes = o1.getBoolAttributes();
                final List<ConfAttribute> o2BoolAttributes = o2.getBoolAttributes();                
                if (o1BoolAttributes.size() != o2BoolAttributes.size()) {
                    return -1;
                }
                
                for (final ConfAttribute o1Attr : o1BoolAttributes) {
                    boolean found = false;
                    for (final ConfAttribute o2Attr : o2BoolAttributes) {
                        if (o1Attr.equals(o2Attr)) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        return -1;
                    }
                }
                
                final List<ConfAttribute> o1StringAttributes = o1.getStringAttributes();
                final List<ConfAttribute> o2StringAttributes = o2.getStringAttributes();
                if (o1StringAttributes.size() != o2StringAttributes.size()) {
                    return -1;
                }
                
                for (final ConfAttribute o1Attr : o1StringAttributes) {
                    boolean found = false;
                    for (final ConfAttribute o2Attr : o2StringAttributes) {
                        if (o1Attr.equals(o2Attr)) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        return -1;
                    }
                }
                
                return 0;
            }
            
            return -1;
        }
        
    }

}
