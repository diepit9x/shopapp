package com.project.shopapp.services;

import com.project.shopapp.dtos.CartItemDTO;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    //private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find user with id = "+orderDTO.getUserId()));

        if (orderDTO.getCartItems().size() <= 0 ){
            throw new Exception("There is no product in this order");
        }
        //Convert orderDTO to Order.
        //use mapper lib
//        modelMapper.typeMap(OrderDTO.class, Order.class)
//                .addMappings(mapper -> mapper.skip(Order::setId));
        Order order = Order.builder()
                .user(user)
                .fullName(orderDTO.getFullName())
                .email(orderDTO.getEmail())
                .phoneNumber(orderDTO.getPhoneNumber())
                .address(orderDTO.getAddress())
                .note(orderDTO.getNote())
                .orderDate(LocalDate.now())
                .status(OrderStatus.PENDING)
                .totalMoney(orderDTO.getTotalMoney())
                .shippingMethod(orderDTO.getShippingMethod())
                .shippingAddress(orderDTO.getShippingAddress())
                .paymentMethod(orderDTO.getPaymentMethod())
                .active(true)
                .build();
        //Order order = new Order();

//
//        modelMapper.map(orderDTO, order);
//        order.setUser(user);
//        order.setOrderDate(new Date());
//        order.setStatus(OrderStatus.PENDING);

        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now(): orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())){
            throw new DataNotFoundException("Date must be at least today");
        }
        order.setShippingDate(shippingDate);
        orderRepository.save(order);

        //create orderdetail list
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO cartItemDTO: orderDTO.getCartItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);

            //get product information from cartItemDTO
            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();

            //get product information form db
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new DataNotFoundException("Can not find product with id: " + productId));
            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(quantity);
            orderDetail.setPrice(product.getPrice());

            //add orderDetail into list
            orderDetails.add(orderDetail);

        }
        orderDetailRepository.saveAll(orderDetails);
        return order;
    }

    @Override
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO) throws Exception {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Can not find order with id: " + id));
        User existingUser = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Can not find user with id: "+orderDTO.getUserId()));
        //mapper order -> orderDTO
//        modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper -> mapper.skip(Order::setId));
//        modelMapper.map(orderDTO, order);

        order.setUser(existingUser);
        order.setFullName(orderDTO.getFullName());
        order.setEmail(orderDTO.getEmail());
        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setAddress(orderDTO.getAddress());
        order.setNote(orderDTO.getNote());
        order.setTotalMoney(orderDTO.getTotalMoney());
        order.setShippingMethod(orderDTO.getShippingMethod());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setPaymentMethod(orderDTO.getPaymentMethod());

        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now(): orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())){
            throw new DataNotFoundException("Date must be at least today");
        }
        order.setShippingDate(shippingDate);

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Page<Order> getOrdersByKeyword(String keyword, Pageable pageable) {
        return orderRepository.findByKeyword(keyword, pageable);
    }
}
