# This file is part of the Salt Edge Authenticator distribution
# (https://github.com/saltedge/sca-identity-service-example)
# Copyright (c) 2020 Salt Edge Inc.

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, version 3 or later.

# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

# For the additional permissions granted for Salt Edge Authenticator
# under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md

require 'pry'

class UserAuthorizeController < BaseController
  get '/' do
    erb :index
  end

  get '/users/sign_in' do
    @action = create_action()

    @instant_action_deeplink = create_instant_action_deep_link(@action.uuid, "", "https://#{request.host_with_port}")

    @qr = Sinatra::QrHelper.create_qr_code(@instant_action_deeplink)

    erb :sign_in
  end

  get '/actions/status' do
    action_uuid = params[:uuid]

    action = Action.find_by(uuid: action_uuid)

    raise StandardError::ActionNotFound if action.nil?

    response = {
      "action_status" => action.status
    }

    if action.status == Action::CONFIRMED
      response[:redirect] = "/admin/connections?user_id=#{action.user_id}"
    end

    content_type :json
    response.to_json
  end

  get '/payments/status' do
    action = Action.find_by(uuid: params[:uuid])

    raise StandardError::ActionNotFound if action.nil?

    username = "UNKNOWN"

    if user = User.find_by(id: action.user_id)
      username = user.name
    end

    response = {
      "action_status" => action.status,
      "username"      => username
    }

    content_type :json
    response.to_json
  end

  get '/payments/order' do
    @action = create_action(require_sca: true)

    @instant_action_deeplink = create_instant_action_deep_link(@action.uuid, "", "https://#{request.host_with_port}")

    @qr = Sinatra::QrHelper.create_qr_code(@instant_action_deeplink)
    binding.pry

    @payment = {
      "uuid"       => @action.uuid,
      "payee_name" => "Amazon US",
      "amount"     => "256",
      "currency"   => "USD",
      "user_name"  => "UNKNOWN",
      "status"     => "UNKNOWN"
    }

    erb :payment
  end

  post '/users/sign_in' do
    @user_name    = params[:username]
    user_password = params[:password]

    user          = User.find_by(name: @user_name, password: user_password)

    if user.nil?
      erb :login_error
    else
      redirect "admin/connections?user_id=#{user.id}"
    end
  end

  post '/users/confirm' do
    @user_name    = params[:username]
    user_password = params[:password]

    if user = User.find_by(name: @user_name, password: user_password)
      connection = Connection.find_by(connect_session_token: params['token'])
      connection.update(user_id: user.id)

      redirect create_redirect_url(connection)
    else
      erb :login_error
    end
  end

  get '/users/register' do
    erb :create_new_user
  end

  get '/users/register_sca' do
    @session_token = params['token']
    erb :sca_sign_in
  end

  get '/users/connect_sca' do
    @configuration_deeplink = create_deep_link(APP_SETTINGS.service_url, nil)

    @qr = Sinatra::QrHelper.create_qr_code(@configuration_deeplink)

    erb :connect_sca
  end

  post '/users/register' do
    raise StandardError::BadRequest unless params.values_at(:username, :password).none?(&:blank?)

    raise StandardError::UserAlreadyExists unless User.find_by(name: params[:username]).nil?

    user = User.create!(name: params[:username], password: params[:password])

    if params[:redirect].present?
      redirect params[:redirect]
    else
      redirect "admin/connections?user_id=#{user.id}"
    end
  end
end