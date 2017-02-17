#MailFilter Interception Hook

## Summary

The mail filter interception hook provides a simple mechanism that allows the interception, further processing and modification of structured mail filter rule objects outside the OX middleware's stack. The interceptor hooks are available both right after the read operation but before any processing has happened by the middleware and before the write operation but after the rules were being processed by the middleware. That provides the flexibility to custom implementations to scan the sieve scripts for certain rules before they get written back to the Sieve server and perform any modifications.

## Building Blocks

The interception framework consists out of a `MailFilterInterceptor` interface which provides three methods:

 * `void before(List<Rule> rules) throws OXException`
 * `void after(List<Rule> rules) throws OXException`
 * `int getRank()`

The `before` method is being invoked right after the sieve script is read from the Sieve server, but BEFORE any processing begins on the OX middleware.

The `after` method is being invoked before the sieve script is written back to the Sieve server, and AFTER any processing has happened on the OX middleware.

The `getRank` method returns the rank of the interceptor. The rank defines the execution order within a chain of multiple interceptors. Higher values are prefered over lower values, which means interceptor with high values are executed before interceptors with lower values. If there are interceptors with equal ranks, then their lexicographical order is being used.

All interceptors are registered in to an OSGi `MailFilterInterceptorRegistry`. The registry also provides three methods:

 * `void register(MailFiliterInterceptor interceptor)`
 * `void executeBefore(List<Rule> rules) throws OXException`
 * `void executeAfter(List<Rule> rules) throws OXException`

The `register` method, as the name suggests, registers the specified interceptor to the registry. The rank of the interceptor is taken into consideration.

The `executeBefore` and `executeAfter` methods are similar to the interceptor methods; they simply execute the interceptor chain in the order provided by the interceptor ranks.

The interceptors can abort any processing via OXExceptions.

## Example

There are two things needed to utilise the interceptor framework.

First, to implement a custom `MailFilterInterceptor:

```java
public class CustomMailFilterInterceptor implements MailFilterInterceptor {
    
    public CustomMailFilterInterceptor() {
        super();
    }

    @Override
    public int getRank() {
        return 50;
    }

    @Override
    public void before(List<Rule> rules) throws OXException {
        for (Rule rule : rules) {
            // do stuff
        }
    }

    @Override
    public void after(List<Rule> rules) throws OXException {
        for (Rule rule : rules) {
            // do stuff
        }
    }
}
```

and then register it via the registry service.

```java
...
MailFilterInterceptor interceptor = new CustomMailFilterInterceptor();
MailFilterInterceptorRegistry registry = serviceLookup.getService(MailFilterInterceptorRegistry.class);
registry.register(interceptor);
...

```

And that's all!