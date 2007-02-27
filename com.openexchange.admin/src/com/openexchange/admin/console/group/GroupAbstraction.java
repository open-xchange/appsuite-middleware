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
package com.openexchange.admin.console.group;

import org.apache.commons.cli.Option;

import com.openexchange.admin.console.BasicCommandlineOptions;

public abstract class GroupAbstraction extends BasicCommandlineOptions {

    protected static final String OPT_NAME_GROUPNAME_LONG = "name";
    protected static final String OPT_NAME_GROUPNAME = "n";

    protected static final String OPT_NAME_GROUPDISPLAYNAME_LONG = "displayname";
    protected static final String OPT_NAME_GROUPDISPLAYNAME = "d";

    protected static final String OPT_NAME_GROUPID_LONG = "groupid";
    protected static final String OPT_NAME_GROUPID = "i";

    protected static final String OPT_NAME_ADDMEMBERS_LONG = "addmembers";
    protected static final String OPT_NAME_ADDMEMBERS = "a";
    
    protected static final String OPT_NAME_REMOVEMEMBERS_LONG = "removemembers";
    protected static final String OPT_NAME_REMOVEMEMBERS = "r";

    protected Option getAddMembersOption() {
        final Option retval = getShortLongOpt(OPT_NAME_ADDMEMBERS, OPT_NAME_ADDMEMBERS_LONG, "List of members to add to group", true, true);
        retval.setArgName(OPT_NAME_ADDMEMBERS_LONG);
        return retval;
    }

    protected Option getRemoveMembersOption() {
        final Option retval = getShortLongOpt(OPT_NAME_REMOVEMEMBERS, OPT_NAME_REMOVEMEMBERS_LONG, "List of members to be removed from group", true, true);
        retval.setArgName(OPT_NAME_REMOVEMEMBERS_LONG);
        return retval;
    }

    protected Option getGroupIdOption() {
        final Option retval = getShortLongOpt(OPT_NAME_GROUPID, OPT_NAME_GROUPID_LONG, "The id of the group which will be deleted", true, true);
        retval.setArgName("id");
        return retval;
    }
    
    protected Option getGroupNameOption() {
        final Option retval = getShortLongOpt(OPT_NAME_GROUPNAME, OPT_NAME_GROUPNAME_LONG, "The group name", true, true);
        retval.setArgName(OPT_NAME_GROUPDISPLAYNAME_LONG);
        return retval;
    }
    
    protected Option getGroupDisplayNameOption() {
        final Option retval = getShortLongOpt(OPT_NAME_GROUPDISPLAYNAME, OPT_NAME_GROUPDISPLAYNAME_LONG, "The displayname for the Group", true, true);
        retval.setArgName(OPT_NAME_GROUPNAME_LONG);
        return retval;
    }
}
