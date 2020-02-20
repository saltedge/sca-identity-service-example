require 'sinatra'
require "sinatra/namespace"
require "sinatra/config_file"
require 'sinatra/reloader' if development?
require 'sinatra/activerecord'

require_relative './helpers/service_helper'
require_relative 'helpers/qr_helper'
require_relative 'helpers/enroll_helper'
require_relative 'helpers/errors'
require_relative 'helpers/notification_sender'
require_relative './model/action'
require_relative './model/user'
require_relative './model/connection'
require_relative './model/authorization'

require_relative 'controllers/user_authorize_controller'
require_relative 'controllers/dashboard_controller'

config_file '../config/application.yml'
enable :sessions
set :bind, '0.0.0.0'
set :show_exceptions, false

helpers Sinatra::ServiceHelper
helpers Sinatra::QrHelper
helpers Sinatra::EnrollHelper

######################### ERROR CATCHER
error Sinatra::BadRequest, BadRequest, AuthorizationRequired,
  AuthorizationNotFound, ConnectionNotFound, UserNotFound,
  SignatureExpired, SignatureMissing, InvalidSignature, JSON::ParserError do
  name = env['sinatra.error'].class.name
  halt 400, {'Content-Type' => 'application/json'}, { "error_class" => name, "error_message" => name }.to_json
end

use DashboardController
run UserAuthorizeController
