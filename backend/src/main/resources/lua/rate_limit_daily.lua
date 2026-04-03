-- File: backend/src/main/resources/lua/rate_limit_daily.lua
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local expireTime = tonumber(ARGV[2])

local current = redis.call('GET', key)
if current and tonumber(current) >= limit then
    return 0
end

current = redis.call('INCR', key)
if tonumber(current) == 1 then
    redis.call('EXPIRE', key, expireTime)
end

return 1