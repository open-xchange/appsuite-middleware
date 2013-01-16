
package com.openexchange.admin.soap.taskmgmt.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TaskManagerException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}TaskManagerException" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "taskManagerException"
})
@XmlRootElement(name = "TaskManagerException")
public class TaskManagerException {

    @XmlElement(name = "TaskManagerException", nillable = true)
    protected com.openexchange.admin.soap.taskmgmt.exceptions.TaskManagerException taskManagerException;

    /**
     * Ruft den Wert der taskManagerException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.taskmgmt.exceptions.TaskManagerException }
     *
     */
    public com.openexchange.admin.soap.taskmgmt.exceptions.TaskManagerException getTaskManagerException() {
        return taskManagerException;
    }

    /**
     * Legt den Wert der taskManagerException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.taskmgmt.exceptions.TaskManagerException }
     *
     */
    public void setTaskManagerException(com.openexchange.admin.soap.taskmgmt.exceptions.TaskManagerException value) {
        this.taskManagerException = value;
    }

}
