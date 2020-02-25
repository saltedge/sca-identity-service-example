[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)
# Authenticator Identity Service Sinatra Example

## Prerequisites

In order to build Salt Edge Authenticator Identity Service Example, it is necessary to install the following tools:

* Ruby 2.4 or greater  

* sqlite3  
	```
	dpkg -s sqlite3
	```
	or
	```
	apt-get install sqlite3 libsqlite3-dev
	```

* Bundler  
	```
	gem install bundler
	```

* Web server (e.g. nginx) or tunnel (e.g. ngrok)  
  
* [Docker (optional)](https://www.docker.com/)  

***Note:*** Authenticator Mobile clients supports only SSL connections.  

## Get source code

Fork this repository  

## Run Sinatra example

1. Change directory:
	```
	cd sinatra-example
	```

1. Run Bundler to pull the required dependencies:
	```
	bundle install
	```

1. Init database configuration
    * Create `config/database.yml` (`cp config/database.example.yml config/database.yml`)
    * Change configuiration if you don't need default one.
    * Run Rake task to initiate database `bundle exec rake db:migrate`

1. Authenticator Identity Service can send push notifications to mobile clients. 
    * Create `config/application.yml` (`cp config/application.example.yml config/application.yml`)
    * For activating fill required fields (optional)

1. Create database (for the first time)
	```
	bundle exec rake db:migrate
	```
	or drop before
	```
	bundle exec rake db:drop
	```

1. Run application
	```
	bundle exec ruby app/service.rb
	```

## Run Sinatra example in Docker container

1. Change directory:
	```
	cd sinatra-example
	```

1. Init database configuration
    * Create `config/database.yml` (`cp config/database.example.yml config/database.yml`)
    * Change configuiration if you don't need default one.
    * Run Rake task to initiate database `bundle exec rake db:migrate`

1. Authenticator Identity Service can send push notifications to mobile clients. 
    * Create `config/application.yml` (`cp config/application.example.yml config/application.yml`)
    * For activating fill required fields (optional)

1. Build Docker image:
	```
	docker build -t authenticator/sinatra .
	```

1. Run Docker container and application:
	```
	docker run --rm -p 4567:4567 -v "$(pwd)":/sinatra-example/ --name service authenticator/sinatra
	```

1. Create database and run migrations (for the first time)
	in new terminal window
	```
	docker exec -ti service bash
	```
	and run mugrations in docker container
	```
	bundle exec rake db:migrate
	```
	or drop before
	```
	bundle exec rake db:drop
	```

## How to use Sinatra example
  
1. Create new user (if needed)  
	```
	curl -w "\n" -d "name=Test&password=test" -X POST http://your_host:4567/admin/users
	```

1. Connect mobile client  

1. Create test authorization  
	```
	curl -w "\n" -d "user_id=1&title=Create%20a%20payment&description=550$%20for%20Air%20America&authorization_code=123456789" -X POST http://your_host:4567/admin/authorizations
	```

_Described above functions are available on Admin page `http://your_host:4567/admin`_
  
----
Copyright © 2019 Salt Edge. https://www.saltedge.com  
