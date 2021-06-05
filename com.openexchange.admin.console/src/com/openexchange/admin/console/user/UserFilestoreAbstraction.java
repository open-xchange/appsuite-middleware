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

package com.openexchange.admin.console.user;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;


/**
 * {@link UserFilestoreAbstraction} - Extends {@link UserAbstraction} by file storage related options.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.0
 */
public abstract class UserFilestoreAbstraction extends UserAbstraction {

    private static final char OPT_QUOTA_SHORT = 'q';
    private static final String OPT_QUOTA_LONG = "quota";

    private static final char OPT_FILESTORE_SHORT = 'f';
    private static final String OPT_FILESTORE_LONG = "filestore";

    private static final char OPT_FILESTORE_OWNER_SHORT = 'o';
    private static final String OPT_FILESTORE_OWNER_LONG = "owner";

    private static final char OPT_MASTER_SHORT = 'm';
    private static final String OPT_MASTER_LONG = "master";

    // ----------------------------------------------------------------------------------------------------------------------------

    private CLIOption contextQuotaOption;
    private CLIOption filestoreIdOption;
    private CLIOption filestoreOwnerOption;
    private CLIOption masterIdOption;

    /** The file storage identifier */
    protected Integer filestoreId;

    /** The identifier for the file storage owner */
    protected Integer filestoreOwner;

    /** The master identifier */
    protected Integer masterId;

