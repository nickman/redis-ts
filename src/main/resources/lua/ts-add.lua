local typeMap = ${tsTypes};
local period = ${periodDuration};   
local periodCount = ${periodCount};
local ts = tonumber(ARGV[1]);
local bucket = ts-(ts%period);
local member = bucket%(period*periodCount);
redis.call('sadd','timeseries.live.members', member)
return member;
