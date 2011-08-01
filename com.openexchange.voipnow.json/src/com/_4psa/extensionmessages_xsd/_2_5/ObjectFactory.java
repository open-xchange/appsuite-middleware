
package com._4psa.extensionmessages_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com._4psa.common_xsd._2_5.DelObject;
import com._4psa.common_xsd._2_5.UpdateObject;
import com._4psa.extensiondata_xsd._2_5.FaxCenter;
import com._4psa.extensiondata_xsd._2_5.ProvisioningInfo;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetAuthCallerIDCreditResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetAvailableCallerIDResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRecordingSettingsResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRulesInResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetCallerIDResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetCardCodeResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetConferenceSettingsResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetExtensionDetailsResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetExtensionPLResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetExtensionResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetFaxCenterSettingsResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetQueueAgentsResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetQueueMembershipResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetVoicemailSettingsResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.ProvisionResponseType;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com._4psa.extensionmessages_xsd._2_5 package.
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

    private final static QName _SetExtensionCpAccessResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetExtensionCpAccessResponse");
    private final static QName _GetProvisionFileResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetProvisionFileResponse");
    private final static QName _DelExtensionResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "DelExtensionResponse");
    private final static QName _AddAuthCallerIDResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "AddAuthCallerIDResponse");
    private final static QName _DelCallRulesInResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "DelCallRulesInResponse");
    private final static QName _GetCallRulesInResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetCallRulesInResponse");
    private final static QName _SetupExtensionResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetupExtensionResponse");
    private final static QName _SetExtensionPLResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetExtensionPLResponse");
    private final static QName _SetFaxCenterRequest_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetFaxCenterRequest");
    private final static QName _GetAuthCallerIDResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetAuthCallerIDResponse");
    private final static QName _AddCardCodeCreditResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "AddCardCodeCreditResponse");
    private final static QName _AssignQueueAgentResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "AssignQueueAgentResponse");
    private final static QName _GetCardCodeResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetCardCodeResponse");
    private final static QName _SetProvisionResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetProvisionResponse");
    private final static QName _GetExtensionDetailsResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetExtensionDetailsResponse");
    private final static QName _SetConferenceResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetConferenceResponse");
    private final static QName _GetQueueAgentsResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetQueueAgentsResponse");
    private final static QName _GetExtensionPLResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetExtensionPLResponse");
    private final static QName _SetExtensionStatusResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetExtensionStatusResponse");
    private final static QName _GetFaxCenterSettingsResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetFaxCenterSettingsResponse");
    private final static QName _UnassignQueueAgentResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "UnassignQueueAgentResponse");
    private final static QName _AssignQueueRemoteAgentResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "AssignQueueRemoteAgentResponse");
    private final static QName _AddAuthCallerIDCreditResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "AddAuthCallerIDCreditResponse");
    private final static QName _AddCardCodeResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "AddCardCodeResponse");
    private final static QName _GetAvailableCallerIDResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetAvailableCallerIDResponse");
    private final static QName _GetExtensionsResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetExtensionsResponse");
    private final static QName _SetCallRecordingResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetCallRecordingResponse");
    private final static QName _EditAuthCallerIDResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "EditAuthCallerIDResponse");
    private final static QName _GetCallRecordingSettingsResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetCallRecordingSettingsResponse");
    private final static QName _AddCallRulesInResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "AddCallRulesInResponse");
    private final static QName _DelCardCodeResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "DelCardCodeResponse");
    private final static QName _SetProvisionRequest_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetProvisionRequest");
    private final static QName _GetConferenceSettingsResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetConferenceSettingsResponse");
    private final static QName _GetVoicemailSettingsResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetVoicemailSettingsResponse");
    private final static QName _SetQueueRemoteAgentResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetQueueRemoteAgentResponse");
    private final static QName _DelAuthCallerIDResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "DelAuthCallerIDResponse");
    private final static QName _EditCardCodeResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "EditCardCodeResponse");
    private final static QName _GetAuthCallerIDRechargesResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetAuthCallerIDRechargesResponse");
    private final static QName _SetVoicemailResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetVoicemailResponse");
    private final static QName _SetQueueMemberResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetQueueMemberResponse");
    private final static QName _GetQueueMembershipResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "GetQueueMembershipResponse");
    private final static QName _SetFaxCenterResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetFaxCenterResponse");
    private final static QName _EditCallRulesInResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "EditCallRulesInResponse");
    private final static QName _SetQueueAgentResponse_QNAME = new QName("http://4psa.com/ExtensionMessages.xsd/2.5.1", "SetQueueAgentResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.extensionmessages_xsd._2_5
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetExtensionSettingsResponse }
     *
     */
    public GetExtensionSettingsResponse createGetExtensionSettingsResponse() {
        return new GetExtensionSettingsResponse();
    }

    /**
     * Create an instance of {@link AddCallRulesInRequest }
     *
     */
    public AddCallRulesInRequest createAddCallRulesInRequest() {
        return new AddCallRulesInRequest();
    }

    /**
     * Create an instance of {@link EditCallRulesInRequest }
     *
     */
    public EditCallRulesInRequest createEditCallRulesInRequest() {
        return new EditCallRulesInRequest();
    }

    /**
     * Create an instance of {@link GetExtensionGroupsResponse }
     *
     */
    public GetExtensionGroupsResponse createGetExtensionGroupsResponse() {
        return new GetExtensionGroupsResponse();
    }

    /**
     * Create an instance of {@link SetupExtensionRequest }
     *
     */
    public SetupExtensionRequest createSetupExtensionRequest() {
        return new SetupExtensionRequest();
    }

    /**
     * Create an instance of {@link EditCallRulesInRequest.Rule }
     *
     */
    public EditCallRulesInRequest.Rule createEditCallRulesInRequestRule() {
        return new EditCallRulesInRequest.Rule();
    }

    /**
     * Create an instance of {@link EditCallRulesInRequest.Rule.Cascade }
     *
     */
    public EditCallRulesInRequest.Rule.Cascade createEditCallRulesInRequestRuleCascade() {
        return new EditCallRulesInRequest.Rule.Cascade();
    }

    /**
     * Create an instance of {@link AddCallRulesInRequest.Rule }
     *
     */
    public AddCallRulesInRequest.Rule createAddCallRulesInRequestRule() {
        return new AddCallRulesInRequest.Rule();
    }

    /**
     * Create an instance of {@link AddCallRulesInRequest.Rule.Cascade }
     *
     */
    public AddCallRulesInRequest.Rule.Cascade createAddCallRulesInRequestRuleCascade() {
        return new AddCallRulesInRequest.Rule.Cascade();
    }

    /**
     * Create an instance of {@link GetQueueMembershipRequest }
     *
     */
    public GetQueueMembershipRequest createGetQueueMembershipRequest() {
        return new GetQueueMembershipRequest();
    }

    /**
     * Create an instance of {@link GetExtensionSettingsResponse.IVR }
     *
     */
    public GetExtensionSettingsResponse.IVR createGetExtensionSettingsResponseIVR() {
        return new GetExtensionSettingsResponse.IVR();
    }

    /**
     * Create an instance of {@link GetCardCodeRechargesResponse }
     *
     */
    public GetCardCodeRechargesResponse createGetCardCodeRechargesResponse() {
        return new GetCardCodeRechargesResponse();
    }

    /**
     * Create an instance of {@link AddCardCodeCreditRequest }
     *
     */
    public AddCardCodeCreditRequest createAddCardCodeCreditRequest() {
        return new AddCardCodeCreditRequest();
    }

    /**
     * Create an instance of {@link SetVoicemailRequest }
     *
     */
    public SetVoicemailRequest createSetVoicemailRequest() {
        return new SetVoicemailRequest();
    }

    /**
     * Create an instance of {@link EditExtensionRequest }
     *
     */
    public EditExtensionRequest createEditExtensionRequest() {
        return new EditExtensionRequest();
    }

    /**
     * Create an instance of {@link DelExtensionRequest }
     *
     */
    public DelExtensionRequest createDelExtensionRequest() {
        return new DelExtensionRequest();
    }

    /**
     * Create an instance of {@link EditAuthCallerIDRequest }
     *
     */
    public EditAuthCallerIDRequest createEditAuthCallerIDRequest() {
        return new EditAuthCallerIDRequest();
    }

    /**
     * Create an instance of {@link SetQueueAgentRequest }
     *
     */
    public SetQueueAgentRequest createSetQueueAgentRequest() {
        return new SetQueueAgentRequest();
    }

    /**
     * Create an instance of {@link GetCallRecordingSettingsRequest }
     *
     */
    public GetCallRecordingSettingsRequest createGetCallRecordingSettingsRequest() {
        return new GetCallRecordingSettingsRequest();
    }

    /**
     * Create an instance of {@link SetCallRecordingRequest }
     *
     */
    public SetCallRecordingRequest createSetCallRecordingRequest() {
        return new SetCallRecordingRequest();
    }

    /**
     * Create an instance of {@link GetExtensionDetailsRequest }
     *
     */
    public GetExtensionDetailsRequest createGetExtensionDetailsRequest() {
        return new GetExtensionDetailsRequest();
    }

    /**
     * Create an instance of {@link SetQueueRemoteAgentRequest }
     *
     */
    public SetQueueRemoteAgentRequest createSetQueueRemoteAgentRequest() {
        return new SetQueueRemoteAgentRequest();
    }

    /**
     * Create an instance of {@link EditExtensionResponse }
     *
     */
    public EditExtensionResponse createEditExtensionResponse() {
        return new EditExtensionResponse();
    }

    /**
     * Create an instance of {@link DelCardCodeRequest }
     *
     */
    public DelCardCodeRequest createDelCardCodeRequest() {
        return new DelCardCodeRequest();
    }

    /**
     * Create an instance of {@link GetAvailableCallerIDRequest }
     *
     */
    public GetAvailableCallerIDRequest createGetAvailableCallerIDRequest() {
        return new GetAvailableCallerIDRequest();
    }

    /**
     * Create an instance of {@link GetQueueAgentsRequest }
     *
     */
    public GetQueueAgentsRequest createGetQueueAgentsRequest() {
        return new GetQueueAgentsRequest();
    }

    /**
     * Create an instance of {@link GetVoicemailSettingsRequest }
     *
     */
    public GetVoicemailSettingsRequest createGetVoicemailSettingsRequest() {
        return new GetVoicemailSettingsRequest();
    }

    /**
     * Create an instance of {@link SetExtensionPLRequest }
     *
     */
    public SetExtensionPLRequest createSetExtensionPLRequest() {
        return new SetExtensionPLRequest();
    }

    /**
     * Create an instance of {@link GetExtensionsRequest }
     *
     */
    public GetExtensionsRequest createGetExtensionsRequest() {
        return new GetExtensionsRequest();
    }

    /**
     * Create an instance of {@link AddCardCodeRequest }
     *
     */
    public AddCardCodeRequest createAddCardCodeRequest() {
        return new AddCardCodeRequest();
    }

    /**
     * Create an instance of {@link AddExtensionResponse }
     *
     */
    public AddExtensionResponse createAddExtensionResponse() {
        return new AddExtensionResponse();
    }

    /**
     * Create an instance of {@link GetCardCodeRechargesRequest }
     *
     */
    public GetCardCodeRechargesRequest createGetCardCodeRechargesRequest() {
        return new GetCardCodeRechargesRequest();
    }

    /**
     * Create an instance of {@link SetExtensionCpAccessRequest }
     *
     */
    public SetExtensionCpAccessRequest createSetExtensionCpAccessRequest() {
        return new SetExtensionCpAccessRequest();
    }

    /**
     * Create an instance of {@link GetExtensionSettingsRequest }
     *
     */
    public GetExtensionSettingsRequest createGetExtensionSettingsRequest() {
        return new GetExtensionSettingsRequest();
    }

    /**
     * Create an instance of {@link DelAuthCallerIDRequest }
     *
     */
    public DelAuthCallerIDRequest createDelAuthCallerIDRequest() {
        return new DelAuthCallerIDRequest();
    }

    /**
     * Create an instance of {@link GetFaxCenterSettingsRequest }
     *
     */
    public GetFaxCenterSettingsRequest createGetFaxCenterSettingsRequest() {
        return new GetFaxCenterSettingsRequest();
    }

    /**
     * Create an instance of {@link GetProvisionFileRequest }
     *
     */
    public GetProvisionFileRequest createGetProvisionFileRequest() {
        return new GetProvisionFileRequest();
    }

    /**
     * Create an instance of {@link SetConferenceRequest }
     *
     */
    public SetConferenceRequest createSetConferenceRequest() {
        return new SetConferenceRequest();
    }

    /**
     * Create an instance of {@link DelCallRulesInRequest }
     *
     */
    public DelCallRulesInRequest createDelCallRulesInRequest() {
        return new DelCallRulesInRequest();
    }

    /**
     * Create an instance of {@link GetConferenceSettingsRequest }
     *
     */
    public GetConferenceSettingsRequest createGetConferenceSettingsRequest() {
        return new GetConferenceSettingsRequest();
    }

    /**
     * Create an instance of {@link GetExtensionPLRequest }
     *
     */
    public GetExtensionPLRequest createGetExtensionPLRequest() {
        return new GetExtensionPLRequest();
    }

    /**
     * Create an instance of {@link GetExtensionGroupsResponse.Groups }
     *
     */
    public GetExtensionGroupsResponse.Groups createGetExtensionGroupsResponseGroups() {
        return new GetExtensionGroupsResponse.Groups();
    }

    /**
     * Create an instance of {@link GetAuthCallerIDRechargesRequest }
     *
     */
    public GetAuthCallerIDRechargesRequest createGetAuthCallerIDRechargesRequest() {
        return new GetAuthCallerIDRechargesRequest();
    }

    /**
     * Create an instance of {@link AssignQueueRemoteAgentRequest }
     *
     */
    public AssignQueueRemoteAgentRequest createAssignQueueRemoteAgentRequest() {
        return new AssignQueueRemoteAgentRequest();
    }

    /**
     * Create an instance of {@link SetQueueMemberRequest }
     *
     */
    public SetQueueMemberRequest createSetQueueMemberRequest() {
        return new SetQueueMemberRequest();
    }

    /**
     * Create an instance of {@link GetAuthCallerIDRequest }
     *
     */
    public GetAuthCallerIDRequest createGetAuthCallerIDRequest() {
        return new GetAuthCallerIDRequest();
    }

    /**
     * Create an instance of {@link GetCallRulesInRequest }
     *
     */
    public GetCallRulesInRequest createGetCallRulesInRequest() {
        return new GetCallRulesInRequest();
    }

    /**
     * Create an instance of {@link EditCardCodeRequest }
     *
     */
    public EditCardCodeRequest createEditCardCodeRequest() {
        return new EditCardCodeRequest();
    }

    /**
     * Create an instance of {@link AddAuthCallerIDCreditRequest }
     *
     */
    public AddAuthCallerIDCreditRequest createAddAuthCallerIDCreditRequest() {
        return new AddAuthCallerIDCreditRequest();
    }

    /**
     * Create an instance of {@link AssignQueueAgentRequest }
     *
     */
    public AssignQueueAgentRequest createAssignQueueAgentRequest() {
        return new AssignQueueAgentRequest();
    }

    /**
     * Create an instance of {@link AddExtensionRequest }
     *
     */
    public AddExtensionRequest createAddExtensionRequest() {
        return new AddExtensionRequest();
    }

    /**
     * Create an instance of {@link AddAuthCallerIDRequest }
     *
     */
    public AddAuthCallerIDRequest createAddAuthCallerIDRequest() {
        return new AddAuthCallerIDRequest();
    }

    /**
     * Create an instance of {@link UnassignQueueAgentRequest }
     *
     */
    public UnassignQueueAgentRequest createUnassignQueueAgentRequest() {
        return new UnassignQueueAgentRequest();
    }

    /**
     * Create an instance of {@link GetExtensionGroupsRequest }
     *
     */
    public GetExtensionGroupsRequest createGetExtensionGroupsRequest() {
        return new GetExtensionGroupsRequest();
    }

    /**
     * Create an instance of {@link SetupExtensionRequest.IVR }
     *
     */
    public SetupExtensionRequest.IVR createSetupExtensionRequestIVR() {
        return new SetupExtensionRequest.IVR();
    }

    /**
     * Create an instance of {@link GetCardCodeRequest }
     *
     */
    public GetCardCodeRequest createGetCardCodeRequest() {
        return new GetCardCodeRequest();
    }

    /**
     * Create an instance of {@link SetExtensionStatusRequest }
     *
     */
    public SetExtensionStatusRequest createSetExtensionStatusRequest() {
        return new SetExtensionStatusRequest();
    }

    /**
     * Create an instance of {@link EditCallRulesInRequest.Rule.Transfer }
     *
     */
    public EditCallRulesInRequest.Rule.Transfer createEditCallRulesInRequestRuleTransfer() {
        return new EditCallRulesInRequest.Rule.Transfer();
    }

    /**
     * Create an instance of {@link EditCallRulesInRequest.Rule.Authenticate }
     *
     */
    public EditCallRulesInRequest.Rule.Authenticate createEditCallRulesInRequestRuleAuthenticate() {
        return new EditCallRulesInRequest.Rule.Authenticate();
    }

    /**
     * Create an instance of {@link EditCallRulesInRequest.Rule.SetCallPriority }
     *
     */
    public EditCallRulesInRequest.Rule.SetCallPriority createEditCallRulesInRequestRuleSetCallPriority() {
        return new EditCallRulesInRequest.Rule.SetCallPriority();
    }

    /**
     * Create an instance of {@link EditCallRulesInRequest.Rule.Cascade.ToNumbers }
     *
     */
    public EditCallRulesInRequest.Rule.Cascade.ToNumbers createEditCallRulesInRequestRuleCascadeToNumbers() {
        return new EditCallRulesInRequest.Rule.Cascade.ToNumbers();
    }

    /**
     * Create an instance of {@link AddCallRulesInRequest.Rule.Transfer }
     *
     */
    public AddCallRulesInRequest.Rule.Transfer createAddCallRulesInRequestRuleTransfer() {
        return new AddCallRulesInRequest.Rule.Transfer();
    }

    /**
     * Create an instance of {@link AddCallRulesInRequest.Rule.Authenticate }
     *
     */
    public AddCallRulesInRequest.Rule.Authenticate createAddCallRulesInRequestRuleAuthenticate() {
        return new AddCallRulesInRequest.Rule.Authenticate();
    }

    /**
     * Create an instance of {@link AddCallRulesInRequest.Rule.SetCallPriority }
     *
     */
    public AddCallRulesInRequest.Rule.SetCallPriority createAddCallRulesInRequestRuleSetCallPriority() {
        return new AddCallRulesInRequest.Rule.SetCallPriority();
    }

    /**
     * Create an instance of {@link AddCallRulesInRequest.Rule.Cascade.ToNumbers }
     *
     */
    public AddCallRulesInRequest.Rule.Cascade.ToNumbers createAddCallRulesInRequestRuleCascadeToNumbers() {
        return new AddCallRulesInRequest.Rule.Cascade.ToNumbers();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetExtensionCpAccessResponse")
    public JAXBElement<UpdateObject> createSetExtensionCpAccessResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetExtensionCpAccessResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvisionResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetProvisionFileResponse")
    public JAXBElement<ProvisionResponseType> createGetProvisionFileResponse(ProvisionResponseType value) {
        return new JAXBElement<ProvisionResponseType>(_GetProvisionFileResponse_QNAME, ProvisionResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "DelExtensionResponse")
    public JAXBElement<DelObject> createDelExtensionResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelExtensionResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "AddAuthCallerIDResponse")
    public JAXBElement<UpdateObject> createAddAuthCallerIDResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AddAuthCallerIDResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "DelCallRulesInResponse")
    public JAXBElement<DelObject> createDelCallRulesInResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelCallRulesInResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCallRulesInResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetCallRulesInResponse")
    public JAXBElement<GetCallRulesInResponseType> createGetCallRulesInResponse(GetCallRulesInResponseType value) {
        return new JAXBElement<GetCallRulesInResponseType>(_GetCallRulesInResponse_QNAME, GetCallRulesInResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetupExtensionResponse")
    public JAXBElement<UpdateObject> createSetupExtensionResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetupExtensionResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetExtensionPLResponse")
    public JAXBElement<UpdateObject> createSetExtensionPLResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetExtensionPLResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FaxCenter }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetFaxCenterRequest")
    public JAXBElement<FaxCenter> createSetFaxCenterRequest(FaxCenter value) {
        return new JAXBElement<FaxCenter>(_SetFaxCenterRequest_QNAME, FaxCenter.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCallerIDResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetAuthCallerIDResponse")
    public JAXBElement<GetCallerIDResponseType> createGetAuthCallerIDResponse(GetCallerIDResponseType value) {
        return new JAXBElement<GetCallerIDResponseType>(_GetAuthCallerIDResponse_QNAME, GetCallerIDResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "AddCardCodeCreditResponse")
    public JAXBElement<UpdateObject> createAddCardCodeCreditResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AddCardCodeCreditResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "AssignQueueAgentResponse")
    public JAXBElement<UpdateObject> createAssignQueueAgentResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AssignQueueAgentResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCardCodeResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetCardCodeResponse")
    public JAXBElement<GetCardCodeResponseType> createGetCardCodeResponse(GetCardCodeResponseType value) {
        return new JAXBElement<GetCardCodeResponseType>(_GetCardCodeResponse_QNAME, GetCardCodeResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvisionResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetProvisionResponse")
    public JAXBElement<ProvisionResponseType> createSetProvisionResponse(ProvisionResponseType value) {
        return new JAXBElement<ProvisionResponseType>(_SetProvisionResponse_QNAME, ProvisionResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetExtensionDetailsResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetExtensionDetailsResponse")
    public JAXBElement<GetExtensionDetailsResponseType> createGetExtensionDetailsResponse(GetExtensionDetailsResponseType value) {
        return new JAXBElement<GetExtensionDetailsResponseType>(_GetExtensionDetailsResponse_QNAME, GetExtensionDetailsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetConferenceResponse")
    public JAXBElement<UpdateObject> createSetConferenceResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetConferenceResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetQueueAgentsResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetQueueAgentsResponse")
    public JAXBElement<GetQueueAgentsResponseType> createGetQueueAgentsResponse(GetQueueAgentsResponseType value) {
        return new JAXBElement<GetQueueAgentsResponseType>(_GetQueueAgentsResponse_QNAME, GetQueueAgentsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetExtensionPLResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetExtensionPLResponse")
    public JAXBElement<GetExtensionPLResponseType> createGetExtensionPLResponse(GetExtensionPLResponseType value) {
        return new JAXBElement<GetExtensionPLResponseType>(_GetExtensionPLResponse_QNAME, GetExtensionPLResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetExtensionStatusResponse")
    public JAXBElement<UpdateObject> createSetExtensionStatusResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetExtensionStatusResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetFaxCenterSettingsResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetFaxCenterSettingsResponse")
    public JAXBElement<GetFaxCenterSettingsResponseType> createGetFaxCenterSettingsResponse(GetFaxCenterSettingsResponseType value) {
        return new JAXBElement<GetFaxCenterSettingsResponseType>(_GetFaxCenterSettingsResponse_QNAME, GetFaxCenterSettingsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "UnassignQueueAgentResponse")
    public JAXBElement<UpdateObject> createUnassignQueueAgentResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_UnassignQueueAgentResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "AssignQueueRemoteAgentResponse")
    public JAXBElement<UpdateObject> createAssignQueueRemoteAgentResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AssignQueueRemoteAgentResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "AddAuthCallerIDCreditResponse")
    public JAXBElement<UpdateObject> createAddAuthCallerIDCreditResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AddAuthCallerIDCreditResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "AddCardCodeResponse")
    public JAXBElement<UpdateObject> createAddCardCodeResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AddCardCodeResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAvailableCallerIDResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetAvailableCallerIDResponse")
    public JAXBElement<GetAvailableCallerIDResponseType> createGetAvailableCallerIDResponse(GetAvailableCallerIDResponseType value) {
        return new JAXBElement<GetAvailableCallerIDResponseType>(_GetAvailableCallerIDResponse_QNAME, GetAvailableCallerIDResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetExtensionResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetExtensionsResponse")
    public JAXBElement<GetExtensionResponseType> createGetExtensionsResponse(GetExtensionResponseType value) {
        return new JAXBElement<GetExtensionResponseType>(_GetExtensionsResponse_QNAME, GetExtensionResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetCallRecordingResponse")
    public JAXBElement<UpdateObject> createSetCallRecordingResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetCallRecordingResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "EditAuthCallerIDResponse")
    public JAXBElement<UpdateObject> createEditAuthCallerIDResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_EditAuthCallerIDResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCallRecordingSettingsResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetCallRecordingSettingsResponse")
    public JAXBElement<GetCallRecordingSettingsResponseType> createGetCallRecordingSettingsResponse(GetCallRecordingSettingsResponseType value) {
        return new JAXBElement<GetCallRecordingSettingsResponseType>(_GetCallRecordingSettingsResponse_QNAME, GetCallRecordingSettingsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "AddCallRulesInResponse")
    public JAXBElement<UpdateObject> createAddCallRulesInResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AddCallRulesInResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "DelCardCodeResponse")
    public JAXBElement<DelObject> createDelCardCodeResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelCardCodeResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvisioningInfo }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetProvisionRequest")
    public JAXBElement<ProvisioningInfo> createSetProvisionRequest(ProvisioningInfo value) {
        return new JAXBElement<ProvisioningInfo>(_SetProvisionRequest_QNAME, ProvisioningInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetConferenceSettingsResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetConferenceSettingsResponse")
    public JAXBElement<GetConferenceSettingsResponseType> createGetConferenceSettingsResponse(GetConferenceSettingsResponseType value) {
        return new JAXBElement<GetConferenceSettingsResponseType>(_GetConferenceSettingsResponse_QNAME, GetConferenceSettingsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetVoicemailSettingsResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetVoicemailSettingsResponse")
    public JAXBElement<GetVoicemailSettingsResponseType> createGetVoicemailSettingsResponse(GetVoicemailSettingsResponseType value) {
        return new JAXBElement<GetVoicemailSettingsResponseType>(_GetVoicemailSettingsResponse_QNAME, GetVoicemailSettingsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetQueueRemoteAgentResponse")
    public JAXBElement<UpdateObject> createSetQueueRemoteAgentResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetQueueRemoteAgentResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "DelAuthCallerIDResponse")
    public JAXBElement<DelObject> createDelAuthCallerIDResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelAuthCallerIDResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "EditCardCodeResponse")
    public JAXBElement<UpdateObject> createEditCardCodeResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_EditCardCodeResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAuthCallerIDCreditResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetAuthCallerIDRechargesResponse")
    public JAXBElement<GetAuthCallerIDCreditResponseType> createGetAuthCallerIDRechargesResponse(GetAuthCallerIDCreditResponseType value) {
        return new JAXBElement<GetAuthCallerIDCreditResponseType>(_GetAuthCallerIDRechargesResponse_QNAME, GetAuthCallerIDCreditResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetVoicemailResponse")
    public JAXBElement<UpdateObject> createSetVoicemailResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetVoicemailResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetQueueMemberResponse")
    public JAXBElement<UpdateObject> createSetQueueMemberResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetQueueMemberResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetQueueMembershipResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "GetQueueMembershipResponse")
    public JAXBElement<GetQueueMembershipResponseType> createGetQueueMembershipResponse(GetQueueMembershipResponseType value) {
        return new JAXBElement<GetQueueMembershipResponseType>(_GetQueueMembershipResponse_QNAME, GetQueueMembershipResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetFaxCenterResponse")
    public JAXBElement<UpdateObject> createSetFaxCenterResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetFaxCenterResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "EditCallRulesInResponse")
    public JAXBElement<UpdateObject> createEditCallRulesInResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_EditCallRulesInResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ExtensionMessages.xsd/2.5.1", name = "SetQueueAgentResponse")
    public JAXBElement<UpdateObject> createSetQueueAgentResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetQueueAgentResponse_QNAME, UpdateObject.class, null, value);
    }

}
