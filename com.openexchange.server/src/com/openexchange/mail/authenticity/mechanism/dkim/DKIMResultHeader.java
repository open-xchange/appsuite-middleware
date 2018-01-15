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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.mail.authenticity.mechanism.dkim;

/**
 * {@link DKIMResultHeader}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class DKIMResultHeader {

    /**
     * A free-form comment on the reason the given result was returned
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.2">RFC 7601, Section 2.2</a>
     */
    public static final String REASON = "reason";

    /**
     * Refers to the content of the agent or user identifier (AUID) on behalf of which the
     * signing domain is taking responsibility.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6376#section-3.5">RFC 6376, Section 3.5</a>
     */
    public static final String HEADER_I = "header.i";

    /**
     * Refers to the content of the signing domain tag from within the signature header field, and
     * not a distinct header field called "d".
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6376#section-3.5">RFC 6376, Section 3.5</a>
     */
    public static final String HEADER_D = "header.d";

    /**
     * Refers to the DKIM signature data (base64)
     */
    public static final String HEADER_B = "header.b";
}
