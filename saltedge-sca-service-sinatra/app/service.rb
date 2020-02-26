# This file is part of the Salt Edge Authenticator distribution
# (https://github.com/saltedge/sca-identity-service-example)
# Copyright (c) 2019 Salt Edge Inc.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, version 3 or later.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#
# For the additional permissions granted for Salt Edge Authenticator
# under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md

require 'sinatra'
require "sinatra/namespace"
require "sinatra/config_file"
require 'sinatra/reloader' if development?
require 'sinatra/activerecord'

# require_relative 'helpers/service_helper'
# require_relative 'helpers/qr_helper'
# require_relative 'helpers/enroll_helper'
# require_relative 'helpers/errors'
# require_relative 'helpers/notification_sender'
# require_relative './model/action'
# require_relative './model/user'
# require_relative 'model/connection'
# require_relative 'model/authorization'