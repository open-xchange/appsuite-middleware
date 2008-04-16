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

package com.openexchange.groupware.infostore.webdav;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.infostore.Classes;

import static com.openexchange.tools.sql.DBUtils.getStatement;

@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_WEBDAV_PROPERTYSTOREIMPL, 
		component=EnumComponent.INFOSTORE
)
public class PropertyStoreImpl extends DBService implements PropertyStore {
	
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(PropertyStoreImpl.class);
	
	private String INSERT = "INSERT INTO %%tablename%% (cid, id, name, namespace, value, language, xml) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private String tablename;
	
	public PropertyStoreImpl(String tablename){
		this(null,tablename);
	}
	
	public PropertyStoreImpl(DBProvider provider, String tablename) {
		setProvider(provider);
		initTable(tablename);
	}
	
	private void initTable(String tablename) {
		INSERT = INSERT.replaceAll("%%tablename%%",tablename);
		this.tablename = tablename;
	}
	
	
	@OXThrows(
			category=Category.CODE_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=0,
			msg="Invalid SQL: '%s'"
	)
	public Map<Integer, List<WebdavProperty>> loadProperties(List<Integer> entities, List<WebdavProperty> properties, Context ctx) throws OXException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuilder builder = null;
		try {
			builder = new StringBuilder("SELECT id, name, namespace, value, language, xml FROM ");
			builder.append(tablename);
			builder.append(" WHERE CID = ? AND id IN (");
			join(entities, builder);
			builder.append(") AND (");
			addOr(builder,properties);
			builder.append(')');
			
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(builder.toString());
			stmt.setInt(1, ctx.getContextId());
			addOr(stmt, properties, 1);
			
			rs = stmt.executeQuery();
			Map<Integer, List<WebdavProperty>> retVal = new HashMap<Integer, List<WebdavProperty>>();
			while(rs.next()) {
				List<WebdavProperty> props = retVal.get(rs.getInt(1));
				if(props == null) {
					props = new ArrayList<WebdavProperty>();
					retVal.put(rs.getInt(1), props);
				}
				props.add(getProperty(rs));
			}
		
			return retVal;
		} catch (SQLException e) {
			throw EXCEPTIONS.create(0,e,builder.toString());
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
	}

	private WebdavProperty getProperty(ResultSet rs) throws SQLException {
		WebdavProperty prop = new WebdavProperty();
		prop.setName(rs.getString("name"));
		prop.setNamespace(rs.getString("namespace"));
		prop.setLanguage(rs.getString("language"));
		prop.setXML(rs.getBoolean("xml"));
		prop.setValue(rs.getString("value"));
		return prop;
	}
	
	@OXThrows(
			category=Category.CODE_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=1,
			msg="Invalid SQL: '%s'"
	)
	public List<WebdavProperty> loadProperties(int entity, List<WebdavProperty> properties, Context ctx) throws OXException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuilder builder = null;
		try {
			builder = new StringBuilder("SELECT id, name, namespace, value, language, xml FROM ");
			builder.append(tablename);
			builder.append(" WHERE CID = ? AND id = ");
			builder.append(entity);
			builder.append(" AND (");
			addOr(builder,properties);
			builder.append(')');
			
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(builder.toString());
			stmt.setInt(1, ctx.getContextId());
			addOr(stmt, properties, 1);
			
			rs = stmt.executeQuery();
			List<WebdavProperty> props = new ArrayList<WebdavProperty>();
			while(rs.next()) {
				props.add(getProperty(rs));
			}
		
			return props;
		} catch (SQLException e) {
			throw EXCEPTIONS.create(1,e,builder.toString());
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
	}
	
	@OXThrows(
			category=Category.CODE_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=2,
			msg="Invalid SQL: '%s'"
	)
	public void saveProperties(int entity, List<WebdavProperty> properties, Context ctx) throws OXException {
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			writeCon = getWriteConnection(ctx);
			removeProperties(entity, properties, ctx, writeCon);
			stmt = writeCon.prepareStatement(INSERT);
			stmt.setInt(1,ctx.getContextId());
			stmt.setInt(2, entity);
			for(WebdavProperty prop : properties) {
				setValues(stmt, prop);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw EXCEPTIONS.create(2,e,getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, writeCon);
		}
	}

	private final void setValues(PreparedStatement stmt, WebdavProperty prop) throws SQLException {
		stmt.setString(3, prop.getName());
		stmt.setString(4, prop.getNamespace());
		stmt.setString(5, prop.getValue());
		stmt.setString(6, prop.getLanguage());
		stmt.setBoolean(7, prop.isXML());
	}
	
