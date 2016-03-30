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

package com.openexchange.user.copy.internal.uwa;

import java.sql.Connection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;


/**
 * {@link UWACopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UWACopyTest extends AbstractUserCopyTest {
    
    private int srcUsrId;

    private Connection srcCon;

    private Connection dstCon;

    private ContextImpl srcCtx;

    private ContextImpl dstCtx;

    private int dstUsrId;
    

    /**
     * Initializes a new {@link UWACopyTest}.
     * @param name
     */
    public UWACopyTest(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcUsrId = getSourceUserId();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtx = getSourceContext();
        dstCtx = getDestinationContext();
        dstUsrId = getDestinationUserId();
    }
    
    public void testCopyWidgets() throws Exception {
        final UWACopyTask copyTask = new UWACopyTask();
        final List<Widget> sourceWidgets = copyTask.loadWidgetsFromDB(srcCon, srcCtx.getContextId(), srcUsrId);
        
        DBUtils.startTransaction(dstCon);
        try {
            copyTask.copyUser(getBasicObjectMapping());
        } catch (final OXException e) {
            DBUtils.rollback(dstCon);
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        }        
        dstCon.commit();
        
        final List<Widget> targetWidgets = copyTask.loadWidgetsFromDB(dstCon, dstCtx.getContextId(), dstUsrId);
        checkAndGetMatchingObjects(sourceWidgets, targetWidgets); 
    }
    
    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtx.getContextId(), "cid", dstCon, "uwaWidget", "uwaWidgetPosition");
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
