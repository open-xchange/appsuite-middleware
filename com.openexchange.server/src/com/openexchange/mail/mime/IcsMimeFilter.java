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

package com.openexchange.mail.mime;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.mail.BodyPart;
import com.openexchange.data.conversion.ical.ICalParser;
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

    private static final Set<String> ITIP_METHODS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("REQUEST", "CANCEL", "REPLY")));

    @Override
    public boolean ignorable(final String contentType, final BodyPart bodyPart) {
        for (final String baseType : ignorableContentTypes) {
            if (contentType.startsWith(baseType)) {
                /*
                 * Check ICal part for a valid METHOD and its presence in Content-Type header
                 */
                final ICalParser iCalParser = ServerServiceRegistry.getInstance().getService(ICalParser.class);
                if (iCalParser != null) {
                    try {
                        final String method = iCalParser.parseProperty("METHOD", MessageUtility.getPartInputStream(bodyPart));
                        return null != method && ITIP_METHODS.contains(method.toUpperCase());
                    } catch (final RuntimeException e) {
                        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IcsMimeFilter.class);
                        logger.warn("A runtime error occurred.", e);
                    }
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean ignorable(final String contentType, final MailPart bodyPart) {
        for (final String baseType : ignorableContentTypes) {
            if (contentType.startsWith(baseType)) {
                /*
                 * Check ICal part for a valid METHOD and its presence in Content-Type header
                 */
                final ICalParser iCalParser = ServerServiceRegistry.getInstance().getService(ICalParser.class);
                if (iCalParser != null) {
                    try {
                        final String method = iCalParser.parseProperty("METHOD", bodyPart.getInputStream());
                        return null != method && ITIP_METHODS.contains(method.toUpperCase());
                    } catch (final Exception e) {
                        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IcsMimeFilter.class);
                        logger.warn("An error occurred.", e);
                    }
                }
                return false;
            }
        }
        return false;
    }

}
