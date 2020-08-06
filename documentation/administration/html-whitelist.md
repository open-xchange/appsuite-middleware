---
title: HTML white-list
icon: fa-filter
tags: Administration, HTML, Whitelist, Configuration
---

# Introduction

To prevent different potential phishing attacks and harmful HTML content there is a white-list filter that lists allowed HTML tags as well as allowed attributes for each HTML tag. The same is availble for allowd CSS elements and allowed values for each CSS element contained in an HTML document.

That HTML/CSS white-list filter is managed through configuration file `/opt/open-xchange/etc/whitelist.properties`.

## HTML tags and attributes

The listing of allowed HTML tags and associated attributes starts at section `# HTML tags and attributes` in file `/opt/open-xchange/etc/whitelist.properties`.

Syntax is:
```
'html.tag.' + <tag-name> + '=' + '"' + <comma-separated-attribute-names>? + '"'
```

An attribute name may further specify the values that are allowed for associated attribute in square brackets delimited by colons; e.g.

```
html.tag.br=",clear[:left:right:all:none:]"
```

An empty attribute listing means that all attributes are allows; e.g.

```
html.tag.i=""
```
(allows HTML tag `i` (italic) with arbitrary attributes)

## CSS elements and values

The listing of allowed CSS elements and associated values starts at section `# CSS key-value-pairs.` in file `/opt/open-xchange/etc/whitelist.properties`.

Syntax is:
```
'html.style.' + <element-name> + '=' + '"' + <comma-separated-values>? + '"'
```

Instead of listing exact values for a CSS element, there is also the possibility to specify placeholders that represent a certain range of values. Thereof:

 * `c` Any CSS color value
 * `u` An URL; e.g. `url(http://www.somewhere.com/myimage.jpg);`
 * `n` Any CSS number value without `'%'` character
 * `N` Any CSS number value
 * `*` Any value allowed
 * `d` delete
 * `t` time

Such placeholders are expected to be at first position in value listing. A combination of multiple placeholders is allowed, too.

The following example allows CSS element `text-shadow` and specifes the placeholder combination `nc` to allowa any value that represents a CSS color or any CSS number value without `'%'` character as well as concrete value `none `:

```
html.style.text-shadow="nc,none,"
```

In opposite to previous HTML tag syntax and empty listing refers to style's combi-map, which is listed at section `# CSS combi-map` in file `/opt/open-xchange/etc/whitelist.properties`.

Syntax is:
```
'html.style.combimap.' + <element-name> + '=' + '"' + <comma-separated-values>? + '"'
```

While the first position in value listing is a combination of placeholders; e.g.

```

# CSS key-value-pairs.
...
html.style.background="" <-- The empty value indicates a reference to style's combi-map
...

# CSS combi-map
...
html.style.combimap.background="uNc,scroll,fixed,transparent,top,bottom,center,left,right,repeat,repeat-x,repeat-y,no-repeat,radial-gradient,"

```