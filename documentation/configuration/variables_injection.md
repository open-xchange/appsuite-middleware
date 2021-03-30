---
title: Injecting variables into Open-Xchange Middleware configuration
icon: fas fa-globe
tags: Configuration
---

# Introduction

With version v8.0.0 the Open-Xchange Middleware allows injecting variables into the configuration of the Middleware.

# Injecting through tokens

It is possible to specify tokens or placeholders directly in .properties, .yml and .yaml configuration files nested in the `/opt/open-xchange/etc` directory (and sub-directories respectively); except files:

* `/opt/open-xchange/etc/external-domains.properties`
* `/opt/open-xchange/etc/HTMLEntities.properties`

A placeholder begins with the character sequence `"{{"` and it ends with character sequence `"}}"`.

The notation accepts an optional source identifier, the actual variable name (required) and an optional default value.

The complete syntax is:

```
"{{" + <source-id> + " " + <variable-name> + ":" + <default-value> + "}}"
```

The source identifier tells from what source to look-up the given variable name and the default value defines the value to use in case no such variable is available in looked-up source.

## Supported source identifiers

* `"env"` determines that system environment is supposed to be look-ed up for a certain variable name. Furthermore, it is assumed as default in case no source identifier is present.
* `"file"` sets that a certain .properties file is supposed to be look-ed up. Moreover, this identifier expects specification of the actual .properties file in surrounding arrow brackets; e.g. `"file<tokenvalues.properties>"`. That .properties file is supposed to reside in the `/opt/open-xchange/etc` directory or any of its sub-directories.
* Any other source identifier refers to a programmatically registered instance of `com.openexchange.config.VariablesProvider` in case a custom source needs to be used.

## Examples

Given that `"PROP1=value1"` has been set as environment variable and there is a file `/opt/open-xchange/etc/tokenvalues.properties` with content:

```
# Content of tokenvalues.properties
com.openexchange.test.value1=mytokenvalue
```

Specifying a .properties file with the following content:

```
# "env" used as default source, no default value
com.openexchange.test.token1={{PROP1}}

# No default value
com.openexchange.test.token2={{env PROP1}}

# "env" used as default source, with "defaultforprop2" as default value
com.openexchange.test.token3={{PROP2:defaultforprop2}}

# Fully qualifying notation with source, variable name and default value
com.openexchange.test.token4={{env PROP3:defaultforprop3}}

# Look-up property "com.openexchange.test.value1" in file "tokenvalues.properties"
com.openexchange.test.token5={{file(tokenvalues.properties) com.openexchange.test.value1}}

# Look-up property "com.openexchange.test.value2" in file "tokenvalues.properties", use "foobar" as default value
com.openexchange.test.token6={{file(tokenvalues.properties) com.openexchange.test.value2:foobar}}
```

The result used by Middleware would be:

```
com.openexchange.test.token1=value1
--> Use value "value1" from environment variable "PROP1"

com.openexchange.test.token2=value1
--> Use value "value1" from environment variable "PROP1"

com.openexchange.test.token3=defaultforprop2
--> No such environment variable "PROP2", therefore use default value

com.openexchange.test.token4=defaultforprop3
--> No such environment variable "PROP3", therefore use default value

com.openexchange.test.token5=mytokenvalue
 --> Read "mytokenvalue" as value for "com.openexchange.test.value1" from file "tokenvalues.properties"

com.openexchange.test.token6=foobar
--> Since no such property "com.openexchange.test.value2" available in file "tokenvalues.properties", the default value is used
```

# Injecting through system environment through fall-back names

In addition to the token injection approach, system environment variables can be set named according to a deducible identifier from a property name. This approach only works for settings available in .properties files; except files:

* `/opt/open-xchange/etc/external-domains.properties`
* `/opt/open-xchange/etc/HTMLEntities.properties`

The deducible fully-qualifying identifier for a property name is:

```
<file-identifier> + "__" + <property-identifier>
```

The `<file-identifier>  + "__"` portion is optional and only useful in case the property identifier is not unique, so that the concrete .properties file is needed to uniquely identify the property to inject/replace.

The deducible `<file-identifier>` is the lower-case notation of the respective file name with any non-digit and non-letter characters replaced with `"_"` underscore character.<br>
(Multiple subsequent `"_"` characters are folded into one to not mess with the "__" delimiter sequence.)

The deducible `<property-identifier>` is the upper-case notation of the respective property name with any non-digit and non-letter characters replaced with `"_"` underscore character.<br>
(Multiple subsequent `"_"` characters are folded into one to not mess with the "__" delimiter sequence.)

For instance

* The identifier for `com.openexchange.test.myproperty` is `COM_OPENEXCHANGE_TEST_MYPROPERTY`
* The identifier for `io.ox/core//theme` is `IO_OX_CORE_THEME`<br>
  (Note: `"//"` sequence folded into one `"_"`)
* The fully-qualifying identifier for `MAX_UPLOAD_SIZE` in file `attachment.properties` is `attachment_properties__MAX_UPLOAD_SIZE`

So, whenever there is an existent variable in system environment for such a variable name, that variable's value is primarily chosen as property value.

## Examples

Given that `"COM_OPENEXCHANGE_INFOSTORE_FEATURE_ENABLED=true"` as well as `"infostore_properties__MAX_UPLOAD_SIZE=10485760"` have been set as environment variables. And there is an `/opt/open-xchange/etc/infostore.properties` file with the following content:

```
# Content of infostore.properties
MAX_UPLOAD_SIZE=5242880
```

The result used by Middleware would be:

```
com.openexchange.infostore.feature.enabled=true
--> Use value "true" from environment variable "COM_OPENEXCHANGE_INFOSTORE_FEATURE_ENABLED"

MAX_UPLOAD_SIZE=10485760
--> Use value "10485760" from environment variable "infostore_properties__MAX_UPLOAD_SIZE"
```
