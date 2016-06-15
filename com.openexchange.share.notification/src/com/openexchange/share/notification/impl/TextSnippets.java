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

package com.openexchange.share.notification.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.i18n.Translator;
import com.openexchange.share.groupware.DriveTargetProxyType;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetProxyType;
import com.openexchange.share.notification.NotificationStrings;

/**
 * {@link TextSnippets}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TextSnippets {

    private final Translator translator;

    public TextSnippets(Translator translator) {
        super();
        this.translator = translator;
    }

    /**
     * A short statement telling that somebody shared one or more folder(s)/image(s)/item(s) with you. E.g.:
     * <ul>
     *  <li>John Doe has shared 3 items with you.</li>
     *  <li>Jane Doe has shared image "duckface.jpg" with you.</li>
     * </ul>
     *
     * @param somebody The replacement for the sharing person
     * @param targetProxies The collection of shared targets as {@link TargetProxy}s
     * @return The translated and formatted string
     */
    public String shareStatementShort(String somebody, Collection<TargetProxy> targetProxies) {
        int count = targetProxies.size();
        Set<TargetProxyType> targetProxyTypes = determineTypes(targetProxies);
        if (count > 1 && targetProxyTypes.size() > 1) {
            return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEMS), somebody, count);
        } else {
            TargetProxy targetProxy = targetProxies.iterator().next();
            TargetProxyType targetProxyType = targetProxy.getProxyType();
            String itemName = targetProxy.getTitle();
            if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_IMAGE), somebody, itemName);
                } else {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_IMAGES), somebody, count);
                }
            } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FILE), somebody, itemName);
                } else {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FILES), somebody, count);
                }
            } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FOLDER), somebody, itemName);
                } else {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FOLDERS), somebody, count);
                }
            } else {
                //fall back to item for other types
                if (count == 1) {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEM), somebody, itemName);
                } else {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEMS), somebody);
                }
            }
        }
    }

    /**
     * A short statement telling that somebody shared one or more folder(s)/image(s)/item(s) with a group. E.g.:
     * <ul>
     *  <li>John Doe has shared 3 items with the group "Sales Dept.".</li>
     *  <li>Jane Doe has shared image "duckface.jpg" with the group "Sales Dept.".</li>
     * </ul>
     *
     * @param somebody The replacement for the sharing person
     * @param group The groups name
     * @param targetProxies The collection of shared targets as {@link TargetProxy}s
     * @return The translated and formatted string
     */
    public String shareStatementGroupShort(String somebody, String group, Collection<TargetProxy> targetProxies) {
        int count = targetProxies.size();
        Set<TargetProxyType> targetProxyTypes = determineTypes(targetProxies);
        if (count > 1 && targetProxyTypes.size() > 1) {
            return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEMS_GROUP), somebody, count, group);
        } else {
            TargetProxy targetProxy = targetProxies.iterator().next();
            TargetProxyType targetProxyType = targetProxy.getProxyType();
            String itemName = targetProxy.getTitle();
            if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_IMAGE_GROUP), somebody, itemName, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_IMAGES_GROUP), somebody, count, group);
                }
            } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FILE_GROUP), somebody, itemName, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FILES_GROUP), somebody, count, group);
                }
            } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                if (count == 1) {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FOLDER_GROUP), somebody, itemName, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FOLDERS_GROUP), somebody, count, group);
                }
            } else {
                //fall back to item for other types
                if (count == 1) {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEM_GROUP), somebody, itemName, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEMS_GROUP), somebody, group);
                }
            }
        }
    }

    /**
     * A long statement telling that somebody shared one or more folder(s)/image(s)/item(s) with you. E.g.:
     * <ul>
     *  <li>John Doe (jd@example.com) has shared 3 items with you and left you a message:</li>
     *  <li>Jane Doe (jd@example.com) has shared the image "duckface.jpg" with you.</li>
     * </ul>
     *
     * @param fullName The replacement for the sharing person
     * @param email The replacement for the sharing persons email address
     * @param targetProxies The collection of shared targets as {@link TargetProxy}s
     * @param hasMessage Whether the sharing entity authored a personal message for the recipient
     * @return The translated and formatted string
     */
    public String shareStatementLong(String fullName, String email, Collection<TargetProxy> targetProxies, boolean hasMessage) {
        int count = targetProxies.size();
        Set<TargetProxyType> targetProxyTypes = determineTypes(targetProxies);
        if (count > 1) {
            if (targetProxyTypes.size() > 1) {//multiple shares of different types
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE), fullName, email, count);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_NO_MESSAGE), fullName, email, count);
                }
            } else {//multiple shares of single type
                TargetProxyType targetProxyType = targetProxyTypes.iterator().next();
                if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                    if (hasMessage) {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGES_AND_MESSAGE), fullName, email, count);
                    } else {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGES_NO_IMAGES), fullName, email, count);
                    }

                } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
                    if (hasMessage) {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_FILES_AND_MESSAGE), fullName, email, count);
                    } else {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_FILES_NO_MESSAGE), fullName, email, count);
                    }
                } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                    if (hasMessage) {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDERS_AND_MESSAGE), fullName, email, count);
                    } else {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDERS_NO_MESSAGE), fullName, email, count);
                    }
                } else {
                    //fall back to item for other types
                    if (hasMessage) {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE), fullName, email, count);
                    } else {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_NO_MESSAGE), fullName, email, count);
                    }
                }
            }
        } else {
            TargetProxy targetProxy = targetProxies.iterator().next();
            TargetProxyType targetProxyType = targetProxyTypes.iterator().next();
            String filename = targetProxy.getTitle();
            if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_PHOTO_AND_MESSAGE), fullName, email, filename);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGE_NO_MESSAGE), fullName, email, filename);
                }
            } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_FILE_AND_MESSAGE), fullName, email, filename);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_FILE_NO_MESSAGE), fullName, email, filename);
                }
            } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDER_AND_MESSAGE), fullName, email, filename);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDER_NO_MESSAGE), fullName, email, filename);
                }
            } else {
                //fall back to item for other types
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEM_AND_MESSAGE), fullName, email, filename);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEM_NO_MESSAGE), fullName, email, filename);
                }
            }
        }
    }

    /**
     * A long statement telling that somebody shared one or more folder(s)/image(s)/item(s) with a group. E.g.:
     * <ul>
     *  <li>John Doe (jd@example.com) has shared 3 items with the group "Sales Dept." and left a message:</li>
     *  <li>Jane Doe (jd@example.com) has shared the image "duckface.jpg" with the group "Sales Dept.".</li>
     * </ul>
     *
     * @param fullName The replacement for the sharing person
     * @param email The replacement for the sharing persons email address
     * @param group The groups name
     * @param targetProxies The collection of shared targets as {@link TargetProxy}s
     * @param hasMessage Whether the sharing entity authored a personal message for the recipient
     * @return The translated and formatted string
     */
    public String shareStatementGroupLong(String fullName, String email, String group, Collection<TargetProxy> targetProxies, boolean hasMessage) {
        int count = targetProxies.size();
        Set<TargetProxyType> targetProxyTypes = determineTypes(targetProxies);
        if (count > 1) {
            if (targetProxyTypes.size() > 1) {//multiple shares of different types
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE_GROUP), fullName, email, count, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_NO_MESSAGE_GROUP), fullName, email, count, group);
                }
            } else {//multiple shares of single type
                TargetProxyType targetProxyType = targetProxyTypes.iterator().next();
                if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                    if (hasMessage) {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGES_AND_MESSAGE_GROUP), fullName, email, count, group);
                    } else {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGES_NO_IMAGES_GROUP), fullName, email, count, group);
                    }

                } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
                    if (hasMessage) {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_FILES_AND_MESSAGE_GROUP), fullName, email, count, group);
                    } else {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_FILES_NO_MESSAGE_GROUP), fullName, email, count, group);
                    }
                } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                    if (hasMessage) {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDERS_AND_MESSAGE_GROUP), fullName, email, count, group);
                    } else {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDERS_NO_MESSAGE_GROUP), fullName, email, count, group);
                    }
                } else {
                    //fall back to item for other types
                    if (hasMessage) {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE_GROUP), fullName, email, count, group);
                    } else {
                        return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_NO_MESSAGE_GROUP), fullName, email, count, group);
                    }
                }
            }
        } else {
            TargetProxy targetProxy = targetProxies.iterator().next();
            TargetProxyType targetProxyType = targetProxyTypes.iterator().next();
            String filename = targetProxy.getTitle();
            if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_PHOTO_AND_MESSAGE_GROUP), fullName, email, filename, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_IMAGE_NO_MESSAGE_GROUP), fullName, email, filename, group);
                }
            } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_FILE_AND_MESSAGE_GROUP), fullName, email, filename, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_FILE_NO_MESSAGE_GROUP), fullName, email, filename, group);
                }
            } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDER_AND_MESSAGE_GROUP), fullName, email, filename, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDER_NO_MESSAGE_GROUP), fullName, email, filename, group);
                }
            } else {
                //fall back to item for other types
                if (hasMessage) {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEM_AND_MESSAGE_GROUP), fullName, email, filename, group);
                } else {
                    return String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEM_NO_MESSAGE_GROUP), fullName, email, filename, group);
                }
            }
        }
    }

    /**
     * Gets the label for a share link based on the target proxies the link resolves to. E.g.:
     * <ul>
     *  <li>View photos</li>
     *  <li>View folder</li>
     * </ul>
     *
     * @param targetProxies The collection of shared targets as {@link TargetProxy}s
     * @return The translated and formatted string
     */
    public String linkLabel(Collection<TargetProxy> targetProxies) {
        boolean multipleShares = targetProxies.size() > 1;
        Set<TargetProxyType> targetProxyTypes = determineTypes(targetProxies);
        TargetProxyType targetProxyType = null;
        if (targetProxyTypes.size() == 1) {
            targetProxyType = targetProxyTypes.iterator().next();
        }

        if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
            if (multipleShares) {
                return translator.translate(NotificationStrings.VIEW_IMAGES);
            } else {
                return translator.translate(NotificationStrings.VIEW_IMAGE);
            }
        } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
            if (multipleShares) {
                return translator.translate(NotificationStrings.VIEW_FILES);
            } else {
                return translator.translate(NotificationStrings.VIEW_FILE);
            }
        } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
            if (multipleShares) {
                return translator.translate(NotificationStrings.VIEW_FOLDERS);
            } else {
                return translator.translate(NotificationStrings.VIEW_FOLDER);
            }
        } else {
            //fall back to item for other types
            if (multipleShares) {
                return translator.translate(NotificationStrings.VIEW_ITEMS);
            } else {
                return translator.translate(NotificationStrings.VIEW_ITEM);
            }
        }
    }

    private static Set<TargetProxyType> determineTypes(Collection<TargetProxy> targetProxies) {
        if (targetProxies.size() == 1) {
            return Collections.singleton(targetProxies.iterator().next().getProxyType());
        }

        HashSet<TargetProxyType> targetProxyTypes = new HashSet<>(targetProxies.size());
        for (TargetProxy targetProxy : targetProxies) {
            TargetProxyType proxyType = targetProxy.getProxyType();
            targetProxyTypes.add(proxyType);
        }
        return targetProxyTypes;
    }

}
