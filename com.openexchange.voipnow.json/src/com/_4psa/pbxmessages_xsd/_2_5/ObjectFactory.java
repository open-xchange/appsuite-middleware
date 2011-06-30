
package com._4psa.pbxmessages_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com._4psa.common_xsd._2_5.DelObject;
import com._4psa.pbxmessagesinfo_xsd._2_5.EnrollmentResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetAdvertisingTemplatesResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetCustomAlertsResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetCustomButtonsResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetDevicesResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetEquipmentListResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetFileLanguagesResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetFoldersResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetIndustriesResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetInterfaceLangResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetPhoneLangResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetSchemaVersionsResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetSoundsResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetTemplatesResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetTimeIntervalBlocksResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.GetTimeIntervalsResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.PingResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.UpdateObjectResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.UpgradeHistoryResponseType;
import com._4psa.pbxmessagesinfo_xsd._2_5.UpgradeVoipNowResponseType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com._4psa.pbxmessages_xsd._2_5 package. 
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

    private final static QName _UpgradeVoipNowRequest_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "UpgradeVoipNowRequest");
    private final static QName _DelCustomAlertResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "DelCustomAlertResponse");
    private final static QName _GetTimeIntervalBlocksResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetTimeIntervalBlocksResponse");
    private final static QName _GetOwnedSoundsResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetOwnedSoundsResponse");
    private final static QName _AddTimeIntervalBlockResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "AddTimeIntervalBlockResponse");
    private final static QName _EnrollResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "EnrollResponse");
    private final static QName _GetSharedSoundsResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetSharedSoundsResponse");
    private final static QName _DelTimeIntervalResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "DelTimeIntervalResponse");
    private final static QName _GetTemplatesResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetTemplatesResponse");
    private final static QName _GetInterfaceLangRequest_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetInterfaceLangRequest");
    private final static QName _GetEquipmentListRequest_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetEquipmentListRequest");
    private final static QName _UpdateLicenseResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "UpdateLicenseResponse");
    private final static QName _GetAdvertisingTemplatesResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetAdvertisingTemplatesResponse");
    private final static QName _UpgradeHistoryRequest_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "UpgradeHistoryRequest");
    private final static QName _GetPhoneLangResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetPhoneLangResponse");
    private final static QName _PingResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "PingResponse");
    private final static QName _GetSchemaVersionsRequest_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetSchemaVersionsRequest");
    private final static QName _GetFoldersResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetFoldersResponse");
    private final static QName _EditTimeIntervalResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "EditTimeIntervalResponse");
    private final static QName _DelDeviceResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "DelDeviceResponse");
    private final static QName _GetIndustriesResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetIndustriesResponse");
    private final static QName _AddTimeIntervalResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "AddTimeIntervalResponse");
    private final static QName _GetTimeIntervalsResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetTimeIntervalsResponse");
    private final static QName _DelTimeIntervalBlockResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "DelTimeIntervalBlockResponse");
    private final static QName _GetCustomButtonsResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetCustomButtonsResponse");
    private final static QName _GetSchemaVersionsResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetSchemaVersionsResponse");
    private final static QName _GetFileLanguagesResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetFileLanguagesResponse");
    private final static QName _GetEquipmentListResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetEquipmentListResponse");
    private final static QName _GetInterfaceLangResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetInterfaceLangResponse");
    private final static QName _GetPhoneLangRequest_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetPhoneLangRequest");
    private final static QName _EditTimeIntervalBlockResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "EditTimeIntervalBlockResponse");
    private final static QName _PingRequest_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "PingRequest");
    private final static QName _UpgradeHistoryResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "UpgradeHistoryResponse");
    private final static QName _UpgradeVoipNowResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "UpgradeVoipNowResponse");
    private final static QName _GetCustomAlertsResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetCustomAlertsResponse");
    private final static QName _GetDevicesResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "GetDevicesResponse");
    private final static QName _DelCustomButtonResponse_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "DelCustomButtonResponse");
    private final static QName _EnrollRequestParamName_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "name");
    private final static QName _EnrollRequestParamValue_QNAME = new QName("http://4psa.com/PBXMessages.xsd/2.5.1", "value");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.pbxmessages_xsd._2_5
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetCallAPISettingsResponse }
     * 
     */
    public GetCallAPISettingsResponse createGetCallAPISettingsResponse() {
        return new GetCallAPISettingsResponse();
    }

    /**
     * Create an instance of {@link GetTimezoneResponse }
     * 
     */
    public GetTimezoneResponse createGetTimezoneResponse() {
        return new GetTimezoneResponse();
    }

    /**
     * Create an instance of {@link GetSystemAPISettingsResponse }
     * 
     */
    public GetSystemAPISettingsResponse createGetSystemAPISettingsResponse() {
        return new GetSystemAPISettingsResponse();
    }

    /**
     * Create an instance of {@link GetRegionsResponse }
     * 
     */
    public GetRegionsResponse createGetRegionsResponse() {
        return new GetRegionsResponse();
    }

    /**
     * Create an instance of {@link EnrollRequest }
     * 
     */
    public EnrollRequest createEnrollRequest() {
        return new EnrollRequest();
    }

    /**
     * Create an instance of {@link GetCallAPISettingsResponse.Access }
     * 
     */
    public GetCallAPISettingsResponse.Access createGetCallAPISettingsResponseAccess() {
        return new GetCallAPISettingsResponse.Access();
    }

    /**
     * Create an instance of {@link GetCallAPISettingsResponse.Auth }
     * 
     */
    public GetCallAPISettingsResponse.Auth createGetCallAPISettingsResponseAuth() {
        return new GetCallAPISettingsResponse.Auth();
    }

    /**
     * Create an instance of {@link GetTemplatesRequest }
     * 
     */
    public GetTemplatesRequest createGetTemplatesRequest() {
        return new GetTemplatesRequest();
    }

    /**
     * Create an instance of {@link AddTimeIntervalBlockRequest }
     * 
     */
    public AddTimeIntervalBlockRequest createAddTimeIntervalBlockRequest() {
        return new AddTimeIntervalBlockRequest();
    }

    /**
     * Create an instance of {@link AddCustomAlertResponse }
     * 
     */
    public AddCustomAlertResponse createAddCustomAlertResponse() {
        return new AddCustomAlertResponse();
    }

    /**
     * Create an instance of {@link SetCustomButtonStatusRequest }
     * 
     */
    public SetCustomButtonStatusRequest createSetCustomButtonStatusRequest() {
        return new SetCustomButtonStatusRequest();
    }

    /**
     * Create an instance of {@link AddDeviceResponse }
     * 
     */
    public AddDeviceResponse createAddDeviceResponse() {
        return new AddDeviceResponse();
    }

    /**
     * Create an instance of {@link AddCustomAlertRequest }
     * 
     */
    public AddCustomAlertRequest createAddCustomAlertRequest() {
        return new AddCustomAlertRequest();
    }

    /**
     * Create an instance of {@link EditIndustryRequest }
     * 
     */
    public EditIndustryRequest createEditIndustryRequest() {
        return new EditIndustryRequest();
    }

    /**
     * Create an instance of {@link AddTimeIntervalRequest }
     * 
     */
    public AddTimeIntervalRequest createAddTimeIntervalRequest() {
        return new AddTimeIntervalRequest();
    }

    /**
     * Create an instance of {@link GetFoldersRequest }
     * 
     */
    public GetFoldersRequest createGetFoldersRequest() {
        return new GetFoldersRequest();
    }

    /**
     * Create an instance of {@link WithdrawRequest }
     * 
     */
    public WithdrawRequest createWithdrawRequest() {
        return new WithdrawRequest();
    }

    /**
     * Create an instance of {@link GetSharedSoundsRequest }
     * 
     */
    public GetSharedSoundsRequest createGetSharedSoundsRequest() {
        return new GetSharedSoundsRequest();
    }

    /**
     * Create an instance of {@link GetTimezoneResponse.Timezone }
     * 
     */
    public GetTimezoneResponse.Timezone createGetTimezoneResponseTimezone() {
        return new GetTimezoneResponse.Timezone();
    }

    /**
     * Create an instance of {@link EditTimeIntervalBlockRequest }
     * 
     */
    public EditTimeIntervalBlockRequest createEditTimeIntervalBlockRequest() {
        return new EditTimeIntervalBlockRequest();
    }

    /**
     * Create an instance of {@link GetTimeIntervalsRequest }
     * 
     */
    public GetTimeIntervalsRequest createGetTimeIntervalsRequest() {
        return new GetTimeIntervalsRequest();
    }

    /**
     * Create an instance of {@link AddIndustryRequest }
     * 
     */
    public AddIndustryRequest createAddIndustryRequest() {
        return new AddIndustryRequest();
    }

    /**
     * Create an instance of {@link GetCustomAlertsRequest }
     * 
     */
    public GetCustomAlertsRequest createGetCustomAlertsRequest() {
        return new GetCustomAlertsRequest();
    }

    /**
     * Create an instance of {@link GetRegionsRequest }
     * 
     */
    public GetRegionsRequest createGetRegionsRequest() {
        return new GetRegionsRequest();
    }

    /**
     * Create an instance of {@link AddCustomButtonResponse }
     * 
     */
    public AddCustomButtonResponse createAddCustomButtonResponse() {
        return new AddCustomButtonResponse();
    }

    /**
     * Create an instance of {@link GetSystemAPISettingsResponse.Access }
     * 
     */
    public GetSystemAPISettingsResponse.Access createGetSystemAPISettingsResponseAccess() {
        return new GetSystemAPISettingsResponse.Access();
    }

    /**
     * Create an instance of {@link EditCustomAlertRequest }
     * 
     */
    public EditCustomAlertRequest createEditCustomAlertRequest() {
        return new EditCustomAlertRequest();
    }

    /**
     * Create an instance of {@link GetCustomButtonsRequest }
     * 
     */
    public GetCustomButtonsRequest createGetCustomButtonsRequest() {
        return new GetCustomButtonsRequest();
    }

    /**
     * Create an instance of {@link EditCustomAlertResponse }
     * 
     */
    public EditCustomAlertResponse createEditCustomAlertResponse() {
        return new EditCustomAlertResponse();
    }

    /**
     * Create an instance of {@link GetFileLanguagesRequest }
     * 
     */
    public GetFileLanguagesRequest createGetFileLanguagesRequest() {
        return new GetFileLanguagesRequest();
    }

    /**
     * Create an instance of {@link WithdrawResponse }
     * 
     */
    public WithdrawResponse createWithdrawResponse() {
        return new WithdrawResponse();
    }

    /**
     * Create an instance of {@link EditDeviceResponse }
     * 
     */
    public EditDeviceResponse createEditDeviceResponse() {
        return new EditDeviceResponse();
    }

    /**
     * Create an instance of {@link UpdateLicenseRequest }
     * 
     */
    public UpdateLicenseRequest createUpdateLicenseRequest() {
        return new UpdateLicenseRequest();
    }

    /**
     * Create an instance of {@link GetRegionsResponse.Region }
     * 
     */
    public GetRegionsResponse.Region createGetRegionsResponseRegion() {
        return new GetRegionsResponse.Region();
    }

    /**
     * Create an instance of {@link EditDeviceRequest }
     * 
     */
    public EditDeviceRequest createEditDeviceRequest() {
        return new EditDeviceRequest();
    }

    /**
     * Create an instance of {@link DelTimeIntervalRequest }
     * 
     */
    public DelTimeIntervalRequest createDelTimeIntervalRequest() {
        return new DelTimeIntervalRequest();
    }

    /**
     * Create an instance of {@link EditIndustryResponse }
     * 
     */
    public EditIndustryResponse createEditIndustryResponse() {
        return new EditIndustryResponse();
    }

    /**
     * Create an instance of {@link DelIndustriesRequest }
     * 
     */
    public DelIndustriesRequest createDelIndustriesRequest() {
        return new DelIndustriesRequest();
    }

    /**
     * Create an instance of {@link SetCustomButtonStatusResponse }
     * 
     */
    public SetCustomButtonStatusResponse createSetCustomButtonStatusResponse() {
        return new SetCustomButtonStatusResponse();
    }

    /**
     * Create an instance of {@link SetCustomAlertStatusRequest }
     * 
     */
    public SetCustomAlertStatusRequest createSetCustomAlertStatusRequest() {
        return new SetCustomAlertStatusRequest();
    }

    /**
     * Create an instance of {@link GetTimeIntervalBlocksRequest }
     * 
     */
    public GetTimeIntervalBlocksRequest createGetTimeIntervalBlocksRequest() {
        return new GetTimeIntervalBlocksRequest();
    }

    /**
     * Create an instance of {@link DelCustomButtonRequest }
     * 
     */
    public DelCustomButtonRequest createDelCustomButtonRequest() {
        return new DelCustomButtonRequest();
    }

    /**
     * Create an instance of {@link SetCustomAlertStatusResponse }
     * 
     */
    public SetCustomAlertStatusResponse createSetCustomAlertStatusResponse() {
        return new SetCustomAlertStatusResponse();
    }

    /**
     * Create an instance of {@link DelIndustriesResponse }
     * 
     */
    public DelIndustriesResponse createDelIndustriesResponse() {
        return new DelIndustriesResponse();
    }

    /**
     * Create an instance of {@link GetTimezoneRequest }
     * 
     */
    public GetTimezoneRequest createGetTimezoneRequest() {
        return new GetTimezoneRequest();
    }

    /**
     * Create an instance of {@link DelTimeIntervalBlockRequest }
     * 
     */
    public DelTimeIntervalBlockRequest createDelTimeIntervalBlockRequest() {
        return new DelTimeIntervalBlockRequest();
    }

    /**
     * Create an instance of {@link EditCustomButtonRequest }
     * 
     */
    public EditCustomButtonRequest createEditCustomButtonRequest() {
        return new EditCustomButtonRequest();
    }

    /**
     * Create an instance of {@link AddCustomButtonRequest }
     * 
     */
    public AddCustomButtonRequest createAddCustomButtonRequest() {
        return new AddCustomButtonRequest();
    }

    /**
     * Create an instance of {@link GetIndustriesRequest }
     * 
     */
    public GetIndustriesRequest createGetIndustriesRequest() {
        return new GetIndustriesRequest();
    }

    /**
     * Create an instance of {@link GetAdvertisingTemplatesRequest }
     * 
     */
    public GetAdvertisingTemplatesRequest createGetAdvertisingTemplatesRequest() {
        return new GetAdvertisingTemplatesRequest();
    }

    /**
     * Create an instance of {@link AddIndustryResponse }
     * 
     */
    public AddIndustryResponse createAddIndustryResponse() {
        return new AddIndustryResponse();
    }

    /**
     * Create an instance of {@link AddDeviceRequest }
     * 
     */
    public AddDeviceRequest createAddDeviceRequest() {
        return new AddDeviceRequest();
    }

    /**
     * Create an instance of {@link EditTimeIntervalRequest }
     * 
     */
    public EditTimeIntervalRequest createEditTimeIntervalRequest() {
        return new EditTimeIntervalRequest();
    }

    /**
     * Create an instance of {@link GetDevicesRequest }
     * 
     */
    public GetDevicesRequest createGetDevicesRequest() {
        return new GetDevicesRequest();
    }

    /**
     * Create an instance of {@link EnrollRequest.Param }
     * 
     */
    public EnrollRequest.Param createEnrollRequestParam() {
        return new EnrollRequest.Param();
    }

    /**
     * Create an instance of {@link DelDeviceRequest }
     * 
     */
    public DelDeviceRequest createDelDeviceRequest() {
        return new DelDeviceRequest();
    }

    /**
     * Create an instance of {@link GetSystemAPISettingsRequest }
     * 
     */
    public GetSystemAPISettingsRequest createGetSystemAPISettingsRequest() {
        return new GetSystemAPISettingsRequest();
    }

    /**
     * Create an instance of {@link DelCustomAlertRequest }
     * 
     */
    public DelCustomAlertRequest createDelCustomAlertRequest() {
        return new DelCustomAlertRequest();
    }

    /**
     * Create an instance of {@link GetOwnedSoundsRequest }
     * 
     */
    public GetOwnedSoundsRequest createGetOwnedSoundsRequest() {
        return new GetOwnedSoundsRequest();
    }

    /**
     * Create an instance of {@link EditCustomButtonResponse }
     * 
     */
    public EditCustomButtonResponse createEditCustomButtonResponse() {
        return new EditCustomButtonResponse();
    }

    /**
     * Create an instance of {@link GetCallAPISettingsRequest }
     * 
     */
    public GetCallAPISettingsRequest createGetCallAPISettingsRequest() {
        return new GetCallAPISettingsRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "UpgradeVoipNowRequest")
    public JAXBElement<String> createUpgradeVoipNowRequest(String value) {
        return new JAXBElement<String>(_UpgradeVoipNowRequest_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "DelCustomAlertResponse")
    public JAXBElement<DelObject> createDelCustomAlertResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelCustomAlertResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTimeIntervalBlocksResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetTimeIntervalBlocksResponse")
    public JAXBElement<GetTimeIntervalBlocksResponseType> createGetTimeIntervalBlocksResponse(GetTimeIntervalBlocksResponseType value) {
        return new JAXBElement<GetTimeIntervalBlocksResponseType>(_GetTimeIntervalBlocksResponse_QNAME, GetTimeIntervalBlocksResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSoundsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetOwnedSoundsResponse")
    public JAXBElement<GetSoundsResponseType> createGetOwnedSoundsResponse(GetSoundsResponseType value) {
        return new JAXBElement<GetSoundsResponseType>(_GetOwnedSoundsResponse_QNAME, GetSoundsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObjectResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "AddTimeIntervalBlockResponse")
    public JAXBElement<UpdateObjectResponseType> createAddTimeIntervalBlockResponse(UpdateObjectResponseType value) {
        return new JAXBElement<UpdateObjectResponseType>(_AddTimeIntervalBlockResponse_QNAME, UpdateObjectResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnrollmentResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "EnrollResponse")
    public JAXBElement<EnrollmentResponseType> createEnrollResponse(EnrollmentResponseType value) {
        return new JAXBElement<EnrollmentResponseType>(_EnrollResponse_QNAME, EnrollmentResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSoundsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetSharedSoundsResponse")
    public JAXBElement<GetSoundsResponseType> createGetSharedSoundsResponse(GetSoundsResponseType value) {
        return new JAXBElement<GetSoundsResponseType>(_GetSharedSoundsResponse_QNAME, GetSoundsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "DelTimeIntervalResponse")
    public JAXBElement<DelObject> createDelTimeIntervalResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelTimeIntervalResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTemplatesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetTemplatesResponse")
    public JAXBElement<GetTemplatesResponseType> createGetTemplatesResponse(GetTemplatesResponseType value) {
        return new JAXBElement<GetTemplatesResponseType>(_GetTemplatesResponse_QNAME, GetTemplatesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetInterfaceLangRequest")
    public JAXBElement<String> createGetInterfaceLangRequest(String value) {
        return new JAXBElement<String>(_GetInterfaceLangRequest_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetEquipmentListRequest")
    public JAXBElement<String> createGetEquipmentListRequest(String value) {
        return new JAXBElement<String>(_GetEquipmentListRequest_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObjectResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "UpdateLicenseResponse")
    public JAXBElement<UpdateObjectResponseType> createUpdateLicenseResponse(UpdateObjectResponseType value) {
        return new JAXBElement<UpdateObjectResponseType>(_UpdateLicenseResponse_QNAME, UpdateObjectResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAdvertisingTemplatesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetAdvertisingTemplatesResponse")
    public JAXBElement<GetAdvertisingTemplatesResponseType> createGetAdvertisingTemplatesResponse(GetAdvertisingTemplatesResponseType value) {
        return new JAXBElement<GetAdvertisingTemplatesResponseType>(_GetAdvertisingTemplatesResponse_QNAME, GetAdvertisingTemplatesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "UpgradeHistoryRequest")
    public JAXBElement<String> createUpgradeHistoryRequest(String value) {
        return new JAXBElement<String>(_UpgradeHistoryRequest_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPhoneLangResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetPhoneLangResponse")
    public JAXBElement<GetPhoneLangResponseType> createGetPhoneLangResponse(GetPhoneLangResponseType value) {
        return new JAXBElement<GetPhoneLangResponseType>(_GetPhoneLangResponse_QNAME, GetPhoneLangResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PingResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "PingResponse")
    public JAXBElement<PingResponseType> createPingResponse(PingResponseType value) {
        return new JAXBElement<PingResponseType>(_PingResponse_QNAME, PingResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetSchemaVersionsRequest")
    public JAXBElement<String> createGetSchemaVersionsRequest(String value) {
        return new JAXBElement<String>(_GetSchemaVersionsRequest_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetFoldersResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetFoldersResponse")
    public JAXBElement<GetFoldersResponseType> createGetFoldersResponse(GetFoldersResponseType value) {
        return new JAXBElement<GetFoldersResponseType>(_GetFoldersResponse_QNAME, GetFoldersResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObjectResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "EditTimeIntervalResponse")
    public JAXBElement<UpdateObjectResponseType> createEditTimeIntervalResponse(UpdateObjectResponseType value) {
        return new JAXBElement<UpdateObjectResponseType>(_EditTimeIntervalResponse_QNAME, UpdateObjectResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "DelDeviceResponse")
    public JAXBElement<DelObject> createDelDeviceResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelDeviceResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetIndustriesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetIndustriesResponse")
    public JAXBElement<GetIndustriesResponseType> createGetIndustriesResponse(GetIndustriesResponseType value) {
        return new JAXBElement<GetIndustriesResponseType>(_GetIndustriesResponse_QNAME, GetIndustriesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObjectResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "AddTimeIntervalResponse")
    public JAXBElement<UpdateObjectResponseType> createAddTimeIntervalResponse(UpdateObjectResponseType value) {
        return new JAXBElement<UpdateObjectResponseType>(_AddTimeIntervalResponse_QNAME, UpdateObjectResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTimeIntervalsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetTimeIntervalsResponse")
    public JAXBElement<GetTimeIntervalsResponseType> createGetTimeIntervalsResponse(GetTimeIntervalsResponseType value) {
        return new JAXBElement<GetTimeIntervalsResponseType>(_GetTimeIntervalsResponse_QNAME, GetTimeIntervalsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "DelTimeIntervalBlockResponse")
    public JAXBElement<DelObject> createDelTimeIntervalBlockResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelTimeIntervalBlockResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomButtonsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetCustomButtonsResponse")
    public JAXBElement<GetCustomButtonsResponseType> createGetCustomButtonsResponse(GetCustomButtonsResponseType value) {
        return new JAXBElement<GetCustomButtonsResponseType>(_GetCustomButtonsResponse_QNAME, GetCustomButtonsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSchemaVersionsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetSchemaVersionsResponse")
    public JAXBElement<GetSchemaVersionsResponseType> createGetSchemaVersionsResponse(GetSchemaVersionsResponseType value) {
        return new JAXBElement<GetSchemaVersionsResponseType>(_GetSchemaVersionsResponse_QNAME, GetSchemaVersionsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetFileLanguagesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetFileLanguagesResponse")
    public JAXBElement<GetFileLanguagesResponseType> createGetFileLanguagesResponse(GetFileLanguagesResponseType value) {
        return new JAXBElement<GetFileLanguagesResponseType>(_GetFileLanguagesResponse_QNAME, GetFileLanguagesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEquipmentListResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetEquipmentListResponse")
    public JAXBElement<GetEquipmentListResponseType> createGetEquipmentListResponse(GetEquipmentListResponseType value) {
        return new JAXBElement<GetEquipmentListResponseType>(_GetEquipmentListResponse_QNAME, GetEquipmentListResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetInterfaceLangResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetInterfaceLangResponse")
    public JAXBElement<GetInterfaceLangResponseType> createGetInterfaceLangResponse(GetInterfaceLangResponseType value) {
        return new JAXBElement<GetInterfaceLangResponseType>(_GetInterfaceLangResponse_QNAME, GetInterfaceLangResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetPhoneLangRequest")
    public JAXBElement<String> createGetPhoneLangRequest(String value) {
        return new JAXBElement<String>(_GetPhoneLangRequest_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObjectResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "EditTimeIntervalBlockResponse")
    public JAXBElement<UpdateObjectResponseType> createEditTimeIntervalBlockResponse(UpdateObjectResponseType value) {
        return new JAXBElement<UpdateObjectResponseType>(_EditTimeIntervalBlockResponse_QNAME, UpdateObjectResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "PingRequest")
    public JAXBElement<String> createPingRequest(String value) {
        return new JAXBElement<String>(_PingRequest_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpgradeHistoryResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "UpgradeHistoryResponse")
    public JAXBElement<UpgradeHistoryResponseType> createUpgradeHistoryResponse(UpgradeHistoryResponseType value) {
        return new JAXBElement<UpgradeHistoryResponseType>(_UpgradeHistoryResponse_QNAME, UpgradeHistoryResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpgradeVoipNowResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "UpgradeVoipNowResponse")
    public JAXBElement<UpgradeVoipNowResponseType> createUpgradeVoipNowResponse(UpgradeVoipNowResponseType value) {
        return new JAXBElement<UpgradeVoipNowResponseType>(_UpgradeVoipNowResponse_QNAME, UpgradeVoipNowResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomAlertsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetCustomAlertsResponse")
    public JAXBElement<GetCustomAlertsResponseType> createGetCustomAlertsResponse(GetCustomAlertsResponseType value) {
        return new JAXBElement<GetCustomAlertsResponseType>(_GetCustomAlertsResponse_QNAME, GetCustomAlertsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDevicesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "GetDevicesResponse")
    public JAXBElement<GetDevicesResponseType> createGetDevicesResponse(GetDevicesResponseType value) {
        return new JAXBElement<GetDevicesResponseType>(_GetDevicesResponse_QNAME, GetDevicesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "DelCustomButtonResponse")
    public JAXBElement<DelObject> createDelCustomButtonResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelCustomButtonResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "name", scope = EnrollRequest.Param.class)
    public JAXBElement<String> createEnrollRequestParamName(String value) {
        return new JAXBElement<String>(_EnrollRequestParamName_QNAME, String.class, EnrollRequest.Param.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", name = "value", scope = EnrollRequest.Param.class)
    public JAXBElement<String> createEnrollRequestParamValue(String value) {
        return new JAXBElement<String>(_EnrollRequestParamValue_QNAME, String.class, EnrollRequest.Param.class, value);
    }

}
