package com.stevechao.tx_service.repository;

import com.stevechao.tx_service.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {

}
