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

class UserAuthorizeController < BaseController
  include Sinatra::ServiceHelper

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

  get '/users/register' do
    session['token'] = params['token']
    erb :login_sign_up
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