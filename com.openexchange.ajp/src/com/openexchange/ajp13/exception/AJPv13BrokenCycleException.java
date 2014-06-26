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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.exception;

/**
 * {@link AJPv13BrokenCycleException} - Thrown if a broken AJP cycle is detected
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJPv13BrokenCycleException extends AJPv13Exception {

    private static final long serialVersionUID = -11763074640120576L;

    private byte[] payload;

    /**
     * The optional package dump.
     */
    protected String dump;

    /**
     * Initializes a new {@link AJPv13BrokenCycleException}
     */
    public AJPv13BrokenCycleException() {
        super(AJPCode.BROKEN_AJP_CYCLE, false);
    }

    /**
     * Initializes a new {@link AJPv13BrokenCycleException}.
     *
     * @param code The code
     * @param args The arguments
     */
    protected AJPv13BrokenCycleException(final AJPCode code, final Object... args) {
        super(code, false, args);
    }

    /**
     * Sets the package's payload.
     *
     * @param payload the payload
     * @return This exception with payload applied
     */
    public AJPv13BrokenCycleException setPayload(final byte[] payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Gets the package payload.
     *
     * @return The payload or <code>null</code>
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Gets the dump.
     *
     * @return The dump
     */
    public String getDump() {
        return dump;
    }

    /**
     * Sets the dump.
     *
     * @param dumped The dump
     */
    public void setDump(final String dumped) {
        this.dump = dumped;
    }

}
