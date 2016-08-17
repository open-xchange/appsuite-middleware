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

package com.openexchange.ajax.oauth.provider;

import java.io.File;
import java.io.IOException;
import com.google.common.io.Files;


/**
 * {@link IconBytes}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.x.x
 */
public class IconBytes {

    static final byte[] DATA = new byte[] {-1,-40,-1,-32,0,16,74,70,73,70,0,1,2,0,0,1,0,1,0,0,-1,-19,0,-124,80,104,111,116,111,115,104,111,112,32,51,46,48,0,56,66,73,77,4,4,0,0,0,0,0,103,28,2,40,0,98,70,66,77,68,48,49,48,48,48,97,56,54,48,49,48,48,48,48,101,49,48,49,48,48,48,48,50,100,48,50,48,48,48,48,53,57,48,50,48,48,48,48,56,101,48,50,48,48,48,48,48,48,48,51,48,48,48,48,53,52,48,51,48,48,48,48,56,101,48,51,48,48,48,48,99,98,48,51,48,48,48,48,48,57,48,52,48,48,48,48,56,54,48,52,48,48,48,48,0,-1,-37,0,67,0,6,4,5,6,5,4,6,6,5,6,7,7,6,8,10,16,10,10,9,9,10,20,14,15,12,16,23,20,24,24,23,20,22,22,26,29,37,31,26,27,35,28,22,22,32,44,32,35,38,39,41,42,41,25,31,45,48,45,40,48,37,40,41,40,-1,-37,0,67,1,7,7,7,10,8,10,19,10,10,19,40,26,22,26,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,-1,-62,0,17,8,0,50,0,50,3,0,34,0,1,17,1,2,17,1,-1,-60,0,25,0,0,3,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,2,3,6,5,-1,-60,0,24,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,0,2,3,5,-1,-60,0,24,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,0,2,3,5,-1,-38,0,12,3,0,0,1,17,2,17,0,0,1,-15,-11,-61,95,67,-49,-38,-14,-92,-88,38,-111,32,-31,-42,114,29,111,62,-102,-53,89,-45,-25,58,46,87,120,100,26,-50,78,88,-24,-13,109,114,-107,2,42,90,98,-38,104,-128,32,10,-1,-60,0,28,16,1,0,2,3,0,3,0,0,0,0,0,0,0,0,0,0,4,1,3,0,2,16,32,48,64,-1,-38,0,8,1,0,0,1,5,2,-116,-113,9,-28,122,36,-91,56,-55,72,87,105,74,116,101,-28,-96,102,-34,117,-104,-28,-88,-119,16,-18,1,45,59,12,109,108,97,-40,89,-7,-1,0,-1,-60,0,26,17,0,2,2,3,0,0,0,0,0,0,0,0,0,0,0,0,0,2,17,65,32,33,48,-1,-38,0,8,1,2,17,1,63,1,-63,96,-43,-115,21,-53,-1,-60,0,26,17,0,2,2,3,0,0,0,0,0,0,0,0,0,0,0,0,0,2,17,65,32,33,48,-1,-38,0,8,1,1,17,1,63,1,-63,-92,-35,9,55,-53,-1,-60,0,35,16,0,2,2,1,3,3,5,0,0,0,0,0,0,0,0,0,2,3,1,17,0,4,33,49,16,18,82,19,20,48,64,97,-1,-38,0,8,1,0,0,6,63,2,-7,-48,-35,71,-86,100,-49,25,-55,82,-59,-30,85,119,51,-116,77,-56,106,-122,-29,-99,-89,35,-36,-52,-98,-96,-72,17,-100,30,-47,-87,-83,-1,0,122,-95,90,-126,98,-55,94,49,-110,-43,-79,-58,85,85,35,-116,104,-37,53,71,-65,27,70,118,-21,44,30,60,24,-57,-40,-1,-60,0,34,16,1,1,1,0,1,3,3,5,0,0,0,0,0,0,0,0,1,0,17,49,16,33,81,32,65,97,48,-79,-63,-16,-15,-1,-38,0,8,1,0,0,1,63,33,-24,22,-6,65,-122,-39,101,-74,24,59,4,120,-77,77,-73,-122,-72,120,69,-21,-48,93,99,-18,31,-119,-77,-72,-40,-89,-13,-26,59,98,-61,-34,86,-60,99,-96,57,119,12,-75,67,62,-23,7,105,61,-52,-43,-29,124,125,-31,94,66,-45,-9,-30,93,-4,-37,16,-61,109,-78,-37,29,119,-23,-1,0,-1,-38,0,12,3,0,0,1,17,2,17,0,0,16,-90,46,-70,59,85,114,77,-60,60,90,-97,-61,-1,-60,0,30,17,0,2,2,1,5,1,0,0,0,0,0,0,0,0,0,0,0,1,17,33,49,16,32,65,97,-16,81,-1,-38,0,8,1,2,17,1,63,16,99,18,32,75,78,123,18,-83,59,-9,-46,-27,-57,-39,32,100,17,-93,-39,-1,-60,0,30,17,0,2,2,1,5,1,0,0,0,0,0,0,0,0,0,0,1,17,0,33,65,16,32,81,-127,-95,-16,-1,-38,0,8,1,1,17,1,63,16,16,24,78,-121,68,-42,84,38,-10,-62,-5,-119,65,-69,-13,-88,-32,-114,61,-33,-1,-60,0,30,16,1,0,2,2,2,3,1,0,0,0,0,0,0,0,0,0,1,0,17,33,49,16,65,32,81,97,113,-1,-38,0,8,1,0,0,1,63,16,113,71,8,88,-91,-59,-52,30,18,87,42,39,36,-122,32,-90,-80,24,-68,-26,80,-118,-42,40,67,86,-34,-50,-91,-76,-119,-43,-76,-71,-42,51,-79,-78,30,-72,87,-97,-18,46,-99,-83,-72,37,-96,4,-82,123,15,92,20,70,53,106,46,-101,116,-31,51,-45,112,80,2,-124,44,-114,-24,-50,59,106,1,-11,-61,59,53,93,47,41,-102,116,104,-44,-62,38,-33,104,-51,61,-81,-47,-11,66,4,3,86,105,-5,-29,1,15,32,-96,-63,-31,113,120,33,15,31,-1,-39};


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder("private static final byte[] ICON_BYTES = new byte[] {");
        byte[] bytes = Files.toByteArray(new File("/home/steffen/git/backend/openexchange-test/testData/ox_logo_sml.jpg"));
        for (byte b : bytes) {
            sb.append(b).append(',');
        }
        sb.setLength(sb.length() - 1);
        sb.append("};");
        System.out.println(sb.toString());

    }

}
