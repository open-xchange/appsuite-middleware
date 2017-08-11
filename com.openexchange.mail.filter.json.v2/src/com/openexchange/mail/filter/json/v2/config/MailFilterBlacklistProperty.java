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

package com.openexchange.mail.filter.json.v2.config;

import com.openexchange.config.lean.Property;

/**
 *
 * {@link MailFilterBlacklistProperty} defines properties to blacklist mailfilter elements. E.g. actions or comparisons.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class MailFilterBlacklistProperty implements Property {

    public enum BasicGroup {
        /**
         * Specifies the actions group.
         */
        actions,

        /**
         * Specifies the tests group
         */
        tests,

        /**
         * Specifies the comparisons group
         */
        comparisons
    }

    public enum Field {

        /**
         * Specifies the comparisons element.
         *
         * Note: This element is similar to the comparisons group but only blacklists comparisons for a single test
         */
        comparisons,

        /**
         * Specifies the headers element
         */
        headers,

        /**
         * Specifies the parts element
         */
        parts;

        public static Field getFieldByName(String name){
            for(Field ele: Field.values()){
                if(ele.name().equals(name.toLowerCase())){
                    return ele;
                }
            }
            return null;
        }
    }

    private static final String DOT = ".";
    private static final String PREFIX = "com.openexchange.mail.filter.blacklist.";
    private final String fqn;

    private final BasicGroup base;
    private final String sub;
    private final Field field;

    /**
     *
     * Initializes a new {@link MailFilterBlacklistProperty}.
     *
     * @param group
     */
    public MailFilterBlacklistProperty(BasicGroup group) {
        this.fqn = PREFIX + group.name();
        this.base = group;
        this.sub = null;
        this.field = null;
    }

    /**
     * Initializes a new {@link MailFilterBlacklistProperty}.
     *
     * @param group The base group (e.g. tests)
     * @param subGroup The sub group (e.g. address)
     * @param field The field (e.g. comparisons)
     */
    public MailFilterBlacklistProperty(BasicGroup group, String subGroup, Field field) {
        StringBuilder builder = new StringBuilder(PREFIX);
        builder.append(group.name()).append(DOT).append(subGroup).append(DOT).append(field);
        this.fqn = builder.toString();
        this.base = group;
        this.sub = subGroup;
        this.field = field;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public <T> T getDefaultValue(Class<T> cls) {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MailFilterBlacklistProperty){
            return  this.fqn.equals(((MailFilterBlacklistProperty) obj).getFQPropertyName());
        }
        return false;
    }


    /**
     * Gets the base. See {@link BasicGroup}
     *
     * @return The base
     */
    public BasicGroup getBase() {
        return base;
    }


    /**
     * Gets the sub. E.g. "address"
     *
     * @return The sub
     */
    public String getSub() {
        return sub;
    }


    /**
     * Gets the name. See {@link Field}
     *
     * @return The name
     */
    public Field getField() {
        return field;
    }
}
