# This file is part of the Salt Edge Authenticator distribution
# (https://github.com/saltedge/sca-identity-service-example)
# Copyright (c) 2019 Salt Edge Inc.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, version 3 or later.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#
# For the additional permissions granted for Salt Edge Authenticator
# under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md

require 'sinatra'
require "sinatra/namespace"
require "sinatra/config_file"
require 'sinatra/reloader' if development?
require 'sinatra/activerecord'

require_relative 'helpers/service_helper'
require_relative 'helpers/qr_helper'
require_relative 'helpers/enroll_helper'
require_relative 'helpers/errors'
require_relative 'helpers/notification_sender'
require_relative 'model/user'
require_relative 'model/connection'
require_relative 'model/authorization'

config_file '../config/application.yml'
enable :sessions
set :bind, '0.0.0.0'
set :show_exceptions, false

helpers Sinatra::ServiceHelper
helpers Sinatra::QrHelper
helpers Sinatra::EnrollHelper

######################### EXAMPLE OF IDENTITY SERVICE API
namespace '/api/authenticator/v1' do
  # Verifies identity and signature
  before do
    next if request.post? && request.path_info.end_with?("/connections") || request.get? && request.path_info.end_with?("/configuration")
    verify_identity
    verify_signature
    content_type :json
  end

  # GET SERVICE PROVIDER CONFIGURATION
  get "/configuration" do
    create_configuration("https://#{request.host_with_port}")
  end

  # CREATE NEW CONNECTION
  # Mobile client query this endpoint after scaning QR code (with Provider info)
  # Create new Connection
  # Return new Connection Id and authentication page connect_url
  post "/connections" do
    new_connection = create_new_connection!
    get_user_authentication_url(new_connection)
  end

  # SHOW CONNECTION
  # Return connection info by access_token
  get "/connections" do
    get_connection
  end

  # REVOKE CONNECTION
  # Mark connection by access_token as revoked
  # Return result of operation
  delete "/connections" do
    revoke_connection!
  end

  # GET ALL ACTIVE (NOT EXPIRED) AUTHORIZATIONS
  # Return list of encrypted and not expired authorizations for connection by access_token
  get "/authorizations" do
    get_encrypted_authorizations
  end

  # GET AUTHORIZATION BY ID
  # Return encrypted and not expired authorization by Id for connection by access_token
  get "/authorizations/:authorization_id" do
    raise StandardError::BadRequest unless params[:authorization_id].present?
    get_encrypted_authorization(params[:authorization_id])
  end

  # CONFIRM/DENY AUTHORIZATION
  # Mark authorization by Id for connection by access_token as Confirmed or Denied
  # Return result of operation
  put "/authorizations/:authorization_id" do
    update_authorization!(params[:authorization_id])
  end
end

######################### EXAMPLE OF ADMIN (or HELPER) SERVICE ROUTES
namespace '/admin' do
  # EXAMPLE OF QR CODE GENERATOR (FOR INTERNAL USE ONLY)
  # Generates QR code with demo provider data
  get '/qr' do
    @configuration_deeplink = create_deep_link("https://#{request.host_with_port}")
    @qr = create_qr_code(@configuration_deeplink)
    erb :qr
  end

  # REVOKE CONNECTION (FOR INTERNAL USE ONLY)
  # Bank Core queries Identity Server to revoke connection by id
  #
  # curl -w "\n" -d "id=1" -X PUT http://localhost:4567/admin/connections/revoke
  put '/connections/revoke' do
    raise StandardError::BadRequest unless params[:id].present?
    Connection.find_by(id: params[:id]).update(revoked: true)

    content_type :json
    { id: params[:id] }.to_json
  end

  # CREATE USER (FOR TEST USE ONLY).
  #
  # curl -w "\n" -d "name=Test&password=test" -X POST http://localhost:4567/admin/users
  post '/users' do
    raise StandardError::BadRequest unless params.values_at(:name, :password).none?(&:blank?)

    user = User.create!(
      name:     params[:name],
      password: params[:password],
    )

    content_type :json
    user.to_json
  end

  # CREATE AUTHORIZATION FOR USER. (FOR INTERNAL USE ONLY)
  # Bank Core queries Identity Server to create new Authorization
  #
  # example of authorization_code = sha256(AMOUNT|CURRENCY_CODE|MERCHANT_ID|MERCHANT_NAME|CURRENT_TIME_STAMP)
  # curl -w "\n" -d "user_id=1&title=Create%20a%20payment&description=550$%20for%20Air%20America&authorization_code=123456789" -X POST http://localhost:4567/admin/authorizations
  post '/authorizations' do
    raise StandardError::BadRequest unless params.values_at(:user_id, :title, :description, :authorization_code).none?(&:blank?)

    authorization = create_new_authorization!(
      params[:user_id],
      params[:title],
      params[:description],
      params[:authorization_code]
    )

    notification_sender = IdentityService::NotificationSender.new(
      "fcm_push_key"              => Sinatra::Application.settings.fcm_push_key,
      "apns_certificate_path"     => Sinatra::Application.settings.apns_certificate_path,
      "apns_certificate_password" => Sinatra::Application.settings.apns_certificate_password
    )

    authorization.user.connections.each { |connection| notification_sender.send(authorization, connection) }
    200
  end
end
######################### EXAMPLE OF WEB LOGIN USED TO AUTHENTICATE USER AND RETURN ACCESS_TOKEN
namespace '/login' do
  # SHOW LOGIN PAGE
  get do
    session['token'] = params['token']
    erb :login_input
  end

  # USER ENTERED LOGIN CREDENTIALS
  post do
    @user_name    = params[:name]
    user_password = params[:password]
    user          = User.find_by(name: @user_name, password: user_password)
    if user.nil?
      erb :login_error
    else
      session['user_id'] = user.id
      erb :login_confirm
    end
  end

  # PROCESS ENROLL FINISH ACTION
  get '/:finish_action' do
    erb :login_error unless session['token'].present? && session['user_id'].present? && params['finish_action'].present?
    user = User.find_by(id: session['user_id'])
    connection = Connection.find_by(connect_session_token: session['token'])
    if user.nil? || connection.nil?
      erb :login_error
    else
      redirect redirect_url(connection, user.id, params['finish_action'])
    end
  end
end

######################### ERROR CATCHER
error Sinatra::BadRequest, BadRequest, AuthorizationRequired,
  AuthorizationNotFound, ConnectionNotFound, UserNotFound,
  SignatureExpired, SignatureMissing, InvalidSignature, JSON::ParserError do
  name = env['sinatra.error'].class.name
  halt 400, {'Content-Type' => 'application/json'}, { "error_class" => name, "error_message" => name }.to_json
end
