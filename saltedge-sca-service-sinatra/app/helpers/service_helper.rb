# This file is part of the Salt Edge Authenticator distribution
# (https://github.com/saltedge/sca-identity-service-example)
# Copyright (c) 2019 Salt Edge Inc.

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

require 'securerandom'
require 'json'
require 'uri'
require 'base64'
require_relative 'crypt'
require_relative 'sign'
require_relative 'enroll_helper'

class Object
  def present?
    !blank?
  end

  def blank?
    respond_to?(:empty?) ? empty? : !self
  end
end

module Sinatra
  module ServiceHelper
    include Sinatra::EnrollHelper

    DEEPLINK_URL = 'authenticator://saltedge.com/connect'

    # Verifies that request has ACCESS_TOKEN header and related Connection exist
    def verify_identity
      raise AuthorizationRequired unless access_token
      raise ConnectionNotFound unless connection
    end

    # Verifies that HTTP_SIGNATURE and HTTP_EXPIRES_AT headers are valid
    def verify_signature
      current_signature = request.env['HTTP_SIGNATURE']
      expires_at        = request.env["HTTP_EXPIRES_AT"]
      request_method    = request.request_method.downcase
      original_url      = request.url
      body              = (+raw_request_body).force_encoding(Encoding::UTF_8)
      data              = "#{request_method}|#{original_url}|#{expires_at}|#{body}"

      raise SignatureMissing unless current_signature.present?
      raise SignatureExpired unless expires_at.present? && Time.now.to_i < expires_at.to_i

      begin
        verify_hash = {
          public_key_file: connection.public_key,
          data:            data,
          signature:       current_signature,
          digest:          :sha256
        }
        verify_result = IdentityService::Sign.new(verify_hash).verify
      rescue
        verify_result = false
      end
      raise InvalidSignature unless verify_result
    end

    # Creates deep link with service configuration url
    def create_deep_link(service_url, user_id)
      configuration_url = "#{service_url}/api/authenticator/v1/configuration"
      url_encoded_string = URI::encode(configuration_url)
      default_deeplink = "authenticator://saltedge.com/connect?configuration=#{url_encoded_string}"

      user = User.find_by(id: user_id) unless user_id.nil?
      if user.nil?
        default_deeplink
      else
        auth_session_token = SecureRandom.hex
        user.update(
          auth_session_token: auth_session_token,
          auth_session_token_expires_at: Time.now.utc + 1 * 60
        )
        "#{default_deeplink}&connect_query=#{auth_session_token}"
      end
    end

    def create_fast_authorization_deeplin(service_url)
      configuration_url = "#{service_url}/api/authenticator/v1/configuration"
      url_encoded_string = URI::encode(configuration_url)
      default_deeplink = "authenticator://saltedge.com/connect?configuration=#{url_encoded_string}"

      auth_session_token = SecureRandom.hex
      "#{default_deeplink}&connect_query=#{auth_session_token}"
    end

    def create_instant_action_deep_link(action_uuid, return_to = "", connect_url)
      "#{DEEPLINK_URL}/action?action_uuid=#{action_uuid}&return_to=#{URI::encode(return_to)}&connect_url=#{URI::encode(connect_url)}"
    end

    # Creates service configuration response
    def create_configuration(service_url)
      provider = create_random_provider
      {
        data: {
          connect_url:   service_url,
          code:          provider[:code],
          name:          provider[:name],
          logo_url:      provider[:logo_url],
          support_email: provider[:support_email],
          version:       "1"
        }
      }.to_json
    end

    # Verifies payload data and creates new Connection in db
    def create_new_connection!
      raise StandardError::BadRequest unless request_data&.values_at(
        'public_key',
        'platform',
        'return_url'
      )&.none?(&:blank?)

      auth_session_token = request_data['connect_query']
      user = User.find_by(auth_session_token: auth_session_token) unless auth_session_token.nil?
      user_id = user.id unless user.nil? || Time.now.to_i > user.auth_session_token_expires_at.to_i

      Connection.create(
        public_key:            request_data['public_key'],
        push_token:            request_data['push_token'],
        platform:              request_data['platform'],
        return_url:            request_data['return_url'],
        connect_session_token: SecureRandom.hex,
        user_id:               user_id
      )
    end

    # Creates response with connect_url by new Connection
    def get_user_authentication_url(connection)
      if connection.user_id.present?
        url = redirect_url(connection, connection.user_id, 'success')
      else
        url = "https://#{request.host_with_port}/users/register?token=#{connection.connect_session_token}"
      end

      {
        data: {
          connect_url:  url,
          access_token: connection.connect_session_token,
          id:           connection.id.to_s
        }
      }.to_json
    end

    # Creates response with Connection info
    def get_connection
      {
        data: {
          id:         connection.id.to_s,
          created_at: connection.created_at,
          push_token: connection.push_token,
          platform:   connection.platform,
          return_url: connection.return_url
        }
      }.to_json
    end

    # Set Connection.update as true
    # and creates json response with operation result and revoked access_token
    def revoke_connection!
      update_result = connection.update(revoked: true)
      {
        data: {
          success:      update_result,
          access_token: access_token
        }
      }.to_json
    end

    def get_encrypted_authorizations
      encrypted_authorizations = connection.pending_authorizations.map do |authorization|
        authorization_hash = authorization_hash(connection.id, authorization)
        encrypted_authorization(authorization_hash, connection.public_key)
      end
      { data: encrypted_authorizations }.to_json
    end

    def get_encrypted_authorization(authorization_id)
      authorization = connection.pending_authorization(authorization_id)
      raise AuthorizationNotFound if authorization.nil?

      authorization_hash = authorization_hash(connection.id, authorization)
      encrypted_authorization = encrypted_authorization(authorization_hash, connection.public_key)
      { data: encrypted_authorization }.to_json
    end

    # Mark Authorization as confirmed (true) or denied (false)
    # and creates response with operation result and authorization_id
    def update_authorization!(authorization_id)
      raise StandardError::BadRequest if request_data.nil? || request_data['confirm'].nil? || request_data['authorization_code'].nil?
      authorization = connection.pending_authorization(authorization_id)
      raise AuthorizationNotFound if authorization.nil?

      valid_code = request_data['authorization_code'] == authorization.authorization_code
      if valid_code
        authorization.update(confirmed: request_data['confirm'])

        if action = Action.find_by(uuid: authorization.action_id)
          action.update(status: request_data['confirm'] ? Action::CONFIRMED : Action::DENIED)
        end
        # NOTIFY BANK CORE ABOUT CONFIRM/DENY ACTION
      end

      { data: { success: valid_code, id: authorization_id } }.to_json
    end

    def create_new_authorization!(user_id, title, description)
      user = User.find_by(id: user_id)
      raise UserNotFound if user.nil?
    
      template = "#{user_id}|#{title}|#{description}|#{Time.now.utc}"
      authorization_code = Base64.urlsafe_encode64(Digest::SHA256.hexdigest(template), padding: false)

      user.authorizations.create(
        expires_at:         Time.now.utc + 5 * 60,
        title:              title,
        description:        description,
        authorization_code: authorization_code,
      )
    end

    def create_action(status: Action::PENDING, require_sca: false)
      Action.create!(
        uuid:                 SecureRandom.uuid,
        status:               status,
        sca_confirm_required: require_sca,
        expires_at:           Time.now.utc + 5 * 60
      )
    end

    private

    def authorization_hash(connection_id, authorization)
      {
        "id"                 => authorization.id.to_s,
        "connection_id"      => connection_id.to_s,
        "title"              => authorization.title,
        "description"        => authorization.description,
        "authorization_code" => authorization.authorization_code,
        "created_at"         => authorization.created_at.round(10).iso8601(3),
        "expires_at"         => authorization.expires_at.round(10).iso8601(3)
      }
    end

    def encrypted_authorization(authorization_hash, public_key)
      encrypted_data = IdentityService::Crypt.new(
        "data" => authorization_hash, "public_key_file" => public_key
      ).encrypt

      {
        id:            authorization_hash["id"].to_s,
        connection_id: authorization_hash["connection_id"].to_s,
        iv:            encrypted_data["iv"],
        key:           encrypted_data["key"],
        algorithm:     encrypted_data["algorithm"],
        data:          encrypted_data["data"]
      }
    end

    def create_random_provider
      logo = [
        'http://pngimg.com/uploads/visa/visa_PNG4.png',
        'http://pngimg.com/uploads/mastercard/mastercard_PNG16.png',
        'https://static-eu.insales.ru/files/1/906/6783882/original/PayPal-Logo.png',
        'https://www.google.com/logos/doodles/2015/googles-new-logo-5078286822539264.3-hp2x.gif'
      ].sample
      {
        code:          'demobank',
        name:          'Demobank',
        logo_url:      logo,
        support_email: 'authenticator@saltedge.com',
      }
    end

    def raw_request_body
      request.body.rewind
      request.body.read
    end

    def parse_request_body
      JSON.parse(raw_request_body)
    end

    def request_data
      @request_body ||= parse_request_body
      @request_data ||= @request_body['data'] if @request_body
    end

    def access_token
      @access_token ||= request.env['HTTP_ACCESS_TOKEN']
    end

    def connection
      @connection ||= Connection.find_by(access_token: access_token, revoked: false)
    end
  end
end