package com.yuchen.kami.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuchen.kami.entity.CardBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CardBatchMapper extends BaseMapper<CardBatch> {

    @Update("UPDATE card_batch SET used_count = used_count + 1 WHERE id = #{batchId} AND used_count < total_count")
    int incrementUsedCount(@Param("batchId") Long batchId);
}
