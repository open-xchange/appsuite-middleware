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

package com.openexchange.drive.client.windows.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.drive.client.windows.service.UpdaterExceptionCodes;
import com.openexchange.drive.client.windows.service.internal.BrandingConfig;
import com.openexchange.drive.client.windows.service.internal.Utils;
import com.openexchange.drive.client.windows.servlet.BrandingException;
import com.openexchange.exception.OXException;


/**
 * {@link UpdateFilesProviderImpl} is an UpdateFilesProvider which uses a FileSystemRessourceLoader per branding to retrieve the files.
 * 
 * For this purpose it searches for subfolder's under a given path, which have a proper '.branding' or '.properties' file.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class UpdateFilesProviderImpl implements UpdateFilesProvider {

    private final static Logger LOG = LoggerFactory.getLogger(UpdateFilesProvider.class);
    private static UpdateFilesProviderImpl instance = null;
    private String path;
    private final Map<String, FileSystemResourceLoader> loaders;
    private static final String DRIVE_ICON = "drive.ico";


    public static UpdateFilesProviderImpl getInstance() {
        if (instance == null) {
            instance = new UpdateFilesProviderImpl();
        }
        return instance;
    }

    private UpdateFilesProviderImpl() {
        super();
        loaders = new HashMap<String, FileSystemResourceLoader>();
    }

    /**
     * Initialize this UpdatesFilesProvider again with the old path
     * 
     * @return this
     * @throws OXException
     */
    public UpdateFilesProvider reinit() throws OXException {
        return init(this.path);
    }

    /**
     * Tests which branding's are available and creates all necessary FileSystemResourceLoader
     * 
     * @param path The path to look for branding's
     * @return this
     * @throws OXException if branding folder is missing
     */
    public UpdateFilesProvider init(String path) throws OXException {
        synchronized (loaders) {

            this.path = path;
            loaders.clear();
            File parent = new File(path);
            if (null == parent || !parent.exists()) {
                throw new BrandingException(BrandingException.MISSING_FOLDER);
            }

            FileFilter dirFilter = new FileFilter() {

                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            };

            File[] dirs = parent.listFiles(dirFilter);
            if (dirs != null && dirs.length != 0) {

                FilenameFilter brandingFilter = new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".properties") || name.endsWith(".branding");
                    }
                };
                BrandingConfig.clear();

                for (File f : dirs) {
                    File[] brandings = f.listFiles(brandingFilter);
                    if (brandings.length == 1) {
                        try {
                            if (BrandingConfig.checkFile(brandings[0])) {
                                loaders.put(f.getName(), new FileSystemResourceLoader(f.getAbsolutePath()));
                            }
                        } catch (IOException e) {
                            LOG.warn("Failed to load properties from %s and will be skipped!", brandings[0].getName());
                            continue;
                        }

                    } else {

                        LOG.warn("Folder %s contains none or more than one property file and will be skipped!", f.getName());
                        continue;
                    }
                }
            }
        }
        return this;
    }

    @Override
    public InputStream getFile(String branding, String name) throws OXException {
        try {
            return loaders.get(branding).get(name);
        } catch (IOException e) {
            throw new OXException(e);
        }
    }

    @Override
    public boolean contains(String branding, String name) throws OXException {
        if (!contains(branding)) {
            return false;
        }
        return loaders.get(branding).getAvailableFiles().contains(name);
    }

    @Override
    public boolean contains(String branding) {
        return loaders.containsKey(branding);
    }

    @Override
    public long getSize(String branding, String name) throws OXException {
        try {
            return loaders.get(branding).getSize(name);
        } catch (IOException e) {
            throw new OXException(e);
        }
    }

    @Override
    public String getFileName(String branding, Pattern regex) throws OXException {
        if (!loaders.containsKey(branding)) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }
        try {
            Set<String> files = loaders.get(branding).getAvailableFiles();
            if (files.isEmpty()) {
                throw UpdaterExceptionCodes.NO_FILES_AVAILABLE.create();
            }

            for (String file : files) {
                Matcher matcher = regex.matcher(file);
                if (matcher.matches()) {
                    return file;
                }
            }

        } catch (PatternSyntaxException e) {
            LOG.error("Regex '" + regex + "' is invalid!");
            throw UpdaterExceptionCodes.NO_FILES_AVAILABLE.create(e);
        }
        return null;
    }

    @Override
    public String getMD5(String branding, String name) throws OXException {
        try {
            return loaders.get(branding).getMD5(name);
        } catch (IOException e) {
            throw new OXException(e);
        }
    }

    @Override
    public String getIcon(String branding) throws IOException {
        File icon = loaders.get(branding).getFile(DRIVE_ICON);
        return Utils.convertToBase64(icon);
    }

    @Override
    public void reload() throws OXException {
        reinit();
    }

    @Override
    public void reload(String path) throws OXException {
        this.path = path;
        reinit();
    }

    @Override
    public List<String> getAvailableBrandings() {
        ArrayList<String> list = new ArrayList<String>(loaders.size());
        list.addAll(loaders.keySet());
        return list;
    }
}

