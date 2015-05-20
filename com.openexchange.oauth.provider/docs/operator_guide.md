Open-Xchange OAuth 2.0 Provider Operator Guide
==============================================

With OX App Suite 7.8.0 a service provider can decide to publish a certain subset of the OX HTTP API via OAuth 2.0. See the [developer guide](developer_guide.html) for an overview of the available APIs. The feature as a whole is contained in separate optional packages and requires some configuration. Supported client applications must be of type `confidential` according to the `web application` profile defined in [RFC 6749](http://tools.ietf.org/html/rfc6749). Every application must be registered at the OX backend. The registration process is up to you, while the backend provides SOAP and RMI interfaces to persist those registrations and generates the client-specific credentials that are needed to gain access for granting users.


Installation And Configuration
------------------------------

The OAuth provider feature is separated into two packages `open-xchange-oauth-provider` and `open-xchange-admin-oauth-provider`. The former one needs to be installed on every groupware node, the latter one provides the client provisioning interfaces and may be installed on your dedicated provisioning nodes. On a Debian setup you would install those packages like so:

    $ apt-get install open-xchange-oauth-provider open-xchange-admin-oauth-provider

Configuration takes place in `/opt/open-xchange/etc/oauth-provider.properties`. There you must enable the feature and provide an encryption key. The encryption key is used to encrypt the credentials of client applications within the database. You MUST set a value here and this value must be the same on all groupware and provisioning nodes. Example:

    # Set to 'true' to basically enable the OAuth 2.0 provider. This setting can then be overridden
    # via config cascade to disallow granting access for certain users. If the provider is enabled,
    # an encryption key (see below) must be set!
    #
    # Default: false
    com.openexchange.oauth.provider.enabled=true

    # Specify how authorization codes shall be stored, to enable OAuth in multi-node environments.
    # Options are Hazelcast ('hz') or database ('db').
    #
    # Default: hz
    com.openexchange.oauth.provider.authcode.type=hz

    # The key to encrypt client secrets that are stored within the database.
    # A value must be set to enable the registration of OAuth 2.0 client
    # applications. It must be the same on every node. After the first client
    # has been registered, the key must not be changed anymore.
    # Default: <empty>
    com.openexchange.oauth.provider.encryptionKey=yen8oT0vohNgoo9mohfai3aitho6eaQu7cieFohsoamooS3IeJeukoov4niechoh

You may to decide how authorization codes are stored. Those codes are short-living one-time tokens that are generated when a user grants access. The client application will then exchange this code for a pair of access and refresh tokens. We store those codes in the hazelcast data grid per default. However you can choose to store them within the database. If using hazelcast, you can also adjust the parameters for the according distributed data structure in `/opt/open-xchange/etc/hazelcast/authcode.properties`.


Client Provisioning
-------------------

For every client application that you want to allow to access the OAuth APIs you need to persist some data. During the registration call a client ID and a secret are generated, which must be provided to the client developers.

**Important:** A client does always belong to a context group and is stored within the global DB. It can therefore only handle users of the according contexts. See https://intranet.open-xchange.com/wiki/backend-team:globaldb for details. As a result you need to pass a context group name to some of the provisioning calls unless a client ID is required. After registering a client, the context group identifier is encoded within the client ID.

The registration data consists of the following parameters:

<table>
  <thead>
    <tr>
      <th>Parameter</th>
      <th>Description</th>
      <th>Required</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>name</td>
      <td>The name of the client application. Will be visible to your users.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>description</td>
      <td>A description of the client application. Will be visible to your users. Translations of the description are currently not supported, you must decide for one language.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>contact address</td>
      <td>E-Mail address to contact the application vendor.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>website</td>
      <td>An URL to the client applications website.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>default scope</td>
      <td>A default scope that will be applied if the client application asks a user for access without providing a certain scope during the request. See the developer guide for available scope tokens. The scope is always a space-delimited string cosisting of one or more scope tokens, e.g. "read_contacts write_contacts".</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>icon</td>
      <td>An icon of the client application. Supported image types are <code>image/png</code>, <code>image/jpg</code> and <code>image/jpeg</code>. Icons SHOULD be of size 128x128 px, otherwise they might not get displayed correctly. The max. image size is 256kb.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>redirect URIs</td>
      <td>One or more URIs that will be used as redirect locations to deliver authorization codes or error messages back to the client application. Every URI must be absolute and not contain a fragment. The scheme must always be <code>https</code>, however for development purposes redirect URIs pointing to <code>localhost</code>, <code>127.0.0.1</code> or <code>[::1]</code> are also allowed with <code>http</code> as scheme.</td>
      <td>Yes</td>
    </tr>
  </tbody>
</table>


###RMI Provisioning###

The according remote interface is `com.openexchange.oauth.provider.rmi.RemoteClientManagement`. All related classes are annotated with JavaDoc. Below you find an example of all operations that manipulate client data. Of course there are also methods to list and get all or certain registered clients.

    package me.coolhosting.ox.oauth;

    import java.io.ByteArrayOutputStream;
    import java.io.FileInputStream;
    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.net.MalformedURLException;
    import java.rmi.Naming;
    import java.rmi.NotBoundException;
    import java.rmi.RemoteException;
    import java.util.ArrayList;
    import java.util.List;
    import com.openexchange.admin.rmi.dataobjects.Credentials;
    import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
    import com.openexchange.oauth.provider.rmi.client.ClientDto;
    import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
    import com.openexchange.oauth.provider.rmi.client.RemoteClientManagementException;
    import com.openexchange.oauth.provider.rmi.client.IconDto;
    import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;

    public class ClientProvisioningRoundtrip {

        public static void main(String[] args) {
            try {
                // Lookup remote
                RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://coolhosting.me:1099/" + RemoteClientManagement.RMI_NAME);
                // All method calls require the master credentials.
                Credentials credentials = new Credentials("oxadminmaster", "secret");

                ClientDataDto clientData = prepareClientData();
                ClientDto client = clientManagement.registerClient(RemoteClientManagement.DEFAULT_GID, clientData, credentials); // use default context group
                System.out.println("Client '" + client.getName() + "' was successfully registered: [ID: " + client.getId() + ", secret: " + client.getSecret() + "]");

                // You can disable clients temporarily. API access is then prohibited.
                if (clientManagement.disableClient(client.getId(), credentials)) {
                    System.out.println("Client '" + client.getName() + "' was disabled");
                }

                // Of course enabling disabled clients is also possible.
                if (clientManagement.enableClient(client.getId(), credentials)) {
                    System.out.println("Client '" + client.getName() + "' was enabled again");
                }

                // You can revoke a clients secret. For security reasons all existing grants are invalidated then.
                client = clientManagement.revokeClientSecret(client.getId(), credentials);
                System.out.println("Client '" + client.getName() + "' was assigned a new secret: " + client.getSecret());

                // Of course you can update the client data. Every field set within ClientData will be overridden.
                // Fields that are not set will not be modified. Scope and redirect URIs must always be submitted
                // in total, no merging will be applied here.
                clientData = new ClientDataDto();
                clientData.setDescription("A new and fancy client description.");
                client = clientManagement.updateClient(client.getId(), clientData, credentials);
                System.out.println("Client '" + client.getName() + "' got a new description: " + client.getDescription());

                // You can unregister clients completely and withdraw their granted accesses.
                if (clientManagement.unregisterClient(client.getId(), credentials)) {
                    System.out.println("Client '" + client.getName() + "' was successfully unregistered");
                }
            } catch (MalformedURLException | RemoteException | NotBoundException | RemoteClientManagementException | InvalidCredentialsException | FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        private static ClientDataDto prepareClientData() throws FileNotFoundException {
            IconDto icon = new IconDto();
            icon.setData(loadIcon()); // the icon serialized as an array of bytes
            icon.setMimeType("image/png");

            List<String> redirectURIs = new ArrayList<>(2);
            redirectURIs.add("http://localhost/oauth/callback"); // URI for local testing
            redirectURIs.add("https://example.com/api/oauth/callback"); // production URI

            ClientDataDto clientData = new ClientDataDto();
            clientData.setName("Example.com");
            clientData.setDescription("The Example.com web apps description.");
            clientData.setIcon(icon);
            clientData.setContactAddress("support@example.com");
            clientData.setWebsite("http://www.example.com");
            clientData.setDefaultScope("read_contacts write_contacts"); // read and write contacts
            clientData.setRedirectURIs(redirectURIs);
            return clientData;
        }

        private static byte[] loadIcon() throws FileNotFoundException {
            byte[] iconBytes = null;
            try (FileInputStream fis = new FileInputStream("/home/admin/oauth-clients/example_com.png")) {
                byte[] buf = new byte[4096];
                int len = fis.read(buf);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                do {
                    baos.write(buf, 0, len);
                    len = fis.read(buf);
                } while (len >= 0);

                iconBytes = baos.toByteArray();
            } catch (IOException e) {
                // closing the input stream failed - ignore...
            }

            return iconBytes;
        }

    }



###SOAP Provisioning###

Besides RMI all provisioning calls are also available via [SOAP](http://oxpedia.org/wiki/index.php?title=Open-Xchange_Provisioning_using_SOAP). After everything orderly set up you can obtain the according WSDL via `https://ox-prov.coolhosting.me/webservices/OAuthClientService?wsdl`, while `ox-prov.coolhosting.me` denotes your provisioning node. Below you find example requests and responses for all operations.

All operations require the master admin credentials. Icons raw bytes are always required/returned as a Base64-encoded strings.


####List Clients####

List all clients of a certain context group. Only IDs and names are returned.

#####Request#####

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soap.provider.oauth.openexchange.com">
       <soapenv:Header/>
       <soapenv:Body>
          <soap:listClients>
             <soap:contextGroup>default</soap:contextGroup>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:listClients>
       </soapenv:Body>
    </soapenv:Envelope>

#####Response#####

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <listClientsResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <client>
                <id>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</id>
                <name>Example.com</name>
             </client>
             <client>
                <id>ZGVmYXVsdA/0b44694662564c1fb2c1bfc008e247d5b27dd32632734f879d8023aa640dd1ae</id>
                <name>Another App</name>
             </client>
          </listClientsResponse>
       </soap:Body>
    </soap:Envelope>

####Get Client Details####

Get the details of a client by its ID.

#####Request#####

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soap.provider.oauth.openexchange.com">
       <soapenv:Header/>
       <soapenv:Body>
          <soap:getClientById>
             <soap:clientId>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</soap:clientId>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:getClientById>
       </soapenv:Body>
    </soapenv:Envelope>

#####Response#####
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <getClientByIdResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <client>
                <id>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</id>
                <name>Example.com</name>
                <description>Example.com is the superior App Suite extension!</description>
                <contactAddress>contact@example.com</contactAddress>
                <website>https://example.com</website>
                <defaultScope>write_contacts read_contacts</defaultScope>
                <redirectURI>https://app.example.com/oauth2</redirectURI>
                <redirectURI>https://testbed.example.com/oauth2</redirectURI>
                <secret>dc989e068a7943dc800069b807fd6fc9b6ef3defa43d4717ba66c0a275c0697f</secret>
                <registrationDate>1429028393472</registrationDate>
                <enabled>true</enabled>
                <icon>
                   <mimeType>image/jpg</mimeType>
                   <data>/9j/4AAQSkZJRgABAgAAAQABAAD/7QCEUGhvdG9zaG9wIDMuMAA4QklNBAQAAAAAAGccAigAYkZCTUQwMTAwMGE4NjAxMDAwMGUxMDEwMDAwMmQwMjAwMDA1OTAyMDAwMDhlMDIwMDAwMDAwMzAwMDA1NDAzMDAwMDhlMDMwMDAwY2IwMzAwMDAwOTA0MDAwMDg2MDQwMDAwAP/bAEMABgQFBgUEBgYFBgcHBggKEAoKCQkKFA4PDBAXFBgYFxQWFhodJR8aGyMcFhYgLCAjJicpKikZHy0wLSgwJSgpKP/bAEMBBwcHCggKEwoKEygaFhooKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKP/CABEIADIAMgMAIgABEQECEQH/xAAZAAADAQEBAAAAAAAAAAAAAAAAAQIDBgX/xAAYAQEBAQEBAAAAAAAAAAAAAAABAAIDBf/EABgBAQEBAQEAAAAAAAAAAAAAAAEAAgMF/9oADAMAAAERAhEAAAHx9cNfQ8/a8qSoJpEg4dZyHW8+mstZ0+c6Lld4ZBrOTljo821ylQIqWmLaaIAgCv/EABwQAQACAwADAAAAAAAAAAAAAAQBAwACECAwQP/aAAgBAAABBQKMjwnkeiSlOMlIV2lKdGXkoGbedZjkqIkQ7gEtOwxtbGHYWfn/AP/EABoRAAICAwAAAAAAAAAAAAAAAAACEUEgITD/2gAIAQIRAT8BwWDVjRXL/8QAGhEAAgIDAAAAAAAAAAAAAAAAAAIRQSAhMP/aAAgBAREBPwHBpN0JN8v/xAAjEAACAgEDAwUAAAAAAAAAAAACAwERAAQhMRASUhMUMEBh/9oACAEAAAY/AvnQ3UeqZM8ZyVLF4lV3M4xNyGqG452nI9zMnqC4EZwe0amt/wB6oVqCYsleMZLVscZVVSOMaNs1R78bRnbrLB48GMfY/8QAIhABAQEAAQMDBQAAAAAAAAAAAQARMRAhUSBBYTCxwfDx/9oACAEAAAE/IegW+kGG2WW2GDsEeLNNt4a4eEXr0F1j7h+Js7jYp/PmO2LD3lbEY6A5dwy1Qz7pB2k9zNXjfH3hXkLT9+Jd/NsQw22y2x136f8A/9oADAMAAAERAhEAABCmLro7VXJNxDxan8P/xAAeEQACAgEFAQAAAAAAAAAAAAAAAREhMRAgQWHwUf/aAAgBAhEBPxBjEiBLTnsSrTv30uXH2SBkEaPZ/8QAHhEAAgIBBQEAAAAAAAAAAAAAAREAIUEQIFGBofD/2gAIAQERAT8QEBhOh0TWVCb2wvuJQbvzqOCOPd//xAAeEAEAAgICAwEAAAAAAAAAAAABABEhMRBBIFFhcf/aAAgBAAABPxBxRwhYpcXMHhJXKickhiCmsBi85lCK1ihDVt7OpbSJ1bS51jOxsh64V5/uLp2tuCWgBK57D1wURjVqLpt04TPTcFAChCyO6M47agH1wzs1XS8pmnRo1MIm32jNPa/R9UIEA1Zp++MBDyCgweFxeCEPH//Z</data>
                </icon>
             </client>
          </getClientByIdResponse>
       </soap:Body>
    </soap:Envelope>

####Register Client####

Register a new client. The response contains the whole client data along with the generated client ID and secret.

#####Request#####

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soap.provider.oauth.openexchange.com">
       <soapenv:Header/>
       <soapenv:Body>
          <soap:registerClient>
             <soap:contextGroup>default</soap:contextGroup>
             <soap:clientData>
                <!-- All client data fields must be set during registration! -->
                <soap:name>Example.com</soap:name>
                <soap:description>Example.com is the superior App Suite extension!</soap:description>
                <soap:contactAddress>contact@example.com</soap:contactAddress>
                <soap:website>https://example.com</soap:website>
                <!-- Scope is required as a space-separated string -->
                <soap:defaultScope>read_contacts write_contacts</soap:defaultScope>
                <!-- You may define one or more redirect URIs -->
                <soap:redirectURI>https://app.example.com/oauth2</soap:redirectURI>
                <soap:redirectURI>https://testbed.example.com/oauth2</soap:redirectURI>
                <soap:icon>
                   <soap:mimeType>image/jpg</soap:mimeType>
                   <soap:data>
                      /9j/4AAQSkZJRgABAgAAAQABAAD/7QCEUGhvdG9zaG9wIDMuMAA4QklNBAQAAAAAAGccAigAYkZC
                TUQwMTAwMGE4NjAxMDAwMGUxMDEwMDAwMmQwMjAwMDA1OTAyMDAwMDhlMDIwMDAwMDAwMzAwMDA1
                NDAzMDAwMDhlMDMwMDAwY2IwMzAwMDAwOTA0MDAwMDg2MDQwMDAwAP/bAEMABgQFBgUEBgYFBgcH
                BggKEAoKCQkKFA4PDBAXFBgYFxQWFhodJR8aGyMcFhYgLCAjJicpKikZHy0wLSgwJSgpKP/bAEMB
                BwcHCggKEwoKEygaFhooKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgo
                KCgoKCgoKP/CABEIADIAMgMAIgABEQECEQH/xAAZAAADAQEBAAAAAAAAAAAAAAAAAQIDBgX/xAAY
                AQEBAQEBAAAAAAAAAAAAAAABAAIDBf/EABgBAQEBAQEAAAAAAAAAAAAAAAEAAgMF/9oADAMAAAER
                AhEAAAHx9cNfQ8/a8qSoJpEg4dZyHW8+mstZ0+c6Lld4ZBrOTljo821ylQIqWmLaaIAgCv/EABwQ
                AQACAwADAAAAAAAAAAAAAAQBAwACECAwQP/aAAgBAAABBQKMjwnkeiSlOMlIV2lKdGXkoGbedZjk
                qIkQ7gEtOwxtbGHYWfn/AP/EABoRAAICAwAAAAAAAAAAAAAAAAACEUEgITD/2gAIAQIRAT8BwWDV
                jRXL/8QAGhEAAgIDAAAAAAAAAAAAAAAAAAIRQSAhMP/aAAgBAREBPwHBpN0JN8v/xAAjEAACAgED
                AwUAAAAAAAAAAAACAwERAAQhMRASUhMUMEBh/9oACAEAAAY/AvnQ3UeqZM8ZyVLF4lV3M4xNyGqG
                452nI9zMnqC4EZwe0amt/wB6oVqCYsleMZLVscZVVSOMaNs1R78bRnbrLB48GMfY/8QAIhABAQEA
                AQMDBQAAAAAAAAAAAQARMRAhUSBBYTCxwfDx/9oACAEAAAE/IegW+kGG2WW2GDsEeLNNt4a4eEXr
                0F1j7h+Js7jYp/PmO2LD3lbEY6A5dwy1Qz7pB2k9zNXjfH3hXkLT9+Jd/NsQw22y2x136f8A/9oA
                DAMAAAERAhEAABCmLro7VXJNxDxan8P/xAAeEQACAgEFAQAAAAAAAAAAAAAAAREhMRAgQWHwUf/a
                AAgBAhEBPxBjEiBLTnsSrTv30uXH2SBkEaPZ/8QAHhEAAgIBBQEAAAAAAAAAAAAAAREAIUEQIFGB
                ofD/2gAIAQERAT8QEBhOh0TWVCb2wvuJQbvzqOCOPd//xAAeEAEAAgICAwEAAAAAAAAAAAABABEh
                MRBBIFFhcf/aAAgBAAABPxBxRwhYpcXMHhJXKickhiCmsBi85lCK1ihDVt7OpbSJ1bS51jOxsh64
                V5/uLp2tuCWgBK57D1wURjVqLpt04TPTcFAChCyO6M47agH1wzs1XS8pmnRo1MIm32jNPa/R9UIE
                A1Zp++MBDyCgweFxeCEPH//Z
             </soap:data>
                </soap:icon>
             </soap:clientData>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:registerClient>
       </soapenv:Body>
    </soapenv:Envelope>

#####Response#####

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <registerClientResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <client>
                <id>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</id>
                <name>Example.com</name>
                <description>Example.com is the superior App Suite extension!</description>
                <contactAddress>contact@example.com</contactAddress>
                <website>https://example.com</website>
                <defaultScope>write_contacts read_contacts</defaultScope>
                <redirectURI>https://app.example.com/oauth2</redirectURI>
                <redirectURI>https://testbed.example.com/oauth2</redirectURI>
                <secret>dc989e068a7943dc800069b807fd6fc9b6ef3defa43d4717ba66c0a275c0697f</secret>
                <registrationDate>1429028393472</registrationDate>
                <enabled>true</enabled>
                <icon>
                   <mimeType>image/jpg</mimeType>
                   <data>/9j/4AAQSkZJRgABAgAAAQABAAD/7QCEUGhvdG9zaG9wIDMuMAA4QklNBAQAAAAAAGccAigAYkZCTUQwMTAwMGE4NjAxMDAwMGUxMDEwMDAwMmQwMjAwMDA1OTAyMDAwMDhlMDIwMDAwMDAwMzAwMDA1NDAzMDAwMDhlMDMwMDAwY2IwMzAwMDAwOTA0MDAwMDg2MDQwMDAwAP/bAEMABgQFBgUEBgYFBgcHBggKEAoKCQkKFA4PDBAXFBgYFxQWFhodJR8aGyMcFhYgLCAjJicpKikZHy0wLSgwJSgpKP/bAEMBBwcHCggKEwoKEygaFhooKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKP/CABEIADIAMgMAIgABEQECEQH/xAAZAAADAQEBAAAAAAAAAAAAAAAAAQIDBgX/xAAYAQEBAQEBAAAAAAAAAAAAAAABAAIDBf/EABgBAQEBAQEAAAAAAAAAAAAAAAEAAgMF/9oADAMAAAERAhEAAAHx9cNfQ8/a8qSoJpEg4dZyHW8+mstZ0+c6Lld4ZBrOTljo821ylQIqWmLaaIAgCv/EABwQAQACAwADAAAAAAAAAAAAAAQBAwACECAwQP/aAAgBAAABBQKMjwnkeiSlOMlIV2lKdGXkoGbedZjkqIkQ7gEtOwxtbGHYWfn/AP/EABoRAAICAwAAAAAAAAAAAAAAAAACEUEgITD/2gAIAQIRAT8BwWDVjRXL/8QAGhEAAgIDAAAAAAAAAAAAAAAAAAIRQSAhMP/aAAgBAREBPwHBpN0JN8v/xAAjEAACAgEDAwUAAAAAAAAAAAACAwERAAQhMRASUhMUMEBh/9oACAEAAAY/AvnQ3UeqZM8ZyVLF4lV3M4xNyGqG452nI9zMnqC4EZwe0amt/wB6oVqCYsleMZLVscZVVSOMaNs1R78bRnbrLB48GMfY/8QAIhABAQEAAQMDBQAAAAAAAAAAAQARMRAhUSBBYTCxwfDx/9oACAEAAAE/IegW+kGG2WW2GDsEeLNNt4a4eEXr0F1j7h+Js7jYp/PmO2LD3lbEY6A5dwy1Qz7pB2k9zNXjfH3hXkLT9+Jd/NsQw22y2x136f8A/9oADAMAAAERAhEAABCmLro7VXJNxDxan8P/xAAeEQACAgEFAQAAAAAAAAAAAAAAAREhMRAgQWHwUf/aAAgBAhEBPxBjEiBLTnsSrTv30uXH2SBkEaPZ/8QAHhEAAgIBBQEAAAAAAAAAAAAAAREAIUEQIFGBofD/2gAIAQERAT8QEBhOh0TWVCb2wvuJQbvzqOCOPd//xAAeEAEAAgICAwEAAAAAAAAAAAABABEhMRBBIFFhcf/aAAgBAAABPxBxRwhYpcXMHhJXKickhiCmsBi85lCK1ihDVt7OpbSJ1bS51jOxsh64V5/uLp2tuCWgBK57D1wURjVqLpt04TPTcFAChCyO6M47agH1wzs1XS8pmnRo1MIm32jNPa/R9UIEA1Zp++MBDyCgweFxeCEPH//Z</data>
                </icon>
             </client>
          </registerClientResponse>
       </soap:Body>
    </soap:Envelope>

####Update Client####

Already registered clients can be modified. The response contains the whole client data with all changes applied.

#####Request#####

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soap.provider.oauth.openexchange.com">
       <soapenv:Header/>
       <soapenv:Body>
          <soap:updateClient>
             <soap:clientId>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</soap:clientId>
             <soap:clientData>
                <!-- All client data fields that are set will be updated.
                     If you want to change the redirect URIs, you must specify all of them! 
                     All URIs are overridden by the data of this request. If you don't specify
                     any redirectURI element, the existing ones are kept. -->
                <!-- Extend default scope -->
                <soap:defaultScope>read_contacts write_contacts read_calendar write_calendar</soap:defaultScope>
             </soap:clientData>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:updateClient>
       </soapenv:Body>
    </soapenv:Envelope>

#####Response#####

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <updateClientResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <client>
                <id>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</id>
                <name>Example.com</name>
                <description>Example.com is the superior App Suite extension!</description>
                <contactAddress>contact@example.com</contactAddress>
                <website>https://example.com</website>
                <defaultScope>write_contacts write_calendar read_contacts read_calendar</defaultScope>
                <redirectURI>https://app.example.com/oauth2</redirectURI>
                <redirectURI>https://testbed.example.com/oauth2</redirectURI>
                <secret>dc989e068a7943dc800069b807fd6fc9b6ef3defa43d4717ba66c0a275c0697f</secret>
                <registrationDate>1429028393472</registrationDate>
                <enabled>true</enabled>
                <icon>
                   <mimeType>image/jpg</mimeType>
                   <data>/9j/4AAQSkZJRgABAgAAAQABAAD/7QCEUGhvdG9zaG9wIDMuMAA4QklNBAQAAAAAAGccAigAYkZCTUQwMTAwMGE4NjAxMDAwMGUxMDEwMDAwMmQwMjAwMDA1OTAyMDAwMDhlMDIwMDAwMDAwMzAwMDA1NDAzMDAwMDhlMDMwMDAwY2IwMzAwMDAwOTA0MDAwMDg2MDQwMDAwAP/bAEMABgQFBgUEBgYFBgcHBggKEAoKCQkKFA4PDBAXFBgYFxQWFhodJR8aGyMcFhYgLCAjJicpKikZHy0wLSgwJSgpKP/bAEMBBwcHCggKEwoKEygaFhooKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKP/CABEIADIAMgMAIgABEQECEQH/xAAZAAADAQEBAAAAAAAAAAAAAAAAAQIDBgX/xAAYAQEBAQEBAAAAAAAAAAAAAAABAAIDBf/EABgBAQEBAQEAAAAAAAAAAAAAAAEAAgMF/9oADAMAAAERAhEAAAHx9cNfQ8/a8qSoJpEg4dZyHW8+mstZ0+c6Lld4ZBrOTljo821ylQIqWmLaaIAgCv/EABwQAQACAwADAAAAAAAAAAAAAAQBAwACECAwQP/aAAgBAAABBQKMjwnkeiSlOMlIV2lKdGXkoGbedZjkqIkQ7gEtOwxtbGHYWfn/AP/EABoRAAICAwAAAAAAAAAAAAAAAAACEUEgITD/2gAIAQIRAT8BwWDVjRXL/8QAGhEAAgIDAAAAAAAAAAAAAAAAAAIRQSAhMP/aAAgBAREBPwHBpN0JN8v/xAAjEAACAgEDAwUAAAAAAAAAAAACAwERAAQhMRASUhMUMEBh/9oACAEAAAY/AvnQ3UeqZM8ZyVLF4lV3M4xNyGqG452nI9zMnqC4EZwe0amt/wB6oVqCYsleMZLVscZVVSOMaNs1R78bRnbrLB48GMfY/8QAIhABAQEAAQMDBQAAAAAAAAAAAQARMRAhUSBBYTCxwfDx/9oACAEAAAE/IegW+kGG2WW2GDsEeLNNt4a4eEXr0F1j7h+Js7jYp/PmO2LD3lbEY6A5dwy1Qz7pB2k9zNXjfH3hXkLT9+Jd/NsQw22y2x136f8A/9oADAMAAAERAhEAABCmLro7VXJNxDxan8P/xAAeEQACAgEFAQAAAAAAAAAAAAAAAREhMRAgQWHwUf/aAAgBAhEBPxBjEiBLTnsSrTv30uXH2SBkEaPZ/8QAHhEAAgIBBQEAAAAAAAAAAAAAAREAIUEQIFGBofD/2gAIAQERAT8QEBhOh0TWVCb2wvuJQbvzqOCOPd//xAAeEAEAAgICAwEAAAAAAAAAAAABABEhMRBBIFFhcf/aAAgBAAABPxBxRwhYpcXMHhJXKickhiCmsBi85lCK1ihDVt7OpbSJ1bS51jOxsh64V5/uLp2tuCWgBK57D1wURjVqLpt04TPTcFAChCyO6M47agH1wzs1XS8pmnRo1MIm32jNPa/R9UIEA1Zp++MBDyCgweFxeCEPH//Z</data>
                </icon>
             </client>
          </updateClientResponse>
       </soap:Body>
    </soap:Envelope>

####Revoke Secret####

A clients secret can be revoked. This leads to a revocation of all grants authorized by any users for this client. A new secret is generated and part of the response.

#####Request#####

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soap.provider.oauth.openexchange.com">
       <soapenv:Header/>
       <soapenv:Body>
          <soap:revokeClientSecret>
             <soap:clientId>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</soap:clientId>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:revokeClientSecret>
       </soapenv:Body>
    </soapenv:Envelope>

#####Response#####

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <revokeClientSecretResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <client>
                <id>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</id>
                <name>Example.com</name>
                <description>Example.com is the superior App Suite extension!</description>
                <contactAddress>contact@example.com</contactAddress>
                <website>https://example.com</website>
                <defaultScope>write_contacts write_calendar read_contacts read_calendar</defaultScope>
                <redirectURI>https://app.example.com/oauth2</redirectURI>
                <redirectURI>https://testbed.example.com/oauth2</redirectURI>
                <secret>67726d3b86854fc594a19b2251f7e668ece7e9205f11431c92f3f2a6bd2295fc</secret>
                <registrationDate>1429028393472</registrationDate>
                <enabled>true</enabled>
                <icon>
                   <mimeType>image/jpg</mimeType>
                   <data>/9j/4AAQSkZJRgABAgAAAQABAAD/7QCEUGhvdG9zaG9wIDMuMAA4QklNBAQAAAAAAGccAigAYkZCTUQwMTAwMGE4NjAxMDAwMGUxMDEwMDAwMmQwMjAwMDA1OTAyMDAwMDhlMDIwMDAwMDAwMzAwMDA1NDAzMDAwMDhlMDMwMDAwY2IwMzAwMDAwOTA0MDAwMDg2MDQwMDAwAP/bAEMABgQFBgUEBgYFBgcHBggKEAoKCQkKFA4PDBAXFBgYFxQWFhodJR8aGyMcFhYgLCAjJicpKikZHy0wLSgwJSgpKP/bAEMBBwcHCggKEwoKEygaFhooKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKP/CABEIADIAMgMAIgABEQECEQH/xAAZAAADAQEBAAAAAAAAAAAAAAAAAQIDBgX/xAAYAQEBAQEBAAAAAAAAAAAAAAABAAIDBf/EABgBAQEBAQEAAAAAAAAAAAAAAAEAAgMF/9oADAMAAAERAhEAAAHx9cNfQ8/a8qSoJpEg4dZyHW8+mstZ0+c6Lld4ZBrOTljo821ylQIqWmLaaIAgCv/EABwQAQACAwADAAAAAAAAAAAAAAQBAwACECAwQP/aAAgBAAABBQKMjwnkeiSlOMlIV2lKdGXkoGbedZjkqIkQ7gEtOwxtbGHYWfn/AP/EABoRAAICAwAAAAAAAAAAAAAAAAACEUEgITD/2gAIAQIRAT8BwWDVjRXL/8QAGhEAAgIDAAAAAAAAAAAAAAAAAAIRQSAhMP/aAAgBAREBPwHBpN0JN8v/xAAjEAACAgEDAwUAAAAAAAAAAAACAwERAAQhMRASUhMUMEBh/9oACAEAAAY/AvnQ3UeqZM8ZyVLF4lV3M4xNyGqG452nI9zMnqC4EZwe0amt/wB6oVqCYsleMZLVscZVVSOMaNs1R78bRnbrLB48GMfY/8QAIhABAQEAAQMDBQAAAAAAAAAAAQARMRAhUSBBYTCxwfDx/9oACAEAAAE/IegW+kGG2WW2GDsEeLNNt4a4eEXr0F1j7h+Js7jYp/PmO2LD3lbEY6A5dwy1Qz7pB2k9zNXjfH3hXkLT9+Jd/NsQw22y2x136f8A/9oADAMAAAERAhEAABCmLro7VXJNxDxan8P/xAAeEQACAgEFAQAAAAAAAAAAAAAAAREhMRAgQWHwUf/aAAgBAhEBPxBjEiBLTnsSrTv30uXH2SBkEaPZ/8QAHhEAAgIBBQEAAAAAAAAAAAAAAREAIUEQIFGBofD/2gAIAQERAT8QEBhOh0TWVCb2wvuJQbvzqOCOPd//xAAeEAEAAgICAwEAAAAAAAAAAAABABEhMRBBIFFhcf/aAAgBAAABPxBxRwhYpcXMHhJXKickhiCmsBi85lCK1ihDVt7OpbSJ1bS51jOxsh64V5/uLp2tuCWgBK57D1wURjVqLpt04TPTcFAChCyO6M47agH1wzs1XS8pmnRo1MIm32jNPa/R9UIEA1Zp++MBDyCgweFxeCEPH//Z</data>
                </icon>
             </client>
          </revokeClientSecretResponse>
       </soap:Body>
    </soap:Envelope>

####Unregister Client####
Of course clients can be unregistered. This leads to a revocation of all grants authorized by any users for this client. If the client ID is invalid, the responses success value will be `false`.

#####Request#####

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soap.provider.oauth.openexchange.com">
       <soapenv:Header/>
       <soapenv:Body>
          <soap:unregisterClient>
             <soap:clientId>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</soap:clientId>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:unregisterClient>
       </soapenv:Body>
    </soapenv:Envelope>

#####Response#####

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <unregisterClientResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <success>true</success>
          </unregisterClientResponse>
       </soap:Body>
    </soap:Envelope>

####Disable Client####
Enabled clients can be disabled. This leads to a revocation of all grants authorized by any users for this client and no further grants can be requested. If the client was already disabled, the responses success value will be `false`.

#####Request#####

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soap.provider.oauth.openexchange.com">
       <soapenv:Header/>
       <soapenv:Body>
          <soap:disableClient>
             <soap:clientId>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</soap:clientId>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:disableClient>
       </soapenv:Body>
    </soapenv:Envelope>

#####Response#####

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <disableClientResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <success>true</success>
          </disableClientResponse>
       </soap:Body>
    </soap:Envelope>

####Enable Client####

Disabled clients can of course be enabled again. If the client was already enabled, the responses success value will be `false`.

#####Request#####

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soap.provider.oauth.openexchange.com">
       <soapenv:Header/>
       <soapenv:Body>
          <soap:enableClient>
             <soap:clientId>ZGVmYXVsdA/2b6d423de5344ed9bd67f95eb6917507f7c8018a5c0a47a1a4ba1bae14615ee6</soap:clientId>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:enableClient>
       </soapenv:Body>
    </soapenv:Envelope>

#####Response#####

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <enableClientResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <success>true</success>
          </enableClientResponse>
       </soap:Body>
    </soap:Envelope>