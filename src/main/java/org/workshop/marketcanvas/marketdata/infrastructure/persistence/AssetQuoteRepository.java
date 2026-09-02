package org.workshop.marketcanvas.marketdata.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public interface AssetQuoteRepository extends JpaRepository<AssetQuoteEntity, String> {
    List<AssetQuoteEntity> findByTickerIn(Collection<String> tickers);

}
