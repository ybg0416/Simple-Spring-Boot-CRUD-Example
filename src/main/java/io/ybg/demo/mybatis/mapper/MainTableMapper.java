package io.ybg.demo.mybatis.mapper;

import io.ybg.demo.mybatis.entity.MainTable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MainTableMapper {
    List<MainTable> selectAllMainWithSub();

    MainTable selectMainById(Long id);
}