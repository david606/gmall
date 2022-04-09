package org.sirius.common.constant;

/**
 * 商品常量
 *
 * @author david
 **/
public class ProductConstant {
    /**
     * 商品属性
     */
    public enum AttrEnum {

        /**
         * 基本属性 1
         */
        ATTR_TYPE_BASE(1, "基本属性"),

        /**
         * 销售属性 0
         */
        ATTR_TYPE_SALE(0, "销售属性");

        private final int code;

        private final String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }

    /**
     * 商品状态属性
     */
    public enum ProductStatusEnum {
        /**
         * 新建商品 0
         */
        NEW_SPU(0, "新建"),
        /**
         * 商品上架 1
         */
        SPU_UP(1, "商品上架"),
        /**
         * 商品下架 2
         */
        SPU_DOWN(2, "商品下架"),
        ;

        private final int code;

        private final String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        ProductStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }
}
