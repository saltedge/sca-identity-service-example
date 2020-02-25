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

require_relative '../helpers/acceptance_helper.rb'

feature "the signin process" do
  before(:each) { FactoryBot.create(:user) }

  scenario "show Invalid Credentials page" do
    visit '/login?token=123456789'

    fill_in 'name', with: 'wrong_user@example.com'
    fill_in 'password', with: 'password'
    click_button 'sign in'

    expect(page).to have_content 'Invalid credentials'
  end

  scenario "show Confirm page and redirect to return_url with connection data" do
    connection = FactoryBot.create(:connection, connect_session_token: "123456789", access_token: nil)

    expect(connection.user_id).to be_nil
    expect(connection.access_token).to be_nil

    visit '/login?token=123456789'

    fill_in 'name', with: 'user@example.com'
    fill_in 'password', with: 'password'
    click_button 'sign in'

    expect(page.status_code).to eq(200)
    expect(page).to have_content 'Welcome'
    expect(page).to have_content 'Confirm connection'
    expect(page.current_url).to eq("http://www.example.com/login")

    click_button 'Confirm connection'

    connection_id = connection.reload.id
    connection_access_token = connection.access_token

    expect(page.current_url).to eq("authenticator://return:80/?id=#{connection_id}&access_token=#{connection_access_token}")
    expect(connection_access_token).not_to be_empty
    expect(connection.user_id).to eq("1")
  end

  scenario "show Confirm page and redirect to return_url with error" do
    connection = FactoryBot.create(:connection, connect_session_token: "123456789", access_token: nil)

    expect(connection.user_id).to be_nil
    expect(connection.access_token).to be_nil

    visit '/login?token=123456789'

    fill_in 'name', with: 'user@example.com'
    fill_in 'password', with: 'password'
    click_button 'sign in'

    expect(page.status_code).to eq(200)
    expect(page).to have_content 'Welcome'
    expect(page).to have_content 'Confirm connection'
    expect(page.current_url).to eq("http://www.example.com/login")

    click_link 'Finish with error'

    connection_id = connection.reload.id
    connection_access_token = connection.access_token

    expect(page.current_url).to eq("authenticator://return:80/?error_class=AUTHENTICATION_ERROR&error_message=AUTHENTICATION_ERROR_MESSAGE")
    expect(connection.user_id).to be_nil
    expect(connection.access_token).to be_nil
  end
end
