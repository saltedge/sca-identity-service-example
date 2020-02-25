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

require 'openssl'
require 'base64'

module IdentityService
  class Sign
    DIGESTS = {
      sha256: OpenSSL::Digest::SHA256
    }

    def initialize(params)
      @data             = params[:data]
      @signature        = params[:signature]
      @public_key_file  = params[:public_key_file]
      @private_key_file = params[:private_key_file]
      @digest           = DIGESTS[params[:digest]] || DIGESTS[:sha256]
    end

    def sign
      Base64.encode64(rsa_key(@private_key_file).sign(@digest.new, @data))
    end

    def verify
      rsa_key(@public_key_file).verify(@digest.new, Base64.decode64(@signature), @data)
    end

    def valid_public?
      key = rsa_key(@public_key_file)
      key.public?
    rescue
      false
    end

    def valid_private?
      key = rsa_key(@private_key_file)
      key.private?
    rescue
      false
    end

    private

    def rsa_key(key)
      OpenSSL::PKey::RSA.new(key)
    end
  end
end