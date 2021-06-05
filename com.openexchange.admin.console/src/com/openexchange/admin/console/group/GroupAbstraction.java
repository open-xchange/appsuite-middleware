/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
package com.openexchange.admin.console.group;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Group;

public abstract class GroupAbstraction extends ObjectNamingAbstraction {

    protected CLIOption addMemberOption = null;
    protected CLIOption removeMemberOption = null;
    protected CLIOption IdOption = null;
    protected CLIOption nameOption = null;
    protected CLIOption displayNameOption = null;
    protected CLIOption mailOption = null;

    protected static final String OPT_NAME_GROUPNAME_LONG = "name";
    protected static final char OPT_NAME_GROUPNAME = 'n';

    protected static final String OPT_NAME_GROUPDISPLAYNAME_LONG = "displayname";
    protected static final char OPT_NAME_GROUPDISPLAYNAME = 'd';

    protected static final String OPT_NAME_GROUPID_LONG = "groupid";
    protected static final char OPT_NAME_GROUPID = 'i';

    protected static final String OPT_NAME_ADDMEMBERS_LONG = "addmembers";
    protected static final char OPT_NAME_ADDMEMBERS = 'a';

    protected static final String OPT_NAME_REMOVEMEMBERS_LONG = "removemembers";
    protected static final char OPT_NAME_REMOVEMEMBERS = 'r';

    protected static final String OPT_MAILADDRESS_LONG = "mailaddress";
    protected static final char OPT_MAILADDRESS_SHORT = 'm';

    // For right error output
    protected String groupid = null;
    protected String groupName = null;

    protected void setAddMembersOption(final AdminParser admp,final boolean required) {
        addMemberOption = setShortLongOpt(admp,OPT_NAME_ADDMEMBERS, OPT_NAME_ADDMEMBERS_LONG, "userid(s)", "List of members to add to group, separated by comma", required);
//        retval.setArgName(OPT_NAME_ADDMEMBERS_LONG);

    }

    protected void setRemoveMembersOption(final AdminParser admp,final boolean required) {
        removeMemberOption = setShortLongOpt(admp,OPT_NAME_REMOVEMEMBERS, OPT_NAME_REMOVEMEMBERS_LONG, "List of members to be removed from group, separated by comma", true, convertBooleantoTriState(required));
    }

    protected void setGroupIdOption(final AdminParser admp, final NeededQuadState required) {
        IdOption = setShortLongOpt(admp,OPT_NAME_GROUPID, OPT_NAME_GROUPID_LONG, "The id of the group", true, required);
    }

    protected void setGroupNameOption(final AdminParser admp, final NeededQuadState required) {
        nameOption= setShortLongOpt(admp,OPT_NAME_GROUPNAME, OPT_NAME_GROUPNAME_LONG, "The group name", true, required);
    }

    protected void setGroupDisplayNameOption(final AdminParser admp,final boolean required) {
        displayNameOption = setShortLongOpt(admp,OPT_NAME_GROUPDISPLAYNAME, OPT_NAME_GROUPDISPLAYNAME_LONG, "The displayname for the Group", true, convertBooleantoTriState(required));
    }

    protected void setMailOption(final AdminParser admp,final boolean required) {
        mailOption = setShortLongOpt(admp,OPT_MAILADDRESS_SHORT, OPT_MAILADDRESS_LONG, "email address if the group should receive mail", true, convertBooleantoTriState(required));
    }

    protected final OXGroupInterface getGroupInterface() throws NotBoundException, MalformedURLException, RemoteException {
        return (OXGroupInterface) Naming.lookup(RMI_HOSTNAME + OXGroupInterface.RMI_NAME);
    }

    @Override
    protected String getObjectName() {
        return "group";
    }

    protected final void parseAndSetGroupId(final AdminParser parser, final Group grp) {
        groupid = (String) parser.getOptionValue(this.IdOption);
        if (null != groupid) {
            grp.setId(Integer.valueOf(groupid));
        }
    }

    protected void parseAndSetGroupName(final AdminParser parser, final Group grp) {
        groupName = (String) parser.getOptionValue(this.nameOption);
        if (null != groupName) {
            grp.setName(groupName);
        }
    }

    private void parseAndSetDisplayName(final AdminParser parser, final Group grp) {
        final String displayName = (String) parser.getOptionValue(this.displayNameOption);
        if (displayName != null) {
            grp.setDisplayname(displayName);
        }
    }

    protected final void parseAndSetGroupAndDisplayName(final AdminParser parser, final Group grp) {
        parseAndSetGroupName(parser, grp);
        parseAndSetDisplayName(parser, grp);
    }
}
