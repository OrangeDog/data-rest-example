package com.example.worldpay.repository;

import com.example.worldpay.model.Offer;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffersRepository extends PagingAndSortingRepository<Offer, Long> {
}
