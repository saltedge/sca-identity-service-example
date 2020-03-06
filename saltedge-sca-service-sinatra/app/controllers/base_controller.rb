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

class BaseController < Sinatra::Base
  register Sinatra::Namespace

  set :raise_errors, false
  set :show_exceptions, false

  configure do
    set :views, "app/views"
  end

  error BadRequest, ActionNotFound, ActionNotValid, AuthorizationRequired,
    AuthorizationNotFound, ConnectionNotFound, UserNotFound,
    SignatureExpired, SignatureMissing, InvalidSignature, UserAlreadyExists, JSON::ParserError do
    name = env['sinatra.error'].class.name
    halt 400, {'Content-Type' => 'application/json'}, { "error_class" => name, "error_message" => name }.to_json
  end
end