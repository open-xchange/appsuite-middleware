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

package com.openexchange.groupware.tx;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;


@OXExceptionSource(classId=Classes.COM_OPENEXCHANGE_GROUPWARE_TX_DBPOOLPROVIDER, component=Component.TRANSACTION)
public class DBPoolProvider implements DBProvider {

	
	private static final TXExceptionFactory EXCEPTIONS = new TXExceptionFactory(DBPoolProvider.class);
	private static final Log LOG = LogFactory.getLog(DBPoolProvider.class);
	
	@OXThrows(category=Category.SUBSYSTEM_OR_SERVICE_DOWN, desc="The Database does not seem to be reachable. This must be fixed by the system administration", exceptionId=0, msg="Database cannot be reached.")	
	public Connection getReadConnection(final Context ctx) throws TransactionException {
		try {
			final Connection readCon = DBPool.pickup(ctx);
			return readCon;
		} catch (final DBPoolingException e) {
			LOG.fatal("",e);
			throw EXCEPTIONS.create(0,e);
		}
	}

	public void releaseReadConnection(final Context ctx, final Connection con) {
		if(con != null) {
			DBPool.closeReaderSilent(ctx,con); //FIXME
		}
	}

	@OXThrows(category=Category.SUBSYSTEM_OR_SERVICE_DOWN, desc="The Database does not seem to be reachable. This must be fixed by the system administration", exceptionId=1, msg="Database cannot be reached.")	
	public Connection getWriteConnection(final Context ctx) throws TransactionException {
		try {
			final Connection writeCon = DBPool.pickupWriteable(ctx);
			return writeCon;
		} catch (final DBPoolingException e) {
			throw EXCEPTIONS.create(1,e);
		}
	}

	public void releaseWriteConnection(final Context ctx, final Connection con) {
		if(con == null) {
			return;
		}
		try {
			con.setAutoCommit(true);
		} catch (final SQLException e) {
			LOG.fatal("",e);
		}
		DBPool.closeWriterSilent(ctx,con);
	}

}
