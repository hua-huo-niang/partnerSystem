--传入一个列表，奇数是key，偶数是value。用2作为步长
if #ARGV == 0 then
    return 0
end
local count = 0;
for i = 1, #ARGV, 2 do
    redis.call('HSET', KEYS[1], ARGV[i], ARGV[i+1])
    count = count+1
end
return tonumber(count)
