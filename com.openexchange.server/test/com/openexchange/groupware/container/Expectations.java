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

package com.openexchange.groupware.container;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;


/**
 * A set of keys that are to be checked and a set of expected values for these keys.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Expectations extends KeyValueHolder {

    public Expectations(Changes changes){
        setMap(changes.getMap());
    }

    public Expectations(){
        setMap(new HashMap<Integer, Object>());
    }

    public void verify(CommonObject actual){
        verify("", actual);
    }

    public void verify(String message, CommonObject actual){
        int successfulTests = 0;
        for(Integer key: getMap().keySet()){
            Object expectedValue = getMap().get(key);
            Object actualValue = actual.get(key.intValue());
            if (actual.contains(key.intValue())) {
                assertEquals(message + " Field "+key+" does not match expectation ("+successfulTests+" successful tests before this)", expectedValue, actualValue);
            } else {
                assertEquals(message + " Field "+key+" does not match expectation ("+successfulTests+" successful tests before this)", expectedValue, null);
            }
            successfulTests++;
        }
    }
}
