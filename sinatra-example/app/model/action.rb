# action.rb
class Action
    attr_accessor :id, :uuid, :type, :status, :created_at, :expires_at

    STATUSES = ["waiting_confirmation", "pending", "confirmed", "denied"].freeze
    TYPES = ["sca", "normal"].freeze

    STATUSES.each do |status|
        const_set(status.upcase, status)
    end

    TYPES.each do |type|
        const_set(type.upcase, type)
    end

	def initialize(type = NORMAL, status = PENDING)
		self.created_at = Time.now.getutc
        self.id = (self.created_at.to_f * 1000).to_i.to_s
        self.uuid = self.id
		self.expires_at = self.created_at + 5 * 60

		self.type = type
		self.status = status
    end

    def to_json
        {
            "type" => type,
            "status" => status,
            "id" => id
        }.to_json
    end
end
