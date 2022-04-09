package org.sirius.gmall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 销售属性 vo
 * @author david
 */
@Data
@ToString
public class SkuItemSaleAttrVo {

    /**
     * 属性id
     */
    private Long attrId;

    /**
     * 属性名
     */
    private String attrName;

    /**
     * <属性值 & sku ids 串> list
     */
    private List<AttrValueWithSkuIdVo> attrValues;

}
