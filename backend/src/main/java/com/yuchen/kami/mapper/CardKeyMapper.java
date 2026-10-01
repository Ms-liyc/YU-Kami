package com.yuchen.kami.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuchen.kami.entity.CardKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CardKeyMapper extends BaseMapper<CardKey> {

    @Update("""
            UPDATE card_key SET status = 1, redeem_user = #{redeemUser}, redeem_ip = #{redeemIp},
            redeem_at = NOW(), version = version + 1
            WHERE id = #{id} AND status = 0 AND version = #{version}
            """)
    int redeem(@Param("id") Long id, @Param("version") Integer version,
               @Param("redeemUser") String redeemUser, @Param("redeemIp") String redeemIp);
}
