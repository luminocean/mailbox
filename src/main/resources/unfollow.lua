local followerId = KEYS[1]
local followeeId = KEYS[2]

--[[ remove follower from followee's followers' set ]]
redis.call('SREM', 'user:'..followeeId..':followers', followerId)

--[[ now remove events of the followee from follower's mailbox ]]
local publishedEvents = redis.call('LRANGE', 'user:'..followeeId..':published', 0, -1)
local box = 'user:'..followerId..':mailbox'

for i,event in ipairs(publishedEvents) 
do
	redis.call('ZREM', box, event)
end