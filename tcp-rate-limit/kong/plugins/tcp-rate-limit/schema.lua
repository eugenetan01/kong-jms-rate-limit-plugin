local typedefs = require "kong.db.schema.typedefs"
local Schema = require "kong.db.schema"
local constants = require "kong.constants"


local stream_protocols = {}
for protocol, subsystem in pairs(constants.PROTOCOLS_WITH_SUBSYSTEM) do
    if subsystem == "stream" then
        stream_protocols[#stream_protocols + 1] = protocol
    end
end
table.sort(stream_protocols)

typedefs.protocols_stream = Schema.define {
    type = "set",
    required = true,
    default = stream_protocols,
    elements = { type = "string", one_of = stream_protocols },
}

-- Grab pluginname from module name
local plugin_name = ({ ... })[1]:match("^kong%.plugins%.([^%.]+)")

local schema = {
    name = plugin_name,
    fields = {
        -- the 'fields' array is the top-level entry with fields defined by Kong
        { consumer = typedefs.no_consumer },
        { protocols = typedefs.protocols_stream },
        {
            config = {
                -- The 'config' record is the custom part of the plugin schema
                type = "record",
                fields = {
                    -- a standard defined field (typedef), with some customizations
                    --[[ {
                        host = {
                            type = "string",
                            default = "1.55.137.70",
                            required = true,
                        },
                    },
                    {
                        port = {
                            type = "string",
                            default = "9225",
                            required = true,
                        },
                    }, ]]
                    {
                         max_size = {
                            type = "integer",
                            default = 800,
                            required = true,
                        },
                    },
                    {
                        time_interval = {
                           type = "integer",
                           default = 60,
                           required = true,
                       },
                   }
                },
            }
        }
    },
}

return schema

