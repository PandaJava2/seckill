package org.seckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//秒杀成功实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessKilled {

    private long seckillId;

    private long userPhone;

    private short state;

    private Date createTime;

    //变通
    //多对一，一个秒杀实体对应多个成功记录，例如一百个iphone在秒杀实体里是一个id对应，但在成功秒杀表里对应100个成功订单
    private Seckill seckill;

}
