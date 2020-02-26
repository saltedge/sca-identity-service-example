class BaseController < Sinatra::Base
  include Sinatra::ServiceHelper
  register Sinatra::Namespace

  set :raise_errors, false
  set :show_exceptions, false

  configure do
    set :views, "app/views"
  end

  error BadRequest, AuthorizationRequired,
    AuthorizationNotFound, ConnectionNotFound, UserNotFound,
    SignatureExpired, SignatureMissing, InvalidSignature, UserAlreadyExists, JSON::ParserError do
    name = env['sinatra.error'].class.name
    halt 400, {'Content-Type' => 'application/json'}, { "error_class" => name, "error_message" => name }.to_json
  end
end