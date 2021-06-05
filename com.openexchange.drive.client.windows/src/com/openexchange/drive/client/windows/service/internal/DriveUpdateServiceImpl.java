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

package com.openexchange.drive.client.windows.service.internal;

import static com.openexchange.java.Strings.quote;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.client.windows.files.UpdateFilesProvider;
import com.openexchange.drive.client.windows.service.BrandingService;
import com.openexchange.drive.client.windows.service.Constants;
import com.openexchange.drive.client.windows.service.DriveUpdateService;
import com.openexchange.drive.client.windows.service.UpdaterExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;

/**
 * {@link DriveUpdateServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class DriveUpdateServiceImpl implements DriveUpdateService {

    private UpdateFilesProvider provider;
    private Pattern regex_exe;
    private Pattern regex_msi;

    @Override
    public void init(UpdateFilesProvider provider) throws OXException {
        if (provider == null) {
            throw new OXException(new IllegalArgumentException("Provider must not be null"));
        }
        this.provider = provider;
        ConfigurationService config = Services.getService(ConfigurationService.class);
        String binaryRegex = config.getProperty(Constants.PROP_BINARY_REGEX_EXE);
        if (binaryRegex == null) {
            throw UpdaterExceptionCodes.MISSING_CONFIG_PROPERTY.create(Constants.PROP_BINARY_REGEX_EXE);
        }

        regex_exe = Pattern.compile(binaryRegex);

        binaryRegex = config.getProperty(Constants.PROP_BINARY_REGEX_MSI);
        if (binaryRegex == null) {
            throw UpdaterExceptionCodes.MISSING_CONFIG_PROPERTY.create(Constants.PROP_BINARY_REGEX_MSI);
        }

        regex_msi = Pattern.compile(binaryRegex);

    }

    @Override
    public String getExeFileName(String branding) throws OXException {
        return provider.getFileName(branding, regex_exe);
    }

    @Override
    public String getMsiFileName(String branding) throws OXException {
        return provider.getFileName(branding, regex_msi);
    }

    @Override
    public OXTemplate getOxtenderSpecificTemplate() throws OXException {
        TemplateService templateService = Services.getService(TemplateService.class);
        ConfigurationService conf = Services.getService(ConfigurationService.class);
        OXTemplate template = templateService.loadTemplate(conf.getProperty(Constants.TMPL_UPDATER_CONFIG, Constants.TMPL_UPDATER_DEFAULT));
        return template;
    }

    @Override
    public String getInstallerDownloadUrl(HostData hostData, Session session) throws OXException {
        // Get associated branding
        String branding = BrandingService.getBranding(session);
        if (!isValid(branding)) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }

        // Get the name of .exe file
        String exeFileName = getExeFileName(branding);

        // Compile server URL
        String serverUrl = (hostData.isSecure() ? "https://" : "http://") + hostData.getHost();

        // ... and return URL with "session" URL parameter
        return Utils.compileUrl(serverUrl, new String[] { Utils.getServletPrefix(), Constants.INSTALL_SERVLET, exeFileName }, Collections.singletonMap("session", session.getSessionID()));
    }

    @Override
    public Map<String, Object> getTemplateValues(String serverUrl, String username, String branding) throws OXException {
        if (!isValid(branding)) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }

        Map<String, Object> values = new HashMap<String, Object>(12);
        values.put("OX_SERVERURL", StringEscapeUtils.escapeXml(quote(serverUrl)));
        values.put("OX_USERNAME", StringEscapeUtils.escapeXml(quote(username)));
        String exeFileName = getExeFileName(branding);
        String msiFileName = getMsiFileName(branding);
        if (exeFileName == null || msiFileName == null) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }
        try {
            values.put("URL", StringEscapeUtils.escapeXml(Utils.getFileUrl(serverUrl, exeFileName)));
            values.put("MD5", provider.getMD5(branding, exeFileName));
            values.put("MSI_URL", StringEscapeUtils.escapeXml(Utils.getFileUrl(serverUrl, msiFileName)));
            values.put("MSI_MD5", provider.getMD5(branding, msiFileName));
            values.put("ICON", loadIcon(branding));

            BrandingConfig conf = BrandingConfig.getBranding(branding);

            Properties prop = conf.getProperties();
            values.put(Constants.BRANDING_NAME, StringEscapeUtils.escapeXml(prop.get(Constants.BRANDING_NAME).toString()));
            values.put(Constants.BRANDING_VERSION, prop.get(Constants.BRANDING_VERSION));
            values.put(Constants.BRANDING_RELEASE, prop.get(Constants.BRANDING_RELEASE));
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw UpdaterExceptionCodes.IO_ERROR.create(cause, "Could not prepare template variables");
            }
            throw e;
        }
        return values;
    }

    @Override
    public String[] getNecessaryPermission() {
        return new String[] { "drive" };
    }

    @Override
    public boolean isResponsibleFor(String fileName, String branding) throws OXException {
        return provider.contains(branding, fileName);
    }

    @Override
    public InputStream getFile(String fileName, String branding) throws OXException {
        if (!provider.contains(branding, fileName)) {
            throw UpdaterExceptionCodes.SERVICE_NOT_RESPONSIBLE.create(fileName);
        }

        return provider.getFile(branding, fileName);
    }

    @Override
    public long getFileSize(String fileName, String branding) throws OXException {
        if (!provider.contains(branding, fileName)) {
            throw UpdaterExceptionCodes.SERVICE_NOT_RESPONSIBLE.create(fileName);
        }
        return provider.getSize(branding, fileName);
    }

    /**
     * Loads the icon for the given branding
     *
     * @param branding The branding
     * @return The icon as a base64 string
     * @throws OXException
     */
    private String loadIcon(String branding) throws OXException {
        return provider.getIcon(branding);
    }

    @Override
    public String getDefaultBranding() {
        ConfigurationService conf = Services.getService(ConfigurationService.class);
        return conf.getProperty(Constants.BRANDING_CONF, "generic");
    }

    /**
     * Checks if the branding is valid
     *
     * @param branding The name of the brand
     * @return true if it is valid, false otherwise
     */
    private boolean isValid(String branding){
        boolean provBool = provider.contains(branding);
        boolean confBool = BrandingConfig.containsBranding(branding);
        return provBool && confBool;
    }

}
