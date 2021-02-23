package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Resource
    BrandDao brandDao;
    @Resource
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationDao categoryBrandRelationDao;
    @Autowired
    BrandServiceImpl brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
       QueryWrapper<CategoryBrandRelationEntity> queryWrapper = new QueryWrapper<>();
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        //1。获取品牌和分类id
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        //2.获取品牌和分类的name
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        //3.进行插入或保存
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        this.baseMapper.insert(categoryBrandRelation);

    }

    @Override
    public void updateCategory(Long catId, String name) {
        //1.创建新的实体对象
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setCatelogName(name);
        categoryBrandRelationEntity.setCatelogId(catId);
        //2.使用updatewrapper找到对应的id找到对应位置,name是实体类自带的，带了哪个进行哪个的更新。
        UpdateWrapper<CategoryBrandRelationEntity> wrapper = new UpdateWrapper<CategoryBrandRelationEntity>();
        wrapper.eq("catelog_id",catId);
        //3.进行数据库更新。
        this.update(categoryBrandRelationEntity,wrapper);
    }

    @Override
    public void updateBrand(Long brandId, String brandName) {
        categoryBrandRelationDao.updateDetail(brandId,brandName);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(final Long catId) {
        List<CategoryBrandRelationEntity> brandRelationEntities = categoryBrandRelationDao.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId)
        );

        List<BrandEntity> brandEntities = brandRelationEntities.stream().map((item) -> {
            Long brandId = item.getBrandId();
            BrandEntity brandEntity = brandService.getById(brandId);
            return brandEntity;
        }).collect(Collectors.toList());

        return brandEntities;
    }


}