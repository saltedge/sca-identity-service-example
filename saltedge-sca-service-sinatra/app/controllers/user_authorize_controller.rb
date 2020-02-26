class UserAuthorizeController < BaseController
  get '/' do
    @action = Action.new()

    @instant_action_deeplink = create_instant_action_deep_link(@action.uuid, "", "https://#{request.host_with_port}")

    @qr = Sinatra::QrHelper.create_qr_code(@instant_action_deeplink)
    erb :index
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
end