
package com._4psa.extensionmessagesinfo_xsd._2_5;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.Notice;
import com._4psa.extensiondata_xsd._2_5.Agent;
import com._4psa.extensiondata_xsd._2_5.RemoteAgent;


/**
 * Get queue agents list: response type
 *
 * <p>Java class for GetQueueAgentsResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetQueueAgentsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="localAgents" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}Agent">
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="notice" type="{http://4psa.com/Common.xsd/2.5.1}notice" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="remoteAgents" type="{http://4psa.com/ExtensionData.xsd/2.5.1}RemoteAgent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetQueueAgentsResponseType", propOrder = {
    "localAgents",
    "notice",
    "remoteAgents"
})
public class GetQueueAgentsResponseType {

    protected List<GetQueueAgentsResponseType.LocalAgents> localAgents;
    protected List<Notice> notice;
    protected List<RemoteAgent> remoteAgents;

    /**
     * Gets the value of the localAgents property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localAgents property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocalAgents().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetQueueAgentsResponseType.LocalAgents }
     *
     *
     */
    public List<GetQueueAgentsResponseType.LocalAgents> getLocalAgents() {
        if (localAgents == null) {
            localAgents = new ArrayList<GetQueueAgentsResponseType.LocalAgents>();
        }
        return this.localAgents;
    }

    /**
     * Gets the value of the notice property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the notice property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNotice().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Notice }
     *
     *
     */
    public List<Notice> getNotice() {
        if (notice == null) {
            notice = new ArrayList<Notice>();
        }
        return this.notice;
    }

    /**
     * Gets the value of the remoteAgents property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the remoteAgents property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRemoteAgents().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteAgent }
     *
     *
     */
    public List<RemoteAgent> getRemoteAgents() {
        if (remoteAgents == null) {
            remoteAgents = new ArrayList<RemoteAgent>();
        }
        return this.remoteAgents;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}Agent">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LocalAgents
        extends Agent
    {


    }

}
