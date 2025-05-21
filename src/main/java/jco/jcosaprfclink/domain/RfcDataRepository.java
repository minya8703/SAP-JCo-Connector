package jco.jcosaprfclink.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RfcDataRepository extends JpaRepository<RfcData, Long> {
} 