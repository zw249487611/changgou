package com.changgou.search.dao;

import com.changgou.goods.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository//可以不加这个注解。。
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {

}
