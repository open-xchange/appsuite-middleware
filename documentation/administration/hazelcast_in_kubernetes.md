---
title: Hazelcast in kubernetes
icon: fas fa-cloud
tags: Administration, Kubernetes, Hazelcast
---

With 7.10.5 it is possible to deploy the appsuite middleware within a kubernetes cluster. For this to work the hazelcast kubernetes plugin has been integrated. 
This plugin provides a discovery mechanism which allows hazelcast instances to find other hazelcast nodes by using kubernetes resources.
The plugin is shipped with the normal open-xchange-hazelcast package. In addition to the usual hazelcast configuration you only need to configure two properties and a kubernetes service for this to work. For example:

```properties
com.openexchange.hazelcast.network.join=kubernetes
com.openexchange.hazelcast.network.join.k8s.serviceName=middleware-hazelcast
```

```yaml
apiVersion: v1
kind: Service
metadata:
  name: middleware-hazelcast
spec:
  type: LoadBalancer
  selector:
    app.kubernetes.io/name: middleware
  ports:
    - name: hazelcast 
      port: 5701
```

Please note that the selector of the service must match all nodes with hazelcast instances.