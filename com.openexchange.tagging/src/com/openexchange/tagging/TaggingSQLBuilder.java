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

package com.openexchange.tagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link TaggingSQLBuilder}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TaggingSQLBuilder {

    private static final Set<String> KEYWORDS = new HashSet<String>() {

        {
            add("OR");
            add(")");
        }
    };

    public SQLStatement build(String string) {
        List<String> tags = new ArrayList<String>();
        string = string.replaceAll("\\(", " ( ");
        string = string.replaceAll("\\)", " ) ");
        String[] elements = string.split("\\s+");
        StringBuilder select = new StringBuilder("SELECT * FROM tags AS t1 ");
        StringBuilder where = new StringBuilder();
        boolean negation = false;
        boolean addJoin = false;
        int joinCounter = 1;
        for (String element : elements) {
            if ("AND".equalsIgnoreCase(element)) {
                addJoin = true;
                where.append("AND ");
            } else if ("NOT".equalsIgnoreCase(element)) {
                negation = true;
            } else if ("(".equals(element)) {
                if (negation) {
                    where.append("NOT ( ");
                    negation = false;
                } else {
                    where.append("( ");
                }
                addJoin = false;
            } else if (isKeyword(element)) {
                where.append(element).append(" ");
            } else {
                String tagExpression = "t1.tag";
                if(addJoin) {
                    joinCounter++;
                    select.append("LEFT JOIN tags AS t").append(joinCounter).append(" ON t1.cid = t").append(joinCounter).append(".cid AND t1.object_id = t").append(joinCounter).append(".object_id ").append(tagsDiffer(joinCounter));
                    tagExpression = "t"+joinCounter+".tag";
                    addJoin = false;
                }
                
                if (negation) {
                    where.append("( ").append(tagExpression).append(" != ? OR ").append(tagExpression).append(" IS NULL ) ");
                } else {
                    where.append(tagExpression).append(" = ? ");
                }
                
                tags.add(element);
                negation = false;
                
            }
        }
        where.append(")");
        return new SQLStatement(select.toString()+"WHERE t1.cid = ? AND (" + where.toString(), tags);
    }

    private String tagsDiffer(int joinCounter) {
        StringBuilder b = new StringBuilder("AND ");
        for(int i = 1; i <= joinCounter; i++) {
            for(int j = i+1; j <= joinCounter; j++) {
                b.append("t").append(i).append(".tag != t").append(j).append(".tag AND ");
            }
        }
        b.setLength(b.length()-4);
        return b.toString();
    }

    private boolean isKeyword(String element) {
        for (String keyword : KEYWORDS) {
            if (keyword.equalsIgnoreCase(element)) {
                return true;
            }
        }
        return false;
    }
}
