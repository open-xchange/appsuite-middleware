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
package com.openexchange.admin.console.resource;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Resource;

public abstract class ResourceAbstraction extends ObjectNamingAbstraction {

    protected static final String _OPT_NAME_LONG = "name";
    protected static final char _OPT_NAME_SHORT = 'n';
    protected static final String _OPT_DISPNAME_LONG = "displayname";
    protected static final char _OPT_DISPNAME_SHORT = 'd';
    protected static final String _OPT_DESCRIPTION_LONG = "description";
    protected static final char _OPT_DESCRIPTION_SHORT = 'D';
    protected static final char _OPT_AVAILABLE_SHORT = 'a';
    protected static final String _OPT_AVAILABLE_LONG = "available";
    protected static final char _OPT_EMAIL_SHORT = 'e';
    protected static final String _OPT_EMAIL_LONG = "email";
    protected static final char _OPT_RESOURCEID_SHORT = 'i';
    protected static final String _OPT_RESOURCEID_LONG = "resourceid";
    protected static final String OPT_RECIPIENT_LONG = "mailrecipients";
    protected static final char OPT_RECIPIENT_SHORT = 'm';

    protected CLIOption resourceDisplayNameOption = null;
    protected CLIOption resourceNameOption = null;
    protected CLIOption resourceAvailableOption = null;
    protected CLIOption resourceDescriptionOption = null;
    protected CLIOption resourceEmailOption = null;
    protected CLIOption resourceIdOption = null;

    protected CLIOption resourceRecipientOption = null;

    // For right error output
    protected String resourceid = null;
    protected String resourcename = null;

    protected void setDisplayNameOption(final AdminParser admp,final boolean required){
        resourceDisplayNameOption = setShortLongOpt(admp, _OPT_DISPNAME_SHORT,_OPT_DISPNAME_LONG,"The resource display name",true, convertBooleantoTriState(required));
    }

    protected void setRecipientOption(final AdminParser admp,final boolean required){
        resourceRecipientOption = setShortLongOpt(admp, OPT_RECIPIENT_SHORT,OPT_RECIPIENT_LONG,"Recipient who should receive mail addressed to the resource",true, convertBooleantoTriState(required));
    }

    protected void setNameOption(final AdminParser admp,final NeededQuadState required){
        resourceNameOption =  setShortLongOpt(admp, _OPT_NAME_SHORT,_OPT_NAME_LONG,"The resource name",true, required);
    }

    protected void setAvailableOption(final AdminParser admp,final boolean required){
        resourceAvailableOption = setShortLongOpt(admp, _OPT_AVAILABLE_SHORT, _OPT_AVAILABLE_LONG, "true/false", "Toggle resource availability", required);
    }

    protected void setDescriptionOption(final AdminParser admp,final boolean required){
        resourceDescriptionOption =   setShortLongOpt(admp,_OPT_DESCRIPTION_SHORT,_OPT_DESCRIPTION_LONG,"Description of this resource", true, convertBooleantoTriState(required));
    }

    protected void setEmailOption(final AdminParser admp,final boolean required){
        resourceEmailOption =  setShortLongOpt(admp,_OPT_EMAIL_SHORT,_OPT_EMAIL_LONG,"Email of this resource", true, convertBooleantoTriState(required));
    }

    protected void setIdOption(final AdminParser admp){
        resourceIdOption = setShortLongOpt(admp,_OPT_RESOURCEID_SHORT,_OPT_RESOURCEID_LONG,"Id of this resource", true, NeededQuadState.eitheror);
    }

    protected final OXResourceInterface getResourceInterface() throws NotBoundException, MalformedURLException, RemoteException {
        return (OXResourceInterface) Naming.lookup(RMI_HOSTNAME + OXResourceInterface.RMI_NAME);
    }

    @Override
    protected String getObjectName() {
        return "resource";
    }

    private void parseAndSetResourceAvailable(final AdminParser parser, final Resource res) {
        final String resourceavailable = (String) parser.getOptionValue(this.resourceAvailableOption);
        if (resourceavailable != null) {
            res.setAvailable(Boolean.parseBoolean(resourceavailable));
        }
    }

    private void parseAndSetResourceDescription(final AdminParser parser, final Resource res) {
        String resourceDescription = (String) parser.getOptionValue(this.resourceDescriptionOption);
        if (resourceDescription != null) {
            if ("".equals(resourceDescription)) { resourceDescription = null; }
            res.setDescription(resourceDescription);
        }
    }

    private void parseAndSetResourceDisplayName(final AdminParser parser, final Resource res) {
        final String resourceDisplayName = (String) parser.getOptionValue(this.resourceDisplayNameOption);
        if (resourceDisplayName != null) {
            res.setDisplayname(resourceDisplayName);
        }
    }

    private void parseAndSetResourceEmail(final AdminParser parser, final Resource res) {
        String resourceEmail = (String) parser.getOptionValue(this.resourceEmailOption);
        if (resourceEmail != null) {
            if ("".equals(resourceEmail)) { resourceEmail = null; }
            res.setEmail(resourceEmail);
        }
    }

    protected void parseAndSetResourceName(final AdminParser parser, final Resource res) {
        this.resourcename = (String) parser.getOptionValue(this.resourceNameOption);
        if (this.resourcename != null) {
            res.setName(this.resourcename);
        }
    }

    protected void parseAndSetMandatoryFields(final AdminParser parser, final Resource res) {
        parseAndSetResourceAvailable(parser, res);

        parseAndSetResourceDescription(parser, res);

        parseAndSetResourceDisplayName(parser, res);

        parseAndSetResourceEmail(parser, res);

        parseAndSetResourceName(parser, res);
    }

    protected void parseAndSetResourceId(final AdminParser parser, final Resource res) {
        resourceid = (String) parser.getOptionValue(this.resourceIdOption);
        if (null != resourceid) {
            res.setId(Integer.parseInt(resourceid));
        }
    }
}
