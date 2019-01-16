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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.icap;

import java.io.InputStream;
import com.openexchange.java.Strings;

/**
 * {@link OperationMode} - The mode at which the Response-Modification mode will be functioning.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum OperationMode {
    /**
     * This mode implies that the header {@link ICAPRequestHeader#ALLOW} is not present.
     * In this case the ICAP server dumps back the entire {@link InputStream} that it was
     * passed to it in the first place. Hence the provided {@link InputStream} of the
     * {@link ICAPRequest} will be streamed to the ICAP Server and back to the original
     * client. The {@link ICAPRequestHeader#PREVIEW} (if present) it will be ignored and
     * no preview will be send to the ICAP server. The whole data in the {@link InputStream}
     * will be streamed through.
     */
    STREAMING("streaming"),
    /**
     * <p>
     * This mode implies that the header {@link ICAPRequestHeader#ALLOW} is present.
     * In this case the ICAP server will reply with either an ICAP status code of 204
     * meaning that nothing was modified (or needs to be modified) and no response body,
     * or it will reply with a status code of 200 and an HTTP response body. The encapsulated
     * header 'Content-Type' dictates the type of the response body.
     * </p>
     * <p>
     * The {@link ICAPRequestHeader#PREVIEW} (if present) it will be honoured. In that case
     * the ICAP server may respond with a status code of 100 to instruct the client to send
     * the rest of the data, or it will reply with on of the defined {@link ICAPStatusCode}s.
     * </p>
     * <p>The {@link InputStream} that contains the original data will then be thrown away.</p>
     */
    DOUBLE_FETCH("double-fetch");

    private final String name;

    /**
     * Initialises a new {@link OperationMode}.
     */
    private OperationMode(String name) {
        this.name = name;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Parses the specified string to a valid {@link OperationMode}.
     * If an unknown mode or an error is encountered then the default
     * operation mode {@link OperationMode#DOUBLE_FETCH} will be returned.
     * 
     * @param s The string to parse
     * @return The {@link OperationMode}.
     */
    public static OperationMode parse(String s) {
        if (Strings.isEmpty(s)) {
            return OperationMode.DOUBLE_FETCH;
        }
        try {
            return OperationMode.valueOf(s.toUpperCase().replaceAll("-", "_"));
        } catch (Exception e) {
            return OperationMode.DOUBLE_FETCH;
        }
    }
}