    /**
     * Initializes a new {@link UserFilestoreAbstraction}.
     */
    protected UserFilestoreAbstraction() {
        super();
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the identifier for the file storage.
     * <p>
     * {@link #parseFilestoreId(AdminParser)} is required to be invoked before.
     *
     * @return The file storage identifier or <code>null</code>
     */
    public Integer getFilestoreId() {
        return filestoreId;
    }

    /**
     * Gets the owner for the file storage.
     * <p>
     * {@link #parseFilestoreOwner(AdminParser)} is required to be invoked before.
     *
     * @return The file storage owner or <code>null</code>
     */
    public Integer getFilestoreOwner() {
        return filestoreOwner;
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Parses and sets the value for the quota option <code>'m'</code>/<code>"master"</code>.
     *
     * @param parser The parser to get the value from
     * @return A new {@code User} instance having the master identifier set
     */
    protected User parseAndSetMaster(final AdminParser parser) {
        Object optionValue = parser.getOptionValue(this.masterIdOption);
        if (null == optionValue) {
            return null;
        }

        masterId = Integer.valueOf((String) optionValue);
        User masterUser = new User(masterId.intValue());
        return masterUser;
    }

    /**
     * Parses and sets the value for the quota option <code>'q'</code>/<code>"quota"</code>.
     *
     * @param parser The parser to get the value from
     * @param user The user to apply the value to
     */
    protected void parseAndSetUserQuota(AdminParser parser, User user) {
        String contextQuota = (String) parser.getOptionValue(this.contextQuotaOption);
        if (null != contextQuota) {
            user.setMaxQuota(Long.valueOf(contextQuota));
        }
    }

    /**
     * Parses and gets the value for the quota option <code>'q'</code>/<code>"quota"</code>.
     *
     * @param parser The parser to get the value from
     * @return The quota value or <code>-1</code> if absent
     */
    protected long parseAndGetUserQuota(AdminParser parser) {
        String contextQuota = (String) parser.getOptionValue(this.contextQuotaOption);
        return null == contextQuota ? -1L : Long.parseLong(contextQuota);
    }

    /**
     * Parses and sets the value for the file storage identifier option <code>'f'</code>/<code>"filestore"</code>.
     * <p>
     * The parsed value is accessible via {@link #getFilestoreId()} method.
     *
     * @param parser The parser to get the value from
     */
    protected Filestore parseAndSetFilestoreId(AdminParser parser) {
        return parseAndSetFilestoreId(parser, null);
    }

    /**
     * Parses and sets the value for the file storage identifier option <code>'f'</code>/<code>"filestore"</code>.
     * <p>
     * The parsed value is accessible via {@link #getFilestoreId()} method.
     *
     * @param parser The parser to get the value from
     * @param optUser The optional user to apply the value to
     */
    protected Filestore parseAndSetFilestoreId(AdminParser parser, User optUser) {
        CLIOption option = filestoreIdOption;
        if (null == option) {
            return null;
        }
        String sFilestoreId = (String) parser.getOptionValue(option);
        if (null == sFilestoreId) {
            return null;
        }

        Integer fsId = Integer.valueOf(sFilestoreId.trim());
        if (null != optUser) {
            optUser.setFilestoreId(fsId);
        }
        filestoreId = fsId;
        return new Filestore(fsId);
    }

    /**
     * Parses and sets the value for the file storage owner option <code>'o'</code>/<code>"owner"</code>.
     * <p>
     * The parsed value is accessible via {@link #getFilestoreOwner()} method.
     *
     * @param parser The parser to get the value from
     * @param user The user to apply the value to
     */
    protected void parseAndSetFilestoreOwner(AdminParser parser, User user) {
        CLIOption option = filestoreOwnerOption;
        if (null == option) {
            return;
        }
        String sFilestoreOwner = (String) parser.getOptionValue(option);
        if (null != sFilestoreOwner) {
            Integer owner = Integer.valueOf(sFilestoreOwner.trim());
            user.setFilestoreOwner(owner);
            filestoreOwner = owner;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Sets the master option <code>'m'</code>/<code>"master"</code>.
     *
     * @param parser The parser to add the option to
     * @param required Whether that option is required or not
     */
    protected void setMasterOption(AdminParser parser, boolean required) {
        this.masterIdOption = setShortLongOpt(parser, OPT_MASTER_SHORT, OPT_MASTER_LONG, "Master user id. If not set, the context administrator is assumed to be the master user.", true, required ? NeededQuadState.needed : NeededQuadState.notneeded);
    }

    /**
     * Initializes the quota option <code>'q'</code>/<code>"quota"</code>.
     * <p>
     * Invoke this method in <code>setFurtherOptions(AdminParser)</code> method to add that option to the command-line tool.
     *
     * @param parser The parser to add the option to
     * @param required Whether that option is required or not
     */
    protected void setUserQuotaOption(AdminParser parser, boolean required){
        this.contextQuotaOption = setShortLongOpt(parser, OPT_QUOTA_SHORT, OPT_QUOTA_LONG, "The file storage quota in MB for associated user.", true, required ? NeededQuadState.needed : NeededQuadState.notneeded);
    }

    /**
     * Initializes the file storage option <code>'f'</code>/<code>"filestore"</code>.
     * <p>
     * Invoke this method in <code>setFurtherOptions(AdminParser)</code> method to add that option to the command-line tool.
     *
     * @param parser The parser to add the option to
     * @param required Whether that option is required or not
     */
    protected void setFilestoreIdOption(AdminParser parser, boolean required){
        this.filestoreIdOption = setShortLongOpt(parser, OPT_FILESTORE_SHORT, OPT_FILESTORE_LONG, "The identifier for the file storage.", true, required ? NeededQuadState.needed : NeededQuadState.notneeded);
    }

    /**
     * Initializes the file storage option <code>'o'</code>/<code>"owner"</code>.
     * <p>
     * Invoke this method in <code>setFurtherOptions(AdminParser)</code> method to add that option to the command-line tool.
     *
     * @param parser The parser to add the option to
     * @param required Whether that option is required or not
     */
    protected void setFilestoreOwnerOption(AdminParser parser, boolean required){
        this.filestoreOwnerOption = setShortLongOpt(parser, OPT_FILESTORE_OWNER_SHORT, OPT_FILESTORE_OWNER_LONG, "The owner for the file storage. If no owner is given and f/filestore option is set, the user itself is taken as owner", true, required ? NeededQuadState.needed : NeededQuadState.notneeded);
    }

}
