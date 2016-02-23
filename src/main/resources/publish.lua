local followers = redis.call('SMEMBERS', 'user:'..KEYS[1]..':followers')
for i,v in ipairs(followers) 
do 
    redis.call('LPUSH', 'user:'..v..':mailbox', ARGV[1])
end