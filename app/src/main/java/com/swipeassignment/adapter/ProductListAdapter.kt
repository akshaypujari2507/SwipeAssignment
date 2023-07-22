package com.swipeassignment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swipeassignment.databinding.ProductitemBinding
import com.swipeassignment.model.ProductDetail

class ProductListAdapter(private val lisItem : List<ProductDetail>) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>(){
    private lateinit var context : Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        context = parent.context
        return ProductViewHolder(ProductitemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val itemPosition = lisItem[position]
        holder.ProductitemBinding.apply {
            productName.text = "Name : ${itemPosition.product_name.replace("\"","")}"
            productPrise.text = "Prize : ₹${itemPosition.price} + ${itemPosition.tax} % tax"
            productType.text = "Type : ${itemPosition.product_type.replace("\"","")}"

            if (itemPosition.image.isNotEmpty()){
                Glide.with(context).load(itemPosition.image).into(imageProduct)
            }
        }
    }

    override fun getItemCount(): Int {
        return lisItem.size
    }
    class ProductViewHolder(val ProductitemBinding: ProductitemBinding) : RecyclerView.ViewHolder(ProductitemBinding.root)
}