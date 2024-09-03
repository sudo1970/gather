package com.cq.accountmanger.mapper;

import com.cq.accountmanger.domain.FakerDataEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FakerDataMapper {

    @Select("select id, name, email, phone_number, address, latitude, longitude " +
            " from faker_data LIMIT #{pageSize} OFFSET #{offset} ")
    List<FakerDataEntity> getFakerData(@Param("pageSize") int pageSize, @Param("offset") int offset);
}