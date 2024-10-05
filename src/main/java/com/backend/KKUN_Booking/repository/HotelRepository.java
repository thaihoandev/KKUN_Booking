package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)

    List<Hotel> findByCategory(String category);
    List<Hotel> findByRatingGreaterThanEqual(double rating);
    boolean existsByOwner(User owner);

    @Query("SELECT h FROM Hotel h WHERE LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Hotel> findByLocationContainingIgnoreCase(@Param("location") String location);
}

