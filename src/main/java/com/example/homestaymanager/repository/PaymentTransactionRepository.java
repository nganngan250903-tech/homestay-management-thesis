package com.example.homestaymanager.repository;

import com.example.homestaymanager.enums.PaymentTransactionStatus;
import com.example.homestaymanager.model.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    @Query(value = """
            select p from PaymentTransaction p
            left join fetch p.booking b
            left join fetch b.customer
            left join fetch b.room r
            left join fetch r.roomType
            where (:status is null or p.status = :status)
              and (:provider is null or lower(p.provider) = lower(:provider))
              and (:bookingId is null or b.id = :bookingId)
            order by p.createdAt desc, p.id desc
            """,
            countQuery = """
                    select count(p) from PaymentTransaction p
                    left join p.booking b
                    where (:status is null or p.status = :status)
                      and (:provider is null or lower(p.provider) = lower(:provider))
                      and (:bookingId is null or b.id = :bookingId)
                    """)
    Page<PaymentTransaction> findByFilters(
            @Param("status") PaymentTransactionStatus status,
            @Param("provider") String provider,
            @Param("bookingId") Integer bookingId,
            Pageable pageable);
}
