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

package com.openexchange.admin.reseller.rmi.extensions;

import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;


public class OXContextExtensionImpl extends OXCommonExtension {

    /**
     * For serialization
     */
    private static final long serialVersionUID = 8443761921961452860L;

    private String errortext;

    private ResellerAdmin owner;

    private String customid;

    private Restriction[] restriction;

    private boolean restrictionset;

    private boolean ownerset;

    private int sid;

    private boolean sidset;

    private boolean customidset;

    /**
     * Initializes a new {@link OXContextExtensionImpl}.
     * @param sid
     */
    public OXContextExtensionImpl() {
        super();
    }

    /**
     * Initializes a new {@link OXContextExtensionImpl}.
     * @param sid
     */
    public OXContextExtensionImpl(final int sid) {
        super();
        setSid(sid);
    }

    /**
     * Initializes a new {@link OXContextExtensionImpl}.
     * @param owner
     */
    public OXContextExtensionImpl(final ResellerAdmin owner) {
        super();
        setOwner(owner);
    }

    /**
     * Initializes a new {@link OXContextExtensionImpl}.
     * @param restriction
     */
    public OXContextExtensionImpl(final Restriction[] restriction) {
        super();
        setRestriction(restriction);
    }

    /**
     * Initializes a new {@link OXContextExtensionImpl}.
     * @param owner
     * @param restriction
     */
    public OXContextExtensionImpl(final ResellerAdmin owner, final Restriction[] restriction) {
        super();
        setOwner(owner);
        setRestriction(restriction);
    }

    /**
     * Initializes a new {@link OXContextExtensionImpl}.
     * @param restriction
     */
    public OXContextExtensionImpl(final String customid) {
        super();
        setCustomid(customid);
    }
    /**
     * Returns the owner of this context
     *
     * @return
     */
    public final ResellerAdmin getOwner() {
        return owner;
    }

    public final int getSid() {
        return sid;
    }

    @Override
    public void setExtensionError(final String errortext) {
        this.errortext = errortext;
    }

    @Override
    public String getExtensionError() {
        return this.errortext;
    }

    /**
     * Sets the owner of this context
     *
     * @param owner
     */
    public final void setOwner(final ResellerAdmin owner) {
        this.ownerset = true;
        this.owner = owner;
    }


    public final void setSid(int sid) {
        this.sidset = true;
        this.sid = sid;
    }


    public final boolean isOwnerset() {
        return ownerset;
    }


    public final boolean isSidset() {
        return sidset;
    }


    public final Restriction[] getRestriction() {
        return restriction;
    }


    public final void setRestriction(Restriction[] restriction) {
        this.restrictionset = true;
        this.restriction = restriction;
    }


    public final boolean isRestrictionset() {
        return restrictionset;
    }

    public final boolean isCustomidset() {
        return customidset;
    }

    /**
     * @return the customid
     */
    public final String getCustomid() {
        return customid;
    }


    /**
     * @param customid the customid to set
     */
    public final void setCustomid(final String customid) {
        this.customidset = true;
        this.customid = customid;
    }

}
