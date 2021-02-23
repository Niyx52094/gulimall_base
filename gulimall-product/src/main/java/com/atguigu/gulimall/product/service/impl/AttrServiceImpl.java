package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.constant.ProductConstant;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrResVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Attr;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Resource
    CategoryDao categoryDao;

    @Resource
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryService categoryService;

    @Resource
    AttrDao attrDao;

    @Transactional
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId,String type) {
        int attrType=type.equals("base")?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode();
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type",attrType);
        if(categoryId!=0){
            wrapper.eq("catelog_id",categoryId);
        }

        String key= (String) params.get("key");
        //select * from pms_attr where attr_name like %key% or attr_id = key
        if(!StringUtils.isEmpty(key)){
            wrapper.and((wrapper1)->{
                wrapper1.like("attr_name",key).or().eq("attr_id",key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );

        List<AttrEntity> records = page.getRecords();
        //转换
        List<AttrResVo> resVos = records.stream().map((attrEntity) -> {
            AttrResVo attrResVo = new AttrResVo();
            BeanUtils.copyProperties(attrEntity, attrResVo);

            //set catlogName
            Long catelogId = attrEntity.getCatelogId();
            CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
            if (categoryEntity != null) {
                attrResVo.setCatelogName(categoryEntity.getName());
            }

            if(type.equals("base")){
                //Set group_name
                Long attrId = attrEntity.getAttrId();
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
                if (attrAttrgroupRelationEntity != null) {
                    Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();

                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                    if (attrGroupEntity != null) {
                        attrResVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }
            return attrResVo;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(resVos);
        return pageUtils;

    }

    @Override
    public void saveAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        //再两个实体类属性名完全相同时可以拷贝,前面的值拷贝后面的值
        BeanUtils.copyProperties(attrVo,attrEntity);
        //1.基本保存：
        this.save(attrEntity);
        //2.保存关联关系
        Long attrGroupId = attrVo.getAttrGroupId();
        if(attrGroupId!=null&&attrEntity.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            Long attrId = attrEntity.getAttrId();//用attrentity的ID
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrId);
            attrAttrgroupRelationEntity.setAttrGroupId(attrGroupId);
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public void removeDetail(List<Long> asList) {
        //首先删除自己
        this.removeByIds(asList);
        // TODO 删除关联项

//        attrAttrgroupRelationDao.selectBatchIds()
    }

    @Override
    public AttrResVo getAttrInfo(Long attrId) {
        //get the attrentity details
        AttrEntity attrEntity = this.getById(attrId);

        AttrResVo attrResVo = new AttrResVo();
        BeanUtils.copyProperties(attrEntity,attrResVo);

        //set the full path category of this attr
        Long catelogId = attrEntity.getCatelogId();
        Long[] categoryFullPath = categoryService.findFullPath(catelogId);

        //set the name of category
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if(categoryEntity!=null){
            attrResVo.setCatelogName(categoryEntity.getName());
        }

        //如果是base类型才需要设置分组名,销售属性直接跳过
        if(attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            //set attrgroup Id
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if(attrAttrgroupRelationEntity!=null){
                attrResVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());

                //set the groupname
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if(attrGroupEntity!=null){
                    attrResVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        //return new AttrResVo instead of AttrEntity.
        attrResVo.setCatelogPath(categoryFullPath);

        return attrResVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {

        AttrEntity attrEntity = new AttrEntity();
        //copy the value to attrEntity, and update
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);

        //同样只有base才有关联,销售属性直接忽略
        if(attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            //update pms_attr_attrgroup_relation table
            Long attrGroupId = attr.getAttrGroupId();
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrGroupId);


            Integer count = attrAttrgroupRelationDao.selectCount(new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if(count>0){
//            修改操作
                attrAttrgroupRelationDao.update(
                        attrAttrgroupRelationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId())
                );
            }else{
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);

            }
        }
    }

    /**
     * 根据分组id查找到关联的所有属性
     * @param attrGroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId)
        );
        List<AttrEntity> attrEntities = relationEntities.stream().map((relationEntity) -> {
            Long attrId = relationEntity.getAttrId();
            AttrEntity attrEntity = attrDao.selectById(attrId);

            return attrEntity;
        }).collect(Collectors.toList());
        return attrEntities;
    }

    /**
     *
     * 获取当前分组没有关联的属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils queryNonAttrListPage( Map<String, Object> params,  Long attrgroupId) {


        //1。当前分组只能关联当前分类当中的属性，找到当前分类
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2.当前分组只能关联别的分组没有引用的属性
            //2.1 当前分类下的其他分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId)
        );
        List<Long> arrtGroupIdsList = attrGroupEntities.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //2.2 这些分组关联的属性
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", arrtGroupIdsList)
        );

                //已经被关联的所有属性Id
            List<Long> attrIdList = relationEntities.stream().map((item) -> {
                return item.getAttrId();
            }).collect(Collectors.toList());

            //2.3 从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(attrIdList!=null){
            wrapper.notIn("attr_id", attrIdList);
        }

        //使用模糊查询
        String key=(String)params.get("key");
        if(key!=null||key.length()!=0){
            wrapper.and((wrapper1)->{
                wrapper1.eq("attr_id",key).or().like("attr_name",key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }
}