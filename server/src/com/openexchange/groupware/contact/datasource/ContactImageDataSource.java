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

package com.openexchange.groupware.contact.datasource;

import java.io.InputStream;
import com.openexchange.api2.OXException;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataException;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link ContactImageDataSource} - A data source to obtains a contact's image data
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactImageDataSource implements DataSource {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ContactImageDataSource.class);

    private static final String[] ARGS = { "com.openexchange.groupware.contact.folder", "com.openexchange.groupware.contact.id" };

    /**
     * Initializes a new {@link ContactImageDataSource}
     */
    public ContactImageDataSource() {
        super();
    }

    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws DataException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }
        /*
         * Get arguments
         */
        final int folder;
        {
            final String val = dataArguments.get(ARGS[0]);
            if (val == null) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[0]);
            }
            try {
                folder = Integer.parseInt(val);
            } catch (final NumberFormatException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(e, ARGS[0], val);
            }
        }
        final int objectId;
        {
            final String val = dataArguments.get(ARGS[1]);
            if (val == null) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[1]);
            }
            try {
                objectId = Integer.parseInt(val);
            } catch (final NumberFormatException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(e, ARGS[1], val);
            }
        }
        /*
         * Get contact
         */
        final Contact contact;
        try {
            final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).newContactInterface(
                folder,
                session);
            contact = contactInterface.getObjectById(objectId, folder);
        } catch (final OXException e) {
            throw new DataException(e);
        }
        /*
         * Return contact image
         */
        final byte[] imageBytes = contact.getImage1();
        final DataProperties properties = new DataProperties();
        if (imageBytes == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(new StringBuilder("Requested a non-existing image in contact: object-id=").append(objectId).append(" folder=").append(
                    folder).append(" context=").append(session.getContextId()).append(" session-user=").append(session.getUserId()).append(
                    "\nReturning an empty image as fallback.").toString());
            }
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }
        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contact.getImageContentType());
        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(imageBytes.length));
        return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(imageBytes)), properties);
    }

    /**
     * <ul>
     * <li><code>&quot;com.openexchange.groupware.contact.folder&quot;</code></li>
     * <li><code>&quot;com.openexchange.groupware.contact.id&quot;</code></li>
     * </ul>
     */
    public String[] getRequiredArguments() {
        final String[] args = new String[ARGS.length];
        System.arraycopy(ARGS, 0, args, 0, ARGS.length);
        return args;
    }

    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

}
