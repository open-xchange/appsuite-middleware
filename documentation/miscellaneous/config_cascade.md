---
title: ConfigCascade
icon: fa-cogs
tags: Config, LeanConfiguration, Configuration, ConfigCascade, ConfigurationService
---

# Introduction

The config cascade is a configuration system that allows administrators to selectively override configuration parameters on context and user level. This means a configuration option can vary between groups of contexts, specific contexts or users.

## Who Should Read This Document

If you are tasked with designing and maintaining the configuration of an OX server or cluster, information contained in this document will acquaint you with options in OX configuration design.

# Core Concepts

## Configuration Scope

The config cascade differentiates between 5 scopes of configuration: `server`, `contextSets`, `reseller`, `context` and `user`, with the latter always overriding the configuration of the one before it. To determine the active value of a certain parameter, the config cascade looks whether the parameter is defined in a certain scope before falling back to the next scope to the left to determine if a value is defined there. This means, that a value in the User scope can override the more general value from the `context` scope, which in turn overrides the value of a context set configuration, which itself overrides a server wide configuration.

## Context taxonomy

When deciding on configuration options it usually makes sense to group contexts according to a certain criterion. Typical uses would be to group contexts by offering (`webmail`, `groupware_standard`, `groupware_plus`), or country (`de`, `fr`, `es`) or brand (`coolhosting`, `supremehosting`) or if they are part of a "friendly users" group you sometimes give access to features to deem whether they are appropriate for rollout (beta). You can then specify configuration options that only take effect if a context is part of one of these groups. For example, the default hostname varies by both country and brand, with the french `coolhosting` domain name being "coolhosting.fr", while the spanish one is "coolhosting.es", or, for the second brand "supremehosting.fr" or "supremehosting.es" respectively. How can you go about classifying a context?

Using the command line tools you can specifiy the taxonomy/types parameter:

```bash
createcontext ... -i 12 --taxonomy/types=webmail,coolhosting,de
```

which would tag context `12` with the types `webmail`, `coolhosting` and `de`. This is also available in `changecontext`. In RMI the equivalent is to call `Context#setUserAttribute("taxonomy", "types", "webmail,coolhosting,de")`. We will later see how configuration options can be specified for these types of contexts.

# Specifying Configuration

## Server Scope

The most general scope is the `server` scope. Every value that can be overridden along the config cascade MUST also be defined with a default value in the `server` scope. This is done using the usual configuration methods of the server: `.properties` files in the config directory (usually `/opt/openexchange/etc`, or `/opt/openexchange/etc/groupware` in versions up to 6.20.7). Let's consider the properties `com.openexchange.messaging.twitter`, which governs whether twitter messaging should be available in a given installation. Since we consider this to be a premium feature, we'll disable this on the server level:

```properties
 $ cat twittermessaging.properties:
 com.openexchange.messaging.twitter=false
```

Later we will see how to enable it for certain groups of contexts.

## Context Set Scope

As we saw, you can classify contexts into groups. These groups will now be used to specify certain configuration options. Let's consider this setup:

```yaml
 Context 12: webmail,de,beta
 Context 13: groupware_plus,es
 Context 14: groupware_plus,fr,beta
```

Let's say, we want to roll out the twitter functionality to those contexts, that have the groupware_plus product and are part of our "friendly users". For this, you can specify a configuration that overrides the server setting like this:

Create a file called `/opt/openexchange/etc/contextSets/messaging.yml` and add the following block:

```yaml
  experimental_gw_plus:
      withTags: groupware_plus & beta
      com.openexchange.messaging.twitter: true
```

Let's go through this line by line. The first line introduces a configuration block that will be used for certain contexts. The name doesn't matter, only insofar as that it may only be used once per file. Choose a good mnemonic here, so a future you or someone else can guess at what is going on in this configuration block.

The second line specifies the criterion to use to find out whether a context belongs in this group of contexts. In this case, a context having both the `groupware_plus` and `beta` tags will be considered to be a part of this group. In the `withTags` expression you can use boolean logic (with `&` for and, `|` for or and brackets to group the expressions). It's best to not go overboard with this, though. If the boolean expressions here become too complex it's usually an indication that you could use another classification for the contexts. Which tags does a context have? Firstly, and most obviously, those specified as its taxonomy/types list. But that is not the whole story. The `/users/` module access permissions are also transformed into tags and applied to the context (at runtime). So, if a user has access to the tasks module and the infostore module, the context will be considered to be tagged with `ucTask` and `ucInfostore` as well. And last keyword which can be used as a tag is the context name. This is sometimes enough to determine if a context is part of a certain offering, but more explicit tagging of contexts according to the offering keeps things readable. Lastly the configuration parameter `com.openexchange.config.cascade.types` (which is itself config cascade enabled) adds its value to the tag list, so, for example:

