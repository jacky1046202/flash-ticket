-- 補償回滾: DB 下單失敗時還原 Redis 的扣減
-- KEYS[1] = seckill:stock:{ticketId}
-- KEYS[2] = seckill:users:{ticketId}
-- ARGV[1] = userId
redis.call('INCR', KEYS[1])
redis.call('SREM', KEYS[2], ARGV[1])
return 1
