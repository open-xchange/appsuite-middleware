---
title: Access Login Counter data with Jolokia
---

This article describes how to access information exposed through JMX by Open-Xchange with the Jolokia JMX-to-HTTP bridge, using "Login Counter" as an example.

# Install Open-Xchange

See the [quick install guide](http://oxpedia.org/wiki/index.php?title=AppSuite:Main_Page_AppSuite#quickinstall) if you don't have Open-Xchange installed yet. Jolokia is part of the base product, no extra packages are needed.

# Enable Jolokia

In `etc/jolokia.properties`, enable Jolokia by setting the following properties:

```properties
com.openexchange.jolokia.start = true
com.openexchange.jolokia.user = youruser
com.openexchange.jolokia.password = yourpassword
```
Note that Jolokia will not be enabled when no user/password is set.

You can optionally adjust this setting:

```properties
com.openexchange.jolokia.servlet.name = /monitoring/jolokia
```
If you do, you need to adjust the examples below as well.

# Allow access from other hosts

This is an optional step in case you want to access the Jolokia interface from other hosts other than `localhost`. This may be very helpful during the development phase of a project. Please be aware that this interface exposes lots of "interesting" data, so if you remove the restriction to localhost, you need to ensure by other means (network setup, firewalls, web server configuration, ...) that no unauthorised access is possible on production systems.

In `etc/jolokia.properties`, set:

```properties
com.openexchange.jolokia.restrict.to.localhost = false
```
In your web server configuration, enable access to the Jolokia servlet. For Apache this is possible by adding a `ProxyPass` directive for each OX host in the cluster:

```apache
 ProxyPass /monitoring/ox1/jolokia http://ox1-ip:8009/monitoring/jolokia
 ProxyPass /monitoring/ox2/jolokia http://ox2-ip:8009/monitoring/jolokia
 ...
```
On a default installation as described by our installation guides, this would be in `proxy_http.conf`.

Reload your apache config and restart the `open-xchange` service for the changes to take effect.

# Access the Jolokia interface

On localhost, call:

```bash
$ curl http://yourname:yourpassword@localhost:8009/monitoring/jolokia/list > ox.json
```
If you enabled access from other hosts, you can also use a standard web browser. For example open the following URL in a browser:

```
http://<yourserver>/monitoring/ox1/jolokia/list
```
You'll be asked for user name and password through a standard HTTP authentication window.

## Access specific information

The `ox.json` file you created in the last step gives you a complete list on what data is available through this interface.

As an example, the "Login Counter" interface (which is also used by the 
[`logincounter`]({{ site.baseurl }}/middleware/components/commandlinetools/logincounter.html) command line tool) is described like this:

```json
"com.openexchange.reporting": {
  "name=Login Counter": {
    "desc": "Information on the management interface of the MBean",
    "op": {
      "getLastLoginTimeStamp": {
        "ret": "java.util.List",
        "desc": "Operation exposed for management",
        "args": [
          {
            "desc": "",
            "name": "p1",
            "type": "int"
          },
          {
            "desc": "",
            "name": "p2",
            "type": "int"
          },
          {
            "desc": "",
            "name": "p3",
            "type": "java.lang.String"
          }
        ]
      },
      "getNumberOfLogins": {
        "ret": "java.util.Map",
        "desc": "Operation exposed for management",
        "args": [
          {
            "desc": "",
            "name": "p1",
            "type": "java.util.Date"
          },
          {
            "desc": "",
            "name": "p2",
            "type": "java.util.Date"
          },
          {
            "desc": "",
            "name": "p3",
            "type": "boolean"
          },
          {
            "desc": "",
            "name": "p4",
            "type": "java.lang.String"
          }
        ]
      }
    }
  }
}
```

A more detailed documentation on how to use the Jolokia interface can be found [here](http://www.jolokia.org/reference/html/protocol.html). Furthermore, [here](http://www.jolokia.org/reference/html/protocol.html#serialization) you can find documentation of the list of datatypes that can be passed as arguments and received in return values.

More information about the parameters `p1`, `p2`, etc. can be found at the [source code](http://oxpedia.org/wiki/index.php?title=SourceCodeAccess).

In the example above, to get the number of logins in a specific timeframe and with a specific client, we need to call the method `getNumberOfLogins` with the parameters `startDate`, `endDate`, `aggregate` and `clientstring`. They correspond to the command line parameters of the `logincounter` command line tool as described [here](http://oxpedia.org/wiki/index.php?title=AppSuite:Logincounter).

In `curl` this call would look like this:

```bash
$ curl http://yourname:yourpassword@localhost:8009/monitoring/jolokia/exec/com.openexchange.reporting:name=Login%20Counter/getNumberOfLogins/2015-01-01T00:00:00/2015-01-31T23:59:59/true/open-xchange-appsuite/
```
If you have enabled access to the Jolokia interface from other hosts, the same information can be viewed in any web browser:

```bash
http://<yourserver>/monitoring/ox1/jolokia/exec/com.openexchange.reporting:name=Login%20Counter/getNumberOfLogins/2015-01-01T00:00:00/2015-01-31T23:59:59/true/open-xchange-appsuite/
```