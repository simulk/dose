# dose
Netty based file server that supports weak and strong etag

Starting point of the application is `Bootstrap.main`

To start the application, properties configuration file is required. You can either
1. provide a `application.properties` in the classpath.
2. set `dose.config.location` as `System property`

#### What properties to set in `application.properties`

> `base.download.folder`:       location of folder to serve the files from   (required property)

> `server.port`:                Port for **DOSE** to listen on. Default is **8080**

> `etag.enabled`:               **true** by default. Set to false to disable etag

> `etag.type`:                  **weak** by default. Set to strong to enable strong etag generation

> `strong.etag.hash.algorithm`  **MD5** by default. Other supported algorithms are **SHA** and **SHA256**



### More information:

https://tools.ietf.org/html/rfc7232#section-2.3

https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag
