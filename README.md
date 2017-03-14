# README
## Requirements
* Java 8
* Maven 3

## Build & run
* run local HTTP server (e.g. via `python -m SimpleHTTPServer 8080`)
* run `mvn spring-boot:run`
* for help, run `mvn spring-boot:run -Drun.arguments="--help"`
* for different domain, run `mvn spring-boot:run -Drun.arguments="--url http://mydomain.com"`

## Tests
* in order to run tests, first start HTTP server in `html` folder
* you can use `python -m SimpleHTTPServer 8080` on Mac or Linux
* for discussion about this limitation, please see class `CrawlerTest`
* I did not integrated embedded Tomcat properly due to time constraints
* normally I would start Tomcat in the integration test with simple, unified way (probably with @ClassRule, @Runner, or something)

## Restrictions & notes
* there are many restrictions in this implementation, please follow the code and read JavaDocs
* most of them are critical, so they need to be addressed for real world usage
* this application runs only against limited test data. Proper implementation would take much more time.
* if you believe I should address one or more limitations I mentioned (or forgot to mention), please let me know
* the exercise has been done on many assumptions and shortcuts, which I would normally discuss with you before coding
