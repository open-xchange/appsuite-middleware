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

package com.openexchange.ajax.framework;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;

/**
 * This parser extracts the JSON object from the web site returned by the server
 * if an upload has been made.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractUploadParser<T extends AbstractAJAXResponse> extends AbstractAJAXParser<T> {

    public static final Pattern CALLBACK_ARG_PATTERN = Pattern.compile("\\((\\{.*?\\})\\)");

    /**
     * @param failOnError
     */
    public AbstractUploadParser(boolean failOnError) {
        super(failOnError);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response getResponse(String body) throws JSONException {
        return super.getResponse(extractFromCallback(body));
    }

    protected Response getSuperResponse(String body) throws JSONException {
        return super.getResponse(body);
    }

    public static String extractFromCallback(final String body) {
        final Matcher matcher = CALLBACK_ARG_PATTERN.matcher(body);
        final String retval;
        if (matcher.find()) {
            // Parse proper response to upload request.
            retval = matcher.group(1);
        } else {
            // Try to parse normal response if something before working on upload fails.
            // For example the session.
            retval = body;
        }
        return retval;
    }
}
