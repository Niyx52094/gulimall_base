package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String)params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if(!(key==null||key.length()==0)){
            //select * from `pms_brand` where brand_id name descript first_letter like %key%
            wrapper.like("brand_id",key).or().like("name",key).or().like("descript",key).or().like("first_letter",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void removeMenuIds(List<Long> asList) {
        //逻辑删除。
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public void updateDetail(BrandEntity brand) {
        //1.进行自己的更新
        this.updateById(brand);
        //2.同步更新其他管理
        if(!StringUtils.isEmpty(brand.getName())){
            final Long brandId = brand.getBrandId();
            final String brandName= brand.getName();
            categoryBrandRelationService.updateBrand(brandId,brandName);
            //TODO 同步其他表的管理
        }
    }
}