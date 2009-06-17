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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.publish.database;

import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.schema.Tables.publications;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.MockUser;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.sql.AbstractPublicationSQLStorageTest;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.SELECT;

/**
 * {@link PublicationUserDeleteListenerTest}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class PublicationUserDeleteListenerTest extends AbstractPublicationSQLStorageTest {

    private MockUser user;

    private PublicationUserDeleteListener listener;

    private Connection writeCon;

    private Connection readCon;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.user = new MockUser();
        user.setId(userId);
        
        this.listener = new PublicationUserDeleteListener();
        this.listener.setStorage(storage);
        
        this.writeCon = getDBProvider().getWriteConnection(ctx);
        this.readCon = getDBProvider().getReadConnection(ctx);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testShouldPerformDeletionAttempt() throws DeleteFailedException, SQLException, PublicationException, TransactionException {
        DeleteEvent event = new DeleteEvent(user, user.getId(), DeleteEvent.TYPE_USER, ctx);
        storage.rememberPublication(pub1);
        listener.deletePerformed(event, readCon , writeCon );
        SELECT select = new SELECT(ASTERISK).FROM(publications).WHERE( new EQUALS("user_id", userId).AND( new EQUALS("cid", ctx.getContextId()) ) );
        assertNoResult(new StatementBuilder().buildCommand(select));
    }

    public void testShouldNotActOnDeleteEventForNonUsers() throws DeleteFailedException, SQLException {
//        DeleteEvent event = new DeleteEvent(user, user.getId(), DeleteEvent.TYPE_GROUP, ctx);

    }
}
