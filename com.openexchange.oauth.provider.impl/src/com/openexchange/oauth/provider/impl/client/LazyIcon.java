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

package com.openexchange.oauth.provider.impl.client;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.oauth.provider.authorizationserver.client.DefaultIcon;
import com.openexchange.oauth.provider.authorizationserver.client.Icon;
import com.openexchange.oauth.provider.impl.osgi.Services;

/**
 * {@link LazyIcon}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class LazyIcon implements Icon {

    private static final long serialVersionUID = 4458877977630523049L;

    private final String groupId;
    private final String clientId;
    private volatile Icon delegate;

    /**
     * Initializes a new {@link LazyIcon}.
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier. Only pass already validated IDs here!
     * @throws OXException
     */
    public LazyIcon(String groupId, String clientId) {
        super();
        this.groupId = groupId;
        this.clientId = clientId;
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
    public byte[] getData() {
        return getDelegate().getData();
    }

    private Icon getDelegate() {
        Icon delegate = this.delegate;
        if (delegate == null) {
            synchronized (this) {
                delegate = this.delegate;
                if (delegate == null) {
                    DatabaseService dbService = null;
                    Connection con = null;
                    PreparedStatement stmt = null;
                    ResultSet rs = null;
                    try {
                        dbService = Services.requireService(DatabaseService.class);
                        con = dbService.getReadOnlyForGlobal(this.groupId);
                        stmt = con.prepareStatement("SELECT icon, icon_mime_type FROM oauth_client WHERE id = ? AND gid = ?");
                        stmt.setString(1, this.clientId);
                        stmt.setString(2, this.groupId);
                        rs = stmt.executeQuery();
                        if (!rs.next()) {
                            throw new IllegalStateException("The client has been removed in the meantime");
                        }

                        Blob blob = rs.getBlob(1);
                        DefaultIcon defaultIcon = new DefaultIcon();
                        defaultIcon.setMimeType(rs.getString(2));
                        defaultIcon.setData(Streams.stream2bytes(blob.getBinaryStream()));
                        delegate = defaultIcon;
                        this.delegate = delegate;
                    } catch (OXException | SQLException | IOException e) {
                        throw new IllegalStateException("Error while loading the client's icon", e);
                    } finally {
                        Databases.closeSQLStuff(rs, stmt);
                        if (con != null && null != dbService) {
                            dbService.backReadOnlyForGlobal(this.groupId, con);
                        }
                    }
                }
            } // End of synchronized block
        }

        return delegate;
    }

    /**
     * The writeObject method is responsible for writing the state of the object for its particular class so that the corresponding
     * readObject method can restore it. The default mechanism for saving the Object's fields can be invoked by calling
     * <code>ObjectOutputStream.defaultWriteObject()</code>. The method does not need to concern itself with the state belonging to its
     * super classes or subclasses. State is saved by writing the individual fields to the ObjectOutputStream using the writeObject method
     * or by using the methods for primitive data types supported by <code>DataOutput</code> .
     *
     * @param out The object output stream
     * @throws IOException If an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(getDelegate());
    }

    /**
     * The readObject method is responsible for reading from the stream and restoring the classes fields. It may call in.defaultReadObject
     * to invoke the default mechanism for restoring the object's non-static and non-transient fields. The
     * <code>ObjectInputStream.defaultReadObject</code> method uses information in the stream to assign the fields of the object saved in
     * the stream with the correspondingly named fields in the current object. This handles the case when the class has evolved to add new
     * fields. The method does not need to concern itself with the state belonging to its super classes or subclasses. State is saved by
     * writing the individual fields to the ObjectOutputStream using the writeObject method or by using the methods for primitive data types
     * supported by <code>DataOutput</code>.
     *
     * @param in The object input stream
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a casting fails
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.delegate = (Icon) in.readObject();
    }

}
