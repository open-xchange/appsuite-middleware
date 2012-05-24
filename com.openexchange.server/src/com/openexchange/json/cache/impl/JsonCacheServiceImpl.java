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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.json.cache.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.json.cache.JsonCacheService;
import com.openexchange.json.cache.JsonCaches;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link JsonCacheServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JsonCacheServiceImpl implements JsonCacheService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link JsonCacheServiceImpl}.
     */
    public JsonCacheServiceImpl(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public JSONValue get(final String id, final int userId, final int contextId) throws OXException {
        final JSONValue ret = opt(id, userId, contextId);
        if (null == ret) {
            throw AjaxExceptionCodes.JSON_ERROR.create(id);
        }
        return ret;
    }

    @Override
    public JSONValue opt(final String id, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT json FROM jsonCache WHERE cid=? AND user=? AND id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final String string = rs.getString(1);
            if ("null".equals(string)) {
                return null;
            }
            final String sJson = new String(Base64.decodeBase64(string), Charsets.UTF_8);
            if ('{' == sJson.charAt(0)) {
                return new JSONObject(sJson);
            }
            if ('[' == sJson.charAt(0)) {
                return new JSONArray(sJson);
            }
            throw AjaxExceptionCodes.JSON_ERROR.create("Not a JSON value.");
        } catch (final SQLException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    @Override
    public void delete(final String id, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, true);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM jsonCache WHERE cid=? AND user=? AND id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, id);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void set(final String id, final JSONValue jsonValue, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * Check for remove operation (DELETE)
             */
            if (null == jsonValue) {
                stmt = con.prepareStatement("DELETE FROM jsonCache WHERE cid=? AND user=? AND id=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, id);
                stmt.executeUpdate();
                return;
            }
            /*
             * Perform INSERT or UPDATE
             */
            stmt = con.prepareStatement("SELECT 1 FROM jsonCache WHERE cid=? AND user=? AND id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, id);
            rs = stmt.executeQuery();
            final boolean update = rs.next();
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            /*
             * Update or insert
             */
            final String text = new String(Base64.encodeBase64(jsonValue.toString().getBytes(Charsets.UTF_8)), Charsets.US_ASCII);
            if (update) {
                stmt = con.prepareStatement("UPDATE jsonCache SET json=? WHERE cid=? AND user=? AND id=?");
                stmt.setString(1, text);
                stmt.setInt(2, contextId);
                stmt.setInt(3, userId);
                stmt.setString(4, id);
            } else {
                stmt = con.prepareStatement("INSERT INTO jsonCache (cid,user,id,json) VALUES (?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, id);
                stmt.setString(4, text);
            }
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public boolean setIfDiffers(final String id, final JSONValue jsonValue, final int userId, final int contextId) throws OXException {
        if (null == jsonValue) {
            return false;
        }
        final Connection con = Database.get(contextId, true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * Perform INSERT or UPDATE
             */
            stmt = con.prepareStatement("SELECT json FROM jsonCache WHERE cid=? AND user=? AND id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, id);
            rs = stmt.executeQuery();
            final boolean update;
            final JSONValue prev;
            {
                if (!rs.next()) {
                    prev = null;
                    update = false;
                } else {
                    update = true;
                    final String string = rs.getString(1);
                    if ("null".equals(string)) {
                        prev = null;
                    } else {
                        final String sJson = new String(Base64.decodeBase64(string), Charsets.UTF_8);
                        if ('{' == sJson.charAt(0)) {
                            prev = new JSONObject(sJson);
                        } else if ('[' == sJson.charAt(0)) {
                            prev = new JSONArray(sJson);
                        } else {
                            throw AjaxExceptionCodes.JSON_ERROR.create("Not a JSON value.");
                        }
                    }
                }
            }
            DBUtils.closeSQLStuff(rs, stmt);
            stmt = null;
            rs = null;
            /*
             * Update if differ
             */
            if (JsonCaches.areEqual(prev, jsonValue)) {
                return false;
            }
            /*
             * Update or insert
             */
            final String text = new String(Base64.encodeBase64(jsonValue.toString().getBytes(Charsets.UTF_8)), Charsets.US_ASCII);
            if (update) {
                stmt = con.prepareStatement("UPDATE jsonCache SET json=? WHERE cid=? AND user=? AND id=?");
                stmt.setString(1, text);
                stmt.setInt(2, contextId);
                stmt.setInt(3, userId);
                stmt.setString(4, id);
            } else {
                stmt = con.prepareStatement("INSERT INTO jsonCache (cid,user,id,json) VALUES (?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, id);
                stmt.setString(4, text);
            }
            stmt.executeUpdate();
            return true;
        } catch (final SQLException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public boolean lock(final String id, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean transactional = false;
        try {
            DBUtils.startTransaction(con);
            transactional = true;
            /*
             * Perform INSERT or UPDATE
             */
            stmt = con.prepareStatement("SELECT 1 FROM jsonCache WHERE cid=? AND user=? AND id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, id);
            rs = stmt.executeQuery();
            final boolean update = rs.next();
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            if (!update) {
                stmt = con.prepareStatement("INSERT INTO jsonCache (cid,user,id,json,inProgress) VALUES (?,?,?,?,1)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, id);
                stmt.setString(4, "null"); // Dummy
                boolean inserted;
                try {
                    inserted = stmt.executeUpdate() > 0;
                } catch (final Exception e) {
                    inserted = false;
                }
                if (inserted) {
                    return true;
                }
            }
            DBUtils.closeSQLStuff(stmt);
            stmt = con.prepareStatement("UPDATE jsonCache SET inProgress=1 WHERE cid=? AND user=? AND id=? AND inProgress=0");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, id);
            final boolean updated = (stmt.executeUpdate() > 0);
            con.commit();
            return updated;
        } catch (final SQLException e) {
            if (transactional) {
                DBUtils.rollback(con);
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            if (transactional) {
                DBUtils.rollback(con);
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (transactional) {
                DBUtils.autocommit(con);
            }
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void unlock(final String id, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, true);
        PreparedStatement stmt = null;
        boolean transactional = false;
        try {
            DBUtils.startTransaction(con);
            transactional = true;
            stmt = con.prepareStatement("UPDATE jsonCache SET inProgress=0 WHERE cid=? AND user=? AND id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, id);
            stmt.executeUpdate();
            con.commit();
        } catch (final SQLException e) {
            if (transactional) {
                DBUtils.rollback(con);
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            if (transactional) {
                DBUtils.rollback(con);
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (transactional) {
                DBUtils.autocommit(con);
            }
            Database.back(contextId, true, con);
        }
    }

}
