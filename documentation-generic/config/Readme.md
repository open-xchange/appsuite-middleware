# How to add a new property

To insert a new property you just have to create or update the corresponding yml file in /documentation-generic/config folder.

The yml file must have the following structure:

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
      related: 
      file:
    - key: c.o.some.property2
      description: |
        line1
        line2
      defaultValue: true
      version: 7.8.0
      reloadable: true
      configcascadeAware: true
      related: c.o.some.property
      file: somefile.properties


If you would like to add a reference to another property in the same file use the following approach:

  * tag the destination property key by using `<a name="com.openexchange.foo">com.openexchange.foo</a>`
  * reference the tagged property by adding it to the 'related' column like `<a href="#com.openexchange.foo">com.openexchange.foo</a>`

