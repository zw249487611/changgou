package com.changgou.goods.service.impl;

import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.BrandRepository;
import com.changgou.goods.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BrandServiceImpl implements BrandService {

    /*
    @Autowired
    private BrandMapper brandMapper;

    */

    @Autowired
    private BrandRepository brandRepository;

    /**
     * 查询所有：
     */
    public List<Brand> findAll() {
//        return brandMapper.selectAll();
        return brandRepository.findAll();
    }

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    @Override
    public Brand findById(Integer id) {
        Optional<Brand> optional = brandRepository.findById(id);
        if (optional.isPresent()) {
            Brand brand = optional.get();
            return brand;
        }
        return null;
    }

    /**
     * 增加品牌的实现
     * @param brand
     */
    @Override
    @Transactional
    public void add(Brand brand) {
        brandRepository.save(brand);
    }

    /**
     * 根据id修改品牌
     * @param
     */
    @Override
    @Transactional
    public void update(Brand brand) {
        Optional<Brand> optional = brandRepository.findById(brand.getId());
        if (optional.isPresent()) {
            //如果存在，则更新
            brandRepository.save(brand);
        }

    }

    /**
     * 删除品牌
     * @param id
     */
    @Override
    @Transactional
    public void delete(Integer id) {

        brandRepository.deleteById(id);
    }

    /**
     * 多条件查询品牌
     * @param brand
     * @return
     */
    /*@Override
    public List<Brand> findList(Brand brand) {
        Example<Brand> example = Example.of(brand);

        return brandRepository.findAll(example);
    }*/

    /**
     * 多条件查询品牌
     * @param brand
     * @return
     */
    @Override
    public List<Brand> findList(Brand brand) {
        Specification<Brand> spec = new Specification<Brand>() {
            @Override
            public Predicate toPredicate(Root<Brand> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.like(root.get("name").as(String.class),"%"+brand.getName()+"%");
            }
        };

        List<Brand> brandList = brandRepository.findAll(spec);
        return brandList;
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Brand> findPage(Integer page, Integer size) {
        //当前页，每页显示条数
//        PageHelper.startPage(page, size);
        Pageable pageable = PageRequest.of(page,size);
        //查询集合
        Page<Brand> all = brandRepository.findAll(pageable);
        return all;
    }


    /**
     * 分页+条件查询
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Brand> findPage(Brand brand, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification spec = new Specification() {
            List<Predicate> listPrec = new ArrayList<Predicate>();
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                listPrec.add(cb.like(root.get("name").as(String.class), "%" + brand.getName() + "%"));
                listPrec.add(cb.equal(root.get("letter").as(String.class), brand.getLetter()));
                Predicate[] p = new Predicate[listPrec.size()];
                return cb.and(listPrec.toArray(p));
            }
        };
        Page<Brand> all = brandRepository.findAll(spec, pageable);
        return all;
    }


}
