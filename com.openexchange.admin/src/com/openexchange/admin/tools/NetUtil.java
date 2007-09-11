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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.tools;

/**
 * @author choeger
 * 
 */
public class NetUtil {

    /**
     * INTERNAL: check if address or mask is a valid dotted decimal notation
     *  
     * @param qdot
     * @return
     */
    private static boolean isValidDDN(final String qdot) {
        if( qdot.length() == 0 ) {
            return false;
        }
        if( qdot.replaceAll("[0-9.]", "").length() > 0 ) {
            return false;
        }
        if( qdot.split("\\.").length < 4 ) {
            return false;
        }
        return true;
    }
    
    /**
     * check if mask is a valid netmask in dotted decimal notation.
     * 
     * @param mask
     * @return
     */
    public static boolean isValidNetmask(final String mask) {
        if (mask == null) {
            return false;
        } else {
            return isValidDDN(mask);
        }
    }

    /**
     * @param ipmask
     * @return
     */
    public static boolean isValidIPNetmask(final String ipmask) {
        if (ipmask == null) {
            return false;
        } else {
            if (!ipmask.contains("/")) {
                return false;
            }
            final String ip = ipmask.split("/")[0];
            if( !isValidDDN(ip) ) {
                return false;
            }
            final String mask = ipmask.split("/")[1];
            if( mask.contains(".") ) {
                return isValidNetmask(mask);
            }
            if( mask.replaceAll("[0-9]", "").length() > 0 )  {
                return false;
            }
            return true;
        }
    }

    /**
     * return dotted decimal notation representation as a String of the CIDR
     * representation of the netmask
     * 
     * @param cidr
     * @return
     */
    public static final String CIDR2Mask(final int cidr) {
        int mask = cidr;
        String ret = "";
        for (int p = 0; p < 4; p++) {
            int bitset = 0;
            for (int bs = 0; bs < 8; bs++) {
                if (mask > 0) {
                    bitset |= (1 << bs);
                    mask--;
                }
            }
            ret += bitset + (p < 3 ? "." : "");
        }
        return ret;
    }
}
