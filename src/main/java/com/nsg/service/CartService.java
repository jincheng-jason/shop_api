package com.nsg.service;

import com.nsg.domain.Cart;
import com.nsg.domain.CartItem;
import com.nsg.domain.form.CartItemForm;
import com.nsg.mapper.CartItemMapper;
import com.nsg.mapper.CartMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by lijc on 16/3/28.
 */
@Transactional
@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Nullable
    public Cart getCartByUser(Map<String, Object> map) {
        return cartMapper.getCartByUser(map);
    }

    public void newCartForUser(@NotNull Cart cart) {
        cartMapper.newCartForUser(cart);
    }

    public void batchDeleteCartItems(List<Long> cartItemIds) {
        cartItemMapper.batchDeleteCartItems(cartItemIds);
    }

    public void putInCart(@NotNull CartItem cartItem) {
        cartItemMapper.putInCart(cartItem);
    }

    public int cartCount(long cartId) {
        return cartMapper.cartCount(cartId);
    }

    public void clearCart(long cart_id) {
        cartItemMapper.clearCart(cart_id);
    }

    public int cartCountOfUser(Map<String, Object> map) {
        return cartMapper.cartCountOfUser(map);
    }

    @Nullable
    public CartItem getCartItemByIdAndSku(@NotNull Map<String, Long> map) {
        return cartItemMapper.getCartItemByIdAndSku(map);
    }

    public void increaseBuyNum(@NotNull Map<String, Object> map) {
        cartItemMapper.increaseBuyNum(map);
    }

    public void updateCartItemBuyNum(CartItemForm cartItem) {
        cartItemMapper.updateCartItemBuyNum(cartItem);
    }
}
