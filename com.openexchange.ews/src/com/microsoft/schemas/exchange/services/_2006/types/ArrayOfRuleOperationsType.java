
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents an array of rule operations to be performed
 * 
 * <p>Java class for ArrayOfRuleOperationsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfRuleOperationsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="CreateRuleOperation" type="{http://schemas.microsoft.com/exchange/services/2006/types}CreateRuleOperationType"/>
 *         &lt;element name="SetRuleOperation" type="{http://schemas.microsoft.com/exchange/services/2006/types}SetRuleOperationType"/>
 *         &lt;element name="DeleteRuleOperation" type="{http://schemas.microsoft.com/exchange/services/2006/types}DeleteRuleOperationType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfRuleOperationsType", propOrder = {
    "createRuleOperationOrSetRuleOperationOrDeleteRuleOperation"
})
public class ArrayOfRuleOperationsType {

    @XmlElements({
        @XmlElement(name = "CreateRuleOperation", type = CreateRuleOperationType.class),
        @XmlElement(name = "DeleteRuleOperation", type = DeleteRuleOperationType.class),
        @XmlElement(name = "SetRuleOperation", type = SetRuleOperationType.class)
    })
    protected List<RuleOperationType> createRuleOperationOrSetRuleOperationOrDeleteRuleOperation;

    /**
     * Gets the value of the createRuleOperationOrSetRuleOperationOrDeleteRuleOperation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the createRuleOperationOrSetRuleOperationOrDeleteRuleOperation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCreateRuleOperationOrSetRuleOperationOrDeleteRuleOperation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CreateRuleOperationType }
     * {@link DeleteRuleOperationType }
     * {@link SetRuleOperationType }
     * 
     * 
     */
    public List<RuleOperationType> getCreateRuleOperationOrSetRuleOperationOrDeleteRuleOperation() {
        if (createRuleOperationOrSetRuleOperationOrDeleteRuleOperation == null) {
            createRuleOperationOrSetRuleOperationOrDeleteRuleOperation = new ArrayList<RuleOperationType>();
        }
        return this.createRuleOperationOrSetRuleOperationOrDeleteRuleOperation;
    }

}
