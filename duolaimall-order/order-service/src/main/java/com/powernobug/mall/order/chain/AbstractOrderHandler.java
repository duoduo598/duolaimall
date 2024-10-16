package com.powernobug.mall.order.chain;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.chain
 * @author: HuangWeiLong
 * @date: 2024/10/16 21:12
 */
public abstract class AbstractOrderHandler {
    //声明下一个节点
    public AbstractOrderHandler next;
    //设置下一个节点的方法
    public void setNext(AbstractOrderHandler handler){
        this.next=handler;
    }
    //责任链过程中需要的参数
    public abstract void handle(SaveOrderContext saveOrderContext);
}
