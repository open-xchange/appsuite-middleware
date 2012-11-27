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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.json;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.json.CharArrayPool;
import org.json.CharArrayPool.Part;


/**
 * {@link JSONMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONMBeanImpl extends StandardMBean implements JSONMBean {

    /**
     * Initializes a new {@link JSONMBeanImpl}.
     */
    public JSONMBeanImpl() throws NotCompliantMBeanException {
        super(JSONMBean.class);
    }

    @Override
    public long getMisses() {
        return CharArrayPool.getMisses();
    }

    @Override
    public int getTotalSmall() {
        return CharArrayPool.getTotal(Part.SMALL);
    }

    @Override
    public int getInUseSmall() {
        return CharArrayPool.getInUse(Part.SMALL);
    }

    @Override
    public int getPooledSmall() {
        return CharArrayPool.getPooled(Part.SMALL);
    }

    @Override
    public int getTotalMedium() {
        return CharArrayPool.getTotal(Part.MEDIUM);
    }

    @Override
    public int getInUseMedium() {
        return CharArrayPool.getInUse(Part.MEDIUM);
    }
 
    @Override
    public int getPooledMedium() {
        return CharArrayPool.getPooled(Part.LARGE);
    }

    @Override
    public int getTotalLarge() {
        return CharArrayPool.getTotal(Part.LARGE);
    }

    @Override
    public int getInUseLarge() {
        return CharArrayPool.getInUse(Part.LARGE);
    }

    @Override
    public int getPooledLarge() {
        return CharArrayPool.getPooled(Part.LARGE);
    }

}