	@OXThrows(
			category=Category.CODE_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=3,
			msg="Invalid SQL: '%s'"
	)
	public List<WebdavProperty> loadAllProperties(int entity, Context ctx) throws OXException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuilder builder = new StringBuilder("SELECT id, name, namespace, value, language, xml FROM ");
			builder.append(tablename);
			builder.append(" WHERE CID = ? AND id = ");
			builder.append(entity);
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(builder.toString());
			stmt.setInt(1, ctx.getContextId());
			
			rs = stmt.executeQuery();
			List<WebdavProperty> props = new ArrayList<WebdavProperty>();
			while(rs.next()) {
				props.add(getProperty(rs));
			}
		
			return props;
		} catch (SQLException e) {
			throw EXCEPTIONS.create(3,e,getStatement(stmt));
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
	}
	
	@OXThrows(
			category=Category.CODE_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=4,
			msg="Invalid SQL: '%s'"
	)
	public Map<Integer, List<WebdavProperty>> loadAllProperties(List<Integer> entities, Context ctx) throws OXException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuilder builder = new StringBuilder("SELECT id, name, namespace, value, language, xml FROM ");
			builder.append(tablename);
			builder.append(" WHERE CID = ? AND id IN (");
			join(entities, builder);
			builder.append(')');
			
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(builder.toString());
			stmt.setInt(1, ctx.getContextId());
			
			rs = stmt.executeQuery();
			Map<Integer, List<WebdavProperty>> retVal = new HashMap<Integer, List<WebdavProperty>>();
			while(rs.next()) {
				List<WebdavProperty> props = retVal.get(rs.getInt(1));
				if(props == null) {
					props = new ArrayList<WebdavProperty>();
					retVal.put(rs.getInt(1), props);
				}
				props.add(getProperty(rs));
			}
		
			return retVal;
		} catch (SQLException e) {
			throw EXCEPTIONS.create(4, e, getStatement(stmt));
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
	}
	
	@OXThrows(
			category=Category.CODE_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=5,
			msg="Invalid SQL: '%s'"
	)
	public void removeAll(List<Integer> entities, Context ctx) throws OXException {
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			StringBuilder b = new StringBuilder("DELETE FROM ");
			b.append(tablename);
			b.append(" WHERE cid = ");
			b.append(ctx.getContextId());
			b.append(" AND id IN (");
			join(entities,b);
			b.append(')');
			writeCon = getWriteConnection(ctx);
			stmt = writeCon.prepareStatement(b.toString());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw EXCEPTIONS.create(5, e, getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, writeCon);
		}
	}
	
	private final void join(List<Integer> entities, StringBuilder b) {
		for(int entity : entities) {
			b.append(entity);
			b.append(',');
		}
		b.setLength(b.length()-1);
	}

	@OXThrows(
			category=Category.CODE_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=6,
			msg="Invalid SQL: '%s'"
	)
	public void removeProperties(int entity, List<WebdavProperty> properties, Context ctx) throws OXException {
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			writeCon = getWriteConnection(ctx);
			removeProperties(entity, properties, ctx, writeCon);
		} catch (SQLException e) {
			throw EXCEPTIONS.create(6,e,getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, writeCon);
		}
	}
	
	private void removeProperties(int entity, List<WebdavProperty> properties, Context ctx, Connection writeCon) throws SQLException {
		if(properties.isEmpty())
			return;
		PreparedStatement stmt = null;
		StringBuilder builder = new StringBuilder("DELETE FROM ");
		builder.append(tablename);
		builder.append(" WHERE cid = ? AND id = ? AND (");
		addOr(builder,properties);
		builder.append(')');
		try {
			stmt = writeCon.prepareStatement(builder.toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, entity);
			addOr(stmt, properties, 2);
			stmt.executeUpdate();
		} finally {
			close(stmt, null);
		}
		
	}

	private final void addOr(PreparedStatement stmt, List<WebdavProperty> properties, int count) throws SQLException {
		for(WebdavProperty property : properties) {
			stmt.setString(++count, property.getName());
			stmt.setString(++count, property.getNamespace());
		}
	}

	private final void addOr(StringBuilder builder, List<WebdavProperty> properties) {
		String append = "(name = ? AND namespace = ?) OR ";
		int size = properties.size();
		for(int i = 0; i < size; i++) {
			builder.append(append);
		}
		builder.setLength(builder.length()-3);
	}

	public void removeAll(int entity, Context ctx) throws OXException {
		removeAll(Arrays.asList(entity), ctx);
	}

}
