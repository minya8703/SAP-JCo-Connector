package jco.jcosaprfclink.repository;

import jco.jcosaprfclink.domain.StateTaxinvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxinvoiceStateRepository extends JpaRepository<StateTaxinvoice, String> {
}
