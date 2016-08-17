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

package com.openexchange.file.storage;

/**
 * {@link FileStorageCapabilityTools} - Utility class for file storage capabilities.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class FileStorageCapabilityTools {

    /**
     * Initializes a new {@link FileStorageCapabilityTools}.
     */
    private FileStorageCapabilityTools() {
        super();
    }

    /**
     * Gets a value indicating whether a specific account supports a specific capability.
     *
     * @param fileAccessClass The file access class to check the capability for
     * @param capability The capability to check
     * @return A {@code Boolean} instance indicating support for specified capability or <code>null</code> if support cannot be checked by class (but by instance; see {@link #supports(FileStorageFileAccess, FileStorageCapability)})
     * @see #supports(FileStorageFileAccess, FileStorageCapability)
     */
    public static Boolean supportsByClass(Class<? extends FileStorageFileAccess> fileAccessClass, FileStorageCapability capability) {
        switch (capability) {
        case FILE_VERSIONS:
            return Boolean.valueOf(FileStorageVersionedFileAccess.class.isAssignableFrom(fileAccessClass));
        case FOLDER_ETAGS:
            return Boolean.valueOf(FileStorageETagProvider.class.isAssignableFrom(fileAccessClass));
        case IGNORABLE_VERSION:
            return Boolean.valueOf(FileStorageIgnorableVersionFileAccess.class.isAssignableFrom(fileAccessClass));
        case PERSISTENT_IDS:
            return Boolean.valueOf(FileStoragePersistentIDs.class.isAssignableFrom(fileAccessClass));
        case RANDOM_FILE_ACCESS:
            return Boolean.valueOf(FileStorageRandomFileAccess.class.isAssignableFrom(fileAccessClass));
        case RECURSIVE_FOLDER_ETAGS:
            // Cannot be checked by class
            return null;
        case SEARCH_BY_TERM:
            return Boolean.valueOf(FileStorageAdvancedSearchFileAccess.class.isAssignableFrom(fileAccessClass));
        case SEQUENCE_NUMBERS:
            return Boolean.valueOf(FileStorageSequenceNumberProvider.class.isAssignableFrom(fileAccessClass));
        case THUMBNAIL_IMAGES:
            return Boolean.valueOf(ThumbnailAware.class.isAssignableFrom(fileAccessClass));
        case EFFICIENT_RETRIEVAL:
            return Boolean.valueOf(FileStorageEfficientRetrieval.class.isAssignableFrom(fileAccessClass));
        case LOCKS:
            return Boolean.valueOf(FileStorageLockedFileAccess.class.isAssignableFrom(fileAccessClass));
        case OBJECT_PERMISSIONS:
            return Boolean.valueOf(ObjectPermissionAware.class.isAssignableFrom(fileAccessClass));
        case RANGES:
            return Boolean.valueOf(FileStorageRangeFileAccess.class.isAssignableFrom(fileAccessClass));
        case EXTENDED_METADATA:
            return Boolean.valueOf(FileStorageExtendedMetadata.class.isAssignableFrom(fileAccessClass));
        case MULTI_MOVE:
            return Boolean.valueOf(FileStorageMultiMove.class.isAssignableFrom(fileAccessClass));
        case READ_ONLY:
            return Boolean.valueOf(FileStorageReadOnly.class.isAssignableFrom(fileAccessClass));
        case MAIL_ATTACHMENTS:
            return Boolean.valueOf(FileStorageMailAttachments.class.isAssignableFrom(fileAccessClass));
        case AUTO_NEW_VERSION:
            return Boolean.valueOf(FileStorageIgnorableVersionFileAccess.class.isAssignableFrom(fileAccessClass));
        case ZIPPABLE_FOLDER:
            return Boolean.valueOf(FileStorageZippableFolderFileAccess.class.isAssignableFrom(fileAccessClass));
        default:
            org.slf4j.LoggerFactory.getLogger(FileStorageCapabilityTools.class).warn("Unknown capability: {}", capability);
            return Boolean.FALSE;
        }
    }

    /**
     * Gets a value indicating whether a specific account supports a specific capability.
     *
     * @param fileAccess The file access reference to check the capability for
     * @param capability The capability to check
     * @return <code>true</code> if the capability is supported, <code>false</code>, otherwise
     */
    public static boolean supports(FileStorageFileAccess fileAccess, FileStorageCapability capability) {
        switch (capability) {
        case FILE_VERSIONS:
            return FileStorageVersionedFileAccess.class.isInstance(fileAccess);
        case FOLDER_ETAGS:
            return FileStorageETagProvider.class.isInstance(fileAccess);
        case IGNORABLE_VERSION:
            return FileStorageIgnorableVersionFileAccess.class.isInstance(fileAccess);
        case PERSISTENT_IDS:
            return FileStoragePersistentIDs.class.isInstance(fileAccess);
        case RANDOM_FILE_ACCESS:
            return FileStorageRandomFileAccess.class.isInstance(fileAccess);
        case RECURSIVE_FOLDER_ETAGS:
            return FileStorageETagProvider.class.isInstance(fileAccess) && ((FileStorageETagProvider) fileAccess).isRecursive();
        case SEARCH_BY_TERM:
            return FileStorageAdvancedSearchFileAccess.class.isInstance(fileAccess);
        case SEQUENCE_NUMBERS:
            return FileStorageSequenceNumberProvider.class.isInstance(fileAccess);
        case THUMBNAIL_IMAGES:
            return ThumbnailAware.class.isInstance(fileAccess);
        case EFFICIENT_RETRIEVAL:
            return FileStorageEfficientRetrieval.class.isInstance(fileAccess);
        case LOCKS:
            return FileStorageLockedFileAccess.class.isInstance(fileAccess);
        case OBJECT_PERMISSIONS:
            return ObjectPermissionAware.class.isInstance(fileAccess);
        case RANGES:
            return FileStorageRangeFileAccess.class.isInstance(fileAccess);
        case EXTENDED_METADATA:
            return FileStorageExtendedMetadata.class.isInstance(fileAccess);
        case MULTI_MOVE:
            return FileStorageMultiMove.class.isInstance(fileAccess);
        case READ_ONLY:
            return FileStorageReadOnly.class.isInstance(fileAccess);
        case MAIL_ATTACHMENTS:
            return FileStorageMailAttachments.class.isInstance(fileAccess);
        case AUTO_NEW_VERSION:
            return FileStorageIgnorableVersionFileAccess.class.isInstance(fileAccess);
        case ZIPPABLE_FOLDER:
            return FileStorageZippableFolderFileAccess.class.isInstance(fileAccess);
        default:
            org.slf4j.LoggerFactory.getLogger(FileStorageCapabilityTools.class).warn("Unknown capability: {}", capability);
            return false;
        }
    }

}
