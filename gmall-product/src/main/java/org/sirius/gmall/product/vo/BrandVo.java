package org.sirius.gmall.product.vo;

import lombok.Data;

/**
 * 品牌 vo
 *
 * @author david
 */
@Data
public class BrandVo {

    /*
      "brandId": 0,
      "brandName": "string",
     */

    /**
     * 品牌Id
     */
    private Long brandId;

    /**
     * 品牌名
     */
    private String brandName;

}
