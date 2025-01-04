package com.example.recipes_mobile
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.recipes_mobile.databinding.FragmentAddRecipeBinding

class AddRecipeFragment : Fragment() {

    private lateinit var binding: FragmentAddRecipeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using data binding
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }
}

