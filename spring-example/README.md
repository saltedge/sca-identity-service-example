[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)
# Authenticator Identity Service Spring Boot Example

## Prerequisites

In order to build Salt Edge Authenticator Identity Service Example, it is necessary to install the following tools:

* Run `gradle build` from terminal to build app

* Web server (e.g. nginx) or tunnel (e.g. ngrok)  

***Note:*** Authenticator Mobile clients supports only SSL connections.  

## Get source code

Fork this repository  

## Run Spring Boot example

1. Change directory:
	```
	cd spring-example
	```

1. Init database configuration
    * Set fields in `src/main/resources/application.properties`

1. Authenticator Identity Service can send push notifications to mobile clients. 
    * Create `src/main/resources/config/application.properties` 
    (`cp src/main/resources/config/application_properties.example src/main/resources/config/application.properties`)
    * For activating fill required fields (optional)

1. Run application
	```
	java -jar artifactname
	```

## How to use example

In example is implemented custom admin page for creating users, connections, authorizations
  
1. Create new user
1. Connect mobile client  
1. Create test authorization  

_Described above functions are available on Admin page `http://your_host:4567/admin`_
  
----
Copyright © 2019 Salt Edge. https://www.saltedge.com  
