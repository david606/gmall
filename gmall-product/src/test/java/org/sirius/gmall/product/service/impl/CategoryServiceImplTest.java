package org.sirius.gmall.product.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.sirius.gmall.product.service.CategoryService;
import org.sirius.gmall.product.vo.Catelog2Vo;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class CategoryServiceImplTest {

    @Resource
    CategoryService categoryService;

    // 实验方法测试，代码未写到接口，直接引用实现
    @Resource
    CategoryServiceImpl categoryServiceImpl;

    @Test
    void getLevel1Category() {
        categoryService.getLevel1Category();
    }

    @Test
    void getCatalogJson() {
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        System.out.println("catalogJson = " + catalogJson);
    }

    @Test
    void getCatelogJsonWithRedisTemplate() {
        categoryServiceImpl.getCatalogJsonTest();
    }

    @Test
    public void getCatelogJsonFromDb() {
        Map<String, List<Catelog2Vo>> db = categoryServiceImpl.getCatelogJsonFromDb();
        System.out.println("db = " + db);
    }

    @Test
    void testLog() {
        log.warn("warn");
        log.info("info");
        log.debug("debug");
        log.error("error");
    }
}