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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.eav;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.exception.OXException;

/**
 * {@link EAVTypeOptionVerifier}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EAVTypeOptionVerifier implements EAVTypeSwitcher {

    @Override
    public Object binary(Object... args) {
        return noOptions(EAVType.BINARY, args);
    }

    @Override
    public Object bool(Object... args) {
        return noOptions(EAVType.BOOLEAN, args);
    }

    @Override
    public Object date(Object... args) {
        return noOptions(EAVType.DATE, args);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.eav.EAVTypeSwitcher#nullValue(java.lang.Object[])
     */
    @Override
    public Object nullValue(Object... args) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.eav.EAVTypeSwitcher#number(java.lang.Object[])
     */
    @Override
    public Object number(Object... args) {
        return noOptions(EAVType.NUMBER, args);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.eav.EAVTypeSwitcher#object(java.lang.Object[])
     */
    @Override
    public Object object(Object... args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object string(Object... args) {
        return noOptions(EAVType.STRING, args);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.eav.EAVTypeSwitcher#time(java.lang.Object[])
     */
    @Override
    public Object time(Object... args) {
        Map<String, Object> options = (Map<String, Object>) args[0];
        String timezone = (String) options.remove("timezone");
        if (options.size() != 0) {
            return EAVErrorMessage.UNKNOWN_OPTION.create(EAVType.TIME, "timezone");
        }
        if (timezone == null || isKnownAbbreviation(timezone) || looksLikeGMTTimeZone(timezone)) {
            return null;
        }
        return EAVErrorMessage.ILLEGAL_OPTION.create(timezone, "timezone");
    }

    private static final Set<String> ABBREVS = new HashSet<String>(){{
        for(String abbrev : TimeZone.getAvailableIDs()) {
            add(abbrev);
        }
    }};


    private boolean isKnownAbbreviation(String timezone) {
        return ABBREVS.contains(timezone);
    }

    private boolean looksLikeGMTTimeZone(String timezone) {
        return timezone.contains("GMT");
    }

    private OXException noOptions(EAVType type, Object... args) {
        Map<String, Object> options = (Map<String, Object>) args[0];
        if (options.size() != 0) {
            return EAVErrorMessage.NO_OPTIONS.create(type);
        }
        return null;
    }

}
