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

package com.openexchange.admin.console.resource;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
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

    protected void setDisplayNameOption(final AdminParser admp, final boolean required) {
        resourceDisplayNameOption = setShortLongOpt(admp, _OPT_DISPNAME_SHORT, _OPT_DISPNAME_LONG, "The resource display name", true, convertBooleantoTriState(required));
    }

    protected void setRecipientOption(final AdminParser admp, final boolean required) {
        resourceRecipientOption = setShortLongOpt(admp, OPT_RECIPIENT_SHORT, OPT_RECIPIENT_LONG, "Recipient who should receive mail addressed to the resource", true, convertBooleantoTriState(required));
    }

    protected void setNameOption(final AdminParser admp, final NeededQuadState required) {
        resourceNameOption = setShortLongOpt(admp, _OPT_NAME_SHORT, _OPT_NAME_LONG, "The resource name", true, required);
    }

    protected void setAvailableOption(final AdminParser admp, final boolean required) {
        resourceAvailableOption = setShortLongOpt(admp, _OPT_AVAILABLE_SHORT, _OPT_AVAILABLE_LONG, "true/false", "Toggle resource availability", required);
    }

    protected void setDescriptionOption(final AdminParser admp, final boolean required) {
        resourceDescriptionOption = setShortLongOpt(admp, _OPT_DESCRIPTION_SHORT, _OPT_DESCRIPTION_LONG, "Description of this resource", true, convertBooleantoTriState(required));
    }

    protected void setEmailOption(final AdminParser admp, final boolean required) {
        resourceEmailOption = setShortLongOpt(admp, _OPT_EMAIL_SHORT, _OPT_EMAIL_LONG, "Email of this resource", true, convertBooleantoTriState(required));
    }

    protected void setIdOption(final AdminParser admp) {
        resourceIdOption = setShortLongOpt(admp, _OPT_RESOURCEID_SHORT, _OPT_RESOURCEID_LONG, "Id of this resource", true, NeededQuadState.eitheror);
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
            res.setAvailable(B(Boolean.parseBoolean(resourceavailable)));
        }
    }

    private void parseAndSetResourceDescription(final AdminParser parser, final Resource res) {
        String resourceDescription = (String) parser.getOptionValue(this.resourceDescriptionOption);
        if (resourceDescription != null) {
            if ("".equals(resourceDescription)) {
                resourceDescription = null;
            }
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
            if ("".equals(resourceEmail)) {
                resourceEmail = null;
            }
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
        resourceid = String.class.cast(parser.getOptionValue(this.resourceIdOption));
        if (null != resourceid) {
            res.setId(I(Integer.parseInt(resourceid)));
        }
    }
}
