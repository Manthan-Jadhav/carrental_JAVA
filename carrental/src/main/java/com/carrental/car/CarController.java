package com.carrental.car;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CarController {

    private final CarRepository carRepository;
    private final JdbcTemplate jdbcTemplate;

    public CarController(
            CarRepository carRepository,
            JdbcTemplate jdbcTemplate) {
        this.carRepository = carRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void populateCarImages(List<Car> cars) {
        if (cars == null) return;
        for (Car car : cars) {
            List<String> images = jdbcTemplate.query(
                    "SELECT image_url FROM CAR_IMAGE " +
                            "WHERE car_id = ? LIMIT 1",
                    (rs, rowNum) -> rs.getString("image_url"),
                    car.getCarId()
            );

            String imageUrl = images.isEmpty()
                    ? "/img/swift_front.jpg"
                    : images.get(0);

            if (!imageUrl.startsWith("/")
                    && !imageUrl.startsWith("http")) {
                imageUrl = "/img/" + imageUrl;
            }

            car.setImageUrl(imageUrl);
        }
    }

    @GetMapping("/cars")
    public String showCars(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) Integer seats,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String pickupDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(required = false) String rentalMode,
            Model model) {

        if (!sortOrder.equals("desc")) {
            sortOrder = "asc";
        }

        List<Car> cars = carRepository.findAvailableCarsWithFuelFilter(
                "Available", branchId, categoryId, seats, fuelType, sortOrder);
        populateCarImages(cars);

        List<CarCategory> categories = jdbcTemplate.query(
                "SELECT category_id, category_name " +
                        "FROM CAR_CATEGORY ORDER BY category_name",
                (rs, rowNum) -> {
                    CarCategory category = new CarCategory();
                    category.setCategoryId(rs.getInt("category_id"));
                    category.setCategoryName(
                            rs.getString("category_name"));
                    return category;
                }
        );

        List<Integer> seatCapacities = jdbcTemplate.queryForList(
                "SELECT DISTINCT seating_capacity " +
                        "FROM CAR_CATEGORY ORDER BY seating_capacity",
                Integer.class
        );

        List<String> fuelTypes = jdbcTemplate.queryForList(
                "SELECT DISTINCT fuel_type FROM CAR " +
                        "WHERE fuel_type IS NOT NULL AND fuel_type <> '' " +
                        "ORDER BY fuel_type",
                String.class);

        model.addAttribute("pickupDate", pickupDate);
        model.addAttribute("returnDate", returnDate);
        model.addAttribute("rentalMode", rentalMode);
        model.addAttribute("fuelTypes", fuelTypes);
        model.addAttribute("selectedFuelType", fuelType);
        model.addAttribute("seatCapacities", seatCapacities);
        model.addAttribute("cars", cars);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedBranchId", branchId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSortOrder", sortOrder);
        model.addAttribute("selectedSeats", seats);

        return "cars";
    }

    @PostMapping("/cars")
    public String showCarsPost(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) Integer seats,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String pickupDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(required = false) String rentalMode,
            Model model) {

        return showCars(branchId, categoryId, sortOrder, seats, fuelType, pickupDate, returnDate, rentalMode, model);
    }

    @PostMapping("/")
    public String showHomePost(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) Integer seats,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String pickupDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(required = false) String rentalMode,
            Model model) {

        return showCars(branchId, categoryId, sortOrder, seats, fuelType, pickupDate, returnDate, rentalMode, model);
    }

    @GetMapping("/")
    public String showHome(
            @RequestParam(required = false) String pickupDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(required = false) String rentalMode,
            Model model) {

        List<Car> bestSellers = carRepository.findBestSellers("Available", 4);
        populateCarImages(bestSellers);

        List<Car> topPicks = carRepository.findTopPicks("Available", 4);
        populateCarImages(topPicks);

        model.addAttribute("pickupDate", pickupDate);
        model.addAttribute("returnDate", returnDate);
        model.addAttribute("rentalMode", rentalMode);
        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("topPicks", topPicks);

        return "home";
    }

    @GetMapping("/cars/{carId}")
    public String showCarDetails(
            @org.springframework.web.bind.annotation.PathVariable Integer carId,
            @RequestParam(required = false) String pickupDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(required = false) String rentalMode,
            Model model) {

        Car car = carRepository.findById(carId).orElse(null);

        if (car == null) {
            return "redirect:/cars";
        }

        List<String> rawImages = jdbcTemplate.query(
                "SELECT image_url FROM CAR_IMAGE WHERE car_id = ?",
                (rs, rowNum) -> rs.getString("image_url"),
                carId
        );

        List<String> formattedImages = new ArrayList<>();
        for (String img : rawImages) {
            if (img != null && !img.trim().isEmpty()) {
                if (!img.startsWith("/") && !img.startsWith("http")) {
                    img = "/img/" + img;
                }
                formattedImages.add(img);
            }
        }

        if (formattedImages.isEmpty()) {
            formattedImages.add("/img/swift_front.jpg");
        }

        car.setImageUrl(formattedImages.get(0));

        Integer categoryId = car.getCategory() != null ? car.getCategory().getCategoryId() : null;
        List<Car> similarCars = carRepository.findSimilarCars("Available", carId, categoryId, 4);
        populateCarImages(similarCars);

        model.addAttribute("car", car);
        model.addAttribute("images", formattedImages);
        model.addAttribute("similarCars", similarCars);
        model.addAttribute("pickupDate", pickupDate);
        model.addAttribute("returnDate", returnDate);
        model.addAttribute("rentalMode", rentalMode);

        return "car-details";
    }
}

