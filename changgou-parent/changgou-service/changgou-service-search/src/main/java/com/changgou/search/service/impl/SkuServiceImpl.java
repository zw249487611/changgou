package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.search.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.goods.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /****
     * ElasticSearchTemplate:可以实现索引库的增删改查【高级搜素】
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 导入数据到索引库中
     */
    @Override
    public void importData() {
        //1.Feign调用，查询出List<SKu>
        Result<List<Sku>> skuResult = skuFeign.findAll();

        //2.将List<Sku>转成List<SkuInfo>
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), SkuInfo.class);

        //循环当前list,
        for (SkuInfo skuInfo : skuInfoList) {
            //获取Spec-> Map(String)->Map类型，
            Map<String ,Object> specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            //如果需要生成动态的域，只需要将该域存入到一个Map<String,Object>对象中即可，该Map<String ,Object>的key会生成一个与，域的名字为该Map的key
            //当前Map<String,Object>后面Object的值会作为当前Sku对象该域（Key)对应的值
            skuInfo.setSpecMap(specMap);
        }
        //3.调用dao,实现数据批量导入
        skuEsMapper.saveAll(skuInfoList);
    }

    /**
     * 多条件搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //条件搜索抽取的方法
        NativeSearchQueryBuilder builder = buildBasicQuery(searchMap);


        //数据搜索抽取的方法
        Map<String, Object> resultMap = searchList(builder);
        /*
        //当用户选择了分类，将分类作为搜索条件，则不需要对分类进行分组搜索，因为分组搜索的数据是用于显示分类搜索条件的
        //分类->searchMap->category
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            //分类分组查询抽取的方法
            List<String> categoryList = searchCategoryList(builder);
            resultMap.put("categoryList", categoryList);

        }

        //当用户选择了品牌，将品牌作为搜索条件，则不需要对品牌进行分组搜索，因为分组搜索的数据是用于显示品牌搜索条件的
        //品牌->searchMap->brand
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            //查询品牌集合
            List<String> brandList = searchBrandList(builder);
            resultMap.put("brandList", brandList);
        }

        //当用户选择了品牌，将品牌作为搜索条件，则不需要对品牌进行分组搜索，因为分组搜索的数据是用于显示品牌搜索条件的
        //品牌->searchMap->spec
            //规格查询
            Map<String, Set<String>> specList = searchSpecList(builder);
            resultMap.put("specList", specList);*/
        //分组搜索实现
        Map<String, Object> groupMap = searchGroupList(builder, searchMap);
        resultMap.putAll(groupMap);

        return resultMap;
    }

    //优化代码，减少查询ES次数，抽取方法
    /**
     * 分组查询：分类分组、品牌分组、规格分组
     * @param builder
     * @return
     */
    private Map<String,Object> searchGroupList(NativeSearchQueryBuilder builder,Map<String,String> searchMap) {
        /**
         * 分组查询分类集合
         * .addAggregation();添加一个聚合操作
         * (1)取别名
         * （2）表示根据哪个域进行分组查询
         */
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        /**
         * 获取分组数据
         *  aggregatedPage.getAggregations()：获取的时集合，可以根据多个域进行分组
         *  get("skuCategory")：这里暂时只获取指定域的集合数
         */
        //定义一个Map,存储所有分组结果
        Map<String, Object> groupMapResult = new HashMap<String,Object>();

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = aggregatedPage.getAggregations().get("skuCategory");
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList", categoryList);
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = aggregatedPage.getAggregations().get("skuBrand");
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList", brandList);
        }
        StringTerms specTerms = aggregatedPage.getAggregations().get("skuSpec");

        //三个都要转集合，就抽取下
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> specMap = putAllSpec(specList);
        groupMapResult.put("specList", specMap);

        return groupMapResult;
    }

    ///三个都要转集合，就抽取下

    /**
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    public List<String> getGroupList(StringTerms stringTerms) {
        List<String> groupList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String fieldName = bucket.getKeyAsString();//其中的一个分类名字
            groupList.add(fieldName);
        }
        return groupList;
    }




    /**
     * 条件搜索抽取的方法
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //执行搜素，响应结果给我
        /**
         * 1、搜索条件封装对象
         * 2、搜索的结果集需要转换的类型
         */
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        //BoolQUery：must,must_not,shold
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (searchMap != null && searchMap.size() > 0) {
            //根据关键词搜索
            String keywords = searchMap.get("keywords");
            //如果关键词不为空，则搜索关键词数据
            if (StringUtils.isNotEmpty(keywords)) {
                QueryStringQueryBuilder name = QueryBuilders.queryStringQuery(keywords).field("name");
                boolQueryBuilder.must(name);
            }

            //输入了分类

            if (StringUtils.isNotEmpty(searchMap.get("category"))) {

                boolQueryBuilder.must( QueryBuilders.termQuery("categoryName",searchMap.get("category")));
            }
            //输入了品牌
            if (StringUtils.isNotEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.must( QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }

            //规格过滤实现：因为规格不像上述的东西，直接查询，规格有很多类，每类里有很多参数，
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                //如果key,以spec_开始（这边是自定义的，定好了其他地方统一认定就好） ,则表示规格筛选查询
                if (key.startsWith("spec_")) {
                    //规格条件的值
                    String value = entry.getValue();
                    boolQueryBuilder.must( QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }

            //价格区间过滤
            String price = searchMap.get("price");
            if (StringUtils.isNotEmpty(price)) {
                //去掉中文中的元，以上等汉字
                price = price.replace("元", "").replace("以上", "");
                String[] prices = price.split("-");
                //x一定不为空，y可能为空
                if (prices != null && prices.length > 0) {
                    //price>price[0]
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    //price<prices[1]
                    if (prices.length == 2) {
                        //这个肯定就是区间
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lt(Integer.parseInt(prices[1])));
                    }
                }
            }

            //排序实现
            String sortField = searchMap.get("sortField");   //排序的域
            String sortRule = searchMap.get("sortRule");     //排序的规则
            if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortRule)) {
                builder.withSort(new FieldSortBuilder(sortField)   //指定排序的域
                        .order(SortOrder.valueOf(sortRule)));                  //指定排序的规则
            }


        }

        //分页、用户如果不传分页参数，则默认第一页
        Integer pageNum = coverterPage(searchMap);//默认第一页
        Integer size = 10;//默认查询的数据条数
        builder.withPageable(PageRequest.of(pageNum - 1, size));



        //将BooleQueryBuilder填充倒builder中
        builder.withQuery(boolQueryBuilder);
        return builder;
    }

    /**
     * 接收前端传入的分页参数
     */
    public Integer coverterPage(Map<String, String> searchMap) {
        if (searchMap != null) {
            String pageNum = searchMap.get("pageNum");

            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }


    /**
     * 数据搜索抽取的方法
     * @param builder
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder builder) {
        /**
         * 添加高亮显示
         */
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");  //指定高亮域
        //前缀
        field.preTags("<em style=\"color:red;\">");
        //后缀
        field.postTags("</em>");
        //碎片长度
        field.fragmentSize(100);
        //添加高亮
        builder.withHighlightFields(field);
        

        AggregatedPage<SkuInfo> page = elasticsearchTemplate
                                .queryForPage(builder.build(),         //搜索条件封装
                                        SkuInfo.class,                //数据集合要转换的类型的字节码
//                                        SearchResultMapper         //执行搜索后，将数据的结果集封装到该对象中
                            new SearchResultMapper() {
                                @Override
                                public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                                    //存储所有转换后的高亮数据
                                    List<T> list = new ArrayList<T>();
                                     //先执行查询，获取所有数据->结果集【非高亮数据|高亮数据】
                                    for (SearchHit hit : response.getHits()) {
                                        //分析结果及数据，获取非高亮数据
                                        SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                                        //分析结果集数据，获取高亮数据->只有某个与的高亮数据
                                        HighlightField highlightField = hit.getHighlightFields().get("name");
                                        if (highlightField != null && highlightField.getFragments() != null) {
                                            //高亮数据读取出来
                                            Text[] fragments = highlightField.getFragments();
                                            StringBuffer buffer = new StringBuffer();
                                            for (Text fragment : fragments) {
                                                buffer.append(fragment.toString());
                                            }
                                            //非高亮数据中指定的域替换成高亮数据
                                            skuInfo.setName(buffer.toString());
                                        }
                                        //将数据返回
                                            //将高亮数据添加到集合中
                                        list.add((T) skuInfo);
                                    }
                                    /**
                                     * (1);搜索的集合数据：携带高亮List<T>
                                     * （2）;分页对象信息，Pageable
                                     * （3）搜索记录的总条数：long total
                                     */
                                    return new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits());
                                }
                            });










        //分析结果

        //分页参数--总记录数
        long totalElements = page.getTotalElements();        
        //总页数
        int totalPages = page.getTotalPages();
        //获取结果集
        List<SkuInfo> contents = page.getContent();

        //封装一个Map存储所有数据，并返回
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("rows", contents);
        resultMap.put("total", totalElements);
        resultMap.put("totalPages", totalPages);
        return resultMap;
    }

    /**
     * 分类分组查询抽取的方法
     * @param builder
     * @return
     */
    private List<String> searchCategoryList(NativeSearchQueryBuilder builder) {
        /**
         * 分组查询分类集合
         * .addAggregation();添加一个聚合操作
         * (1)取别名
         * （2）表示根据哪个域进行分组查询
         */
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        /**
         * 获取分组数据
         *  aggregatedPage.getAggregations()：获取的时集合，可以根据多个域进行分组
         *  get("skuCategory")：这里暂时只获取指定域的集合数
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");
        List<String> categoryList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String categoryName = bucket.getKeyAsString();//其中的一个分类名字
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    /**
     * 品牌分组查询抽取的方法
     * @param builder
     * @return
     */
    private List<String> searchBrandList(NativeSearchQueryBuilder builder) {
        /**
         * 分组查询分类集合
         * .addAggregation();添加一个聚合操作
         * (1)取别名
         * （2）表示根据哪个域进行分组查询
         */
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        /**
         * 获取分组数据
         *  aggregatedPage.getAggregations()：获取的时集合，可以根据多个域进行分组
         *  get("skuCategory")：这里暂时只获取指定域的集合数
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuBrand");
        List<String> BrandList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();//其中的一个分类名字
            BrandList.add(brandName);
        }
        return BrandList;
    }


    /**
     * 规格分组查询抽取的方法
     * @param builder
     * @return
     */
    private Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder builder) {
        /**
         * 分组查询规格集合
         * .addAggregation();添加一个聚合操作
         * (1)取别名
         * （2）表示根据哪个域进行分组查询
         */
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        /**
         * 获取分组数据
         *  aggregatedPage.getAggregations()：获取的时集合，可以根据多个域进行分组
         *  get("skuCategory")：这里暂时只获取指定域的集合数
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuSpec");
        List<String> specList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String specName = bucket.getKeyAsString();//其中的一个分类名字
            specList.add(specName);
        }

        //抽取一个方法，将数据规格分类合并
        Map<String, Set<String>> allSpec = putAllSpec(specList);

        return allSpec;
    }

    /***
     * 抽取一个方法，规格汇总合并
     * @param specList
     * @return
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        //合并后的MApddu对象:将每隔Map对象合成一个Map<String,Set<String>>
        Map<String, Set<String>> allSpec = new HashMap<String,Set<String>>();
        //1.循环specList
        for (String spec : specList) {
            //2.将每隔json字符串转成Map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);

            //3、合并流程
            for (Map.Entry<String, String> entry : specMap.entrySet()) {

                //3.1、取出dangqianMAp,并且获取对应的Key,以及对应的value
                String key = entry.getKey();//规格名字
                String value = entry.getValue();//规格值
                //3.2、将当前循环的数据合并到一个Map<Sting,Set<String>>中
                    //从allSpec中获取当前规格对应的Set集合数据
                Set<String> specSet = allSpec.get(key);
                if (specSet == null) {
                    //之前allsPec中们没有该规格
                    specSet = new HashSet<String>();

                }
                specSet.add(value);
                allSpec.put(key, specSet);
            }
        }
        return allSpec;
    }

}
