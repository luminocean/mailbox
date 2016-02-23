local followerId = KEYS[1]
local publisherId = KEYS[2]

--[[ remove follower from publisher's followers' set ]]
redis.call('SREM', 'user:'..publisherId..':followers', followerId)

--[[ now remove events of the publisher from follower's mailbox ]]
local publishedEvents = redis.call('LRANGE', 'user:'..publisherId..':published', 0, -1)
local box = 'user:'..followerId..':mailbox'

for i,event in ipairs(publishedEvents) 
do
	redis.call('ZREM', box, event)
end