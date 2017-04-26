# Introduction

This document describes all infos needed to document properties. 
In order to preserve the same style for all properties, the styling informations given here should be followed.

# How to add a new property

To insert a new property you just have to create or update the corresponding yml file in the /documentation-generic/config folder.

The yml file contains a feature_name (which is the name of the feature the properties belong to (unique!)), an optional feature_description (which describes the feature in detail) and an array of properties. Each property contains the following keys:

* key - The name of the property
* description - A description of the property.
* defaultValue - The default value (Defining no value is allowed)
* version - The version the property was first introduced (if known)
* reloadable - Whether the property is reloadable
* configcascadeAware - Whether the property is config cascade aware
* related - An optional single property name (key) or an array of properties names which are directly related (E.g. c.o.feature.login and c.o.feature.password). 
* file - The file in which the property should be specified. (This is a required field)
* packageName - The package which contains this property
* tags - An array of tags which are related to the property

For example:
```yml
feature_name: Feature XYZ
feature_description: |
  This pages describes feature XYZ
properties:
    - key: c.o.some.property
      description: |
        line1
        line2
        line3
      defaultValue: true
      version: 7.8.3
      reloadable: true
      configcascadeAware: false
      related: com.openexchange.some.property
      file:
      packageName: open-xchange-core
      tags:[]
    - key: c.o.some.property2
      description: |
        line1
        line2
      defaultValue: true
      version: 7.8.0
      reloadable: true
      configcascadeAware: true
      related: ["com.openexchange.some.property1","com.openexchange.some.property2"]
      file: somefile.properties
      packageName: open-xchange-packageB
      tags: ["tagA","tagB"]
```

# Tags

Whenever a new property is introduced a list of tags should be added too. Here are some rules that should be followed:

* The tag array must contain the feature name of the yml file.
* Dont use different cases for tag names like: 'Rest' and 'REST'
* Check for existing tags before you introduce an own tag
* Check at least if one of these tags match your property:
 * Limit - All properties which define a max value
 * Security - All properties which are related to security issues. E.g. encryption
 * Credential - All properties which define credentials. E.g. login and password fields.
 * Cache - All properties which configures caches
 * "Black List" or "White List" - a property which defines a black- or white-list.
 * Database - All database related properties

# Description styling guide

This chapter provides recommendations about how a property description should be formatted.

Some general guidelines:

* Don't describe default values in the description field, so if someone has to change the default value he doesn't need to change the description too.
* Don't forget to use punctuation.
* If you want to add a note or warning or something similar please use the following form:

```
Some text here

Note:  
Note to leave in the next line with a simple linebreak before.

```
* If you want to reference the property itself use "this property" or something similar instead of the property name.
* If you use the property name of another property write the complete property name (key) surrounded by two square brackets. E.g. [[com.openexchange.feature.property]]
* You also link to html pages this way. You only need to start the link with either "http://" or "https://".
* You can also use `{{version}}` as a placeholder for the current version. E.g.:
```
[[https://documentation.open-xchange.com/{{version}}/middleware/components/saml.html]]
```
will be replace for 7.8.4 with:
```
<a href=https://documentation.open-xchange.com/7.8.4/middleware/components/saml.html>
https://documentation.open-xchange.com/7.8.4/middleware/components/saml.html
</a>
```


## Linebreaks

To make the yml documentation more human readable it is recommended that the property descriptions are described with multiple lines.
Since the layout of the final documentation is unkown to the author of the property the normal linebreaks used in the yml files can't be 
transfered to the final layout. Instead a syntax similar to markdown is used. There are only two rules:

* If you want to add a normal linebreak add two spaces to the end of a row.
* If you want to add a double linebreak add a empty line instead.

All other linebreaks are ignored. Please note that some html elements like the `<pre>` tag also add linebreaks to the layout.

## Use of html

The property documentation is a html based website with makes use of javascript. Therefore it is possible to use normal html syntax.
This also means, that smaller than (<) and greater than (>) signes must be replaced with html entities. E.g.:  `&lt;`

Here are some guideline to follow to retain a equal style throughout the hole property documentation:

* Always define possible values in html `<code>` tags
* If you want to define a bigger list of possible values make use html lists like this:

```
Possible values:
<ul>
 <li><code>Value1</code> - Some description1</li>
 <li><code>Value2</code> - Some description2</li>
 <li><code>Value3</code> - Some description3</li>
 <li><code>Value4</code> - Some description4</li>
</ul>
```

* If you want to define a example configuration use the `<pre>` html tags and use the complete property names, not just the value.
* If your description contains some sort of chapters use the h3 html tag (<h3>) for the headings.
* You want to emphasize something you can use the normal bold html tag (<b>).



