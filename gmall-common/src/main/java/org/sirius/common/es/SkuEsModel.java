package org.sirius.common.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Elastic Search 搜索模型
 *
 * @author david
 */
@Data
public class SkuEsModel {

    /**
     * sku
     */
    private Long skuId;

    /**
     * spu
     */
    private Long spuId;

    /**
     * 商品标题
     */
    private String skuTitle;

    /**
     * 商品价格
     */
    private BigDecimal skuPrice;

    /**
     * 商品图片
     */
    private String skuImg;

    /**
     * 销售数量
     */
    private Long saleCount;

    /**
     * 是否有库存
     */
    private Boolean hasStock;

    /**
     * 热点评分
     */
    private Long hotScore;

    /**
     * 品牌
     */
    private Long brandId;

    /**
     * 分类
     */
    private Long catalogId;

    private String brandName;

    /**
     * 品牌图片
     */
    private String brandImg;

    private String catalogName;

    private List<Attrs> attrs;

    @Data
    public static class Attrs {

        private Long attrId;

        private String attrName;

        private String attrValue;

    }

}
