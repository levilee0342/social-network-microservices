package com.example.user_service.repository;

import com.example.user_service.entity.UserKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserKeyRepository extends JpaRepository<UserKey, String> {}

