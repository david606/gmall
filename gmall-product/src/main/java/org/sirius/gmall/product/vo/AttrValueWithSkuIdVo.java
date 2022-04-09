package org.sirius.gmall.product.vo;

import lombok.Data;


/**
 * 属性值& sku ids 串
 * @author david
 */
@Data
public class AttrValueWithSkuIdVo {

    /**
     * 属性值
     */
    private String attrValue;
    /**
     * sku ids 串
     */
    private String skuIds;

}
