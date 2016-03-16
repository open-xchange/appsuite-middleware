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

package com.openexchange.admin.reseller.console.extensionimpl;

import java.util.HashMap;
import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleCreateInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.console.user.UserAbstraction.CSVConstants;
import com.openexchange.admin.reseller.console.ResellerAbstraction;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public class ContextConsoleCreateImpl implements ContextConsoleCreateInterface {

    public enum ResellerConstants implements CSVConstants {
        ADD_RESTRICTION(ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, false),
        CUSTOMID(ResellerAbstraction.OPT_CUSTOMID_LONG, false);

        private final String string;

        private int index;

        private boolean required;

        private ResellerConstants(final String string, final boolean required) {
            this.string = string;
            this.required = required;
        }

        @Override
        public String getString() {
            return string;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public void setRequired(boolean required) {
            this.required = required;
        }

    }

    protected CLIOption addRestrictionsOption = null;
    protected CLIOption customidOption = null;

    @Override
    public void addExtensionOptions(final AdminParser parser) throws OXConsolePluginException {
        addRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_ADD_RESTRICTION_SHORT, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, "Restriction to add (can be specified multiple times)", NeededQuadState.notneeded, true);
        customidOption = parser.addOption(ResellerAbstraction.OPT_CUSTOMID_SHORT, ResellerAbstraction.OPT_CUSTOMID_LONG, ResellerAbstraction.OPT_CUSTOMID_LONG, "Custom Context ID", NeededQuadState.notneeded, true);
    }

    @Override
    public void applyExtensionValuesFromCSV(final String[] nextLine, final int[] idarray, final Context context) throws OXConsolePluginException {
        final OXContextExtensionImpl firstExtensionByName = (OXContextExtensionImpl) context.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
        try {
            if (null == firstExtensionByName) {
                final OXContextExtensionImpl ctxext = new OXContextExtensionImpl();
                {
                    final int i = idarray[ResellerConstants.ADD_RESTRICTION.getIndex()];
                    if (-1 != i) {
                        if (nextLine[i].length() > 0) {
                            final HashSet<Restriction> restrictions = getRestrictions(getRestrictionFromCSV(nextLine, i));
                            if (null != restrictions) {
                                ctxext.setRestriction(restrictions.toArray(new Restriction[restrictions.size()]));
                            }
                        }
                    }
                }
                {
                    final int i = idarray[ResellerConstants.CUSTOMID.getIndex()];
                    if (-1 != i) {
                        if (nextLine[i].length() > 0) {
                            ctxext.setCustomid(nextLine[i]);
                        }
                    }
                }
                // TODO: Maybe it's worth checking if the extension has data here, this may depend on the how much overhead a set extension imposes to the server
                context.addExtension(ctxext);
            } else {
                {
                    final int i = idarray[ResellerConstants.ADD_RESTRICTION.getIndex()];
                    if (-1 != i) {
                        if (nextLine[i].length() > 0) {
                            final HashSet<Restriction> restrictions = getRestrictions(getRestrictionFromCSV(nextLine, i));
                            if (null != restrictions) {
                                firstExtensionByName.setRestriction(restrictions.toArray(new Restriction[restrictions.size()]));
                            }
                        }
                    }
                }
                {
                    final int i = idarray[ResellerConstants.CUSTOMID.getIndex()];
                    if (-1 != i) {
                        if (nextLine[i].length() > 0) {
                            firstExtensionByName.setCustomid(nextLine[i]);
                        }
                    }
                }

            }
        } catch (final DuplicateExtensionException e) {
            // Throw this one, but this should never occur as we check beforehand
            throw new OXConsolePluginException(e);
        } catch (InvalidDataException e) {
            throw new OXConsolePluginException(e);
        } catch (OXResellerException e) {
            throw new OXConsolePluginException("A reseller exception occured: " + e.getMessage());
        }
    }

    private HashSet<Restriction> getRestrictionFromCSV(final String[] nextLine, final int i) throws InvalidDataException {
        final HashSet<Restriction> res = new HashSet<Restriction>();
        // The restrictions are given in a comma-separated array
        final String[] restrictionStringArray = nextLine[i].split(",");
        for (final String restriction : restrictionStringArray) {
            res.add(ResellerAbstraction.getRestrictionFromString(restriction));
        }
        return res;
    }

    @Override
    public void setAndFillExtension(final AdminParser parser, final Context ctx, final Credentials auth) throws OXConsolePluginException {
        final OXContextExtensionImpl firstExtensionByName = (OXContextExtensionImpl) ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
        try {
            HashSet<Restriction> ret = getRestrictions(ResellerAbstraction.parseRestrictions(parser, this.addRestrictionsOption));
            Restriction[] restrictions = null;
            if( ret != null ) {
                restrictions = ret.toArray(new Restriction[ret.size()]);
            }
            final String customid = ResellerAbstraction.parseCustomId(parser, customidOption);
            if (null == firstExtensionByName) {
                final OXContextExtensionImpl ctxext;
                if (null != restrictions) {
                    ctxext = new OXContextExtensionImpl(restrictions);
                }  else {
                    ctxext = new OXContextExtensionImpl();
                }
                if (null != customid) {
                    ctxext.setCustomid(customid);
                }
                // TODO: Maybe it's worth checking if the extension has data here, this may depend on the how much overhead a set extension imposes to the server
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

    /**
     *
     *
     * @param addres
     * @return might return null
     * @throws InvalidDataException
     * @throws OXResellerException
     */
    private HashSet<Restriction> getRestrictions(final HashSet<Restriction> addres) throws InvalidDataException, OXResellerException {
        HashSet<Restriction> restrictions = null;
        if (!addres.isEmpty()) {
            final HashSet<Restriction> dbres = new HashSet<Restriction>();
            restrictions = ResellerAbstraction.handleAddEditRemoveRestrictions(dbres, addres, null, null);
        }
        return restrictions;
    }

    @Override
    public void processCSVConstants(HashMap<String, CSVConstants> constantsMap) {
        // How much contants value to we have already...
        int currentMaxIndex = constantsMap.size();
        for (final ResellerConstants value : ResellerConstants.values()) {
            value.setIndex(currentMaxIndex++);
            constantsMap.put(value.getString(), value);
        }
    }

}
