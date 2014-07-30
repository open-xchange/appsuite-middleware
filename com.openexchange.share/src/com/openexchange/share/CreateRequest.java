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

package com.openexchange.share;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.groupware.modules.Module;


/**
 * {@link ShareRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.x.x
 */
public class ShareRequest {

//    {
//        "module":"drive",
//        "folder":"43242",
//        "item":null,
//        "displayName":"Party Pictures",
//        "entities":[
//          {
//            "userId":42,
//            "permissions":268500996
//          },
//          {
//            "email":"otto@example.com",
//            "permissions":268500996,
//            "expires":1383056574868,
//            "auth":1
//          },
//          {
//            "email":"tante.erna@example.com",
//            "contactId":12,
//            "contactFolder":"contacts",
//            "permissions":268500996
//          }
//        ]
//      }

    private Module module;

    private String folder;

    private String item;

    private String displayName;

    private final List<Entity> entities = new ArrayList<Entity>(4);


    public Module getModule() {
        return module;
    }


    public void setModule(Module module) {
        this.module = module;
    }


    public String getFolder() {
        return folder;
    }


    public void setFolder(String folder) {
        this.folder = folder;
    }


    public String getItem() {
        return item;
    }


    public void setItem(String item) {
        this.item = item;
    }


    public String getDisplayName() {
        return displayName;
    }


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public List<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }



}
