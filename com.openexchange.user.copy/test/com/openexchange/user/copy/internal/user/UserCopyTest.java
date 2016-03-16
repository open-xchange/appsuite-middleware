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

package com.openexchange.user.copy.internal.user;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;


/**
 * {@link UserCopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UserCopyTest extends AbstractUserCopyTest {
    
    private int srcUsrId;

    private Connection srcCon;

    private Connection dstCon;

    private ContextImpl srcCtx;

    private ContextImpl dstCtx;

    /**
     * Initializes a new {@link UserCopyTest}.
     * @param name
     */
    public UserCopyTest(final String name) {
        super(name);        
    }
    
    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcUsrId = getSourceUserId();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtx = getSourceContext();
        dstCtx = getDestinationContext();
    }
    
    public void testUserCopy() throws Exception {
        final UserService userService = new MockUserService();
        final UserCopyTask copyTask = new UserCopyTask(userService);
        final User sourceUser = userService.getUser(srcCon, srcUsrId, srcCtx);        
        
        DBUtils.startTransaction(dstCon);
        UserMapping mapping = null;
        try {
            mapping = copyTask.copyUser(getBasicObjectMapping());
        } catch (final OXException e) {
            DBUtils.rollback(dstCon);
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        }        
        dstCon.commit();
        
        final User targetUser = userService.getUser(dstCon, mapping.getDestination(mapping.getSource(srcUsrId)).getId(), dstCtx);
        checkUser(sourceUser, targetUser);
    }
    
    private void checkUser(final User sourceUser, final User targetUser) throws Exception {
        assertEquals("Password was not equal.", sourceUser.getUserPassword(), targetUser.getUserPassword());
        assertEquals("isMailEnabled was not equal.", sourceUser.isMailEnabled(), targetUser.isMailEnabled());
        assertEquals("getImapServer was not equal.", sourceUser.getImapServer(), targetUser.getImapServer());
        assertEquals("getImapLogin was not equal.", sourceUser.getImapLogin(), targetUser.getImapLogin());
        assertEquals("getSmtpServer was not equal.", sourceUser.getSmtpServer(), targetUser.getSmtpServer());
        assertEquals("getMailDomain was not equal.", sourceUser.getMailDomain(), targetUser.getMailDomain());
        assertEquals("getShadowLastChange was not equal.", sourceUser.getShadowLastChange(), targetUser.getShadowLastChange());
        assertEquals("getMail was not equal.", sourceUser.getMail(), targetUser.getMail());
        assertEquals("getTimeZone was not equal.", sourceUser.getTimeZone(), targetUser.getTimeZone());
        assertEquals("getPreferredLanguage was not equal.", sourceUser.getPreferredLanguage(), targetUser.getPreferredLanguage());
        assertEquals("getPasswordMech was not equal.", sourceUser.getPasswordMech(), targetUser.getPasswordMech());
        
        checkAttributes(sourceUser.getAttributes(), targetUser.getAttributes());
    }
    
    private void checkAttributes(final Map<String, Set<String>> sourceAttributes, final Map<String, Set<String>> targetAttributes) throws Exception {
        for (final String sourceKey : sourceAttributes.keySet()) {
            if (targetAttributes.containsKey(sourceKey)) {
                final Set<String> toCompare = targetAttributes.get(sourceKey);
                assertEquals(sourceAttributes.get(sourceKey), toCompare);
            } else {
                fail("Target attributes did not contain " + sourceKey);
            }
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtx.getContextId(), "cid", dstCon, "login2user", "user", "user_attribute", "user_configuration");
        super.tearDown();
    }

    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return null;
    }

}
