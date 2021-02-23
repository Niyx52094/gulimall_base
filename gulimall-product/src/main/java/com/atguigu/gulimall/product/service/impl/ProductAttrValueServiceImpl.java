package com.atguigu.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;

import javax.print.attribute.standard.PrinterMessageFromOperator;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveProductAttr(List<ProductAttrValueEntity> productAttrValueEntities) {
        this.saveBatch(productAttrValueEntities);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListspu(final Long spuId) {
        List<ProductAttrValueEntity> list = this.list(
                new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId)
        );
        return list;
    }

    @Transactional
    @Override
    public void updateAttrListspu(final Long spuId, final List<ProductAttrValueEntity> entities) {
        //新增或者取消一些数据
        //可以先把spuid的那些属性先全部删掉
        //再把这些新属性加上去
        this.baseMapper.delete(
                new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId)
        );
        for(ProductAttrValueEntity entity:entities){
            entity.setSpuId(spuId);
        }

        this.saveBatch(entities);

    }

}