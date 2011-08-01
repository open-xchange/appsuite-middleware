
package com._4psa.channeldata_xsd._2_5;

import java.math.BigDecimal;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com._4psa.channeldata_xsd._2_5 package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SIPChannelInfoQualify_QNAME = new QName("http://4psa.com/ChannelData.xsd/2.5.1", "qualify");
    private final static QName _SIPChannelInfoConcurentCalls_QNAME = new QName("http://4psa.com/ChannelData.xsd/2.5.1", "concurentCalls");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.channeldata_xsd._2_5
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PublicNoInfo }
     *
     */
    public PublicNoInfo createPublicNoInfo() {
        return new PublicNoInfo();
    }

    /**
     * Create an instance of {@link PublicNoSelection }
     *
     */
    public PublicNoSelection createPublicNoSelection() {
        return new PublicNoSelection();
    }

    /**
     * Create an instance of {@link ChannelGroupSelection }
     *
     */
    public ChannelGroupSelection createChannelGroupSelection() {
        return new ChannelGroupSelection();
    }

    /**
     * Create an instance of {@link SIPChannelInfo }
     *
     */
    public SIPChannelInfo createSIPChannelInfo() {
        return new SIPChannelInfo();
    }

    /**
     * Create an instance of {@link ChannelGroupInfo }
     *
     */
    public ChannelGroupInfo createChannelGroupInfo() {
        return new ChannelGroupInfo();
    }

    /**
     * Create an instance of {@link RoutingRuleGroupInfo }
     *
     */
    public RoutingRuleGroupInfo createRoutingRuleGroupInfo() {
        return new RoutingRuleGroupInfo();
    }

    /**
     * Create an instance of {@link PublicNoInfo.IncomingCost }
     *
     */
    public PublicNoInfo.IncomingCost createPublicNoInfoIncomingCost() {
        return new PublicNoInfo.IncomingCost();
    }

    /**
     * Create an instance of {@link RoutingRuleInfo }
     *
     */
    public RoutingRuleInfo createRoutingRuleInfo() {
        return new RoutingRuleInfo();
    }

    /**
     * Create an instance of {@link CallRulesOutGroupList }
     *
     */
    public CallRulesOutGroupList createCallRulesOutGroupList() {
        return new CallRulesOutGroupList();
    }

    /**
     * Create an instance of {@link PublicNoList }
     *
     */
    public PublicNoList createPublicNoList() {
        return new PublicNoList();
    }

    /**
     * Create an instance of {@link ChannelGroupList }
     *
     */
    public ChannelGroupList createChannelGroupList() {
        return new ChannelGroupList();
    }

    /**
     * Create an instance of {@link ChannelList }
     *
     */
    public ChannelList createChannelList() {
        return new ChannelList();
    }

    /**
     * Create an instance of {@link Codecs }
     *
     */
    public Codecs createCodecs() {
        return new Codecs();
    }

    /**
     * Create an instance of {@link PublicNoSelection.Available }
     *
     */
    public PublicNoSelection.Available createPublicNoSelectionAvailable() {
        return new PublicNoSelection.Available();
    }

    /**
     * Create an instance of {@link PublicNoSelection.Assigned }
     *
     */
    public PublicNoSelection.Assigned createPublicNoSelectionAssigned() {
        return new PublicNoSelection.Assigned();
    }

    /**
     * Create an instance of {@link ChannelGroupSelection.Available }
     *
     */
    public ChannelGroupSelection.Available createChannelGroupSelectionAvailable() {
        return new ChannelGroupSelection.Available();
    }

    /**
     * Create an instance of {@link ChannelGroupSelection.Assigned }
     *
     */
    public ChannelGroupSelection.Assigned createChannelGroupSelectionAssigned() {
        return new ChannelGroupSelection.Assigned();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelData.xsd/2.5.1", name = "qualify", scope = SIPChannelInfo.class)
    public JAXBElement<BigDecimal> createSIPChannelInfoQualify(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_SIPChannelInfoQualify_QNAME, BigDecimal.class, SIPChannelInfo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelData.xsd/2.5.1", name = "concurentCalls", scope = SIPChannelInfo.class, defaultValue = "10")
    public JAXBElement<BigDecimal> createSIPChannelInfoConcurentCalls(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_SIPChannelInfoConcurentCalls_QNAME, BigDecimal.class, SIPChannelInfo.class, value);
    }

}
