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

package com.openexchange.dataretention.csv;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dataretention.DataRetentionExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link CSVDataRetentionConfig} - The configuration for CSV data retention.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CSVDataRetentionConfig {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CSVDataRetentionConfig.class);

    private static volatile CSVDataRetentionConfig instance;

    /**
     * Gets the singleton instance of {@link CSVDataRetentionConfig}.
     *
     * @return The singleton instance of {@link CSVDataRetentionConfig}.
     */
    public static CSVDataRetentionConfig getInstance() {
        CSVDataRetentionConfig tmp = instance;
        if (null == tmp) {
            synchronized (CSVDataRetentionConfig.class) {
                if (null == (tmp = instance)) {
                    tmp = instance = new CSVDataRetentionConfig();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the singleton instance of {@link CSVDataRetentionConfig}.
     */
    public static void releaseInstance() {
        if (null != instance) {
            synchronized (CSVDataRetentionConfig.class) {
                if (null != instance) {
                    instance = null;
                }
            }
        }
    }

    /*-
     * ######################### MEMBERS #########################
     */

    private File directory;

    private int versionNumber;

    private String clientId;

    private String sourceId;

    private String location;

    private TimeZone timeZone;

    private long rotateLength;

    /**
     * Initializes a new {@link CSVDataRetentionConfig}.
     */
    private CSVDataRetentionConfig() {
        super();
    }

    /**
     * Initializes this configuration with specified configuration service.
     *
     * @param configurationService The configuration service
     * @throws OXException If initialization fails
     */
    public void init(final ConfigurationService configurationService) throws OXException {
        final StringBuilder logBuilder = new StringBuilder(512).append("\nCSV data retention configuration:");
        // Directory
        {
            final String directoryStr = configurationService.getProperty("com.openexchange.dataretention.dir", "/var/log/open-xchange").trim();
            directory = new File(directoryStr);
            if (!directory.exists()) {
                throw DataRetentionExceptionCodes.IO.create("Directory \"" + directoryStr + "\" does not exist.");
            } else if (!directory.isDirectory()) {
                throw DataRetentionExceptionCodes.IO.create("Pathname \"" + directoryStr + "\" does not denote a directoy.");
            } else if (!directory.canWrite()) {
                throw DataRetentionExceptionCodes.IO.create("Directory \"" + directoryStr + "\" does not grant write permission.");
            }
            if (null != logBuilder) {
                logBuilder.append("\n\tcom.openexchange.dataretention.dir=").append(directory.getPath());
            }
        }
        // Version number
        {
            final String versionNumberStr = configurationService.getProperty("com.openexchange.dataretention.versionNumber", "1").trim();
            try {
                versionNumber = Integer.parseInt(versionNumberStr);
            } catch (final NumberFormatException e) {
                LOG.error("Property \"com.openexchange.dataretention.versionNumber\" is not a number: {}.Using fallback \"1\" instead.", versionNumberStr);
                versionNumber = 1;
            }
            if (null != logBuilder) {
                logBuilder.append("\n\tcom.openexchange.dataretention.versionNumber=").append(versionNumber);
            }
        }
        // Client ID
        {
            final String clientIDStr = configurationService.getProperty("com.openexchange.dataretention.clientID", "").trim();
            if (clientIDStr.length() == 0) {
                LOG.warn("Missing client ID. Using empty string.");
            }
            clientId = clientIDStr;
            if (null != logBuilder) {
                logBuilder.append("\n\tcom.openexchange.dataretention.clientID=").append(clientId);
            }
        }
        // Source ID
        {
            final String srcIDStr = configurationService.getProperty("com.openexchange.dataretention.sourceID", "").trim();
            if (srcIDStr.length() == 0) {
                LOG.warn("Missing source ID. Using empty string.");
            }
            sourceId = srcIDStr;
            if (null != logBuilder) {
                logBuilder.append("\n\tcom.openexchange.dataretention.sourceID=").append(sourceId);
            }
        }
        // Location
        {
            final String locationStr = configurationService.getProperty("com.openexchange.dataretention.location", "").trim();
            if (locationStr.length() == 0) {
                LOG.warn("Missing location. Using empty string.");
            }
            location = locationStr;
            if (null != logBuilder) {
                logBuilder.append("\n\tcom.openexchange.dataretention.location=").append(location);
            }
        }
        // Time zone
        {
            String tzStr = configurationService.getProperty("com.openexchange.dataretention.timeZone", "").trim();
            if (tzStr.length() == 0) {
                LOG.warn("Missing time zone. Using \"GMT\" as fallback.");
                tzStr = "GMT";
            }
            // Get all available IDs
            final Set<String> ids = new HashSet<String>(Arrays.asList(TimeZone.getAvailableIDs()));
            if (!ids.contains(tzStr)) {
                LOG.error("Time zone ID \"{}\" is not supported. Using \"GMT\" as fallback.", tzStr);
                tzStr = "GMT";
            }
            timeZone = TimeZone.getTimeZone(tzStr);
            if (null != logBuilder) {
                logBuilder.append("\n\tcom.openexchange.dataretention.timeZone=").append(timeZone.getID());
            }
        }
        // Rotate length
        {
            String rl = configurationService.getProperty("com.openexchange.dataretention.rotateLength", "0").trim();
            if (rl.length() == 0) {
                LOG.warn("Missing rotation length. Using \"0\" as fallback.");
                rl = "0";
            }
            try {
                rotateLength = Long.parseLong(rl);
            } catch (final NumberFormatException e) {
                LOG.error("Property \"com.openexchange.dataretention.rotateLength\" is not a number: {}.Using fallback \"0\" instead.", rl);
                rotateLength = 0L;
            }
            if (null != logBuilder) {
                logBuilder.append("\n\tcom.openexchange.dataretention.rotateLength=").append(rotateLength);
            }
        }
        LOG.info(logBuilder.toString());
    }

    /**
     * Gets the parent directory of the CSV file.
     *
     * @return The parent directory of the CSV file.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Gets the version number which identifies the format version of the file.
     *
     * @return The version number
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Gets the client ID. A string identifying the tenant; e.g. <code>&quot;1UND1&quot;</code>.
     *
     * @return The client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the source ID. Any string identifying the data source; e.g. <code>&quot;GMX_mail_01&quot;</code>.
     *
     * @return The sourceId
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Gets the location of the system generating the CSV file; e.g. <code>&quot;DE/Karlsruhe;</code>.
     *
     * @return The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the time zone of the location.
     *
     * @return The time zone of the location.
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Gets the rotate length.
     *
     * @return The rotate length.
     */
    public long getRotateLength() {
        return rotateLength;
    }
}
