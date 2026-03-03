package com.ecommerce.user.service.impl;

// STRANGLER FIG - Phase: 6 - Domain: User / Customer Management
// Migrated from: CustomerDelegate.java, CustomerBO.java
// BC References: BC-001..012

import com.ecommerce.shared.constants.CustomerType;
import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.Customer;
import com.ecommerce.user.mapper.CustomerMapper;
import com.ecommerce.user.repository.CustomerRepository;
import com.ecommerce.user.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerSummaryDto getCustomerSummary(Long customerId) {
        Customer customer = findByIdOrThrow(customerId);
        return customerMapper.toSummaryDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> listCustomers(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return customerRepository.search(query, pageable).map(customerMapper::toDto);
        }
        return customerRepository.findAll(pageable).map(customerMapper::toDto);
    }

    @Override
    @Transactional
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", request.getEmail());
        }
        Customer customer = customerMapper.toEntity(request);
        // BC-003: auto-generate customerNumber
        customer.setCustomerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Customer saved = customerRepository.save(customer);
        log.info("Created customer id={} number={}", saved.getId(), saved.getCustomerNumber());
        return customerMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(Long customerId, UpdateCustomerRequest request) {
        Customer customer = findByIdOrThrow(customerId);
        // BC-004: email uniqueness check (skip if same email)
        if (!customer.getEmail().equalsIgnoreCase(request.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", request.getEmail());
        }
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCustomerType(resolveCustomerType(request.getCustomerType()));
        customer.setProspect(request.isProspect());
        customer.setSpecialAssistanceIndicators(request.getSpecialAssistanceIndicators());
        customer.setRecommendationEngineUrl(request.getRecommendationEngineUrl());
        customer.setCrmUrl(request.getCrmUrl());
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSummaryDto getCustomerSummaryByNumber(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customerNumber", customerNumber));
        return customerMapper.toSummaryDto(customer);
    }

    private Customer findByIdOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    private CustomerType resolveCustomerType(String value) {
        try {
            return CustomerType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return CustomerType.fromLongDescription(value);
        }
    }
}
