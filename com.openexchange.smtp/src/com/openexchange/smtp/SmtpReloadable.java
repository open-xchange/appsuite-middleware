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

package com.openexchange.smtp;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.DefaultInterests.Builder;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.smtp.config.SMTPProperties;

/**
 * {@link SmtpReloadable} - Collects reloadables for SMTP module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class SmtpReloadable implements Reloadable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SmtpReloadable.class);

    private static final SmtpReloadable INSTANCE = new SmtpReloadable();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static SmtpReloadable getInstance() {
        return INSTANCE;
    }

    // --------------------------------------------------------------------------------------------------- //

    private final List<Reloadable> reloadables;

    /**
     * Initializes a new {@link SmtpReloadable}.
     */
    private SmtpReloadable() {
        super();
        reloadables = new CopyOnWriteArrayList<Reloadable>();
    }

    /**
     * Adds given {@link Reloadable} instance.
     *
     * @param reloadable The instance to add
     */
    public void addReloadable(final Reloadable reloadable) {
        reloadables.add(reloadable);
    }

    @Override
    public void reloadConfiguration(final ConfigurationService configService) {
        try {
            final SMTPProperties smtpProperties = SMTPProperties.getInstance();
            if (null != smtpProperties) {
                smtpProperties.resetProperties();
                smtpProperties.loadProperties();
            }
        } catch (OXException e) {
            LOGGER.warn("Failed to reload SMTP properties", e);
        }

        for (final Reloadable reloadable : reloadables) {
            reloadable.reloadConfiguration(configService);
        }
    }

    @Override
    public Interests getInterests() {
        Set<String> properties = new TreeSet<>();
        Set<String> fileNames = new TreeSet<>();

        boolean somethingAdded = false;
        for (Reloadable reloadable : reloadables) {
            Interests interests = reloadable.getInterests();
            if (null == interests) {
                return Reloadables.getInterestsForAll();
            }

            String[] propertiesOfInterest = interests.getPropertiesOfInterest();
            if (null != propertiesOfInterest) {
                properties.addAll(Arrays.asList(propertiesOfInterest));
                somethingAdded = true;
            }
            String[] configFileNames = interests.getConfigFileNames();
            if (null != configFileNames) {
                fileNames.addAll(Arrays.asList(configFileNames));
                somethingAdded = true;
            }
        }

        if (!somethingAdded) {
            return Reloadables.getInterestsForAll();
        }

        Builder builder = DefaultInterests.builder();
        if (!properties.isEmpty()) {
            builder.propertiesOfInterest(properties.toArray(new String[properties.size()]));
        }
        if (!fileNames.isEmpty()) {
            builder.configFileNames(fileNames.toArray(new String[fileNames.size()]));
        }
        return builder.build();
    }

}
