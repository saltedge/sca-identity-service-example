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
#
# For the additional permissions granted for Salt Edge Authenticator
# under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md

require 'openssl'
require 'base64'

module IdentityService
  class Crypt
    attr_reader :public_key_file

    DEFAULT_ALGORITHM = "AES-256-CBC"

    def initialize(params={})
      @key              = params["key"]
      @iv               = params["iv"]
      @data             = params["data"]
      @public_key_file  = params["public_key_file"]
      @algorithm        = params["algorithm"] || DEFAULT_ALGORITHM
      @padding          = params["padding"] || OpenSSL::PKey::RSA::PKCS1_PADDING
      @cipher           = OpenSSL::Cipher.new(@algorithm)
    end

    def encrypt
      public_key = OpenSSL::PKey::RSA.new(public_key_file)

      @cipher.encrypt
      @cipher.key = key = @cipher.random_key
      @cipher.iv  = iv  = @cipher.random_iv

      json           = @data.to_json
      encrypted_data = @cipher.update(json) << @cipher.final

      {
        "algorithm" => @algorithm,
        "key"       => Base64.encode64(public_key.public_encrypt(key, @padding)),
        "iv"        => Base64.encode64(public_key.public_encrypt(iv, @padding)),
        "data"      => Base64.encode64(encrypted_data),
        "padding"   => @padding
      }
    end
  end
end