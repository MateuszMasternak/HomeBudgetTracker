package com.rainy.homebudgettracker.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDefaultCurrencyRepository extends JpaRepository<DefaultCurrency, String> {
}
