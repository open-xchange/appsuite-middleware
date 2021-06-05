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

package com.openexchange.mail.mime;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import javax.mail.BodyPart;
import com.google.common.collect.ImmutableSet;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link IcsMimeFilter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IcsMimeFilter extends MimeFilter {

    private static final IcsMimeFilter INSTANCE = new IcsMimeFilter();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static IcsMimeFilter getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link IcsMimeFilter}.
     */
    private IcsMimeFilter() {
        super(Arrays.asList("text/calendar", "application/ics"));
    }

    private static final Set<String> ITIP_METHODS = ImmutableSet.of("REQUEST", "CANCEL", "REPLY");

    @Override
    public boolean ignorable(String contentType, BodyPart bodyPart) {
        for (String baseType : ignorableContentTypes) {
            if (contentType.startsWith(baseType)) {
                /*
                 * Check ICal part for a valid METHOD and its presence in Content-Type header
                 */
                try {
                    final String method = getICalMethod(MessageUtility.getPartInputStream(bodyPart));
                    return null != method && ITIP_METHODS.contains(method.toUpperCase());
                } catch (Exception e) {
                    final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IcsMimeFilter.class);
                    logger.warn("A runtime error occurred.", e);
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean ignorable(String contentType, MailPart bodyPart) {
        for (String baseType : ignorableContentTypes) {
            if (contentType.startsWith(baseType)) {
                /*
                 * Check ICal part for a valid METHOD and its presence in Content-Type header
                 */
                try {
                    final String method = getICalMethod(bodyPart.getInputStream());
                    return null != method && ITIP_METHODS.contains(method.toUpperCase());
                } catch (Exception e) {
                    final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IcsMimeFilter.class);
                    logger.warn("An error occurred.", e);
                }
                return false;
            }
        }
        return false;
    }

    private static String getICalMethod(InputStream inputStream) throws OXException {
        return ServerServiceRegistry.getInstance().getService(ICalService.class, true).importICal(inputStream, null).getMethod();
    }

}
