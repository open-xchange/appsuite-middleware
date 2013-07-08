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

package com.openexchange.index.solr;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.groupware.Types;
import com.openexchange.java.Strings;


/**
 * {@link ModuleSet}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ModuleSet {

    private final Set<Integer> modules = new HashSet<Integer>();


    public ModuleSet() {
        super();
    }

    public ModuleSet(String moduleStr) {
        super();
        if (moduleStr != null) {
            String[] split = Strings.splitByComma(moduleStr);
            for (String s : split) {
                try {
                    Integer module = new Integer(s.trim());
                    if (module.intValue() == Types.ATTACHMENT) {
                        continue;
                    }

                    modules.add(module);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
    }

    public boolean addModule(int module) {
        if (module == Types.ATTACHMENT) {
            return true;
        }

        return modules.add(module);
    }

    public boolean removeModule(int module) {
        if (module == Types.ATTACHMENT) {
            return false;
        }

        return modules.remove(module);
    }

    public boolean containsModule(int module) {
        if (module == Types.ATTACHMENT) {
            return true;
        }

        return modules.contains(module);
    }

    /**
     * Returns the string representation of this module set.
     */
    @Override
    public String toString() {
        if (modules.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Integer module : modules) {
            sb.append(module);
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

}
