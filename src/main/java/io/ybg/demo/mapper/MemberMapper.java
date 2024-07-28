package io.ybg.demo.mapper;

import io.ybg.demo.dto.MemberDTO;
import io.ybg.demo.entity.Member;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MemberMapper {
    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Member Update(@MappingTarget Member member, Member update);

    MemberDTO.Info MemeberToInfo(Member member);
    List<MemberDTO.Info> MemeberToInfo(List<Member> member);

    Member UpdateToMember(MemberDTO.Update update);
    Member CreateToMember(MemberDTO.Create member);

}