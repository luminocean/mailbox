local followerId = KEYS[1]
local leaderId = KEYS[2]

--[[ remove follower from leader's followers' set and vice versa ]]
redis.call('SREM', 'user:'..leaderId..':followers', followerId)
redis.call('SREM', 'user:'..followerId..':leaders', leaderId)

--[[ now remove events of the leader from follower's mailbox ]]
local publishedEvents = redis.call('LRANGE', 'user:'..leaderId..':published', 0, -1)
local box = 'user:'..followerId..':mailbox'

for i,event in ipairs(publishedEvents) 
do
	redis.call('ZREM', box, event)
end