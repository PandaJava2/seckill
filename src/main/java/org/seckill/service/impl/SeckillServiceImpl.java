package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*
* Service实现类操作DAO
* */
@Service
public class SeckillServiceImpl implements SeckillService {
    //日志
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //装配
    @Autowired//根据类型装配
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //md5盐值字符串，用于混淆md5
    private final String slat = "sasfhjknmwhqriuhk^#$^#412akwfnh49!";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    //秒杀地址指的是Exposer中的(id,phone,md5)构造器，只有符合条件才会初始化这个构造
    //而执行秒杀的传入参数正是(id,phone,md5)，总的来说就是为了获得md5，每一个秒杀产品特有一个md5
    public Exposer exportSeckillUrl(long seckillId) {
        //所有的秒杀单都需要调用暴露接口方法，用redis缓存起来降低数据库访问的压力
        //优化数据库操作，通过主键访问
        /*
        * get from cache 从缓存中拿
        * if null 如果不存在
        *   get db 从db里拿
        *       put cache 然后放到缓存
        * else 如果cache中存在
        *   logic 执行逻辑代码
        * */
        //优化点：缓存优化:超时的基础上维护一致性
        //1:访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            //2:访问数据库
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null){
                return new Exposer(false,seckillId);
            }else{
                //3:存入redis
                redisDao.putSeckill(seckill);
            }
        }
//        seckill = seckillDao.queryById(seckillId);
//        if(seckill == null){
//            return new Exposer(false,seckillId);
//        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //当前系统时间
        Date nowTime = new Date();
        if(nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    //为什么抽象成方法 因为用户也要传入数据转为md5验证
    private String getMD5(long seckillId){
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    //这里的参数md5是用户输入的
    @Override
    @Transactional
    /*
    * 使用注解控制事务方法的优点：
    * 1:开发团队达成一致约定，明确标注事务方法的编程风格
    * 2:保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
    * 3:不是所有的方法都需要事务，只有一条修改操作，只读操作不需要事务控制。
    * */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if(md5 == null || !md5.equals(getMD5(seckillId))){//如果md5为null或者md5不匹配
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }
        //经过上面判断seckillId和md5都匹配，执行秒杀逻辑：减库存 + 记录秒杀成功
        Date nowTime = new Date();
        //nowTime与秒杀的endTime作比较，看一下执行秒杀的时候是否秒杀已结束

        //为什么要try catch:因为可能出现其他的异常如插入成功订单超时，数据库断开
        try{
            int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
            //唯一：Id + phone
            if(insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else{
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
                if(updateCount <= 0){
                    //如果更新值小于等于0说明没有更新到记录,意味秒杀结束
                    throw new SeckillCloseException("seckill is closed");
                }else{
                    //减库存成功，记录购买行为
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,successKilled);
                }
            }

        } catch (RepeatKillException e2){
            throw e2;
        } catch (SeckillException e1){
            throw e1;
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            //所有编译期异常 转化为运行期异常
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId,long userPhone,String md5) {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStateEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        HashMap<String, Object> map = new HashMap<>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        //执行存储过程，result被复制
        try {
            seckillDao.killByProcedure(map);
            //获取result
            Integer result = MapUtils.getInteger(map, "result", -2);
            if(result == 1){
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId,SeckillStateEnum.SUCCESS,sk);
            }else{
                return new SeckillExecution(seckillId,SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStateEnum.INNER_ERROR);
        }
    }
}
