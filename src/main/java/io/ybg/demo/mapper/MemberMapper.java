package io.ybg.demo.mapper;

import io.ybg.demo.dto.MemberDTO;
import io.ybg.demo.entity.MemberEntity;
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
    MemberEntity Update(@MappingTarget MemberEntity memberEntity, MemberEntity update);

    MemberDTO.InfoMemberDTO MemberToInfo(MemberEntity memberEntity);

    List<MemberDTO.InfoMemberDTO> MemberToInfo(List<MemberEntity> memberEntity);

    MemberEntity UpdateToMember(MemberDTO.UpdateMemberDTO updateDTO);

    MemberEntity CreateToMember(MemberDTO.CreateMemberDTO member);

}