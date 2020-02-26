require 'sinatra'
require "sinatra/namespace"
require "sinatra/config_file"
require 'sinatra/reloader' if development?
require 'sinatra/activerecord'
require 'pry'
require 'yaml'

require_relative './helpers/service_helper'
require_relative 'helpers/qr_helper'
require_relative 'helpers/enroll_helper'
require_relative 'helpers/errors'
require_relative 'helpers/notification_sender'
require_relative './model/action'
require_relative './model/user'
require_relative './model/connection'
require_relative './model/authorization'

require_relative 'controllers/base_controller'
require_relative 'controllers/user_authorize_controller'
require_relative 'controllers/dashboard_controller'
require_relative 'controllers/sca_controller'

enable :sessions
set :bind, '0.0.0.0'

helpers Sinatra::ServiceHelper
helpers Sinatra::QrHelper
helpers Sinatra::EnrollHelper

APP_SETTINGS = OpenStruct.new(YAML.load_file('config/application.yml')[settings.environment.to_s])

use DashboardController
use SCAController
run UserAuthorizeController
