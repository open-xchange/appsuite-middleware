---
title: Properties Overview
---

# Introduction

With release 7.8.3 Open-Xchange starts to use predefined values for properties stored in the code and to prevent adding the properties to files contained in /opt/open-xchange/etc and its folders below. The administrator is (of course) able to change the provided defaults by adding the property to the file of his choice (below /opt/open-xchange/etc). Because of this change this page describes (mainly) new properties that aren't visible  in a file but can be configured by the administrator.

All properties will be described by using the layout of the table below:

| Key | Description | Default | Version | Related | File |
| :---         |     :---      |          :---: | :---: | :--- |:--- |
| `com.openexchange.foo`   | Defines the foo     | true    | 7.8.3 |  `com.openexchange.bar`| |
| `com.openexchange.bar`   | Defines the bar     | false    | 7.8.0 |  `com.openexchange.foo`| foobar.properties|

These information are contained within the columns have the following meaning:

  * **Key**: The key of the property. This key has to be added to the file of the administrators choice to overwrite the default value.
  * **Description**: A short description of the property.
  * **Default**: The default value of the property (as defined within the code).
  * **Version**: The first version the property is available with.
  * **Related**: Contains information about other properties that are related to the currently described one.
  * **File**: Describes the file where the property is defined. This column mainly exists for properties that have been available before 7.8.3 and are contained within a file.

## New properties

Please structure the insertion of new properties based on its category and add a new section if not yet available (e. g. 'sharing', 'database', 'mail' and so on).

If you would like to add a reference to another property use the following approach:

  * tag the destination property key by using `<a name="com.openexchange.foo">com.openexchange.foo</a>`
  * reference the tagged property by adding it to the 'related' column like `<a href="#com.openexchange.foo">com.openexchange.foo</a>`

# Properties

<span style="color:red">The following entries are just examples. Remove them when adding existing properties!</span>

## Database (example)

| Key | Description | Default | Version | Related | File |
| :---         |     :---      |          :---: | :---: | :--- |:--- |
| <a name="com.openexchange.foo">`com.openexchange.foo`</a>   | Defines the foo     | true    | 7.8.3 |  `com.openexchange.bar`| |
| `com.openexchange.bar`   | Defines the bar     | false    | 7.8.0 |  `com.openexchange.foo`| foobar.properties|

## Mail (example)

| Key | Description | Default | Version | Related | File |
| :---         |     :---      |          :---: | :---: | :--- |:--- |
| `com.openexchange.foo`   | Defines the foo     | true    | 7.8.3 | `com.openexchange.bar`| |
| `com.openexchange.bar`   | Defines the bar     | false   | 7.8.0 | <a href="#com.openexchange.foo">`com.openexchange.foo`</a>| foobar.properties|
