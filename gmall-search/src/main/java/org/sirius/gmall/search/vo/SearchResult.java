package org.sirius.gmall.search.vo;

import lombok.Data;
import org.sirius.common.es.SkuEsModel;

import java.util.List;


/**
 * 检索结果 VO
 * <pre>
 * 品牌 list 用于在品牌栏显示;
 * 分类 list 用于在分类栏显示;
 * 其他栏每栏用 AttrVo 表示。
 * </pre>
 *
 * @author david
 */
@Data
public class SearchResult {

    /*
     *  ===================== 返回给页面的数据 ====================
     */

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    private List<Integer> pageNavs;

    /**
     * 当前查询到的结果，所有涉及到的品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询到的结果，所有涉及到的所有属性
     */
    private List<AttrVo> attrs;

    /**
     * 当前查询到的结果，所有涉及到的所有分类
     */
    private List<CatalogVo> catalogs;


    /*
     *  ===================== 面包屑导航数据 ====================
     */


    /**
     * 面包屑导航数据
     */
    private List<NavVo> navs;

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }


    @Data
    public static class BrandVo {

        private Long brandId;

        private String brandName;

        private String brandImg;
    }


    @Data
    public static class AttrVo {

        private Long attrId;

        private String attrName;

        private List<String> attrValue;
    }


    @Data
    public static class CatalogVo {

        private Long catalogId;

        private String catalogName;
    }
}
