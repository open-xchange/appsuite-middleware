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
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link UserImageDataSource} - A data source to obtains a user's image data
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class UserImageDataSource implements ImageDataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserImageDataSource.class);
    private static final String REGISTRATION_NAME = "com.openexchange.user.image";
    private static final String ALIAS = "/user/picture";
    private static final String ID_ARGUMENT = "com.openexchange.groupware.user.id";

    private static final UserImageDataSource INSTANCE = new UserImageDataSource();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static UserImageDataSource getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link UserImageDataSource}.
     */
    private UserImageDataSource() {
        super();
    }

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
        /*
         * Get user contact
         */
        Contact user = optUser(session, userID, ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE);
        if (null == user) {
            throw UserExceptionCode.USER_NOT_FOUND.create(Integer.valueOf(userID), Integer.valueOf(session.getContextId()));
        }
        /*
         * Return user contact image
         */
        byte[] imageBytes = user.getImage1();
        DataProperties properties = new DataProperties(5);
        properties.put(DataProperties.PROPERTY_FOLDER_ID, String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID));
        properties.put(DataProperties.PROPERTY_ID, String.valueOf(userID));

        if (null == imageBytes) {
            LOG.warn("Requested a non-existing image in user contact: user-id={} context={} session-user={}. Returning an empty image as fallback.", userID, session.getContextId(), session.getUserId());
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            return new SimpleData<D>((D)new UnsynchronizedByteArrayInputStream(new byte[0]), properties);
        }

        if (com.openexchange.ajax.helper.ImageUtils.isSvg(imageBytes)) {
            LOG.warn("Detected a possibly harmful SVG image in user contact: user-id={} context={} session-user={}. Returning an empty image as fallback.", userID, session.getContextId(), session.getUserId());
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            return new SimpleData<D>((D)new UnsynchronizedByteArrayInputStream(new byte[0]), properties);
        }

        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, user.getImageContentType());
        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(imageBytes.length));
        properties.put(DataProperties.PROPERTY_NAME, user.getImageContentType().replace('/', '.'));
        return new SimpleData<D>((D)new UnsynchronizedByteArrayInputStream(imageBytes), properties);
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
        String timestamp = imageLocation.getTimestamp();
        if (null == timestamp) {
            Contact user = optUser(session, imageLocation, ContactField.LAST_MODIFIED);
            if (null != user && null != user.getLastModified()) {
                timestamp = String.valueOf(user.getLastModified().getTime());
            }
        }
        return null != timestamp ? timestamp : "0";
    }

    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

    private static Contact optUser(Session session, ImageLocation imageLocation, ContactField...fields) throws OXException {
        return optUser(session, ImageUtility.getUnsignedInteger(imageLocation.getId()), fields);
    }

    private static Contact optUser(Session session, int userID, ContactField...fields) {
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        if (null != contactService) {
            try {
                return contactService.getUser(session, userID, fields);
            } catch (OXException e) {
                LOG.debug("error getting user contact", e);
            }
        }
        return null;
    }

}
