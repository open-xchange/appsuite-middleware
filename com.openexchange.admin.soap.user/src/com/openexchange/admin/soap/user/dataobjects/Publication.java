
package com.openexchange.admin.soap.user.dataobjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr Publication complex type.
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Publication", propOrder = {
    "userId",
    "context",
    "id",
    "entityId",
    "module",
    "name",
    "description"
})
public class Publication {

    @XmlElement(nillable = true)
    protected Integer userId;

    @XmlElement(nillable = true)
    protected Context context;

    @XmlElement(nillable = true)
    protected Integer id;

    @XmlElement(nillable = true)
    protected String entityId;

    @XmlElement(nillable = true)
    protected String module;

    @XmlElement(nillable = true)
    protected String name;

    @XmlElement(nillable = true)
    protected String description;

    /**
     * Initializes a new {@link Publication}.
     */
    public Publication() {
        super();
    }

    /**
     * Gets the user identifier
     * 
     * @return The user identifier
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Sets the user identifier
     * 
     * @param userId The user identifier to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Gets the context
     * 
     * @return The context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Sets the context
     * 
     * @param context The context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Gets the identifier
     * 
     * @return The identifier
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the identifier
     * 
     * @param id The identifier to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the entity identifier
     * 
     * @return The entity identifier
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Sets the entity identifier
     * 
     * @param entityId The entity identifier to set
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Gets the module
     * 
     * @return The module
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the module
     * 
     * @param module The module to set
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Gets the name
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     * 
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     * 
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
