---
Title: Advertisement
---

With 7.8.3 the OX middleware is able to manage advertisement configurations. These configurations can be used by various clients to show or hide specific advertisements. For example there could be an advertisement banner at the top of the client view which can be switched on and off.

These configurations will be stored as JSON strings within the configdb in the table 'advertisement_config'. Additional there is another table with the name 'advertisement_mapping' which contains a mapping from a pair of reseller and package to an advertisement config. Here reseller references the name of the reseller and package references a generic package name. In case there is no reseller or in case of a single package the entry can be replaced by the default value "default". For example if you have no reseller and two packages with the names "packA" and "packB" you use "default" for the reseller and either "packA" or "packB" for the package. Which packages are available depends on the configuration of the middleware. Furmost it depends on which package scheme is configured. The scheme determines how the package will be retrieved. The package scheme can be configured per reseller via the com.openexchange.advertisement.[reseller].packageScheme property. There are currently three possible package schemes:

#### Global

The global scheme is the default scheme. It assumes that there are no (or just one) reseller and just one package. It uses the default value for both reseller and package.

#### TaxonomyTypes

The TaxonomyTypes scheme retrieves the package name by retrieving the taxonomy types of a user. For this purpose it uses a configurable subset of taxonomy types which can be configured via the com.openexchange.advertisement.[reseller].taxonomy.types property. It retrieves the first taxonomy type which matches one type of the configured subset. Therefore it should be avoided to use more than one taxonomy type of the subset for the same user or context.

#### AccessCombinations

The AccessCombinations scheme uses accesscombinationnames as package names. The accesscombinationname is dertermined by the configured module accesses of the user. If this scheme is used it should therefore be avoided to manually change single module accesses for a user.

# Rest API
In order to store and retrieve these configurations a set of rest requests was introduced.

## Extension to the HTTP API

For the normal HTTP API a new module named "advertisement" was introduced. For more information see [http-api](https://documentation.open-xchange.com/latest/middleware/http_api.html).

## Internal rest requests

This section describes the requests which are used by an admin tool to manage the configurations. These requests using basic auth authorization. Therefore in order to use them one has to configure the basic auth credentials first:

com.openexchange.rest.services.basic-auth.login
com.openexchange.rest.services.basic-auth.password

### Set configuration by user id and context id

PUT /advertisement/v1/config/user

**Params:**

| Name   | Description                 |
|:-------|:----------------------------|
| ctxId  | The context id of the user. |
| userId | The user id of the user.    |

**Body:**

A valid json object containing the configuration.

**Response:**

Empty response with HTTP status code 200 or 201 in case a new configuration is created.


### Set configuration by user name and context id

PUT /advertisement/v1/config/name

**Params:**


| Name  | Description                 |
|:------|:----------------------------|
| ctxId | The context id of the user. |
| name  | The user name.              |

**Body:**

A valid json object containing the configuration.

**Response:**

Empty response with HTTP status code 200 or 201 in case a new configuration is created.


### Set configuration by reseller and package

PUT /advertisement/v1/config/package

**Params:**


| Name     | Description              |
|:---------|:-------------------------|
| reseller | The reseller name or id. |
| package  | The package name.        |

**Body:**

A valid json object containing the configuration.

**Response:**

Empty response with HTTP status code 200 or 201 in case a new configuration is created.


### Set multiple configurations by reseller

PUT /advertisement/v1/config/reseller

**Params:**


| Name     | Description              |
|:---------|:-------------------------|
| reseller | The reseller name or id. |

**Body:**

A JSONArray of JSONObjects with the following structure:

    {
    "package": "package1",
    "config": "configdata..."
    }

Setting the config parameter to <code>null</code> will delete the current configuration for the reseller.

**Response:**

An JSONArray of JSONObjects with the following structure:

    {
    "status": CREATED|UPDATED|DELETED|IGNORED|ERROR,
    "error": <error message>
    }


### Delete configuration by user id and context id

DELETE /advertisement/v1/config/user

**Params:**

| Name   | Description                 |
|:-------|:----------------------------|
| ctxId  | The context id of the user. |
| userId | The user id of the user.    |

**Response:**

Empty response with HTTP status code 204.

### Delete configuration by user name and context id

DELETE /advertisement/v1/config/name

**Params:**

| Name  | Description                 |
|:------|:----------------------------|
| ctxId | The context id of the user. |
| name  | The user name.              |

**Response:**

Empty response with HTTP status code 204.

### Delete configuration by reseller and package

DELETE /advertisement/v1/config/package

**Params:**

| Name     | Description              |
|:---------|:-------------------------|
| reseller | The reseller name or id. |
| package  | The package name.        |

**Response:**

Empty response with HTTP status code 204.


# Command-Line Tools

removeadvertisementconfigs

This clt allows the master admin to remove the advertisement configurations. He can either remove all or a single reseller configurations.
It is also possible to remove only the resellers, which are not active anymore (e.g in case they are deleted).

**Help text:**

    -A,--adminuser <arg>         Admin username
    -c,--clean                   If set the clt only removes configurations
                                 of resellers which doesn't exist any more.
    -h,--help                    Prints a help text
    -i,--includePreviews         If set the clt also removes preview
                                 configurations. This is only applicable in
                                 case the argument 'clean' is used.
    -P,--adminpass <arg>         Admin password
    -p,--port <arg>              The optional RMI port (default:1099)
    -r,--reseller <arg>          Defines the reseller for which the
                                 configurations should be deleted. Use
                                 'default' for the default reseller or in
                                 case no reseller are defined. If missing all
                                 configurations are deleted instead.
       --responsetimeout <arg>   The optional response timeout in seconds
                                 when reading data from server (default: 0s;
                                 infinite)
    -s,--server <arg>            The optional RMI server (default: localhost)
