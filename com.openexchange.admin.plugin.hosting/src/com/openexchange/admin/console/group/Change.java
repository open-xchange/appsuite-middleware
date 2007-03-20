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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends GroupAbstraction {

    public static void main(String[] args) {
        new Change(args);
    }

    private Change() {
        
    }

    protected Option getAddMembersOption() {
        final Option retval = getShortLongOpt(OPT_NAME_ADDMEMBERS, OPT_NAME_ADDMEMBERS_LONG, "List of members to add to group", true, false);
        retval.setArgName(OPT_NAME_ADDMEMBERS_LONG);
        return retval;
    }

    protected Option getRemoveMembersOption() {
        final Option retval = getShortLongOpt(OPT_NAME_REMOVEMEMBERS, OPT_NAME_REMOVEMEMBERS_LONG, "List of members to be removed from group", true, false);
        retval.setArgName(OPT_NAME_REMOVEMEMBERS_LONG);
        return retval;
    }

    protected Option getGroupIdOption() {
        final Option retval = getShortLongOpt(OPT_NAME_GROUPID, OPT_NAME_GROUPID_LONG, "The id of the group which will be deleted", true, true);
        retval.setArgName("id");
        return retval;
    }
    
    protected Option getGroupNameOption() {
        final Option retval = getShortLongOpt(OPT_NAME_GROUPNAME, OPT_NAME_GROUPNAME_LONG, "The group name", true, false);
        retval.setArgName(OPT_NAME_GROUPDISPLAYNAME_LONG);
        return retval;
    }
    
    protected Option getGroupDisplayNameOption() {
        final Option retval = getShortLongOpt(OPT_NAME_GROUPDISPLAYNAME, OPT_NAME_GROUPDISPLAYNAME_LONG, "The displayname for the Group", true, false);
        retval.setArgName(OPT_NAME_GROUPNAME_LONG);
        return retval;
    }

    public Change(String[] args2) {
        
        final CommandLineParser parser = new PosixParser();

        final Options options = getDefaultCommandLineOptions();

        // create options for this command line tool        
        options.addOption(getGroupIdOption());
        options.addOption(getGroupNameOption());
        options.addOption(getGroupDisplayNameOption());
        options.addOption(getAddMembersOption());
        options.addOption(getRemoveMembersOption());

        try {
            final CommandLine cmd = parser.parse(options, args2);
            final Context ctx = new Context(DEFAULT_CONTEXT);

            if (cmd.hasOption(OPT_NAME_CONTEXT_SHORT)) {
                ctx.setID(Integer.parseInt(cmd.getOptionValue(OPT_NAME_CONTEXT_SHORT)));
            }

            final Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT), cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));

            final OXGroupInterface oxgrp = (OXGroupInterface) Naming.lookup(OXGroupInterface.RMI_NAME);
            final Group grp = new Group();

            final int groupid = Integer.valueOf(cmd.getOptionValue(OPT_NAME_GROUPID));
            grp.setId(groupid);
            
            int []newMemberList = null;
            int []removeMemberList = null;
            if(cmd.hasOption(OPT_NAME_ADDMEMBERS)) {
                final String tmpmembers = cmd.getOptionValue(OPT_NAME_ADDMEMBERS);
                ArrayList<Integer> newmembers = new ArrayList<Integer>();
                for(String member : tmpmembers.split(",") ) {
                    newmembers.add(Integer.parseInt(member));
                }
                if(newmembers.size() > 0) {
                    newMemberList = new int[newmembers.size()];
                    for(int i=0; i<newmembers.size(); i++) {
                        newMemberList[i] = newmembers.get(i);
                    }
                }
            }
            if(cmd.hasOption(OPT_NAME_REMOVEMEMBERS)) {
                final String tmpmembers = cmd.getOptionValue(OPT_NAME_REMOVEMEMBERS);
                ArrayList<Integer> removemembers = new ArrayList<Integer>();
                for(String member : tmpmembers.split(",") ) {
                    removemembers.add(Integer.parseInt(member));
                }
                if(removemembers.size() > 0) {
                    removeMemberList = new int[removemembers.size()];
                    for(int i=0; i<removemembers.size(); i++) {
                        removeMemberList[i] = removemembers.get(i);
                    }
                }
            }
            if(newMemberList != null) {
                oxgrp.addMember(ctx, groupid, newMemberList, auth);
            }
            if(removeMemberList != null) {
                oxgrp.removeMember(ctx, groupid, removeMemberList, auth);
            }

            oxgrp.change(ctx, grp, auth);
        }catch(java.rmi.ConnectException neti){
            printError(neti.getMessage());            
        }catch(org.apache.commons.cli.MissingArgumentException as){
            printError("Missing arguments on the command line: " + as.getMessage());;
            printHelpText("changegroup", options);
        }catch(org.apache.commons.cli.UnrecognizedOptionException ux){
            printError("Unrecognized options on the command line: " + ux.getMessage());;
            printHelpText("changegroup", options);
        } catch (final org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());;
            printHelpText("changegroup", options);
        } catch (final ParseException e) {
            printError("Error parsing the command line. Message was: " + e.getMessage());            
            printHelpText("changegroup", options);
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
        } catch (final NotBoundException e) {
            printServerResponse(e.getMessage());
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
        } catch (final NoSuchContextException e) {
            printServerResponse(e.getMessage());
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
        }

    }

}
