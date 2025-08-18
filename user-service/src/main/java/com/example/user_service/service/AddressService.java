package com.example.user_service.service;

import com.example.user_service.service.interfaces.IAddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final IAddressService addressService;

    public AddressService(IAddressService addressService) {
        this.addressService = addressService;
    }

    public List<String> getProvincesFromCache(){
        return addressService.getProvincesFromCache();
    }
}
