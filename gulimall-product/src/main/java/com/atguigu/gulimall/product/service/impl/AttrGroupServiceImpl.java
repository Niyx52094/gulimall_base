package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrgroupWithAttr;
import org.springframework.beans.BeanUtils;
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

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;



@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        String key =(String) params.get("key");
        //搜索sql语句
        //select * from pms_attr_group where catelog_id = catelogId
        // and (attr_group_id like %keys% or attr_group_name like %keys%)
        if(!StringUtils.isEmpty(key)){
            wrapper.and((wrapper2)->{
                wrapper2.like("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if(catelogId==0){
          //不是三级目录，只展示全部列表
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );

            return new PageUtils(page);

        }else{
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }
    }

    /**
     * 根据三级分类id查出所有的分组一级这些组里面的属性
     * @param catId
     * @return
     */
    @Override
    public List<AttrgroupWithAttr> getAttrGroupWithAttr(final Long catId) {

        List<AttrGroupEntity> attrGroupEntities = this.list(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId)
        );
        List<AttrgroupWithAttr> attrgroupWithAttrList = attrGroupEntities.stream().map((attrGroupEntity) -> {
            AttrgroupWithAttr attrgroupWithAttr = new AttrgroupWithAttr();
            BeanUtils.copyProperties(attrGroupEntity, attrgroupWithAttr);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrgroupWithAttr.getAttrGroupId());
            attrgroupWithAttr.setAttrs(attrs);
            return attrgroupWithAttr;
        }).collect(Collectors.toList());



        return attrgroupWithAttrList;
    }


}