package com.gesture.avius2.customui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class QuestionsPagerAdapter(
    private val lifecycle: Lifecycle,
    private val fragments: List<Fragment>,
    fm: FragmentManager
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

