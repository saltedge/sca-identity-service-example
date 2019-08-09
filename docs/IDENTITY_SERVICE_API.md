# Identity Service

* [What is Identity Service](#what-is-identity-service)
* [Identity Service Models](#identity-service-models)
* [Deep link](#deep-link)
* [Public API](#identity-service-api)
  * [API Data Types](#api-data-types)
  * [API Security](#api-security)
  * [Errors](#errors)
  * [Get configuration](#get-service-provider-configuration)
  * [Connect to Service Provider](#connect-to-service-provider)
  * [Obtain access token](#obtain-access-token)
  * [Revoke Access token](#revoke-access-token)
  * [Show Authorizations List](#show-authorizations-list)
  * [Show Authorization](#show-authorization)
  * [Confirm or Deny Authorization](#confirm-or-deny-authorization)
* [Authorization code builder example](#authorization-code-builder-example)


## What is Identity Service
  
[Identity Service WIKI](https://github.com/saltedge/sca-identity-service-example/wiki)  
  
The purpose of Identity Service is to process and store customer identities, roles, credentials, and add the necessary functionality in order to implement SCA. Hence, besides standard functionality, in Identity Service should be implemented additional functionalities and an API, as an extension to the existing Identity Service. The extension (additional functionality and API) processes received from Core banking information and appeals to the Authenticator app via Authentication Service in order to get either action confirmation or denial from the customer.

There is a little effort from Service Provider in order to implement and extension Identity Service with 6 API end-points: 
* Connect to Service Provider; 
* Revoke Access Token; 
* Show Authorizations List; 
* Show Authorization; 
* Confirm or Deny Authorization.  
and add authentication/enrollment flow for `Obtaining Access Token`
  
Salt Edge has developed the SCA solution with such an architecture, having Identity Service in infrastructure of Service Provider, in order not to have any access to the personal information/credentials of the customer. All related to the customer private data is isolated from any third party or application and is controlled exclusively by the Service Provider.

Besides public API, may be implemented next util endpoints for internal usage (non-public):
* Revoke connection. For some reasons (security reason, cleaning job, etc.) Bank (Service Provider) should have possibility to revoke any connection
* Create new authorization. Identity service should create new authorization with data from request from Bank Core (`authorization_code`, `title`, `description`, `user_id`), and send notifications (through Push Service) about new authorization to related connections.

## Identity Service Models

![schema](images/authenticator_models_schema.png)

### User model
`User` represents an abstract single customer of Bank's Identity Service (e.g  Service Provider). Bank already has it

### Connection (Mobile Client) model
`Mobile Client` represents a single connection between `User` and  Mobile Application. Each `User` can have multiple `Mobile Client`'s because user can have many connections to a single Service Provider from different applications.
  
- `id` - a unique ID string
- `user_id` - id of related user model
- `public_key` - a unique Asymmetric Public Key in PEM format string
- `push_token` - a unique token string which uniquely identify mobile application for Push Notification system (i.e. unique address of current mobile application instance)
- `access_token` - a unique token string for authenticated access to API resources
- `return_url` - a URL the mobile application will be redirected to at the end of the authentication process
- `connect_session_token` - a unique token string for authentication session process
- `revoked` - a boolean value which indicate that connection is active or revoked
- `created_at` - a datetime
- `updated_at` - a datetime

### Authorization model
`Authorization` represents a single action for user. Each `User` can have multiple `Authorization`'s.
  
- `id` - a unique ID string
- `user_id` - id of related user model
- `expires_at` - a datetime which indicates period of validity of authorization action
- `title` - title of authorization action
- `description` - description of authorization action
- `authorization_code` - generated unique code per each authorization action based on set of input information (datetime, amount, payee, account, etc.)
- `confirmed` - a boolean value which indicate that Authorization was confirmed or denied
- `created_at` - a datetime
- `updated_at` - a datetime


### Log model
- `id` - a unique ID string
- `message` - logged message as JSON
- `signature` - signed message 
- `public_key` - a unique Asymmetric Public Key in PEM format string
- `created_at` - a datetime

### Push Server Configuration
 - `push_server_url` - a URL of Push Server
 - `push_server_app_id` - a unique token string, released by Push Server owner
 - `push_server_app_secret` - a unique token string, released by Push Server owner

***Optional***, if implemented Push Service inside Identity Service.

## Deep link

For initiating connect flow, service should generate deep-link for initiating connection in mobile application. Deep-link can be encoded as QR code. Deep-link should contain link to configuration endpoint.  
``` 
  authenticator://saltedge.com/connect?configuration=https://saltedge.com/configuration
```  

---
## Identity Service API

### API Data Types
The following section describes the different data types used for request and response data.

[JSON format](https://restfulapi.net/json-data-types/) is using for request/response data formating. Since all data is eventually represented as UTF-8 strings, these types mostly define what characters are considered valid for data of a specific type. Additional validation rules may apply for specific parameters.  
Several primitive types:
**Boolean** - A case insensitive Boolean value, represented as either `true` or `false`.

**Integer** - An integer number. For example: `123`

**String** - A string of characters. For example: `"any string"`

**TimeStamp** - The time and date represented in ISO 8601 format (e.g. `2017-04-19T13:53:31Z`). The time and date must always be represented in the GMT time zone, even if the server or client uses a different default time zone.

**Array** - An array of values. Arrays are encoded by adding brackets.  For example: `"data": ["1", "2", "3"]`

**Object/Hashmap/Dictionary** - A associative array of values. For example: `"data": { id: "1", connection_id: "333" }`
  
### API Security
The following section describes the different security approaches used for securing data flow.

####  Encryption
All sensitive data is encrypted using 2048-bit keys.  
All communications should be performed over HTTPS channels

#### Signature
All requests should be signed (if possible). 
Before all, service client should provide public key (Mobile Application to Identity Service or Identity Service to Push Service). The private key should be securely stored on client side.
In case of possible security breach, the private key should be regenerated and the public key should be updated.

There are several common points about the request we send:
- The `Content-Type` header is  `application/json`;
- There is a `Signature` header that identifies the request was signed;
- The JSON object sent will always have a  `data` field;

#### Signature Headers
The following headers are required for your request to be considered signed:
-   `Expires-at` - request expiration time as a UNIX timestamp in UTC timezone. We suggest to use +1 minute from the current time. The maximum value is 1 hour from now in UTC, otherwise  `SignatureExpired` error will be raised;
-   `Signature`  -  `base64`  encoded  `SHA256`  signature of the string represented in the form  
`request_method|original_url|Expires-at|post_body`  
4 parameters concatenated with a vertical bar  `|`, signed with the client’s private key.

Example:
```
  Expires-at: "2017-04-19T13:53:31Z"
  Signature: "0H6xaZ8g67....H8="
```  
 
The fields  `request_method`, `original_url` and `post_body` from the `Signature` header represent:  
- `request_method` - lowercase method name of the HTTP request. Example: `get`, `post`, `put`, `delete`, etc.;
- `original_url`   - the full requested URL, with all its complementary parameters;
- `Expires-at`     - request expiration time as a UNIX timestamp in UTC timezone, should be equal to `Expires-at` header parameter;
- `post_body`      - the request post body. Should be left empty if it is a GET request, or the body is empty;

### Errors

Each authenticated endpoint can return next errors:
* `BadRequest` - some of request params not valid.
* `AuthorizationRequired` - `access_token` param is missing.
* `SignatureExpired` - `expires_at` param is missing or `expires_at` is before now.
* `SignatureMissing` - `signature` param is missing.
* `InvalidSignature` - `signature` param is invalid.
* `ConnectionNotFound` - connection associated with by `access_token` param not found
* `AuthorizationNotFound` - Authorization queried by `authorization_id` param not found

---
### Get Service provider configuration   
Public resource (not authenticated) for fetching of initial data of Service Provider.
Included in [deep-link](#qr-code). Endpoint can be arbitrary and not in Authenticator API namespace (`/api/authenticator/v1/`)
  
`GET` `configuration url from deep-link`

```bash
curl \
  -H 'Content-Type: application/json' \
  -X GET \
  https://connector.service_host.com/configuration
```

#### Response parameters
- `connect_url`   **[string, required]** - base url of the Identity Service
- `code`          **[string, required]** - code of the Service Provider
- `name`          **[string, required]** - name of the Service Provider
- `logo_url`      **[string, optional]** - url of the Service Provider's logo asset
- `support_email` **[string, optional]** - email address of Provider's Customer Support
- `version`       **[string, required]** - required Authenticator API version

#### Example response
```json  
{
  "data": {
    "connect_url": "https://connector.service_host.com",
    "code": "demobank",
    "name": "Demobank",
    "logo_url": "https://connector.service_host.com/assets/logo.png",
    "support_email": "support@your_host.com",
    "version": "1"
  }
}
```
---
### Connect to Service Provider
Init the new Mobile Client (i.e. Service Connection) and return Connect Url for future user authentication.

`POST` `/api/authenticator/v1/connections`  

```bash
curl \
  -H 'Content-Type: application/json' \
  -X POST \
  -d '{ "data": { "public_key": "-----BEGIN PUBLIC KEY-----\nMIGfMAGCSqGSIAB\n-----END PUBLIC KEY-----\n", "return_url": "authenticator://oauth/redirect", "platform": "android", "push_token": "e886d1a84cfa3cd5343b70a3f9971758e" } }' \
  https://connector.service_host.com/api/authenticator/v1/connections
```
  
#### Request Headers 
- `Accept-Language` **[string, optional]** - advertises which locale variant is preferred by client. By default `en`; 
  
#### Request Parameters
- `public_key` **[string, required]** - a unique Asymmetric Public Key linked to the new Mobile Client (Connection) in PEM format 
- `return_url` **[string, required]** - a URL the mobile application will be redirected to at the end of the authentication process
- `platform` **[string, required]** - mobile platform's name (e.g.  `android` or `ios`)
- `push_token` **[string, optional]** - a token which uniquely identify mobile application for Push Notification system (e.g. Firebase Cloud Messaging, Apple Push Notifications) (i.e. unique address of current mobile application instance). Sometimes is not available for current application.

#### Request Example
```json
{
  "data": {
    "public_key": "-----BEGIN PUBLIC KEY-----\nMIGfMAGCSqGSIAB\n-----END PUBLIC KEY-----\n",
    "return_url": "authenticator://oauth/redirect",
    "platform": "android",
    "push_token": "e886d1a84cfa3cd5343b70a3f9971758e"
  }
}
```

#### Response Parameters
- `connect_url` **[string]** - an url of Connect Web Page for future end-user authentication
- `id` **[string]** - an unique id of current connection model.

#### Response Example
```json
{
  "data": {
    "connect_url": "https://connector.service_host.com/oauth/dbcde9971758e",
    "id": "333"
  }
}
```
---
### Obtain access token
User should open `connect_url` and pass authentication procedure.
On authentication flow finish, client (WebView on Mobile Application) will be redirected to url which should start with `return_url` ((passed on Connect)[#connect-to-service-provider]) and extra params. Once client has captured the redirect url, it has to deserialize the JSON-encoded URL path following the custom scheme and the host.

#### Redirect Parameters of successful authentication
- `id` **[string, required]** - a unique id of Connection
- `access_token` **[string, required]** - a unique token for future authenticated access to API resources

#### Example of successful authentication
```
  authenticator://oauth/redirect?id=333&access_token=Oqws977brjJUfXbEnGqHNsIRl8PytSL60T7JIsRBCZM
```
*The URL will be URLEncoded (percent-encoded),  the URL above is not URLEncoded to preserve it’s readability.*

#### Redirect Parameters of failed authentication
- `error_class` **[string, required]** - a class name which describe occurred error.
- `error_message` **[string, required]** - a human-readable error message

#### Example of failed authentication
```
  authenticator://oauth/redirect?error_class=WRONG_CREDENTIALS&error_message=Wrong login or password
``` 
*The URL will be URLEncoded (percent-encoded), the URL above is not URLEncoded to preserve it’s readability.*

---
### Revoke Access token
Invalidates a mobile client by `Access-Token` (in header).  
  
`DELETE` `/api/authenticator/v1/connections`  
  
```bash
curl \
  -H 'Content-Type: application/json' \
  -H 'Access-Token: replace_with_your_token' \
  -H 'Expires-at: expires_at_time' \
  -H 'Signature: generated_signature' \
  -X DELETE \
  https://connector.service_host.com/api/authenticator/v1/connections
```
  
#### Request Headers 
- `Accept-Language` **[string, optional]** - advertises which locale variant is preferred by client. By default `en`; 
- `Access-Token` **[string, required]** - access token, required to access resources which require authentication. 
- `Expires-at` **[datetime, required]** - expires at datetime stamp, required to access resources which verify request signature.
- `Signature` **[string, required]** - signed by Asymmetric Key string, required to access resources which verify request signature.

#### Response Parameters
- `success` **[boolean]** - result of deletion
- `access_token` **[string]** - revoked access token

#### Response Example 
```json
{
  "data": {
    "success": true,
    "access_token": "Oqws977brjJUfXbEnGqHNsIRl8PytSL60T7JIsRBCZM"
  }
}
```
---
### Show Authorizations List
Return list of all current Authorizations which require end-user confirmation for Service Provider by `Access-Token` from headers.  
Each Authorization's `data` (authorization data) is encrypted with algorithm mentioned in `algorithm` param. Necessary data for decryption (`key` and `iv`) are encrypted by asymmetric `public_key` sent on (creating new connection)[#connect-to-service-provider] earlier.  

`GET` `/api/authenticator/v1/authorizations`  
  
```bash
curl \
  -H 'Content-Type: application/json' \
  -H 'Access-Token: replace_with_your_token' \
  -H 'Expires-at: expires_at_time' \
  -H 'Signature: generated_signature' \
  -X GET \
  https://connector.service_host.com/api/authenticator/v1/authorizations
```
  
#### Request Headers 
- `Accept-Language` **[string, optional]** - advertises which locale variant is preferred by client. By default `en`; 
- `Access-Token` **[string, required]** - access token, required to access resources which require authentication. 
- `Expires-at` **[datetime, required]** - expires at datetime stamp, required to access resources which verify request signature.
- `Signature` **[string, required]** - signed by Asymmetric Key string, required to access resources which verify request signature. 

#### Response Body Parameters
- `id` **[string]** - a unique id of authorization model
- `connection_id` **[string]** - a unique ID of Mobile Client (Service Connection). Used to decrypt models in the mobile application
- `iv` **[string]** - an initialization vector of encryption algorithm, this string is encrypted with public key linked to mobile client
- `key` **[string]** - an secure key of encryption algorithm, this string is encrypted with public key linked to mobile client
- `algorithm` **[string]** - encryption algorithm and block mode type
- `data` **[string]** - encrypted authorization payload with algorithm mentioned above

#### Response Example
```json
{
  "data": [
    {
      "id": "444",
      "connection_id": "333",
      "iv": "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
      "key": "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
      "algorithm": "AES-256-CBC",
      "data": "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
    }
  ]
}
```

#### Authorization Payload Parameters (Decrypted payload)  
- `id` **[string]** - a unique id of authorization model
- `connection_id` **[string]** - a unique ID of Mobile Client (Service Connection). Used to decrypt models in the mobile application
- `title` **[string]** - a human-readable title of authorization action
- `description` **[string]** - a human-readable description of authorization action
- `authorization_code` **[string]** - a unique code for each operation (e.g. payment transaction), specific to the attributes of operation, must be used once
- `created_at` **[datetime]** - time when the authorization was created
- `expires_at` **[datetime]** - time when the authorization should expire

#### Authorization Payload Example (Decrypted payload)  
```json
{
  "id": "444",
  "connection_id": "333",
  "title": "Create payment",
  "description": "Create payment 111.0 EUR for ...",
  "authorization_code": "123456789",
  "created_at": "2017-09-22T08:29:03Z",
  "expires_at": "2017-09-22T08:34:03Z"
}
```

---
### Show Authorization
Return the one authorization which require end-user confirmation for Service Provider by `Access-Token` from headers and by `id` parameter.
Each Authorization's `confirmation_data` is encrypted with algorithm mentioned in `algorithm` param. Necessary data for decryption (`key` and `iv`) are encrypted by asymmetric `public_key' sent on (creating new connection)[#connect-to-service-provider] earlier.

`GET` `/api/authenticator/v1/authorizations/:authorization_id` 
  
```bash
curl \
  -H 'Content-Type: application/json' \
  -H 'Access-Token: replace_with_your_token' \
  -H 'Expires-at: expires_at_time' \
  -H 'Signature: generated_signature' \
  -X GET \
  https://connector.service_host.com/api/authenticator/v1/authorizations/444
```
  
#### Request Path Parameters
- `authorization_id` **[string, required]** - a unique code of authorization model 

#### Request Headers 
- `Accept-Language` **[string, optional]** - advertises which locale variant is preferred by client. By default `en`; 
- `Access-Token` **[string, required]** - access token, required to access resources which require authentication. 
- `Expires-at` **[datetime, required]** - expires at datetime stamp, required to access resources which verify request signature.
- `Signature` **[string, required]** - signed by Asymmetric Key string, required to access resources which verify request signature.  

#### Response Parameters
- `id` **[string]** - a unique code of authorization model  
- `connection_id` **[string]** - a unique ID of Mobile Client (Service Connection). Used to decrypt models in the mobile application
- `iv` **[string]** - an initialization vector of encryption algorithm, this string is encrypted with public key linked to mobile client
- `key` **[string]** - an secure key of encryption algorithm, this string is encrypted with public key linked to mobile client
- `algorithm` **[string]** - encryption algorithm and block mode type
- `data` **[string]** - encrypted authorization payload with algorithm mentioned above

#### Response Example
```json
{
  "data": {
    "id": "444",
    "connection_id": "333",
    "iv": "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
    "key": "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
    "algorithm": "AES-256-CBC",
    "data": "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
  }
}
```

#### Authorization Payload Parameters (Decrypted payload)  
- `id` **[string]** - a unique id of authorization model
- `connection_id` **[string]** - a unique ID of Mobile Client (Service Connection). Used to decrypt models in the mobile application
- `title` **[string]** - a human-readable title of authorization action
- `description` **[string]** - a human-readable description of authorization action
- `authorization_code` **[string]** - a unique code for each operation (e.g. payment transaction), specific to the attributes of operation, must be used once
- `created_at` **[datetime]** - time when the authorization was created
- `expires_at` **[datetime]** - time when the authorization should expire

#### Authorization Payload Example (Decrypted payload)  
```json
{
  "id": "444",
  "connection_id": "333",
  "title": "Create payment",
  "description": "Create payment 111.0 EUR for ...",
  "authorization_code": "123456789",
  "created_at": "2017-09-22T08:29:03Z",
  "expires_at": "2017-09-22T08:34:03Z"
}
```

---
### Confirm or Deny Authorization
Confirm/Denies authorization model (e.g. payment, operation, etc) from Service Provider.  
Requests for Confirm and Deny are practicaly equal. The only difference in the value of `confirm` field.
For `Confirm`, field `confirm` should be `true`. For `Deny`, field `confirm` should be `false`.  

`PUT` `/api/authenticator/v1/authorizations/:authorization_id`  
  
```bash
curl \
  -H 'Content-Type: application/json' \
  -H 'Access-Token: replace_with_your_token' \
  -H 'Expires-at: expires_at_time' \
  -H 'Signature: generated_signature' \
  -X PUT \
  -d '{ "data": { "confirm": true, "authorization_code": "123456789" } }' \
  https://connector.service_host.com/api/authenticator/v1/authorizations/444
```
  
#### Request Path Parameters
- `authorization_id` **[string, required]** - a unique code of authorization model  

#### Request Headers 
- `Accept-Language` **[string, optional]** - advertises which locale variant is preferred by client. By default `en`; 
- `Access-Token` **[string, required]** - access token, required to access resources which require authentication. 
- `Expires-at` **[datetime, required]** - expires at datetime stamp, required to access resources which verify request signature.
- `Signature` **[string, required]** - signed by Asymmetric Key string, required to access resources which verify request signature.  

#### Request Body Parameters
- `confirm` **[boolean, required]** - Confirm (`true`) or Deny (`false`) authorization
- `authorization_code` **[string, required]** - a unique code for each action

#### Request Example
```json
{
  "data": {
    "confirm": true,
    "authorization_code": "123456789"
  }
}
```  

#### Response Parameters
- `success` **[boolean]** - result of confirm/deny operation. 
- `id` **[string]** - a unique id of authorization model

#### Response Example 
```json
{
  "data": {
    "sucess": true,
    "id": "444"
  }
}
```  
----

## Authorization code builder example
```ruby
Base64.encode64(Digest::SHA256.hexdigest("#{payee_details}|#{amount}|#{time}|#{user_id}|#{description}|#{salt}"))
```  
  
----  
Copyright © 2019 Salt Edge. https://www.saltedge.com
