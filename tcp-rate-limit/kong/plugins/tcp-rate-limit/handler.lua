local ngx = ngx
local kong = kong

local plugin = {
    PRIORITY = 1000,
    VERSION = "0.1",
}


function plugin:init_worker()
    kong.log.debug("saying hi from the 'init_worker' handler")
end

function plugin:preread(plugin_conf)
    kong.log.err("saying hi from preread")
    local time_now = ngx.shared.kong:get("time_window")
    local time_diff = 0 

    if not time_now then
        ngx.shared.kong:set("time_window", ngx.now())
    else 
        time_diff = ngx.now() - ngx.shared.kong:get("time_window")
        if time_diff >= plugin_conf.time_interval then
           ngx.shared.kong:set("time_window", ngx.now())
        end
    end

    if not ngx.shared.kong:get("bytes_sent") then
        ngx.shared.kong:set("bytes_sent", 0)
    end

    kong.log.err(ngx.shared.kong:get("bytes_sent"))
    max_size = plugin_conf.max_size

    if ngx.shared.kong:get("bytes_sent") >= max_size and time_diff <= plugin_conf.time_interval then
        kong.log.err(time_diff)
        kong.log.err("Rate Limit exceeded: ", ngx.shared.kong:get("bytes_sent"))
        return ngx.exit(429)
    end
end


function plugin:log(plugin_conf)
    kong.log.err("saying hi from the 'log' handler")
    local bytes_sent = ngx.var.bytes_sent
    local bytes_received = ngx.var.bytes_received
    local remote_addr = ngx.var.remote_addr

    
    local shared_dict = ngx.shared.kong
    local current_size = shared_dict:get("bytes_sent") + bytes_sent
    shared_dict:set("bytes_sent", current_size)

    kong.log.err("log phase: ", shared_dict:get("bytes_sent"))
    kong.log.err("TCP connection from ", remote_addr, " sent ", bytes_sent, " bytes and received ", bytes_received, " bytes")
end

return plugin
