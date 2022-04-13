package org.sirius.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.sirius.common.es.SkuEsModel;
import org.sirius.common.utils.R;
import org.sirius.gmall.search.config.GmallElasticSearchConfig;
import org.sirius.gmall.search.constant.ElasticSearchConstant;
import org.sirius.gmall.search.feign.ProductFeignService;
import org.sirius.gmall.search.service.MallSearchService;
import org.sirius.gmall.search.vo.AttrResponseVo;
import org.sirius.gmall.search.vo.SearchParam;
import org.sirius.gmall.search.vo.SearchResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/11 上午11:23
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {

        SearchResult searchResult = null;

        try {
            // 1.将用户传过来的检索参数，转换成 ElasticSearch 的 SearchRequest
//            SearchRequest searchRequest = buildSearchRequest(param);
            SearchRequest searchRequest = buildREqeuet(param);

            // 2.请求 ElasticSearch 检索数据
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GmallElasticSearchConfig.COMMON_OPTIONS);

            // 3.将 ElasticSearch 检索结果，封装成 SearchResult Vo 返回
            searchResult = buildSearchResult1(searchResponse, param);
        } catch (IOException e) {
            log.error("An exception occurred in the search: {}", e.getMessage());
            e.printStackTrace();
        }

        return searchResult;
    }

    /**
     * 将用户检索参数转化成 ElasticSearch#SearchRequest
     * <pre>
     * 1.模糊匹配/过滤（按照属性，分类，品牌，价格区间，库存）
     * 2.排序/分页/高亮
     * 3.聚合分析
     * --1).品牌聚合
     * --2).分类聚合
     * --3).属性聚合
     * </pre>
     *
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        // 构建检索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /* 1.模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存） */

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // 1.1 精确检索 sku 标题
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 1.2 精确检索商品分类
        if (null != param.getCatalog3Id()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        // 1.3 精确检索品牌分布
        if (null != param.getBrandId()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        /*
         * 1.4 精确检索商品属性
         * 参数格式：<id>_<attrValue1>[:][attrValue2:attrValue..]
         * 格式说明：1).下划线前是[属性id]; 2).下划线后是属性值（如果有多个属性值，用 ":" 分分隔）。
         * attrs=1_3G:4G:5G&
         * attrs=2_骁龙845&
         * attrs=4_高清屏
         */
        if (null != param.getAttrs() && param.getAttrs().size() > 0) {

            param.getAttrs().forEach(attrStr -> {
                // attrStr 内容为： 1_3G:4G:5G
                String[] attr = attrStr.split(ElasticSearchConstant.UNDERSCORE);
                // 分出[属性Id]
                String attrId = attr[0];

                // 分出[属性值]
                String[] attrValues = attr[1].split(ElasticSearchConstant.COLON);

                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(QueryBuilders.termQuery("attr.attrId", attrId));
                boolQuery.must(QueryBuilders.termsQuery("attr.attrValue", attrValues));

                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            });
        }

        // 1.5 精确检索是否有库存
        if (null != param.getHasStock()) {
            boolean hasStock = param.getHasStock() == 1;
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", hasStock));
        }

        /*
         * 1.6 检索价格范围
         * 格式：1_500 或 _500 或 500_
         * 说明：1). 1到500之间; 2). 不大于500; 3). 大于500
         */
        if (StringUtils.isNotEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");

            String[] prices = param.getSkuPrice().split(ElasticSearchConstant.UNDERSCORE);
            if (prices.length == 2) {
                rangeQueryBuilder.gte(prices[0]).lte(prices[1]);
            } else if (prices.length == 1) {
                if (param.getSkuPrice().startsWith(ElasticSearchConstant.UNDERSCORE)) {
                    rangeQueryBuilder.lte(param.getSkuPrice());
                } else if (param.getSkuPrice().endsWith(ElasticSearchConstant.UNDERSCORE)) {
                    rangeQueryBuilder.gte(param.getSkuPrice());
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);

        /*
         * 2. 排序/分页/高亮
         */

        // 2.1 排序
        if (StringUtils.isNotEmpty(param.getSort())) {

            /*
             * 格式：sort=hotScore_asc 或 sort=hostScore_desc
             * 说明：1）排序字段; 2) 升序/降序
             */
            String sortFieldStr = param.getSort();
            String[] sortFields = sortFieldStr.split(ElasticSearchConstant.UNDERSCORE);
            SortOrder sortOrder = "asc".equalsIgnoreCase(sortFields[1]) ? SortOrder.ASC : SortOrder.DESC;

            // 根据给定的字段名称和排序顺序添加排序。
            searchSourceBuilder.sort(sortFields[0], sortOrder);
        }

        // 2.2 分页
        int currRecords = (param.getPageNum() - 1) * ElasticSearchConstant.PRODUCT_PAGE_SIZE;
        searchSourceBuilder.from(currRecords);
        searchSourceBuilder.size(ElasticSearchConstant.PRODUCT_PAGE_SIZE);

        // 2.3 高亮
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");

            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /*
         * 3. 聚合分析
         */

        // 3.1 品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);

        // 3.1.1 品牌子聚合：品牌名称聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));

        // 3.1.2 品牌子聚合：品牌图片聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));

        searchSourceBuilder.aggregation(brandAgg);

        // 3.2 分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);

        // 3.2.1 分类子聚合：分类名称聚合
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));

        searchSourceBuilder.aggregation(catalogAgg);

        // 3.3 属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        // 3.3.1 属性Id聚合
        NestedAggregationBuilder attrIdAgg = attrAgg.subAggregation(AggregationBuilders.terms("attr_id_agg").field("attrs.attrId"));
        // 3.3.1.1 属性Id：属性名称聚合
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 3.3.1.2 属性Id：属性值聚合
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attr.attrValue").size(50));

        searchSourceBuilder.aggregation(attrAgg);

        log.debug("构建 DSL 语句：{}", searchSourceBuilder.toString());

        String[] indices = new String[]{ElasticSearchConstant.INDEX_NAME};

        return new SearchRequest(indices, searchSourceBuilder);

    }

    /**
     * 将检索结果 ElasticSearch#SearchReponse 转换成用户需求的结果 SearchResult Vo
     *
     * @param response org.elasticsearch.action.search.SearchResponse
     * @return SearchResult 用户需要的数据格式
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult searchResult = new SearchResult();

        // 1.获取检索取的商品信息

        // 1.1检索命中
        SearchHits hits = response.getHits();

        List<SkuEsModel> skuEsModels = new ArrayList<>();

        // 1.2遍历所有商品信息
        if (null != hits.getHits() && hits.getHits().length > 0) {
            Arrays.stream(hits.getHits()).forEach(hit -> {
                // 文档源JSON串
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                // 判断是否检索了关键字，如果是就显示高亮
                if (StringUtils.isNotEmpty(param.getKeyword())) {
                    // 拿到高亮显示的标题
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String skuTitleValue = skuTitle.getFragments()[0].toString();
                    skuEsModel.setSkuTitle(skuTitleValue);
                }

                skuEsModels.add(skuEsModel);
            });
        }

        searchResult.setProducts(skuEsModels);

        // 2.获取检索到的商品属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        attrIdAgg.getBuckets().forEach(bucket -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            // 2.1 获取属性Id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            // 2.2 获取属性名称
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            // 2.3 获取属性的必所有属性值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());

            attrVo.setAttrValue(attrValues);
        });

        searchResult.setAttrs(attrVos);

        // 3.获取商品检索到的品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();

        // 3.1 获取品牌聚合
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        brandAgg.getBuckets().forEach(bucket -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 3.1.1 获取品牌Id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            // 3.1.2 获取品牌名称
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            // 3.1.3 获取品牌图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        });

        searchResult.setBrands(brandVos);

        // 4.获取检索到的分类信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();

        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        catalogAgg.getBuckets().forEach(bucket -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();

            // 4.1 获取分类Id
            long catalogId = bucket.getKeyAsNumber().longValue();
            catalogVo.setCatalogId(catalogId);

            // 4.2 获取分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);

            catalogVos.add(catalogVo);
        });

        searchResult.setCatalogs(catalogVos);

        // 5.设置分页信息

        // 5.1 页码
        searchResult.setPageNum(param.getPageNum());
        // 5.2 总记录数
        long total = response.getHits().getTotalHits().value;
        searchResult.setTotal(total);
        // 5.3 总页码：记录数%页大小，如果能整除，返回页码; 否则 +1页
        long totalPages = total % ElasticSearchConstant.PRODUCT_PAGE_SIZE == 0 ? total / ElasticSearchConstant.PRODUCT_PAGE_SIZE : (total / ElasticSearchConstant.PRODUCT_PAGE_SIZE) + 1;

        searchResult.setTotalPages(((int) totalPages));
        // 5.4 页码导航
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 0; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        // 6.构建面包屑导航

        // 构建面包屑导航-属性
        if (null != param.getAttrs() && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {

                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // 查询参数格式：attrs=2_5存:6寸
                String[] split = attr.split(ElasticSearchConstant.UNDERSCORE);

                // 6.1设置属性Id
                String attrValue = split[1];
                navVo.setNavValue(attrValue);

                // 6.2设置属性名
                long attrId = Long.parseLong(split[0]);
                // 远程获取属性信息
                R r = productFeignService.info(attrId);
                if (r.getCode() == 0) {
                    AttrResponseVo attrResponseVo = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attrResponseVo.getAttrName());
                } else {
                    navVo.setNavName(split[0]);
                }

                /*
                 * 6.3 设置 Link
                 * 取消了这个面包屑以后，我们要跳转到那个地方.将请求地址的url里面的当前置空
                 */
                String queryString = replaceQueryString(param, "attrs", attr);
                navVo.setLink("http://search.gmall.com/list.html?" + queryString);

                return navVo;
            }).collect(Collectors.toList());

            searchResult.setNavs(navVos);
        }

        // 构建面包屑导航-品牌，分类
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = searchResult.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("品牌");
            // 远程查询所有品牌
            R r = productFeignService.getBrands(param.getBrandId());
            if (r.getCode() == 0) {
                List<SearchResult.BrandVo> brand = r.getData("brand", new TypeReference<List<SearchResult.BrandVo>>() {
                });
                StringBuilder builder = new StringBuilder();

                String queryString = "";
                for (SearchResult.BrandVo brandVo : brand) {
                    builder.append(brandVo.getBrandName()).append(";");
                    queryString = replaceQueryString(param, "brandId", brandVo.getBrandId().toString());
                }
                navVo.setNavValue(builder.toString());
                navVo.setLink("http://search.gmall.com/list.html?" + queryString);
            }

            navs.add(navVo);
        }
        return searchResult;
    }

    /**
     * 替换查询字符串
     *
     * @param param
     * @param key
     * @param value
     * @return
     */
    private String replaceQueryString(SearchParam param, String key, String value) {
        String encode = URLEncoder.encode(value, StandardCharsets.UTF_8);
        //浏览器对空格编码和java不一样
        encode = encode.replace("+", "%20");
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }


    private SearchRequest buildREqeuet(SearchParam param) {

        // 构建DSL 查询语句
        String[] indexes = {ElasticSearchConstant.INDEX_NAME};
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // ====================== 1.查询、过滤 =============

        // 1.1模糊匹配
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 1.2过滤

        // 过滤-按照分类
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        // 过滤-按照品牌
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        // 过滤-按照属性
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            // attrs=1_3G:4G:5G
            for (String attrStr : param.getAttrs()) {

                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] entry = attrStr.split("_");
                // 检索的属性id，例如 1
                String attrId = entry[0];
                // 检索的属性值，例如 3G:4G:5G
                String[] attrValues = entry[1].split(":");

                // 为每一个属性生成Nested查询
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId))
                        .must(QueryBuilders.termsQuery("attrs.attrValue", Arrays.asList(attrValues)));
                // 每一个属性相关的信息都得生成一个嵌入式(attr_agg)的过滤(聚合的属性不参与评分)
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);

                boolQuery.filter(nestedQuery);
            }
        }

        // 过滤-按照库存
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // 过滤-按照价格区间
        if (StringUtils.isNotEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] prices = param.getSkuPrice().split("_");
            if (prices.length == 2) { // 对应两种情况：  1_500 / _500
                if (StringUtils.isNotBlank(prices[0])) {
                    rangeQuery.gte(prices[0]);
                }
                // 第二个是一定有值得
                rangeQuery.lte(prices[1]);
            } else if (prices.length == 1) { // 对应1中情况： 500_
                rangeQuery.gte(prices[0]);
            }
            boolQuery.filter(rangeQuery);
        }

        // 封装所有查询条件
        sourceBuilder.query(boolQuery);

        // ====================== 2.排序、分页、高亮 =============

        //2.1 排序
        if (StringUtils.isNotEmpty(param.getSort())) {
            // sort=skuPrice_asc/desc 价格升序/降序
            String[] sort = param.getSort().split("_");
            // 排序字段
            String sortField = sort[0];
            // 升序/降序
            SortOrder order = sort[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(sortField, order);
        }

        // 2.2 分页

        // 分页算法：从第几条记录开始 =（当前页-1）* 每页大小 。例如：第1页 （1-1）*5=0 ；第2页 (2-1)*5=5
        int from = (param.getPageNum() - 1) * ElasticSearchConstant.PRODUCT_PAGE_SIZE;
        sourceBuilder.from(from);
        sourceBuilder.size(ElasticSearchConstant.PRODUCT_PAGE_SIZE);

        // 2.3 高亮

        // 当存在模糊匹配查询才需要高亮
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red;'>");
            builder.postTags("</b>");

            sourceBuilder.highlighter(builder);
        }

        // ====================== 3.聚合分析 ====================
        // 子聚合可以使用父聚合的结果，下面的聚合是先根据id进行一次聚合，再根据聚合结果（id），以子聚合的方式来获取其它值

        // 1.品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 1.1 关于品牌名的子聚合:
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        // 1.2 关于品牌图片的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAgg);

        // 2.分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        // 2.1 关于分类名的子聚合
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAgg);

        // 3.属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");

        // 3.1 关于属性ID聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        attrAgg.subAggregation(attrIdAgg);
        // 3.1.1 关于属性名聚合
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 3.1.2 关于属性值聚合（一个属性的属性值可能很多，例如：CPU:海思、骁龙）
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        sourceBuilder.aggregation(attrAgg);

        System.out.println("sourceBuilder = " + sourceBuilder.toString());

        SearchRequest request = new SearchRequest(indexes, sourceBuilder);

        return request;
    }

    private SearchResult buildSearchResult1(SearchResponse response, SearchParam param) {

        SearchResult result = new SearchResult();


        // 1.设置SKU
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        SearchHits hits = response.getHits();
        if (hits != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String source = hit.getSourceAsString();
                SkuEsModel model = JSON.parseObject(source, SkuEsModel.class);
                if (StringUtils.isNotEmpty(param.getKeyword())) {
                    String skuTitle = hit.getHighlightFields().get("skuTitle").fragments()[0].toString();
                    model.setSkuTitle(skuTitle);
                }
                skuEsModels.add(model);
            }
        }
        result.setProducts(skuEsModels);

        Aggregations aggregations = response.getAggregations();

        // 2.设置分类
        List<SearchResult.CatalogVo> categories = new ArrayList<>();
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            long catalogId = bucket.getKeyAsNumber().longValue();
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            SearchResult.CatalogVo vo = new SearchResult.CatalogVo();
            vo.setCatalogId(catalogId);
            vo.setCatalogName(catalogName);
            categories.add(vo);
        }
        result.setCatalogs(categories);

        // 3.设置品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            long brandId = bucket.getKeyAsNumber().longValue();
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            SearchResult.BrandVo vo = new SearchResult.BrandVo();
            vo.setBrandId(brandId);
            vo.setBrandName(brandName);
            vo.setBrandImg(brandImg);
            brandVos.add(vo);
        }
        result.setBrands(brandVos);

        // 4.设置属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = aggregations.get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            long attrId = bucket.getKeyAsNumber().longValue();
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();

            List<String> attrValues = new ArrayList<>();
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            for (Terms.Bucket valueAggBucket : attrValueAgg.getBuckets()) {
                String attrValue = valueAggBucket.getKeyAsString();
                attrValues.add(attrValue);
            }
            SearchResult.AttrVo vo = new SearchResult.AttrVo();
            vo.setAttrId(attrId);
            vo.setAttrName(attrName);
            vo.setAttrValue(attrValues);
            attrVos.add(vo);
        }
        result.setAttrs(attrVos);

        // 5.设置分页
        result.setPageNum(param.getPageNum());
        long totalRecords = response.getHits().getTotalHits().value;
        result.setTotal(totalRecords);
        long totalPages = totalRecords % ElasticSearchConstant.PRODUCT_PAGE_SIZE == 0 ?
                totalRecords / ElasticSearchConstant.PRODUCT_PAGE_SIZE :
                (totalRecords / ElasticSearchConstant.PRODUCT_PAGE_SIZE + 1);
        result.setTotalPages(((int) totalPages));

        // 分页数字导航
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6.构建面包屑导航（请求时带的属性参数通过面包屑方式展示出来）
        if (param.getAttrs() != null) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] attrArr = attr.split("_");
                String attrId = attrArr[0];
                String attrVal = attrArr[1];

                navVo.setNavValue(attrVal);
                navVo.setNavName(attrId);

                R res = productFeignService.info(Long.parseLong(attrId));
                if (res.getCode() == 0) {
                    AttrResponseVo attrResponseVo = res.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attrResponseVo.getAttrName());
                }

                // 面包屑导航叉掉某个属性条件
                String encode = URLEncoder.encode(attr, StandardCharsets.UTF_8);
                // 浏览器与Java编码差异特殊处理：空格（Java +;Browser %20）
                encode = encode.replace("+", "%20");

                // attrs=5_以官网信息为准&catalogId=225&attrs=3_iPhone%2012%20Pro%20Max
                String queryString = param.get_queryString();
                queryString = queryString.replace("&attrs=" + encode, "");
                queryString = queryString.replace("attrs=" + encode, "");
                navVo.setLink("http://search.gmall.com/list.html?" + queryString);

                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(navVos);
        }

        return result;
    }

}
