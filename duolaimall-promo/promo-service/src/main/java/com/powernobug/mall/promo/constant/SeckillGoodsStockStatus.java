package com.powernobug.mall.promo.constant;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.promo.constant
 * @author: HuangWeiLong
 * @date: 2024/10/16 20:49
 */
public enum SeckillGoodsStockStatus {

    HAS_STOCK("1","有库存"),
    STOCK_NOT_ENOUGH("0","没有库存")

    ;

    SeckillGoodsStockStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    String code;
    String desc;
}