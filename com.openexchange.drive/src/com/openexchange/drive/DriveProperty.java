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

package com.openexchange.drive;

import com.openexchange.config.lean.Property;

/**
 * {@link DriveProperty}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.2
 */
public enum DriveProperty implements Property {

    /**
     * Short product name as used in the version comment string inserted for drive
     * uploads, e.g. "Uploaded with OX Drive (Ottos Laptop)".
     * Default: "OX Drive"
     */
    SHORT_PRODUCT_NAME("shortProductName", "OX Drive"),

    /**
     * Specifies whether the synchronization logic will make use of a folder named
     * ".drive" below the root synchronization folder or not. If enabled, this
     * folder is used to store temporary uploads and removed files, which usually
     * leads to a better user experience since previously synchronized files can
     * be restored from there for example. If not, removed files are not kept, and
     * uploads are performed directly in the target folder.
     * Default: true
     */
    USE_TEMP_FOLDER("useTempFolder", true),

    /**
     * Configures the interval between runs of the cleaner process for the
     * temporary ".drive" folder. A cleaner run is only initiated if the
     * synchronization is idle, i.e. the last synchronization resulted in no
     * actions to be performed, and the last run was before the configured
     * interval. The value can be defined using units of measurement: "D" (=days),
     * "W" (=weeks) and "H" (=hours).
     * Default: "1D"
     */
    CLEANER_INTERVAL("cleaner.interval", "1D"),

    /**
     * Defines the maximum age of files and directories to be kept inside the
     * temporary ".drive" folder. Files or directories that were last modified
     * before the configured age are deleted during the next run of the cleaner
     * process. The value can be defined using units of measurement: "D" (=days),
     * "W" (=weeks) and "H" (=hours).
     * Default: "1D"
     */
    CLEANER_MAX_AGE("cleaner.maxAge", "1D"),

    /**
     * Defines the interval of a periodic background task that performs cleanup
     * operations for cached checksums in the database. The task is executed only
     * once per interval in the cluster, so this value should be equally defined
     * on each node.
     * The value can be defined using units of measurement: "D" (=days),
     * "W" (=weeks) and "H" (=hours). Defaults to "1D" (one day), with a minimum
     * of "1H" (one hour). A value of "0" disables the periodic background task.
     */
    CHECKSUM_CLEANER_INTERVAL("checksum.cleaner.interval", "1D"),

    /**
     * Defines the timespan after which an unused checksum should be removed from
     * the database cache.
     * The value can be defined using units of measurement: "D" (=days),
     * "W" (=weeks) and "H" (=hours). Defaults to "4W" (four weeks), with a minimum
     * of "1D" (one day).
     */
    CHECKSUM_CLEANER_MAXAGE("checksum.cleaner.maxAge", "4W"),

    /**
     * Allows to limit the maximum used bandwidth for all downloads. If
     * configured, downloads via the drive module handled by this backend node will
     * not exceed the configured bandwidth. The available bandwidth is defined as
     * the number of allowed bytes per second, where the byte value can be
     * specified with one of the units "B" (bytes), "kB" (kilobyte), "MB"
     * (Megabyte) or "GB" (Gigabyte), e.g. "10 MB". Must fit into the "Integer"
     * range, i.e. the configured number of bytes has to be be smaller than 2^31.
     * Default: -1
     */
    MAX_BANDWIDTH("maxBandwidth", "-1"),

    /**
     * Allows to limit the maximum used bandwidth for client downloads within the
     * same session. If configured, downloads originating in the same session via
     * the drive module handled by this backend node will not exceed the
     * configured bandwidth. The available bandwidth is defined as the number of
     * allowed bytes per second, where the byte value can be specified with one of
     * the units "B" (bytes), "kB" (kilobyte), "MB" (Megabyte) or "GB" (Gigabyte),
     * e.g. "500 kB". Must fit into the "Integer" range, i.e. the configured
     * number of bytes has to be be smaller than 2^31.
     * Default: -1
     */
    MAX_BANDWIDTH_PER_CLIENT("maxBandwidthPerClient", "-1"),

    /**
     * Specifies the maximum allowed number of synchronization operations, i.e.
     * all requests to the "drive" module apart from up- and downloads, that the
     * server accepts concurrently. While the limit is reached, further
     * synchronization requests are rejected in a HTTP 503 manner (service
     * unavailable), and the client is instructed to try again at a later time.
     * Default: -1
     */
    MAX_CONCURRENT_SYNC_OPERATIONS("maxConcurrentSyncOperations", -1),

    /**
     * Defines the maximum number of synchronizable directories per root folder. A
     * value of "-1" disables the limitation.
     * Default: 65535 (2^16 - 1)
     */
    MAX_DIRECTORIES("maxDirectories", 65535),

