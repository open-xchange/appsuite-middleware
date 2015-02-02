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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal.client;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.google.common.io.ByteStreams;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.DefaultIcon;
import com.openexchange.oauth.provider.Icon;


/**
 * {@link LazyIcon}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class LazyIcon implements Icon {

    private final Lock lock = new ReentrantLock();

    private final String clientId;

    private final DatabaseService dbService;

    private Icon delegate;

    /**
     * Initializes a new {@link LazyIcon}.
     */
    public LazyIcon(String clientId, DatabaseService dbService) {
        super();
        this.clientId = clientId;
        this.dbService = dbService;
    }

    @Override
    public String getMimeType() {
        return getDelegate().getMimeType();
    }

    @Override
    public int getSize() {
        return getDelegate().getSize();
    }

    @Override
    public InputStream getInputStream() {
        return getDelegate().getInputStream();
    }

    private Icon getDelegate() {
        if (delegate == null) {
            lock.lock();
            try {
                if (delegate == null) {
                    Connection con = null;
                    PreparedStatement stmt = null;
                    ResultSet rs = null;
                    try {
                        con = dbService.getReadOnly();
                        stmt = con.prepareStatement("SELECT icon, icon_mime_type FROM oauth_client WHERE id = ?");
                        stmt.setString(1, clientId);
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                            Blob blob = rs.getBlob(1);
                            String mimeType = rs.getString(2);
                            DefaultIcon defaultIcon = new DefaultIcon();
                            defaultIcon.setSize((int) blob.length());
                            defaultIcon.setMimeType(mimeType);
                            defaultIcon.setData(ByteStreams.toByteArray(blob.getBinaryStream()));
                            delegate = defaultIcon;
                        } else {
                            throw new IllegalStateException("The client has been removed in the meantime");
                        }
                    } catch (OXException | SQLException | IOException e) {
                        throw new IllegalStateException("Error while loading the clients icon", e);
                    } finally {
                        Databases.closeSQLStuff(rs, stmt);
                        if (con != null) {
                            dbService.backReadOnly(con);
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        return delegate;
    }

}
