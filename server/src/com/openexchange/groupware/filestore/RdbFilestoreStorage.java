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

package com.openexchange.groupware.filestore;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;

@OXExceptionSource(
    classId = Classes.RDB_FILESTORE_STORAGE,
    component = EnumComponent.FILESTORE
)
public class RdbFilestoreStorage extends FilestoreStorage {

    /**
     * For creating exceptions.
     */
    private static final FilestoreExceptionFactory EXCEPTION =
        new FilestoreExceptionFactory(RdbFilestoreStorage.class);

	private static final String SELECT = "SELECT uri, size, max_context FROM filestore WHERE id = ?";
	
	private static final Log LOG = LogFactory.getLog(RdbFilestoreStorage.class);
	
	@Override
    @OXThrowsMultiple(
        category = { Category.SETUP_ERROR, Category.SETUP_ERROR, Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.CODE_ERROR },
        desc = { "", "" },
        exceptionId = { 3, 4, 5, 6 },
        msg = { "Cannot find filestore with id %1$d.",
            "Cannot create URI from \"%1$s\".",
            "Can't access DBPool",
            "Got SQL Exception"}
    )
	public Filestore getFilestore(final int id) throws FilestoreException {
		
		
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			readCon = DBPool.pickup();
			
			stmt = readCon.prepareStatement(SELECT);
			stmt.setInt(1,id);
			
			rs = stmt.executeQuery();
			
			if(!rs.next()) {
				throw EXCEPTION.create(3, Integer.valueOf(id));
			}
			
			final FilestoreImpl filestore = new FilestoreImpl();
			filestore.setId(id);
            String tmp = null;
			try {
                tmp = rs.getString("uri");
				filestore.setUri(new URI(tmp));
			} catch (final URISyntaxException e) {
				throw EXCEPTION.create(4, e, tmp);
			}
			filestore.setSize(rs.getLong("size"));
			filestore.setMaxContext(rs.getLong("max_context"));
			return filestore;
		} catch (final DBPoolingException e) {
			throw EXCEPTION.create(5,e);
		} catch (final SQLException e) {
			throw EXCEPTION.create(6,e);
		} finally {
			
			if(stmt!=null) {
				try {
					stmt.close();
				} catch (final SQLException e1) {
					LOG.error("",e1);
				}
			}
			if(rs!=null) {
				try {
					rs.close();
				} catch (final SQLException e) {
					LOG.error("",e);
				}
			}
			if(readCon!=null){
				DBPool.closeReaderSilent(readCon);
			}
		}
		
	}

}
