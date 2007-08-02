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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;

/**
 * @author choeger
 * 
 */
public abstract class EnforceableDataObject implements Serializable, Cloneable {

    private ArrayList<String> unset_members = null;

    /**
     * This method must be implemented and it must return a String array
     * containing all names of mandatory members of the corresponding class
     * required to CREATE data.
     * 
     * @return String array containing names of mandatory members or null if
     *         unwanted
     */
    protected abstract String[] getMandatoryMembersCreate();

    /**
     * This method must be implemented and it must return a String array
     * containing all names of mandatory members of the corresponding class
     * required to CHANGE data.
     * 
     * @return String array containing names of mandatory members or null if
     *         unwanted
     */
    protected abstract String[] getMandatoryMembersChange();

    /**
     * This method must be implemented and it must return a String array
     * containing all names of mandatory members of the corresponding class
     * required to DELETE data.
     * 
     * @return String array containing names of mandatory members or null if
     *         unwanted
     */
    protected abstract String[] getMandatoryMembersDelete();

    /**
     * This method must be implemented and it must return a String array
     * containing all names of mandatory members of the corresponding class
     * required to REGISTER data.
     * 
     * @return String array containing names of mandatory members or null if
     *         unwanted
     */
    protected abstract String[] getMandatoryMembersRegister();

    /**
     * @return
     * @throws EnforceableDataObjectException
     */
    public boolean mandatoryCreateMembersSet() throws EnforceableDataObjectException {
        return mandatoryMembersSet(getMandatoryMembersCreate());
    }

    /**
     * @return
     * @throws EnforceableDataObjectException
     */
    public boolean mandatoryChangeMembersSet() throws EnforceableDataObjectException {
        return mandatoryMembersSet(getMandatoryMembersChange());
    }

    /**
     * @return
     * @throws EnforceableDataObjectException
     */
    public boolean mandatoryDeleteMembersSet() throws EnforceableDataObjectException {
        return mandatoryMembersSet(getMandatoryMembersDelete());
    }

    /**
     * @return
     * @throws EnforceableDataObjectException
     */
    public boolean mandatoryRegisterMembersSet() throws EnforceableDataObjectException {
        return mandatoryMembersSet(getMandatoryMembersRegister());
    }

    private boolean mandatoryMembersSet(final String[] members) throws EnforceableDataObjectException {
        this.unset_members.clear();

        try {
            if (members != null && members.length > 0) {
                for (final String m : members) {
                    Field f = this.getClass().getDeclaredField(m);
                    f.setAccessible(true);
                    Object val = f.get(this);
                    if (val == null || (val instanceof String && ((String) val).equals(""))) {
                        this.unset_members.add(m);
                    }
                }
                if (this.unset_members.size() > 0) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (final SecurityException e) {
            throw new EnforceableDataObjectException(e);
        } catch (final NoSuchFieldException e) {
            throw new EnforceableDataObjectException("No such member: " + e.getMessage());
        } catch (final IllegalArgumentException e) {
            throw new EnforceableDataObjectException(e);
        } catch (final IllegalAccessException e) {
            throw new EnforceableDataObjectException(e);
        }
    }

    public ArrayList<String> getUnsetMembers() {
        return this.unset_members;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        final EnforceableDataObject object = (EnforceableDataObject) super.clone();
        object.unset_members = new ArrayList<String>(this.unset_members);
        return object;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder(super.toString());
        ret.append("\n");
        ret.append(" Mandatory members:\n");
        ret.append("  Create: ");
        if (getMandatoryMembersCreate() != null && getMandatoryMembersCreate().length > 0) {
            for (final String m : getMandatoryMembersCreate()) {
                ret.append(m);
                ret.append(" ");
            }
            ret.append("\n");
        } else {
            ret.append(" NONE\n");
        }
        ret.append("  Change:");
        if (getMandatoryMembersChange() != null && getMandatoryMembersChange().length > 0) {
            for (final String m : getMandatoryMembersChange()) {
                ret.append(m);
                ret.append(" ");
            }
            ret.append("\n");
        } else {
            ret.append(" NONE\n");
        }
        ret.append("  Delete:");
        if (getMandatoryMembersDelete() != null && getMandatoryMembersDelete().length > 0) {
            for (final String m : getMandatoryMembersDelete()) {
                ret.append(m);
                ret.append(" ");
            }
            ret.append("\n");
        } else {
            ret.append(" NONE\n");
        }
        ret.append("  Register:");
        if (getMandatoryMembersRegister() != null && getMandatoryMembersRegister().length > 0) {
            for (final String m : getMandatoryMembersRegister()) {
                ret.append(m);
                ret.append(" ");
            }
            ret.append("\n");
        } else {
            ret.append(" NONE\n");
        }

        return ret.toString();
    }

    /**
     * 
     */
    public EnforceableDataObject() {
        this.unset_members = new ArrayList<String>();
    }

}
