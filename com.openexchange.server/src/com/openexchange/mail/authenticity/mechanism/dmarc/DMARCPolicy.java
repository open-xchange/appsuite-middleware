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

package com.openexchange.mail.authenticity.mechanism.dmarc;

/**
 * {@link DMARCPolicy} - Defines the requested Mail Receiver policy
 * (plain-text; REQUIRED for policy records). Indicates the policy
 * to be enacted by the Receiver at the request of the Domain Owner.
 * Policy applies to the domain queried and to subdomains, unless
 * subdomain policy is explicitly described using the "sp" tag.
 * This tag is mandatory for policy records only, but not for
 * third-party reporting records
 *
 * @see <a href="https://tools.ietf.org/html/rfc7489#section-6.3">RFC-7489, Section 6.3</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum DMARCPolicy {

    /**
     * The Domain Owner requests no specific action be taken
     * regarding delivery of messages.
     */
    none,
    /**
     * The Domain Owner wishes to have email that fails the
     * DMARC mechanism check be treated by Mail Receivers as
     * suspicious. Depending on the capabilities of the Mail
     * Receiver, this can mean "place into spam folder", "scrutinize
     * with additional intensity", and/or "flag as suspicious".
     */
    quarantine,
    /**
     * The Domain Owner wishes for Mail Receivers to reject
     * email that fails the DMARC mechanism check. Rejection SHOULD
     * occur during the SMTP transaction. See
     * <a href="https://tools.ietf.org/html/rfc7489#section-10.3">RFC-7489, Section 10.3</a>
     * for some discussion of SMTP rejection methods and their implications.
     */
    reject
}
