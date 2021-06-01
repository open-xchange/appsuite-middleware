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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.contact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.mysqlStorage.ContactImageScaler;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.imagetransformation.ImageMetadataService;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.user.UserService;

import static com.openexchange.java.Autoboxing.I;

/**
 * {@link ContactImageAttributeChanger} - Updates or removes the contact picture of a user
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class ContactImageAttributeChanger extends AbstractAttributeChangers {

    private static final Set<String> RESULT_OK = ImmutableSet.<String> builderWithExpectedSize(2).add("image1").add("image1ContextType").build();

    private static final String INSERT_OR_UPDATE_STMT = "INSERT INTO prg_contacts_image (cid, intfield01, image1, changing_date, mime_type) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE image1 = ?, mime_type = ?, changing_date=?";
    private static final String DELETE_STMT = "DELETE FROM prg_contacts_image WHERE cid= ? AND intfield01 = ?";
    private static final String UPDATE_CONTACT_INCREMENT_IMAGECOUNT = "UPDATE prg_contacts SET changing_date=?, intfield04 = 1 WHERE cid = ? and userid = ?";
    private static final String UPDATE_CONTACT_DECREMENT_IMAGECOUNT = "UPDATE prg_contacts SET changing_date=?, intfield04 = 0 WHERE cid = ? and userid = ?";

    /**
     * Gets the service of the given type
     *
     * @param <S> The service type
     * @param clazz The service type
     * @return The service of the given type
     * @throws OXException if the given service is not present
     */
    private <S extends Object> S requireService(final Class<? extends S> clazz) throws OXException {
        return AdminServiceRegistry.getInstance().getService(clazz, true);
    }

    /**
     *
     * Updates the image count
     *
     * @param connection The connection to use
     * @param contextId The context ID to update the counter for
     * @param userId The user ID to update the counter for
     * @param sql The SQL statement to use for updating the counter
     * @return the number of rows updated
     * @throws StorageException
     */
    private int updateImageCount(Connection connection, int contextId, int userId, String sql) throws StorageException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 0;
            statement.setLong(++index, System.currentTimeMillis());
            statement.setInt(++index, contextId);
            statement.setInt(++index, userId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Ensures that the given image data does not exceed the max_image_size property
     *
     * @param imageData The image data to check
     * @throws OXException if the given data exceeds the max_image_size property
     */
    private void checkImageSize(byte[] imageData) throws OXException {
        int maxImageSize = requireService(ConfigurationService.class).getIntProperty("max_image_size", 4194304);
        if (imageData != null && imageData.length > maxImageSize) {
            throw ContactExceptionCodes.IMAGE_TOO_LARGE.create(I(imageData.length), I(maxImageSize));
        }
    }

    /**
     * Inserts a new contact image or updates the existing contact image of the given contactId
     *
     * @param connection The connection to use
     * @param contextId The context ID
     * @param contactId The contact ID
     * @param imageData the image data
     * @param imageContentType the content type of the image
     * @return The modified attributes
     * @throws StorageException
     * @throws OXException
     */
    private Set<String> insertOrUpdate(Connection connection, int contextId, int userId, int contactId, byte[] imageData, String imageContentType) throws StorageException, OXException {
        if (imageData == null || imageData.length == 0) {
            return Collections.emptySet();
        }
        Objects.requireNonNull(imageContentType, "imageContentType must not be null");
        checkImageSize(imageData);

        // Scale if required
        ContactImageScaler imageScaler = new ContactImageScaler(requireService(ImageMetadataService.class), requireService(ImageTransformationService.class));
        byte[] bytes = imageScaler.scaleIfRequired(imageData, imageContentType);

        try (PreparedStatement statement = connection.prepareStatement(INSERT_OR_UPDATE_STMT)) {
            long now = System.currentTimeMillis();
            int index = 0;
            // new arguments
            statement.setInt(++index, contextId);
            statement.setInt(++index, contactId);
            statement.setBytes(++index, bytes);
            statement.setLong(++index, now);
            statement.setString(++index, imageContentType);
            // update arguments
            statement.setBytes(++index, bytes);
            statement.setString(++index, imageContentType);
            statement.setLong(++index, now);
            //@formatter:off
            return statement.executeUpdate() > 0 && updateImageCount(connection, contextId, userId, UPDATE_CONTACT_INCREMENT_IMAGECOUNT) > 0 ?
                RESULT_OK :
                Collections.emptySet();
            //@formatter:on
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Deletes the contact image of given contactId
     *
     * @param connection The connection to use
     * @param contextId The context ID
     * @param userId The user ID
     * @param contactId The contact ID
     * @return The deleted attributes
     * @throws StorageException
     */
    private Set<String> delete(Connection connection, int contextId, int userId, int contactId) throws StorageException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_STMT)) {
            int index = 0;
            statement.setInt(++index, contextId);
            statement.setInt(++index, contactId);
            //@formatter:off
            return statement.executeUpdate() > 0 && updateImageCount(connection, contextId, userId, UPDATE_CONTACT_DECREMENT_IMAGECOUNT) > 0 ?
                RESULT_OK :
                Collections.emptySet();
            //@formatter:on
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        try {
            int contactId = requireService(UserService.class).getUser(userId, contextId).getContactId();
            if (userData.getImage1() != null) {
                // Delete or update
                return insertOrUpdate(connection, contextId, userId, contactId, userData.getImage1(), userData.getImage1ContentType());
            }
            if (userData.isImage1set()) {
                // Explicitly set to null
                return delete(connection, contextId, userId, contactId);
            }
            return Collections.emptySet();
        } catch (OXException e) {
            throw new StorageException(e);
        }
    }
}
