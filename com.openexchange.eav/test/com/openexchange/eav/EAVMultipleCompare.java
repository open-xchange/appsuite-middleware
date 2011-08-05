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

package com.openexchange.eav;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link EAVMultipleCompare}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EAVMultipleCompare implements EAVTypeSwitcher {

    @Override
    public Object binary(Object... args) {
        List<List<Byte>> list1 = convert((InputStream[]) args[0]);
        List<List<Byte>> list2 = convert((InputStream[]) args[1]);

        if(list1.size() != list2.size()) {
            return false;
        }

        list1.removeAll(list2);

        return list1.isEmpty();
    }

    private List<List<Byte>> convert(InputStream[] inputStreams) {
        try {
            List<List<Byte>> retval = new ArrayList<List<Byte>>(inputStreams.length);
            for (InputStream inputStream : inputStreams) {
                List<Byte> bytes = new ArrayList<Byte>();
                int b = -1;
                while((b = inputStream.read()) != -1) {
                    bytes.add((byte)b);
                }
                inputStream.close();
                retval.add(bytes);
            }
            return retval;
        } catch (IOException x) {
            x.printStackTrace();
            return null;
        }
    }

    @Override
    public Object bool(Object... args) {
        return multisetWiseComparison(args[0], args[1]);
    }

    @Override
    public Object date(Object... args) {
        return multisetWiseComparison(args[0], args[1]);
    }

    @Override
    public Object number(Object... args) {
        return multisetWiseComparison(coerceLong(args[0]), coerceLong(args[1]));
    }

    private Object coerceLong(Object object) {
        Number[] orig = (Number[]) object;
        if(orig.length == 0) {
            return new Long[0];
        }

        Long[] longs = new Long[orig.length];
        int i = 0;
        for(Number number : orig) {
            longs[i] = Long.valueOf(orig[i].toString());
            i++;
        }

        return longs;
    }

    @Override
    public Object object(Object... args) {
        return true;
    }

    @Override
    public Object string(Object... args) {
        return multisetWiseComparison(args[0], args[1]);
    }

    @Override
    public Object time(Object... args) {
        return multisetWiseComparison(args[0], args[1]);
    }

    @Override
    public Object nullValue(Object... args) {
        return true;
    }

    private boolean multisetWiseComparison(Object object, Object object2) {
        if(object == object2) {
            return true;
        }
        if(object == null) {
            return false;
        }
        if(object2 == null) {
            return false;
        }
        List<Object> list1 = new ArrayList<Object>(Arrays.asList((Object[])object));
        List<Object> list2 = new ArrayList<Object>(Arrays.asList((Object[])object2));

        if(list1.size() != list2.size()) {
            return false;
        }

        list1.removeAll(list2);

        return list1.isEmpty();
    }

}
