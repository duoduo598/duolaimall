package com.powernobug.mall.search.service.impl;

import com.powernobug.mall.product.dto.CategoryHierarchyDTO;
import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.dto.SkuInfoDTO;
import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.search.client.ProductApiClient;
import com.powernobug.mall.search.converter.GoodsConverter;
import com.powernobug.mall.search.dto.GoodsDTO;
import com.powernobug.mall.search.dto.SearchResponseAttrDTO;
import com.powernobug.mall.search.dto.SearchResponseDTO;
import com.powernobug.mall.search.dto.SearchResponseTmDTO;
import com.powernobug.mall.search.model.Goods;
import com.powernobug.mall.search.model.SearchAttr;
import com.powernobug.mall.search.param.SearchParam;
import com.powernobug.mall.search.repository.GoodsRepository;
import com.powernobug.mall.search.service.SearchService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.search.service.impl
 * @author: HuangWeiLong
 * @date: 2024/10/6 16:30
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    ElasticsearchRestTemplate restTemplate;
    @Autowired
    ProductApiClient productApiClient;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    GoodsConverter goodsConverter;
    static ExecutorService pool = Executors.newFixedThreadPool(10);
    @Override
    public void upperGoods(Long skuId) {
        //商品信息：id，price，默认图片，title
        //品牌属性：id，品牌属性名，品牌属性值
        //平台属性：id，平台属性名。平台属性值
        //分类信息：一级，二级，三级

        Goods goods = new Goods();
        //1.商品信息
        CompletableFuture<SkuInfoDTO> cf1 = CompletableFuture.supplyAsync(new Supplier<SkuInfoDTO>() {
            @Override
            public SkuInfoDTO get() {
                SkuInfoDTO skuInfo = productApiClient.getSkuInfo(skuId);
                goods.setId(skuInfo.getId());
                goods.setDefaultImg(skuInfo.getSkuDefaultImg());
                goods.setTitle(skuInfo.getSkuName());
                goods.setPrice(skuInfo.getPrice().doubleValue());
                return skuInfo;
            }
        }, pool);

        //2.品牌
        CompletableFuture<Void> cf2 = cf1.thenAcceptAsync(skuInfo -> {
            Long tmId = skuInfo.getTmId();
            TrademarkDTO trademark = productApiClient.getTrademark(tmId);
            if (trademark != null) {
                goods.setTmId(trademark.getId());
                goods.setTmName(trademark.getTmName());
                goods.setTmLogoUrl(trademark.getLogoUrl());
            }
        }, pool);

        //3.平台属性
        CompletableFuture<Void> cf3 = CompletableFuture.runAsync(() -> {
            List<PlatformAttributeInfoDTO> attrList = productApiClient.getAttrList(skuId);
            List<SearchAttr> searchAttrList = attrList.stream().map(platformAttributeInfoDTO -> {
                SearchAttr searchAttr = new SearchAttr();
                if (platformAttributeInfoDTO != null) {
                    Long attrId = platformAttributeInfoDTO.getId();
                    String attrName = platformAttributeInfoDTO.getAttrName();
                    String valueName = platformAttributeInfoDTO.getAttrValueList().get(0).getValueName();

                    searchAttr.setAttrId(attrId);
                    searchAttr.setAttrName(attrName);
                    searchAttr.setAttrValue(valueName);
                }
                return searchAttr;
            }).toList();
            goods.setAttrs(searchAttrList);
        }, pool);

        //4.分类
        CompletableFuture<Void> cf4 = cf1.thenAcceptAsync(skuInfo -> {
            CategoryHierarchyDTO categoryView = productApiClient.getCategoryView(skuInfo.getThirdLevelCategoryId());
            goods.setFirstLevelCategoryId(categoryView.getFirstLevelCategoryId());
            goods.setFirstLevelCategoryName(categoryView.getFirstLevelCategoryName());
            goods.setSecondLevelCategoryId(categoryView.getSecondLevelCategoryId());
            goods.setSecondLevelCategoryName(categoryView.getSecondLevelCategoryName());
            goods.setThirdLevelCategoryId(categoryView.getThirdLevelCategoryId());
            goods.setThirdLevelCategoryName(categoryView.getThirdLevelCategoryName());
        }, pool);

        CompletableFuture.allOf(cf2,cf3,cf4).join();
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //1.新建一个zset
        RScoredSortedSet<Object> scoredSortedSet = redissonClient.getScoredSortedSet("hot:score");
        //2.增加得分
        scoredSortedSet.addScore(skuId,1);
        //3.到达一定值，es从redis中更新热度
        Double currentScore = scoredSortedSet.getScore(skuId);
        if(currentScore.intValue()%5==0){
            //查询商品信息
            Optional<Goods> optionalGoods = goodsRepository.findById(skuId);
            Goods goods = optionalGoods.get();
            goods.setHotScore(currentScore.longValue());
            //更新热度
            goodsRepository.save(goods);
        }
    }

    @Override
    public SearchResponseDTO search(SearchParam searchParam) throws IOException {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //1.构建搜索请求
        buildDSl(nativeSearchQueryBuilder,searchParam);

        //2.发起搜索请求
        NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();
        SearchHits<Goods> searchResponse = restTemplate.search(nativeSearchQuery, Goods.class);

        //3.解析搜索结果
        SearchResponseDTO searchResponseDTO=parseResponse(searchResponse,searchParam);
        return searchResponseDTO;
    }

    private void buildDSl(NativeSearchQueryBuilder nativeSearchQueryBuilder, SearchParam searchParam) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.关键字
        String keyword = searchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //2.品牌
        String trademark = searchParam.getTrademark();
        if(StringUtils.isNotBlank(trademark)){
            String[] split = trademark.split(":");
            String tmId=split[0];
            String tmName = split[1];
            TermQueryBuilder tmQueryBuilder = QueryBuilders.termQuery("tmId", tmId);
            boolQueryBuilder.filter(tmQueryBuilder);
        }

        //3.分类查询

        //一级分类
        Long firstLevelCategoryId = searchParam.getFirstLevelCategoryId();
        if(firstLevelCategoryId!=null){
            TermQueryBuilder termQueryBuilder1 = QueryBuilders.termQuery("firstLevelCategoryId", firstLevelCategoryId);
            boolQueryBuilder.filter(termQueryBuilder1);
        }

        //二级分类
        Long secondLevelCategoryId = searchParam.getSecondLevelCategoryId();
        if(secondLevelCategoryId!=null){
            TermQueryBuilder termQueryBuilder2 = QueryBuilders.termQuery("secondLevelCategoryId", secondLevelCategoryId);
            boolQueryBuilder.filter(termQueryBuilder2);
        }
        //三级分类
        Long thirdLevelCategoryId = searchParam.getThirdLevelCategoryId();
        if(thirdLevelCategoryId!=null){
            TermQueryBuilder termQueryBuilder3 = QueryBuilders.termQuery("thirdLevelCategoryId", thirdLevelCategoryId);
            boolQueryBuilder.filter(termQueryBuilder3);
        }
        //4.平台属性
        String[] props = searchParam.getProps();
        if(props != null){
            for (String prop : props) {
                String[] split = prop.split(":");
                String attId = split[0];
                String attValue = split[1];
                String attInfo = split[2];
                BoolQueryBuilder subQueryBuilder = QueryBuilders.boolQuery();

                TermQueryBuilder termQueryBuilder1 = QueryBuilders.termQuery("attrs.attrId", attId);
                TermQueryBuilder termQueryBuilder2 = QueryBuilders.termQuery("attrs.attrValue", attValue);
                subQueryBuilder.filter(termQueryBuilder1).filter(termQueryBuilder2);


                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", subQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        //5.分页
        PageRequest pageRequest = PageRequest.of(searchParam.getPageNo() - 1, searchParam.getPageSize());
        nativeSearchQueryBuilder.withPageable(pageRequest);
        //6.排序
        String order = searchParam.getOrder();
        if(StringUtils.isNotBlank(order)){
            String[] split = order.split(":");
            String orderNum = split[0];
            String orderPricinple = split[1];

            String field= "1".equals(orderNum)?"hotScore":"price";
            SortOrder sortOrder = "asc".equals(orderPricinple)?SortOrder.ASC:SortOrder.DESC;
            //构造
            FieldSortBuilder sortBuilder= SortBuilders.fieldSort(field).order(sortOrder);
            nativeSearchQueryBuilder.withSort(sortBuilder);
        }
        //7.高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        if(StringUtils.isNotBlank(keyword)){
            highlightBuilder.field("title").preTags("<span style=color:red>").postTags("</span>");
            nativeSearchQueryBuilder.withHighlightBuilder(highlightBuilder);
        }
        //8.聚合
        //品牌聚合
        TermsAggregationBuilder tmAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        nativeSearchQueryBuilder.withAggregations(tmAggregationBuilder);

        //平台聚合
        NestedAggregationBuilder platformAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))
                );
        nativeSearchQueryBuilder.withAggregations(platformAggregationBuilder);
        //9.选择字段
        String[] includes={"id","title","price","defaultImg"};
        FetchSourceFilter sourceFilter = new FetchSourceFilter(includes,null);
        nativeSearchQueryBuilder.withSourceFilter(sourceFilter);
    }

    private SearchResponseDTO parseResponse(SearchHits<Goods> searchResponse, SearchParam searchParam) {
        SearchResponseDTO searchResponseDTO = new SearchResponseDTO();
        //1.商品信息
        List<SearchHit<Goods>> searchHits = searchResponse.getSearchHits();
        List<Goods> goodsList = searchHits.stream().map(searchHit -> {
            Goods goods = searchHit.getContent();
            List<String> title = searchHit.getHighlightField("title");
            if (CollectionUtils.isNotEmpty(title)) {
                String newTitle = title.get(0);
                goods.setTitle(newTitle);
            }
            return goods;
        }).toList();
        List<GoodsDTO> goodsDTOS = goodsConverter.goodsPOs2DTOs(goodsList);
        searchResponseDTO.setGoodsList(goodsDTOS);
        //2.品牌信息
        Aggregations aggregations = (Aggregations) searchResponse.getAggregations().aggregations();
        Terms tmIdAgg = aggregations.get("tmIdAgg");
        List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
        List<SearchResponseTmDTO> searchResponseTmDTOS = buckets.stream().map(bucket -> {
            long tmId = bucket.getKeyAsNumber().longValue();

            Map<String, Aggregation> aggregationMap1 = bucket.getAggregations().asMap();
            Terms tmNameAgg1 = (Terms) aggregationMap1.get("tmNameAgg");
            String tmName = tmNameAgg1.getBuckets().get(0).getKeyAsString();

            Map<String, Aggregation> aggregationMap2 = bucket.getAggregations().asMap();
            Terms tmNameAgg2 = (Terms) aggregationMap2.get("tmLogoUrlAgg");
            String tmLogoUrl = tmNameAgg2.getBuckets().get(0).getKeyAsString();

            SearchResponseTmDTO searchResponseTmDTO = new SearchResponseTmDTO();
            searchResponseTmDTO.setTmId(tmId);
            searchResponseTmDTO.setTmName(tmName);
            searchResponseTmDTO.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmDTO;
        }).toList();

        searchResponseDTO.setTrademarkList(searchResponseTmDTOS);
        //3.平台属性
        Nested agg = aggregations.get("attrAgg");
        Map<String, Aggregation> aggregationMap = agg.getAggregations().asMap();
        Terms attrIdAgg = (Terms) aggregationMap.get("attrIdAgg");
        List<? extends Terms.Bucket> buckets1 = attrIdAgg.getBuckets();
        List<SearchResponseAttrDTO> searchResponseAttrDTOS = buckets1.stream().map(bucket -> {
                    long attrId = bucket.getKeyAsNumber().longValue();
                    Map<String, Aggregation> aggregationMap1 = bucket.getAggregations().asMap();
                    Terms agg1 = (Terms) aggregationMap1.get("attrNameAgg");
                    String attrName = agg1.getBuckets().get(0).getKeyAsString();

                    Terms agg2 = (Terms) aggregationMap1.get("attrValueAgg");
                    List<String> attrValueList = agg2.getBuckets().stream().map(bucket1 -> {
                        return bucket1.getKeyAsString();
                    }).toList();
                    SearchResponseAttrDTO searchResponseAttrDTO = new SearchResponseAttrDTO();
                    searchResponseAttrDTO.setAttrId(attrId);
                    searchResponseAttrDTO.setAttrName(attrName);
                    searchResponseAttrDTO.setAttrValueList(attrValueList);
                    return searchResponseAttrDTO;
                }
        ).toList();

        searchResponseDTO.setAttrsList(searchResponseAttrDTOS);

        //4.分页
        long total = searchResponse.getTotalHits();//总记录数
        Integer pageNo = searchParam.getPageNo(); //当前页面
        Integer pageSize = searchParam.getPageSize();//每页显示的内容

        // 计算总页数
        // Math.round() 四舍五入
        // Math.floor() 向下取整
        // Math.ceil()  向上取整（5/2=2.5-----3）
        double v = Double.valueOf(total) / Double.valueOf(pageSize);
        long totalPages = new BigDecimal(Math.ceil(v)).longValue();
        searchResponseDTO.setTotal(total);
        searchResponseDTO.setPageSize(pageSize);
        searchResponseDTO.setPageNo(pageNo);
        searchResponseDTO.setTotalPages(totalPages);
        return searchResponseDTO;
    }
}
