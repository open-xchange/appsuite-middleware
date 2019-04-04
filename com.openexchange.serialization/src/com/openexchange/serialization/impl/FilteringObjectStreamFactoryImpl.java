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

package com.openexchange.serialization.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
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
    public FilteringObjectInputStream createFilteringStream(InputStream stream) throws IOException {
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
            return new FilteringObjectInputStream(stream, config);
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