    /**
     * Defines the maximum number of synchronizable files per root folder. A
     * value of "-1" disables the limitation.
     * Default: 65535 (2^16 - 1)
     */
    MAX_FILES_PER_DIRECTORY("maxFilesPerDirectory", 65535),

    /**
     * Configures a list of allowed file storage services where synchronization via
     * OX Drive should be enabled. The services must be defined in a comma-
     * separated list of their unique identifiers.
     * By default, only the default "com.openexchange.infostore" service is listed
     * here.
     */
    ENABLED_SERVICES("enabledServices", "com.openexchange.infostore"),

    /**
     * Allows to exclude specific root folders from OX Drive synchronization
     * explicitly. Excluded folders may not be used as root folder for the
     * synchronization, however, this does not apply to their subfolders
     * automatically.
     * Excluded folders should be specified in a comma-separated list of their
     * unique identifiers. Typical candidates for the blacklist would be folder 15
     * (the "public folders" root) or folder 10 (the "shared folders" root) in
     * large enterprise installations.
     * Default: no default
     */
    EXCLUDED_FOLDERS("excludedFolders", null),

    /**
     * Configures the pattern for a direct link to manage a user's quota. Text in
     * brackets is replaced dynamically during link generation in the backend,
     * however, it's still possible to overwrite them here with a static value, or
     * even define an arbitrary URL here.
     * [protocol] is replaced automatically with the protocol used by the client
     * (typically "http" or "https").
     * [hostname] should be replaced with the server's canonical host name (if not,
     * the server tries to determine the hostname on it's own), [uiwebpath] is
     * replaced with the value of "com.openexchange.UIWebPath" as defined in
     * "server.properties", while [dispatcherPrefix] is replaced with the value of
     * "com.openexchange.dispatcher.prefix" ("server.properties", too).
     * [contextid], [userid] and [login] are replaced to reflect the values of the
     * current user.
     * Default: "[protocol]://[hostname]"
     */
    DIRECT_LINK_QUOTA("directLinkQuota", "[protocol]://[hostname]"),

    /**
     * Configures the pattern for a direct link to the online help. This serves as
     * target for the "Help" section in the client applications. Text in brackets
     * is replaced dynamically during link generation in the backend, however, it's
     * still possible to overwrite them here with a static value, or even define an
     * arbitrary URL here.
     * [protocol] is replaced automatically with the protocol used by the client
     * (typically "http" or "https").
     * [hostname] should be replaced with the server's canonical host name (if not,
     * the server tries to determine the hostname on it's own), [uiwebpath] is
     * replaced with the value of "com.openexchange.UIWebPath" as defined in
     * "server.properties", while [dispatcherPrefix] is replaced with the value of
     * "com.openexchange.dispatcher.prefix" ("server.properties", too).
     * [contextid], [userid] and [login] are replaced to reflect the values of the
     * current user.
     * Default: "[protocol]://[hostname]/[uiwebpath]/help-drive/l10n/[locale]/index.html"
     */
    DIRECT_LINK_HELP("directLinkHelp", "[protocol]://[hostname]/[uiwebpath]/help-drive/l10n/[locale]/index.html"),

    /**
     * The following properties allow the configuration of version restrictions for
     * the supported clients. For each client (Windows, Mac OS, iOS and Android),
     * two restrictions can be set. First, a "soft" limit that has informational
     * character only, i.e. the client is just informed about an available update
     * when identifying with a lower version number. Second, the "hard" limit will
     * restrict further synchronization of clients that identify themselves with a
     * lower version number.
     * Default: no default
     */
    VERSION_WINDOWS_SOFT_MINIMUM("version.windows.softMinimum", null),

    /**
     * The following properties allow the configuration of version restrictions for
     * the supported clients. For each client (Windows, Mac OS, iOS and Android),
     * two restrictions can be set. First, a "soft" limit that has informational
     * character only, i.e. the client is just informed about an available update
     * when identifying with a lower version number. Second, the "hard" limit will
     * restrict further synchronization of clients that identify themselves with a
     * lower version number.
     * Default: no default
     */
    VERSION_WINDOWS_HARD_MINIMUM("version.windows.hardMinimum", null),

    /**
     * The following properties allow the configuration of version restrictions for
     * the supported clients. For each client (Windows, Mac OS, iOS and Android),
     * two restrictions can be set. First, a "soft" limit that has informational
     * character only, i.e. the client is just informed about an available update
     * when identifying with a lower version number. Second, the "hard" limit will
     * restrict further synchronization of clients that identify themselves with a
     * lower version number.
     * Default: no default
     */
    VERSION_MACOS_SOFT_MINIMUM("version.macos.softMinimum", null),

