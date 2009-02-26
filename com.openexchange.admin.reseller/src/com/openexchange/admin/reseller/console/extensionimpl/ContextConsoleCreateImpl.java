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
package com.openexchange.admin.reseller.console.extensionimpl;

import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleCreateInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.reseller.console.ResellerAbstraction;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtension;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;


public class ContextConsoleCreateImpl implements ContextConsoleCreateInterface {
    
    protected Option addRestrictionsOption = null;
    protected Option customidOption = null;

    public void addExtensionOptions(final AdminParser parser) throws OXConsolePluginException {
        parser.removeOption("c", "contextid");
        addRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_ADD_RESTRICTION_SHORT, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, "Restriction to add (can be specified multiple times)", NeededQuadState.notneeded, true);
        customidOption = parser.addOption(ResellerAbstraction.OPT_CUSTOMID_SHORT, ResellerAbstraction.OPT_CUSTOMID_LONG, ResellerAbstraction.OPT_CUSTOMID_LONG, "Custom Context ID", NeededQuadState.notneeded, true); 
    }

    public void setAndFillExtension(final AdminParser parser, final Context ctx, final Credentials auth) throws OXConsolePluginException {
        final OXContextExtension firstExtensionByName = (OXContextExtension) ctx.getFirstExtensionByName(OXContextExtension.class.getName());
        try {
            HashSet<Restriction> restrictions = getRestrictions(parser);
            final String customid = ResellerAbstraction.parseCustomId(parser, customidOption);
            if (null == firstExtensionByName) {
                final OXContextExtension ctxext;
                if (null != restrictions) {
                    ctxext = new OXContextExtension(restrictions);
                }  else {
                    ctxext = new OXContextExtension();
                }
                if (null != customid) {
                    ctxext.setCustomid(customid);
                }
                // TODO: Maybe it's worth checking if the extension has data here, this may depent on the how much overhead a set extension imposes to the server
                ctx.addExtension(ctxext);
            } else {
                if (null != restrictions) {
                    firstExtensionByName.setRestriction(restrictions);
                }
                if (null != customid) {
                    firstExtensionByName.setCustomid(customid);
                }
            }
        } catch (final InvalidDataException e) {
            throw new OXConsolePluginException(e);
        } catch (final OXResellerException e) {
            throw new OXConsolePluginException("A reseller exception occured: " + e.getMessage());
        } catch (final DuplicateExtensionException e) {
            // Throw this one, but this should never occur as we check beforehand
            throw new OXConsolePluginException(e);
        }

    }

    private HashSet<Restriction> getRestrictions(final AdminParser parser) throws InvalidDataException, OXResellerException {
        final HashSet<Restriction> addres = ResellerAbstraction.parseRestrictions(parser, this.addRestrictionsOption);
        
        HashSet<Restriction> restrictions = null;
        if (!addres.isEmpty()) {
            final HashSet<Restriction> dbres = new HashSet<Restriction>();
            restrictions = ResellerAbstraction.handleAddEditRemoveRestrictions(dbres, addres, null, null);
        }
        return restrictions;
    }

}
