-- 原子的「去重 + 判斷庫存 + 扣減」
-- KEYS[1] = seckill:stock:{ticketId}   庫存計數
-- KEYS[2] = seckill:users:{ticketId}   已搶購的使用者集合
-- ARGV[1] = userId
-- 回傳: -2 = 重複搶購, -1 = 售完(或未預熱), >=0 = 成功(剩餘庫存)
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -2
end
local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')
if stock <= 0 then
    return -1
end
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return stock - 1
