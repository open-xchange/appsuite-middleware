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
