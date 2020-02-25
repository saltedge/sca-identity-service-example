[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)
# Salt Edge Authenticator SCA Service Example & SDK (based on Spring Boot)

This codebase was created to demonstrate a full-stack application built on Spring Boot, 
designated to demonstrate SCA flow and communication between Service Provider and Salt Edge Authenticator Application. 
Current application implements Salt Edge Authenticator v1 API.  
Consists of modules:
* **Example Application**
* **SCA Service SDK v1**

## Application Requirements

1. JDK, at least version 8 
1. Spring Boot Framework, at least version 2.2.+
1. Registration & API Keys. Contact the Salt Edge Authenticator team via the [following link](https://www.saltedge.com/pages/contact_support)   
1. Authenticator Mobile clients supports only SSL connections.

## Example Application Quick Setup
  
1. Clone project
1. Navigate to project's root folder
    ```bash
    cd sca-identity-service-example/saltedge-sca-service-spring
    ```
1. Create configuration files
    ```bash
    cp example/src/main/resources/application.example.properties example/src/main/resources/application.properties
    ```  
1. Edit configuration files (`application.properties`)  
    * Example application uses a H2 in memory database (for now), can be changed easily for any other database type.
    * Set external host name for application
      ```yaml
      app.url=http://123456789.ngrok.io
      ```
    * Set external host name for SCA Service (can be equal to host name for application) 
      ```yaml
      sca_service.url=http://123456789.ngrok.io
      ```
    * Set params for Salt Edge Push service. Ask credentials from Salt Edge Service.
      ```yaml
      sca_push_service.url=https://push.service.com/notification
      sca_push_service.app_id=xxxxxxxxx
      sca_push_service.app_secret=yyyyyyyyyyy
      ``` 

## SDK Integration

1. Add SDK to target application as Module or as JAR library (`out/saltedge-sca-service-sdk-1.0.0-all.jar`);
1. Setup application as [described before](#example-application-quick-setup)
1. Add SDK package (`com.saltedge.sca.sdk`) to component scan annotation in Application class.
    ```java
    @SpringBootApplication(scanBasePackages = {EXAMPLE_PACKAGE, SDK_PACKAGE})
    @EnableJpaRepositories(basePackages = {EXAMPLE_PACKAGE, SDK_PACKAGE})
    @EntityScan(basePackages = {EXAMPLE_PACKAGE, SDK_PACKAGE})
    public class ExampleApplication {
       public static final String SDK_PACKAGE = "com.saltedge.sca.sdk";
       
    }
    ```
1. Create a service which will provide info required by SCA Service:
    * Service should implement `ServiceProvider` interface and should have `@Service` annotation;
    * `findUserIdByAuthorizationSessionSecret(...)` should return unique identifier of User (Customer) of application;
    * `getAuthorizationPageUrl()` should return URL of authorization page for Authenticator enroll flow;
    * `getProviderCode(...)` should return unique text code of Service Provider (Bank) (e.g. "example-bank");
    * `getProviderName(...)` should return name of Service Provider (Bank) (e.g. "Example Bank");
    * `getProviderLogoUrl(...)` should return logo image url of Service Provider (Bank);
    * `getProviderSupportEmail(...)` should return e-mail of support service of Service Provider (Bank);
1. Use for callback service `ScaSdkService`:  
    * `getClientConnections(userId)` returns all connections with authenticators for user;
    * `revokeConnection(connectionId)` should be invoked in case when Authenticator Connection should be revoked;
    * `createConnectAppLink()` returns App-Link (Deep-Link) for initiating enrollment flow in the Salt Edge Authenticator application;
    * `createConnectAppLink(authSessionSecret)` returns App-Link (Deep-Link) with `authSessionSecret` for initiating enrollment flow in the Salt Edge Authenticator application;
    * `createAuthorization(userId, title, description)` returns new Authorization for user with required title and description, and send notification about new authorization;
    * `getAuthorizations(userId)` returns all Authorizations for user;
    * `onUserAuthenticationSuccess(authSessionSecret, userId)` should be invoked when oAuth authentication flow ends successfully and user should be redirected back to app. Return ReturnTo Url with new accessToken; 
    * `onUserAuthenticationFail(authSessionSecret, errorMessage)` should be invoked when oAuth authentication failed and user should be redirected back to app. Return ReturnTo Url with error;
    * `createAction(code)` creates an Action entity with required code and returns it;
    * `getActionByUUID(actionUUID)` returns Action by `actionUUID`;
    * `getActionStatus(actionUUID)` returns Action's status by `actionUUID`;
    * `createActionAppLink(actionUUID)` return App-Link (Deep-Link) for initiating Action authentication flow in the Salt Edge Authenticator application;
    

## How to use example

In example is implemented custom admin page for creating users, authorizing, creating connections, authorizations
`http://your_host:8080/`  
  
----
Copyright © 2019 Salt Edge. https://www.saltedge.com  
