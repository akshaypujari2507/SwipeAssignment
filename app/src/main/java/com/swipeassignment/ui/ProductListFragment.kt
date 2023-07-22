package com.swipeassignment.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.swipeassignment.R
import com.swipeassignment.adapter.ProductListAdapter
import com.swipeassignment.databinding.FragmentProductListBinding
import com.swipeassignment.model.ProductDetail
import com.swipeassignment.network.ApiInstance
import com.swipeassignment.network.ApiServices
import com.swipeassignment.repo.Repository
import com.swipeassignment.utilities.ApiResponce
import com.swipeassignment.viewmodel.ProductViewModel
import com.swipeassignment.viewmodel.ProductViewModelFactory

class ProductListFragment : Fragment(R.layout.fragment_product_list) {

    private lateinit var viewModel: ProductViewModel
    private lateinit var productListAdapter: ProductListAdapter
    lateinit var productDetailsItem : List<ProductDetail>

    private var _binding: FragmentProductListBinding? = null
//    private var _binding: Fragment? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductListBinding.bind(view)

        // Add Product button
        binding.fab.setOnClickListener {
            navigateForward()
        }
        initializeViewModel()

        viewModel.getLiveData()
        getProductData()

        productDetailsItem = emptyList()

        searchData()


    }

    private fun initializeViewModel() {
        val apiInstance = ApiInstance.getApiIntence().create(ApiServices::class.java)
        viewModel = ViewModelProvider(this, ProductViewModelFactory(Repository(apiInstance, requireContext())))[ProductViewModel::class.java]
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getProductData() {
        viewModel.liveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is ApiResponce.Loading ->{
                    binding.progressBar.visibility = VISIBLE
                }
                is ApiResponce.Successful ->{
                    productDetailsItem = it.data!!

                    productListAdapter = ProductListAdapter(it.data)
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerView.adapter = productListAdapter
                    binding.recyclerView.setHasFixedSize(true)
                    productListAdapter.notifyDataSetChanged()

                    binding.progressBar.visibility = GONE
                    binding.tvNoRecord.visibility = View.GONE
                }
                is ApiResponce.Error ->{
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = GONE
                }
            }
        })
    }

    private fun searchData() {
        binding.searchData.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val filteredUsers = productDetailsItem.filter { data ->
                    data.product_name.lowercase().contains(p0.toString().lowercase()) || data.product_type.lowercase().contains(p0.toString().lowercase())
                }
                if (filteredUsers.isEmpty()) {
                    binding.tvNoRecord.visibility = View.VISIBLE
                } else {
                    binding.tvNoRecord.visibility = View.GONE
                }

                productListAdapter = ProductListAdapter(filteredUsers)
                binding.recyclerView.adapter = productListAdapter
                binding.recyclerView.setHasFixedSize(true)
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun navigateForward(){
        findNavController().navigate(R.id.action_productListFragment_to_addProductFragment)
    }
    
}