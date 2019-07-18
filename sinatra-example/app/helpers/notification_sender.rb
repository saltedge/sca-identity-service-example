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

require 'fcm'
require 'apnotic'

module IdentityService
  class NotificationSender
    def initialize(params={})
      @fcm_push_key              = params["fcm_push_key"]
      @apns_certificate_path     = params["apns_certificate_path"]
      @apns_certificate_password = params["apns_certificate_password"]
    end

    def send(authorization, connection)
      return if connection.push_token.nil? || connection.push_token.empty? || connection.platform.nil? || connection.platform.empty?

      notification_title = "Authorization Request"
      notification_description = "Tap to confirm/deny action"
      notification_data = {
        "title"            => notification_title,
        "body"             => notification_description,
        "authorization_id" => authorization.id.to_s,
        "connection_id"    => connection.id.to_s,
        "expires_at"       => authorization.expires_at.round(10).iso8601(3)
      }

      if connection.platform == "android"
        send_fcm_notification(notification_title, notification_description, notification_data, connection.push_token)
      elsif connection.platform == "ios"
        send_apns_notification(notification_title, notification_description, notification_data, connection.push_token)
      end
    end

    private

    def apns_certificate
      @apns_certificate ||= File.read(@apns_certificate_path) if File.exist?(@apns_certificate_path)
    end

    def send_fcm_notification(notification_title, notification_description, notification_data, push_token)
      tokens = [push_token]

      fcm = FCM.new(@fcm_push_key)
      options = {
        "notification" => {
          "title" => notification_title,
          "body" => notification_description
        },
        "data" => notification_data
      }
      response = fcm.send(tokens, options)
    rescue => error
      puts error.message
    end

    def send_apns_notification(notification_title, notification_description, notification_data, push_token)
      connection = Apnotic::Connection.new(cert_path: @apns_certificate_path, cert_pass: @apns_certificate_password)

      notification = Apnotic::Notification.new(push_token)
      notification.alert = {
        title =>  notification_title,
        body  =>  notification_description
      }
      
      response = connection.push(notification)
      connection.close
    end
  end
end