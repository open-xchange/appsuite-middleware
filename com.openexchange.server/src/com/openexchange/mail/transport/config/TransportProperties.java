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

package com.openexchange.mail.transport.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link TransportProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportProperties implements ITransportProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TransportProperties.class);

    private static final TransportProperties instance = new TransportProperties();

    /**
     * Gets the singleton instance of {@link TransportProperties}
     *
     * @return The singleton instance of {@link TransportProperties}
     */
    public static TransportProperties getInstance() {
        return instance;
    }

    private final AtomicBoolean loaded;

    /*-
     * Fields for global properties
     */

    private int referencedPartLimit;

    private String defaultTransportProvider;

    private String publishingInfostoreFolder;

    private boolean removeMimeVersionInSubParts;

    /**
     * Initializes a new {@link TransportProperties}
     */
    private TransportProperties() {
        super();
        loaded = new AtomicBoolean();
    }

    /**
     * Exclusively loads the global transport properties
     */
    void loadProperties() {
        if (!loaded.get()) {
            synchronized (loaded) {
                if (!loaded.get()) {
                    loadProperties0();
                    loaded.set(true);
                }
            }
        }
    }

    /**
     * Exclusively resets the global transport properties
     */
    void resetProperties() {
        if (loaded.get()) {
            synchronized (loaded) {
                if (loaded.get()) {
                    resetFields();
                    loaded.set(false);
                }
            }
        }
    }

    private void resetFields() {
        referencedPartLimit = 0;
        publishingInfostoreFolder = null;
        removeMimeVersionInSubParts = false;
    }

    private void loadProperties0() {
        StringBuilder logBuilder = new StringBuilder(1024);
        List<Object> args = new ArrayList<Object>(32);
        String lineSeparator = Strings.getLineSeparator();

        logBuilder.append("{}Loading global transport properties...{}");
        args.add(lineSeparator);
        args.add(lineSeparator);

        final ConfigurationService configuration = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);

        {
            final String referencedPartLimitStr = configuration.getProperty(
                "com.openexchange.mail.transport.referencedPartLimit",
                "1048576").trim();
            try {
                referencedPartLimit = Integer.parseInt(referencedPartLimitStr);
                logBuilder.append("    Referenced Part Limit: {}{}");
                args.add(referencedPartLimit);
                args.add(lineSeparator);
            } catch (NumberFormatException e) {
                referencedPartLimit = 1048576;
                logBuilder.append("    Referenced Part Limit: Invalid value \"{}\". Setting to fallback {}{}");
                args.add(referencedPartLimitStr);
                args.add(referencedPartLimit);
                args.add(lineSeparator);

            }
        }

        {
            final String defaultTransProvStr = configuration.getProperty("com.openexchange.mail.defaultTransportProvider", "smtp").trim();
            defaultTransportProvider = defaultTransProvStr;
            logBuilder.append("    Default Transport Provider: {}{}");
            args.add(defaultTransportProvider);
            args.add(lineSeparator);
        }

        {
            final String tmp = configuration.getProperty(
                "com.openexchange.mail.transport.publishingPublicInfostoreFolder",
                "i18n-defined").trim();
            publishingInfostoreFolder = tmp;
            logBuilder.append("    Publishing Infostore Folder Name: \"{}\"{}");
            args.add(publishingInfostoreFolder);
            args.add(lineSeparator);
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.transport.removeMimeVersionInSubParts", "false").trim();
            removeMimeVersionInSubParts = Boolean.parseBoolean(tmp);
            logBuilder.append("    Remove \"MIME-Version\" header in sub-parts: {}{}");
            args.add(removeMimeVersionInSubParts ? Boolean.TRUE : Boolean.FALSE);
            args.add(lineSeparator);
        }

        logBuilder.append("Global transport properties successfully loaded!");
        LOG.info(logBuilder.toString(), args.toArray(new Object[args.size()]));
    }

    @Override
    public int getReferencedPartLimit() {
        return referencedPartLimit;
    }

    /**
     * Checks whether to remove <i>"MIME-Version"</i> header from sub-parts.
     *
     * @return <code>true</code> to remove <i>"MIME-Version"</i> header from sub-parts; otherwise <code>false</code>
     */
    public boolean isRemoveMimeVersionInSubParts() {
        return removeMimeVersionInSubParts;
    }

    /**
     * Gets the default transport provider
     *
     * @return The default transport provider
     */
    public String getDefaultTransportProvider() {
        return defaultTransportProvider;
    }

    /**
     * Gets the name of the publishing infostore folder.
     *
     * @return The name of the publishing infostore folder
     */
    public String getPublishingInfostoreFolder() {
        return publishingInfostoreFolder;
    }

}