    /**
     * The following properties allow the configuration of version restrictions for
     * the supported clients. For each client (Windows, Mac OS, iOS and Android),
     * two restrictions can be set. First, a "soft" limit that has informational
     * character only, i.e. the client is just informed about an available update
     * when identifying with a lower version number. Second, the "hard" limit will
     * restrict further synchronization of clients that identify themselves with a
     * lower version number.
     * Default: no default
     */
    VERSION_MACOS_HARD_MINIMUM("version.macOS.hardMinimum", null),

    /**
     * The following properties allow the configuration of version restrictions for
     * the supported clients. For each client (Windows, Mac OS, iOS and Android),
     * two restrictions can be set. First, a "soft" limit that has informational
     * character only, i.e. the client is just informed about an available update
     * when identifying with a lower version number. Second, the "hard" limit will
     * restrict further synchronization of clients that identify themselves with a
     * lower version number.
     * Default: no default
     */
    VERSION_IOS_SOFT_MINIMUM("version.ios.softMinimum", null),

    /**
     * The following properties allow the configuration of version restrictions for
     * the supported clients. For each client (Windows, Mac OS, iOS and Android),
     * two restrictions can be set. First, a "soft" limit that has informational
     * character only, i.e. the client is just informed about an available update
     * when identifying with a lower version number. Second, the "hard" limit will
     * restrict further synchronization of clients that identify themselves with a
     * lower version number.
     * Default: no default
     */
    VERSION_IOS_HARD_MINIMUM("version.ios.hardMinimum", null),

    /**
     * The following properties allow the configuration of version restrictions for
     * the supported clients. For each client (Windows, Mac OS, iOS and Android),
     * two restrictions can be set. First, a "soft" limit that has informational
     * character only, i.e. the client is just informed about an available update
     * when identifying with a lower version number. Second, the "hard" limit will
     * restrict further synchronization of clients that identify themselves with a
     * lower version number.
     * Default: no default
     */
    VERSION_ANDROID_SOFT_MINIMUM("version.android.softMinimum", null),

    /**
     * The following properties allow the configuration of version restrictions for
     * the supported clients. For each client (Windows, Mac OS, iOS and Android),
     * two restrictions can be set. First, a "soft" limit that has informational
     * character only, i.e. the client is just informed about an available update
     * when identifying with a lower version number. Second, the "hard" limit will
     * restrict further synchronization of clients that identify themselves with a
     * lower version number.
     * Default: no default
     */
    VERSION_ANDROID_HARD_MINIMUM("version.android.hardMinimum", null),

    /**
     * Configures whether blocking long polling for pushing synchronization events
     * to clients may be used as fallback when no other long polling handlers are
     * available due to missing support of the HTTP service. Handling long polling
     * in a blocking manner consumes a server thread, and should therefore only be
     * enabled for testing purposes.
     * Default: false
     */
    EVENTS_BLOCKING_LONG_POLLING_ENABLED("events.blockingLongPolling.enabled", false),

    /**
     * The name of the system wide drive branding identifier
     * This name must be equal to the name of one of the subfolder's under com.openexchange.drive.updater.path
     * Default: "generic"
     */
    UPDATE_BRANDING("update.branding", "generic"),

    /**
     * The minimum expected API version the client has to support
     * Default: 1
     */
    MIN_API_VERSION("minApiVersion", 1),

    /**
     * REGEX-pattern to filter file names to exclude from sync
     * Default: "thumbs\\.db|desktop\\.ini|\\.ds_store|icon\\\r|\\.msngr_hstr_data_.*\\.log"
     */
    EXCLUDED_FILES_PATTERN("excludedFilesPattern", "thumbs\\.db|desktop\\.ini|\\.ds_store|icon\\\r|\\.msngr_hstr_data_.*\\.log"),

    /**
     * REGEX-pattern to filter d names to exclude from sync
     * Default: TODO: find a way to put default value here...
     */
    EXCLUDED_DIRECTORIES_PATTERN("excludedDirectoriesPattern", "^/\\.drive$|^.*/\\.msngr_hstr_data$|^.*/\\.drive-meta(?:$|/.*)"),

    /**
     * Gets the maximum number of actions to be evaluated per synchronization request. Any further open actions will need to be handled in
     * consecutive synchronizations. A smaller value will lead to faster responses for the client and less resource utilization on the
     * backend, but increases the chance of rename- and move-optimizations not being detected.
     * Default: 1000
     */
    MAX_DIRECTORY_ACTIONS("maxDirectoryActions", 1000),

    /**
     * Gets the maximum number of actions to be evaluated per synchronization request. Any further open actions will need to be handled in
     * consecutive synchronizations. A smaller value will lead to faster responses for the client and less resource utilization on the
     * backend, but increases the chance of rename- and move-optimizations not being detected.
     * Default: 500
     */
    MAX_FILE_ACTIONS("maxFileActions", 500),

