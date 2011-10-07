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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.obs.api;

/**
 * {@link Code}s for the package status.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum Code {

    SUCCEEDED("succeeded"),
    EXPANSION_ERROR("expansion error"),
    BROKEN("broken"),
    FAILED("failed"),
    DISABLED("disabled"),
    BLOCKED("blocked"),
    BUILDING("building"),
    SCHEDULED("scheduled"),
    FINISHED("finished"),
    DISPATCHING("dispatching"),
    UNKNOWN("unknown"),
    EXCLUDED("excluded"),
    UNRESOLVABLE("unresolvable");

    private final String str;

    private Code(final String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    public final static Code parseCode(final String code) throws BuildServiceException {
        if (SUCCEEDED.str.equalsIgnoreCase(code)) {
            return SUCCEEDED;
        } else if (EXPANSION_ERROR.str.equalsIgnoreCase(code)) {
            return EXPANSION_ERROR;
        } else if (BROKEN.str.equalsIgnoreCase(code)) {
            return BROKEN;
        } else if (FAILED.str.equalsIgnoreCase(code)) {
            return FAILED;
        } else if (DISABLED.str.equalsIgnoreCase(code)) {
            return DISABLED;
        } else if (BLOCKED.str.equalsIgnoreCase(code)) {
            return BLOCKED;
        } else if (BUILDING.str.equalsIgnoreCase(code)) {
            return BUILDING;
        } else if (SCHEDULED.str.equalsIgnoreCase(code)) {
            return SCHEDULED;
        } else if (FINISHED.str.equalsIgnoreCase(code)) {
            return FINISHED;
        } else if (EXCLUDED.str.equalsIgnoreCase(code)) {
            return EXCLUDED;
        } else if (DISPATCHING.str.equalsIgnoreCase(code)) {
            return DISPATCHING;
        } else if (UNRESOLVABLE.str.equalsIgnoreCase(code)) {
            return UNRESOLVABLE;
        }
        throw new BuildServiceException("Unknown package status code: \"" + code + "\".");
    }
}