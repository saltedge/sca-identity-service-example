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

module Sinatra
  module EnrollHelper
    def redirect_url(connection, user_id, finish_action)
      case finish_action
      when 'success'
        access_token = SecureRandom.hex
        connection.update(access_token: access_token, user_id: user_id)
        add_params_to_redirect_url(connection.return_url, {id: connection.id.to_s, access_token: access_token})
      else
        add_params_to_redirect_url(connection.return_url, {error_class: "AUTHENTICATION_ERROR", error_message: "AUTHENTICATION_ERROR_MESSAGE"})
      end
    end

    private

    def add_params_to_redirect_url(return_url, params = {})
      uri       = URI(return_url)
      params    = Hash[URI.decode_www_form(uri.query || '')].merge(params)
      uri.query = URI.encode_www_form(params)
      uri.to_s
    end
  end
end