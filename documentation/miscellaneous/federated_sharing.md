## Federated Sharing 

*OX App Suite 7.10.5* introduces the new concept of "*Federated Sharing*" which allows a user to integrate received shares from other contexts
or servers into his own account. This provides a seamless integration of shared data and a smoothly user experience.

### Motivation
With the introduction of the guest mode in OX App Suite 7.8.0 external users, without a regular account on the server, 
are able to interact with the shared data in the same way as regular users do by using the guest user interface.

However, if a guest user is also a regular user on another OX App Suite server, or on the same server but in another context, 
jumping back and fourth between the regular and the guest user interface is quite cumbersome.

OX App Suite 7.10.5 introduces the new concept of "Federated Sharing" which allows a user to integrate a received share from another context 
or another server into his own account and access these data in the same way as regular data.
This provides a seamless integration of data shared from a different context or server withouth using the guest interface.


With the release of 7.10.5 the supported "Federated Sharing" modules are "Drive" and "Calendar"

### Installation

The new "Federated Sharing" feature is part of of the **open-xchange-subscribe** package. Just install this package if not already installed.

### Modes

When it comes to integrating a share, OX App Suite can handle two different modes: The *cross-context* and the *cross-ox* mode.

#### Cross context

The *cross-context* mode is chosen when a share comes from a different context on the same OX server. 
The server will use internal services to access the shared data in the other context.  No remote communication is required in this case.

The following properties needs to be set in order to enable this feature:

* <code>com.openexchange.capability.filestorage_xctx=true</code>
* <code>com.openexchange.share.crossContextGuests=true</code>

See [Federated-Sharing](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}/#mode=tags&tag=Federated Sharing) for a 
full list of configuration options related to Federated Sharing.

#### Cross OX

The *cross-ox* mode is used when a share is from *another* OX server. The communication between the OX servers will then take place over the HTTP API.

The following properties needs to be set in order to enable this feature:

* <code>com.openexchange.capability.filestorage_xox=true</code>

See [Federated-Sharing](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}/#mode=tags&tag=Federated Sharing) for a 
full list of configuration options related to Federated Sharing.

Furthermore there are properties that influences the communication between the servers. A administrator can adjust several settings:

* <code>com.openexchange.api.client.blacklistedHosts</code>
* <code>com.openexchange.api.client.allowedPorts</code>
* <code>com.openenexchange.httpclient.apiClient*</code>, see [here]({{ site.baseurl }}/middleware/administration/http_client_configuration.html) for more details.


### Further information 

[Sharing and Guest Mode]({{ site.baseurl }}/middleware/miscellaneous/sharing_and_guest_mode.html)
