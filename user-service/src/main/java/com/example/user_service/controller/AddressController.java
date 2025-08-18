package com.example.user_service.controller;

import com.example.user_service.dto.response.ApiResponse;
import com.example.user_service.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<String>>> getAllProvinces() {
        List<String> provinces = addressService.getProvincesFromCache();
        return ResponseEntity.ok(
                ApiResponse.<List<String>>builder()
                        .code(200)
                        .message("Lấy danh sách tỉnh thành công")
                        .result(provinces)
                        .build()
        );
    }
}
