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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.notification.impl;

import com.openexchange.i18n.LocalizableStrings;


/**
 * Translatable Strings to compose share notification mails.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class NotificationStrings implements LocalizableStrings {

    // subject
    /** $username has shared file "$filename" with you. */
    public static final String SUBJECT_SHARED_FILE = "%1$s has shared the file \"%2$s\" with you.";

    /** $username has shared $number_of_files files with you. */
    public static final String SUBJECT_SHARED_FILES = "%1$s has shared %2$s files with you.";

    /** $username has shared image "$filename" with you. */
    public static final String SUBJECT_SHARED_IMAGE = "%1$s has shared the image \"%2$s\" with you.";

    /** $username has shared $number_of_images images with you. */
    public static final String SUBJECT_SHARED_IMAGES = "%1$s has shared %2$s images with you.";

    /** $username has shared item "$filename" with you. */
    public static final String SUBJECT_SHARED_ITEM = "%1$s has shared the item \"%2$s\" with you.";

    /** $username has shared $number items with you. */
    public static final String SUBJECT_SHARED_ITEMS = "%1$s has shared %2$s items with you.";

    /** $username has shared folder "$folder" with you. */
    public static final String SUBJECT_SHARED_FOLDER = "%1$s has shared the folder \"%2$s\" with you.";

    /** $username has shared $number folders with you. */
    public static final String SUBJECT_SHARED_FOLDERS = "%1$s has shared %2$d folders with you.";

    // detailed body
    /** $username ($user_email) has shared file "$filename" with you. */
    public static final String HAS_SHARED_FILE = "%1$s (%2$s) has shared the file \"%3$s\" with you.";

    /** $username ($user_email) has shared $number_of_files files with you. */
    public static final String HAS_SHARED_FILES = "%1$s (%2$s) has shared %3$s files with you.";

    /** $username ($user_email) has shared image "$filename" with you. */
    public static final String HAS_SHARED_IMAGE = "%1$s (%2$s) has shared the image \"%3$s\" with you.";

    /** $username ($user_email) has shared $number_of_images iamges with you. */
    public static final String HAS_SHARED_IMAGES = "%1$s (%2$s) has shared %3$s images with you.";

    /** $username ($user_email) has shared item "$filename" with you. */
    public static final String HAS_SHARED_ITEM = "%1$s (%2$s) has shared the item \"%3$s\" with you.";

    /** $username ($user_email) has shared $number items with you. */
    public static final String HAS_SHARED_ITEMS = "%1$s (%2$s) has shared %3$s items with you.";

    /** $username ($user_email) has shared folder $folder with you. */
    public static final String HAS_SHARED_FOLDER = "%1$s (%2$s) has shared the folder \"%3$s\" with you.";

    /** $username ($user_email) has shared folder $number folders with you. */
    public static final String HAS_SHARED_FOLDERS = "%1$s (%2$s) has shared %3$ folders with you.";


    /** $username ($user_email) has shared file "$filename" with you and left you a message: */
    public static final String HAS_SHARED_FILE_AND_MESSAGE = "%1$s (%2$s) has shared the file \"%3$s\" with you and left you a message:";

    /** $username ($user_email) has shared $number_of_files files with you and left you a message: */
    public static final String HAS_SHARED_FILES_AND_MESSAGE = "%1$s (%2$s) has shared %3$s files with you and left you a message:";

    /** $username ($user_email) has shared photo "$filename" with you and left you a message: */
    public static final String HAS_SHARED_PHOTO_AND_MESSAGE = "%1$s (%2$s) has shared the image \"%3$s\" with you and left you a message:";

    /** $username ($user_email) has shared $number_of_images images with you and left you a message: */
    public static final String HAS_SHARED_IMAGES_AND_MESSAGE = "%1$s (%2$s) has shared %3$s images with you and left you a message:";

    /** $username ($user_email) has shared a folder with you and left you a message: */
    public static final String HAS_SHARED_FOLDER_AND_MESSAGE = "%1$s (%2$s) has shared the folder \"%3$s\" with you and left you a message:";

    /** $username ($user_email) has shared item "$filename" with you. */
    public static final String HAS_SHARED_ITEM_AND_MESSAGE = "%1$s (%2$s) has shared the item \"%3$s\" with you and left you a message:";

    /** $username ($user_email) has shared $number_of_items items with you. */
    public static final String HAS_SHARED_ITEMS_AND_MESSAGE = "%1$s (%2$s) has shared %3$d items with you and left you a message:";

    /** $username ($user_email) has shared $number_of_folder items with you. */
    public static final String HAS_SHARED_FOLDERS_AND_MESSAGE = "%1$s (%2$s) has shared %3$d folders with you and left you a message:";

    //please click
    /** Please click the button below to view it. */
    public static final String PLEASE_CLICK_IT = "Please click the button below to view it.";

    /** Please click the button below to view them. */
    public static final String PLEASE_CLICK_THEM = "Please click the button below to view them.";

    // button with label
    /** View photo */
    public static final String VIEW_IMAGE = "View image";

    /** View photos */
    public static final String VIEW_IMAGES = "View images";

    /** View file */
    public static final String VIEW_FILE = "View file";

    /** View files */
    public static final String VIEW_FILES = "View files";

    /** View folder */
    public static final String VIEW_FOLDER = "View folder";

    /** View folders */
    public static final String VIEW_FOLDERS = "View folders";

    /** View item */
    public static final String VIEW_ITEM = "View item";

    /** View items */
    public static final String VIEW_ITEMS = "View items";


    // expires
    /** The link will expire on $preformatted_date */
    public static final String LINK_EXPIRE = "The link will expire on %1$s";

    /*
     * Password reset confirm mails
     */

    public static final String PWRC_SUBJECT = "Password Reset";

    public static final String PWRC_GREETING = "Hello,";

    /** We received a request to reset the password to your account: */
    public static final String PWRC_REQUESTRECEIVED = "We received a request to reset the password to your account:";

    /** You can set a new password here:*/
    public static final String PWRC_SET_NEW_PWD = "You can set a new password here:";

    /** Reset password */
    public static final String PWRC_LINK_LABEL = "Reset password";

    /** If you didn't request this, please ignore this email. Your password won't change until you access the button above and create a new one. */
    public static final String PWRC_IGNORE = "If you didn't request this, please ignore this email. Your password won't change until you access the button above and create a new one.";

    /** This is an automated email, please do not reply. */
    public static final String PWRC_AUTOMATED_MAIL = "This is an automated email, please do not reply.";

    /** Thanks,*/
    public static final String PWRC_THANKS = "Thanks,";

    /** The $product Team*/
    public static final String PWRC_THE_TEAM = "The %1$s Team";

    // An error occurred for user 'unknown'
    public static final String UNKNOWN_USER_NAME = "unknown";

}

