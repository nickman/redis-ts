local ts = math.floor(tonumber(ARGV[1])/1000);
-- local period = tonumber(redis.call('get', 'timeseries.config.period'));
-- local periodCount = tonumber(redis.call('get', 'timeseries.config.live.periods'));
local period = 15;
local periodCount = 40;
local bucket = ts-(ts%period);
local member = bucket%(period*periodCount);
redis.call('sadd','timeseries.live.members', member)
return member;
