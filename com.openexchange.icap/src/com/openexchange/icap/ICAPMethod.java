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

/**
 * {@link ICAPMethod} - Defines the allowed request ICAP methods as described in
 * <a href="https://tools.ietf.org/html/rfc3507#section-4.3.2">RFC-3507, Section 4.3.2</a>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum ICAPMethod {

    /**
     * <p>The Request Modification method.</p>
     * <p>The ICAP client sends an HTTP request to an ICAP server. The ICAP server
     * returns a modified version of the request, an HTTP response, or (if the client
     * indicates it supports 204 responses) an indication that no modification is
     * required.</p>
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.8">RFC-3507, Section 4.8</a>
     */
    REQMOD,
    /**
     * <p>The Response Modification method.</p>
     * <p>The ICAP client sends an origin server's HTTP response to an ICAP server, and
     * (if available) the original client request that caused that response. Similar to
     * {@link #REQMOD}, the response from the ICAP server can be an adapted HTTP
     * response, an error, or a 204 response code indicating that no adaptation is required.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.9">RFC-3507, Section 4.9</a>
     */
    RESPMOD,
    /**
     * <p>The Options method.</p>
     * <p>The ICAP "OPTIONS" method is used by the ICAP client to retrieve
     * configuration information from the ICAP server. In this method, the
     * ICAP client sends a request addressed to a specific ICAP resource and
     * receives back a response with options that are specific to the
     * service named by the URI. All OPTIONS requests MAY also return
     * options that are global to the server (i.e., apply to all services).</p>
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.10">RFC-3507, Section 4.10</a>
     */
    OPTIONS;
}
