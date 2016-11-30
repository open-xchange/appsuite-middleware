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

package com.openexchange.groupware.contact.datasource;

import java.io.InputStream;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.contact.ContactService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.java.util.Tools;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link ContactImageDataSource} - A data source to obtains a contact's image data
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactImageDataSource implements ImageDataSource {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(ContactImageDataSource.class);

    private static final ContactImageDataSource INSTANCE = new ContactImageDataSource();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ContactImageDataSource getInstance() {
        return INSTANCE;
    }

    private static final String[] ARGS = { "com.openexchange.groupware.contact.folder", "com.openexchange.groupware.contact.id" };

    /**
     * Initializes a new {@link ContactImageDataSource}
     */
    private ContactImageDataSource() {
        super();
    }

    @Override
    public String generateUrl(final ImageLocation imageLocation, final Session session) throws OXException {
        final StringBuilder sb = new StringBuilder(64);
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        if (null == imageLocation.getTimestamp()) {
            final Contact contact = optContact(session, imageLocation, ContactField.LAST_MODIFIED);
            if (null != contact) {
                sb.append('&').append("timestamp=").append(contact.getLastModified().getTime());
            }
        }
        return sb.toString();
    }

    @Override
    public DataArguments generateDataArgumentsFrom(final ImageLocation imageLocation) {
        final DataArguments dataArguments = new DataArguments(2);
        dataArguments.put(ARGS[0], imageLocation.getFolder());
        dataArguments.put(ARGS[1], imageLocation.getId());
        return dataArguments;
    }

    @Override
    public long getExpires() {
        return -1L;
    }

    @Override
    public ImageLocation parseUrl(final String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

    @Override
    public String getETag(final ImageLocation imageLocation, final Session session) throws OXException {
        final char delim = '#';
        final StringBuilder builder = new StringBuilder(128);
        builder.append(delim).append(imageLocation.getFolder());
        // builder.append(delim).append(imageLocation.getId());
        // builder.append(delim).append(session.getUserId());
        // builder.append(delim).append(session.getContextId());
        if (null == imageLocation.getTimestamp()) {
            final Contact contact = optContact(session, imageLocation, ContactField.LAST_MODIFIED);
            if (null != contact) {
                builder.append(delim).append(contact.getLastModified().getTime());
            }
        } else {
            builder.append(delim).append(imageLocation.getTimestamp());
        }
        builder.append(delim);
        return ImageUtility.getMD5(builder.toString(), "hex");
    }

    @Override
    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws OXException {
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
        final Contact contact = optContact(session, objectId, folder, ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE);
        if (null == contact) {
            throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(Integer.valueOf(objectId), Integer.valueOf(session.getContextId()));
        }
        /*
         * Return contact image
         */
        final byte[] imageBytes = contact.getImage1();
        final DataProperties properties = new DataProperties(8);
        properties.put(DataProperties.PROPERTY_FOLDER_ID, Integer.toString(folder));
        properties.put(DataProperties.PROPERTY_ID, Integer.toString(objectId));

        if (imageBytes == null) {
            LOG.warn("Requested a non-existing image in contact: object-id={} folder={} context={} session-user={}. Returning an empty image as fallback.", objectId, folder, session.getContextId(), session.getUserId());
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }

        if (com.openexchange.ajax.helper.ImageUtils.isSvg(imageBytes)) {
            LOG.warn("Detected a possibly harmful SVG image in contact: object-id={} folder={} context={} session-user={}. Returning an empty image as fallback.", objectId, folder, session.getContextId(), session.getUserId());
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }

        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contact.getImageContentType());
        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(imageBytes.length));
        properties.put(DataProperties.PROPERTY_NAME, contact.getImageContentType().replace('/', '.'));
        return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(imageBytes)), properties);
    }

    /**
     * Gets a contact with specified fields.
     *
     * @param session The current session
     * @param imageLocation The image location containing the contact information
     * @param fields The contact fields to retrieve
     * @return The contact, or <code>null</code> if it can't be found or loaded.
     * @throws OXException
     */
    private static Contact optContact(Session session, ImageLocation imageLocation, ContactField...fields) throws OXException {
        return optContact(session, Tools.getUnsignedInteger(imageLocation.getId()),
            Tools.getUnsignedInteger(imageLocation.getFolder()), fields);
    }

    /**
     * Gets a contact with specified fields.
     *
     * @param objectId The object ID of the contact to get
     * @param folder The parent folder ID of the contact to get
     * @param session The current session
     * @param fields The contact fields to retrieve
     * @return The contact, or <code>null</code> if it can't be found or loaded.
     * @throws OXException
     */
    private static Contact optContact(Session session, int objectID, int folderID, ContactField...fields) throws OXException {
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class, false);
        try {
            return contactService.getContact(session, Integer.toString(folderID),Integer.toString(objectID), fields);
        } catch (OXException e) {
            if (ContactExceptionCodes.CONTACT_NOT_FOUND.equals(e) || ContactExceptionCodes.NOT_IN_FOLDER.equals(e)) {
                LOG.debug("unable to get contact", e);
                return null;
            } else {
                throw e;
            }
        }
    }

    /**
     * <ul>
     * <li><code>&quot;com.openexchange.groupware.contact.folder&quot;</code></li>
     * <li><code>&quot;com.openexchange.groupware.contact.id&quot;</code></li>
     * </ul>
     */
    @Override
    public String[] getRequiredArguments() {
        final String[] args = new String[ARGS.length];
        System.arraycopy(ARGS, 0, args, 0, ARGS.length);
        return args;
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    private static final String REGISTRATION_NAME = "com.openexchange.contact.image";

    @Override
    public String getRegistrationName() {
        return REGISTRATION_NAME;
    }

    private static final String ALIAS = "/contact/picture";

    @Override
    public String getAlias() {
        return ALIAS;
    }

}
