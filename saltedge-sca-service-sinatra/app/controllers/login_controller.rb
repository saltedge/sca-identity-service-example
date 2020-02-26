require 'pry'

class LoginController < Sinatra::Base
  register Sinatra::Namespace
  
  configure do
    set :views, "app/views"
  end

  ######################### EXAMPLE OF WEB LOGIN USED TO AUTHENTICATE USER AND RETURN ACCESS_TOKEN (FOR TEST PURPOUSE)
  namespace '/login' do
    # SHOW LOGIN PAGE
    get do
      session['token'] = params['token']
      erb :login_sign_up
    end

    # CREATE USER (FOR TEST PURPOUSE).
    #
    # curl -w "\n" -d "name=Test&password=test" -X POST http://localhost:4567/admin/users
    post '/users' do
      raise StandardError::BadRequest unless params.values_at(:name, :password).none?(&:blank?)

      user = User.create!(name: params[:name], password: params[:password])

      # binding.pry

      if params[:redirect].present?
        redirect params[:redirect]
      else
        erb :user_dashboard
      end
    end

    # # USER ENTERED LOGIN CREDENTIALS
    # post do
    #   @user_name    = params[:name]
    #   user_password = params[:password]
    #   user          = User.find_by(name: @user_name, password: user_password)
    #   if user.nil?
    #     erb :login_error
    #   else
    #     session['user_id'] = user.id
    #     erb :login_confirm
    #   end
    # end

    # # PROCESS ENROLL FINISH ACTION
    # get '/:finish_action' do
    #   erb :login_error unless session['token'].present? && session['user_id'].present? && params['finish_action'].present?
    #   user = User.find_by(id: session['user_id'])
    #   connection = Connection.find_by(connect_session_token: session['token'])
    #   if user.nil? || connection.nil?
    #     erb :login_error
    #   else
    #     redirect redirect_url(connection, user.id, params['finish_action'])
    #   end
    # end
  end
end