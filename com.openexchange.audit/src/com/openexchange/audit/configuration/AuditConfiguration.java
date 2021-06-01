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

package com.openexchange.audit.configuration;

import java.util.logging.Formatter;
import java.util.logging.Level;
import com.openexchange.audit.services.Services;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * @author Benjamin Otterbach
 */
public class AuditConfiguration {

    public static boolean getEnabled() throws OXException {
        return Boolean.parseBoolean(getConfigService().getProperty("com.openexchange.audit.logging.AuditFileHandler.enabled", "false"));
    }

    public static String getLogfileLocation() throws OXException {
        return getConfigService().getProperty("com.openexchange.audit.logging.AuditFileHandler.pattern", "/var/log/open-xchange/open-xchange-audit.log");
    }

    public static Level getLoglevel() throws OXException {
        return Level.parse(getConfigService().getProperty("com.openexchange.audit.logging.AuditFileHandler.level", Level.INFO.toString()));
    }

    public static int getLogfileLimit() throws OXException {
        return Integer.parseInt(getConfigService().getProperty("com.openexchange.audit.logging.AuditFileHandler.limit", "2097152"));
    }

    public static int getLogfileCount() throws OXException {
        return Integer.parseInt(getConfigService().getProperty("com.openexchange.audit.logging.AuditFileHandler.count", "99"));
    }

    public static Formatter getLogfileFormatter() throws InstantiationException, IllegalAccessException, ClassNotFoundException, OXException {
        return (Formatter) Class.forName(getConfigService().getProperty("com.openexchange.audit.logging.AuditFileHandler.formatter", "java.util.logging.SimpleFormatter")).newInstance();
    }

    public static boolean getLogfileAppend() throws OXException {
        return Boolean.parseBoolean(getConfigService().getProperty("com.openexchange.audit.logging.AuditFileHandler.append", "true"));
    }

    public static boolean getFileAccessLogging() throws OXException {
        return Boolean.parseBoolean(getConfigService().getProperty("com.openexchange.audit.logging.FileAccessLogging.enabled", "true"));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private static ConfigurationService getConfigService() throws OXException {
        ConfigurationService configservice = Services.optService(ConfigurationService.class);
        if (null == configservice) {
            throw ServiceExceptionCode.absentService(ConfigurationService.class);
        }
        return configservice;
    }

}
