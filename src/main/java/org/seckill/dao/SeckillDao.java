package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SeckillDao {

    /*
    * 减库存
    * seckillId 要减的商品的id
    * killTime 秒杀的时间
    * 返回值表示更新的记录行数，如果返回为0说明更新没有成功执行
    * */
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

    /*
    * 根据id查询秒杀对象
    * */
    Seckill queryById(long seckillId);

    /*
    * 根据偏移量查询秒杀商品列表
    * */
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    /*
    * 使用存储过程执行秒杀
    * */
    void killByProcedure(Map<String,Object> paramMap);

}
