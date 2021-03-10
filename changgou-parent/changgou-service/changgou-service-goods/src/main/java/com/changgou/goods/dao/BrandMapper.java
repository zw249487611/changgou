package com.changgou.goods.dao;

import com.changgou.search.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Dao使用通用Mapper，Dao接口需要继承tk.mybatis.mapper.common.Mapper
 *      增加数据:调用Mapper.insert()
 *      增加数据：调用Mapper.insertSelective()
 *
 *      修改数据：调用Mapper.update(T)
 *      修改数据：调用Mapper.updateByPrimaryKey(T)
 *
 *      查询数据：根据ID查询：Mapper.selectByPrimaryKey(ID)
 *      查询数据：条件查询：Mapper.select(T)
 *
 * 继承完了就可以使用通用Mapper么？？不能
 *  还需要再启动类上启动通用mapper
 */
public interface BrandMapper extends Mapper<Brand> {


    /**
     * 根据分类id查询品牌集合
     * @param categoryid:分类集合
     * @return
     */
    @Select("select tb.* from tb_brand tb,tb_category_brand tcb where tb.id=tcb.brand_id and tcb.category_id=#{pid}")
    List<Brand> findByCategory(Integer categoryid);
}
