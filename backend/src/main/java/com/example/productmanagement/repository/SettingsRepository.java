package com.example.productmanagement.repository;

import com.example.productmanagement.entity.Settings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Long> {

  Optional<Settings> findByUsername(String username);
}
