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
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.java.Streams;
import com.openexchange.java.util.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link UserImageDataSource} - A data source to obtains a user's image data
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @deprecated Use the new ContactPictureService instead
 */
@Deprecated
public final class UserImageDataSource implements ImageDataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserImageDataSource.class);

    private static final String REGISTRATION_NAME = "com.openexchange.user.image";
    private static final String ALIAS = "/user/picture";
    private static final String ID_ARGUMENT = "com.openexchange.groupware.user.id";

    private final ServiceLookup services;

    /**
     * Initializes a new {@link UserImageDataSource}.
     *
     * @param services The {@link ServiceLookup}
     */
    public UserImageDataSource(ServiceLookup services) {
        this.services = services;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        if (false == InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }
        /*
         * Extract user ID
         */
        String argument = dataArguments.get(ID_ARGUMENT);
        if (null == argument) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(ID_ARGUMENT);
        }
        int userID;
        try {
            userID = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw DataExceptionCodes.INVALID_ARGUMENT.create(e, ID_ARGUMENT, argument);
        }

        PictureSearchData contactPictureRequestData = new PictureSearchData(I(userID), null, null, null, null);
        ContactPicture picture = services.getServiceSafe(ContactPictureService.class).getPicture(session, contactPictureRequestData);
        IFileHolder fileHolder = picture.getFileHolder();
        try {
            /*
             * Return contact image
             */
            final DataProperties properties = new DataProperties(8);
            properties.put(DataProperties.PROPERTY_FOLDER_ID, String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID));
            properties.put(DataProperties.PROPERTY_ID, String.valueOf(userID));

            if (fileHolder == null) {
                LOG.warn("Requested a non-existing image in user contact: user-id={} context={} session-user={}. Returning an empty image as fallback.", I(userID), I(session.getContextId()), I(session.getUserId()));
                properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
                properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
                properties.put(DataProperties.PROPERTY_NAME, "image.jpg");
                return new SimpleData<D>((D) newEmptyStream(), properties);
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

    @Override
    public String[] getRequiredArguments() {
        return new String[] { ID_ARGUMENT };
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    @Override
    public String getRegistrationName() {
        return REGISTRATION_NAME;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public ImageLocation parseUrl(final String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public DataArguments generateDataArgumentsFrom(ImageLocation imageLocation) {
        final DataArguments dataArguments = new DataArguments(1);
        dataArguments.put(ID_ARGUMENT, imageLocation.getId());
        return dataArguments;
    }

    @Override
    public String generateUrl(ImageLocation imageLocation, Session session) throws OXException {
        StringBuilder stringBuilder = new StringBuilder();
        ImageUtility.startImageUrl(imageLocation, session, this, true, stringBuilder);
        if (null == imageLocation.getTimestamp()) {
            Contact user = optUser(session, imageLocation, ContactField.LAST_MODIFIED);
            if (null != user && null != user.getLastModified()) {
                stringBuilder.append('&').append("timestamp=").append(user.getLastModified().getTime());
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public long getExpires() {
        return -1L;
    }

    @Override
    public String getETag(ImageLocation imageLocation, Session session) throws OXException {
        PictureSearchData contactPictureRequestData = new PictureSearchData(I(Tools.getUnsignedInteger(imageLocation.getId())), null, null, null, null);
        return services.getServiceSafe(ContactPictureService.class).getETag(session, contactPictureRequestData);
    }

    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

    private Contact optUser(Session session, ImageLocation imageLocation, ContactField... fields) throws OXException {
        return optUser(session, Tools.getUnsignedInteger(imageLocation.getId()), fields);
    }

    private Contact optUser(Session session, int userID, ContactField... fields) throws OXException {
        ContactService contactService = services.getServiceSafe(ContactService.class);
        try {
            return contactService.getUser(session, userID, fields);
        } catch (OXException e) {
            LOG.debug("error getting user contact", e);
        }
        return null;
    }

}
