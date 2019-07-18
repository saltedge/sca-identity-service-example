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

require 'rack/test'
require 'rspec'
require 'database_cleaner'
require 'factory_bot'
require_relative "../app/service.rb"

ENV['RACK_ENV'] ||= 'test'
RSPEC_ROOT = File.dirname __FILE__
APP_ROOT = "#{RSPEC_ROOT}/../app"
SPEC_PUBLIC_KEY = File.read("#{RSPEC_ROOT}/fixtures/public_key.pem")
SPEC_PRIVATE_KEY = File.read("#{RSPEC_ROOT}/fixtures/private_key.pem")

set :environment, :test

DatabaseCleaner.strategy = :truncation
DatabaseCleaner.clean # cleanup of the test

module RSpecMixin
  include Rack::Test::Methods
  def app() Sinatra::Application end
end

RSpec.configure do |config|
  config.include RSpecMixin
  config.filter_run_when_matching :focus
  config.full_backtrace = false
  config.include FactoryBot::Syntax::Methods

  config.before(:suite) do
    FactoryBot.find_definitions

    DatabaseCleaner.strategy = :transaction
    DatabaseCleaner.clean_with(:truncation)
  end

  config.around(:each) do |example|
    DatabaseCleaner.cleaning do
      example.run
    end
  end
end

def create_valid_signature(request_method, original_url, expires_at, body)
  data = "#{request_method}|#{original_url}|#{expires_at}|#{body}"
  IdentityService::Sign.new({
    private_key_file: SPEC_PRIVATE_KEY,
    data:             data,
    digest:           :sha256
  }).sign
end

def create_valid_headers(request_method:, route_path:, body: '')
  expires_at = Time.now.getutc + 5 * 60
  signature = create_valid_signature(request_method, "http://example.org#{route_path}", expires_at, body)

  {
    'HTTP_ACCESS_TOKEN' => 'access_token',
    'HTTP_SIGNATURE' => signature,
    'HTTP_EXPIRES_AT' => expires_at
  }
end

def parsed_response
  JSON.parse(last_response.body)
end