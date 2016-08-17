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

package com.openexchange.json.cache.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.UnsynchronizedPushbackReader;
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
            final UnsynchronizedPushbackReader reader = new UnsynchronizedPushbackReader(rs.getNCharacterStream(1));
            final int read = reader.read();
            // Check for possible JSON
            if (read < 0) {
                return null;
            }
            final char c = (char) read;
            reader.unread(c);
            if ('[' == c || '{' == c) {
                // Either starting JSON object or JSON array
                return JSONObject.parse(reader);
            }
            final String s = AJAXServlet.readFrom(reader);
            if ("null".equals(s)) {
                return null;
            }
            throw AjaxExceptionCodes.JSON_ERROR.create("Not a JSON value.");
        } catch (final SQLException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
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
    public void set(final String id, final JSONValue jsonValue, final long duration, final int userId, final int contextId) throws OXException {
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
            final long now = System.currentTimeMillis();
            //final String asciiOnly = toJavaNotation(jsonValue.toString());
            if (update) {
                stmt = con.prepareStatement("UPDATE jsonCache SET json=?, size=?, lastUpdate=?, took=? WHERE cid=? AND user=? AND id=?");
                stmt.setNCharacterStream(1, new AsciiReader(new JSONInputStream(jsonValue, "US-ASCII")));
                stmt.setLong(2, jsonValue.length());
                stmt.setLong(3, now);
                if (duration < 0) {
                    stmt.setNull(4, Types.BIGINT);
                } else {
                    stmt.setLong(4, duration);
                }
                stmt.setInt(5, contextId);
                stmt.setInt(6, userId);
                stmt.setString(7, id);
            } else {
                stmt = con.prepareStatement("INSERT INTO jsonCache (cid,user,id,json,size,lastUpdate,took) VALUES (?,?,?,?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, id);
                stmt.setNCharacterStream(4, new AsciiReader(new JSONInputStream(jsonValue, "US-ASCII")));
                stmt.setLong(5, jsonValue.length());
                stmt.setLong(6, now);
                if (duration < 0) {
                    stmt.setNull(7, Types.BIGINT);
                } else {
                    stmt.setLong(7, duration);
                }
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
    public boolean setIfDifferent(final String id, final JSONValue jsonValue, final long duration, final int userId, final int contextId) throws OXException {
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
            JSONValue prev = null;
            {
                if (rs.next()) {
                    update = true;
                    final UnsynchronizedPushbackReader reader = new UnsynchronizedPushbackReader(rs.getNCharacterStream(1));
                    if (!rs.wasNull()) { // Not NULL
                        final int read = reader.read();
                        // Check for possible JSON
                        if (read >= 0) {
                            try {
                                final char c = (char) read;
                                reader.unread(c);
                                if ('[' == c || '{' == c) {
                                    // Either starting JSON object or JSON array
                                    prev = JSONObject.parse(reader);
                                } else {
                                    final String s = AJAXServlet.readFrom(reader);
                                    if ("null".equals(s)) {
                                        prev = null;
                                    }
                                    throw AjaxExceptionCodes.JSON_ERROR.create("Not a JSON value.");
                                }
                            } catch (final JSONException e) {
                                // Read invalid JSON data
                                prev = null;
                            }
                        }
                    }
                } else {
                    prev = null;
                    update = false;
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
            // final String asciiOnly = toJavaNotation(jsonValue.toString());
            final long now = System.currentTimeMillis();
            if (update) {
                stmt = con.prepareStatement("UPDATE jsonCache SET json=?, size=?, lastUpdate=?, took=? WHERE cid=? AND user=? AND id=?");
                stmt.setNCharacterStream(1, new AsciiReader(new JSONInputStream(jsonValue, "US-ASCII")));
                stmt.setLong(2, jsonValue.length());
                stmt.setLong(3, now);
                if (duration < 0) {
                    stmt.setNull(4, Types.BIGINT);
                } else {
                    stmt.setLong(4, duration);
                }
                stmt.setInt(5, contextId);
                stmt.setInt(6, userId);
                stmt.setString(7, id);
            } else {
                stmt = con.prepareStatement("INSERT INTO jsonCache (cid,user,id,json,size,lastUpdate,took) VALUES (?,?,?,?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, id);
                stmt.setNCharacterStream(4, new AsciiReader(new JSONInputStream(jsonValue, "US-ASCII")));
                stmt.setLong(5, jsonValue.length());
                stmt.setLong(6, now);
                if (duration < 0) {
                    stmt.setNull(7, Types.BIGINT);
                } else {
                    stmt.setLong(7, duration);
                }
            }
            stmt.executeUpdate();
            return true;
        } catch (final SQLException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
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
            final long now = System.currentTimeMillis();
            if (!update) {
                stmt = con.prepareStatement("INSERT INTO jsonCache (cid,user,id,json,size,inProgress,inProgressSince,lastUpdate) VALUES (?,?,?,?,?,1,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, id);
                stmt.setString(4, "null"); // Dummy
                stmt.setLong(5, 0L);
                stmt.setLong(6, now);
                stmt.setLong(7, now);
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
            stmt = con.prepareStatement("UPDATE jsonCache SET inProgress=1, inProgressSince=? WHERE cid=? AND user=? AND id=? AND inProgress=0");
            stmt.setLong(1, now);
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            stmt.setString(4, id);
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

    private static String toJavaNotation(final String unicode) {
        final int length = unicode.length();
        final StringBuilder sb = new StringBuilder(length << 1);
        for (int i = 0; i < length; ++i) {
            final char a = unicode.charAt(i);
            if (a > 127) {
                final String hexString = Integer.toHexString(a);
                sb.append("\\u");
                if (2 == hexString.length()) {
                    sb.append("00");
                }
                sb.append(hexString);
            } else {
                sb.append(a);
            }
        }
        return sb.toString();
    }

    private static String abbreviate(final String str, final int offset, final int maxWidth) {
        if (str == null) {
            return null;
        }
        final int length = str.length();
        if (length <= maxWidth) {
            return str;
        }
        int off = offset;
        if (off > length) {
            off = length;
        }
        if ((length - off) < (maxWidth - 3)) {
            off = length - (maxWidth - 3);
        }
        if (off <= 4) {
            return str.substring(0, maxWidth - 3) + "...";
        }
        if ((off + (maxWidth - 3)) < length) {
            return "..." + abbreviate(str.substring(off), 0, maxWidth - 3);
        }
        return "..." + str.substring(length - (maxWidth - 3));
    }

}
