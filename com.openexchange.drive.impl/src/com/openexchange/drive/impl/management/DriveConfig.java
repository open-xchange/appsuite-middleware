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

package com.openexchange.drive.impl.management;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.BrandedDriveVersionService;
import com.openexchange.drive.DriveClientType;
import com.openexchange.drive.DriveClientVersion;
import com.openexchange.drive.DriveProperty;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.drive.impl.management.version.BrandedDriveVersionServiceImpl;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link DriveConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveConfig {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveConfig.class);
    }

    private static final long MILLIS_PER_HOUR = 1000 * 60 * 60;

    private static final DriveConfig INSTANCE = new DriveConfig();

    /**
     * Gets the drive configuration instance.
     *
     * @return The drive config instance
     */
    public static DriveConfig getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DriveConfig}.
     */
    private DriveConfig() {
        super();
    }

    /**
     * Gets the useTempFolder
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The useTempFolder
     */
    public boolean isUseTempFolder(int contextId, int userId) {
        return getConfigService().getBooleanProperty(userId, contextId, DriveProperty.USE_TEMP_FOLDER);
    }

    /**
     * Gets the cleanerInterval
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The cleanerInterval
     */
    public long getCleanerInterval(int contextId, int userId) throws OXException {
        String cleanerIntervalValue = getConfigService().getProperty(userId, contextId, DriveProperty.CLEANER_INTERVAL);
        return parseTimeSpan(cleanerIntervalValue, 1);
    }

    /**
     * Gets the cleanerMaxAge
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The cleanerMaxAge
     */
    public long getCleanerMaxAge(int contextId, int userId) throws OXException {
        String cleanerMaxAgeValue = getConfigService().getProperty(userId, contextId, DriveProperty.CLEANER_MAX_AGE);
        return parseTimeSpan(cleanerMaxAgeValue, 1);
    }

    /**
     * Gets the maxBandwidth
     *
     * @return The maxBandwidth
     */
    public int getMaxBandwidth() {
        String maxBandwidthValue = getConfigService().getProperty(DriveProperty.MAX_BANDWIDTH);
        return Strings.isEmpty(maxBandwidthValue) || "-1".equals(maxBandwidthValue) ? -1 : parseBytes(maxBandwidthValue);
    }

    /**
     * Gets the maxBandwidthPerClient
     *
     * @return The maxBandwidthPerClient
     */
    public int getMaxBandwidthPerClient() {
        String maxBandwidthPerClientValue = getConfigService().getProperty(DriveProperty.MAX_BANDWIDTH_PER_CLIENT);
        return Strings.isEmpty(maxBandwidthPerClientValue) || "-1".equals(maxBandwidthPerClientValue) ? -1 : parseBytes(maxBandwidthPerClientValue);
    }

    /**
     * Gets the maxConcurrentSyncOperations
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The maxConcurrentSyncOperations
     */
    public int getMaxConcurrentSyncOperations(int contextId, int userId) {
        return getConfigService().getIntProperty(userId, contextId, DriveProperty.MAX_CONCURRENT_SYNC_OPERATIONS);
    }

    /**
     * Gets the directLinkQuota
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The directLinkQuota
     */
    public String getDirectLinkQuota(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.DIRECT_LINK_QUOTA);
    }

    /**
     * Gets the directLinkHelp
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The directLinkHelp
     */
    public String getDirectLinkHelp(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.DIRECT_LINK_HELP);
    }

    /**
     * Gets the excludedFilenamesPattern
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The excludedFilenamesPattern
     */
    public Pattern getExcludedFilenamesPattern(int contextId, int userId) {
        try {
            return Pattern.compile(getConfigService().getProperty(userId, contextId, DriveProperty.EXCLUDED_FILES_PATTERN), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException e) {
            LoggerHolder.LOG.warn("{} configuration error for user {} in context {}", DriveProperty.EXCLUDED_FILES_PATTERN.getFQPropertyName(), Integer.valueOf(userId), Integer.valueOf(contextId), e);
            return Pattern.compile(DriveProperty.EXCLUDED_FILES_PATTERN.getDefaultValue(String.class), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }

    /**
     * Gets the excludedDirectoriesPattern
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The excludedDirectoriesPattern
     */
    public Pattern getExcludedDirectoriesPattern(int contextId, int userId) {
        try {
            return Pattern.compile(getConfigService().getProperty(userId, contextId, DriveProperty.EXCLUDED_DIRECTORIES_PATTERN), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException e) {
            LoggerHolder.LOG.warn("{} configuration error for user {} in context {}", DriveProperty.EXCLUDED_DIRECTORIES_PATTERN.getFQPropertyName(), Integer.valueOf(userId), Integer.valueOf(contextId), e);
            return Pattern.compile(DriveProperty.EXCLUDED_DIRECTORIES_PATTERN.getDefaultValue(String.class), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }

    /**
     * Gets the shortProductName
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The shortProductName
     */
    public String getShortProductName(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.SHORT_PRODUCT_NAME);
    }

    /**
     * Gets the minApiVersion
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The minApiVersion
     */
    public int getMinApiVersion(int contextId, int userId) {
        return getConfigService().getIntProperty(userId, contextId, DriveProperty.MIN_API_VERSION);
    }

    /**
     * Gets the maxDirectoryActions
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The maxDirectoryActions
     */
    public int getMaxDirectoryActions(int contextId, int userId) {
        return getConfigService().getIntProperty(userId, contextId, DriveProperty.MAX_DIRECTORY_ACTIONS);
    }

    /**
     * Gets the maxFileActions
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The maxFileActions
     */
    public int getMaxFileActions(int contextId, int userId) {
        return getConfigService().getIntProperty(userId, contextId, DriveProperty.MAX_FILE_ACTIONS);
    }

    /**
     * Gets the directLinkFragmentsFile
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The directLinkFragmentsFile
     */
    public String getDirectLinkFragmentsFile(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.DIRECT_LINK_FRAGMENTS_FILE);
    }

    /**
     * Gets the directLinkFile
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The directLinkFile
     */
    public String getDirectLinkFile(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.DIRECT_LINK_FILE);
    }

    public String getJumpLink(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.JUMP_LINK);
    }

    /**
     * Gets the thumbnailImageSize
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The thumbnailImageSize
     */
    public int[] getThumbnailImageSize(int contextId, int userId) {
        String value = getConfigService().getProperty(userId, contextId, DriveProperty.THUMBNAIL_IMAGE_SIZE);
        try {
            return parseDimensions(value);
        } catch (OXException e) {
            LoggerHolder.LOG.warn("{} configuration error for user {} in context {}", DriveProperty.THUMBNAIL_IMAGE_SIZE.getFQPropertyName(), Integer.valueOf(userId), Integer.valueOf(contextId), e);
            return new int[] { 250, 100 };
        }
    }

    /**
     * Gets the previewImageSize
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The previewImageSize
     */
    public int[] getPreviewImageSize(int contextId, int userId) {
        String value = getConfigService().getProperty(userId, contextId, DriveProperty.PREVIEW_IMAGE_SIZE);
        try {
            return parseDimensions(value);
        } catch (OXException e) {
            LoggerHolder.LOG.warn("{} configuration error for user {} in context {}", DriveProperty.PREVIEW_IMAGE_SIZE.getFQPropertyName(), Integer.valueOf(userId), Integer.valueOf(contextId), e);
            return new int[] { 1600, 1600 };
        }
    }

    /**
     * Gets the imageLinkDocumentFile
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The imageLinkDocumentFile
     */
    public String getImageLinkDocumentFile(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.IMAGE_LINK_DOCUMENT_FILE);
    }

    /**
     * Gets the directLinkDirectory
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The directLinkDirectory
     */
    public String getDirectLinkDirectory(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.DIRECT_LINK_DIRECTORY);
    }

    /**
     * Gets the directLinkFragmentsDirectory
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The directLinkFragmentsDirectory
     */
    public String getDirectLinkFragmentsDirectory(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.DIRECT_LINK_FRAGMENTS_DIRECTORY);
    }

    /**
     * Gets the imageLinkAudioFile
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The imageLinkAudioFile
     */
    public String getImageLinkAudioFile(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.IMAGE_LINK_AUDIO_FILE);
    }

    /**
     * Gets the imageLinkImageFile
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The imageLinkImageFile
     */
    public String getImageLinkImageFile(int contextId, int userId) {
        return getConfigService().getProperty(userId, contextId, DriveProperty.IMAGE_LINK_IMAGE_FILE);
    }

    /**
     * Gets the uiWebPath
     *
     * @return The uiWebPath
     */
    public String getUiWebPath() {
        return ServerConfig.getProperty(Property.UI_WEB_PATH);
    }

    /**
     * Gets the dispatcherPrefix
     *
     * @return The dispatcherPrefix
     */
    public String getDispatcherPrefix() {
        DispatcherPrefixService service = DriveServiceLookup.getService(DispatcherPrefixService.class);
        if (null != service) {
            return service.getPrefix();
        }
        return DispatcherPrefixService.DEFAULT_PREFIX;
    }

    /**
     * Gets the maxDirectories
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The maxDirectories
     */
    public int getMaxDirectories(int contextId, int userId) {
        return getConfigService().getIntProperty(userId, contextId, DriveProperty.MAX_DIRECTORIES);
    }

    /**
     * Gets the maxFilesPerDirectory
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The maxFilesPerDirectory
     */
    public int getMaxFilesPerDirectory(int contextId, int userId) {
        return getConfigService().getIntProperty(userId, contextId, DriveProperty.MAX_FILES_PER_DIRECTORY);
    }

    /**
     * Gets a value indicating whether synchronization is enabled for a specific file storage service or not.
     *
     * @param serviceID The identifier of the file storage service to check
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if synchronization is enabled, <code>false</code>, otherwise
     */
    public boolean isEnabledService(String serviceID, int contextId, int userId) {
        String[] enabledServicesValue = Strings.splitByCommaNotInQuotes(getConfigService().getProperty(userId, contextId, DriveProperty.ENABLED_SERVICES));
        Set<String> enabledServices = new HashSet<String>(Arrays.asList(enabledServicesValue));
        return Strings.isNotEmpty(serviceID) && enabledServices.contains(serviceID);
    }

    /**
     * Gets a value indicating whether a specific folder is excluded explicitly from synchronization or not.
     *
     * @param folderID The identifier of the folder to check
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if the folder is excluded, <code>false</code>, otherwise
     */
    public boolean isExcludedFolder(String folderID, int contextId, int userId) {
        return Strings.isNotEmpty(folderID) && getExcludedFolders(contextId, userId).contains(folderID);
    }

    /**
     * Gets the excluded folders
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The excluded folders
     */
    public Set<String> getExcludedFolders(int contextId, int userId) {
        String[] exclduedFoldersValue = Strings.splitByCommaNotInQuotes(getConfigService().getProperty(userId, contextId, DriveProperty.EXCLUDED_FOLDERS));
        return ((null == exclduedFoldersValue) || (0 == exclduedFoldersValue.length)) ? Collections.emptySet() : new HashSet<String>(Arrays.asList(exclduedFoldersValue));
    }

    /**
     * Gets the checksumCleanerInterval
     *
     * @return The checksumCleanerInterval
     */
    public long getChecksumCleanerInterval() {
        String value = getConfigService().getProperty(DriveProperty.CHECKSUM_CLEANER_INTERVAL);
        try {
            return parseTimeSpan(value, 1);
        } catch (OXException e) {
            LoggerHolder.LOG.warn("{} configuration error.", DriveProperty.CHECKSUM_CLEANER_INTERVAL.getFQPropertyName(), e);
            return MILLIS_PER_HOUR * 24;
        }
    }

    /**
     * Gets the checksumCleanerMaxAge
     *
     * @return The checksumCleanerMaxAge
     */
    public long getChecksumCleanerMaxAge() {
        String value = getConfigService().getProperty(DriveProperty.CHECKSUM_CLEANER_MAXAGE);
        try {
            return parseTimeSpan(value, 24);
        } catch (OXException e) {
            LoggerHolder.LOG.warn("{} configuration error.", DriveProperty.CHECKSUM_CLEANER_MAXAGE.getFQPropertyName(), e);
            return MILLIS_PER_HOUR * 24 * 4 * 7;
        }
    }

    /**
     * Gets the optimisticSaveThresholdMobile
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The optimisticSaveThresholdMobile
     */
    public long getOptimisticSaveThresholdMobile(int contextId, int userId) {
        return parseBytes(getConfigService().getProperty(userId, contextId, DriveProperty.OPTIMISTIC_SAVE_THRESHOLD_MOBILE));
    }

    /**
     * Gets the optimisticSaveThresholdDesktop
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The optimisticSaveThresholdDesktop
     */
    public long getOptimisticSaveThresholdDesktop(int contextId, int userId) {
        return parseBytes(getConfigService().getProperty(userId, contextId, DriveProperty.OPTIMISTIC_SAVE_THRESHOLD_DESKTOP));
    }

    /**
     * Parse Drive client version
     *
     * @param value The version to parse
     * @return The parsed {@link DriveClientVersion}
     * @throws OXException
     */
    private static DriveClientVersion parseClientVersion(String value) throws OXException {
        try {
            return Strings.isEmpty(value) ? DriveClientVersion.VERSION_0 : new DriveClientVersion(value);
        } catch (IllegalArgumentException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, value);
        }
    }

    /**
     * Parse image dimensions into int[]
     *
     * @param value The string to parse
     * @return The parsed dimensions as int[]
     * @throws OXException
     */
    private static int[] parseDimensions(String value) throws OXException {
        int idx = value.indexOf('x');
        if (1 > idx) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(value);
        }
        try {
            return new int[] { Integer.parseInt(value.substring(0, idx)), Integer.parseInt(value.substring(idx + 1)) };
        } catch (NumberFormatException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, value);
        }
    }

    /**
     * Parses a byte value including an optional unit.
     *
     * @param value the value to parse
     * @return The parsed number of bytes
     * @throws NumberFormatException If the supplied string is not parsable or greater then <code>Integer.MAX_VALUE</code>
     */
    private static int parseBytes(String value) throws NumberFormatException {
        StringBuilder numberAllocator = new StringBuilder(8);
        StringBuilder unitAllocator = new StringBuilder(4);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c) || '.' == c || '-' == c) {
                numberAllocator.append(c);
            } else if (false == Character.isWhitespace(c)) {
                unitAllocator.append(c);
            }
        }
        double number = Double.parseDouble(numberAllocator.toString());
        if (0 < unitAllocator.length()) {
            String unit = unitAllocator.toString().toUpperCase();
            int exp = Arrays.asList("B", "KB", "MB", "GB").indexOf(unit);
            if (0 <= exp) {
                number *= Math.pow(1024, exp);
            } else {
                throw new NumberFormatException(value);
            }
        }
        if (Integer.MAX_VALUE >= number) {
            return (int)number;
        }
        throw new NumberFormatException(value);
    }

    /**
     * Gets the (soft) minimum version limit for the supplied client type
     *
     * @param clientType The client type to get the limit for
     * @param session The current session
     * @return The configured limit, or {@link DriveClientVersion#VERSION_0} if not defined
     */
    public DriveClientVersion getSoftMinimumVersion(DriveClientType clientType, Session session) {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        try {
            String value;
            switch (clientType) {
                case WINDOWS:
                    {
                        LeanConfigurationService configService = getConfigService();
                        BrandedDriveVersionService versionService = BrandedDriveVersionServiceImpl.getInstance();
                        String branding = configService.getProperty(userId, contextId, DriveProperty.UPDATE_BRANDING);
                        if (branding != null && !branding.isEmpty()) {
                            value = versionService.getHardMinimumVersion(branding);
                        } else {
                            value = configService.getProperty(userId, contextId, DriveProperty.VERSION_WINDOWS_SOFT_MINIMUM);
                        }
                    }
                    break;
                case MAC_OS:
                    value = getConfigService().getProperty(userId, contextId, DriveProperty.VERSION_MACOS_SOFT_MINIMUM);
                    break;
                case ANDROID:
                    value = getConfigService().getProperty(userId, contextId, DriveProperty.VERSION_ANDROID_SOFT_MINIMUM);
                    break;
                case IOS:
                    value = getConfigService().getProperty(userId, contextId, DriveProperty.VERSION_IOS_SOFT_MINIMUM);
                    break;
                default:
                    return DriveClientVersion.VERSION_0;
            }
            return parseClientVersion(value);
        } catch (OXException e) {
            LoggerHolder.LOG.warn(e.getMessage(), e);
            return DriveClientVersion.VERSION_0;
        }
    }

    /**
     * Gets the (hard) minimum version limit for the supplied client type
     *
     * @param clientType The client type to get the limit for
     * @param session The current session
     * @return The configured limit, or {@link DriveClientVersion#VERSION_0} if not defined
     */
    public DriveClientVersion getHardMinimumVersion(DriveClientType clientType, Session session) {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        try {
            String value;
            switch (clientType) {
                case WINDOWS:
                    {
                        LeanConfigurationService configService = getConfigService();
                        BrandedDriveVersionService versionService = BrandedDriveVersionServiceImpl.getInstance();
                        String branding = configService.getProperty(userId, contextId, DriveProperty.UPDATE_BRANDING);
                        if (branding != null && !branding.isEmpty()) {
                            value = versionService.getHardMinimumVersion(branding);
                        } else {
                            value = configService.getProperty(userId, contextId, DriveProperty.VERSION_WINDOWS_HARD_MINIMUM);
                        }
                    }
                    break;
                case MAC_OS:
                    value = getConfigService().getProperty(userId, contextId, DriveProperty.VERSION_MACOS_HARD_MINIMUM);
                    break;
                case ANDROID:
                    value = getConfigService().getProperty(userId, contextId, DriveProperty.VERSION_ANDROID_HARD_MINIMUM);
                    break;
                case IOS:
                    value = getConfigService().getProperty(userId, contextId, DriveProperty.VERSION_IOS_HARD_MINIMUM);
                    break;
                default:
                    return DriveClientVersion.VERSION_0;
            }
            return parseClientVersion(value);
        } catch (OXException e) {
            LoggerHolder.LOG.warn(e.getMessage(), e);
            return DriveClientVersion.VERSION_0;
        }
    }

    /**
     * Parse a timespan including an optional unit
     *
     * @param interval The interval to parse
     * @param minimum The minimal value
     * @return The parsed timespan in milliseconds
     * @throws OXException
     */
    private long parseTimeSpan(String interval, int minimum) throws OXException {
        long cleanerInterval = -1L;
        try {
            cleanerInterval = TimeSpanParser.parseTimespan(interval).longValue();
        } catch (IllegalArgumentException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, interval);
        }
        if ((MILLIS_PER_HOUR * minimum) > cleanerInterval) {
            LoggerHolder.LOG.warn("The configured interval of ''{}'' is smaller than the allowed minimum of {}h. Falling back to ''{}h'' instead.", interval, Integer.valueOf(minimum), Integer.valueOf(minimum));
            cleanerInterval = MILLIS_PER_HOUR;
        }
        return cleanerInterval;
    }

    private LeanConfigurationService getConfigService() {
        LeanConfigurationService service = DriveServiceLookup.getService(LeanConfigurationService.class);
        if (null == service) {
            throw new IllegalStateException("LeanConfigurationService not available");
        }
        return service;
    }

}
