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

package com.openexchange.report.internal;

import java.util.Date;
import java.util.Map;


public class ReportContext {
    private Integer id;

    private Integer adminId;

    private Long age;

    private Date created;

    private Integer adminPermission;

    private Integer numUsers;

    private Map<Integer,Integer> accessCombinations;

    private Map<Integer,Integer> inactiveByCombination;

    public ReportContext() {
        super();
        this.id = null;
        this.adminId = null;
        this.age = null;
        this.created = null;
        this.adminPermission = null;
        this.numUsers = null;
        this.accessCombinations = null;
        this.inactiveByCombination = null;
    }






    /**
     * @return the inactiveByCombination
     */
    public final Map<Integer, Integer> getInactiveByCombination() {
        return inactiveByCombination;
    }






    /**
     * @param inactiveByCombination the inactiveByCombination to set
     */
    public final void setInactiveByCombination(Map<Integer, Integer> inactiveByCombination) {
        this.inactiveByCombination = inactiveByCombination;
    }





    /**
     * @return the numUsers
     */
    public final Integer getNumUsers() {
        return numUsers;
    }






    /**
     * @return the accessCombinations
     */
    public final Map<Integer, Integer> getAccessCombinations() {
        return accessCombinations;
    }






    /**
     * @param accessCombinations the accessCombinations to set
     */
    public final void setAccessCombinations(Map<Integer, Integer> accessCombinations) {
        this.accessCombinations = accessCombinations;
    }






    /**
     * @param numUsers the numUsers to set
     */
    public final void setNumUsers(Integer numUsers) {
        this.numUsers = numUsers;
    }




    /**
     * @return the adminPermission
     */
    public final Integer getAdminPermission() {
        return adminPermission;
    }




    /**
     * @param adminPermission the adminPermission to set
     */
    public final void setAdminPermission(Integer adminPermission) {
        this.adminPermission = adminPermission;
    }



    /**
     * @return the created
     */
    public final Date getCreated() {
        return created;
    }



    /**
     * @param created the created to set
     */
    public final void setCreated(Date created) {
        this.created = created;
    }


    /**
     * @return the id
     */
    public final Integer getId() {
        return id;
    }


    /**
     * @param id the id to set
     */
    public final void setId(Integer id) {
        this.id = id;
    }


    /**
     * @return the adminId
     */
    public final Integer getAdminId() {
        return adminId;
    }


    /**
     * @param adminId the adminId to set
     */
    public final void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }


    /**
     * @return the age
     */
    public final Long getAge() {
        return age;
    }


    /**
     * @param age the age to set
     */
    public final void setAge(Long age) {
        this.age = age;
    }

}
