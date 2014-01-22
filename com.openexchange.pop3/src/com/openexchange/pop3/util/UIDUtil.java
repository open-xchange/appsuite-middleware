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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.pop3.util;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.session.Session;
import com.planetj.math.rabinhash.RabinHashFunction64;

/**
 * {@link UIDUtil} - UID utility class for POP3 bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UIDUtil {

    /**
     * Initializes a new {@link UIDUtil}.
     */
    private UIDUtil() {
        super();
    }

    /**
     * Computes the Rabin hash value of specified UID string.
     *
     * @param uidl The UID string as received from POP3 UIDL command
     * @return The Rabin hash value
     */
    public static long uid2long(final String uidl) {
        return RabinHashFunction64.DEFAULT_HASH_FUNCTION.hash(uidl);
    }

    /**
     * Computes the Rabin hash value of specified UID strings.
     *
     * @param uidls The UID strings as received from POP3 UIDL command
     * @return The Rabin hash values
     */
    public static long[] uid2long(final String[] uidls) {
        if (null == uidls) {
            return null;
        }
        final long[] longs = new long[uidls.length];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = uid2long(uidls[i]);
        }
        return longs;
    }

    private static final String SELECT_UID = "SELECT uidl FROM user_pop3_data WHERE cid = ? AND user = ? AND uid = ?";

    /**
     * Gets the UIDL for specified UID value.
     *
     * @param uid The UID
     * @param session The session
     * @return The UIDL for specified UID value
     * @throws OXException If UIDL look-up fails
     */
    public static String long2uid(final long uid, final Session session) throws OXException {
        final int cid = session.getContextId();
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_UID);
            stmt.setLong(1, cid);
            stmt.setLong(2, session.getUserId());
            stmt.setLong(3, uid);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(uid), "INBOX");
            }
            return rs.getString(1);
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    /**
     * Gets the UIDLs for specified UID values.
     *
     * @param uids The UIDs
     * @param session The session
     * @return The UIDLs for specified UID values
     * @throws OXException If UIDL look-up fails
     */
    public static String[] longs2uids(final long[] uids, final Session session) throws OXException {
        if (null == uids) {
            return null;
        }
        final String[] retval = new String[uids.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = long2uid(uids[i], session);
        }
        return retval;
    }

}
