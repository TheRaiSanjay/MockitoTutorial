package com.learning.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.learning.util.ModelMapperUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


public abstract class AbstractDTO<D,E> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String transactionId;
	@JsonFormat
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getModifiedAt() {
		return modifiedAt;
	}
	public void setModifiedAt(LocalDateTime modifiedAt) {
		this.modifiedAt = modifiedAt;
	}
	@JsonIgnore
	protected ModelMapperUtil modelMapper=new ModelMapperUtil();
	@JsonIgnore
	public abstract E toEntity(D dto);
	@JsonIgnore
	public abstract D toDto(E entity);
	@JsonIgnore
	public abstract List<D> toDtoList(List<E> entityList);
	@JsonIgnore
	public abstract List<E> toEntityList(List<D> dtoList);
	
	
	
	

}
