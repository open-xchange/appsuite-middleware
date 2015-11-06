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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.drive.update.service;

import static com.openexchange.java.Strings.quote;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.update.files.UpdateFilesProvider;
import com.openexchange.drive.update.files.UpdateFilesProvider.Category;
import com.openexchange.exception.OXException;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DriveUpdateServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class DriveUpdateServiceImpl implements DriveUpdateService {

    private static final String PROP_BINARY_REGEX_EXE = "com.openexchange.drive.windows.binaryRegex.exe";
    private static final String PROP_BINARY_REGEX_MSI = "com.openexchange.drive.windows.binaryRegex.msi";

    private static final String OXDRIVE_UPDATE_TMPL = "oxdrive_update.tmpl";

    private UpdateFilesProvider provider;

    private Pattern regex_exe;
    private Pattern regex_msi;


    @Override
    public void init(UpdateFilesProvider provider) throws OXException {
        this.provider = provider;
        ConfigurationService config = Services.getService(ConfigurationService.class);
        String binaryRegex = config.getProperty(PROP_BINARY_REGEX_EXE);
        if (binaryRegex == null) {
            throw UpdaterExceptionCodes.MISSING_CONFIG_PROPERTY.create(PROP_BINARY_REGEX_EXE);
        }

        regex_exe = Pattern.compile(binaryRegex);

        binaryRegex = config.getProperty(PROP_BINARY_REGEX_MSI);
        if (binaryRegex == null) {
            throw UpdaterExceptionCodes.MISSING_CONFIG_PROPERTY.create(PROP_BINARY_REGEX_MSI);
        }

        regex_msi = Pattern.compile(binaryRegex);

    }

    @Override
    public String getExeFileName(String branding) throws OXException {
        return provider.getFileName(Category.DRIVE, branding, regex_exe);
    }

    @Override
    public String getMsiFileName(String branding) throws OXException {
        return provider.getFileName(Category.DRIVE, branding, regex_msi);
    }

    @Override
    public String getProductName() {
        return "com.openexchange.updater.drive";
    }

    @Override
    public OXTemplate getOxtenderSpecificTemplate() throws OXException {
        TemplateService templateService = Services.getService(TemplateService.class);
        OXTemplate template = templateService.loadTemplate(OXDRIVE_UPDATE_TMPL);
        return template;
    }

    @Override
    public Map<String, Object> getTemplateValues(String serverUrl, ServerSession session, String branding) throws OXException {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("OX_SERVERURL", quote(serverUrl));
        values.put("OX_USERNAME", quote(Utils.getUserName(session)));
        String exeFileName = getExeFileName(branding);
        String msiFileName = getMsiFileName(branding);
        try {
            values.put("URL", Utils.getFileUrl(serverUrl, branding, exeFileName));
            values.put("MD5", provider.getMD5(Category.DRIVE, branding, exeFileName));
            values.put("MSI_URL", Utils.getFileUrl(serverUrl, branding, msiFileName));
            values.put("MSI_MD5", provider.getMD5(Category.DRIVE, branding, msiFileName));
            values.put("ICON", loadIcon(branding));
        } catch (IOException e) {
            throw UpdaterExceptionCodes.IO_ERROR.create(e, "Could not prepare template variables");
        }
        return values;
    }

    @Override
    public String[] getNecessaryPermission() {
        return new String[] { "drive" };
    }

    @Override
    public boolean isResponsibleFor(String fileName, String branding) throws OXException {
        return provider.contains(Category.DRIVE, branding, fileName);
    }

    @Override
    public InputStream getFile(String fileName, String branding) throws OXException {
        if (!provider.contains(Category.DRIVE, branding, fileName)) {
            throw UpdaterExceptionCodes.SERVICE_NOT_RESPONSIBLE.create(fileName);
        }

        return provider.getFile(Category.DRIVE, branding, fileName);
    }

    @Override
    public long getFileSize(String fileName, String branding) throws OXException {
        if (!provider.contains(Category.DRIVE, branding, fileName)) {
            throw UpdaterExceptionCodes.SERVICE_NOT_RESPONSIBLE.create(fileName);
        }
        return provider.getSize(Category.DRIVE, branding, fileName);
    }

    private String loadIcon(String branding) throws IOException {
        return provider.getIcon(branding);
    }

    @Override
    public String getDefaultBranding() {
        ConfigurationService conf = Services.getService(ConfigurationService.class);
        return conf.getProperty(Constants.BRANDING_FILE, "generic");
    }

}
