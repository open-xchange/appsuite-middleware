
package com._4psa.channelmessages_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com._4psa.channeldata_xsd._2_5.ChannelGroupInfo;
import com._4psa.channeldata_xsd._2_5.RoutingRuleGroupInfo;
import com._4psa.channelmessagesinfo_xsd._2_5.AddCallRulesOutResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.AddPublicNoResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.AssignPublicNoResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.DelCallRulesOutGroupResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.DelChannelGroupResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.DelChannelResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.DelPublicNoResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.EditPublicNoResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.GetCallRulesOutResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.GetChannelGroupsResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.GetChannelsResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.GetCodecsResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.GetGroupSelectionResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.GetNoSelectionResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.GetPublicNoResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.UpdateCallRulesOutGroupResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.UpdateChannelGroupResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.UpdateChannelResponseType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com._4psa.channelmessages_xsd._2_5 package. 
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

    private final static QName _AssignPublicNoResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "AssignPublicNoResponse");
    private final static QName _GetChannelsResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "GetChannelsResponse");
    private final static QName _AddChannelGroupRequest_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "AddChannelGroupRequest");
    private final static QName _UnassignPublicNoResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "UnassignPublicNoResponse");
    private final static QName _DelPublicNoResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "DelPublicNoResponse");
    private final static QName _AddCallRulesOutResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "AddCallRulesOutResponse");
    private final static QName _AddChannelGroupResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "AddChannelGroupResponse");
    private final static QName _GetCodecsResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "GetCodecsResponse");
    private final static QName _EditCallRulesOutGroupResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "EditCallRulesOutGroupResponse");
    private final static QName _AddChannelResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "AddChannelResponse");
    private final static QName _DelCallRulesOutResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "DelCallRulesOutResponse");
    private final static QName _DelChannelResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "DelChannelResponse");
    private final static QName _AddPublicNoResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "AddPublicNoResponse");
    private final static QName _AddCallRulesOutGroupRequest_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "AddCallRulesOutGroupRequest");
    private final static QName _EditChannelGroupResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "EditChannelGroupResponse");
    private final static QName _EditPublicNoResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "EditPublicNoResponse");
    private final static QName _AddCallRulesOutGroupResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "AddCallRulesOutGroupResponse");
    private final static QName _GetPublicNoPollResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "GetPublicNoPollResponse");
    private final static QName _GetPublicNoResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "GetPublicNoResponse");
    private final static QName _GetChannelGroupPollResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "GetChannelGroupPollResponse");
    private final static QName _DelCallRulesOutGroupResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "DelCallRulesOutGroupResponse");
    private final static QName _EditChannelResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "EditChannelResponse");
    private final static QName _GetCallRulesOutResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "GetCallRulesOutResponse");
    private final static QName _GetChannelGroupsResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "GetChannelGroupsResponse");
    private final static QName _DelChannelGroupResponse_QNAME = new QName("http://4psa.com/ChannelMessages.xsd/2.5.1", "DelChannelGroupResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.channelmessages_xsd._2_5
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EditCallRulesOutGroupRequest }
     * 
     */
    public EditCallRulesOutGroupRequest createEditCallRulesOutGroupRequest() {
        return new EditCallRulesOutGroupRequest();
    }

    /**
     * Create an instance of {@link AddPublicNoRequest }
     * 
     */
    public AddPublicNoRequest createAddPublicNoRequest() {
        return new AddPublicNoRequest();
    }

    /**
     * Create an instance of {@link EditCallRulesOutGroupRequest.RuleStatus }
     * 
     */
    public EditCallRulesOutGroupRequest.RuleStatus createEditCallRulesOutGroupRequestRuleStatus() {
        return new EditCallRulesOutGroupRequest.RuleStatus();
    }

    /**
     * Create an instance of {@link AssignPublicNoRequest }
     * 
     */
    public AssignPublicNoRequest createAssignPublicNoRequest() {
        return new AssignPublicNoRequest();
    }

    /**
     * Create an instance of {@link DelChannelRequest }
     * 
     */
    public DelChannelRequest createDelChannelRequest() {
        return new DelChannelRequest();
    }

    /**
     * Create an instance of {@link EditChannelRequest }
     * 
     */
    public EditChannelRequest createEditChannelRequest() {
        return new EditChannelRequest();
    }

    /**
     * Create an instance of {@link EditChannelGroupRequest }
     * 
     */
    public EditChannelGroupRequest createEditChannelGroupRequest() {
        return new EditChannelGroupRequest();
    }

    /**
     * Create an instance of {@link GetCallRulesOutGroupRequest }
     * 
     */
    public GetCallRulesOutGroupRequest createGetCallRulesOutGroupRequest() {
        return new GetCallRulesOutGroupRequest();
    }

    /**
     * Create an instance of {@link EditPublicNoRequest }
     * 
     */
    public EditPublicNoRequest createEditPublicNoRequest() {
        return new EditPublicNoRequest();
    }

    /**
     * Create an instance of {@link GetCodecsRequest }
     * 
     */
    public GetCodecsRequest createGetCodecsRequest() {
        return new GetCodecsRequest();
    }

    /**
     * Create an instance of {@link DelCallRulesOutRequest }
     * 
     */
    public DelCallRulesOutRequest createDelCallRulesOutRequest() {
        return new DelCallRulesOutRequest();
    }

    /**
     * Create an instance of {@link AddPublicNoRequest.PublicNo }
     * 
     */
    public AddPublicNoRequest.PublicNo createAddPublicNoRequestPublicNo() {
        return new AddPublicNoRequest.PublicNo();
    }

    /**
     * Create an instance of {@link AddChannelRequest }
     * 
     */
    public AddChannelRequest createAddChannelRequest() {
        return new AddChannelRequest();
    }

    /**
     * Create an instance of {@link UnassignPublicNoRequest }
     * 
     */
    public UnassignPublicNoRequest createUnassignPublicNoRequest() {
        return new UnassignPublicNoRequest();
    }

    /**
     * Create an instance of {@link GetChannelsRequest }
     * 
     */
    public GetChannelsRequest createGetChannelsRequest() {
        return new GetChannelsRequest();
    }

    /**
     * Create an instance of {@link DelPublicNoRequest }
     * 
     */
    public DelPublicNoRequest createDelPublicNoRequest() {
        return new DelPublicNoRequest();
    }

    /**
     * Create an instance of {@link GetPublicNoRequest }
     * 
     */
    public GetPublicNoRequest createGetPublicNoRequest() {
        return new GetPublicNoRequest();
    }

    /**
     * Create an instance of {@link GetChannelGroupPollRequest }
     * 
     */
    public GetChannelGroupPollRequest createGetChannelGroupPollRequest() {
        return new GetChannelGroupPollRequest();
    }

    /**
     * Create an instance of {@link AddCallRulesOutRequest }
     * 
     */
    public AddCallRulesOutRequest createAddCallRulesOutRequest() {
        return new AddCallRulesOutRequest();
    }

    /**
     * Create an instance of {@link DelCallRulesOutGroupRequest }
     * 
     */
    public DelCallRulesOutGroupRequest createDelCallRulesOutGroupRequest() {
        return new DelCallRulesOutGroupRequest();
    }

    /**
     * Create an instance of {@link GetPublicNoPollRequest }
     * 
     */
    public GetPublicNoPollRequest createGetPublicNoPollRequest() {
        return new GetPublicNoPollRequest();
    }

    /**
     * Create an instance of {@link GetCallRulesOutRequest }
     * 
     */
    public GetCallRulesOutRequest createGetCallRulesOutRequest() {
        return new GetCallRulesOutRequest();
    }

    /**
     * Create an instance of {@link GetCallRulesOutGroupResponse }
     * 
     */
    public GetCallRulesOutGroupResponse createGetCallRulesOutGroupResponse() {
        return new GetCallRulesOutGroupResponse();
    }

    /**
     * Create an instance of {@link GetChannelGroupsRequest }
     * 
     */
    public GetChannelGroupsRequest createGetChannelGroupsRequest() {
        return new GetChannelGroupsRequest();
    }

    /**
     * Create an instance of {@link DelChannelGroupRequest }
     * 
     */
    public DelChannelGroupRequest createDelChannelGroupRequest() {
        return new DelChannelGroupRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssignPublicNoResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "AssignPublicNoResponse")
    public JAXBElement<AssignPublicNoResponseType> createAssignPublicNoResponse(AssignPublicNoResponseType value) {
        return new JAXBElement<AssignPublicNoResponseType>(_AssignPublicNoResponse_QNAME, AssignPublicNoResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChannelsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "GetChannelsResponse")
    public JAXBElement<GetChannelsResponseType> createGetChannelsResponse(GetChannelsResponseType value) {
        return new JAXBElement<GetChannelsResponseType>(_GetChannelsResponse_QNAME, GetChannelsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChannelGroupInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "AddChannelGroupRequest")
    public JAXBElement<ChannelGroupInfo> createAddChannelGroupRequest(ChannelGroupInfo value) {
        return new JAXBElement<ChannelGroupInfo>(_AddChannelGroupRequest_QNAME, ChannelGroupInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssignPublicNoResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "UnassignPublicNoResponse")
    public JAXBElement<AssignPublicNoResponseType> createUnassignPublicNoResponse(AssignPublicNoResponseType value) {
        return new JAXBElement<AssignPublicNoResponseType>(_UnassignPublicNoResponse_QNAME, AssignPublicNoResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelPublicNoResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "DelPublicNoResponse")
    public JAXBElement<DelPublicNoResponseType> createDelPublicNoResponse(DelPublicNoResponseType value) {
        return new JAXBElement<DelPublicNoResponseType>(_DelPublicNoResponse_QNAME, DelPublicNoResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddCallRulesOutResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "AddCallRulesOutResponse")
    public JAXBElement<AddCallRulesOutResponseType> createAddCallRulesOutResponse(AddCallRulesOutResponseType value) {
        return new JAXBElement<AddCallRulesOutResponseType>(_AddCallRulesOutResponse_QNAME, AddCallRulesOutResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateChannelGroupResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "AddChannelGroupResponse")
    public JAXBElement<UpdateChannelGroupResponseType> createAddChannelGroupResponse(UpdateChannelGroupResponseType value) {
        return new JAXBElement<UpdateChannelGroupResponseType>(_AddChannelGroupResponse_QNAME, UpdateChannelGroupResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCodecsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "GetCodecsResponse")
    public JAXBElement<GetCodecsResponseType> createGetCodecsResponse(GetCodecsResponseType value) {
        return new JAXBElement<GetCodecsResponseType>(_GetCodecsResponse_QNAME, GetCodecsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateCallRulesOutGroupResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "EditCallRulesOutGroupResponse")
    public JAXBElement<UpdateCallRulesOutGroupResponseType> createEditCallRulesOutGroupResponse(UpdateCallRulesOutGroupResponseType value) {
        return new JAXBElement<UpdateCallRulesOutGroupResponseType>(_EditCallRulesOutGroupResponse_QNAME, UpdateCallRulesOutGroupResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateChannelResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "AddChannelResponse")
    public JAXBElement<UpdateChannelResponseType> createAddChannelResponse(UpdateChannelResponseType value) {
        return new JAXBElement<UpdateChannelResponseType>(_AddChannelResponse_QNAME, UpdateChannelResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelCallRulesOutGroupResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "DelCallRulesOutResponse")
    public JAXBElement<DelCallRulesOutGroupResponseType> createDelCallRulesOutResponse(DelCallRulesOutGroupResponseType value) {
        return new JAXBElement<DelCallRulesOutGroupResponseType>(_DelCallRulesOutResponse_QNAME, DelCallRulesOutGroupResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelChannelResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "DelChannelResponse")
    public JAXBElement<DelChannelResponseType> createDelChannelResponse(DelChannelResponseType value) {
        return new JAXBElement<DelChannelResponseType>(_DelChannelResponse_QNAME, DelChannelResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddPublicNoResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "AddPublicNoResponse")
    public JAXBElement<AddPublicNoResponseType> createAddPublicNoResponse(AddPublicNoResponseType value) {
        return new JAXBElement<AddPublicNoResponseType>(_AddPublicNoResponse_QNAME, AddPublicNoResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RoutingRuleGroupInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "AddCallRulesOutGroupRequest")
    public JAXBElement<RoutingRuleGroupInfo> createAddCallRulesOutGroupRequest(RoutingRuleGroupInfo value) {
        return new JAXBElement<RoutingRuleGroupInfo>(_AddCallRulesOutGroupRequest_QNAME, RoutingRuleGroupInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateChannelGroupResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "EditChannelGroupResponse")
    public JAXBElement<UpdateChannelGroupResponseType> createEditChannelGroupResponse(UpdateChannelGroupResponseType value) {
        return new JAXBElement<UpdateChannelGroupResponseType>(_EditChannelGroupResponse_QNAME, UpdateChannelGroupResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EditPublicNoResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "EditPublicNoResponse")
    public JAXBElement<EditPublicNoResponseType> createEditPublicNoResponse(EditPublicNoResponseType value) {
        return new JAXBElement<EditPublicNoResponseType>(_EditPublicNoResponse_QNAME, EditPublicNoResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateCallRulesOutGroupResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "AddCallRulesOutGroupResponse")
    public JAXBElement<UpdateCallRulesOutGroupResponseType> createAddCallRulesOutGroupResponse(UpdateCallRulesOutGroupResponseType value) {
        return new JAXBElement<UpdateCallRulesOutGroupResponseType>(_AddCallRulesOutGroupResponse_QNAME, UpdateCallRulesOutGroupResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetNoSelectionResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "GetPublicNoPollResponse")
    public JAXBElement<GetNoSelectionResponseType> createGetPublicNoPollResponse(GetNoSelectionResponseType value) {
        return new JAXBElement<GetNoSelectionResponseType>(_GetPublicNoPollResponse_QNAME, GetNoSelectionResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPublicNoResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "GetPublicNoResponse")
    public JAXBElement<GetPublicNoResponseType> createGetPublicNoResponse(GetPublicNoResponseType value) {
        return new JAXBElement<GetPublicNoResponseType>(_GetPublicNoResponse_QNAME, GetPublicNoResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGroupSelectionResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "GetChannelGroupPollResponse")
    public JAXBElement<GetGroupSelectionResponseType> createGetChannelGroupPollResponse(GetGroupSelectionResponseType value) {
        return new JAXBElement<GetGroupSelectionResponseType>(_GetChannelGroupPollResponse_QNAME, GetGroupSelectionResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelCallRulesOutGroupResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "DelCallRulesOutGroupResponse")
    public JAXBElement<DelCallRulesOutGroupResponseType> createDelCallRulesOutGroupResponse(DelCallRulesOutGroupResponseType value) {
        return new JAXBElement<DelCallRulesOutGroupResponseType>(_DelCallRulesOutGroupResponse_QNAME, DelCallRulesOutGroupResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateChannelResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "EditChannelResponse")
    public JAXBElement<UpdateChannelResponseType> createEditChannelResponse(UpdateChannelResponseType value) {
        return new JAXBElement<UpdateChannelResponseType>(_EditChannelResponse_QNAME, UpdateChannelResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCallRulesOutResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "GetCallRulesOutResponse")
    public JAXBElement<GetCallRulesOutResponseType> createGetCallRulesOutResponse(GetCallRulesOutResponseType value) {
        return new JAXBElement<GetCallRulesOutResponseType>(_GetCallRulesOutResponse_QNAME, GetCallRulesOutResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChannelGroupsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "GetChannelGroupsResponse")
    public JAXBElement<GetChannelGroupsResponseType> createGetChannelGroupsResponse(GetChannelGroupsResponseType value) {
        return new JAXBElement<GetChannelGroupsResponseType>(_GetChannelGroupsResponse_QNAME, GetChannelGroupsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelChannelGroupResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ChannelMessages.xsd/2.5.1", name = "DelChannelGroupResponse")
    public JAXBElement<DelChannelGroupResponseType> createDelChannelGroupResponse(DelChannelGroupResponseType value) {
        return new JAXBElement<DelChannelGroupResponseType>(_DelChannelGroupResponse_QNAME, DelChannelGroupResponseType.class, null, value);
    }

}
