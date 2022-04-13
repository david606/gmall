package org.sirius.gmall.search.service;

import org.sirius.gmall.search.vo.SearchParam;
import org.sirius.gmall.search.vo.SearchResult;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/10 下午8:06
 */
public interface MallSearchService {

    /**
     * 根据检索参数，返回检索结果
     * <pre>
     * 1.请求带来的参数是 SearchParam
     * 2.把请求参数封装成 ElasticSearch 请求 SearchRequest
     * 3.ElasticSearch 返回的是 SearchResponse
     * 4.把 SearchResponse 封装为 SearchResult Vo
     * </pre>
     *
     * @param param 检索参数
     * @return SearchResult 检索结果 vo
     */
    SearchResult search(SearchParam param);
}
