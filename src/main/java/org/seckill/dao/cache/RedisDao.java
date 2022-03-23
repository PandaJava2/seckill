package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JedisPool jedisPoll;

    public RedisDao(String ip,int port){
        jedisPoll = new JedisPool(ip,port);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    //如果redis中有的话，从redis中返回Seckill对象，但是redis内部没实现序列化
    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        try {
            Jedis jedis = jedisPoll.getResource();
            try {
                String key = "seckill:" + seckillId;
                //redis并没实现内部序列化操作
                //get-> byte[] -> 反序列化 ->Object(Seckill)
                //采用自定义序列化
                //protostuff : pojo.(有get,set方法标准对象)

                //从redis拿到字节数组
                byte[] bytes = jedis.get(key.getBytes());
                if(bytes != null){
                    //空对象
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    //通过Protostuff将字节数组转换为秒杀对象
                    //seckill 被反序列化
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //如果redis中不存在就放到redis中
    //put方法 需要把一个对象转换为字节数组存到redis中
    public String putSeckill(Seckill seckill){
        // set Object(Seckill) -> 序列化 -> byte[]
        try{
            Jedis jedis = jedisPoll.getResource();
            try{
                String key = "seckill:" + seckill.getSeckillId();
                //LinkedBuffer缓存器，如果数据太大了需要缓存
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout = 60 * 60;//1小时
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;//执行成功返回ok
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

        return null;
    }

}
