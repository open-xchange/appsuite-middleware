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

package com.openexchange.mail.authenticity;

/**
 * {@link ReceivedSPFHeaderFieldKey} - Defines the keys that appear inside the '<code>Received-SPF</code>' header field.
 * '<code>Received-SPF</code>' header is intended to include enough information to enable reconstruction of the SPF
 * evaluation of the message.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7208#section-9.1">RFC-7208, Section 9.1</a>
 */
public enum ReceivedSPFHeaderFieldKey {

    /**
     * The IP address of the SMTP client
     */
    CLIENT_IP("client-ip"),
    /**
     * The envelope sender mailbox
     */
    ENVELOPE_FROM("envelope-from"),
    /**
     * The host name given in the <code>HELO</code> or <code>EHLO</code> command
     */
    HELO("helo"),
    /**
     * The mechanism that matched (if no mechanisms matched, substitute the word "default")
     */
    MECHANISM("mechanism"),
    /**
     * If an error was returned, details about the error
     */
    PROBLEM("problem"),
    /**
     * The host name of the SPF verifier
     */
    RECEIVER("receiver"),
    /**
     * The identity that was checked
     */
    IDENTITY("identity");

    private final String technicalName;

    private ReceivedSPFHeaderFieldKey(String technicalName) {
        this.technicalName = technicalName;
    }

    /**
     * Returns the technical name of the header field
     *
     * @return the technical name of the header field
     */
    public String getTechnicalName() {
        return technicalName;
    }
}
