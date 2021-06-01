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

package com.openexchange.config.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.openexchange.config.VariablesProvider;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.java.Charsets;
import com.openexchange.java.ImmutableReference;
import com.openexchange.java.Streams;

/**
 * {@link FileVariablesProvider} - The variables provider reading from a properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class FileVariablesProvider implements VariablesProvider {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(TokenReplacingReader.class);
    }

    private static final Cache<String, ImmutableReference<FileVariablesProvider>> INSTANCES = CacheBuilder.newBuilder().concurrencyLevel(1).initialCapacity(4).build();

    /**
     * Clears cached instances from previous {@link #getInstanceFor(String)} invocations.
     */
    public static void clearInstances() {
        INSTANCES.invalidateAll();
    }

    /**
     * Gets the appropriate instance for given properties file.
     *
     * @param fileName The name of the properties file
     * @return The variables provider reading serving from given properties file or <code>null</code> if reading properties file fails
     */
    public static FileVariablesProvider getInstanceFor(String fileName) {
        if (fileName == null) {
            // Garbage in, garbage out...
            return null;
        }
        String fileNameToUse = fileName.trim();
        ImmutableReference<FileVariablesProvider> variablesProviderRef = INSTANCES.getIfPresent(fileNameToUse);
        if (variablesProviderRef == null) {
            try {
                variablesProviderRef = INSTANCES.get(fileNameToUse, new FileVariablesProviderLoader(fileNameToUse));
            } catch (ExecutionException | UncheckedExecutionException e) {
                Throwable cause = e.getCause();
                LoggerHolder.LOG.error("Failed to read properties from file: {}", fileNameToUse, cause == null ? e : cause);
                return null;
            }
        }
        return variablesProviderRef.getValue();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Properties properties;

    /**
     * Initializes a new {@link FileVariablesProvider}.
     *
     * @param properties The properties providing values to replace with
     */
    FileVariablesProvider(Properties properties) {
        super();
        this.properties = properties;
    }

    @Override
    public String getForKey(String variableKey) {
        return variableKey == null ? null : properties.getProperty(variableKey);
    }

    @Override
    public String getName() {
        return "file";
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class FileVariablesProviderLoader implements Callable<ImmutableReference<FileVariablesProvider>> {

        private final String fileName;

        FileVariablesProviderLoader(String fileName) {
            super();
            this.fileName = fileName;
        }

        @Override
        public ImmutableReference<FileVariablesProvider> call() throws Exception {
            File file = ConfigurationImpl.doGetFileByName(fileName);
            if (file == null) {
                return new ImmutableReference<FileVariablesProvider>(null);
            }
            Properties properties = loadPropertiesFrom(file);
            return new ImmutableReference<FileVariablesProvider>(new FileVariablesProvider(properties));
        }
    }

    /**
     * Loads the properties from given file.
     *
     * @param file The file
     * @return The loaded properties
     * @throws IOException If loading properties fails
     */
    static Properties loadPropertiesFrom(File file) throws IOException {
        if (null == file) {
            return null;
        }

        FileInputStream fis = null;
        InputStreamReader fr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(file);
            fr = new InputStreamReader(fis, Charsets.UTF_8);
            br = new BufferedReader(fr, 2048);
            Properties properties = new Properties();
            properties.load(br);
            return properties;
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            Streams.close(br, fr, fis);
        }
    }

}