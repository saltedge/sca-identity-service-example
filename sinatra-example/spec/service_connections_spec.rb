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

describe "POST /connections" do
  context "when request is valid" do
    it "responds with 200 and JSON with connect_url" do
      post '/api/authenticator/v1/connections', { data: { public_key: "public_key", push_token: "push_token", platform: "android", return_url: "return_url" } }.to_json

      expect(last_response.status).to eq 200
      expect(parsed_response["data"]["connect_url"]).not_to be_empty
      expect(parsed_response["data"]["id"]).not_to be_empty
      expect(Connection.count).to eq(1)
    end
  end

  context "when request is not valid" do
    it "responds with 400 and Json::ParserError" do
      post '/api/authenticator/v1/connections'

      expect(last_response.status).to eq 400
      expect(parsed_response).to eq({"error_class" => "JSON::ParserError", "error_message" => "JSON::ParserError"})
    end
  end
end

describe "GET /connections" do
  before(:each) { FactoryBot.create :user, :with_connection }

  context "when request is valid" do
    it "responds with 200 and Connection data" do
      headers = create_valid_headers(request_method: 'get', route_path: '/api/authenticator/v1/connections')
      get '/api/authenticator/v1/connections', nil, headers

      expect(last_response.status).to eq 200
      expect(parsed_response["data"]["id"]).to eq "1"
      expect(parsed_response["data"]["platform"]).to eq "android"
    end
  end

  context "when request is not valid" do
    it "responds with 400 and AccessTokenMissing if access_token not sent" do
      get '/api/authenticator/v1/connections'

      expect(last_response.status).to eq 400
      expect(parsed_response).to eq({"error_class" => "AuthorizationRequired", "error_message" => "AuthorizationRequired"})
    end

    it "responds with 400 and ConnectionNotFound if no connection with given token" do
      get '/api/authenticator/v1/connections', nil, {'HTTP_ACCESS_TOKEN' => 'unknown_token'}

      expect(last_response.status).to eq 400
      expect(parsed_response).to eq({"error_class" => "ConnectionNotFound", "error_message" => "ConnectionNotFound"})
    end

    it "responds with 400 and  SignatureMissing if no Signature in request" do
      get '/api/authenticator/v1/connections', nil, {'HTTP_ACCESS_TOKEN' => 'access_token'}

      expect(last_response.status).to eq 400
      expect(parsed_response).to eq({"error_class" => "SignatureMissing", "error_message" => "SignatureMissing"})
    end

    it "responds with 400 and ExpiredSignature if no HTTP_EXPIRES_AT" do
      headers = {
        'HTTP_ACCESS_TOKEN' => 'access_token',
        'HTTP_SIGNATURE' => 'signature'
      }
      get '/api/authenticator/v1/connections', nil, headers

      expect(last_response.status).to eq 400
      expect(parsed_response).to eq({"error_class" => "SignatureExpired", "error_message" => "SignatureExpired"})
    end

    it "responds with 400 and ExpiredSignature if HTTP_EXPIRES_AT header is outdated" do
      headers = {
        'HTTP_ACCESS_TOKEN' => 'access_token',
        'HTTP_SIGNATURE' => 'signature',
        'HTTP_EXPIRES_AT' => Time.now.getutc - 100
      }
      get '/api/authenticator/v1/connections', nil, headers

      expect(last_response.status).to eq 400
      expect(parsed_response).to eq({"error_class" => "SignatureExpired", "error_message" => "SignatureExpired"})
    end

    it "responds with 400 and InvalidSignature if Signature header is invalid and not passes verification" do
      headers = {
        'HTTP_ACCESS_TOKEN' => 'access_token',
        'HTTP_SIGNATURE' => 'signature',
        'HTTP_EXPIRES_AT' => Time.now.getutc + 100
      }
      get '/api/authenticator/v1/connections', nil, headers

      expect(last_response.status).to eq 400
      expect(parsed_response).to eq({"error_class" => "InvalidSignature", "error_message" => "InvalidSignature"})
    end
  end
end

describe "DELETE /connections" do
  before(:each) { FactoryBot.create :user, :with_connection }

  context "when Connection exist" do
    it "update field `revoked` with `true` value" do
      expect(Connection.find_by!(access_token: "access_token").revoked).to be false

      headers = create_valid_headers(request_method: 'delete', route_path: '/api/authenticator/v1/connections')
      delete '/api/authenticator/v1/connections', nil, headers

      expect(last_response.status).to eq 200
      expect(parsed_response).to eq({"data" => {"success" => true, "access_token" => "access_token"}})
      expect(Connection.find_by!(access_token: "access_token").revoked).to be true
    end
  end
end