    /**
     * Template for direct link fragments for a file.
     * Default: "m=infostore&f=[folder]&i=[object]"
     */
    DIRECT_LINK_FRAGMENTS_FILE("directLinkFragmentsFile", "m=infostore&f=[folder]&i=[object]"),

    /**
     * Template for a ready-to-use direct link for a file.
     * Default: "[protocol]://[hostname]/[uiwebpath]#[filefragments]"
     */
    DIRECT_LINK_FILE("directLinkFile", "[protocol]://[hostname]/[uiwebpath]#[filefragments]"),

    /**
     * Template for a direct link to jump to a file.
     * Default: "[protocol]://[hostname]/[uiwebpath]#[app]&[folder]&[id]"
     */
    JUMP_LINK("jumpLink", "[protocol]://[hostname]/[uiwebpath]#[app]&[folder]&[id]"),

    /**
     * Preview images' size
     * Default: "1600x1600"
     */
    PREVIEW_IMAGE_SIZE("previewImageSize", "1600x1600"),

    /**
     * Thumbnails' size
     * Default: "200x150"
     */
    THUMBNAIL_IMAGE_SIZE("thumbnailImageSize", "200x150"),

    /**
     * Template for a link to generate a preview for an image file
     * Default:
     * "[protocol]://[hostname]/[dispatcherPrefix]/files?action=document&folder=[folder]&id=[object]&version=[version]&context=[contextid]&user=[userid]&delivery=download&scaleType=contain&width=[width]&height=[height]&shrinkOnly=true&rotate=true"
     */
    IMAGE_LINK_IMAGE_FILE("imageLinkImageFile", "[protocol]://[hostname]/[dispatcherPrefix]/files?action=document&" +
        "folder=[folder]&id=[object]&version=[version]&context=[contextid]&user=[userid]&" +
        "delivery=download&scaleType=contain&width=[width]&height=[height]&shrinkOnly=true&rotate=true"),

    /**
     * Template for a link to generate a preview for an audio file
     * Default: "[protocol]://[hostname]/[dispatcherPrefix]/image/file/mp3Cover?folder=[folder]&id=[object]&version=[version]&context=[contextid]&user=[userid]&delivery=download&scaleType=contain&width=[width]&height=[height]")
     */
    IMAGE_LINK_AUDIO_FILE("imageLinkAudioFile", "[protocol]://[hostname]/[dispatcherPrefix]/image/file/mp3Cover?" +
        "folder=[folder]&id=[object]&version=[version]&context=[contextid]&user=[userid]&" +
        "delivery=download&scaleType=contain&width=[width]&height=[height]"),

    /**
     * Template for a link to generate a preview for a document file
     * Default: "[protocol]://[hostname]/[dispatcherPrefix]/files?action=document&format=preview_image&folder=[folder]&id=[object]&version=[version]&context=[contextid]&user=[userid]&delivery=download&scaleType=contain&width=[width]&height=[height]"
     */
    IMAGE_LINK_DOCUMENT_FILE("imageLinkDocumentFile", "[protocol]://[hostname]/[dispatcherPrefix]/files?action=document&format=preview_image&" +
        "folder=[folder]&id=[object]&version=[version]&context=[contextid]&user=[userid]&" +
        "delivery=download&scaleType=contain&width=[width]&height=[height]"),

    /**
     * Template for direct link fragments for a directory.
     * Default: "m=infostore&f=[folder]"
     */
    DIRECT_LINK_FRAGMENTS_DIRECTORY("directLinkFragmentsDirectory", "m=infostore&f=[folder]"),

    /**
     * Template for a direct link for a directory.
     * Default: "[protocol]://[hostname]/[uiwebpath]#[directoryfragments]"
     */
    DIRECT_LINK_DIRECTORY("directLinkDirectory", "[protocol]://[hostname]/[uiwebpath]#[directoryfragments]"),

    /**
     * The maximum file length of uploads to be stored directly at the target location - others are going to be written to a
     * temporary upload file first.
     * Default: 64kB
     */
    OPTIMISTIC_SAVE_THRESHOLD_DESKTOP("optimisticSaveThresholdDesktop", "64kB"),

    /**
     * The maximum file length of uploads to be stored directly at the target location - others are going to be written to a
     * temporary upload file first.
     * Default: 64kB
     */
    OPTIMISTIC_SAVE_THRESHOLD_MOBILE("optimisticSaveThresholdMobile", "64kB")
    ;

    private static final String PREFIX = "com.openexchange.drive.";

    private final String name;
    private final Object defaultValue;

    private DriveProperty(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return PREFIX + name;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
