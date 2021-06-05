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
