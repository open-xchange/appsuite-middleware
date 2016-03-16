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

package com.openexchange.report.client.container;

import java.util.ArrayList;
import java.util.List;

public class ContextDetail {

    private String id;

    private String users;

    private String age;

    private String created;

    private String mappings;

    private String adminmac;

    private List<ContextModuleAccessCombination> moduleAccessCombinations;

    public ContextDetail() {
        this.moduleAccessCombinations = new ArrayList<ContextModuleAccessCombination>();
    }

    public ContextDetail(final String id, final String users, final String age, final String created, final String mappings, final String adminmac) {
        this.id = id;
        this.users = users;
        this.age = age;
        this.created = created;
        this.mappings = mappings;
        this.adminmac = adminmac;
        this.moduleAccessCombinations = new ArrayList<ContextModuleAccessCombination>();
    }

    public ContextDetail(final String id, final String users, final String age, final String created, final String mappings, final String adminmac, final List<ContextModuleAccessCombination> moduleAccessCombinations) {
        this.id = id;
        this.users = users;
        this.age = age;
        this.created = created;
        this.mappings = mappings;
        this.adminmac = adminmac;
        this.moduleAccessCombinations = moduleAccessCombinations;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(final String users) {
        this.users = users;
    }

    public String getAge() {
        return age;
    }

    public void setAge(final String age) {
        this.age = age;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getMappings() {
        return mappings;
    }

    public void setMappings(final String mappings) {
        this.mappings = mappings;
    }

    public String getAdminmac() {
        return adminmac;
    }

    public void setAdminmac(final String adminmac) {
        this.adminmac = adminmac;
    }

    public List<ContextModuleAccessCombination> getModuleAccessCombinations() {
        return moduleAccessCombinations;
    }

    public void setModuleAccessCombinations(final List<ContextModuleAccessCombination> moduleAccessCombinations) {
        this.moduleAccessCombinations = moduleAccessCombinations;
    }

    public void clearModuleAccessCombinations() {
        this.moduleAccessCombinations = new ArrayList<ContextModuleAccessCombination>();
    }

    public void addModuleAccessCombination(final ContextModuleAccessCombination moduleAccessCombination) {
        moduleAccessCombinations.add(moduleAccessCombination);
    }

    public ContextModuleAccessCombination getModuleAccessCombination(final int position) {
        return moduleAccessCombinations.get(position);
    }

    public void setModuleAccessCombination(final ContextModuleAccessCombination moduleAccessCombination, final int position) {
        moduleAccessCombinations.set(position, moduleAccessCombination);
    }

    public void removeModuleAccessCombination(final int position) {
        moduleAccessCombinations.remove(position);
    }

    @Override
    public String toString() {
        return "ContextDetailObject [age=" + age + ", created=" + created + ", id=" + id + ", mappings=" + mappings + ", moduleAccessCombinations=" + moduleAccessCombinations + ", users=" + users + "]";
    }

}
