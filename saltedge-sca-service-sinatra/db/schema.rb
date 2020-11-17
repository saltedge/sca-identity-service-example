# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# This file is the source Rails uses to define your schema when running `rails
# db:schema:load`. When creating a new database, `rails db:schema:load` tends to
# be faster and is potentially less error prone than running all of your
# migrations from scratch. Old migrations may fail to apply correctly if those
# migrations use external dependencies or application code.
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 2020_02_26_102512) do

  create_table "actions", force: :cascade do |t|
    t.string "uuid"
    t.string "user_id"
    t.string "status"
    t.boolean "sca_confirm_required", default: false, null: false
    t.datetime "expires_at"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
  end

  create_table "authorizations", force: :cascade do |t|
    t.string "user_id"
    t.string "connection_id"
    t.string "action_id"
    t.datetime "expires_at"
    t.string "title", limit: 4096
    t.string "description", limit: 4096
    t.string "authorization_code", limit: 4096
    t.boolean "confirmed"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
  end

  create_table "connections", force: :cascade do |t|
    t.string "user_id"
    t.string "public_key", limit: 4096
    t.string "push_token", limit: 4096
    t.string "platform", limit: 32
    t.string "return_url", limit: 4096
    t.string "connect_session_token", limit: 4096
    t.string "access_token", limit: 4096
    t.boolean "revoked", default: false
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
  end

  create_table "users", force: :cascade do |t|
    t.string "name", limit: 64
    t.string "password", limit: 64
    t.string "auth_session_token", limit: 4096
    t.datetime "auth_session_token_expires_at"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["name"], name: "index_users_on_name", unique: true
  end

end
