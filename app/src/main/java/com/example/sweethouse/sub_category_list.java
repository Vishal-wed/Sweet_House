package com.example.sweethouse;

import com.google.android.gms.common.internal.Objects;

public class sub_category_list {
    String category_id;
    String item_id;
    String price;
    String quantity;
    String sub_category_desc;

    public String getInStock() {
        return InStock;
    }

    public void setInStock(String inStock) {
        InStock = inStock;
    }

    String InStock;

    public String getTotal_quantity() {
        return total_quantity;
    }

    public void setTotal_quantity(String total_quantity) {
        this.total_quantity = total_quantity;
    }

    String total_quantity;

    public String getSub_category_img() {
        return sub_category_img;
    }

    public void setSub_category_img(String sub_category_img) {
        this.sub_category_img = sub_category_img;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSub_category_desc() {
        return sub_category_desc;
    }

    public void setSub_category_desc(String sub_category_desc) {
        this.sub_category_desc = sub_category_desc;
    }

    public String getSub_category_name() {
        return sub_category_name;
    }

    public void setSub_category_name(String sub_category_name) {
        this.sub_category_name = sub_category_name;
    }

    String sub_category_img;
    String sub_category_name;
}
