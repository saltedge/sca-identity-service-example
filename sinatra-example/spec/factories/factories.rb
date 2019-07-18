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

require 'factory_bot'
require_relative "../spec_helper.rb"

FactoryBot.define do
  factory :user do
    name { "user@example.com" }
    password { "password" }

    trait :with_connection do
      after :create do |user|
        create :connection, user: user
      end
    end

    trait :with_authorizations do
      after :create do |user|
        create :authorization, user: user, expires_at: Time.now.getutc + 5 * 60
        create :authorization, user: user, expires_at: Time.now.getutc - 1
        create :authorization, user: user, expires_at: Time.now.getutc + 5 * 60, confirmed: true
      end
    end
  end

  factory :connection do
    public_key { SPEC_PUBLIC_KEY }
    push_token { "push_token" }
    platform { "android" }
    return_url { "authenticator://return" }
    connect_session_token { "connect_session_token" }
    access_token { "access_token" }
  end

  factory :authorization do
    title { "title" }
    description { "description" }
    sequence(:authorization_code) { |n| "code#{n}" }
  end
end