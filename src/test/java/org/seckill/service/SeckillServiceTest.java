package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext.xml"})
public class SeckillServiceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void getById() throws Exception {
        long id = 1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    //集成测试代码完整逻辑，注意可重复执行
    @Test
    public void testSeckillLogic() throws Exception {
        long id = 1002;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            logger.info("exposer={}",exposer);
            long phone = 13502178891L;
            String md5 = exposer.getMd5();
            try {
                SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
                logger.info("result={}",execution);
            } catch (RepeatKillException e){
                logger.error(e.getMessage(),e);
            }
            catch (SeckillCloseException e){
                logger.error(e.getMessage());
            }
        }else{
            //秒杀未开启
            logger.info("exposer={}",exposer);
        }
        //nowTime < startTime没开启
        //exposer=Exposer(
        // exposed=false,
        // md5=null,
        // seckillId=1000, now=1647852881357, start=1667232000000, end=1667232000000)

        //nowTime > startTime开启
        //exposer=Exposer(
        // exposed=true,
        // md5=4ce730c1ed8499e8082859a568afcea7,
        // seckillId=1000, now=0, start=0, end=0)
    }

    @Test
    public void executeSeckill() throws Exception {
        long id = 1000;
        long phone = 13502171128L;
        String md5 = "4ce730c1ed8499e8082859a568afcea7";
        try {
            SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
            logger.info("result={}",execution);
        } catch (RepeatKillException e){
            logger.error(e.getMessage(),e);
        }
        catch (SeckillCloseException e){
            logger.error(e.getMessage());
        }
    }

    @Test
    public void excuteSeckillProcedure(){
        long seckillId = 1002;
        long phone = 1368011101;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
            logger.info(execution.getStateInfo());
        }
    }
}