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

package com.openexchange.serialization.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.serialization.ClassResolver;
import com.openexchange.serialization.FilteringObjectStreamFactory;

/**
 * The implementation for {@link FilteringObjectStreamFactory}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class FilteringObjectStreamFactoryImpl implements FilteringObjectStreamFactory, ForcedReloadable {

    private static final String FILENAME = "serialkiller.xml";

    private final AtomicReference<ConfigParseResult> configReference;

    /**
     * Initializes a new {@link FilteringObjectStreamFactoryImpl}.
     */
    public FilteringObjectStreamFactoryImpl() {
        super();
        configReference = new AtomicReference<>(null);
    }

    @Override
    public FilteringObjectInputStream createFilteringStream(InputStream stream, Object optContext, ClassResolver optClassResolver) throws IOException {
        ConfigParseResult configParseResult = configReference.get();
        if (configParseResult == null) {
            // Parse configuration on-the-fly
            synchronized (this) {
                configParseResult = configReference.get();
                if (configParseResult == null) {
                    configParseResult = parseConfig();
                    configReference.set(configParseResult);
                }
            }
        }

        SerializationFilteringConfig config = configParseResult.getConfig();
        if (null != config) {
            return new FilteringObjectInputStream(stream, optContext, optClassResolver, config);
        }

        // Configuration could not be successfully parsed
        throw configParseResult.getIoError();
    }

    private ConfigParseResult parseConfig() {
        try {
            SerializationFilteringConfig config = new SerializationFilteringConfig(new File(System.getProperty("openexchange.propdir"), FILENAME));
            return success(config);
        } catch (IOException e) {
            return failure(e);
        } catch (ParserConfigurationException | SAXException | RuntimeException e) {
            return failure(new IOException("Unable to create FilteredObjectStream: " + e.getMessage(), e));
        }
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        configReference.set(null);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static ConfigParseResult success(SerializationFilteringConfig config) {
        return new ConfigParseResult(config, null);
    }

    private static ConfigParseResult failure(IOException ioError) {
        return new ConfigParseResult(null, ioError);
    }

    private static class ConfigParseResult {

        private final SerializationFilteringConfig config;
        private final IOException ioError;

        /**
         * Initializes a new {@link ConfigParseResult}
         */
        ConfigParseResult(SerializationFilteringConfig config, IOException ioError) {
            super();
            this.config = config;
            this.ioError = ioError;
        }

        /**
         * Gets the configuration
         *
         * @return The configuration or <code>null</code>
         */
        SerializationFilteringConfig getConfig() {
            return config;
        }

        /**
         * Gets the I/O error
         *
         * @return The I/O error or <code>null</code>
         */
        IOException getIoError() {
            return ioError;
        }
    }

}
