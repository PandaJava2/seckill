package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;

public interface SuccessKilledDao {

    /*
    * 插入秒杀成功的订单明细,可过滤重复（设置主键为联合唯一主键）
    * 返回值表示插入的行数
    * */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /*
    * 根据id查询SuccessKilled并携带秒杀产品对象实体
    * */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId")long seckillId, @Param("userPhone") long userPhone);

}
