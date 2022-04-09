/** Copyright 2020 bejson.com */
package org.sirius.gmall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品(spu)保存 vo
 * @author david
 */
@Data
public class SpuSaveVo {

  private String spuName;
  private String spuDescription;
  private Long catalogId;
  private Long brandId;
  private BigDecimal weight;
  private int publishStatus;
  private List<String> desc;
  private List<String> images;
  private Bounds bounds;
  private List<BaseAttrs> baseAttrs;
  private List<Skus> skus;


}
