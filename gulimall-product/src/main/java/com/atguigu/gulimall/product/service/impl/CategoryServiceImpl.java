package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //组装成父子的树形结构
        List<CategoryEntity> level1Menu=entities.stream().filter((categoryEntity)->{
            //筛选一级菜单
            return categoryEntity.getParentCid()==0;
        }).map((menu)->{
            //每个一级菜单获得二级菜单列表
            menu.setChildren(getChildren(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort())-(menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
            //2.1找到所有的一级分类（parent_id是0）
       return level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1.检查当前删除的菜单，是否被别的地方引用。

        //这个是物理删除
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到categoryId的所有的路径
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findFullPath(Long catelogId) {
        CategoryEntity entity = baseMapper.selectById(catelogId);
        List<Long> path=new ArrayList<>();
        while(entity.getParentCid()!=0){
            path.add(0,catelogId);
            entity= baseMapper.selectById(entity.getParentCid());
            catelogId=entity.getCatId();
        }
        path.add(0,entity.getCatId());
        return path.toArray(new Long[path.size()]);
    }

    @Override
    public void updateDetail(CategoryEntity category) {
        //首先更新自己表中的信息
        this.updateById(category);
        if(!(category.getName()==null||category.getName().length()==0)){
            //随后同步更新pms_category_brand_relation这张表的categoryname数据
            final String name = category.getName();
            final Long catId = category.getCatId();
            categoryBrandRelationService.updateCategory(catId,name);

            //TODO 同步更新其他管理
        }


    }

    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> entities){
        List<CategoryEntity> children=entities.stream().filter((categoryEntity)->{
            return categoryEntity.getParentCid()== root.getCatId();
        }).map((menu)->{
            //每个二级菜单下获得三次菜单列表
            menu.setChildren(getChildren(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort())-(menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }
}