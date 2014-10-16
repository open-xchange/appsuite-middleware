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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.drive.management;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.drive.DriveClientType;
import com.openexchange.drive.DriveClientVersion;
import com.openexchange.drive.DriveConstants;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.Initialization;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link DriveConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveConfig implements Initialization {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveConfig.class);
    private static final DriveConfig instance = new DriveConfig();

    /**
     * Gets the drive configuration instance.
     *
     * @return The drive config instance
     */
    public static DriveConfig getInstance() {
        return instance;
    }

    private final AtomicBoolean started;

    private boolean useTempFolder;
    private long cleanerInterval;
    private long cleanerMaxAge;
    private int maxBandwidth;
    private int maxBandwidthPerClient;
    private int maxConcurrentSyncOperations;
    private String directLinkQuota;
    private String directLinkHelp;
    private Pattern excludedFilenamesPattern;
    private Pattern excludedDirectoriesPattern;
    private String shortProductName;
    private int minApiVersion;
    private int maxDirectoryActions;
    private int maxFileActions;
    private String directLinkFragmentsFile;
    private String directLinkFile;
    private int[] previewImageSize;
    private int[] thumbnailImageSize;
    private String imageLinkImageFile;
    private String imageLinkAudioFile;
    private String imageLinkDocumentFile;
    private String directLinkFragmentsDirectory;
    private String directLinkDirectory;
    private String uiWebPath;
    private String dispatcherPrefix;

    private EnumMap<DriveClientType, DriveClientVersion> softMinimumVersions;
    private EnumMap<DriveClientType, DriveClientVersion> hardMinimumVersions;

    /**
     * Initializes a new {@link DriveConfig}.
     */
    private DriveConfig() {
        super();
        this.started = new AtomicBoolean();
    }

    @Override
    public void start() throws OXException {
        if (false == started.compareAndSet(false, true)) {
            LOG.warn("Already started - aborting.");
            return;
        }
        /*
         * register properties
         */
        load(DriveServiceLookup.getService(ConfigurationService.class, true));
    }

    @Override
    public void stop() throws OXException {
        if (false == started.compareAndSet(true, false)) {
            LOG.warn("Not started - aborting.");
            return;
        }
    }

    /**
     * Gets the useTempFolder
     *
     * @return The useTempFolder
     */
    public boolean isUseTempFolder() {
        return useTempFolder;
    }

    /**
     * Gets the cleanerInterval
     *
     * @return The cleanerInterval
     */
    public long getCleanerInterval() {
        return cleanerInterval;
    }

    /**
     * Gets the cleanerMaxAge
     *
     * @return The cleanerMaxAge
     */
    public long getCleanerMaxAge() {
        return cleanerMaxAge;
    }

    /**
     * Gets the maxBandwidth
     *
     * @return The maxBandwidth
     */
    public int getMaxBandwidth() {
        return maxBandwidth;
    }

    /**
     * Gets the maxBandwidthPerClient
     *
     * @return The maxBandwidthPerClient
     */
    public int getMaxBandwidthPerClient() {
        return maxBandwidthPerClient;
    }

    /**
     * Gets the maxConcurrentSyncOperations
     *
     * @return The maxConcurrentSyncOperations
     */
    public int getMaxConcurrentSyncOperations() {
        return maxConcurrentSyncOperations;
    }

    /**
     * Gets the directLinkQuota
     *
     * @return The directLinkQuota
     */
    public String getDirectLinkQuota() {
        return directLinkQuota;
    }

    /**
     * Gets the directLinkHelp
     *
     * @return The directLinkHelp
     */
    public String getDirectLinkHelp() {
        return directLinkHelp;
    }

    /**
     * Gets the excludedFilenamesPattern
     *
     * @return The excludedFilenamesPattern
     */
    public Pattern getExcludedFilenamesPattern() {
        return excludedFilenamesPattern;
    }

    /**
     * Gets the excludedDirectoriesPattern
     *
     * @return The excludedDirectoriesPattern
     */
    public Pattern getExcludedDirectoriesPattern() {
        return excludedDirectoriesPattern;
    }

    /**
     * Gets the shortProductName
     *
     * @return The shortProductName
     */
    public String getShortProductName() {
        return shortProductName;
    }

    /**
     * Gets the minApiVersion
     *
     * @return The minApiVersion
     */
    public int getMinApiVersion() {
        return minApiVersion;
    }

    /**
     * Gets the maxDirectoryActions
     *
     * @return The maxDirectoryActions
     */
    public int getMaxDirectoryActions() {
        return maxDirectoryActions;
    }

    /**
     * Gets the maxFileActions
     *
     * @return The maxFileActions
     */
    public int getMaxFileActions() {
        return maxFileActions;
    }

    /**
     * Gets the directLinkFragmentsFile
     *
     * @return The directLinkFragmentsFile
     */
    public String getDirectLinkFragmentsFile() {
        return directLinkFragmentsFile;
    }

    /**
     * Gets the directLinkFile
     *
     * @return The directLinkFile
     */
    public String getDirectLinkFile() {
        return directLinkFile;
    }

    /**
     * Gets the thumbnailImageSize
     *
     * @return The thumbnailImageSize
     */
    public int[] getThumbnailImageSize() {
        return thumbnailImageSize;
    }

    /**
     * Gets the previewImageSize
     *
     * @return The previewImageSize
     */
    public int[] getPreviewImageSize() {
        return previewImageSize;
    }

    /**
     * Gets the imageLinkDocumentFile
     *
     * @return The imageLinkDocumentFile
     */
    public String getImageLinkDocumentFile() {
        return imageLinkDocumentFile;
    }

    /**
     * Gets the directLinkDirectory
     *
     * @return The directLinkDirectory
     */
    public String getDirectLinkDirectory() {
        return directLinkDirectory;
    }

    /**
     * Gets the directLinkFragmentsDirectory
     *
     * @return The directLinkFragmentsDirectory
     */
    public String getDirectLinkFragmentsDirectory() {
        return directLinkFragmentsDirectory;
    }

    /**
     * Gets the imageLinkAudioFile
     *
     * @return The imageLinkAudioFile
     */
    public String getImageLinkAudioFile() {
        return imageLinkAudioFile;
    }

    /**
     * Gets the imageLinkImageFile
     *
     * @return The imageLinkImageFile
     */
    public String getImageLinkImageFile() {
        return imageLinkImageFile;
    }

    /**
     * Gets the uiWebPath
     *
     * @return The uiWebPath
     */
    public String getUiWebPath() {
        return uiWebPath;
    }

    /**
     * Gets the dispatcherPrefix
     *
     * @return The dispatcherPrefix
     */
    public String getDispatcherPrefix() {
        return dispatcherPrefix;
    }

    /**
     * Loads all relevant drive properties from the configuration service.
     *
     * @param configService The configuration service
     * @throws OXException
     */
    private void load(ConfigurationService configService) throws OXException {
        /*
         * general
         */
        minApiVersion = configService.getIntProperty("com.openexchange.drive.minApiVersion", DriveConstants.DEFAULT_MIN_API_VERSION);
        shortProductName = configService.getProperty("com.openexchange.drive.shortProductName", "OX Drive");
        try {
            excludedFilenamesPattern = Pattern.compile(configService.getProperty("com.openexchange.drive.excludedFilesPattern",
                "thumbs\\.db|desktop\\.ini|\\.ds_store|icon\\\r|\\.msngr_hstr_data_.*\\.log"),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "com.openexchange.drive.excludedFilesPattern");
        }
        try {
            excludedDirectoriesPattern = Pattern.compile(configService.getProperty("com.openexchange.drive.excludedDirectoriesPattern",
                "^.*/\\.msngr_hstr_data$"), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "com.openexchange.drive.excludedDirectoriesPattern");
        }
        /*
         * temp cleaner
         */
        useTempFolder = configService.getBoolProperty("com.openexchange.drive.useTempFolder", true);
        final long MILLIS_PER_HOUR = 1000 * 60 * 60;
        String cleanerIntervalValue = configService.getProperty("com.openexchange.drive.cleaner.interval", "1D");
        try {
            cleanerInterval = TimeSpanParser.parseTimespan(cleanerIntervalValue);
        } catch (IllegalArgumentException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, cleanerIntervalValue);
        }
        if (MILLIS_PER_HOUR > cleanerInterval) {
            LOG.warn("The configured interval of ''{}'' is smaller than the allowed minimum of one hour. Falling back to ''1h'' instead.", cleanerIntervalValue);
            cleanerInterval = MILLIS_PER_HOUR;
        }
        String cleanerMaxAgeValue = configService.getProperty("com.openexchange.drive.cleaner.maxAge", "1D");
        try {
            cleanerMaxAge = TimeSpanParser.parseTimespan(cleanerMaxAgeValue);
        } catch (IllegalArgumentException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, cleanerMaxAgeValue);
        }
        if (MILLIS_PER_HOUR > cleanerMaxAge) {
            LOG.warn("The configured interval of ''{}'' is smaller than the allowed minimum of one hour. Falling back to ''1h'' instead.", cleanerMaxAgeValue);
            cleanerMaxAge = MILLIS_PER_HOUR;
        }
        /*
         * throttling
         */
        String maxBandwidthValue = configService.getProperty("com.openexchange.drive.maxBandwidth");
        maxBandwidth = Strings.isEmpty(maxBandwidthValue) || "-1".equals(maxBandwidthValue) ? -1 : parseBytes(maxBandwidthValue);
        String maxBandwidthPerClientValue = configService.getProperty("com.openexchange.drive.maxBandwidthPerClient");
        maxBandwidthPerClient = Strings.isEmpty(maxBandwidthPerClientValue) || "-1".equals(maxBandwidthPerClientValue) ?
            -1 : parseBytes(maxBandwidthPerClientValue);
        maxConcurrentSyncOperations = configService.getIntProperty("com.openexchange.drive.maxConcurrentSyncOperations", -1);
        maxDirectoryActions = configService.getIntProperty("com.openexchange.drive.maxDirectoryActions", 1000);
        maxFileActions = configService.getIntProperty("com.openexchange.drive.maxFileActions", 500);
        /*
         * direct link templates
         */
        directLinkQuota = configService.getProperty("com.openexchange.drive.directLinkQuota", "[protocol]://[hostname]");
        directLinkHelp = configService.getProperty("com.openexchange.drive.directLinkHelp",
            "[protocol]://[hostname]/[uiwebpath]/help/[locale]/index.html");
        directLinkFragmentsFile = configService.getProperty("com.openexchange.drive.directLinkFragmentsFile",
            "m=infostore&f=[folder]&i=[object]");
        directLinkFile = configService.getProperty("com.openexchange.drive.directLinkFile",
            "[protocol]://[hostname]/[uiwebpath]#[filefragments]");
        previewImageSize = parseDimensions(configService.getProperty("com.openexchange.drive.previewImageSize", "800x800"));
        thumbnailImageSize = parseDimensions(configService.getProperty("com.openexchange.drive.thumbnailImageSize", "100x100"));
        imageLinkImageFile = configService.getProperty("com.openexchange.drive.imageLinkImageFile",
            "[protocol]://[hostname]/[dispatcherPrefix]/files?action=document&folder=[folder]&id=[object]&version=[version]&" +
            "delivery=download&scaleType=contain&width=[width]&height=[height]&rotate=true");
        imageLinkAudioFile = configService.getProperty("com.openexchange.drive.imageLinkAudioFile",
            "[protocol]://[hostname]/[dispatcherPrefix]/image/file/mp3Cover?folder=[folder]&id=[object]&version=[version]&" +
            "delivery=download&scaleType=contain&width=[width]&height=[height]");
        imageLinkDocumentFile = configService.getProperty("com.openexchange.drive.imageLinkDocumentFile",
            "[protocol]://[hostname]/[dispatcherPrefix]/files?action=document&format=preview_image&folder=[folder]&id=[object]&" +
            "version=[version]&delivery=download&scaleType=contain&width=[width]&height=[height]");
        directLinkFragmentsDirectory = configService.getProperty("com.openexchange.drive.directLinkFragmentsDirectory",
            "m=infostore&f=[folder]");
        directLinkDirectory = configService.getProperty("com.openexchange.drive.directLinkDirectory",
            "[protocol]://[hostname]/[uiwebpath]#[directoryfragments]");
        uiWebPath = configService.getProperty("com.openexchange.UIWebPath", "/ox6/index.html");
        dispatcherPrefix = configService.getProperty("com.openexchange.dispatcher.prefix", "ajax");
        /*
         * version restrictions
         */
        softMinimumVersions = new EnumMap<DriveClientType, DriveClientVersion>(DriveClientType.class);
        hardMinimumVersions = new EnumMap<DriveClientType, DriveClientVersion>(DriveClientType.class);
        softMinimumVersions.put(DriveClientType.WINDOWS, parseClientVersion(
            configService.getProperty("com.openexchange.drive.version.windows.softMinimum",
            configService.getProperty("com.openexchange.drive.windows.version", "0"))));
        hardMinimumVersions.put(DriveClientType.WINDOWS, parseClientVersion(
            configService.getProperty("com.openexchange.drive.version.windows.hardMinimum",
            configService.getProperty("com.openexchange.drive.windows.minimumVersion", "0"))));
        softMinimumVersions.put(DriveClientType.MAC_OS,
            parseClientVersion(configService.getProperty("com.openexchange.drive.version.macos.softMinimum", "0")));
        hardMinimumVersions.put(DriveClientType.MAC_OS,
            parseClientVersion(configService.getProperty("com.openexchange.drive.version.macos.hardMinimum", "0")));
        softMinimumVersions.put(DriveClientType.ANDROID,
            parseClientVersion(configService.getProperty("com.openexchange.drive.version.android.softMinimum", "0")));
        softMinimumVersions.put(DriveClientType.IOS,
            parseClientVersion(configService.getProperty("com.openexchange.drive.version.ios.softMinimum", "0")));
        hardMinimumVersions.put(DriveClientType.ANDROID,
            parseClientVersion(configService.getProperty("com.openexchange.drive.version.android.hardMinimum", "0")));
        hardMinimumVersions.put(DriveClientType.IOS,
            parseClientVersion(configService.getProperty("com.openexchange.drive.version.ios.hardMinimum", "0")));
    }

    private static DriveClientVersion parseClientVersion(String value) throws OXException {
        try {
            return Strings.isEmpty(value) ? DriveClientVersion.VERSION_0 : new DriveClientVersion(value);
        } catch (IllegalArgumentException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(value);
        }
    }

    private static int[] parseDimensions(String value) throws OXException {
        int idx = value.indexOf('x');
        if (1 > idx) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(value);
        }
        try {
            return new int[] { Integer.parseInt(value.substring(0, idx)), Integer.parseInt(value.substring(idx + 1)) };
        } catch (NumberFormatException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(1, value);
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
     * @return The configured limit, or {@link DriveClientVersion#VERSION_0} if not defined
     */
    public DriveClientVersion getSoftMinimumVersion(DriveClientType clientType) {
        DriveClientVersion version = softMinimumVersions.get(clientType);
        return null != version ? version : DriveClientVersion.VERSION_0;
    }

    /**
     * Gets the (hard) minimum version limit for the supplied client type
     *
     * @param clientType The client type to get the limit for
     * @return The configured limit, or {@link DriveClientVersion#VERSION_0} if not defined
     */
    public DriveClientVersion getHardMinimumVersion(DriveClientType clientType) {
        DriveClientVersion version = hardMinimumVersions.get(clientType);
        return null != version ? version : DriveClientVersion.VERSION_0;
    }

}
