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

package com.openexchange.messaging;

/**
 * {@link CaptchaParams} - Simple container class for captcha parameters,
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CaptchaParams {

    private String challenge;

    private String response;

    private String address;

    /**
     * Initializes a new {@link CaptchaParams}.
     */
    public CaptchaParams() {
        super();
    }

    /**
     * Gets the challenge.
     *
     * @return The challenge
     */
    public String getChallenge() {
        return challenge;
    }

    /**
     * Sets the challenge.
     *
     * @param challenge The challenge
     */
    public void setChallenge(final String challenge) {
        this.challenge = challenge;
    }

    /**
     * Gets the response string.
     *
     * @return The response string
     */
    public String getResponse() {
        return response;
    }

    /**
     * Sets the response string.
     *
     * @param response The response string.
     */
    public void setResponse(final String response) {
        this.response = response;
    }

    /**
     * Gets the host; either an IP address or a host name.
     *
     * @return The host name
     */
    public String getHost() {
        return address;
    }

    /**
     * Sets the remote address.
     *
     * @param address The remote address
     */
    public void setAddress(final String address) {
        this.address = address;
    }

}
