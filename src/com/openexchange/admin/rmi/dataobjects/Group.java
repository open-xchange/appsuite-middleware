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

package com.openexchange.admin.rmi.dataobjects;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.openexchange.admin.rmi.extensions.OXCommonExtensionInterface;
import com.openexchange.admin.rmi.extensions.OXGroupExtensionInterface;

public class Group extends ExtendableDataObject {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -916912847701699619L;

    private Integer id;

    private String name;

    private String displayname;

    private Integer[] members;

    public Group() {
        super();
        init();
    }

    
    public Group(Integer id) {
        super();
        init();
        this.id = id;
    }

    /**
     * @param id
     * @param name
     * @param displayname
     */
    public Group(Integer id, String name, String displayname) {
        super();
        init();
        this.id = id;
        this.name = name;
        this.displayname = displayname;
    }

    private void init(){
        initExtendable();
        this.id = null;
        this.name = null;
        this.displayname = null;
        this.members = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer val) {
        this.id = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        this.name = val;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String val) {
        this.displayname = val;
    }

    public Integer[] getMembers() {
        return members;
    }

    public void setMembers(Integer[] val) {
        this.members = val;
    }


    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                Object ob = f.get(this);
                String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }

    /**
     * @param extension
     * @deprecated Please remove the usage of this method as fast as you can because it used a dangerous downcast.
     * This method will go away with the next update
     */
    public void addExtension(final OXGroupExtensionInterface extension) {
        getAllExtensionsAsHash().put(extension.getExtensionName(), extension);
    }

    /**
     * @return
     * @deprecated Please remove the usage of this method as fast as you can because it used a dangerous downcast.
     * This method will go away with the next update
     */
    public ArrayList<OXGroupExtensionInterface> getExtensions() {
        final ArrayList<OXGroupExtensionInterface> retval = new ArrayList<OXGroupExtensionInterface>();
        for (final OXCommonExtensionInterface commoninterface : getAllExtensionsAsHash().values()) {
            retval.add((OXGroupExtensionInterface) commoninterface);
        }
        return retval;
    }
    
    /**
     * @param o
     * @return
     * @deprecated Please remove the usage of this method as fast as you can because it used a dangerous downcast.
     * This method will go away with the next update
     */
    public boolean removeExtension(final OXGroupExtensionInterface o) {
        if (null == getAllExtensionsAsHash().remove(o)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method is used to get an extensions through the name of this
     * extension. This first occurence will be returned, or null if no fitting
     * extension was found.
     * 
     * @param extname a String for the extension
     * @return the {@link OXGroupExtensionInterface} with extname
     * @deprecated Please remove the usage of this method as fast as you can because it used a dangerous downcast.
     * This method will go away with the next update
     */
    public OXGroupExtensionInterface getExtensionbyName(final String extname) {
        for (final OXCommonExtensionInterface ext : getAllExtensionsAsHash().values()) {
            if (ext.getExtensionName().equals(extname)) {
                return (OXGroupExtensionInterface) ext;
            }
        }
        return null;
    }

    @Override
    protected String[] getMandatoryMembersCreate() {
        return new String[]{ "displayname", "name" };
    }
    
    @Override
    protected final String[] getMandatoryMembersChange() {
        return new String[]{ "id" };
    }
}
