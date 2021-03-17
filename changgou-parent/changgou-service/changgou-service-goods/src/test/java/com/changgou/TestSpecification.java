package com.changgou;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSpecification {

   /* @Autowired
    private BrandRepository brandRepository;

    @Test
    public void TestSpec() {
        Brand brand = new Brand();
        brand.setName("努比亚");
//        brand.setLetter("Z");
        Specification<Brand> spec = new Specification<Brand>() {
            List<Predicate> listPrec = new ArrayList<Predicate>();
            @Override
            public Predicate toPredicate(Root<Brand> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                listPrec.add(cb.like(root.get("name").as(String.class), "%" + brand.getName() + "%"));
//                listPrec.add(cb.equal(root.get("letter").as(String.class), brand.getLetter()));
                Predicate[] p = new Predicate[listPrec.size()];

                return cb.and(listPrec.toArray(p));

            }
        };
        List<Brand> brandList = brandRepository.findAll(spec);
        for (Brand brand1 : brandList) {
            System.out.println(brand1);
        }
    }

    @Test
    public void TestBean() {
        Brand brand = new Brand();
        brand.setName("中兴");
        brand.setLetter("Z");
        Example<Brand> example = Example.of(brand);
        List<Brand> brands = brandRepository.findAll(example);
        for (Brand brand1 : brands) {
            System.out.println(brand1);

        }
    }

    *//**
     * 分页+条件查询
     *//*
    @Test
    public void TestPageAndSpec() {
        Brand brand = new Brand();
        brand.setName("亚");
//        brand.setLetter("Z");
        int page = 1;
        int size = 3;
        Pageable pageable = PageRequest.of(page, size);
        Specification<Brand> spec = new Specification<Brand>() {
            @Override
            public Predicate toPredicate(Root<Brand> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.like(root.get("name").as(String.class), "%" + brand.getName() + "%");
            }
        };
        Page<Brand> brands = brandRepository.findAll(spec, pageable);
        for (Brand brand1 : brands) {
            System.out.println(brand1);

        }
    }*/
}
