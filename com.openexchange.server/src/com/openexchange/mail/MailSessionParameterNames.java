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

package com.openexchange.mail;

/**
 * {@link MailSessionParameterNames} - Constants used as keys for session parameters.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailSessionParameterNames {

    /**
     * No instantiation.
     */
    private MailSessionParameterNames() {
        super();
    }

    /**
     * Publishing infostore folder ID.
     */
    private static final String PARAM_PUBLISHING_INFOSTORE_FOLDER_ID = "mail.pubid";

    /**
     * Gets the parameter name for publishing infostore folder ID.
     *
     * @return The parameter name for publishing infostore folder ID
     */
    @Deprecated
    public static String getParamPublishingInfostoreFolderID() {
        return PARAM_PUBLISHING_INFOSTORE_FOLDER_ID;
    }

    /**
     * Sharing Drive folder ID.
     */
    private static final String PARAM_SHARING_DRIVE_FOLDER_ID = "mail.shareid";

    /**
     * Gets the parameter name for sharing Drive folder ID.
     *
     * @return The parameter name for sharing Drive folder ID
     */
    public static String getParamSharingDriveFolderID() {
        return PARAM_SHARING_DRIVE_FOLDER_ID;
    }

    /*-
     * ####################### DEFAULT FOLDERS ########################
     */

    /**
     * Default folder flag.
     */
    private static final String PARAM_DEF_FLD_FLAG = "mail.deffldflag";

    /**
     * Gets the parameter name for default folder flag.
     *
     * @return The parameter name for default folder flag
     */
    public static String getParamDefaultFolderChecked() {
        return PARAM_DEF_FLD_FLAG;
    }

    /**
     * Default folder flag.
     */
    private static final String PARAM_DEF_FLD_TRASH = "mail.deftrash";

    /**
     * Gets the parameter name for default trash folder.
     *
     * @return The parameter name for default trash folder
     */
    public static String getParamDefaultTrashFolder() {
        return PARAM_DEF_FLD_TRASH;
    }

    /*-
     * ####################### OTHER STUFF ########################
     */

    /**
     * Mail folder separator.
     */
    private static final String PARAM_SEPARATOR = "mail.separator";

    /**
     * Gets the parameter name for mail folder separator.
     *
     * @return The parameter name for mail folder separator
     */
    public static String getParamSeparator() {
        return PARAM_SEPARATOR;
    }

    /**
     * Default folder array.
     */
    private static final String PARAM_DEF_FLD_ARR = "mail.deffldarr";

    /**
     * Gets the parameter name for default folder array.
     *
     * @return The parameter name for default folder array
     */
    public static String getParamDefaultFolderArray() {
        return PARAM_DEF_FLD_ARR;
    }

    /**
     * Session mail cache.
     */
    private static final String PARAM_MAIL_CACHE = "mail.mailcache";

    /**
     * Gets the parameter name for session mail cache.
     *
     * @return The parameter name for session mail cache
     */
    public static String getParamMailCache() {
        return PARAM_MAIL_CACHE;
    }

    /**
     * Session mail cache.
     */
    private static final String PARAM_MAIN_CACHE = "mail.maincache";

    /**
     * Gets the parameter name for session main cache.
     *
     * @return The parameter name for session main cache
     */
    public static String getParamMainCache() {
        return PARAM_MAIN_CACHE;
    }

    /**
     * Mail provider.
     */
    private static final String PARAM_MAIL_PROVIDER = "mail.provider";

    /**
     * Gets the parameter name for mail provider.
     *
     * @return The parameter name for mail provider
     */
    public static String getParamMailProvider() {
        return PARAM_MAIL_PROVIDER;
    }

    /**
     * Mail transport.
     */
    private static final String PARAM_TRANSPORT_PROVIDER = "mail.tansport";

    /**
     * Gets the parameter name for mail transport.
     *
     * @param accountId The account ID
     * @return The parameter name for mail transport
     */
    public static String getParamTransportProvider() {
        return PARAM_TRANSPORT_PROVIDER;
    }

    /**
     * Organization header field when composing new mails.
     */
    public static final String PARAM_ORGANIZATION_HDR = "mail.orga";

    /**
     * The reference to session's context.
     */
    private static final String PARAM_CONTEXT = "mail.context";

    /**
     * Gets the parameter name for session's context.
     *
     * @return The parameter name for session's context
     */
    public static String getParamSessionContext() {
        return PARAM_CONTEXT;
    }

    /**
     * Spam handler.
     */
    private static final String PARAM_SPAM_HANDLER = "mail.shandler";

    /**
     * Gets the parameter name for spam handler.
     *
     * @return The parameter name for spam handler
     */
    public static String getParamSpamHandler() {
        return PARAM_SPAM_HANDLER;
    }

}
