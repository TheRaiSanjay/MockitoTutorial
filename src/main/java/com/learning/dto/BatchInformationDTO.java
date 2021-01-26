package com.learning.dto;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.JobExecution;

import com.learning.util.DatesUtil;

public class BatchInformationDTO extends AbstractDTO<BatchInformationDTO, JobExecution> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private String status;
	
	@Override
	public JobExecution toEntity(BatchInformationDTO dto) {
		return modelMapper.map(dto, JobExecution.class);
	}

	@Override
	public BatchInformationDTO toDto(JobExecution entity) {
		BatchInformationDTO dto=modelMapper.map(entity, BatchInformationDTO.class);
		if(entity.getStartTime() !=null)
			dto.setModifiedAt(DatesUtil.toLocalDateTime(entity.getStartTime()));
		if(entity.getCreateTime() !=null)
		dto.setCreatedAt(DatesUtil.toLocalDateTime(entity.getCreateTime()));
		if(entity.getStatus() !=null)
			dto.setStatus(entity.getStatus().name());
		return null;
	}

	@Override
	public List<BatchInformationDTO> toDtoList(List<JobExecution> entityList) {

		return entityList.stream().map(element->modelMapper.map(element, BatchInformationDTO.class)).collect(Collectors.toList());
	}

	@Override
	public List<JobExecution> toEntityList(List<BatchInformationDTO> dtoList) {
		return dtoList.stream().map(element->modelMapper.map(element, JobExecution.class)).collect(Collectors.toList());
	}

}
