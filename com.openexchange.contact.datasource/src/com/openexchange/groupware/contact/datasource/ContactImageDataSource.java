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

package com.openexchange.groupware.contact.datasource;

import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.PictureSearchData;
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
import com.openexchange.java.Streams;
import com.openexchange.java.util.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link ContactImageDataSource} - A data source to obtains a contact's image data
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated Use the new ContactPictureService instead
 */
@Deprecated
public final class ContactImageDataSource implements ImageDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(ContactImageDataSource.class);

    private static final String[] ARGS = { "com.openexchange.groupware.contact.folder", "com.openexchange.groupware.contact.id" };

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactImageDataSource}
     */
    public ContactImageDataSource(ServiceLookup services) {
        super();
        this.services = services;
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
        PictureSearchData contactPictureRequestData = new PictureSearchData(null, null, imageLocation.getFolder(), imageLocation.getId(), null);
        return services.getServiceSafe(ContactPictureService.class).getETag(session, contactPictureRequestData);
    }

    @SuppressWarnings("unchecked")
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
            } catch (NumberFormatException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(e, ARGS[0], val);
            }
        }
        final int contactId;
        {
            final String val = dataArguments.get(ARGS[1]);
            if (val == null) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[1]);
            }
            try {
                contactId = Integer.parseInt(val);
            } catch (NumberFormatException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(e, ARGS[1], val);
            }
        }

        PictureSearchData contactPictureRequestData = new PictureSearchData(null, null, Integer.toString(folder), Integer.toString(contactId), null);
        ContactPicture picture = services.getServiceSafe(ContactPictureService.class).getPicture(session, contactPictureRequestData);
        IFileHolder fileHolder = picture.getFileHolder();
        try {
            /*
             * Return contact image
             */
            final DataProperties properties = new DataProperties(8);
            properties.put(DataProperties.PROPERTY_FOLDER_ID, Integer.toString(folder));
            properties.put(DataProperties.PROPERTY_ID, Integer.toString(contactId));

            if (fileHolder == null) {
                LOG.warn("Requested a non-existing image in contact: object-id={} folder={} context={} session-user={}. Returning an empty image as fallback.", I(contactId), I(folder), I(session.getContextId()), I(session.getUserId()));
                properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
                properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
                properties.put(DataProperties.PROPERTY_NAME, "image.jpg");
                return new SimpleData<D>((D) (newEmptyStream()), properties);
            }

            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, fileHolder.getContentType());
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            properties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
            InputStream stream = fileHolder.getStream();
            try {
                SimpleData<D> retval = new SimpleData<D>((D) stream, properties);
                stream = null;
                fileHolder = null;
                return retval;
            } finally {
                Streams.close(stream);
            }
        } finally {
            Streams.close(fileHolder);
        }
    }

    private UnsynchronizedByteArrayInputStream newEmptyStream() {
        return new UnsynchronizedByteArrayInputStream(new byte[0]);
    }

    /**
     * Gets a contact with specified fields.
     *
     * @param session The current session
     * @param imageLocation The image location containing the contact information
     * @param fields The contact fields to retrieve
     * @return The contact, or <code>null</code> if it can't be found or loaded.
     * @throws OXException If getting contact failed
     */
    private Contact optContact(Session session, ImageLocation imageLocation, ContactField... fields) throws OXException {
        return optContact(session, Tools.getUnsignedInteger(imageLocation.getId()),
            Tools.getUnsignedInteger(imageLocation.getFolder()), fields);
    }

    /**
     * Gets a contact with specified fields.
     *
     * @param session The current session
     * @param objectID The object ID of the contact to get
     * @param folderID The parent folder ID of the contact to get
     * @param fields The contact fields to retrieve
     * @return The contact, or <code>null</code> if it can't be found or loaded.
     * @throws OXException If getting contact failed
     */
    private Contact optContact(Session session, int objectID, int folderID, ContactField... fields) throws OXException {
        ContactService contactService = services.getServiceSafe(ContactService.class);
        try {
            return contactService.getContact(session, Integer.toString(folderID),Integer.toString(objectID), fields);
        } catch (OXException e) {
            if (ContactExceptionCodes.CONTACT_NOT_FOUND.equals(e) || ContactExceptionCodes.NOT_IN_FOLDER.equals(e)) {
                LOG.debug("unable to get contact", e);
                return null;
            }
            throw e;
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
