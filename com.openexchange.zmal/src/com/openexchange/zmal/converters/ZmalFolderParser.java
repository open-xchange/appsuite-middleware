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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.zmal.converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.zmal.ACLPermission;
import com.openexchange.zmal.Services;
import com.openexchange.zmal.ZmalSoapPerformer;
import com.openexchange.zmal.ZmalSoapResponse;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.Rights;
import com.zimbra.common.soap.Element;

/**
 * {@link ZmalFolderParser}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalFolderParser {

    private final ZmalSoapPerformer performer;

    /**
     * Initializes a new {@link ZmalFolderParser}.
     */
    public ZmalFolderParser(final ZmalSoapPerformer performer) {
        super();
        this.performer = performer;
    }

    /**
     * Parses folders from specified SOAP response.
     * 
     * @param response The SOAP response
     * @return The parsed messages
     * @throws OXException If parsing fails
     */
    public List<MailFolder> parseFolders(final ZmalSoapResponse response) throws OXException {
        final List<Element> results = response.getResults();
        final List<MailFolder> mails = new ArrayList<MailFolder>(results.size());
        for (final Element element : results) {
            mails.addAll(parseElement(element));
        }
        return mails;
    }

    /**
     * Parses specified element to its mail representation(s).
     * 
     * @param element The element
     * @return The mail representation(s)
     * @throws OXException If parsing element fails
     */
    public List<MailFolder> parseElement(final Element element) throws OXException {
        if (null == element) {
            return null;
        }
        // Check if current element denotes a folder
        if (isFolder(element)) {
            return Collections.singletonList(parseSingleFolder(element));
        }
        final List<Element> elements = element.listElements();
        final List<MailFolder> mails = new ArrayList<MailFolder>(elements.size());
        for (final Element sub : elements) {
            final MailFolder folder = parseSingleFolder(sub);
            if (null != folder) {
                mails.add(folder);
            }
        }
        return mails;
    }

    /**
     * Parses folders from specified SOAP response.
     * 
     * @param response The SOAP response
     * @return The parsed messages
     * @throws OXException If parsing fails
     */
    public List<MailFolder> parseSubFolders(final ZmalSoapResponse response) throws OXException {
        final List<Element> results = response.getResults();
        final List<MailFolder> mails = new ArrayList<MailFolder>(results.size());
        for (final Element element : results) {
            mails.addAll(parseSubElement(element));
        }
        return mails;
    }

    /**
     * Parses specified element to its mail representation(s).
     * 
     * @param element The element
     * @return The mail representation(s)
     * @throws OXException If parsing element fails
     */
    public List<MailFolder> parseSubElement(final Element element) throws OXException {
        if (null == element) {
            return null;
        }
        // Check if current element denotes a message
        
        final List<Element> subfolderElements = element.listElements("folder");
        if (null == subfolderElements) {
            return Collections.emptyList();
        }
        final List<MailFolder> subfolders = new ArrayList<MailFolder>(subfolderElements.size());
        for (Element subfolderElement : subfolderElements) {
            subfolders.add(parseSingleFolder(subfolderElement));
        }
        return subfolders;
    }

    /*-
     * Folders:

      <folder id="{folder-id}" name="{folder-name}" l="{parent-id}" [f="{flags}"] [color="{color}"]
             u="{unread}" n="{msg-count}" s="{total-size}" [view="{default-type}"]
             [url="{remote-url}"] [perm="{effective-perms}"] [rest="{rest-url}"]>
        [<acl> <grant perm="{rights}" gt="{grantee-type}" zid="{zimbra-id}" d="{grantee-name}" [pw="{password-for-guest}"] [key=="{access-key}"]/>* </acl>]
      </folder>
    
      {folder-name}  = name of folder; max length 128; whitespace is trimmed by server;
                       cannot contain ':', '"', '/', or any character below 0x20
      {parent-id}    = id of parent folder (absent for root folder)
      {flags}        = checked in UI (#), exclude free/(b)usy info, IMAP subscribed (*), does not (i)nherit rights from parent, is a s(y)nc folder with external data source, sync is turned on(~), folder does n(o)t allow inferiors / children
      {color}        = numeric; range 0-127; defaults to 0 if not present; client can display only 0-7
      {unread}       = number of unread messages in folder
      {msg-count}    = number of non-subfolder items in folder
      {total-size}   = total size of all of non-subfolder items in folder
      {default-type} = (optional) default type for the folder; used by web client to decide which view to use;
                       possible values are the same as <SearchRequest>'s {types}: conversation|message|contact|etc
      {remote-url}   = url (RSS, iCal, etc.) this folder syncs its contents to
      {rest-url}     = url to the folder on rest interface for rest-enabled apps (such as wiki and notebook)
      {effective-perms} = for remote folders, the access rights the authenticated user has on the folder
                            - will contain the calculated (c)reate folder permission if the user has
                              both (i)nsert and (r)ead access on the folder
    
      folders can have an optional ACL set on them for sharing.  if they do (and the authenticated
        user has (a)dminister rights on the folder), an <acl> element will be returned containing
        1 or more <grant> elements, with the following attributes:
    
        {rights}       = some combination of (r)ead, (w)rite, (i)nsert, (d)elete, (a)dminister, workflow action (x), view (p)rivate, view (f)reebusy
        {grantee-type} = the type of grantee: "usr", "grp", "dom" (domain), "cos", 
                         "all" (all authenticated users), "pub" (public authenticated and unauthenticated access),
                         "guest" (non-Zimbra email address and password),
                         "key" (non-Zimbra email address and access key)
        {grantee-name} = the display name (*not* the zimbra id) of the principal being granted rights;
                         optional if {grantee-type} is "all"
        {pw}           = optional argument.  password when {grantee-type} is "guest"
        {key}          = optional argument.  access key when {grantee-type} is "key"
     */
    private MailFolder parseSingleFolder(final Element element) throws OXException {
        if (!isFolder(element)) {
            return null;
        }
        try {
            final MailFolder mailFolder = new MailFolder();
            mailFolder.setSupportsUserFlags(false);
            {
                final String s = element.getAttribute("id", "");
                if (!isEmpty(s)) {
                    mailFolder.setFullname(s);
                }
            }
            {
                final String s = element.getAttribute("name", "");
                if (!isEmpty(s)) {
                    mailFolder.setName(s);
                }
            }
            {
                final String s = element.getAttribute("l", "");
                if (!isEmpty(s)) {
                    mailFolder.setParentFullname(s);
                }
            }
            {
                final String sFlags = element.getAttribute("f", "");
                if (!isEmpty(sFlags)) {
                    if (sFlags.indexOf('*') >= 0) {
                        mailFolder.setSubscribed(true);
                    }
                    if (sFlags.indexOf('0') >= 0) {
                        mailFolder.setHoldsFolders(false);
                        mailFolder.setSubscribedSubfolders(false);
                        mailFolder.setSubfolders(false);
                    }
                }
            }
            {
                final String s = element.getAttribute("u", "");
                if (!isEmpty(s)) {
                    try {
                        mailFolder.setUnreadMessageCount(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        // Ignore
                        mailFolder.setUnreadMessageCount(-1);
                    }
                }
            }
            {
                final String s = element.getAttribute("n", "");
                if (!isEmpty(s)) {
                    try {
                        mailFolder.setMessageCount(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        // Ignore
                        mailFolder.setMessageCount(-1);
                    }
                }
            }
            {
                final String s = element.getAttribute("perm", "");
                final DefaultMailPermission ownPermission = new DefaultMailPermission();
                ownPermission.setAllPermission(
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS);
                ownPermission.setFolderAdmin(false);
                if (!isEmpty(s)) {
                    if (s.indexOf('r') >= 0) {
                        ownPermission.setFolderAdmin(false);
                        ownPermission.setAllObjectPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                        ownPermission.setFolderPermission(OCLPermission.READ_FOLDER);
                    }
                    if (s.indexOf('i') >= 0) {
                        ownPermission.setFolderAdmin(false);
                        ownPermission.setAllObjectPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
                        ownPermission.setFolderPermission(OCLPermission.READ_FOLDER);
                    }
                    if (s.indexOf('c') >= 0) {
                        ownPermission.setFolderAdmin(true);
                        ownPermission.setAllObjectPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
                        ownPermission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
                    }
                }
            }
            {
                final Element aclElement = element.getOptionalElement("acl");
                if (null != aclElement) {
                    final List<Element> grantElements = aclElement.listElements("grant");
                    if (null != grantElements && !grantElements.isEmpty()) {
                        final List<MailPermission> perms = new ArrayList<MailPermission>(grantElements.size());
                        final ContextService contextService = Services.getService(ContextService.class);
                        final Context ctx = contextService.getContext(performer.getContextId());
                        for (final Element grantElement : grantElements) {
                            if (!"usr".equals(grantElement.getAttribute("gt", ""))) {
                                continue;
                            }
                            // ACL
                            final ACLPermission permission = new ACLPermission();
                            final ACL acl = new ACL(grantElement.getAttribute("d", ""));
                            String s = grantElement.getAttribute("perm", "");
                            final Rights rights = new Rights();
                            if (!isEmpty(s)) {
                                if (s.indexOf('r') >= 0) {
                                    rights.add(Rights.Right.READ);
                                }
                                if (s.indexOf('w') >= 0) {
                                    rights.add(Rights.Right.WRITE);
                                }
                                if (s.indexOf('i') >= 0) {
                                    rights.add(Rights.Right.INSERT);
                                }
                                if (s.indexOf('d') >= 0) {
                                    rights.add(Rights.Right.DELETE);
                                }
                                if (s.indexOf('a') >= 0) {
                                    rights.add(Rights.Right.ADMINISTER);
                                }
                            }
                            permission.parseACL(acl, ctx);
                            perms.add(permission);
                        }
                        mailFolder.addPermissions(perms);
                    }
                }
            }
            {
                final List<Element> subfolders = element.listElements("folder");
                if (null != subfolders) {
                    mailFolder.setSubfolders(!subfolders.isEmpty());
                } else {
                    mailFolder.setSubfolders(false);
                }
            }
            if (null == mailFolder.getParentFullname()) {
                mailFolder.setRootFolder(true);
                mailFolder.setFullname(MailFolder.DEFAULT_FOLDER_ID);
                mailFolder.setName(MailFolder.DEFAULT_FOLDER_NAME);
            } else {
                mailFolder.setRootFolder(false);
            }
            return mailFolder;
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public static boolean isFolder(final Element element) {
        if (null == element) {
            return false;
        }
        return "folder".equals(element.getName());
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
