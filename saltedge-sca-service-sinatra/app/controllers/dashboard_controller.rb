class DashboardController < BaseController
  include Sinatra::ServiceHelper

  ######################### EXAMPLE OF ADMIN (or HELPER) SERVICE ROUTES (FOR TEST PURPOUSE)
  namespace '/admin' do
    # REVOKE CONNECTION (FOR TEST PURPOUSE)
    # Bank Core queries Identity Server to revoke connection by id
    #
    # curl -w "\n" -d "id=1" -X PUT http://localhost:4567/admin/connections/revoke
    get '/connections' do
      @user_id = params[:user_id]

      user = User.find_by(id: @user_id) unless @user_id.nil?

      @user = user

      @user_connections = user.connections

      @configuration_deeplink = create_deep_link(APP_SETTINGS.service_url, params[:user_id])

      @qr = Sinatra::QrHelper.create_qr_code(@configuration_deeplink)

      erb :users_dashboard
    end

    # REMOVE CONNECTION FROM LIST AND REDIRECT BACK (FOR TEST PURPOUSE)
    post '/connections/remove' do
      raise StandardError::BadRequest unless params[:id].present?
      Connection.find_by(id: params[:id]).update(revoked: true)

      redirect "admin/connections?user_id=#{params[:user_id]}" if params[:user_id].present?
    end

    # REVOKE CONNECTION (FOR TEST PURPOUSE)
    # Bank Core queries Identity Server to revoke connection by id
    #
    # curl -w "\n" -d "id=1" -X PUT http://localhost:4567/admin/connections/revoke
    put '/connections/revoke' do
      raise StandardError::BadRequest unless params[:id].present?
      Connection.find_by(id: params[:id]).update(revoked: true)

      content_type :json
      { id: params[:id] }.to_json
    end

    # CREATE USER (FOR TEST PURPOUSE).
    #
    # curl -w "\n" -d "name=Test&password=test" -X POST http://localhost:4567/admin/users
    post '/users' do
      raise StandardError::BadRequest unless params.values_at(:name, :password).none?(&:blank?)

      user = User.create!(name: params[:name], password: params[:password])

      if params[:redirect].present?
        redirect params[:redirect]
      else
        content_type :json
        user.to_json
      end
    end

    # CREATE AUTHORIZATION FOR USER (FOR TEST PURPOUSE)
    # Bank Core queries Identity Server to create new Authorization
    #
    # example of authorization_code = sha256(AMOUNT|CURRENCY_CODE|MERCHANT_ID|MERCHANT_NAME|CURRENT_TIME_STAMP)
    # curl -w "\n" -d "user_id=1&title=Create%20a%20payment&description=550$%20for%20Air%20America&authorization_code=123456789" -X POST http://localhost:4567/admin/authorizations
    post '/authorizations/create' do
      raise StandardError::BadRequest unless params.values_at(:user_id).none?(&:blank?)

      amount = rand(1..200)

      authorization = create_new_authorization!(
        params[:user_id],
        "Payment for #{amount} EUR",
        "Confirm payment #{amount} EUR from account GB1234567890 to Salt Edge Payment Processor",
      )

      connections = authorization.user.connections.where(revoked: false)

      if connections.any?
        notification_sender = IdentityService::NotificationSender.new(
          "push_service_url"        => APP_SETTINGS.push_service_url,
          "push_service_app_id"     => APP_SETTINGS.push_service_app_id,
          "push_service_app_secret" => APP_SETTINGS.push_service_app_secret
        )
        connections.each { |connection| notification_sender.send(authorization, connection) }
      end

      if params[:redirect].present?
        redirect params[:redirect]
      else
        redirect "admin/connections?user_id=#{params[:user_id]}"
      end
    end
  end
end