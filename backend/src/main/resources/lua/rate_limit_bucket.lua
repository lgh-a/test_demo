-- File: backend/src/main/resources/lua/rate_limit_bucket.lua
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local limitInfo = redis.call('HMGET', key, 'tokens', 'timestamp')
local tokens = tonumber(limitInfo[1])
local lastRefreshed = tonumber(limitInfo[2])

if tokens == nil then
    tokens = capacity
    lastRefreshed = now
end

local deltaTokens = math.max(0, (now - lastRefreshed) * rate)
tokens = math.min(capacity, tokens + deltaTokens)

if tokens >= requested then
    tokens = tokens - requested
    redis.call('HMSET', key, 'tokens', tokens, 'timestamp', now)
    local expireTime = math.ceil(capacity / rate) * 2
    redis.call('EXPIRE', key, expireTime)
    return 1
else
    redis.call('HMSET', key, 'timestamp', now)
    return 0
end