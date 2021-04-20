[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html) 
![SDK verson](https://img.shields.io/badge/SDK%20version-1.4.0-orange) 
  
# Salt Edge Authenticator SCA Service Example & SDK (based on Spring Boot)

This codebase is a full-stack application built on Spring Boot Framework. 
Is designated to demonstrate (simulate) SCA flow and communication between ASPSP/Bank and Salt Edge Authenticator Application.  
_This application is just a Proof Of Concept._  
[See how to setup application](#Example-Application-Quick-Setup-and-Run). 

This codebase contains SDK module which is the set of tools for simplification of communication between ASPSP/Bank and Salt Edge Authenticator Application.    
The SDK module implements the Salt Edge Authenticator v1 API.  
[See how to add SDK](#SDK-Integration)  

### Codebase consists of modules:
* **example** - simulates work of ASPSP/Bank Application.  
* **saltedge-sca-service-sdk** - set of tools for integration in existing Spring Applications.

## Application Requirements

1. JDK, at least version 8 
1. Spring Boot Framework, at least version 2.2.+
1. Registration & API Keys. Contact the Salt Edge Authenticator team via the [following link](https://www.saltedge.com/pages/contact_support)   
1. Authenticator Mobile clients supports only SSL connections.

## Example Application Quick Setup and Run
  
1. Clone project
    ```bash
    git clone git@github.com:saltedge/sca-identity-service-example.git
    ```
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
    * Set params for Salt Edge Push service. Ask credentials from Salt Edge Service [**optional**].
      ```yaml
      sca_push_service.url=https://push.service.com/notification
      sca_push_service.app_id=xxxxxxxxx
      sca_push_service.app_secret=yyyyyyyyyyy
      ``` 
1. Run the Example Application
    ```bash
    ./gradlew bootRun
    ```  
      
## How to use example
  
Example Application implements custom admin page for creating users, authorizing, creating connections, creating authorizations. Open in browser: `http://your_host:8080/`.  
  
## SDK Integration
### 1. Add SDK to target application
#### Add as project module
  1. Fork this repository
  2. In IntelliJ IDE Import module `File/New/Import Module...` in your project
  3. Build and run application on target device or emulator
#### Add as Maven dependency  
  Add Salt Edge maven repository to application's build.gradle
  ```groovy
      repositories {
          maven {
              url 'https://raw.github.com/saltedge/sca-identity-service-example/master/maven-repo/'
          }
      }
  ```
  Add Connector SDK dependency to application build.gradle
  ```groovy
      implementation ('com.saltedge.sca.sdk:saltedge-sca-service-sdk:1.x.x') {
          transitive = true
      }
  ```


### 2. Setup application as [described before](#example-application-quick-setup) (add configuration)

### 3. Add SDK package (`com.saltedge.sca.sdk`) to component scan annotation in Application class.  
    ```java
    @SpringBootApplication(scanBasePackages = {CURRENT_APP_PACKAGE, ScaSdkConstants.SDK_PACKAGE})
    @EnableJpaRepositories(basePackages = {CURRENT_APP_PACKAGE, ScaSdkConstants.SDK_PACKAGE})
    @EntityScan(basePackages = {CURRENT_APP_PACKAGE, ScaSdkConstants.SDK_PACKAGE})
    public class ExampleApplication {
       
    }
    ```
### 4. Create a service which will provide info required by SCA SDK Module (Service should implement `ServiceProvider` interface and should have `@Service` annotation):

  * `getProviderConfiguration()` - Provides Service configuration `ScaProviderConfigurationData` designated for connection initiation;  
  * `getAuthorizationPageUrl(String enrollSessionSecret)` - Provides URL of authentication page of Service Provider for redirection in Authenticator app. `enrollSessionSecret` is created by SDK;  
    (**Ignore if REDIRECT authentication is not supported**)  
  * `getUserIdByAuthenticationSessionSecret(String sessionSecret)` - Find User entity by authentication session secret code. 
      Param `sessionSecret` is created by Service Provider and should be created when user already authenticated and need to connect Authenticator App (SDK);
      Return UserIdentity with userId, accessToken and accessTokenExpiresAt. 
      All values are optional.  
  * `getProviderCode()` - Provides code name of Service Provider (e.g demo-bank-code);  
  * `getProviderName()` - Provides human readable name of Service Provider (e.g. Demo Bank). Will be displayed for end customers;  
  * `getProviderLogoUrl()` - Provides logo image of Service Provider. Will be displayed for end customers;  
  * `getProviderSupportEmail()` - Provides email of Service Provider for clients support. Will be displayed for end customers;  
  * `onAuthorizationConfirmed(Authorization authorization)` - Notifies application about confirmation or denying of SCA Authorization;  
  * `onAuthenticateAction(AuthenticateAction action)` - Notifies application about receiving new authenticated Action request. 
    It can be Sign-in to portal action or Payment action which requires authentication;
    Return AuthorizationContent object with `confirmationCode`, `title` and `description`.  
    (**Ignore if Instant Action flow is not supported**)   
    
### 5. Use service `ScaSDKCallbackService` for backward communication between Application and SCA SDK Module. Using of service functions is not mandatory and depends on implemented features (Enrollment, Instant Enrollment, Authorization and Instant Action)  
    
Connections management:   
  * `createConnectAppLink()` - returns App-Link (deep-link) for initiating Enrollment flow in the Authenticator application.    
    (**Ignore if mobile client not supports enrollment initiated by App-Link**);  
  * `createConnectAppLink(authSessionSecret)` - returns App-Link (deep-link) with `authSessionSecret` for initiating Instant Enrollment flow in the Salt Edge Authenticator application;  
    (**Ignore if mobile client not supports enrollment initiated by App-Link**);  
  * `getClientConnections(userId)` - returns all Connections to Authenticators for User. Can be used for further Connections management by Service Provider (e.g. revoking);  
  * `revokeConnection(connectionId)` - invoke for revoking of Authenticator Connection. After that Authenticator will not receive pending Authorizations;  
  * `onUserAuthenticationSuccess(enrollSessionSecret, userId, accessToken, accessTokenExpiresAt)` - should be invoked when REDIRECT authentication flow ends successfully and user should be redirected back to Authenticator app. 
      Returns `ReturnTo Url` with `accessToken` for Authenticator app. 
      Where  `enrollSessionSecret` is unique code of enrollment session provided by `ServiceProvider.getAuthorizationPageUrl()`.
      If accessToken is NULL then SDK will generate random string.
      If accessTokenExpiresAt is NULL then accessToken never expires.
  * `onUserAuthenticationFail(enrollSessionSecret, errorMessage)` - should be invoked when REDIRECT authentication failed and user should be redirected back to Authenticator app. Return `ReturnTo Url` with error;  
    
Authorizations management:    
  * `createAuthorization(userId, confirmationCode, title, description)` - create new Authorization for user with required title, description and confirmationCode, and send push notification about new pending Authorization;  
  * `getAllAuthorizations(userId)` - returns all Authorizations for user;  
  * `getAuthorizationById(authorizationId)` - returns Authorization by ID;    
    
Instant Actions management (**ignore if Instant Action flow is not supported**):    
  * `createAction(code)` - creates an Action entity with required code;  
  * `createActionAppLink(actionUUID)` - return App-Link (deep-link) for initiating Instant Action authentication flow in the Salt Edge Authenticator application;  
  * `getActionByUUID(actionUUID)` - returns Action by `actionUUID`;  
  * `getActionStatus(actionUUID)` - returns Action's status by `actionUUID`;  
    
----
Copyright © 2019 Salt Edge. https://www.saltedge.com  
