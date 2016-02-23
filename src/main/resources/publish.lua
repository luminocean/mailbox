--[[ This is a script for publish routine ]]
local publisherId = KEYS[1]
local event = ARGV[1]
local maxSize = tonumber(ARGV[2])

--[[ save the event for publisher him/herself, prepare for future delete operations ]]
redis.call('LPUSH', 'user:'..publisherId..':published', event)
redis.call('LTRIM', 'user:'..publisherId..':published', 0, maxSize-1)

--[[ get followers of the given user ]]
local followers = redis.call('SMEMBERS', 'user:'..publisherId..':followers')

--[[ iterate ]]
for i,followerId in ipairs(followers) 
do
	--[[ shothand of a follower's mailbox key ]]
	local box = 'user:'..followerId..':mailbox'
	
	--[[ add a event into this follower's mailbox ]]
	local score = redis.call('INCR', box..':counter')
    redis.call('ZADD', box, score, event)
    
    --[[ trim the mailbox to the predefined size ]]
	local size = redis.call('ZCOUNT', box, '-inf', '+inf')
	local maxSize = maxSize
    if size > maxSize then
    	redis.call('ZREMRANGEBYRANK', box, 0, size - maxSize - 1)
    end --[[ requires a space here ]]
end