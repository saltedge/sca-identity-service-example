require 'sinatra'
require 'sinatra/namespace'
require 'sinatra/config_file'
require 'sinatra/reloader' if development?
require 'sinatra/activerecord'
require 'pry'
require 'yaml'

require_relative 'app/models/action'
require_relative 'app/models/user'
require_relative 'app/models/connection'
require_relative 'app/models/authorization'
require_relative 'app/helpers/service_helper'
require_relative 'app/helpers/qr_helper'
require_relative 'app/helpers/enroll_helper'
require_relative 'app/helpers/errors'
require_relative 'app/helpers/notification_sender'
require_relative 'app/controllers/base_controller'
require_relative 'app/controllers/user_authorize_controller'
require_relative 'app/controllers/dashboard_controller'
require_relative 'app/controllers/sca_controller'

enable :sessions
set :bind, '0.0.0.0'
set :port, 4567

helpers Sinatra::ServiceHelper
helpers Sinatra::QrHelper
helpers Sinatra::EnrollHelper

APP_SETTINGS = OpenStruct.new(YAML.load_file('config/application.yml')[settings.environment.to_s])
