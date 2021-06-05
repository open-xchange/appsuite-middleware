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

package com.openexchange.contact.vcard;

import java.awt.Dimension;
import java.util.Set;
import com.openexchange.session.Session;

/**
 * {@link VCardParameters}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public interface VCardParameters {

    /**
     * Gets the target version to use during export.
     *
     * @return The version
     */
    VCardVersion getVersion();

    /**
     * Sets the target version to use during export.
     *
     * @param version The version
     * @return A self reference
     */
    VCardParameters setVersion(VCardVersion version);

    /**
     * Gets the dimensions for scaling contact images during export.
     *
     * @return The photo scale dimension, or <code>null</code> if no scaling should be done
     */
    Dimension getPhotoScaleDimension();

    /**
     * Sets the dimensions for scaling contact images during export.
     *
     * @param dimension The photo scale dimension, or <code>null</code> if no scaling should be done
     * @return A self reference
     */
    VCardParameters setPhotoScaleDimension(Dimension dimension);

    /**
     * Gets a value indicating whether import and export is done in <i>strict</i> mode or not.
     *
     * @return <code>true</code> if strict mode is enabled, <code>false</code>, otherwise
     */
    boolean isStrict();

    /**
     * Sets a value indicating whether import and export is done in <i>strict</i> mode or not.
     *
     * @param strict <code>true</code> if strict mode is enabled, <code>false</code>, otherwise
     * @return A self reference
     */
    VCardParameters setStrict(boolean strict);

    /**
     * Gets the underlying groupware session.
     *
     * @return The session, or <code>null</code> if not set
     */
    Session getSession();

    /**
     * Sets the underlying groupware session.
     *
     * @param session The session, or <code>null</code> if not set
     * @return A self reference
     */
    VCardParameters setSession(Session session);

    /**
     * Gets the maximum allowed size in bytes for contact images.
     *
     * @return The maximum allowed size
     */
    long getMaxContactImageSize();

    /**
     * Sets the maximum allowed size in bytes for contact images.
     *
     * @param maxSize The maximum allowed size
     * @return A self reference
     */
    VCardParameters setMaxContactImageSize(long maxSize);

    /**
     * Gets a value indicating whether e-mail addresses in contacts should by checked for validity or not.
     *
     * @return <code>true</code> to validate e-mail addresses, <code>false</code>, otherwise
     */
    boolean isValidateContactEMail();

    /**
     * Sets a value indicating whether e-mail addresses in contacts should by checked for validity or not.
     *
     * @param validateContactEMail <code>true</code> to validate e-mail addresses, <code>false</code>, otherwise
     * @return A self reference
     */
    VCardParameters setValidateContactEMail(boolean validateContactEMail);

    /**
     * Gets a value indicating whether vCards should be validated after parsing or not.
     *
     * @return <code>true</code> if additional validation is skipped, <code>false</code>, otherwise
     */
    boolean isSkipValidation();

    /**
     * Sets a value indicating whether vCards should be validated after parsing or not.
     *
     * @param skipValidation <code>true</code> if additional validation is skipped, <code>false</code>, otherwise
     * @return A self reference
     */
    VCardParameters setSkipValidation(boolean skipValidation);

    /**
     * Gets the maximum allowed size of a (single) vCard file in bytes. vCards larger than the configured maximum size are rejected and
     * not parsed. A value of <code>0</code> or smaller is considered as unlimited.
     *
     * @return The maximum allowed size of a (single) vCard file in bytes, or <code>0</code> if not restricted
     */
    long getMaxVCardSize();

    /**
     * Sets the maximum allowed size of a (single) vCard file in bytes. vCards larger than the configured maximum size are rejected and
     * not parsed. A value of <code>0</code> or smaller is considered as unlimited.
     *
     * @param maxSize The maximum allowed size of a (single) vCard file in bytes, or <code>0</code> if not restricted
     * @return A self reference
     */
    VCardParameters setMaxVCardSize(long maxSize);

    /**
     * Gets a value indicating whether the original vCard should be remembered during import and provided via the associated import
     * result afterwards or not.
     * <p/>
     * If enabled, the original vCard is written into a file holder that is available via {@link VCardImport#getVCard()} afterwards, so
     * the calling method should ensure to close each import result in a try/finally block to release resources.
     *
     * @return <code>true</code> if the original vCard should be kept, <code>false</code>, otherwise
     */
    boolean isKeepOriginalVCard();

    /**
     * Sets a value indicating whether the original vCard should be remembered during import and provided via the associated import
     * result afterwards or not.
     * <p/>
     * If enabled, the original vCard is written into a file holder that is available via {@link VCardImport#getVCard()} afterwards, so
     * the calling method should ensure to close each import result in a try/finally block to release resources.
     *
     * @param keepOriginalVCard <code>true</code> if the original vCard should be kept, <code>false</code>, otherwise
     * @return A self reference
     */
    VCardParameters setKeepOriginalVCard(boolean keepOriginalVCard);

    /**
     * Gets a value indicating whether the previously imported binary <code>PHOTO</code> value should be removed to reduce the file size
     * when storing the original vCard or not.
     * <p/>
     * This setting is only effective if <code>keepOriginalVCard</code> is <code>true</code>.
     *
     * @return <code>true</code> if binary value of the imported <code>PHOTO</code> property should be removed, <code>false</code>, otherwise
     */
    boolean isRemoveImageFromKeptVCard();

    /**
     * Sets a value indicating whether the previously imported binary <code>PHOTO</code> value should be removed to reduce the file size
     * when storing the original vCard or not.
     * <p/>
     * This setting is only effective if <code>keepOriginalVCard</code> is <code>true</code>.
     *
     * @param removeImageFromKeptVCard <code>true</code> if binary value of the imported <code>PHOTO</code> property should be removed,
     *            <code>false</code>, otherwise
     * @return A self reference
     */
    VCardParameters setRemoveImageFromKeptVCard(boolean removeImageFromKeptVCard);

    /**
     * Gets a value indicating whether attachments found in (non-standard) <code>ATTACH</code> properties should be processed and imported
     * into some specific extended properties of the contact.
     *
     * @return <code>true</code> if attachments should be imported, <code>false</code>, otherwise
     */
    boolean isImportAttachments();

    /**
     * Configures if attachments found in (non-standard) <code>ATTACH</code> properties should be processed and imported into some
     * specific extended properties of the contact or not.
     *
     * @param importAttachments <code>true</code> if attachments should be imported, <code>false</code>, otherwise
     * @return A self reference
     */
    VCardParameters setImportAttachments(boolean importAttachments);

    /**
     * Gets a value indicating whether imported attachments found in (non-standard) <code>ATTACH</code> properties should be removed from
     * the imported vCard after processing or not.
     *
     * @return <code>true</code> if attachments should be removed from the kept vCard, <code>false</code>, otherwise
     */
    boolean isRemoveAttachmentsFromKeptVCard();

    /**
     * Configures if imported attachments found in (non-standard) <code>ATTACH</code> properties should be removed from the imported vCard
     * after processing or not.
     *
     * @param removeAttachmentsFromKeptVCard <code>true</code> if attachments should be removed from the kept vCard, <code>false</code>, otherwise
     * @return A self reference
     */
    VCardParameters setRemoveAttachmentsFromKeptVCard(boolean removeAttachmentsFromKeptVCard);

    /**
     * Gets the configured mode used for the export of distribution lists.
     * 
     * @return The distribution list mode, or <code>null</code> if not defined
     */
    DistributionListMode getDistributionListMode();

    /**
     * Configures the mode used for the export of distribution lists.
     * 
     * @param mode The distribution list mode to use
     * @return A self reference
     */
    VCardParameters setDistributionListMode(DistributionListMode mode);

    /**
     * Gets the vCard property names to consider during import/export.
     *
     * @return The property names, or <code>null</code> if not set
     */
    Set<String> getPropertyNames();

    /**
     * Sets the vCard property names to consider during import/export.
     *
     * @param propertyNames The property names, or <code>null</code> if not set
     * @return A self reference
     */
    VCardParameters setPropertyNames(Set<String> propertyNames);

    /**
     * Gets the value of an arbitrary extended parameter.
     *
     * @param name The parameter name
     * @param clazz The parameter value's class
     * @return The parameter's value, or <code>null</code> if not set
     */
    <T> T get(String name, Class<T> clazz);

    /**
     * Sets the value for an arbitrary extended parameter.
     *
     * @param name The parameter name
     * @param value The parameter value, or <code>null</code> to remove the parameter
     * @return A self reference
     */
    <T> VCardParameters set(String name, T value);
}
