class UserAuthorizeController < BaseController
  include Sinatra::ServiceHelper

  get '/' do
    erb :index
  end

  get '/users/sign_in' do
    @action = create_action()

    @instant_action_deeplink = create_instant_action_deep_link(@action.uuid, "", "https://#{request.host_with_port}")

    @qr = Sinatra::QrHelper.create_qr_code(@instant_action_deeplink)

    erb :sign_in
  end

  get '/actions' do
    action_uuid = params[:uuid]

    action = Action.find_by(uuid: action_uuid)

    raise StandardError::ActionNotFound if action.nil?

    user = User.find_by(id: action.user_id)

    raise StandardError::UserNotFound if user.nil?

    response = {
      "action_status" => action.status
    }

    if action.status == Action::CONFIRMED
      response[:redirect] = "/admin/connections?user_id=#{action.user_id}"
    end

    content_type :json
    response.to_json
  end

  get '/payments/status' do
    find_action_by_uuid

    response = {
      "action_status" => action.status,
      "username"      => user.name
    }

    content_type :json
    response.to_json
  end

  get '/payments/order' do
    @action = create_action(true)

    @instant_action_deeplink = create_instant_action_deep_link(@action.uuid, "", "https://#{request.host_with_port}")

    @qr = Sinatra::QrHelper.create_qr_code(@instant_action_deeplink)

    @payment = {
      "uuid"       => @action.uuid,
      "payee_name" => "Amazon US",
      "amount"     => "256",
      "currency"   => "USD",
      "user_name"  => "UNKNOWN",
      "status"     => "UNKNOWN"
    }

    erb :payment
  end

  post '/users/sign_in' do
    @user_name    = params[:username]
    user_password = params[:password]

    user          = User.find_by(name: @user_name, password: user_password)

    if user.nil?
      erb :login_error
    else
      redirect "admin/connections?user_id=#{user.id}"
    end
  end

  get '/users/register' do
    session['token'] = params['token']
    erb :login_sign_up
  end

  post '/users/register' do
    raise StandardError::BadRequest unless params.values_at(:username, :password).none?(&:blank?)

    raise StandardError::UserAlreadyExists unless User.find_by(name: params[:username]).nil?

    user = User.create!(name: params[:username], password: params[:password])

    if params[:redirect].present?
      redirect params[:redirect]
    else
      redirect "admin/connections?user_id=#{user.id}"
    end
  end

  private

  def find_action_by_uuid
    action_uuid = params[:uuid]

    action = Action.find_by(uuid: action_uuid)

    raise StandardError::ActionNotFound if action.nil?
  end
end