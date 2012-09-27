# Dynamic Include Filter

## Purpose

The purpose of the module presented here is to replace dynamic generated components (eg. current time or foreign exchange rates) with server-side include tag (eg. [SSI](http://httpd.apache.org/docs/current/howto/ssi.html) or [ESI](http://www.w3.org/TR/esi-lang)). Therefore the dispatcher is able to cache the whole page but dynamic components are generated and included with every request. Components to include are chosen in filter configuration using `resourceType` attribute.

When the filter intercepts request for a component with given `resourceType`, it'll return a server-side include tag (eg. `<!--#include virtual="/path/to/resource">` for Apache server). However the path is extended by new selector (`nocache` by default). This is required because filter has to know when to return actual content.

Components don't have to be modified in order to use this module (or even aware of its existence). It's servlet filter, installed as an OSGi bundle and it can be enabled, disabled or reconfigured without touching CQ installation.

## Prerequisites

* CQ / Apache Sling 2
* Maven 2.x, 3.x

## Installation

Automated:

* `mvn package sling:install` - build and install bundle on CQ instance running at http://localhost:4502
* `mvn package sling:install -Dsling.url=http://192.168.0.1:4503/system/console -Dsling.username=admin -Dsling.password=123` - build and install using custom server properties

Manual:

* `mvn package`
* open Felix console and install sling-caching-filter-<version>.jar bundle manually

## Configuration

Filter is delivered as a standard OSGi bundle. There are following configuration options:

* Enabled - enable SDI
* Filter selector - selector used to get actual content
* Resource types - which components should be replaced with tags
* Include type - type of include tag (Apache SSI, ESI or Javascript)
* Default extension - extension added with selector if there is no other extension
* Add comment - adds debug comment: `<!-- Following component is included by DynamicIncludeFilter (path: %s ) -->` to every replaced component
* Skip requests with params - disables filter for request with parameters (eg. POST or index.html?parameter=1)
* Only included - disables filter for direct HTTP requests for components. Eg. carousel component on the page will be replaced with the include tag, but if you paste [component URL](http://localhost:5403/content/geometrixx/en/_jcr_content/carousel.html) into your browser, you'll get content
* Externalize links - use `resourceResolver.map(...)` to externalize links. If disabled, only namespace parts (like `jcr:content`) will be escaped (`_jcr_content`).

## Compatibility with components

Filter is incompatible with following types of component:

* components which handles POST requests or GET parameters,
* components which has to be requested without any extension (because if there is no extension, default `.html` is added by filter),
* synthetic components which uses suffixes (because suffix is used to pass `requestType` of the synthetic resource).

If component do not generate HTML but eg. JS or binary data then remember to turn off *Comment* option in configuration.

## Enabling SSI in Apache & dispatcher

In order to enable SSI in Apache with dispatcher first enable `Include` mod (on Debian: `a2enmod include`). Then add `Includes` option to the `Options` directive in your virtual configuration host. After that find following lines in `dispatcher.conf` file:

        <IfModule dispatcher_module>
            SetHandler dispatcher-handler
        </IfModule>

and modify it:

        <IfModule dispatcher_module>
            SetHandler dispatcher-handler
        </IfModule>
        SetOutputFilter INCLUDES

You can also use second configuration directive and use following:

        <IfModule dispatcher_module>
            SetHandler dispatcher-handler
        </IfModule>
        AddOutputFilter INCLUDES .html

A problem with this configuration is that dynamic components won't be included if requested page is not in cache (eg. it's requested for the first time). It may also not display pages with suffixes (as Apache doesn't recognize suffix in address).

After setting output filter open virtualhost configuration and add `Includes` option to `Options` directive:

        <Directory />
            Options FollowSymLinks Includes
            AllowOverride None
        </Directory>
        <Directory /var/www/>
            Options Indexes FollowSymLinks MultiViews Includes
            AllowOverride None
            Order allow,deny
            allow from all
        </Directory>

It's also a good idea to disable the caching for `.nocache.html` files in `dispatcher.any` config file. Just add:

        /disable-nocache
        {
            /glob "*.nocache.html*"
            /type "deny"
        }

at the end of the `/rules` section.

## Enabling ESI in Varnish

Just add following lines at the beginning of the `vcl_fetch` section in `/etc/varnish/default.vcl` file:

        if(req.url ~ "\.nocache.html") {
            set beresp.ttl = 0s;
        } else if (req.url ~ "\.html") {
            set beresp.do_esi = true;
        }

It'll enable ESI includes in `.html` files and disable caching of the `.nocache.html` files.

## JavaScript Include

Dynamic Include Filter can also replace dynamic components with AJAX tags, so they are loaded by the browser. It's called JSI. In the current version jQuery framework is used. More attention is required if included component has some Javascript code. Eg. Geometrixx Carousel component won't work because it's initialization is done in page `<head>` section while the component itself is still not loaded.

## Plain and synthetic resources

There are two cases: the first involves including a component which is available at some URL (eg. `/content/geometrixx/en/jcr:content/carousel.html`). In this case, component is replaced with include tag, and `nocache` selector is added `<!--#include virtual="/content/geometrixx/en/jcr:content/carousel.nocache.html">`. If the filter gets request with selector it'll pass it (using `doChain`) further without taking any action.

![Plain include](https://raw.github.com/Cognifide/Sling-Dynamic-Include/master/src/main/doc/plain-include.png)

There are also components which are created from so-called synthetic resources. Synthetic resource have some resourceType and path, but they don't have any node is JCR repository. An example is `/content/geometrixx/en/jcr:content/userinfo` component with `foundation/components/userinfo` resource type. These components return 404 error if you try to make a HTTP request. SDI recognizes these components and forms a different include URL for them in which resource type is added as a suffix. For example: `/content/geometrixx/en/jcr:content/userinfo.nocache.html/foundation/components/userinfo`. If filter got such request, it'll try to emulate `<sling:include>` JSP tag and includes resource with given type and `nocache` selector (eg. `/content/geometrixx/en/jcr:content/userinfo.nocache.html`). Selector is necessary, because otherwise filter would again replace component with a SSI tag.

# Commercial Support

Technical support can be made available if needed. Please [contact us](https://www.cognifide.com/get-in-touch/) for more details.

We can:

* prioritize your feature request,
* tailor the product to your needs,
* provide a training for your engineers,
* support your development teams.
