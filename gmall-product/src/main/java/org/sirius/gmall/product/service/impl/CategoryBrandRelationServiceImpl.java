package org.sirius.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.sirius.gmall.product.dao.BrandDao;
import org.sirius.gmall.product.dao.CategoryDao;
import org.sirius.gmall.product.entity.BrandEntity;
import org.sirius.gmall.product.entity.CategoryEntity;
import org.sirius.gmall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.Query;

import org.sirius.gmall.product.dao.CategoryBrandRelationDao;
import org.sirius.gmall.product.entity.CategoryBrandRelationEntity;
import org.sirius.gmall.product.service.CategoryBrandRelationService;

import javax.annotation.Resource;


/**
 * @author david
 */
@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Resource
    private BrandDao brandDao;

    @Resource
    private CategoryDao categoryDao;

    @Autowired
    private CategoryBrandRelationDao relationDao;

    @Autowired
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        // 1、查询品牌详细信息
        BrandEntity brandEntity = brandDao.selectById(brandId);
        // 2、查询分类详细信息
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        /*
         将信息保存到categoryBrandRelation中：在关系表中对 [品牌] 和 [分类] 的 Name 做了冗余。
         一般情况下，关系表只保存id 即可，但为了减少不必要查询，添加冗余字段
         这样也会存在一个问题，就是冗余字段的源更新了，也要保证这里的冗余也能及时更新
        */
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        // 保存到数据库中
        this.baseMapper.insert(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId);
        relationEntity.setBrandName(name);
        this.update(relationEntity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId, name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {

        List<CategoryBrandRelationEntity> catelogId = relationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));

        List<BrandEntity> collect = catelogId.stream().map(item -> {
            Long brandId = item.getBrandId();
            // 查询品牌的详情
            return brandService.getById(brandId);
        }).collect(Collectors.toList());

        return collect;
    }

}