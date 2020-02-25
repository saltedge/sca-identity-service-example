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

require_relative 'spec_helper.rb'

describe "GET /authorizations" do
  context "when there are no authorization" do
    before(:each) { FactoryBot.create :user, :with_connection }

    it "responds with 200 and empty array" do
      headers = create_valid_headers(
        request_method: 'get',
        route_path: '/api/authenticator/v1/authorizations'
      )
      get '/api/authenticator/v1/authorizations', nil, headers

      expect(last_response.status).to eq 200
      expect(parsed_response).to eq({"data" => []})
    end
  end

  context "when there are authorization" do
    before(:each) { FactoryBot.create :user, :with_connection, :with_authorizations }

    it "responds with 200 and non-empty array of valid authorizations" do
      FactoryBot.create(:authorization, expires_at: Time.now.getutc + 5 * 60, user_id: "99999")

      headers = create_valid_headers(
        request_method: 'get',
        route_path: '/api/authenticator/v1/authorizations'
      )
      get '/api/authenticator/v1/authorizations', nil, headers

      parsed_data = parsed_response["data"]

      expect(last_response.status).to eq 200
      expect(parsed_data.length).to eq 1
      expect(parsed_data[0]['id']).to eq "1"
      expect(parsed_data[0]['connection_id']).to eq "1"
      expect(parsed_data[0]['algorithm']).to eq "AES-256-CBC"
      expect(parsed_data[0]['iv']).not_to be_empty
      expect(parsed_data[0]['key']).not_to be_empty
      expect(parsed_data[0]['data']).not_to be_empty
    end
  end
end

describe "GET /authorization/:authorization_id" do
  before(:each) { FactoryBot.create :user, :with_connection, :with_authorizations }

  context "when there are no authorization with given `authorization_id`" do
    it "responds with 400 and BadRequest" do
      headers = create_valid_headers(
        request_method: 'get',
        route_path: '/api/authenticator/v1/authorizations/999'
      )
      get '/api/authenticator/v1/authorizations/999', nil, headers

      expect(last_response.status).to eq 400
      expect(parsed_response).to eq({"error_class" => "AuthorizationNotFound", "error_message" => "AuthorizationNotFound"})
    end
  end

  context "when there are authorization with given `authorization_id`" do
    it "responds with 200 and Authorization data" do
      headers = create_valid_headers(
        request_method: 'get',
        route_path: '/api/authenticator/v1/authorizations/1'
      )
      get '/api/authenticator/v1/authorizations/1', nil, headers

      parsed_data = parsed_response["data"]

      expect(last_response.status).to eq 200
      expect(parsed_data['id']).to eq "1"
      expect(parsed_data['connection_id']).to eq "1"
      expect(parsed_data['algorithm']).to eq "AES-256-CBC"
      expect(parsed_data['iv']).not_to be_empty
      expect(parsed_data['key']).not_to be_empty
      expect(parsed_data['data']).not_to be_empty
    end
  end
end

describe "PUT /authorizations/" do
  before(:each) { @user = FactoryBot.create :user, :with_connection, :with_authorizations }

  context "when request is invalid" do
    it "responds with 400 and AuthorizationNotFound if authorization not exist" do
      payload = { data: { confirm: true, authorization_code: "code1" } }.to_json
      headers = create_valid_headers(
        request_method: 'put',
        route_path: '/api/authenticator/v1/authorizations/999',
        body: payload
      )
      put '/api/authenticator/v1/authorizations/999', payload, headers

      expect(parsed_response).to eq({"error_class" => "AuthorizationNotFound", "error_message" => "AuthorizationNotFound"})
      expect(last_response.status).to eq 400
    end

    it "responds with 400 and BadRequest if request payload is invalid" do
      headers = create_valid_headers(
        request_method: 'put',
        route_path: '/api/authenticator/v1/authorizations/1'
      )
      put '/api/authenticator/v1/authorizations/1', nil, headers

      expect(parsed_response).to eq({"error_class" => "JSON::ParserError", "error_message" => "JSON::ParserError"})
      expect(last_response.status).to eq 400
    end
  end

  context "when request is valid" do
    it "responds with 200 and success=false result" do
      authorization = @user.authorizations.find(1)

      expect(authorization.confirmed).to be_nil

      payload = { data: { confirm: true, authorization_code: "codeX" } }.to_json
      headers = create_valid_headers(
        request_method: 'put',
        route_path: '/api/authenticator/v1/authorizations/1',
        body: payload
      )
      put '/api/authenticator/v1/authorizations/1', payload, headers

      expect(parsed_response).to eq({"data" => {"success" => false, "id" => "1"} })
      expect(last_response.status).to eq 200
      expect(@user.authorizations.find(1).confirmed).to be_nil
    end

    it "responds with 200 and success=true result" do
      authorization = @user.authorizations.find(1)

      expect(authorization.confirmed).to be_nil

      payload = { data: { confirm: true, authorization_code: authorization.authorization_code } }.to_json
      headers = create_valid_headers(
        request_method: 'put',
        route_path: '/api/authenticator/v1/authorizations/1',
        body: payload
      )
      put '/api/authenticator/v1/authorizations/1', payload, headers
      authorization.reload

      expect(parsed_response).to eq({"data" => {"success" => true, "id" => "1"} })
      expect(last_response.status).to eq 200
      expect(authorization.confirmed).to eq true
    end
  end
end