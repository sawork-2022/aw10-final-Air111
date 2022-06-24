package com.micropos.carts.rest;

import com.micropos.api.CartsApi;
import com.micropos.carts.model.Cart;
import com.micropos.carts.model.Product;
import com.micropos.carts.service.CartService;
import com.micropos.dto.ItemDto;
import com.micropos.dto.CartDto;
import com.micropos.carts.mapper.CartMapper;
import com.micropos.carts.model.Item;
import com.micropos.dto.ProductDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api")
public class CartController implements CartsApi {

    private final CartMapper cartMapper;

    private final CartService cartService;

    public CartController(CartService cartService, CartMapper cartMapper) {
        this.cartMapper = cartMapper;
        this.cartService = cartService;
        System.out.println("cart constructed");
    }

    @Override
    @CrossOrigin
    public Mono<ResponseEntity<CartDto>> createCart(Mono<CartDto> cartDto, ServerWebExchange exchange) {

        return cartDto
                .map(cartMapper::toCart)
                .map(cart -> {
                    if (!cartService.addCart(cart))
                        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
                    else
                        return new ResponseEntity<>(cartMapper.toCartDto(cart), HttpStatus.CREATED);
                });
    }

    @Override
    @CrossOrigin
    public Mono<ResponseEntity<Flux<CartDto>>> listCarts(ServerWebExchange exchange) {
        List<CartDto> cartsDto = (List<CartDto>) cartMapper.toCartsDto(cartService.getCarts());
        return Mono.just(new ResponseEntity<>(Flux.fromIterable(cartsDto), HttpStatus.OK));
    }

    @Override
    @CrossOrigin
    public Mono<ResponseEntity<CartDto>> showCartById(Integer cartId, ServerWebExchange exchange) {
        Cart cart = this.cartService.getCart(cartId);
        if (cart == null) {
            return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        CartDto cartDto = cartMapper.toCartDto(cart);
        return Mono.just(new ResponseEntity<>(cartDto, HttpStatus.OK));
    }

//    @Override
//    public ResponseEntity<CartDto> addItemToCart(Integer userId, ItemDto itemDto) {
//        Item item = cartMapper.toItem(itemDto);
//        Cart cart = cartService.getCart(userId);
//        if (cart == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        cartService.add(userId, item);
//        CartDto cartDto = cartMapper.toCartDto(cart);
//        return new ResponseEntity<>(cartDto, HttpStatus.OK);
//    }

    @Override
    @CrossOrigin
    public Mono<ResponseEntity<CartDto>> addProductToCart(Integer cartId,
                                                    Mono<ProductDto> productDto,
                                                    ServerWebExchange exchange
    ) {
        Cart cart = cartService.getCart(cartId);
        if (cart == null) {
            return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        productDto.subscribe(data -> cartService.add(cartId, cartMapper.toProduct(data)));
        CartDto cartDto = cartMapper.toCartDto(cart);
        return Mono.just(new ResponseEntity<>(cartDto, HttpStatus.OK));
    }

    @Override
    @CrossOrigin
    public Mono<ResponseEntity<Double>> showCartTotal(Integer cartId, ServerWebExchange exchange) {
        Cart cart = cartService.getCart(cartId);
        if (cart == null) {
            return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        Double res = cartService.getTotal(cartId);
        return Mono.just(new ResponseEntity<>(res, HttpStatus.OK));
    }
}