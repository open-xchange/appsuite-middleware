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

package com.openexchange.fitnesse.environment;

import static com.openexchange.fitnesse.wrappers.FitnesseResult.green;
import static fitnesse.util.ListUtility.list;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.user.UserResolver;
import com.openexchange.fitnesse.FitnesseEnvironment;
import com.openexchange.fitnesse.SlimTableTable;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link SearchUsers}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SearchUsers implements SlimTableTable {

    private String pattern = "*";

    private FitnesseEnvironment env;

    public SearchUsers() {
        this.env = FitnesseEnvironment.getInstance();
    }

    public List<Object> doTable(List<List<String>> table) throws Exception {
        pattern = getPattern(table);
        
        User[] users = runQuery();
        
        List<String> fields = getFields(table);
        
        List<Object> userRows = new ArrayList<Object>();
        if(addPatternToOutput()) {
            userRows.add(list(green(pattern)));
        }
        userRows.add(green(fields));
        for (User user : users) {
            List<String> values = new ArrayList<String>(fields.size());
            for(String field : fields) {
                values.add(green(getValue(user, field)));
            }
            userRows.add(values);
        }
        return userRows;
    }

    
    protected List<String> getFields(List<List<String>> table) {
        if(table.size() > 1) {
            return table.get(1);
        } else {
            return list ("display_name", "given_name", "sur_name", "mail");
        }
    }

    protected String getPattern(List<List<String>> table) {
        return table.get(0).get(0);
    }

    protected boolean addPatternToOutput() {
        return true;
    }

    private String getValue(User user, String field) {
        if("display_name".equalsIgnoreCase(field)) {
            return user.getDisplayName();
        } else if ("given_name".equalsIgnoreCase(field)) {
            return user.getGivenName();
        } else if ("sur_name".equalsIgnoreCase(field)) {
            return user.getSurname();
        } else if ("mail".equalsIgnoreCase(field)) {
            return user.getMail();
        } else {
            return "error: Unknown Field "+field;
        }
    }

    private User[] runQuery() throws AjaxException, IOException, SAXException, JSONException {
        UserResolver userResolver = new UserResolver(env.getClient());
        return userResolver.resolveUser(pattern);
    }


    /*private String resolveGroups(int[] groupIds) throws AjaxException, OXJSONException, IOException, SAXException, JSONException {
        if(null == groupIds) { return ""; }
        GroupResolver resolver = new GroupResolver(env.getClient());
        Group[] groups = resolver.loadGroups(groupIds);
        StringBuilder groupList = new StringBuilder();
                      
        for(Group group : groups) {
            groupList.append(group.getDisplayName()).append(",");
        }
        groupList.setLength(groupList.length()-1);
        return groupList.toString();
    }*/

}
