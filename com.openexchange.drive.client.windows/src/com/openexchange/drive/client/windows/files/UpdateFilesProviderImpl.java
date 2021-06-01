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

package com.openexchange.drive.client.windows.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.drive.client.windows.service.UpdaterExceptionCodes;
import com.openexchange.drive.client.windows.service.internal.BrandingConfig;
import com.openexchange.drive.client.windows.service.internal.Utils;
import com.openexchange.drive.client.windows.servlet.BrandingExceptionCodes;
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

    private static final Logger LOG = LoggerFactory.getLogger(UpdateFilesProvider.class);

    private static final String DRIVE_ICON = "drive.ico";

    private static volatile UpdateFilesProviderImpl instance;

    public static UpdateFilesProviderImpl getInstance() {
        UpdateFilesProviderImpl tmp = instance;
        if (null == tmp) {
            synchronized (UpdateFilesProviderImpl.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new UpdateFilesProviderImpl();
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private String path;
    private final Map<String, FileSystemResourceLoader> loaders;

    private UpdateFilesProviderImpl() {
        super();
        loaders = new ConcurrentHashMap<String, FileSystemResourceLoader>();
    }

    /**
     * Initialize this UpdatesFilesProvider again with the old path
     *
     * @return this
     * @throws OXException
     */
    public UpdateFilesProvider reinit() throws OXException {
        synchronized (loaders) {
            return init(path);
        }
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

            File parent = null == path ? null : new File(path);
            if (null == parent || !parent.exists()) {
                throw BrandingExceptionCodes.MISSING_FOLDER.create();
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
        FileSystemResourceLoader resourceLoader = loaders.get(branding);
        if (null == resourceLoader) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }

        try {
            return resourceLoader.get(name);
        } catch (IOException e) {
            throw new OXException(e);
        }
    }

    @Override
    public boolean contains(String branding, String name) throws OXException {
        FileSystemResourceLoader resourceLoader = loaders.get(branding);
        if (null == resourceLoader) {
            return false;
        }

        return resourceLoader.getAvailableFiles().contains(name);
    }

    @Override
    public boolean contains(String branding) {
        return loaders.containsKey(branding);
    }

    @Override
    public long getSize(String branding, String name) throws OXException {
        FileSystemResourceLoader resourceLoader = loaders.get(branding);
        if (null == resourceLoader) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }

        try {
            return resourceLoader.getSize(name);
        } catch (IOException e) {
            throw new OXException(e);
        }
    }

    @Override
    public String getFileName(String branding, Pattern regex) throws OXException {
        FileSystemResourceLoader resourceLoader = loaders.get(branding);
        if (null == resourceLoader) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }

        try {
            Set<String> files = resourceLoader.getAvailableFiles();
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
            LOG.error("Regex ''{}'' is invalid!", regex);
            throw UpdaterExceptionCodes.NO_FILES_AVAILABLE.create(e);
        }
        return null;
    }

    @Override
    public String getMD5(String branding, String name) throws OXException {
        FileSystemResourceLoader resourceLoader = loaders.get(branding);
        if (null == resourceLoader) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }

        try {
            return resourceLoader.getMD5(name);
        } catch (IOException e) {
            throw new OXException(e);
        }
    }

    @Override
    public String getIcon(String branding) throws OXException {
        FileSystemResourceLoader resourceLoader = loaders.get(branding);
        if (null == resourceLoader) {
            throw UpdaterExceptionCodes.BRANDING_ERROR.create(branding);
        }

        try {
            File icon = resourceLoader.getFile(DRIVE_ICON);
            return Utils.convertToBase64(icon);
        } catch (IOException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void reload() throws OXException {
        reinit();
    }

    @Override
    public void reload(String path) throws OXException {
        init(path);
    }

    @Override
    public List<String> getAvailableBrandings() {
        return new ArrayList<String>(loaders.keySet());
    }
}

