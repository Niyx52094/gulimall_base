package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author fishingfreedom
 * @email 601514291@qq.com
 * @date 2020-12-21 20:26:18
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
