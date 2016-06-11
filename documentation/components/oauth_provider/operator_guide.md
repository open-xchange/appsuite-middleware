---
title: Operator Guide
---

With OX App Suite v7.8.0 a service provider can decide to publish a certain subset of the OX HTTP API via OAuth 2.0. See the [developer guide](AppSuite:OAuth_2_0_Client_Developer_Guide "wikilink") for an overview of the available APIs. The feature as a whole is contained in separate optional packages and requires some configuration. Supported client applications must be of type `confidential` according to the `web application` profile defined in [RFC 6749](http://tools.ietf.org/html/rfc6749). The OX middleware will act as `resource server` and by default also as `authorization server`.

When acting as `authorization server`, every application must be registered at the OX backend. The registration process is up to you, while the backend provides SOAP and RMI interfaces to persist those registrations and generates the client-specific credentials that are needed to gain access for granting users. With OX App Suite v7.8.1 it is possible to use an external authorization server.

# Installation and Configuration

The OAuth provider feature is separated into two packages `open-xchange-oauth-provider` and `open-xchange-admin-oauth-provider`. The former one needs to be installed on every groupware node, the latter one provides the client provisioning interfaces and may be installed on your dedicated provisioning nodes. On a Debian setup you would install those packages like so:

    $ apt-get install open-xchange-oauth-provider open-xchange-admin-oauth-provider

Configuration takes place in `/opt/open-xchange/etc/oauth-provider.properties`. The feature is disabled by default and must be activated manually.

# Using the internal authorization server

**Note:** Using an external authorization server is earliest possible with v7.8.1.

The internal authorization server needs some configuration, that also takes place in `/opt/open-xchange/etc/oauth-provider.properties`. An encryption key is used to encrypt the credentials of client applications within the database. You MUST set a value here and this value must be the same on all groupware and provisioning nodes. Example:

    # Set to 'true' to basically enable the OAuth 2.0 provider. This setting can then be overridden
    # via config cascade to disallow granting access for certain users. If the provider is enabled,
    # an encryption key (see below) must be set!
    #
    # Default: false
    com.openexchange.oauth.provider.enabled=true

    # Defines whether the enabled OAuth 2.0 provider does not only act as resource server but also
    # as authorization server. If 'true' the following functionality will be provided:
    #   * An authorization endpoint, token endpoint and revocation endpoint are made available via HTTP
    #   * API calls for revoking access to external clients are made available, access can be revoked via
    #     App Suite UI
    #   * Provisioning interfaces to manage trusted clients are enabled
    #
    # If set to 'false' while the provider itself is enabled, a custom bridge to the external authorization
    # server must be provided.
    #
    # Default: true
    com.openexchange.oauth.provider.isAuthorizationServer=true

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

# Client Provisioning

For every client application that you want to allow to access the OAuth APIs you need to persist some data. During the registration call a client ID and a secret are generated, which must be provided to the client developers.

**Important:** A client does always belong to a context group and is stored within the global DB. It can therefore only handle users of the according contexts. As a result you need to pass a context group name to some of the provisioning calls unless a client ID is required. After registering a client, the context group identifier is encoded within the client ID.

The registration data consists of the following parameters:

| Parameter       | Description                                                                                                                                                                                                                                                                                                                                                                | Required |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| name            | The name of the client application. Will be visible to your users.                                                                                                                                                                                                                                                                                                         | Yes      |
| description     | A description of the client application. Will be visible to your users. Translations of the description are currently not supported, you must decide for one language.                                                                                                                                                                                                     | Yes      |
| contact address | E-Mail address to contact the application vendor.                                                                                                                                                                                                                                                                                                                          | Yes      |
| website         | An URL to the client applications website.                                                                                                                                                                                                                                                                                                                                 | Yes      |
| default scope   | A default scope that will be applied if the client application asks a user for access without providing a certain scope during the request. See the developer guide for available scope tokens. The scope is always a space-delimited string cosisting of one or more scope tokens, e.g. "read\_contacts write\_contacts".                                             | Yes      |
| icon            | An icon of the client application. Supported image types are `image/png`, `image/jpg` and `image/jpeg`. Icons SHOULD be of size 128x128 px, otherwise they might not get displayed correctly. The max. image size is 256kb.                                                                                                                                                | Yes      |
| redirect URIs   | One or more URIs that will be used as redirect locations to deliver authorization codes or error messages back to the client application. Every URI must be absolute and not contain a fragment. The scheme must always be `https`, however for development purposes redirect URIs pointing to `localhost`, `127.0.0.1` or `[::1]` are also allowed with `http` as scheme. | Yes      |

## RMI Provisioning

To use RMI as provisioning mechanism you need to link your code against the correct API classes. You'll find the according JAR files on your provisioning node. Navigate to `/opt/open-xchange/libs` and fetch `com.openexchange.admin.rmi.jar` and `com.openexchange.oauth.provider.rmi.jar`. Note that both files are symlinks. Besides the JARs you'll find the according JavaDoc in `/usr/share/doc/open-xchange-admin/javadoc` and `/usr/share/doc/open-xchange-oauth-provider/javadoc`. After adding both JARs to your classpath you can start development. The remote interface is `com.openexchange.oauth.provider.rmi.RemoteClientManagement`. Below you find an example of all operations that manipulate client data. Of course there are also methods to list and get all or certain registered clients.

```java
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
      RemoteClientManagement clientManagement = 
        (RemoteClientManagement) Naming.lookup(
          "rmi://coolhosting.me:1099/" + RemoteClientManagement.RMI_NAME);

      // All method calls require the master credentials.
      Credentials credentials = new Credentials("oxadminmaster", "secret");

      ClientDataDto clientData = prepareClientData();
      ClientDto client = clientManagement.registerClient(
        RemoteClientManagement.DEFAULT_GID, // use default context group
        clientData,
        credentials);

      System.out.println("Client '" + client.getName() + "' was successfully " +
        "registered: [ID: " + client.getId() + ", secret: " + client.getSecret() + "]");

      // You can disable clients temporarily. API access is then prohibited.
      if (clientManagement.disableClient(client.getId(), credentials)) {
        System.out.println("Client '" + client.getName() + "' was disabled");
      }

      // Of course enabling disabled clients is also possible.
      if (clientManagement.enableClient(client.getId(), credentials)) {
        System.out.println("Client '" + client.getName() + "' was enabled again");
      }

      // You can revoke a clients secret. For security reasons all existing grants
      // are invalidated then.
      client = clientManagement.revokeClientSecret(client.getId(), credentials);
      System.out.println("Client '" + client.getName() + "' was assigned a new " +
        "secret: " + client.getSecret());

      // Of course you can update the client data. Every field set within ClientData
      // will be overridden. Fields that are not set will not be modified. Scope and
      // redirect URIs must always be submitted in total, no merging will be applied
      // here.
      clientData = new ClientDataDto();
      clientData.setDescription("A new and fancy client description.");
      client = clientManagement.updateClient(client.getId(), clientData, credentials);
      System.out.println("Client '" + client.getName() + 
        "' got a new description: " + client.getDescription());

      // You can unregister clients completely and withdraw their granted accesses.
      if (clientManagement.unregisterClient(client.getId(), credentials)) {
        System.out.println("Client '" + client.getName() + 
          "' was successfully unregistered");
      }
    } catch (MalformedURLException | RemoteException | NotBoundException | 
             RemoteClientManagementException | InvalidCredentialsException |
             FileNotFoundException e) {

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
    clientData.setDefaultScope("read_contacts write_contacts");
    clientData.setRedirectURIs(redirectURIs);
    return clientData;
  }

  private static byte[] loadIcon() throws FileNotFoundException {
    byte[] iconBytes = null;
    // TODO: change this path accordingly
    try (FileInputStream fis = new FileInputStream("/path/to/icon.png")) {
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
```

## Command-line provisioning

The following command-line tools are available starting with v7.8.1.


### createoauthclient

**` createoauthclient `** is the tool to create a new OAuth client app.

#### Parameters

|                                      |                                                                                                                    |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| -h,--help                            | Prints a help text                                                                                                 |
| --environment                        | Show info about commandline environment                                                                            |
| --nonl                               | Remove all newlines (\\n) from output                                                                              |
| --responsetimeout <integer>    | Response timeout in seconds for reading response from the backend (default 0s; infinite) **Available with v7.8.0** |
| -c/--context-group-id <string> | The id of the context group                                                                                        |
| -n/--name <string>             | The name for the OAuth client app                                                                                  |
| -d/--description <string>      | The description for the OAuth client app                                                                           |
| -w/--website <string>          | The client website                                                                                                 |
| -o/--contact-address <string>  | The contact address for the OAuth client app                                                                       |
| -i/--icon-path <string>        | Path to an image file which acts as a icon for the OAuth client                                                    |
| -s/--default-scope <string>    | The default scope of the OAuth client                                                                              |
| --urls <string>                | The redirect urls of the OAuth client as a comma-separated list                                                    |

#### Extra parameters when authentication is enabled

|                               |                         |
|-------------------------------|-------------------------|
| -A,--adminuser <string> | Context admin user name |
| -P,--adminpass <string> | Context admin password  |

#### Return value

`0` on success

`>0` on failure

#### Mandatory parameters

`context-group-id {adminuser adminpass} name description website contact-address icon-path default-scope urls`

#### Command output

On success:

`The registration of oauth client was successful`

On failure:

`The registration of oauth client has failed`


### enableoauthclient

**` enableoauthclient `** is the tool to enable or disable a certain OAuth client app.

#### Parameters

|                                   |                                                                                                                    |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------|
| -h,--help                         | Prints a help text                                                                                                 |
| --environment                     | Show info about commandline environment                                                                            |
| --nonl                            | Remove all newlines (\\n) from output                                                                              |
| --responsetimeout <integer> | Response timeout in seconds for reading response from the backend (default 0s; infinite) **Available with v7.8.0** |
| --id <integer>              | The id of the oauth client                                                                                         |
| -e, --enable <string>       | Flag that indicates whether the client should be enabled or disabled (values: 'true' or 'false')                   |

#### Extra parameters when authentication is enabled

|                               |                         |
|-------------------------------|-------------------------|
| -A,--adminuser <string> | Context admin user name |
| -P,--adminpass <string> | Context admin password  |

#### Return value

`0` on success

`>0` on failure

#### Mandatory parameters

`id {adminuser adminpass} enable`

#### Command output

On success:

`Enabling the oauth client was successful!`

On failure:

`Enabling the oauth client has failed!`


### updateoauthclient

**` updateoauthclient `** is the tool to update a certain OAuth client app.

#### Parameters

|                                        |                                                                                                                    |
|----------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| --environment                          | Show info about commandline environment                                                                            |
| --nonl                                 | Remove all newlines (\\n) from output                                                                              |
| --responsetimeout <integer>      | Response timeout in seconds for reading response from the backend (default 0s; infinite) **Available with v7.8.0** |
| --id <id> <integer>              | The id of the oauth client                                                                                         |
| -x, --context-group-id <integer> | The id of the context group                                                                                        |
| -n, --name <string>              | The name of the oauth client                                                                                       |
| -d, --description <string>       | The description of the oauth client                                                                                |
| -w, --website <string>           | The client website                                                                                                 |
| -o, --contact-address <string>   | The contact adress of the oauth client                                                                             |
| -i, --icon-path <string>         | Path to a image file which acts as a icon for the oauth client                                                     |
| -s, --default-scope <string>     | The default scope of the oauth client                                                                              |
| --urls <string>                  | The redirect urls of the oauth client as a comma separated list                                                    |

#### Extra parameters when authentication is enabled

|                               |                         |
|-------------------------------|-------------------------|
| -A,--adminuser <string> | Context admin user name |
| -P,--adminpass <string> | Context admin password  |

#### Return value

`0` on success

`>0` on failure

#### Mandatory parameters

`id {adminuser adminpass}`

#### Command output

On success:

`The update of oauth client with id XYZ was successful!<br>
The updated oauth client: <br>
Client_ID = id <br>
Name = name <br>
Enabled = true|false <br>
Description = description <br>
Website = url <br>
Contact address = address <br>
Default scope = scope <br>
Redirect URL's = urls <br>
Client's current secret = secret <br>
`

On failure:

`The update of oauth client with id XYZ has failed!`


### removeoauthclient

**` removeoauthclient `** is the tool to remove (unregister) a certain OAuth client app.

#### Parameters

|                                   |                                                                                                                    |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------|
| --environment                     | Show info about commandline environment                                                                            |
| --nonl                            | Remove all newlines (\\n) from output                                                                              |
| --responsetimeout <integer> | Response timeout in seconds for reading response from the backend (default 0s; infinite) **Available with v7.8.0** |
| --id <id> <integer>         | The id of the oauth client                                                                                         |

#### Extra parameters when authentication is enabled

|                               |                         |
|-------------------------------|-------------------------|
| -A,--adminuser <string> | Context admin user name |
| -P,--adminpass <string> | Context admin password  |

#### Return value

`0` on success

`>0` on failure

#### Mandatory parameters

`id {adminuser adminpass}`

#### Command output

On success:

`The removal of oauth client with id XYZ was successful!`

On failure:

`The removal of oauth client with id XYZ has failed!`


### getoauthclient

**` getoauthclient `** is the tool to get a certain OAuth client app.

#### Parameters

|                                   |                                                                                                                    |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------|
| --environment                     | Show info about commandline environment                                                                            |
| --nonl                            | Remove all newlines (\\n) from output                                                                              |
| --responsetimeout <integer> | Response timeout in seconds for reading response from the backend (default 0s; infinite) **Available with v7.8.0** |
| --id <id> <integer>         | The id of the oauth client                                                                                         |

#### Extra parameters when authentication is enabled

|                               |                         |
|-------------------------------|-------------------------|
| -A,--adminuser <string> | Context admin user name |
| -P,--adminpass <string> | Context admin password  |

#### Return value

`0` on success

`>0` on failure

#### Mandatory parameters

`id {adminuser adminpass}`

#### Command output

On success:

`Client_ID = id <br>
Name = name <br>
Enabled = true|false <br>
Description = description <br>
Website = url <br>
Contact address = address <br>
Default scope = scope <br>
Redirect URL's = urls <br>
Client's current secret = secret <br>
`

On failure:

`Client not found!`
## 

### listoauthclient

**` listoauthclient `** is the tool to list all OAuth client apps for a certain context-group-id.

#### Parameters

|                                        |                                                                                                                    |
|----------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| --environment                          | Show info about commandline environment                                                                            |
| --nonl                                 | Remove all newlines (\\n) from output                                                                              |
| --responsetimeout <integer>      | Response timeout in seconds for reading response from the backend (default 0s; infinite) **Available with v7.8.0** |
| -c, --context-group-id <integer> | The id of the context group                                                                                        |

#### Extra parameters when authentication is enabled

|                               |                         |
|-------------------------------|-------------------------|
| -A,--adminuser <string> | Context admin user name |
| -P,--adminpass <string> | Context admin password  |

#### Return value

`0` on success

`>0` on failure

#### Mandatory parameters

`context-group-id {adminuser adminpass}`

#### Command output

On success:
`Following clients are registered: <br>
Client_ID = id <br>
Name = name <br>
Enabled = true|false <br>
Description = description <br>
Website = url <br>
Contact address = address <br>
Default scope = scope <br>
Redirect URL's = urls <br>
Client's current secret = secret <br>
[...]
`

On failure:
An empty list.


### revokeoauthclient

**` revokeoauthclient `** is the tool to revoke the secret of a certain OAuth client app.

#### Parameters

|                                   |                                                                                                                    |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------|
| --environment                     | Show info about commandline environment                                                                            |
| --nonl                            | Remove all newlines (\\n) from output                                                                              |
| --responsetimeout <integer> | Response timeout in seconds for reading response from the backend (default 0s; infinite) **Available with v7.8.0** |
| --id <id> <integer>         | The id of the oauth client                                                                                         |

#### Extra parameters when authentication is enabled

|                               |                         |
|-------------------------------|-------------------------|
| -A,--adminuser <string> | Context admin user name |
| -P,--adminpass <string> | Context admin password  |

#### Return value

`0` on success

`>0` on failure

#### Mandatory parameters

`id {adminuser adminpass}`

#### Command output

On success:

`The revocation of the client's current secret was successfull!<br>
Generated a new secret for following client: <br>
Client_ID = id <br>
Name = name <br>
Enabled = true|false <br>
Description = description <br>
Website = url <br>
Contact address = address <br>
Default scope = scope <br>
Redirect URL's = urls <br>
Client's current secret = secret
`

On failure:

`The revocation of the client's current secret has failed!`


## SOAP Provisioning

Besides RMI all provisioning calls are also available via [SOAP](http://oxpedia.org/wiki/index.php?title=Open-Xchange_Provisioning_using_SOAP). After everything orderly set up you can obtain the according WSDL via `https://ox-prov.coolhosting.me/webservices/OAuthClientService?wsdl`, while `ox-prov.coolhosting.me` denotes your provisioning node. Below you find example requests and responses for all operations.

All operations require the master admin credentials. Icons raw bytes are always required/returned as a Base64-encoded strings.

### List Clients

List all clients of a certain context group. Only IDs and names are returned.

#### Request

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:soap="http://soap.provider.oauth.openexchange.com">
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

#### Response

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

### Get Client Details

Get the details of a client by its ID.

#### Request

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:soap="http://soap.provider.oauth.openexchange.com">
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

#### Response

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
                   <data>
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
                  </data>
                </icon>
             </client>
          </getClientByIdResponse>
       </soap:Body>
    </soap:Envelope>

### Register Client

Register a new client. The response contains the whole client data along with the generated client ID and secret.

#### Request

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:soap="http://soap.provider.oauth.openexchange.com">
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
                   <data>
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
                  </data>
                </soap:icon>
             </soap:clientData>
             <soap:credentials>
                <soap:login>oxadminmaster</soap:login>
                <soap:password>secret</soap:password>
             </soap:credentials>
          </soap:registerClient>
       </soapenv:Body>
    </soapenv:Envelope>

#### Response

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
                   <data>
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
                  </data>
                </icon>
             </client>
          </registerClientResponse>
       </soap:Body>
    </soap:Envelope>

### Update Client

Already registered clients can be modified. The response contains the whole client data with all changes applied.

#### Request

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:soap="http://soap.provider.oauth.openexchange.com">
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

#### Response

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
                  <data>
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
                  </data>
                </icon>
             </client>
          </updateClientResponse>
       </soap:Body>
    </soap:Envelope>

### Revoke Secret

A clients secret can be revoked. This leads to a revocation of all grants authorized by any users for this client. A new secret is generated and part of the response.

#### Request

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:soap="http://soap.provider.oauth.openexchange.com">
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

#### Response

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
                   <data>
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
                  </data>
                </icon>
             </client>
          </revokeClientSecretResponse>
       </soap:Body>
    </soap:Envelope>

### Unregister Client

Of course clients can be unregistered. This leads to a revocation of all grants authorized by any users for this client. If the client ID is invalid, the responses success value will be `false`.

#### Request

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:soap="http://soap.provider.oauth.openexchange.com">
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

#### Response

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <unregisterClientResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <success>true</success>
          </unregisterClientResponse>
       </soap:Body>
    </soap:Envelope>

### Disable Client

Enabled clients can be disabled. This leads to a revocation of all grants authorized by any users for this client and no further grants can be requested. If the client was already disabled, the responses success value will be `false`.

#### Request

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:soap="http://soap.provider.oauth.openexchange.com">
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

#### Response

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <disableClientResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <success>true</success>
          </disableClientResponse>
       </soap:Body>
    </soap:Envelope>

### Enable Client

Disabled clients can of course be enabled again. If the client was already enabled, the responses success value will be `false`.

#### Request

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:soap="http://soap.provider.oauth.openexchange.com">
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

#### Response

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <enableClientResponse xmlns="http://soap.provider.oauth.openexchange.com">
             <success>true</success>
          </enableClientResponse>
       </soap:Body>
    </soap:Envelope>


# Using an external authorization server

When using an external authorization server, the internal client and grant management are deactivated. The according provisioning interfaces will not be available and the "External Apps" section within the App Suite settings page will not show up. The whole client and grant management will be off-loaded to the external identity management system.

You need to deactivate the internal authorization server, by setting `com.openexchange.oauth.provider.isAuthorizationServer` in `/opt/open-xchange/etc/oauth-provider.properties` to `false`. Additionally you need to provide an implementation of `com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService` as OSGi service. Its purpose is to validate the access tokens of incoming requests and to identify the according OX user. The service provider interface is contained in bundle `com.openexchange.oauth.provider`.

## Example

An example implementation using the [WSO2 Identity Server 5.1.0](http://wso2.com/products/identity-server/) can be found here: <https://code.open-xchange.com/git/examples/backend-samples> as project `com.openexchange.oauth.provider.wso2`. It is based on this [blog post](http://shanakaweerasinghe.blogspot.de/2016_01_01_archive.html).

If you want to test the WSO2 provider in production, you need to create a proper bundle JAR and install it to your OX middleware OSGi runtime. The endpoint of the WSO2 `OAuth2TokenValidationService` must be configured via `/opt/open-xchange/etc/wso2-oauth.properties`. Example content:

    com.openexchange.oauth.provider.wso2.tokenEndpoint = https://localhost:9443/services/OAuth2TokenValidationService
    com.openexchange.oauth.provider.wso2.apiUser = admin
    com.openexchange.oauth.provider.wso2.apiPassword = admin

The WSO2 identity server also needs some configuration:

-   Edit `wso2is-5.1.0/repository/conf/identity/identity.xml` and change the `<Enabled>` element of `<AuthorizationContextTokenGeneration>` to `true`
-   Edit and change `<HideAdminServiceWSDLs>` to `false`
-   Restart the server and login as admin to the management console
-   Add a new service provider with name "OAuth" and configure OAuth as inbound authentication mechanism
	-   OAuth Version: 2.0
	-   Callback Url: depends on your application
	-   Allowed Grant Types: Code and Refresh Token
-   Copy the shown client credentials and configure your test application accordingly
-   Add a new user
	-   Assign the role "Application/OAuth"
	-   Edit its profile and configure the email address to match the pattern `<ox-user-name>@<ox-context-name>`. The context name must end with a TLD, you'll need an according login mapping within OX.

You can now configure your client with the according authorization and token endpoint URLs:

-   Authorization endpoint: `https://<wso2-host>:9443/oauth2/authorize`
-   Token endpoint: `https://<wso2-host>:9443/oauth2/token`

