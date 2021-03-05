package com.changgou.goods.service;

import com.changgou.goods.goods.pojo.Brand;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /**
     * 查询所有：
     */
    public List<Brand> findAll();

    /**
     * 根据id查询
     */
    public Brand findById(Integer id);

    /**
     * 增加品牌
     */
    public void add(Brand brand);

    /**
     * 根据id修改品牌
     * @param id
     */
    void update(Brand brand);

    /**
     * 删除品牌
     * @param id
     */
    void delete(Integer id);

    /**
     * 多条件查询
     */
    public List<Brand> findList(Brand brand);

    /**
     * 分页查询
     */
    public Page<Brand> findPage(Integer page, Integer size);

    /**
     * 分页+条件查询
     */
    public Page<Brand> findPage(Brand brand, Integer page, Integer size);

}
