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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveConstants;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.server.Initialization;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link DriveConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveConfig implements Initialization {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DriveConfig.class));
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
    private final List<DriveProperty> driveProperties;

    private boolean useTempFolder;
    private long cleanerInterval;
    private long cleanerMaxAge;
    private int maxBandwidth;
    private int maxBandwidthPerClient;
    private int maxConcurrentSyncOperations;
    private String directLinkQuota;
    private boolean diagnostics;
    private Set<String> diagnosticsUsers;
    private Pattern excludedFilenamesPattern;
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

    /**
     * Initializes a new {@link DriveConfig}.
     */
    private DriveConfig() {
        super();
        this.started = new AtomicBoolean();
        this.driveProperties = initProperties(this);
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
        ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class, true);
        for (DriveProperty driveProperty : driveProperties) {
            driveProperty.register(configService);
        }
    }

    @Override
    public void stop() throws OXException {
        if (false == started.compareAndSet(true, false)) {
            LOG.warn("Not started - aborting.");
            return;
        }
        /*
         * unregister properties
         */
        if (null != driveProperties && 0 < driveProperties.size()) {
            ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class, false);
            if (null != configService) {
                for (DriveProperty driveProperty : driveProperties) {
                    driveProperty.unregister(configService);
                }
            } else {
                LOG.warn("Unable to access config service, unable to unregister property listeners");
            }
        }
    }

    /**
     * Gets a value indicating if the supplied session is configured to write the (server-side) diagnostics log or not.
     *
     * @param session The session to check
     * @return <code>true</code> if diagnostic logging is enabled for the session, <code>false</code>, otherwise
     */
    public boolean isDiagnostics(ServerSession session) {
        if (isDiagnostics()) {
            return true;
        } else if (null != session && 0 < diagnosticsUsers.size()) {
            return diagnosticsUsers.contains(session.getLogin()) ||
                diagnosticsUsers.contains("*@" + session.getContextId()) ||
                diagnosticsUsers.contains(session.getUserId() + '@' + session.getContextId()) ||
                diagnosticsUsers.contains(session.getLoginName() + '@' + session.getContextId()) ||
                diagnosticsUsers.contains(session.getUserId() + '@' + session.getContext().getName()) ||
                diagnosticsUsers.contains(session.getLoginName() + '@' + session.getContext().getName()) ||
                diagnosticsUsers.contains("*@" + session.getContext().getName())
            ;
        }
        return false;
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
     * Sets the useTempFolder
     *
     * @param useTempFolder The useTempFolder to set
     */
    public void setUseTempFolder(boolean useTempFolder) {
        this.useTempFolder = useTempFolder;
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
     * Sets the cleanerInterval
     *
     * @param cleanerInterval The cleanerInterval to set
     */
    public void setCleanerInterval(long cleanerInterval) {
        this.cleanerInterval = cleanerInterval;
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
     * Sets the cleanerMaxAge
     *
     * @param cleanerMaxAge The cleanerMaxAge to set
     */
    public void setCleanerMaxAge(long cleanerMaxAge) {
        this.cleanerMaxAge = cleanerMaxAge;
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
     * Sets the maxBandwidth
     *
     * @param maxBandwidth The maxBandwidth to set
     */
    public void setMaxBandwidth(int maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
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
     * Sets the maxBandwidthPerClient
     *
     * @param maxBandwidthPerClient The maxBandwidthPerClient to set
     */
    public void setMaxBandwidthPerClient(int maxBandwidthPerClient) {
        this.maxBandwidthPerClient = maxBandwidthPerClient;
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
     * Sets the maxConcurrentSyncOperations
     *
     * @param maxConcurrentSyncOperations The maxConcurrentSyncOperations to set
     */
    public void setMaxConcurrentSyncOperations(int maxConcurrentSyncOperations) {
        this.maxConcurrentSyncOperations = maxConcurrentSyncOperations;
    }

    /**
     * Gets the diagnostics
     *
     * @return The diagnostics
     */
    public boolean isDiagnostics() {
        return diagnostics;
    }

    /**
     * Sets the diagnostics
     *
     * @param diagnostics The diagnostics to set
     */
    public void setDiagnostics(boolean diagnostics) {
        this.diagnostics = diagnostics;
    }

    /**
     * Gets the diagnosticsUsers
     *
     * @return The diagnosticsUsers
     */
    public Set<String> getDiagnosticsUsers() {
        return Collections.unmodifiableSet(diagnosticsUsers);
    }

    public boolean addDiagnisticsUser(String user) {
        return diagnosticsUsers.add(user);
    }

    public boolean removeDiagnisticsUser(String user) {
        return diagnosticsUsers.remove(user);
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
     * Sets the directLinkQuota
     *
     * @param directLinkQuota The directLinkQuota to set
     */
    public void setDirectLinkQuota(String directLinkQuota) {
        this.directLinkQuota = directLinkQuota;
    }

    /**
     * Initializes all drive properties for the supplied {@link DriveConfig} instance.
     *
     * @param config The parent drive config
     * @return The drive properties
     */
    private static List<DriveProperty> initProperties(final DriveConfig config) {
        List<DriveProperty> properties = new ArrayList<DriveProperty>();
        properties.add(new DriveProperty("com.openexchange.drive.useTempFolder", "true", false) {

            @Override
            protected void set(String value) {
                config.useTempFolder = Boolean.valueOf(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.cleaner.interval", "1D", false) {

            @Override
            protected void set(String value) {
                config.cleanerInterval = TimeSpanParser.parseTimespan(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.cleaner.maxAge", "1D", false) {

            @Override
            protected void set(String value) {
                config.cleanerMaxAge = TimeSpanParser.parseTimespan(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.maxBandwidth", "-1", false) {

            @Override
            protected void set(String value) {
                config.maxBandwidth = Strings.isEmpty(value) ? -1 : parseBytes(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.maxBandwidthPerClient", "-1", false) {

            @Override
            protected void set(String value) {
                config.maxBandwidthPerClient = Strings.isEmpty(value) ? -1 : parseBytes(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.maxConcurrentSyncOperations", "-1", false) {

            @Override
            protected void set(String value) {
                config.maxConcurrentSyncOperations = Strings.isEmpty(value) ? -1 : Integer.valueOf(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.directLinkQuota", "[protocol]://[hostname]", false) {

            @Override
            protected void set(String value) {
                config.directLinkQuota = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.diagnostics", "false", true) {

            @Override
            protected void set(String value) {
                config.diagnostics = Boolean.valueOf(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.diagnosticsUsers", null, true) {

            @Override
            protected void set(String value) {
                Set<String> diagnosticsUsers = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
                if (false == Strings.isEmpty(value)) {
                    diagnosticsUsers.addAll(Strings.splitAndTrim(value, ","));
                }
                config.diagnosticsUsers = diagnosticsUsers;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.excludedFilesPattern", "thumbs\\.db|desktop\\.ini|\\.ds_store|icon\\\r", false) {

            @Override
            protected void set(String value) {
                config.excludedFilenamesPattern = Pattern.compile(value, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.shortProductName", "OX Drive", false) {

            @Override
            protected void set(String value) {
                config.shortProductName = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.minApiVersion", String.valueOf(DriveConstants.DEFAULT_MIN_API_VERSION), false) {

            @Override
            protected void set(String value) {
                config.minApiVersion = Strings.isEmpty(value) ? DriveConstants.DEFAULT_MIN_API_VERSION : Integer.valueOf(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.maxDirectoryActions", "1000", false) {

            @Override
            protected void set(String value) {
                config.maxDirectoryActions = Strings.isEmpty(value) ? 1000 : Integer.valueOf(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.maxFileActions", "500", false) {

            @Override
            protected void set(String value) {
                config.maxFileActions = Strings.isEmpty(value) ? 500 : Integer.valueOf(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.directLinkFragmentsFile", "m=infostore&f=[folder]&i=[object]", false) {

            @Override
            protected void set(String value) {
                config.directLinkFragmentsFile = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.directLinkFile", "[protocol]://[hostname]/[uiwebpath]#[filefragments]", false) {

            @Override
            protected void set(String value) {
                config.directLinkFile = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.previewImageSize", "800x800", false) {

            @Override
            protected void set(String value) {
                config.previewImageSize = parseDimensions(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.thumbnailImageSize", "100x100", false) {

            @Override
            protected void set(String value) {
                config.thumbnailImageSize = parseDimensions(value);
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.imageLinkImageFile", "[protocol]://[hostname]/[dispatcherPrefix]/files" +
            "?action=document&folder=[folder]&id=[object]&version=[version]&delivery=download&scaleType=contain" +
            "&width=[width]&height=[height]&rotate=true", false) {

            @Override
            protected void set(String value) {
                config.imageLinkImageFile = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.imageLinkAudioFile", "[protocol]://[hostname]/[dispatcherPrefix]/image/file/" +
            "mp3Cover?folder=[folder]&id=[object]&version=[version]&delivery=download&scaleType=contain&width=[width]&height=[height]", false) {

            @Override
            protected void set(String value) {
                config.imageLinkAudioFile = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.imageLinkDocumentFile", "[protocol]://[hostname]/[dispatcherPrefix]/files?action=" +
                    "document&format=preview_image&folder=[folder]&id=[object]&version=[version]&delivery=download&scaleType=contain" +
                    "&width=[width]&height=[height]", false) {

            @Override
            protected void set(String value) {
                config.imageLinkDocumentFile = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.directLinkFragmentsDirectory", "m=infostore&f=[folder]", false) {

            @Override
            protected void set(String value) {
                config.directLinkFragmentsDirectory = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.drive.directLinkDirectory", "[protocol]://[hostname]/[uiwebpath]#[directoryfragments]", false) {

            @Override
            protected void set(String value) {
                config.directLinkDirectory = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.UIWebPath", "/ox6/index.html", false) {

            @Override
            protected void set(String value) {
                config.uiWebPath = value;
            }
        });
        properties.add(new DriveProperty("com.openexchange.dispatcher.prefix", "ajax", false) {

            @Override
            protected void set(String value) {
                config.dispatcherPrefix = value;
            }
        });


        return properties;
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
     * Sets the excludedFilenamesPattern
     *
     * @param excludedFilenamesPattern The excludedFilenamesPattern to set
     */
    public void setExcludedFilenamesPattern(Pattern excludedFilenamesPattern) {
        this.excludedFilenamesPattern = excludedFilenamesPattern;
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
     * Sets the shortProductName
     *
     * @param shortProductName The shortProductName to set
     */
    public void setShortProductName(String shortProductName) {
        this.shortProductName = shortProductName;
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
     * Sets the minApiVersion
     *
     * @param minApiVersion The minApiVersion to set
     */
    public void setMinApiVersion(int minApiVersion) {
        this.minApiVersion = minApiVersion;
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
     * Sets the maxDirectoryActions
     *
     * @param maxDirectoryActions The maxDirectoryActions to set
     */
    public void setMaxDirectoryActions(int maxDirectoryActions) {
        this.maxDirectoryActions = maxDirectoryActions;
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
     * Sets the maxFileActions
     *
     * @param maxFileActions The maxFileActions to set
     */
    public void setMaxFileActions(int maxFileActions) {
        this.maxFileActions = maxFileActions;
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
     * Sets the directLinkFragmentsFile
     *
     * @param directLinkFragmentsFile The directLinkFragmentsFile to set
     */
    public void setDirectLinkFragmentsFile(String directLinkFragmentsFile) {
        this.directLinkFragmentsFile = directLinkFragmentsFile;
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
     * Sets the directLinkFile
     *
     * @param directLinkFile The directLinkFile to set
     */
    public void setDirectLinkFile(String directLinkFile) {
        this.directLinkFile = directLinkFile;
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
     * Sets the thumbnailImageSize
     *
     * @param thumbnailImageSize The thumbnailImageSize to set
     */
    public void setThumbnailImageSize(int[] thumbnailImageSize) {
        this.thumbnailImageSize = thumbnailImageSize;
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
     * Sets the previewImageSize
     *
     * @param previewImageSize The previewImageSize to set
     */
    public void setPreviewImageSize(int[] previewImageSize) {
        this.previewImageSize = previewImageSize;
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
     * Sets the imageLinkDocumentFile
     *
     * @param imageLinkDocumentFile The imageLinkDocumentFile to set
     */
    public void setImageLinkDocumentFile(String imageLinkDocumentFile) {
        this.imageLinkDocumentFile = imageLinkDocumentFile;
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
     * Sets the directLinkDirectory
     *
     * @param directLinkDirectory The directLinkDirectory to set
     */
    public void setDirectLinkDirectory(String directLinkDirectory) {
        this.directLinkDirectory = directLinkDirectory;
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
     * Sets the directLinkFragmentsDirectory
     *
     * @param directLinkFragmentsDirectory The directLinkFragmentsDirectory to set
     */
    public void setDirectLinkFragmentsDirectory(String directLinkFragmentsDirectory) {
        this.directLinkFragmentsDirectory = directLinkFragmentsDirectory;
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
     * Sets the imageLinkAudioFile
     *
     * @param imageLinkAudioFile The imageLinkAudioFile to set
     */
    public void setImageLinkAudioFile(String imageLinkAudioFile) {
        this.imageLinkAudioFile = imageLinkAudioFile;
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
     * Sets the imageLinkImageFile
     *
     * @param imageLinkImageFile The imageLinkImageFile to set
     */
    public void setImageLinkImageFile(String imageLinkImageFile) {
        this.imageLinkImageFile = imageLinkImageFile;
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
     * Sets the uiWebPath
     *
     * @param uiWebPath The uiWebPath to set
     */
    public void setUiWebPath(String uiWebPath) {
        this.uiWebPath = uiWebPath;
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
     * Sets the dispatcherPrefix
     *
     * @param dispatcherPrefix The dispatcherPrefix to set
     */
    public void setDispatcherPrefix(String dispatcherPrefix) {
        this.dispatcherPrefix = dispatcherPrefix;
    }

}
