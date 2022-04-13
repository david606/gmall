package org.sirius.gmall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 检索参数
 * <pre>
 * 全文检索：skuTitle -> keyword
 * 排序：saleCount（销量）、hotScore（热度分）、skuPrice（价格）
 * 过滤：hasStock、skuPrice 区间、brandId、catalog3Id、attrs
 * 聚合：attrs
 *
 * 示例：
 * keyword=小米&
 * sort=saleCount_desc/asc&
 * hasStock=0/1&
 * skuPrice=400_1900&
 * brandId=1&
 * catalog3Id=1&
 * attrs=1_3G:4G:5G&
 * attrs=2_骁龙845&
 * attrs=4_高清屏
 * </pre>
 *
 * @author david
 */
@Data
public class SearchParam {

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 品牌id,可以多选
     */
    private List<Long> brandId;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件：sort=price/salecount/hotscore_desc/asc
     */
    private String sort;

    /**
     * 是否显示有货: 1 有库存; 0 无库存
     */
    private Integer hasStock;

    /**
     * 价格区间查询
     */
    private String skuPrice;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生的所有查询条件
     */
    private String _queryString;


}
