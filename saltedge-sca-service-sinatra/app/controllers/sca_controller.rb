class SCAController < BaseController
  include Sinatra::ServiceHelper

  ######################### EXAMPLE OF IDENTITY SERVICE API
  namespace '/api/authenticator/v1' do
    # Verifies identity and signature
    before do
      next if request.post? && request.path_info.end_with?("/connections") || request.get? && request.path_info.end_with?("/configuration")
      verify_identity
      verify_signature
      content_type :json
    end

    # GET SERVICE PROVIDER CONFIGURATION
    get "/configuration" do
      create_configuration("https://#{request.host_with_port}")
    end

    # CREATE NEW CONNECTION
    # Mobile client query this endpoint after scaning QR code and receiving configuration data
    # Create new Connection
    # Return new Connection Id and authentication page connect_url
    post "/connections" do
      new_connection = create_new_connection!
      get_user_authentication_url(new_connection)
    end

    # SHOW CONNECTION
    # Return connection info by access_token
    get "/connections" do
      get_connection
    end

    # REVOKE CONNECTION
    # Mark connection by access_token as revoked
    # Return result of operation
    delete "/connections" do
      revoke_connection!
    end

    # GET ALL ACTIVE (NOT EXPIRED) AUTHORIZATIONS
    # Return list of encrypted and not expired authorizations for connection by access_token
    get "/authorizations" do
      get_encrypted_authorizations
    end

    # GET AUTHORIZATION BY ID
    # Return encrypted and not expired authorization by Id for connection by access_token
    get "/authorizations/:authorization_id" do
      raise StandardError::BadRequest unless params[:authorization_id].present?
      get_encrypted_authorization(params[:authorization_id])
    end

    # CONFIRM/DENY AUTHORIZATION
    # Mark authorization by Id for connection by access_token as Confirmed or Denied
    # Return result of operation
    put "/authorizations/:authorization_id" do
      update_authorization!(params[:authorization_id])
    end
    
    # SUBMIT ACTION
    put "/actions/:uuid" do
      access_token = request.env['HTTP_ACCESS_TOKEN']
      raise StandardError::AuthorizationRequired if access_token.nil?

      connection = Connection.find_by(access_token: access_token, revoked: false)

      raise StandardError::ConnectionNotFound if connection.nil?

      verify_signature

      action_uuid = params['uuid']

      action = Action.find_by(uuid: action_uuid)

      raise StandardError::ActionNotFound if action.nil?
      raise StandardError::ActionNotValid if action.status != Action::PENDING

      # action.status = Action::CONFIRMED
      # action.user_id = connection.user_id

      action.update(status: Action::CONFIRMED, user_id: connection.user_id)

      content_type :json
      { "data" =>
        {
          "success" => true
        }
      }.to_json
    end
  end
end