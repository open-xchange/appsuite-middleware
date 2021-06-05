/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
