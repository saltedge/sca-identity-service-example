# action.rb
class Action < ActiveRecord::Base
    STATUSES = ["expired", "pending", "confirmed", "denied"].freeze 

    STATUSES.each do |status|
        const_set(status.upcase, status)
    end

    def to_json
        {
            "status" => status,
            "uuid" => uuid
        }.to_json
    end
end
