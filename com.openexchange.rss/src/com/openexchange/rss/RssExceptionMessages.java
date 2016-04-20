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

package com.openexchange.rss;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link RssExceptionMessages} - Exception messages for RSS module that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RssExceptionMessages implements LocalizableStrings {

    // Invalid RSS URL -- No or not well-formed XML content provided by URL: %1$s
    public static final String INVALID_RSS_MSG = "Invalid RSS URL or not well-formed XML content provided by URL: %1$s";

    // HTTP error %1$s while loading RSS feed from URL: %2$s.
    public static final String RSS_HTTP_ERROR_MSG = "HTTP error %1$s while loading RSS feed from URL: %2$s.";

    // Timeout while reading the RSS feed from URL: %1$s
    public static final String TIMEOUT_ERROR_MSG = "Timeout while reading the RSS feed from URL: %1$s";

    // Failed to read RSS feed from URL: %1$s
    public static final String GENERIC_ERROR_WITH_ARG1_MSG = "Failed to read RSS feed from URL: %1$s";

    // Failed to read RSS feed from URL: %2$s
    public static final String GENERIC_ERROR_WITH_ARG2_MSG = "Failed to read RSS feed from URL: %2$s";
    
    // The RSS feed is exceeding the maximum allowed size of '%1$s'
    public static final String RSS_SIZE_EXCEEDED = "The RSS feed is exceeding the maximum allowed size of '%1$s'";

    // Cannot connect to RSS with URL: %1$s. Please change URL and try again.
    public static final String RSS_CONNECTION_ERROR_MSG = "Cannot connect to RSS with URL: %1$s. Please change URL and try again.";

    /**
     * Initializes a new {@link RssExceptionMessages}.
     */
    private RssExceptionMessages() {
        super();
    }

}
