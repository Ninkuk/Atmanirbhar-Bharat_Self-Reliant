package com.ninkuk.atmanirbharbharat_tarunmanch.ui.categories

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ninkuk.atmanirbharbharat_tarunmanch.R
import com.ninkuk.atmanirbharbharat_tarunmanch.data.Business
import kotlinx.android.synthetic.main.fragment_category_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class CategoryListFragment : Fragment() {

    private val args: CategoryListFragmentArgs by navArgs()
    private lateinit var categoriesViewModel: CategoriesViewModel

    private val IN: String = "IN"
    private val EN: String = "US"
    private var selectedLanguage: String = EN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_list, container, false)

        categoriesViewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]

        CoroutineScope(Dispatchers.IO).launch {
            if (categoriesViewModel.backupList.isEmpty()) {
                categoriesViewModel.getInitialBusinessList(args.categoryShort)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        categoryTitle.text = args.category

        val businessList: ArrayList<Business> = ArrayList()

        categoryListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = CategoriesRecyclerViewAdapter(businessList)
        categoryListRecyclerView.adapter = adapter

        categoriesViewModel.businessList.observe(viewLifecycleOwner, {
            if (it != null) {
                businessList.clear()
                businessList.addAll(it)
            } else {
                progressIndicator.visibility = View.VISIBLE
            }

            adapter.notifyDataSetChanged()
        })

        categoriesViewModel.listLength.observe(viewLifecycleOwner, {
            progressIndicator.visibility = if (it == 0) {
                View.VISIBLE
            } else {
                View.GONE
            }

            showMoreButton.visibility = if (it < 5) {
                View.GONE
            } else {
                View.VISIBLE
            }
        })

        showMoreButton.setOnClickListener { handleShowMore() }
        backButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun handleShowMore() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                categoriesViewModel.getBusinessList(args.categoryShort)
            } catch (e: Exception) {
                showMoreButton.visibility = View.GONE
            }
        }
    }

    private fun switchKeyboardLanguage() {
        val inputMethodManager: InputMethodManager =
            context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showInputMethodPicker()

        if (selectedLanguage == IN) {
            searchTextField.endIconDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_united_kingdom)
            selectedLanguage = EN
        } else {
            searchTextField.endIconDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_india)
            selectedLanguage = IN
        }
    }
}