```yaml
  friendly_users:
      withTags: groupware_plus & beta
      com.openexchange.config.cascade.types: friendly_and_paying
```

Would add the `friendly_and_paying` tag to all contexts already classified as `groupware_plus` and `beta`. Also since this value can also be specified on user level, you could classify users irrespective of their contexts, should the need arise.

The third line then specifies the setting to override. You can specify all properties to override in this block, so if we wanted to enable both twitter and rss messaging for these contexts, we'd use the following configuration:

```yaml
  experimental_gw_plus:
      withTags: groupware_plus & beta
      com.openexchange.messaging.twitter: true
      com.openexchange.messaging.rss: true
```

Most configuration use cases can probably be handled with the context sets system. Only if a configuration is truly unique for just one context or user should the other options be pursued.

## Reseller Scope

OX Cloud offers a level "above" the `context` to support different resellers. Certain configuration might be related in OX App Suite to this level and this is not yet supported. Therefore, with 7.10.5, a new scope was introduced, i.e. the `reseller` scope to bridge the gap between the `context` and `contextSets` scopes.

For this to work, the `open-xchange-reseller` package must be installed. If so, the config cascade lookup order goes  from most to least specific: user (API) -> context (API) -> reseller (API) -> contextSet (Configuration) -> server (Configuration) -> default (Code).

If a reseller does not have a value for a given config, then its parents (if exist) are iterated until a value is available. If no value can be found then the next scope down the line will be used, i.e. `contextSets`.

To set properties, taxonomies and capabilities in the `reseller` scope, the `createadmin` and `changeadmin` command line tools can be used:

```bash
$ createadmin [...] --config/com.openexchange.oauth.twitter=false --config/com.openexchange.oauth.google=attributes --capabilities-to-add "portal, -autologin" --taxonomy/types=some-taxonomy
$ changeadmin [...]  --config/com.openexchange.oauth.google=attributes --remove-config/com.openexchange.oauth.twitter --capabilities-to-add "portal" --capabilities-to-drop "autologin" --taxonomy/types=some-taxonomy --remove-taxonomy/types=hosting
```

Note that the difference between `remove` and `drop` in capabilities is that with remove the capability will be removed from a set if it was set in a less narrower scope, see "-" prefix; while with drop the entry will be erased from the database table and its default behavior will be restored.

No operations are allowed on properties starting with `com.openexchange.capability.` and no capabilities are allowed that match a user permission bit.

## Context Scope and User Scope

Configuration options can be overridden on user and context level, using a dynamic property. For example:

```bash
$ createcontext [...] --config/com.openexchange.messaging.twitter=true
$ changecontext [...] --config/com.openexchange.messaging.twitter=true

$ createuser [...] --config/com.openexchange.messaging.twitter=true
$ changeuser [...] --config/com.openexchange.messaging.twitter=true
```

Depending on the number of users and contexts in your system, this could pose a problem further down the road when you need to update this value for a large number of users.

To remove such a setting again the following syntax can be used:

```
$ changecontext [...] --remove-config/com.openexchange.messaging.twitter
$ changeuser [...] --remove-config/com.openexchange.messaging.twitter
```

## UI Properties

A common use case for the OX configuration system is to allow fine-tuning of the UI by providing configuration data on the backend. All properties defined in properties files below `/opt/open-xchange/etc/groupware/settings` are transported to the UI and are config cascade enabled. So every customization you can specify for the UI using these settings, can also be selectively overridden with the config cascade.

Since the config cascade only overrides existing settings, whether a property is a UI property or a server property is automatically determined by the directory in which the corresponding .properties file is found. For example if `/opt/open-xchange/etc/settings/appsuite.properties` contains the setting

```properties
io.ox/core//theme=default
```

Then you can overwrite it for any context (or user, context set, etc.):

```bash
$ changecontext [...] --config/io.ox/core//theme=org.example.theme
```

The values are first parsed using JSON syntax. If that fails, they are interpreted as plain strings.

# Further reading

[ConfigCascadeCookbook](https://www.oxpedia.org/wiki/index.php?title=ConfigCascadeCookbook) - Collects typical configuration scenarios and how to handle them using the config cascade.
