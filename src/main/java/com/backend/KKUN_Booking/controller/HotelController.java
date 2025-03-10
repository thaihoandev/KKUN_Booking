package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.NearbyPlaceDto;
import com.backend.KKUN_Booking.dto.RoomDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.enumModel.BedType;
import com.backend.KKUN_Booking.model.enumModel.HotelCategory;
import com.backend.KKUN_Booking.model.enumModel.PaymentPolicy;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.HotelService;
import com.backend.KKUN_Booking.service.NearbyPlaceService;
import com.backend.KKUN_Booking.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final NearbyPlaceService nearbyPlaceService;
    private final RoomService roomService;
    private final BookingService bookingService;

    public HotelController(HotelService hotelService, NearbyPlaceService nearbyPlaceService, RoomService roomService, BookingService bookingService) {
        this.hotelService = hotelService;
        this.nearbyPlaceService = nearbyPlaceService;
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        List<HotelDto> hotels = hotelService.getAllHotels();
        return new ResponseEntity<>(hotels, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public HotelDto getHotelById(@PathVariable UUID id) {
        return hotelService.getHotelById(id);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<HotelDto> createHotel(
        @ModelAttribute HotelDto hotelDto,
        @RequestParam(value = "exteriorImageList", required = false) MultipartFile[] exteriorImageList,
        Principal principal) throws IOException {

        // Get the email or username of the authenticated user
        String userEmail = principal.getName();

        // Pass the data to the service layer
        HotelDto createdHotel = hotelService.createHotel(hotelDto, exteriorImageList, userEmail);

        return new ResponseEntity<>(createdHotel, HttpStatus.CREATED);
    }

    @PostMapping(value = "/create-hotel-rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HotelDto> createHotelAndRooms(
            @RequestPart("hotel") HotelDto hotelDto,
            @RequestParam(value = "exteriorImages", required = false) MultipartFile[] exteriorImages,
            @RequestParam(value = "roomImages", required = false) MultipartFile[] roomImages,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userEmail = userDetails.getUsername();
        HotelDto createdHotel = hotelService.createHotelAndRooms(hotelDto, exteriorImages, roomImages, userEmail);
        return ResponseEntity.ok(createdHotel);
    }

    @PutMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HotelDto> updateHotel(
            @PathVariable UUID id,
            @ModelAttribute HotelDto hotelDto,
            @RequestParam(value = "exteriorImageList", required = false) MultipartFile[] exteriorImageList,
            Principal principal) {

        // Lấy email hoặc username từ token JWT
        String userEmail = principal.getName(); // Thường là email hoặc username
        HotelDto updatedHotel = hotelService.updateHotel(id, hotelDto, exteriorImageList, userEmail);
        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/delete")
    public void deleteHotel(@PathVariable UUID id) {
        hotelService.deleteHotel(id);
    }

    @GetMapping("/{hotelId}/nearby-places")
    public ResponseEntity<List<NearbyPlaceDto>> findNearbyPlaces(
            @PathVariable String hotelId,
            @RequestParam String address
    ) {


        List<NearbyPlaceDto> nearbyPlaces = nearbyPlaceService.findNearbyNotablePlaces(address);
        return ResponseEntity.ok(nearbyPlaces);
    }

    @GetMapping("/{hotelId}/available-room")
        public ResponseEntity<List<RoomDto>> findRoomAvailableByHotel(
            @PathVariable UUID hotelId,
            @RequestParam String checkinDate,
            @RequestParam String checkoutDate
    ) {
        LocalDateTime parsedCheckinDate = LocalDateTime.parse(checkinDate);
        LocalDateTime parsedCheckoutDate = LocalDateTime.parse(checkoutDate);
        List<RoomDto> availableRooms = roomService.findAvailableRooms(hotelId, parsedCheckinDate, parsedCheckoutDate);
        return ResponseEntity.ok(availableRooms);
    }

    @GetMapping("/booking-hotel/history")
    public ResponseEntity<?> getBookingHistory(Principal principal) {
        String userEmail = principal.getName();
        try {
            List<BookingDto> bookingDtos = bookingService.getHotelBookingHistory(userEmail);

            // Kiểm tra nếu danh sách bookingDtos rỗng
            if (bookingDtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Không có lịch sử đặt phòng nào.");
            }

            return ResponseEntity.ok(bookingDtos);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Xử lý các lỗi khác (nếu có)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi trong quá trình xử lý yêu cầu.");
        }
    }
    @GetMapping("/hotel-categories")
    public ResponseEntity<List<Map<String, String>>> getHotelCategories() {
        List<Map<String, String>> bedTypes = Arrays.stream(HotelCategory.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bedTypes);
    }

    @GetMapping("/hotel-policies")
    public ResponseEntity<List<Map<String, String>>> getHotelPolicies() {
        List<Map<String, String>> bedTypes = Arrays.stream(PaymentPolicy.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bedTypes);
    }
}
