package org.seckill.exception;

/*
* 秒杀关闭异常
* 如果秒杀时间到了 库存没有了 就不能秒杀
* */
public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
