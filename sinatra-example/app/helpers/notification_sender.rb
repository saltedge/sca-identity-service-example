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

require 'rest-client'

module IdentityService
  class NotificationSender
    def initialize(params={})
      @push_service_url        = params["push_service_url"]
      @push_service_app_id     = params["push_service_app_id"]
      @push_service_app_secret = params["push_service_app_secret"]
    end

    def send(authorization, connection)
      return if connection.push_token.nil? || connection.push_token.empty? || connection.platform.nil? || connection.platform.empty?

      payload = {
        "data" => []
      }

      notification_title = "Authorization Request"
      notification_description = "Tap to confirm/deny action"
      notification_data = {
        "title"            => notification_title,
        "body"             => notification_description,
        "expires_at"       => authorization.expires_at.round(10).iso8601(3),
        "push_token"       => connection.push_token,
        "platform"         => connection.platform,
        "data"             => {
          "connection_id"    => connection.id.to_s,
          "authorization_id" => authorization.id.to_s
        }
      }

      payload["data"] << notification_data

      RestClient.post @push_service_url, payload, {'Content-Type': :json, 'App-id': @push_service_app_id, 'App-secret': @push_service_app_secret}
    end
  end
end