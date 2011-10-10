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

package com.openexchange.sessiond.impl;


/**
 * {@link IPRange}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IPRange {

    private final int[] start;
    private final int[] end;

    public IPRange(int[] start, int[] end) {
        super();
        this.start = start;
        this.end = end;
    }

    public IPRange(int[] start) {
        this.start = start;
        this.end = start;
    }

    public int[] getStart() {
        return start;
    }

    public int[] getEnd() {
        return end;
    }

    public boolean contains(String ipAddress) {
        int[] other = parse(ipAddress);
        if (other == null) { // FIXME ipv6
        	return false;
        }
        boolean endCarryOver = false;
        boolean startCarryOver = false;

        for(int i = 0; i < 4; i++) {
            int part = other[i];
            endCarryOver = endCarryOver || part < end[i];
            startCarryOver = startCarryOver || part > start[i];
            if(startCarryOver && endCarryOver) {
                return true;
            }
            if((part < start[i] && ! startCarryOver)|| (part > end[i] && ! endCarryOver) ) {
                return false;
            }
        }
        return true;
    }
    
    // FIXME ipv6
    private static int[] parse(String ipAddress) {
        String[] split = ipAddress.split("\\.");
        if(split.length != 4) {
        	return null;
        }
        int[] parsed = new int[4];
        for(int i = 0; i < 4; i++) {
            parsed[i] = Integer.parseInt(split[i]);
        }
        return parsed;
    }

    public static IPRange parseRange(String string) {
        if(string.contains("-")) {
            String[] addresses = string.split("\\s*-\\s*");
            return new IPRange(parse(addresses[0]), parse(addresses[1]));
        }
        return new IPRange(parse(string));
    }

}
