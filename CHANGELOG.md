# Changelog

# [2.3.0](https://github.com/clarin-eric/fcs-sru-client/releases/tag/SRUClient-2.3.0) - 2024-12-06

- Additions:
  - Add context map and send flag to the authentication workflow to allow for more fine-grained requesting logic by holding request specific attributes, e.g. Resource availability restriction etc.  
    (primarily required for FCS SRU Aggregator that uses a shared client for all endpoints/resources)
    - `authenticationContext` map and `sendAuthentication` flag in `SRUAbstractRequest`
    - evaluation of `sendAuthentication` flag in `SRUSimpleClient#executeRequest` with forwarding the `authenticationContext` map
    - context map parameter for `SRURequestAuthenticator#createAuthenticationHeaderValue` function (for custom implementations)

- Dependencies:
  - Bump `org.slf4j` to `2.0.16`
  - Bump `org.apache.httpcomponents:httpclient` to `4.5.14`

# [2.2.1](https://github.com/clarin-eric/fcs-sru-client/releases/tag/SRUClient-2.2.1) - 2024-02-02

- Dependencies:
  - Add `maven-release-plugin`
  - Bump Maven build plugin versions
  - Bump `org.slf4j` to `1.7.36`

# [2.2.0](https://github.com/clarin-eric/fcs-sru-client/releases/tag/SRUClient-2.2.0) - 2024-02-01

- Additions:
  - Add `parseToDocument(XMLStreamReader)` helper method to `XmlStreamReaderUtils`.
    Allows to use DOM/XPath based parsing on subtrees from `XMLStreamReader`s.
  - Add [Github Pages](https://clarin-eric.github.io/fcs-sru-client/) with [JavaDoc](https://clarin-eric.github.io/fcs-sru-client/project-reports.html)
  - Add Changelog document

  * Add _experimental_ support for processing authenticated requests

- Changes:
  - Disable DTD and parsing of external entities (OWASP XXE)
  - Re-factor to make SRUOperation available

- General:
  - Update copyright
  - Cleanup


For older changes, see commit history at [https://github.com/clarin-eric/fcs-sru-client/commits/main/](https://github.com/clarin-eric/fcs-sru-client/commits/main/?after=1c6c7e0b75f393cd34527af918878ff21cccb821+0&branch=main&qualified_name=refs%2Fheads%2Fmain)
