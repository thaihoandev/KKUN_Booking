package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.converter.StringListConverter;
import com.backend.KKUN_Booking.model.enumModel.BedType;
import com.backend.KKUN_Booking.model.enumModel.RoomType;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private RoomType type;
    private Integer capacity;
    private BigDecimal basePrice;
    private Boolean available;
    @Enumerated(EnumType.STRING)
    private BedType bedType;  // Loại giường
    private Integer bedCount; // Số lượng giường
    private Double area;

    @Convert(converter = StringListConverter.class)
    @Column(name = "room_images", columnDefinition = "TEXT") // Lưu dưới dạng TEXT
    private List<String> roomImages = new ArrayList<>(); // Khởi tạo danh sách

    @ManyToMany
    @JoinTable(
            name = "room_amenity",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomReview> reviews = new ArrayList<>();

    public void addReview(RoomReview review) {
        reviews.add(review);
        review.setRoom(this);
    }

    public double getAverageRating() {
        if (reviews.isEmpty()) return 0;
        return reviews.stream().mapToDouble(Review::calculateOverallRating).average().orElse(0);
    }
    // Getters and Setters

}
