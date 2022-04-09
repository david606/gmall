package org.sirius.gmall.product.vo;

import lombok.Data;

/**
 * Attr 响应 vo
 *
 * @author david
 */
@Data
public class AttrRespVo extends AttrVo {
    /*
      "catelogName": "手机/数码/手机",
      "groupName": "主体",
     */

    /**
     * 属性所属分类名
     */
    private String catelogName;

    /**
     * 属性所属分组名
     */
    private String groupName;

    /**
     * 分类路径
     */
    private Long[] catelogPath;

}
