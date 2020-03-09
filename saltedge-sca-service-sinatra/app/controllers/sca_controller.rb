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

class SCAController < BaseController

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
    # Mobile client query this endpoint after scaning QR code and receiving configuration data
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
    
    # SUBMIT ACTION
    put "/actions/:uuid" do
      access_token = request.env['HTTP_ACCESS_TOKEN']
      raise StandardError::AuthorizationRequired if access_token.nil?

      connection = Connection.find_by(access_token: access_token, revoked: false)

      raise StandardError::ConnectionNotFound if connection.nil?

      verify_signature

      action_uuid = params['uuid']

      action = Action.find_by(uuid: action_uuid)

      raise StandardError::ActionNotFound if action.nil?
      raise StandardError::ActionNotValid if action.status != Action::PENDING

      if action.sca_confirm_required
        action.update(status: Action::WAITING_CONFIRMATION, user_id: connection.user_id)

        authorization = create_new_authorization!(connection.user_id, "Payment", "Pay 260 USD to Amazon US")
        authorization.update(action_id: action.uuid)

        content_type :json
		    { "data" =>
			    {
				    "success" => true,
				    "authorization_id" => authorization.id,
				    "connection_id" => connection.id
			    }
		    }.to_json
      else
        action.update(status: Action::CONFIRMED, user_id: connection.user_id)

        content_type :json
        { "data" =>
          {
            "success" => true
          }
        }.to_json
      end
    end
  end
end