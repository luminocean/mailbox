--[[ This is a script for publish routine ]]
local leaderId = KEYS[1]
local event = ARGV[1]
local maxSize = tonumber(ARGV[2])

--[[ save the event for leader him/herself, prepare for future delete operations ]]
redis.call('LPUSH', 'user:'..leaderId..':published', event)
redis.call('LTRIM', 'user:'..leaderId..':published', 0, maxSize-1)

--[[ get followers of the given user ]]
local followers = redis.call('SMEMBERS', 'user:'..leaderId..':followers')

--[[ iterate ]]
for i,followerId in ipairs(followers) 
do
	--[[ shothand of a follower's mailbox key ]]
	local box = 'user:'..followerId..':mailbox'
	
	--[[ add a event into this follower's mailbox ]]
	--[[ the reason that we neeed a zset is because we need a set that it has orders ]]
	local score = redis.call('INCR', box..':counter')
    redis.call('ZADD', box, score, event)
    
    --[[ trim the mailbox to the predefined size ]]
	local size = redis.call('ZCOUNT', box, '-inf', '+inf')
    if size > maxSize then
    	redis.call('ZREMRANGEBYRANK', box, 0, size - maxSize - 1)
    end --[[ requires a space here ]]
end