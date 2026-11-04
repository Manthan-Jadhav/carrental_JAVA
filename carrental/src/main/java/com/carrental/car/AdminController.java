/* package com.carrental.car;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRepository carRepository;
    private final JdbcTemplate jdbcTemplate;

    public AdminController(
            CarRepository carRepository,
            JdbcTemplate jdbcTemplate) {
        this.carRepository = carRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void populateCarImages(List<Car> cars) {
        if (cars == null) return;

            String imageUrl = images.isEmpty()
                    ? "/img/swift_front.jpg"
                    : images.get(0);

            if (!imageUrl.startsWith("/") && !imageUrl.startsWith("http")) {
                imageUrl = "/img/" + imageUrl;
            }

            car.setImageUrl(imageUrl);
        }
    }

    @GetMapping
    public String showAdminDashboard(
            @RequestParam(required = false) Integer branchId,
            Model model) {

        List<Car> cars = carRepository.findAllCarsForAdmin(branchId);
        populateCarImages(cars);

        List<Branch> branches = jdbcTemplate.query(
                "SELECT branch_id, branch_name FROM BRANCH ORDER BY branch_id",
                (rs, rowNum) -> {
                    Branch b = new Branch();
                    b.setBranchId(rs.getInt("branch_id"));
                    b.setBranchName(rs.getString("branch_name"));
                    return b;
                }
        );

        List<CarCategory> categories = jdbcTemplate.query(
                "SELECT category_id, category_name FROM CAR_CATEGORY ORDER BY category_name",
                (rs, rowNum) -> {
                    CarCategory c = new CarCategory();
                    c.setCategoryId(rs.getInt("category_id"));
                    c.setCategoryName(rs.getString("category_name"));
                    return c;
                }
        );

        long totalCars = cars.size();
        long availableCars = cars.stream()
                .filter(c -> "Available".equalsIgnoreCase(c.getStatus()))
                .count();
        long discontinuedCars = cars.stream()
                .filter(c -> "Discontinued".equalsIgnoreCase(c.getStatus()))
                .count();

        model.addAttribute("cars", cars);
        model.addAttribute("branches", branches);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedBranchId", branchId);
        model.addAttribute("totalCars", totalCars);
        model.addAttribute("availableCars", availableCars);
        model.addAttribute("discontinuedCars", discontinuedCars);

        return "admin";
    }

    @PostMapping("/cars/add")
    public String addCar(
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam Integer year,
            @RequestParam String colour,
            @RequestParam String fuelType,
            @RequestParam String transmission,
            @RequestParam BigDecimal pricePerDay,
            @RequestParam Integer branchId,
            @RequestParam Integer categoryId,
            @RequestParam(required = false) String imageUrl,
            RedirectAttributes redirectAttributes) {

        Car car = new Car();
        car.setBrand(brand.trim());
        car.setModel(model.trim());
        car.setYear(year);
        car.setColour(colour.trim());
        car.setFuelType(fuelType.trim());
        car.setTransmission(transmission.trim());
        car.setPricePerDay(pricePerDay);
        car.setBranchId(branchId);
        car.setCategoryId(categoryId);
        car.setStatus("Available");

        car = carRepository.save(car);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            String img = imageUrl.trim();
            if (!img.startsWith("/") && !img.startsWith("http")) {
                img = "/img/" + img;
            }
            jdbcTemplate.update(
                    "INSERT INTO CAR_IMAGE (car_id, image_url) VALUES (?, ?)",
                    car.getCarId(),
                    img
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO CAR_IMAGE (car_id, image_url) VALUES (?, ?)",
                    car.getCarId(),
                    "/img/swift_front.jpg"
            );
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "New car '" + car.getBrand() + " " + car.getModel() + "' successfully added to the fleet!"
        );

        return "redirect:/admin" + (branchId != null ? "?branchId=" + branchId : "");
    }

    @PostMapping("/cars/{carId}/price")
    public String updateCarPrice(
            @PathVariable Integer carId,
            @RequestParam BigDecimal pricePerDay,
            @RequestParam(required = false) Integer branchId,
            RedirectAttributes redirectAttributes) {

        Car car = carRepository.findById(carId).orElse(null);
        if (car != null) {
            car.setPricePerDay(pricePerDay);
            carRepository.save(car);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Daily rate for " + car.getBrand() + " " + car.getModel() + " updated to ₹" + pricePerDay + "/day."
            );
        }

        return "redirect:/admin" + (branchId != null ? "?branchId=" + branchId : "");
    }

    @PostMapping("/cars/{carId}/status")
    public String toggleCarStatus(
            @PathVariable Integer carId,
            @RequestParam String status,
            @RequestParam(required = false) Integer branchId,
            RedirectAttributes redirectAttributes) {

        Car car = carRepository.findById(carId).orElse(null);
        if (car != null) {
            car.setStatus(status);
            carRepository.save(car);
            String action = "Discontinued".equalsIgnoreCase(status) ? "discontinued" : "activated";
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    car.getBrand() + " " + car.getModel() + " is now " + action + "."
            );
        }

        return "redirect:/admin" + (branchId != null ? "?branchId=" + branchId : "");
    }

    @PostMapping("/cars/{carId}/image")
    public String addCarImage(
            @PathVariable Integer carId,
            @RequestParam String imageUrl,
            @RequestParam(required = false) Integer branchId,
            RedirectAttributes redirectAttributes) {

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            String img = imageUrl.trim();
            if (!img.startsWith("/") && !img.startsWith("http")) {
                img = "/img/" + img;
            }
            jdbcTemplate.update(
                    "INSERT INTO CAR_IMAGE (car_id, image_url) VALUES (?, ?)",
                    carId,
                    img
            );
            redirectAttributes.addFlashAttribute("successMessage", "New photo added for Car #" + carId + ".");
        }

        return "redirect:/admin" + (branchId != null ? "?branchId=" + branchId : "");
    }

    @PostMapping("/cars/{carId}/update")
    public String updateCarDetails(
            @PathVariable Integer carId,
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam Integer year,
            @RequestParam String colour,
            @RequestParam String fuelType,
            @RequestParam String transmission,
            @RequestParam BigDecimal pricePerDay,
            @RequestParam Integer branchId,
            @RequestParam Integer categoryId,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        Car car = carRepository.findById(carId).orElse(null);
        if (car != null) {
            car.setBrand(brand.trim());
            car.setModel(model.trim());
            car.setYear(year);
            car.setColour(colour.trim());
            car.setFuelType(fuelType.trim());
            car.setTransmission(transmission.trim());
            car.setPricePerDay(pricePerDay);
            car.setBranchId(branchId);
            car.setCategoryId(categoryId);
            car.setStatus(status);
            carRepository.save(car);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Details for " + car.getBrand() + " " + car.getModel() + " updated successfully."
            );
        }

        return "redirect:/admin" + (branchId != null ? "?branchId=" + branchId : "");
    }
}
*/
